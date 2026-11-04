-- Draft for a disposable development project. Install through a reviewed migration.
begin;

create table public.shared_trips (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null default auth.uid() references auth.users(id),
  title text not null check (length(btrim(title)) between 1 and 120)
);

create table public.shared_trip_members (
  trip_id uuid not null references public.shared_trips(id) on delete cascade,
  user_id uuid not null references auth.users(id),
  role text not null check (role in ('reader', 'editor')),
  primary key (trip_id, user_id)
);
create index shared_trip_members_user_idx on public.shared_trip_members(user_id);

create table public.shared_trip_invites (
  id uuid primary key default gen_random_uuid(),
  trip_id uuid not null references public.shared_trips(id) on delete cascade,
  recipient_id uuid not null references auth.users(id),
  role text not null check (role in ('reader', 'editor')),
  expires_at timestamptz not null default (now() + interval '7 days'),
  accepted_at timestamptz,
  revoked_at timestamptz,
  check (accepted_at is null or revoked_at is null)
);
create index shared_trip_invites_trip_idx on public.shared_trip_invites(trip_id);
create index shared_trip_invites_recipient_idx on public.shared_trip_invites(recipient_id);

alter table public.shared_trips enable row level security;
alter table public.shared_trip_members enable row level security;
alter table public.shared_trip_invites enable row level security;
revoke all on public.shared_trips, public.shared_trip_members, public.shared_trip_invites from public, anon, authenticated;
grant select on public.shared_trips, public.shared_trip_members, public.shared_trip_invites to authenticated;
grant insert (title) on public.shared_trips to authenticated;

create policy read_own_membership on public.shared_trip_members for select to authenticated
  using (user_id = (select auth.uid()));
create policy read_trip on public.shared_trips for select to authenticated
  using (owner_id = (select auth.uid()) or exists (
    select 1 from public.shared_trip_members m
    where m.trip_id = shared_trips.id and m.user_id = (select auth.uid())
  ));
create policy create_trip on public.shared_trips for insert to authenticated
  with check (owner_id = (select auth.uid()) and not coalesce((select auth.jwt()->>'is_anonymous')::boolean, false));
create policy read_invite on public.shared_trip_invites for select to authenticated
  using (recipient_id = (select auth.uid()) or exists (
    select 1 from public.shared_trips t
    where t.id = shared_trip_invites.trip_id and t.owner_id = (select auth.uid())
  ));

commit;
