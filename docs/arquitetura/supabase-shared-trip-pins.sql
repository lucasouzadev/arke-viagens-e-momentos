create table public.shared_trip_pins (
  id uuid primary key default gen_random_uuid(),
  trip_id uuid not null references public.shared_trips(id) on delete cascade,
  author_id uuid not null default auth.uid() references auth.users(id),
  title text not null check (length(btrim(title)) between 1 and 120),
  latitude double precision not null check (latitude between -90 and 90),
  longitude double precision not null check (longitude between -180 and 180)
);
create index shared_trip_pins_trip_idx on public.shared_trip_pins(trip_id);
alter table public.shared_trip_pins enable row level security;
revoke all on public.shared_trip_pins from public, anon, authenticated;
grant select on public.shared_trip_pins to authenticated;
grant insert (trip_id,title,latitude,longitude), update (title,latitude,longitude) on public.shared_trip_pins to authenticated;

create policy read_shared_pin on public.shared_trip_pins for select to authenticated
  using (exists (select 1 from public.shared_trips t where t.id = trip_id));
create policy publish_shared_pin on public.shared_trip_pins for insert to authenticated
  with check (author_id = (select auth.uid()) and (
    arke_private.owns_trip(trip_id) or exists (
      select 1 from public.shared_trip_members m
      where m.trip_id = shared_trip_pins.trip_id and m.user_id = (select auth.uid()) and m.role = 'editor'
    )
  ));
create policy edit_shared_pin on public.shared_trip_pins for update to authenticated
  using (arke_private.owns_trip(trip_id) or (
    author_id = (select auth.uid()) and exists (
      select 1 from public.shared_trip_members m
      where m.trip_id = shared_trip_pins.trip_id and m.user_id = (select auth.uid()) and m.role = 'editor'
    )
  ));
