import { createClient } from "@/lib/supabase/server";
import { ReportsView } from "./ReportsView";

export const dynamic = "force-dynamic";

export default async function ReportsPage() {
  const supabase = await createClient();

  const { data: monthly } = await supabase
    .from("expenses")
    .select("date, amount")
    .order("date", { ascending: false });

  const { data: byCategory } = await supabase
    .from("expenses")
    .select("amount, category_id, categories(name)")
    .order("amount", { ascending: false });

  const byMonth = (monthly ?? []).reduce<Record<string, number>>((acc, row) => {
    const month = (row.date as string).slice(0, 7);
    acc[month] = (acc[month] ?? 0) + Number(row.amount);
    return acc;
  }, {});

  const byCategoryMap = (byCategory ?? []).reduce<
    Record<string, { name: string; total: number }>
  >((acc, row) => {
    const id = (row.category_id as string) ?? "_none_";
    const name =
      (row.categories as { name?: string } | null)?.name ?? "Uncategorized";
    if (!acc[id]) acc[id] = { name, total: 0 };
    acc[id].total += Number(row.amount);
    return acc;
  }, {});

  const categoryTotals = Object.entries(byCategoryMap).map(([id, v]) => ({
    id,
    name: v.name,
    total: v.total,
  }));

  const monthTotals = Object.entries(byMonth)
    .map(([month, total]) => ({ month, total }))
    .sort((a, b) => b.month.localeCompare(a.month))
    .slice(0, 12);

  return (
    <div className="space-y-8">
      <h1 className="text-xl font-semibold">Reports</h1>
      <ReportsView
        monthTotals={monthTotals}
        categoryTotals={categoryTotals}
      />
    </div>
  );
}
