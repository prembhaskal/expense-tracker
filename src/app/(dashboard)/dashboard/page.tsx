import { createClient } from "@/lib/supabase/server";
import Link from "next/link";
import AddExpenseForm from "./AddExpenseForm";

export const dynamic = "force-dynamic";

export default async function DashboardPage() {
  const supabase = await createClient();
  const { data: categories } = await supabase
    .from("categories")
    .select("id, name")
    .order("name");
  const {
    data: expenses,
    error: expensesError,
  } = await supabase
    .from("expenses")
    .select(
      "id, amount, description, date, category_id, categories(name), profiles(full_name)"
    )
    .order("date", { ascending: false })
    .limit(20);

  console.log(
    "[DashboardPage] fetch at",
    new Date().toISOString(),
    "expenses count:",
    expenses?.length ?? 0,
    "ids:",
    expenses?.map((e: { id: string }) => e.id) ?? [],
    "error:",
    expensesError?.message ?? null
  );

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Dashboard</h1>
      <AddExpenseForm categoryOptions={categories ?? []} />
      <section>
        <h2 className="text-sm font-medium text-neutral-500 mb-2">
          Recent expenses
        </h2>
        <ul className="space-y-2">
          {expenses?.length
            ? expenses.map((e: Record<string, unknown>) => (
                <li
                  key={e.id as string}
                  className="flex items-center justify-between gap-4 py-2 px-3 rounded-lg bg-neutral-100 dark:bg-neutral-800"
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
                  <Link
                    href={`/expenses/${e.id}/edit`}
                    className="touch-target text-sm text-blue-600 dark:text-blue-400 shrink-0"
                  >
                    Edit
                  </Link>
                </li>
              ))
            : (
                <p className="text-neutral-500 text-sm py-4">
                  No expenses yet. Add one above.
                </p>
              )}
        </ul>
        {expenses?.length ? (
          <Link
            href="/expenses"
            className="inline-block mt-3 text-sm text-blue-600 dark:text-blue-400"
          >
            View all expenses →
          </Link>
        ) : null}
      </section>
    </div>
  );
}
