"use server";

import { revalidatePath } from "next/cache";
import { createClient } from "@/lib/supabase/server";
import { z } from "zod";

const expenseSchema = z.object({
  amount: z.coerce.number().positive(),
  description: z.string().optional(),
  date: z.string().min(1),
  category_id: z.string().uuid().optional().nullable(),
});

export async function addExpense(formData: FormData) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) throw new Error("Unauthorized");

  const categoryIdRaw = formData.get("category_id");
  const raw = {
    amount: formData.get("amount"),
    description: formData.get("description") || undefined,
    date: formData.get("date"),
    category_id:
      categoryIdRaw && String(categoryIdRaw).trim()
        ? categoryIdRaw
        : null,
  };
  const parsed = expenseSchema.safeParse(raw);
  if (!parsed.success) throw new Error(parsed.error.message);

  const { data: inserted, error } = await supabase
    .from("expenses")
    .insert({
      amount: parsed.data.amount,
      description: parsed.data.description ?? null,
      date: parsed.data.date,
      category_id: parsed.data.category_id ?? null,
      user_id: user.id,
    })
    .select("id")
    .single();
  if (error) throw new Error(error.message);
  console.log("[addExpense] inserted id:", inserted?.id, "user_id:", user.id);
  revalidatePath("/dashboard");
  revalidatePath("/expenses");
  revalidatePath("/reports");
  console.log("[addExpense] revalidatePath done for /dashboard, /expenses, /reports");
}

export async function updateExpense(id: string, formData: FormData) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) throw new Error("Unauthorized");

  const raw = {
    amount: formData.get("amount"),
    description: formData.get("description") || undefined,
    date: formData.get("date"),
    category_id: formData.get("category_id") || null,
  };
  const parsed = expenseSchema.safeParse(raw);
  if (!parsed.success) throw new Error(parsed.error.message);

  const { error } = await supabase
    .from("expenses")
    .update({
      amount: parsed.data.amount,
      description: parsed.data.description ?? null,
      date: parsed.data.date,
      category_id: parsed.data.category_id || null,
    })
    .eq("id", id);
  if (error) throw new Error(error.message);
  revalidatePath("/dashboard");
  revalidatePath("/expenses");
  revalidatePath(`/expenses/${id}/edit`);
  revalidatePath("/reports");
}

export async function deleteExpense(id: string) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) throw new Error("Unauthorized");

  const { error } = await supabase.from("expenses").delete().eq("id", id);
  if (error) throw new Error(error.message);
  revalidatePath("/dashboard");
  revalidatePath("/expenses");
  revalidatePath("/reports");
}
