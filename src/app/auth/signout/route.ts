import { createClient } from "@/lib/supabase/server";
import { NextResponse } from "next/server";

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const next = searchParams.get("next") ?? "/login";

  const supabase = await createClient();
  await supabase.auth.signOut();

  const url = new URL(next, request.url);
  return NextResponse.redirect(url);
}
