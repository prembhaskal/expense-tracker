import { createClient } from "@supabase/supabase-js";
import { NextResponse } from "next/server";
import { isEmailAllowed } from "@/lib/auth-allowed-emails";

export async function POST(request: Request) {
  let body: { id_token?: string };
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ error: "Invalid JSON body" }, { status: 400 });
  }
  const idToken = body.id_token;
  if (!idToken || typeof idToken !== "string") {
    return NextResponse.json(
      { error: "Missing id_token in body" },
      { status: 400 }
    );
  }

  const supabase = createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
  );

  const { data, error } = await supabase.auth.signInWithIdToken({
    provider: "google",
    token: idToken,
  });

  if (error) {
    return NextResponse.json(
      { error: error.message || "Invalid ID token" },
      { status: 401 }
    );
  }

  const user = data.user;
  if (!user) {
    return NextResponse.json({ error: "No user in session" }, { status: 401 });
  }

  if (!isEmailAllowed(user.email ?? undefined)) {
    return NextResponse.json(
      { error: "Access restricted to allowed emails only" },
      { status: 403 }
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
    user: {
      id: user.id,
      email: user.email,
    },
  });
}
