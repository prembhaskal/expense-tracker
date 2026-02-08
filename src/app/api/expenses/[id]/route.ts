import { NextResponse } from "next/server";
import { createSupabaseClientWithToken, getBearerTokenFromRequest } from "@/lib/supabase/api-auth";
import { z } from "zod";

const expenseBodySchema = z.object({
  amount: z.coerce.number().positive(),
  description: z.string().optional(),
  date: z.string().min(1),
  category_id: z.string().uuid().optional().nullable(),
});

async function getSupabaseUser(request: Request) {
  const token = getBearerTokenFromRequest(request);
  if (!token) return null;
  const supabase = createSupabaseClientWithToken(token);
  const { data: { user } } = await supabase.auth.getUser();
  return user ? { supabase, user } : null;
}

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const ctx = await getSupabaseUser(request);
  if (!ctx) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  const { id } = await params;
  const { data, error } = await ctx.supabase
    .from("expenses")
    .select("id, amount, description, date, category_id, user_id, created_at, updated_at, categories(name), profiles(full_name)")
    .eq("id", id)
    .single();

  if (error) {
    if (error.code === "PGRST116") {
      return NextResponse.json({ error: "Not found" }, { status: 404 });
    }
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
  return NextResponse.json(data);
}

export async function PATCH(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const ctx = await getSupabaseUser(request);
  if (!ctx) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  const { id } = await params;
  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ error: "Invalid JSON" }, { status: 400 });
  }
  const parsed = expenseBodySchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.message }, { status: 400 });
  }

  const { data, error } = await ctx.supabase
    .from("expenses")
    .update({
      amount: parsed.data.amount,
      description: parsed.data.description ?? null,
      date: parsed.data.date,
      category_id: parsed.data.category_id ?? null,
    })
    .eq("id", id)
    .select("id, amount, description, date, category_id, user_id, created_at, updated_at")
    .single();

  if (error) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
  return NextResponse.json(data);
}

export async function DELETE(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const ctx = await getSupabaseUser(request);
  if (!ctx) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  const { id } = await params;
  const { error } = await ctx.supabase.from("expenses").delete().eq("id", id);
  if (error) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
  return new NextResponse(null, { status: 204 });
}
