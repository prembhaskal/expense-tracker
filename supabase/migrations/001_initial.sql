-- Profiles (mirror auth.users for display)
create table if not exists public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  email text,
  full_name text,
  avatar_url text,
  created_at timestamptz default now()
);

-- Single account: all categories are shared (family-wide)
create table if not exists public.categories (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  color text,
  created_at timestamptz default now()
);

-- Expenses: shared list, each row stores user_id (who added it)
create table if not exists public.expenses (
  id uuid primary key default gen_random_uuid(),
  amount numeric not null check (amount > 0),
  description text,
  date date not null,
  category_id uuid references public.categories (id) on delete set null,
  user_id uuid not null references auth.users (id) on delete cascade,
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

create index if not exists expenses_date_idx on public.expenses (date);
create index if not exists expenses_category_id_idx on public.expenses (category_id);
create index if not exists expenses_user_id_idx on public.expenses (user_id);

-- RLS: allow authenticated users to read/write (single shared account)
alter table public.profiles enable row level security;
alter table public.categories enable row level security;
alter table public.expenses enable row level security;

create policy "Profiles are viewable by authenticated users"
  on public.profiles for select to authenticated using (true);

create policy "Users can update own profile"
  on public.profiles for update to authenticated using (auth.uid() = id);

create policy "Categories are viewable by authenticated users"
  on public.categories for select to authenticated using (true);

create policy "Categories are insertable by authenticated users"
  on public.categories for insert to authenticated with check (true);

create policy "Categories are updatable by authenticated users"
  on public.categories for update to authenticated using (true);

create policy "Categories are deletable by authenticated users"
  on public.categories for delete to authenticated using (true);

create policy "Expenses are viewable by authenticated users"
  on public.expenses for select to authenticated using (true);

create policy "Expenses are insertable by authenticated users"
  on public.expenses for insert to authenticated with check (auth.uid() = user_id);

create policy "Expenses are updatable by authenticated users"
  on public.expenses for update to authenticated using (true);

create policy "Expenses are deletable by authenticated users"
  on public.expenses for delete to authenticated using (true);

-- Trigger to set updated_at
create or replace function public.set_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

create trigger expenses_updated_at
  before update on public.expenses
  for each row execute function public.set_updated_at();

-- Create profile on signup (optional)
create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.profiles (id, email, full_name, avatar_url)
  values (
    new.id,
    new.email,
    new.raw_user_meta_data->>'full_name',
    new.raw_user_meta_data->>'avatar_url'
  );
  return new;
end;
$$ language plpgsql security definer;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();
