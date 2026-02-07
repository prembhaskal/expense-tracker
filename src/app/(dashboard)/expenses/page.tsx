import { createClient } from "@/lib/supabase/server";
import Link from "next/link";
import DeleteExpenseButton from "./DeleteExpenseButton";

export const dynamic = "force-dynamic";

export default async function ExpensesPage() {
  const supabase = await createClient();
  const { data: expenses } = await supabase
    .from("expenses")
    .select(
      "id, amount, description, date, category_id, categories(name), profiles(full_name)"
    )
    .order("date", { ascending: false });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">Expenses</h1>
        <Link
          href="/expenses/new"
          className="touch-target px-4 py-2 rounded-lg bg-neutral-900 text-white text-sm font-medium hover:bg-neutral-800 dark:bg-neutral-100 dark:text-neutral-900"
        >
          Add expense
        </Link>
      </div>
      <ul className="space-y-2">
        {expenses?.length
          ? expenses.map((e: Record<string, unknown>) => (
              <li
                key={e.id as string}
                className="flex items-center justify-between gap-4 py-3 px-4 rounded-lg bg-neutral-100 dark:bg-neutral-800"
              >
                <div className="min-w-0 flex-1">
                  <p className="font-medium">
                    {(e.amount as number).toFixed(2)}
                    {e.description ? ` · ${String(e.description)}` : ""}
                  </p>
                  <p className="text-xs text-neutral-500">
                    {new Date(e.date as string).toLocaleDateString()}
                    {(e.categories as { name?: string } | null)?.name &&
                      ` · ${(e.categories as { name: string }).name}`}
                    {(e.profiles as { full_name?: string } | null)?.full_name &&
                      ` · ${(e.profiles as { full_name: string }).full_name}`}
                  </p>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <Link
                    href={`/expenses/${e.id}/edit`}
                    className="touch-target text-sm text-blue-600 dark:text-blue-400"
                  >
                    Edit
                  </Link>
                  <DeleteExpenseButton id={e.id as string} />
                </div>
              </li>
            ))
          : (
              <p className="text-neutral-500 text-sm py-8 text-center">
                No expenses yet.
              </p>
            )}
      </ul>
    </div>
  );
}
