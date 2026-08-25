-- Android presents these human-facing units. Keep the enum values canonical while accepting all
-- form choices after the client maps its labels before synchronisation.
alter type public.quantity_unit add value if not exists 'SET';
alter type public.quantity_unit add value if not exists 'METRE';
