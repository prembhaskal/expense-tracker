"use client";

import { useRouter } from "next/navigation";
import { addExpense } from "@/app/actions/expenses";
import { useTransition, useState } from "react";

type AddExpenseFormProps = {
  categoryOptions: { id: string; name: string }[];
};

export default function AddExpenseForm({ categoryOptions }: AddExpenseFormProps) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const [error, setError] = useState<string | null>(null);

  return (
    <form
      action={(formData) => {
        setError(null);
        startTransition(async () => {
          try {
            console.log("[AddExpenseForm] calling addExpense...");
            await addExpense(formData);
            console.log("[AddExpenseForm] addExpense ok, router.push /dashboard");
            router.push("/dashboard");
            console.log("[AddExpenseForm] router.refresh");
            router.refresh();
          } catch (e) {
            console.error("[AddExpenseForm] addExpense error", e);
            setError(e instanceof Error ? e.message : "Failed to save expense");
          }
        });
      }}
      className="space-y-3 p-4 rounded-lg border border-neutral-200 dark:border-neutral-700"
    >
      {error && (
        <p className="text-sm text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/30 px-3 py-2 rounded">
          {error}
        </p>
      )}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
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
            className="touch-target w-full rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2 text-sm"
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
            defaultValue={new Date().toISOString().slice(0, 10)}
            className="touch-target w-full rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2 text-sm"
          />
        </div>
      </div>
      <div>
        <label htmlFor="description" className="block text-sm font-medium mb-1">
          Description
        </label>
        <input
          id="description"
          name="description"
          type="text"
          placeholder="Optional"
          className="touch-target w-full rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2 text-sm"
        />
      </div>
      <div>
        <label htmlFor="category_id" className="block text-sm font-medium mb-1">
          Category
        </label>
        <select
          id="category_id"
          name="category_id"
          className="touch-target w-full rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2 text-sm"
        >
          <option value="">None</option>
          {categoryOptions.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>
      <button
        type="submit"
        disabled={isPending}
        className="touch-target w-full sm:w-auto px-4 py-2 rounded-lg bg-neutral-900 text-white text-sm font-medium hover:bg-neutral-800 disabled:opacity-50 dark:bg-neutral-100 dark:text-neutral-900"
      >
        {isPending ? "Adding…" : "Add expense"}
      </button>
    </form>
  );
}
