"use client";

import { createClient } from "@/lib/supabase/client";
import { useTransition } from "react";

export default function LoginForm() {
  const [isPending, startTransition] = useTransition();

  async function signInWithGoogle() {
    const supabase = createClient();
    await supabase.auth.signInWithOAuth({
      provider: "google",
      options: { redirectTo: `${window.location.origin}/auth/callback` },
    });
  }

  return (
    <button
      type="button"
      onClick={() => startTransition(() => signInWithGoogle())}
      disabled={isPending}
      className="touch-target w-full rounded-lg bg-neutral-900 text-white font-medium hover:bg-neutral-800 disabled:opacity-50 dark:bg-neutral-100 dark:text-neutral-900 dark:hover:bg-neutral-200"
    >
      {isPending ? "Signing in…" : "Sign in with Google"}
    </button>
  );
}
