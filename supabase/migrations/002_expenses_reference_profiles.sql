-- So Supabase/PostgREST can join expenses -> profiles (for "added by" display).
-- profiles.id = auth.users.id, so this is equivalent for data.
alter table public.expenses
  drop constraint if exists expenses_user_id_fkey;

alter table public.expenses
  add constraint expenses_user_id_fkey
  foreign key (user_id) references public.profiles(id) on delete cascade;
