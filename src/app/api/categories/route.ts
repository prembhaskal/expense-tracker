import { NextResponse } from "next/server";
import { createSupabaseClientWithToken, getBearerTokenFromRequest } from "@/lib/supabase/api-auth";
import { z } from "zod";

const categoryBodySchema = z.object({
  name: z.string().min(1).max(100),
  color: z.string().optional().nullable(),
});

async function getSupabaseUser(request: Request) {
  const token = getBearerTokenFromRequest(request);
  if (!token) return null;
  const supabase = createSupabaseClientWithToken(token);
  const { data: { user } } = await supabase.auth.getUser();
  return user ? supabase : null;
}

export async function GET(request: Request) {
  const supabase = await getSupabaseUser(request);
  if (!supabase) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  const { data, error } = await supabase
    .from("categories")
    .select("id, name, color, created_at")
    .order("name");
  if (error) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
  return NextResponse.json(data ?? []);
}

export async function POST(request: Request) {
  const supabase = await getSupabaseUser(request);
  if (!supabase) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
  }
  const parsed = categoryBodySchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.message }, { status: 400 });
  }
  const { data, error } = await supabase
    .from("categories")
    .insert({
      name: parsed.data.name,
      color: parsed.data.color ?? null,
    })
    .select("id, name, color, created_at")
    .single();
  if (error) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
  return NextResponse.json(data);
}
