import assert from 'node:assert/strict'
import { readFile, readdir } from 'node:fs/promises'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import test from 'node:test'
import { PGlite } from '@electric-sql/pglite'
import { pgcrypto } from '@electric-sql/pglite/contrib/pgcrypto'

const here = dirname(fileURLToPath(import.meta.url))
const migrationsDirectory = resolve(here, '..', 'migrations')

async function createDatabase() {
  const database = new PGlite({ extensions: { pgcrypto } })
  await database.exec(`
    create extension if not exists pgcrypto;
    create schema auth;
    create schema storage;
    create role anon;
    create role authenticated;
    create table auth.users (
      id uuid primary key,
      email text,
      raw_user_meta_data jsonb not null default '{}'::jsonb
    );
    create or replace function auth.uid() returns uuid language sql stable as $$
      select nullif(current_setting('request.jwt.claim.sub', true), '')::uuid
    $$;
    create or replace function auth.jwt() returns jsonb language sql stable as $$
      select coalesce(nullif(current_setting('request.jwt.claims', true), '')::jsonb, '{}'::jsonb)
    $$;
    create table storage.buckets (id text primary key, name text not null, public boolean not null default false);
    create table storage.objects (id uuid primary key default gen_random_uuid(), bucket_id text not null, name text not null);
    create or replace function storage.foldername(name text) returns text[] language sql immutable as $$
      select string_to_array(name, '/')
    $$;
    grant usage on schema auth, storage to anon, authenticated;
    grant execute on function auth.uid(), auth.jwt(), storage.foldername(text) to anon, authenticated;
  `)

  const migrationFiles = (await readdir(migrationsDirectory))
    .filter((name) => /^\d+.*\.sql$/.test(name))
    .sort()
  for (const migrationFile of migrationFiles) {
    const sql = await readFile(resolve(migrationsDirectory, migrationFile), 'utf8')
    await database.exec(sql)
  }
  return database
}

async function createActor(database, id, role) {
  const email = `${role.toLowerCase()}@example.test`
  await database.query(
    `insert into auth.users(id, email, raw_user_meta_data) values ($1, $2, jsonb_build_object('display_name', $3::text))`,
    [id, email, `${role} test user`]
  )
  await database.query(`select set_config('request.jwt.claim.sub', $1::text, false)`, [id])
  await database.query(
    `select set_config('request.jwt.claims', jsonb_build_object('email', $1::text, 'user_metadata', jsonb_build_object('display_name', $2::text))::text, false)`,
    [email, `${role} test user`]
  )
  await database.exec('set role authenticated')
  await database.query(`select public.complete_profile_role($1::public.user_role)`, [role])
  await database.exec('reset role')
}

async function runAs(database, actorId, callback) {
  await database.query(`select set_config('request.jwt.claim.sub', $1::text, false)`, [actorId])
  await database.exec('set role authenticated')
  try {
    return await callback()
  } finally {
    await database.exec('reset role')
  }
}

async function createMarketplaceFixture(database) {
  const organizerId = '10000000-0000-4000-8000-000000000011'
  const participantId = '10000000-0000-4000-8000-000000000012'
  const otherParticipantId = '10000000-0000-4000-8000-000000000013'
  const partnerId = '10000000-0000-4000-8000-000000000014'
  await createActor(database, organizerId, 'ORGANIZER')
  await createActor(database, participantId, 'PARTICIPANT')
  await createActor(database, otherParticipantId, 'PARTICIPANT')
  await createActor(database, partnerId, 'PARTNER')

  const owned = await runAs(database, organizerId, async () => {
    const event = await database.query(`
      insert into public.events(
        owner_id, name, event_type, starts_at, ends_at, timezone_id, address_text,
        latitude, longitude, expected_attendance, status
      ) values (
        $1, 'Circular operations', 'COMMUNITY', now() + interval '1 day', now() + interval '2 days',
        'Asia/Kuala_Lumpur', 'Kuala Lumpur', 3.139000, 101.686900, 50, 'ACTIVE'
      ) returning id
    `, [organizerId])
    const resource = await database.query(`
      insert into public.resource_items(
        origin_event_id, created_by, current_owner_id, title, category, material,
        condition, quantity, unit, status
      ) values ($1, $2, $2, 'Reusable cups', 'SERVICEWARE', 'plastic', 'GOOD', 5, 'ITEM', 'ACTIVE')
      returning id
    `, [event.rows[0].id, organizerId])
    const listing = await database.query(`
      insert into public.marketplace_listings(
        resource_id, seller_id, allowed_actions, published_quantity,
        unit_coin_price_buy, unit_coin_price_rent, default_duration_days,
        terms, status, published_at
      ) values (
        $1, $2, array['BORROW', 'RENT', 'BUY', 'DONATE', 'EXCHANGE']::public.transaction_type[], 5,
        25, 10, 7, 'Return clean and on time.', 'PUBLISHED', now()
      ) returning id
    `, [resource.rows[0].id, organizerId])
    return { eventId: event.rows[0].id, resourceId: resource.rows[0].id, listingId: listing.rows[0].id }
  })

  const programme = await runAs(database, partnerId, async () => {
    const result = await database.query(`
      insert into public.circular_programmes(
        partner_id, name, programme_type, accepted_categories, accepted_materials,
        accepted_conditions, minimum_quantity, maximum_quantity, unit, remaining_capacity,
        coin_direction, unit_coin_amount, address_text, latitude, longitude,
        processing_method, terms, active
      ) values (
        $1, 'Plastic recovery', 'RECYCLE', array['serviceware'], array['plastic'],
        array['GOOD', 'FAIR', 'END_OF_LIFE']::public.resource_condition[], 1, 5, 'ITEM', 10,
        'PARTNER_PAYS_OWNER', 3, 'Kuala Lumpur', 3.140000, 101.690000,
        'Mechanical recycling', 'Clean plastics only.', true
      ) returning id
    `, [partnerId])
    return result.rows[0].id
  })
  return { organizerId, participantId, otherParticipantId, partnerId, programmeId: programme, ...owned }
}

test('all migrations apply and expose the frozen release schema', async () => {
  const database = await createDatabase()
  const expectedTables = [
    'events', 'resource_items', 'resource_photos', 'resource_passports', 'passport_events',
    'marketplace_listings', 'circular_programmes', 'circular_transactions',
    'transaction_allocations', 'transaction_confirmations', 'recoin_wallets', 'recoin_holds',
    'recoin_ledger_entries', 'impact_factors', 'impact_records', 'idempotency_records'
  ]
  const result = await database.query(`
    select table_name from information_schema.tables
    where table_schema = 'public' and table_type = 'BASE TABLE'
  `)
  const actual = new Set(result.rows.map((row) => row.table_name))
  for (const table of expectedTables) assert(actual.has(table), `missing table ${table}`)
  await database.close()
})

test('critical workflow and history tables deny direct authenticated writes', async () => {
  const database = await createDatabase()
  const criticalTables = [
    'circular_transactions', 'transaction_allocations', 'transaction_confirmations',
    'recoin_wallets', 'recoin_holds', 'recoin_ledger_entries', 'impact_records', 'idempotency_records'
  ]
  const result = await database.query(`
    select table_name, privilege_type
    from information_schema.role_table_grants
    where grantee = 'authenticated' and table_schema = 'public'
      and table_name = any($1) and privilege_type in ('INSERT', 'UPDATE', 'DELETE')
  `, [criticalTables])
  assert.deepEqual(result.rows, [])
  await database.close()
})

test('resource lifecycle fields are server-owned and safe archival appends exactly one event', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)

  await assert.rejects(
    runAs(database, fixture.organizerId, () => database.query(`
      update public.resource_items set status = 'RECOVERED' where id = $1
    `, [fixture.resourceId])),
    /RESOURCE_LIFECYCLE_SERVER_ONLY/
  )
  await assert.rejects(
    runAs(database, fixture.organizerId, () => database.query(`
      update public.resource_items set quantity = 4 where id = $1
    `, [fixture.resourceId])),
    /RESOURCE_LIFECYCLE_SERVER_ONLY/
  )
  await assert.rejects(
    runAs(database, fixture.organizerId, () => database.query(`
      update public.resource_items set status = 'ARCHIVED', archived_at = now() where id = $1
    `, [fixture.resourceId])),
    /RESOURCE_LIFECYCLE_SERVER_ONLY/
  )

  const freeResourceId = await runAs(database, fixture.organizerId, async () => {
    const created = await database.query(`
      insert into public.resource_items(
        origin_event_id, created_by, current_owner_id, title, category, material,
        condition, quantity, unit, status
      ) values ($1, $2, $2, 'Archive-safe sign', 'SIGNAGE', 'plastic', 'GOOD', 1, 'ITEM', 'ACTIVE')
      returning id
    `, [fixture.eventId, fixture.organizerId])
    await database.query(`
      update public.resource_items set title = 'Edited sign' where id = $1
    `, [created.rows[0].id])
    await database.query(`
      update public.resource_items set condition = 'FAIR' where id = $1
    `, [created.rows[0].id])
    await database.query(`
      update public.resource_items set condition = 'FAIR' where id = $1
    `, [created.rows[0].id])
    await database.query(`
      update public.resource_items set status = 'ARCHIVED', archived_at = now() where id = $1
    `, [created.rows[0].id])
    await database.query(`
      update public.resource_items set status = 'ARCHIVED', archived_at = now() where id = $1
    `, [created.rows[0].id])
    return created.rows[0].id
  })

  const history = await database.query(`
    select event_type from public.passport_events event
    join public.resource_passports passport on passport.id = event.passport_id
    where passport.resource_id = $1 order by event.occurred_at, event.id
  `, [freeResourceId])
  assert.deepEqual(
    new Set(history.rows.map((row) => row.event_type)),
    new Set(['CREATED', 'CONDITION_CHANGED', 'ARCHIVED'])
  )
  await database.close()
})

test('release enums and idempotency uniqueness match the frozen contract', async () => {
  const database = await createDatabase()
  const statuses = await database.query(`
    select enumlabel from pg_enum e join pg_type t on t.oid = e.enumtypid
    where t.typname = 'transaction_status' order by e.enumsortorder
  `)
  assert.deepEqual(statuses.rows.map((row) => row.enumlabel), [
    'REQUESTED', 'APPROVED', 'IN_TRANSIT', 'ACTIVE', 'RETURN_IN_PROGRESS',
    'COMPLETED', 'REJECTED', 'CANCELLED'
  ])
  const constraint = await database.query(`
    select 1 from pg_constraint
    where conrelid = 'public.idempotency_records'::regclass and contype = 'p'
  `)
  assert.equal(constraint.rows.length, 1)
  await database.close()
})

test('role onboarding grants one wallet and root resources receive one passport', async () => {
  const database = await createDatabase()
  const organizerId = '10000000-0000-4000-8000-000000000001'
  await createActor(database, organizerId, 'ORGANIZER')
  await database.exec('set role authenticated')
  const event = await database.query(`
    insert into public.events(
      owner_id, name, event_type, starts_at, ends_at, timezone_id, address_text,
      latitude, longitude, expected_attendance, status
    ) values (
      $1, 'Release event', 'COMMUNITY', now() + interval '1 day', now() + interval '2 days',
      'Asia/Kuala_Lumpur', 'Kuala Lumpur', 3.139000, 101.686900, 50, 'ACTIVE'
    ) returning id
  `, [organizerId])
  const authority = await database.query(`
    select auth.uid() as actor_id, public.has_role('ORGANIZER') as has_role,
      public.is_event_owner($1::uuid) as owns_event
  `, [event.rows[0].id])
  assert.deepEqual(authority.rows, [{ actor_id: organizerId, has_role: true, owns_event: true }])
  const resource = await database.query(`
    insert into public.resource_items(
      origin_event_id, created_by, current_owner_id, title, category, material,
      condition, quantity, unit, status
    ) values ($1, $2, $2, 'Reusable banner', 'SIGNAGE', 'plastic', 'GOOD', 2, 'ITEM', 'ACTIVE')
    returning id
  `, [event.rows[0].id, organizerId])
  await database.exec('reset role')

  const wallet = await database.query(
    `select available_balance, held_balance from public.recoin_wallets where profile_id = $1`,
    [organizerId]
  )
  assert.deepEqual(wallet.rows, [{ available_balance: 1000, held_balance: 0 }])
  const passport = await database.query(`
    select p.public_token, count(e.id)::int as event_count
    from public.resource_passports p
    join public.passport_events e on e.passport_id = p.id
    where p.resource_id = $1
    group by p.public_token
  `, [resource.rows[0].id])
  assert.equal(passport.rows.length, 1)
  assert.match(passport.rows[0].public_token, /^[A-Za-z0-9_-]{22}$/)
  assert.equal(passport.rows[0].event_count, 1)
  await database.close()
})

test('quantity and active-event constraints reject invented release data', async () => {
  const database = await createDatabase()
  await assert.rejects(
    database.query(`
      insert into public.resource_items(
        origin_event_id, title, category, material, condition, quantity, unit
      ) values ('20000000-0000-4000-8000-000000000001', 'Invalid', 'TEST', 'TEST', 'GOOD', 1.5, 'ITEM')
    `)
  )
  await assert.rejects(
    database.query(`
      insert into public.events(name, starts_at, ends_at, status)
      values ('Invalid active event', now(), now() + interval '1 hour', 'ACTIVE')
    `)
  )
  await database.close()
})

test('listing request, approval, replay, and cancellation preserve allocations and ReCoins', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)
  const requestKey = '30000000-0000-4000-8000-000000000001'
  const request = await runAs(database, fixture.participantId, () => database.query(`
    select public.request_listing_transaction(
      $1::uuid, 'RENT', 2, null, 'Borrow for a workshop', $2::uuid
    ) as result
  `, [fixture.listingId, requestKey]))
  const transactionId = request.rows[0].result.transaction.id

  const replay = await runAs(database, fixture.participantId, () => database.query(`
    select public.request_listing_transaction(
      $1::uuid, 'RENT', 2, null, 'Borrow for a workshop', $2::uuid
    ) as result
  `, [fixture.listingId, requestKey]))
  assert.equal(replay.rows[0].result.transaction.id, transactionId)
  assert.equal((await database.query(
    `select count(*)::int as count from public.circular_transactions where requester_id = $1`,
    [fixture.participantId]
  )).rows[0].count, 1)
  await assert.rejects(
    runAs(database, fixture.participantId, () => database.query(`
      select public.request_listing_transaction($1::uuid, 'RENT', 3, null, 'Changed', $2::uuid)
    `, [fixture.listingId, requestKey])),
    /IDEMPOTENCY_KEY_REUSED/
  )
  await assert.rejects(
    runAs(database, fixture.otherParticipantId, () => database.query(
      `select public.approve_transaction($1::uuid, $2::uuid)`,
      [transactionId, '30000000-0000-4000-8000-000000000002']
    )),
    /DECISION_ACTOR_REQUIRED/
  )

  await runAs(database, fixture.organizerId, () => database.query(
    `select public.approve_transaction($1::uuid, $2::uuid)`,
    [transactionId, '30000000-0000-4000-8000-000000000003']
  ))
  const approved = await database.query(`
    select t.status, t.due_at is not null as has_due_date, a.quantity, h.amount, h.status as hold_status,
      w.available_balance, w.held_balance
    from public.circular_transactions t
    join public.transaction_allocations a on a.transaction_id = t.id
    join public.recoin_holds h on h.transaction_id = t.id
    join public.recoin_wallets w on w.id = h.payer_wallet_id
    where t.id = $1
  `, [transactionId])
  assert.deepEqual(approved.rows, [{
    status: 'APPROVED', has_due_date: true, quantity: '2.000', amount: 20,
    hold_status: 'ACTIVE', available_balance: 980, held_balance: 20
  }])

  await runAs(database, fixture.participantId, () => database.query(
    `select public.cancel_transaction($1::uuid, 'Plans changed', $2::uuid)`,
    [transactionId, '30000000-0000-4000-8000-000000000004']
  ))
  const cancelled = await database.query(`
    select t.status, a.state, h.status as hold_status, w.available_balance, w.held_balance
    from public.circular_transactions t
    join public.transaction_allocations a on a.transaction_id = t.id
    join public.recoin_holds h on h.transaction_id = t.id
    join public.recoin_wallets w on w.id = h.payer_wallet_id
    where t.id = $1
  `, [transactionId])
  assert.deepEqual(cancelled.rows, [{
    status: 'CANCELLED', state: 'RELEASED', hold_status: 'RELEASED',
    available_balance: 1000, held_balance: 0
  }])
  await database.close()
})

test('request validation rejects self-dealing, unavailable quantity, and direct workflow writes', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)
  await assert.rejects(
    runAs(database, fixture.organizerId, () => database.query(`
      select public.request_listing_transaction($1::uuid, 'DONATE', 1, null, null, $2::uuid)
    `, [fixture.listingId, '30000000-0000-4000-8000-000000000011'])),
    /SELF_DEALING_FORBIDDEN/
  )
  await assert.rejects(
    runAs(database, fixture.participantId, () => database.query(`
      select public.request_listing_transaction($1::uuid, 'BUY', 6, null, null, $2::uuid)
    `, [fixture.listingId, '30000000-0000-4000-8000-000000000012'])),
    /QUANTITY_UNAVAILABLE/
  )
  await assert.rejects(
    runAs(database, fixture.participantId, () => database.exec(`insert into public.circular_transactions default values`)),
    /permission denied/
  )
  await database.close()
})

test('programme approval reserves capacity and partner-funded ReCoins, then cancellation releases both', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)
  const requested = await runAs(database, fixture.organizerId, () => database.query(`
    select public.request_programme_transaction($1::uuid, $2::uuid, 2, 'Recover damaged stock', $3::uuid) as result
  `, [fixture.programmeId, fixture.resourceId, '30000000-0000-4000-8000-000000000021']))
  const transactionId = requested.rows[0].result.transaction.id
  await runAs(database, fixture.partnerId, () => database.query(
    `select public.approve_transaction($1::uuid, $2::uuid)`,
    [transactionId, '30000000-0000-4000-8000-000000000022']
  ))
  const reserved = await database.query(`
    select p.remaining_capacity, h.amount, w.available_balance, w.held_balance
    from public.circular_programmes p
    join public.circular_transactions t on t.programme_id = p.id
    join public.recoin_holds h on h.transaction_id = t.id
    join public.recoin_wallets w on w.id = h.payer_wallet_id
    where t.id = $1
  `, [transactionId])
  assert.deepEqual(reserved.rows, [{ remaining_capacity: '8.000', amount: 6, available_balance: 994, held_balance: 6 }])

  await runAs(database, fixture.organizerId, () => database.query(
    `select public.cancel_transaction($1::uuid, 'No longer needed', $2::uuid)`,
    [transactionId, '30000000-0000-4000-8000-000000000023']
  ))
  const released = await database.query(`
    select p.remaining_capacity, h.status, w.available_balance, w.held_balance
    from public.circular_programmes p
    join public.circular_transactions t on t.programme_id = p.id
    join public.recoin_holds h on h.transaction_id = t.id
    join public.recoin_wallets w on w.id = h.payer_wallet_id
    where t.id = $1
  `, [transactionId])
  assert.deepEqual(released.rows, [{ remaining_capacity: '10.000', status: 'RELEASED', available_balance: 1000, held_balance: 0 }])
  await database.close()
})

test('rent handover and return complete once with atomic settlement, reward, impact, and passport effects', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)
  const requested = await runAs(database, fixture.participantId, () => database.query(`
    select public.request_listing_transaction($1::uuid, 'RENT', 2, null, 'Workshop use', $2::uuid) as result
  `, [fixture.listingId, '40000000-0000-4000-8000-000000000001']))
  const transactionId = requested.rows[0].result.transaction.id
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.approve_transaction($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000002']
  ))
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.begin_transaction_handover($1::uuid, 'PRIMARY', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000003']
  ))
  await runAs(database, fixture.participantId, () => database.query(
    `select public.confirm_transaction_receipt($1::uuid, 'PRIMARY', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000004']
  ))
  await runAs(database, fixture.participantId, () => database.query(
    `select public.begin_transaction_return($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000005']
  ))
  await assert.rejects(
    runAs(database, fixture.otherParticipantId, () => database.query(
      `select public.confirm_transaction_return($1::uuid, $2::uuid)`,
      [transactionId, '40000000-0000-4000-8000-000000000006']
    )),
    /ORIGINAL_OWNER_REQUIRED/
  )
  const completionKey = '40000000-0000-4000-8000-000000000007'
  const completed = await runAs(database, fixture.organizerId, () => database.query(
    `select public.confirm_transaction_return($1::uuid, $2::uuid) as result`,
    [transactionId, completionKey]
  ))
  assert.equal(completed.rows[0].result.transaction.status, 'COMPLETED')
  const replay = await runAs(database, fixture.organizerId, () => database.query(
    `select public.confirm_transaction_return($1::uuid, $2::uuid) as result`,
    [transactionId, completionKey]
  ))
  assert.equal(replay.rows[0].result.transaction.id, transactionId)
  assert.equal(replay.rows[0].result.replayed, true)

  const effects = await database.query(`
    select t.status, a.state, h.status as hold_status, r.reuse_count,
      (select available_balance from public.recoin_wallets where profile_id = $2) as participant_balance,
      (select available_balance from public.recoin_wallets where profile_id = $3) as organizer_balance,
      (select count(*)::int from public.impact_records where transaction_id = t.id) as impact_count,
      (select count(*)::int from public.recoin_ledger_entries where transaction_id = t.id and entry_type = 'CIRCULAR_REWARD') as reward_count,
      (select count(*)::int from public.passport_events where transaction_id = t.id and event_type = 'RETURNED') as return_event_count
    from public.circular_transactions t
    join public.transaction_allocations a on a.transaction_id = t.id
    join public.recoin_holds h on h.transaction_id = t.id
    join public.resource_items r on r.id = t.resource_id
    where t.id = $1
  `, [transactionId, fixture.participantId, fixture.organizerId])
  assert.deepEqual(effects.rows, [{
    status: 'COMPLETED', state: 'RELEASED', hold_status: 'SETTLED', reuse_count: 1,
    participant_balance: 985, organizer_balance: 1020, impact_count: 1,
    reward_count: 1, return_event_count: 1
  }])
  await assert.rejects(database.query(
    `update public.circular_transactions set terminal_reason = 'tampered' where id = $1`,
    [transactionId]
  ), /TERMINAL_TRANSACTION_IMMUTABLE/)
  await database.close()
})

test('partial plastic recycling creates a recovered child and one factor-backed impact result', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)
  const kgResource = await runAs(database, fixture.organizerId, async () => {
    const result = await database.query(`
      insert into public.resource_items(
        origin_event_id, created_by, current_owner_id, title, category, material,
        condition, quantity, unit, status
      ) values ($1, $2, $2, 'Acrylic panels', 'SIGNAGE', 'acrylic', 'END_OF_LIFE', 5, 'KG', 'ACTIVE')
      returning id
    `, [fixture.eventId, fixture.organizerId])
    return result.rows[0].id
  })
  const kgProgramme = await runAs(database, fixture.partnerId, async () => {
    const result = await database.query(`
      insert into public.circular_programmes(
        partner_id, name, programme_type, accepted_categories, accepted_materials,
        accepted_conditions, minimum_quantity, maximum_quantity, unit, remaining_capacity,
        coin_direction, unit_coin_amount, address_text, latitude, longitude,
        processing_method, terms, active
      ) values (
        $1, 'Acrylic recovery', 'RECYCLE', array['signage'], array['acrylic'],
        array['END_OF_LIFE']::public.resource_condition[], 1, 5, 'KG', 20,
        'PARTNER_PAYS_OWNER', 3, 'Kuala Lumpur', 3.140000, 101.690000,
        'Mechanical recycling', 'Clean panels only.', true
      ) returning id
    `, [fixture.partnerId])
    return result.rows[0].id
  })
  const requested = await runAs(database, fixture.organizerId, () => database.query(`
    select public.request_programme_transaction($1::uuid, $2::uuid, 2.5, 'Recycle a partial lot', $3::uuid) as result
  `, [kgProgramme, kgResource, '40000000-0000-4000-8000-000000000011']))
  const transactionId = requested.rows[0].result.transaction.id
  await runAs(database, fixture.partnerId, () => database.query(
    `select public.approve_transaction($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000012']
  ))
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.begin_transaction_handover($1::uuid, 'PRIMARY', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000013']
  ))
  const completed = await runAs(database, fixture.partnerId, () => database.query(
    `select public.confirm_transaction_receipt($1::uuid, 'PRIMARY', $2::uuid) as result`,
    [transactionId, '40000000-0000-4000-8000-000000000014']
  ))
  assert.equal(completed.rows[0].result.transaction.status, 'COMPLETED')

  const result = await database.query(`
    select parent.quantity as parent_quantity, parent.status as parent_status,
      child.quantity as child_quantity, child.status as child_status,
      impact.material_diverted_kg, impact.emissions_avoided_kg,
      impact.recoins_transferred, impact.recoins_rewarded,
      (select count(*)::int from public.resource_passports where resource_id = child.id) as child_passports,
      (select available_balance from public.recoin_wallets where profile_id = $2) as owner_balance,
      (select available_balance from public.recoin_wallets where profile_id = $3) as partner_balance
    from public.resource_items parent
    join public.resource_items child on child.parent_resource_id = parent.id
    join public.impact_records impact on impact.transaction_id = $1
    where parent.id = $4
  `, [transactionId, fixture.organizerId, fixture.partnerId, kgResource])
  assert.deepEqual(result.rows, [{
    parent_quantity: '2.500', parent_status: 'ACTIVE', child_quantity: '2.500', child_status: 'RECOVERED',
    material_diverted_kg: '2.500', emissions_avoided_kg: '3.99277065',
    recoins_transferred: 8, recoins_rewarded: 10, child_passports: 1,
    owner_balance: 1018, partner_balance: 992
  }])
  await database.close()
})

test('exchange completes only after both handovers and receipts, then swaps both owners atomically', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)
  const counterResource = await runAs(database, fixture.organizerId, async () => {
    const result = await database.query(`
      insert into public.resource_items(
        origin_event_id, created_by, current_owner_id, title, category, material,
        condition, quantity, unit, status
      ) values ($1, $2, $2, 'Reusable trays', 'SERVICEWARE', 'metal', 'GOOD', 3, 'ITEM', 'ACTIVE')
      returning id
    `, [fixture.eventId, fixture.organizerId])
    return result.rows[0].id
  })
  await database.query(`update public.resource_items set current_owner_id = $1 where id = $2`, [fixture.participantId, counterResource])
  const requested = await runAs(database, fixture.participantId, () => database.query(`
    select public.request_listing_transaction($1::uuid, 'EXCHANGE', 5, $2::uuid, 'Swap serviceware', $3::uuid) as result
  `, [fixture.listingId, counterResource, '40000000-0000-4000-8000-000000000021']))
  const transactionId = requested.rows[0].result.transaction.id
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.approve_transaction($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000022']
  ))
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.begin_transaction_handover($1::uuid, 'PRIMARY', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000023']
  ))
  await runAs(database, fixture.participantId, () => database.query(
    `select public.begin_transaction_handover($1::uuid, 'COUNTER', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000024']
  ))
  const firstReceipt = await runAs(database, fixture.participantId, () => database.query(
    `select public.confirm_transaction_receipt($1::uuid, 'PRIMARY', $2::uuid) as result`,
    [transactionId, '40000000-0000-4000-8000-000000000025']
  ))
  assert.equal(firstReceipt.rows[0].result.transaction.status, 'IN_TRANSIT')
  const secondReceipt = await runAs(database, fixture.organizerId, () => database.query(
    `select public.confirm_transaction_receipt($1::uuid, 'COUNTER', $2::uuid) as result`,
    [transactionId, '40000000-0000-4000-8000-000000000026']
  ))
  assert.equal(secondReceipt.rows[0].result.transaction.status, 'COMPLETED')

  const owners = await database.query(`
    select
      (select current_owner_id from public.resource_items where id = $1) as primary_owner,
      (select current_owner_id from public.resource_items where id = $2) as counter_owner,
      (select count(*)::int from public.transaction_allocations where transaction_id = $3 and state = 'TRANSFERRED') as transferred,
      (select count(*)::int from public.impact_records where transaction_id = $3) as impacts
  `, [fixture.resourceId, counterResource, transactionId])
  assert.deepEqual(owners.rows, [{
    primary_owner: fixture.participantId, counter_owner: fixture.organizerId,
    transferred: 2, impacts: 1
  }])
  await database.close()
})

test('partial buy transfers a child lot, closes the listing, and settles exactly once', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)
  const requested = await runAs(database, fixture.participantId, () => database.query(`
    select public.request_listing_transaction($1::uuid, 'BUY', 2, null, 'Buy two cups', $2::uuid) as result
  `, [fixture.listingId, '40000000-0000-4000-8000-000000000031']))
  const transactionId = requested.rows[0].result.transaction.id
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.approve_transaction($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000032']
  ))
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.begin_transaction_handover($1::uuid, 'PRIMARY', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000033']
  ))
  const completed = await runAs(database, fixture.participantId, () => database.query(
    `select public.confirm_transaction_receipt($1::uuid, 'PRIMARY', $2::uuid) as result`,
    [transactionId, '40000000-0000-4000-8000-000000000034']
  ))
  assert.equal(completed.rows[0].result.transaction.status, 'COMPLETED')
  const result = await database.query(`
    select parent.quantity as source_quantity, child.quantity as child_quantity,
      child.current_owner_id as child_owner, listing.status as listing_status,
      allocation.state as allocation_state, hold.status as hold_status,
      (select available_balance from public.recoin_wallets where profile_id = $2) as buyer_balance,
      (select available_balance from public.recoin_wallets where profile_id = $3) as seller_balance,
      (select count(*)::int from public.impact_records where transaction_id = $1) as impact_count
    from public.resource_items parent
    join public.resource_items child on child.parent_resource_id = parent.id
    join public.marketplace_listings listing on listing.resource_id = parent.id
    join public.transaction_allocations allocation on allocation.transaction_id = $1
    join public.recoin_holds hold on hold.transaction_id = $1
    where parent.id = $4
  `, [transactionId, fixture.participantId, fixture.organizerId, fixture.resourceId])
  assert.deepEqual(result.rows, [{
    source_quantity: '3.000', child_quantity: '2.000', child_owner: fixture.participantId,
    listing_status: 'CLOSED', allocation_state: 'TRANSFERRED', hold_status: 'SETTLED',
    buyer_balance: 950, seller_balance: 1050, impact_count: 1
  }])
  await database.close()
})

test('full repair remains in recovery custody until return and then rewards the owner', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)
  const repairProgramme = await runAs(database, fixture.partnerId, async () => {
    const result = await database.query(`
      insert into public.circular_programmes(
        partner_id, name, programme_type, accepted_categories, accepted_materials,
        accepted_conditions, minimum_quantity, maximum_quantity, unit, remaining_capacity,
        coin_direction, unit_coin_amount, address_text, latitude, longitude,
        processing_method, terms, active
      ) values (
        $1, 'Cup repair', 'REPAIR', array['serviceware'], array['plastic'],
        array['GOOD']::public.resource_condition[], 1, 5, 'ITEM', 10,
        'OWNER_PAYS_PARTNER', 4, 'Kuala Lumpur', 3.140000, 101.690000,
        'Inspection and repair', 'Repairable items only.', true
      ) returning id
    `, [fixture.partnerId])
    return result.rows[0].id
  })
  const requested = await runAs(database, fixture.organizerId, () => database.query(`
    select public.request_programme_transaction($1::uuid, $2::uuid, 5, 'Repair all cups', $3::uuid) as result
  `, [repairProgramme, fixture.resourceId, '40000000-0000-4000-8000-000000000041']))
  const transactionId = requested.rows[0].result.transaction.id
  await runAs(database, fixture.partnerId, () => database.query(
    `select public.approve_transaction($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000042']
  ))
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.begin_transaction_handover($1::uuid, 'PRIMARY', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000043']
  ))
  await runAs(database, fixture.partnerId, () => database.query(
    `select public.confirm_transaction_receipt($1::uuid, 'PRIMARY', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000044']
  ))
  const inRepair = await database.query(`
    select t.status, r.status as resource_status from public.circular_transactions t
    join public.resource_items r on r.id = t.resource_id where t.id = $1
  `, [transactionId])
  assert.deepEqual(inRepair.rows, [{ status: 'ACTIVE', resource_status: 'RECOVERY_IN_PROGRESS' }])
  await runAs(database, fixture.partnerId, () => database.query(
    `select public.begin_transaction_return($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000045']
  ))
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.confirm_transaction_return($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000046']
  ))
  const completed = await database.query(`
    select t.status, r.status as resource_status, r.reuse_count,
      (select available_balance from public.recoin_wallets where profile_id = $2) as owner_balance,
      (select available_balance from public.recoin_wallets where profile_id = $3) as partner_balance,
      impact.recoins_rewarded
    from public.circular_transactions t
    join public.resource_items r on r.id = t.resource_id
    join public.impact_records impact on impact.transaction_id = t.id
    where t.id = $1
  `, [transactionId, fixture.organizerId, fixture.partnerId])
  assert.deepEqual(completed.rows, [{
    status: 'COMPLETED', resource_status: 'ACTIVE', reuse_count: 0,
    owner_balance: 995, partner_balance: 1020, recoins_rewarded: 15
  }])
  await database.close()
})

test('a completion failure rolls back resource, passport, allocation, transaction, and impact changes', async () => {
  const database = await createDatabase()
  const fixture = await createMarketplaceFixture(database)
  const requested = await runAs(database, fixture.participantId, () => database.query(`
    select public.request_listing_transaction($1::uuid, 'RENT', 2, null, null, $2::uuid) as result
  `, [fixture.listingId, '40000000-0000-4000-8000-000000000051']))
  const transactionId = requested.rows[0].result.transaction.id
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.approve_transaction($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000052']
  ))
  await runAs(database, fixture.organizerId, () => database.query(
    `select public.begin_transaction_handover($1::uuid, 'PRIMARY', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000053']
  ))
  await runAs(database, fixture.participantId, () => database.query(
    `select public.confirm_transaction_receipt($1::uuid, 'PRIMARY', $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000054']
  ))
  await runAs(database, fixture.participantId, () => database.query(
    `select public.begin_transaction_return($1::uuid, $2::uuid)`,
    [transactionId, '40000000-0000-4000-8000-000000000055']
  ))
  await database.query(`
    update public.recoin_wallets set held_balance = 0
    where profile_id = $1
  `, [fixture.participantId])
  await assert.rejects(
    runAs(database, fixture.organizerId, () => database.query(
      `select public.confirm_transaction_return($1::uuid, $2::uuid)`,
      [transactionId, '40000000-0000-4000-8000-000000000056']
    )),
    /HELD_BALANCE_CORRUPT/
  )
  const unchanged = await database.query(`
    select t.status, a.state, r.reuse_count, h.status as hold_status,
      (select count(*)::int from public.impact_records where transaction_id = t.id) as impacts,
      (select count(*)::int from public.passport_events where transaction_id = t.id and event_type = 'RETURNED') as returned_events,
      (select count(*)::int from public.transaction_confirmations where transaction_id = t.id and actor_id = $2 and confirmation_type = 'RETURN') as owner_confirmations
    from public.circular_transactions t
    join public.transaction_allocations a on a.transaction_id = t.id
    join public.resource_items r on r.id = t.resource_id
    join public.recoin_holds h on h.transaction_id = t.id
    where t.id = $1
  `, [transactionId, fixture.organizerId])
  assert.deepEqual(unchanged.rows, [{
    status: 'RETURN_IN_PROGRESS', state: 'IN_CUSTODY', reuse_count: 0,
    hold_status: 'ACTIVE', impacts: 0, returned_events: 0, owner_confirmations: 0
  }])
  await database.close()
})
