import { createClient } from "@supabase/supabase-js";
import { NextResponse } from "next/server";

export async function POST(request: Request) {
  let body: { refresh_token?: string };
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ error: "Invalid JSON body" }, { status: 400 });
  }
  const refreshToken = body.refresh_token;
  if (!refreshToken || typeof refreshToken !== "string") {
    return NextResponse.json(
      { error: "Missing refresh_token in body" },
      { status: 400 }
    );
  }

  const supabase = createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
  );

  const { data, error } = await supabase.auth.refreshSession({
    refresh_token: refreshToken,
  });

  if (error) {
    return NextResponse.json(
      { error: error.message || "Invalid refresh token" },
      { status: 401 }
    );
  }

  const session = data.session;
  if (!session?.access_token) {
    return NextResponse.json(
      { error: "No session returned" },
      { status: 500 }
    );
  }

  return NextResponse.json({
    access_token: session.access_token,
    refresh_token: session.refresh_token ?? undefined,
    expires_at: session.expires_at ?? undefined,
  });
}
