-- Authenticated partner-map discovery. Device coordinates are request-scoped and never stored.

alter table public.circular_programmes
  add constraint programmes_latitude_check check (latitude is null or latitude between -90 and 90),
  add constraint programmes_longitude_check check (longitude is null or longitude between -180 and 180);

create index programmes_map_discovery_idx
  on public.circular_programmes(programme_type, pickup_available, latitude, longitude, updated_at desc)
  where active;

create table public.geocoding_rate_limits (
  user_id uuid primary key references public.profiles(id) on delete cascade,
  window_started_at timestamptz not null,
  request_count integer not null check (request_count >= 0)
);
alter table public.geocoding_rate_limits enable row level security;
revoke all on public.geocoding_rate_limits from anon, authenticated;
grant select, insert, update, delete on public.geocoding_rate_limits to service_role;

create or replace function public.consume_geocoding_quota(
  p_user_id uuid,
  p_max_requests integer default 30
)
returns boolean
language plpgsql
security definer
set search_path = public
as $$
declare
  allowed boolean;
begin
  if p_user_id is null or p_max_requests < 1 then return false; end if;
  insert into public.geocoding_rate_limits(user_id, window_started_at, request_count)
  values (p_user_id, now(), 1)
  on conflict (user_id) do update set
    window_started_at = case
      when geocoding_rate_limits.window_started_at <= now() - interval '1 minute' then now()
      else geocoding_rate_limits.window_started_at
    end,
    request_count = case
      when geocoding_rate_limits.window_started_at <= now() - interval '1 minute' then 1
      else geocoding_rate_limits.request_count + 1
    end
  returning request_count <= p_max_requests into allowed;
  return allowed;
end;
$$;
revoke all on function public.consume_geocoding_quota(uuid, integer) from public;
grant execute on function public.consume_geocoding_quota(uuid, integer) to service_role;

create or replace function public.find_partner_programmes(
  p_resource_id uuid default null,
  p_origin_latitude double precision default null,
  p_origin_longitude double precision default null,
  p_material text default null,
  p_programme_types public.programme_type[] default null,
  p_max_distance_km double precision default null,
  p_pickup_only boolean default false,
  p_limit integer default 100,
  p_offset integer default 0
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  actor_id uuid := public.require_verified_actor();
  resource_row public.resource_items;
  event_row public.events;
  origin_lat double precision;
  origin_lon double precision;
  origin_address text;
  origin_source text := 'NONE';
  effective_material text := nullif(btrim(p_material), '');
  candidate_rows jsonb;
  exclusion_rows jsonb;
  eligible_count integer;
begin
  if (p_origin_latitude is null) <> (p_origin_longitude is null) then
    raise exception using errcode = '22023', message = 'ORIGIN_COORDINATES_INCOMPLETE';
  end if;
  if p_origin_latitude is not null and (
    p_origin_latitude not between -90 and 90 or p_origin_longitude not between -180 and 180
  ) then
    raise exception using errcode = '22023', message = 'ORIGIN_COORDINATES_INVALID';
  end if;
  if p_max_distance_km is not null and (p_max_distance_km <= 0 or p_max_distance_km > 500) then
    raise exception using errcode = '22023', message = 'DISTANCE_FILTER_INVALID';
  end if;
  if p_limit not between 1 and 100 or p_offset < 0 then
    raise exception using errcode = '22023', message = 'PAGINATION_INVALID';
  end if;

  if p_resource_id is not null then
    select * into strict resource_row from public.resource_items where id = p_resource_id;
    if resource_row.current_owner_id <> actor_id then
      raise exception using errcode = '42501', message = 'RESOURCE_OWNER_REQUIRED';
    end if;
    effective_material := resource_row.material;
  end if;

  if p_origin_latitude is not null then
    origin_lat := p_origin_latitude;
    origin_lon := p_origin_longitude;
    origin_source := 'DEVICE';
  elsif p_resource_id is not null and resource_row.latitude is not null then
    origin_lat := resource_row.latitude;
    origin_lon := resource_row.longitude;
    origin_address := resource_row.address_text;
    origin_source := 'RESOURCE';
  elsif p_resource_id is not null then
    select * into strict event_row from public.events where id = resource_row.origin_event_id;
    if event_row.latitude is not null then
      origin_lat := event_row.latitude;
      origin_lon := event_row.longitude;
      origin_address := event_row.address_text;
      origin_source := 'EVENT';
    end if;
  end if;

  with distanced as (
    select
      programme.*,
      case when origin_lat is null then null else
        6371.0088 * 2 * asin(sqrt(least(1.0,
          power(sin(radians(programme.latitude::double precision - origin_lat) / 2), 2) +
          cos(radians(origin_lat)) * cos(radians(programme.latitude::double precision)) *
          power(sin(radians(programme.longitude::double precision - origin_lon) / 2), 2)
        )))
      end as distance_km
    from public.circular_programmes programme
    where programme.active
  ), evaluated as (
    select
      distanced.*,
      case
        when distanced.latitude is null or distanced.longitude is null then 'PROGRAMME_LOCATION_MISSING'
        when p_programme_types is not null and cardinality(p_programme_types) > 0 and
          not distanced.programme_type = any(p_programme_types) then 'TYPE_FILTERED'
        when p_pickup_only and not distanced.pickup_available then 'PICKUP_UNAVAILABLE'
        when effective_material is not null and cardinality(distanced.accepted_materials) > 0 and not (
          lower(effective_material) = any(select lower(value) from unnest(distanced.accepted_materials) value)
        ) then 'MATERIAL_NOT_ACCEPTED'
        when p_max_distance_km is not null and (distance_km is null or distance_km > p_max_distance_km) then 'OUTSIDE_DISTANCE'
        when p_resource_id is null then null
        when resource_row.status <> 'ACTIVE' then 'RESOURCE_NOT_ACTIVE'
        when distanced.partner_id = actor_id then 'SELF_DEALING_FORBIDDEN'
        when distanced.unit is not null and distanced.unit <> resource_row.unit then 'UNIT_NOT_ACCEPTED'
        when distanced.minimum_quantity is not null and resource_row.quantity < distanced.minimum_quantity then 'BELOW_MINIMUM_QUANTITY'
        when distanced.maximum_quantity is not null and resource_row.quantity > distanced.maximum_quantity then 'ABOVE_MAXIMUM_QUANTITY'
        when distanced.remaining_capacity is not null and resource_row.quantity > distanced.remaining_capacity then 'CAPACITY_UNAVAILABLE'
        when cardinality(distanced.accepted_categories) > 0 and not (
          lower(resource_row.category) = any(select lower(value) from unnest(distanced.accepted_categories) value)
        ) then 'CATEGORY_NOT_ACCEPTED'
        when cardinality(distanced.accepted_materials) > 0 and not (
          lower(resource_row.material) = any(select lower(value) from unnest(distanced.accepted_materials) value)
        ) then 'MATERIAL_NOT_ACCEPTED'
        when not resource_row.condition = any(distanced.accepted_conditions) then 'CONDITION_NOT_ACCEPTED'
        else null
      end as exclusion_code,
      case when p_resource_id is null then null else
        (case when cardinality(distanced.accepted_materials) = 0 then 15 else 30 end) +
        (case when cardinality(distanced.accepted_categories) = 0 then 10 else 20 end) +
        (case when distance_km is null or distance_km > 50 then 0 when distance_km <= 5 then 25 when distance_km <= 15 then 18 else 8 end) +
        (case when distanced.remaining_capacity is null or distanced.remaining_capacity >= resource_row.quantity * 2 then 15 else 8 end) +
        (case when p_pickup_only and distanced.pickup_available then 10 else 0 end)
      end as match_score
    from distanced
  )
  select
    coalesce((
      select jsonb_agg(jsonb_build_object(
        'id', page.id,
        'partner_id', page.partner_id,
        'name', page.name,
        'programme_type', page.programme_type,
        'accepted_categories', page.accepted_categories,
        'accepted_materials', page.accepted_materials,
        'accepted_conditions', page.accepted_conditions,
        'minimum_quantity', page.minimum_quantity,
        'maximum_quantity', page.maximum_quantity,
        'unit', page.unit,
        'remaining_capacity', page.remaining_capacity,
        'coin_direction', page.coin_direction,
        'unit_coin_amount', page.unit_coin_amount,
        'pickup_available', page.pickup_available,
        'address_text', page.address_text,
        'latitude', page.latitude,
        'longitude', page.longitude,
        'processing_method', page.processing_method,
        'terms', page.terms,
        'distance_km', page.distance_km,
        'score', page.match_score,
        'reasons', case when p_resource_id is null then jsonb_build_array('Active partner programme') else jsonb_build_array(
          case when cardinality(page.accepted_materials) = 0 then 'Accepts all materials' else 'Accepts ' || resource_row.material end,
          case when cardinality(page.accepted_categories) = 0 then 'Accepts all categories' else 'Accepts ' || resource_row.category end,
          case when page.distance_km is null then 'Distance unavailable' else round(page.distance_km::numeric, 1)::text || ' km away' end,
          case when page.pickup_available then 'Pickup available' else 'Drop-off required' end
        ) end,
        'created_at_ms', floor(extract(epoch from page.created_at) * 1000)::bigint,
        'updated_at_ms', floor(extract(epoch from page.updated_at) * 1000)::bigint
      ) order by page.match_score desc nulls last, page.distance_km nulls last, lower(page.name), page.id)
      from (
        select * from evaluated where exclusion_code is null
        order by match_score desc nulls last, distance_km nulls last, lower(name), id
        limit p_limit offset p_offset
      ) page
    ), '[]'::jsonb),
    coalesce((
      select jsonb_object_agg(exclusion_code, excluded_count)
      from (
        select exclusion_code, count(*)::integer as excluded_count
        from evaluated where exclusion_code is not null group by exclusion_code
      ) excluded
    ), '{}'::jsonb),
    (select count(*)::integer from evaluated where exclusion_code is null)
  into candidate_rows, exclusion_rows, eligible_count;

  return jsonb_build_object(
    'origin_source', origin_source,
    'origin_address', origin_address,
    'origin_latitude', origin_lat,
    'origin_longitude', origin_lon,
    'candidates', candidate_rows,
    'exclusion_counts', exclusion_rows,
    'next_offset', case when p_offset + p_limit < eligible_count then p_offset + p_limit else null end
  );
end;
$$;

revoke all on function public.find_partner_programmes(
  uuid, double precision, double precision, text, public.programme_type[], double precision, boolean, integer, integer
) from public;
grant execute on function public.find_partner_programmes(
  uuid, double precision, double precision, text, public.programme_type[], double precision, boolean, integer, integer
) to authenticated;
