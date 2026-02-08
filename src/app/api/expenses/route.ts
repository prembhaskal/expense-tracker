import { NextResponse } from "next/server";
import { createSupabaseClientWithToken, getBearerTokenFromRequest } from "@/lib/supabase/api-auth";
import { z } from "zod";

const expenseBodySchema = z.object({
  amount: z.coerce.number().positive(),
  description: z.string().optional(),
  date: z.string().min(1),
  category_id: z.string().uuid().optional().nullable(),
});

export async function GET(request: Request) {
  const token = getBearerTokenFromRequest(request);
  if (!token) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  const supabase = createSupabaseClientWithToken(token);
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) {
    return NextResponse.json({ error: "Invalid token" }, { status: 401 });
  }

  const { searchParams } = new URL(request.url);
  const limit = Math.min(Number(searchParams.get("limit")) || 100, 500);
  const offset = Number(searchParams.get("offset")) || 0;
  const fromDate = searchParams.get("fromDate") || undefined;
  const toDate = searchParams.get("toDate") || undefined;

  let query = supabase
    .from("expenses")
    .select("id, amount, description, date, category_id, user_id, created_at, updated_at, categories(name), profiles(full_name)")
    .order("date", { ascending: false })
    .range(offset, offset + limit - 1);

  if (fromDate) query = query.gte("date", fromDate);
  if (toDate) query = query.lte("date", toDate);

  const { data, error } = await query;
  if (error) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
  return NextResponse.json(data ?? []);
}

export async function POST(request: Request) {
  const token = getBearerTokenFromRequest(request);
  if (!token) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  const supabase = createSupabaseClientWithToken(token);
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) {
    return NextResponse.json({ error: "Invalid token" }, { status: 401 });
  }

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

  const { data: row, error } = await supabase
    .from("expenses")
    .insert({
      amount: parsed.data.amount,
      description: parsed.data.description ?? null,
      date: parsed.data.date,
      category_id: parsed.data.category_id ?? null,
      user_id: user.id,
    })
    .select("id, amount, description, date, category_id, user_id, created_at, updated_at")
    .single();

  if (error) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
  return NextResponse.json(row);
}
