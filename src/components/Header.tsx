import Link from "next/link";
import { createClient } from "@/lib/supabase/server";
import SignOutButton from "./SignOutButton";

export default async function Header() {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();

  return (
    <header className="flex items-center justify-between gap-4 py-3 px-4">
      <Link href="/dashboard" className="font-semibold text-lg">
        Expense Tracker
      </Link>
      <div className="flex items-center gap-3 shrink-0">
        {user && (
          <span className="text-sm text-neutral-500 truncate max-w-[140px] hidden sm:inline">
            {user.email}
          </span>
        )}
        <SignOutButton />
      </div>
    </header>
  );
}
