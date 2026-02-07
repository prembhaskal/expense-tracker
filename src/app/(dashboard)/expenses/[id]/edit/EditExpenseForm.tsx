"use client";

import { useRouter } from "next/navigation";
import Link from "next/link";
import { updateExpense } from "@/app/actions/expenses";
import { useTransition } from "react";

type Expense = {
  id: string;
  amount: number;
  description: string | null;
  date: string;
  category_id: string | null;
};

type EditExpenseFormProps = {
  expense: Expense;
  categoryOptions: { id: string; name: string }[];
};

export default function EditExpenseForm({
  expense,
  categoryOptions,
}: EditExpenseFormProps) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  return (
    <form
      action={(formData) => {
        startTransition(async () => {
          await updateExpense(expense.id, formData);
          router.push("/expenses");
          router.refresh();
        });
      }}
      className="space-y-4 max-w-md"
    >
      <div>
        <label htmlFor="amount" className="block text-sm font-medium mb-1">
          Amount
        </label>
        <input
          id="amount"
          name="amount"
          type="number"
          step="0.01"
          min="0.01"
          required
          defaultValue={expense.amount}
          className="touch-target w-full rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2"
        />
      </div>
      <div>
        <label htmlFor="date" className="block text-sm font-medium mb-1">
          Date
        </label>
        <input
          id="date"
          name="date"
          type="date"
          required
          defaultValue={expense.date}
          className="touch-target w-full rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2"
        />
      </div>
      <div>
        <label htmlFor="description" className="block text-sm font-medium mb-1">
          Description
        </label>
        <input
          id="description"
          name="description"
          type="text"
          defaultValue={expense.description ?? ""}
          className="touch-target w-full rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2"
        />
      </div>
      <div>
        <label htmlFor="category_id" className="block text-sm font-medium mb-1">
          Category
        </label>
        <select
          id="category_id"
          name="category_id"
          defaultValue={expense.category_id ?? ""}
          className="touch-target w-full rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2"
        >
          <option value="">None</option>
          {categoryOptions.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>
      <div className="flex gap-3">
        <button
          type="submit"
          disabled={isPending}
          className="touch-target px-4 py-2 rounded-lg bg-neutral-900 text-white font-medium hover:bg-neutral-800 disabled:opacity-50 dark:bg-neutral-100 dark:text-neutral-900"
        >
          {isPending ? "Saving…" : "Save"}
        </button>
        <Link
          href="/expenses"
          className="touch-target px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 font-medium"
        >
          Cancel
        </Link>
      </div>
    </form>
  );
}
