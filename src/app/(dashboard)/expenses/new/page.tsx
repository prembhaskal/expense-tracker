import { createClient } from "@/lib/supabase/server";
import Link from "next/link";
import NewExpenseForm from "./NewExpenseForm";

export default async function NewExpensePage() {
  const supabase = await createClient();
  const { data: categories } = await supabase
    .from("categories")
    .select("id, name")
    .order("name");

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link
          href="/expenses"
          className="touch-target text-sm text-neutral-500 hover:text-neutral-700"
        >
          ← Back
        </Link>
        <h1 className="text-xl font-semibold">Add expense</h1>
      </div>
      <NewExpenseForm categoryOptions={categories ?? []} />
    </div>
  );
}
