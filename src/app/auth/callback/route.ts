import { createClient } from "@/lib/supabase/server";
import { NextResponse } from "next/server";
import { isEmailAllowed } from "@/lib/auth-allowed-emails";

export async function GET(request: Request) {
  const { searchParams, origin } = new URL(request.url);
  const code = searchParams.get("code");
  const next = searchParams.get("next") ?? "/dashboard";

  if (code) {
    const supabase = await createClient();
    const { data, error } = await supabase.auth.exchangeCodeForSession(code);
    if (!error && data.user) {
      if (!isEmailAllowed(data.user.email ?? undefined)) {
        await supabase.auth.signOut();
        return NextResponse.redirect(`${origin}/login?error=restricted`);
      }
      return NextResponse.redirect(`${origin}${next}`);
    }
  }

  return NextResponse.redirect(`${origin}/login?error=auth`);
}
