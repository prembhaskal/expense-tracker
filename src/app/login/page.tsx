import { createClient } from "@/lib/supabase/server";
import LoginForm from "./LoginForm";

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ error?: string }>;
}) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (user) {
    return null; // middleware redirects to /dashboard
  }

  const { error } = await searchParams;

  return (
    <main className="min-h-screen flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-sm space-y-8">
        <h1 className="text-2xl font-semibold text-center">Expense Tracker</h1>
        <p className="text-center text-neutral-500 text-sm">
          Sign in with Google to track family expenses.
        </p>
        {error === "auth" && (
          <p className="text-center text-sm text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/30 px-3 py-2 rounded">
            Sign-in failed. Check that Google is enabled in Supabase and redirect URL is set (see README).
          </p>
        )}
        <LoginForm />
      </div>
    </main>
  );
}
