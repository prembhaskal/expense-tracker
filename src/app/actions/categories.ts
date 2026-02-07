"use server";

import { revalidatePath } from "next/cache";
import { createClient } from "@/lib/supabase/server";
import { z } from "zod";

const categorySchema = z.object({
  name: z.string().min(1).max(100),
  color: z.string().optional().nullable(),
});

export async function addCategory(formData: FormData) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) throw new Error("Unauthorized");

  const raw = {
    name: formData.get("name"),
    color: formData.get("color") || null,
  };
  const parsed = categorySchema.safeParse(raw);
  if (!parsed.success) throw new Error(parsed.error.message);

  const { error } = await supabase.from("categories").insert({
    name: parsed.data.name,
    color: parsed.data.color ?? null,
  });
  if (error) throw new Error(error.message);
  revalidatePath("/categories");
  revalidatePath("/dashboard");
  revalidatePath("/expenses");
  revalidatePath("/reports");
}

export async function updateCategory(id: string, formData: FormData) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) throw new Error("Unauthorized");

  const raw = {
    name: formData.get("name"),
    color: formData.get("color") || null,
  };
  const parsed = categorySchema.safeParse(raw);
  if (!parsed.success) throw new Error(parsed.error.message);

  const { error } = await supabase
    .from("categories")
    .update({
      name: parsed.data.name,
      color: parsed.data.color ?? null,
    })
    .eq("id", id);
  if (error) throw new Error(error.message);
  revalidatePath("/categories");
  revalidatePath("/dashboard");
  revalidatePath("/expenses");
  revalidatePath("/reports");
}

export async function deleteCategory(id: string) {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) throw new Error("Unauthorized");

  const { error } = await supabase.from("categories").delete().eq("id", id);
  if (error) throw new Error(error.message);
  revalidatePath("/categories");
  revalidatePath("/dashboard");
  revalidatePath("/expenses");
  revalidatePath("/reports");
}
