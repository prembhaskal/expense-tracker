"use client";

type MonthTotal = { month: string; total: number };
type CategoryTotal = { id: string; name: string; total: number };

export function ReportsView({
  monthTotals,
  categoryTotals,
}: {
  monthTotals: MonthTotal[];
  categoryTotals: CategoryTotal[];
}) {
  const formatMonth = (ym: string) => {
    const [y, m] = ym.split("-");
    const d = new Date(Number(y), Number(m) - 1);
    return d.toLocaleDateString("en-US", { month: "short", year: "numeric" });
  };

  const totalAll = categoryTotals.reduce((s, c) => s + c.total, 0);

  return (
    <div className="space-y-8">
      <section>
        <h2 className="text-sm font-medium text-neutral-500 mb-3">
          Monthly expenses
        </h2>
        <ul className="space-y-2">
          {monthTotals.length
            ? monthTotals.map(({ month, total }) => (
                <li
                  key={month}
                  className="flex items-center justify-between py-2 px-3 rounded-lg bg-neutral-100 dark:bg-neutral-800"
                >
                  <span>{formatMonth(month)}</span>
                  <span className="font-medium">
                    {total.toFixed(2)}
                  </span>
                </li>
              ))
            : (
                <p className="text-neutral-500 text-sm py-4">
                  No expense data yet.
                </p>
              )}
        </ul>
      </section>

      <section>
        <h2 className="text-sm font-medium text-neutral-500 mb-3">
          By category
        </h2>
        <ul className="space-y-2">
          {categoryTotals.length
            ? categoryTotals.map(({ id, name, total }) => (
                <li
                  key={id}
                  className="flex items-center justify-between py-2 px-3 rounded-lg bg-neutral-100 dark:bg-neutral-800"
                >
                  <span>{name}</span>
                  <span className="font-medium">
                    {total.toFixed(2)}
                    {totalAll > 0 && (
                      <span className="text-neutral-500 text-xs ml-1">
                        ({((100 * total) / totalAll).toFixed(0)}%)
                      </span>
                    )}
                  </span>
                </li>
              ))
            : (
                <p className="text-neutral-500 text-sm py-4">
                  No categories used yet.
                </p>
              )}
        </ul>
        {totalAll > 0 && (
          <p className="mt-3 text-sm font-medium">
            Total: {totalAll.toFixed(2)}
          </p>
        )}
      </section>
    </div>
  );
}
