import { createClient } from "@/lib/supabase/server";
import { notFound } from "next/navigation";
import Link from "next/link";
import EditExpenseForm from "./EditExpenseForm";

export default async function EditExpensePage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const supabase = await createClient();
  const { data: expense } = await supabase
    .from("expenses")
    .select("id, amount, description, date, category_id")
    .eq("id", id)
    .single();
  const { data: categories } = await supabase
    .from("categories")
    .select("id, name")
    .order("name");

  if (!expense) notFound();

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link
          href="/expenses"
          className="touch-target text-sm text-neutral-500 hover:text-neutral-700"
        >
          ← Back
        </Link>
        <h1 className="text-xl font-semibold">Edit expense</h1>
      </div>
      <EditExpenseForm
        expense={expense}
        categoryOptions={categories ?? []}
      />
    </div>
  );
}
