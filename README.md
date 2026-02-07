# Expense Tracker

Shared family expense tracker: multiple users, Google sign-in, single shared ledger with per-expense attribution.

## Setup

1. **Install dependencies**

   ```bash
   npm install
   ```

2. **Environment**

   Copy `.env.example` to `.env.local` and set:

   - `NEXT_PUBLIC_SUPABASE_URL` – your Supabase project URL
   - `NEXT_PUBLIC_SUPABASE_ANON_KEY` – your Supabase anon key
   - `ALLOWED_EMAILS` – comma-separated list of Google emails that can sign in. If unset, no one can sign in.

3. **Supabase**

   - In the Supabase SQL Editor, run the migration: `supabase/migrations/001_initial.sql`
   - **Google OAuth (required for sign-in):**
     1. **Google Cloud Console:** Create a project (or use existing) → APIs & Services → Credentials → Create OAuth 2.0 Client ID → Application type: **Web application**. Add authorized redirect URI: `https://rtqcaqblvzcqguxlhxez.supabase.co/auth/v1/callback` (replace with your Supabase project URL: `https://<project-ref>.supabase.co/auth/v1/callback`). Copy the Client ID and Client Secret.
     2. **Supabase Dashboard:** Authentication → Providers → **Google** → Enable, paste Client ID and Client Secret, Save.
     3. **Supabase:** Authentication → URL Configuration → **Redirect URLs** → add `http://localhost:3000/auth/callback` (and your production URL when deploying). Save.
   - If you get **HTTP 400** on sign-in, the cause is usually: Google provider not enabled, wrong/missing Client ID or Secret, or the Supabase redirect URL above not in the allow list.

4. **Run locally**

   ```bash
   npm run dev
   ```

   Open [http://localhost:3000](http://localhost:3000). Sign in with Google, then add categories and expenses.

## Deploy (Vercel)

- Connect the repo to Vercel and set `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`, and `ALLOWED_EMAILS` (comma-separated family emails) in project environment variables.
- Add your production URL (e.g. `https://your-app.vercel.app/auth/callback`) to Supabase Redirect URLs.

## Tech

- Next.js 15 (App Router), React, TypeScript, Tailwind CSS
- Supabase (Postgres, Auth with Google)
- Single shared account; each expense stores `user_id` for who added it
