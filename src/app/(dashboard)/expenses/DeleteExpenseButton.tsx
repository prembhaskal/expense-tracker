"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { deleteExpense } from "@/app/actions/expenses";

export default function DeleteExpenseButton({ id }: { id: string }) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  return (
    <button
      type="button"
      onClick={() => {
        if (!confirm("Delete this expense?")) return;
        startTransition(async () => {
          await deleteExpense(id);
          router.refresh();
        });
      }}
      disabled={isPending}
      className="touch-target text-sm text-red-600 dark:text-red-400 hover:underline disabled:opacity-50"
    >
      {isPending ? "…" : "Delete"}
    </button>
  );
}
