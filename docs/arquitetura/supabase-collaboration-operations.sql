create schema arke_private;
revoke all on schema arke_private from public;
grant usage on schema arke_private to authenticated;

create function arke_private.owns_trip(target uuid) returns boolean
language sql stable security definer set search_path = ''
as $$
  select exists (select 1 from public.shared_trips where id = target and owner_id = auth.uid());
$$;
revoke all on function arke_private.owns_trip(uuid) from public;
grant execute on function arke_private.owns_trip(uuid) to authenticated;

create policy owner_reads_members on public.shared_trip_members for select to authenticated
  using (arke_private.owns_trip(trip_id));
grant delete on public.shared_trip_members to authenticated;
create policy owner_removes_members on public.shared_trip_members for delete to authenticated
  using (arke_private.owns_trip(trip_id));

grant insert (trip_id, recipient_id, role), update (revoked_at) on public.shared_trip_invites to authenticated;
create policy owner_creates_invite on public.shared_trip_invites for insert to authenticated
  with check (arke_private.owns_trip(trip_id) and recipient_id <> (select auth.uid()));
create policy owner_revokes_invite on public.shared_trip_invites for update to authenticated
  using (arke_private.owns_trip(trip_id) and accepted_at is null and revoked_at is null)
  with check (arke_private.owns_trip(trip_id) and revoked_at is not null and accepted_at is null);

create function arke_private.accept_invite(invite_id uuid) returns uuid
language plpgsql security definer set search_path = ''
as $$
declare
  invitation public.shared_trip_invites%rowtype;
begin
  update public.shared_trip_invites
  set accepted_at = clock_timestamp()
  where id = invite_id and recipient_id = auth.uid()
    and not coalesce((auth.jwt()->>'is_anonymous')::boolean, false)
    and accepted_at is null and revoked_at is null and expires_at > clock_timestamp()
  returning * into invitation;

  if not found then
    raise exception 'Invitation unavailable' using errcode = '42501';
  end if;

  insert into public.shared_trip_members (trip_id, user_id, role)
  values (invitation.trip_id, invitation.recipient_id, invitation.role)
  on conflict (trip_id, user_id) do nothing;
  return invitation.trip_id;
end;
$$;
revoke all on function arke_private.accept_invite(uuid) from public;
grant execute on function arke_private.accept_invite(uuid) to authenticated;

create function public.accept_shared_trip_invite(invite_id uuid) returns uuid
language sql security invoker set search_path = ''
as $$ select arke_private.accept_invite(invite_id); $$;
revoke all on function public.accept_shared_trip_invite(uuid) from public, anon, authenticated;
grant execute on function public.accept_shared_trip_invite(uuid) to authenticated;
