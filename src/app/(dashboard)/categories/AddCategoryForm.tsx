"use client";

import { useRouter } from "next/navigation";
import { addCategory } from "@/app/actions/categories";
import { useTransition } from "react";

export default function AddCategoryForm() {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  return (
    <form
      action={(formData) => {
        startTransition(async () => {
          await addCategory(formData);
          router.refresh();
        });
      }}
      className="flex flex-wrap items-end gap-3"
    >
      <div>
        <label htmlFor="name" className="block text-sm font-medium mb-1">
          Name
        </label>
        <input
          id="name"
          name="name"
          type="text"
          required
          placeholder="e.g. Groceries"
          className="touch-target w-full min-w-[140px] rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2 text-sm"
        />
      </div>
      <div>
        <label htmlFor="color" className="block text-sm font-medium mb-1">
          Color
        </label>
        <input
          id="color"
          name="color"
          type="text"
          placeholder="#hex (optional)"
          className="touch-target w-24 rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-3 py-2 text-sm"
        />
      </div>
      <button
        type="submit"
        disabled={isPending}
        className="touch-target px-4 py-2 rounded-lg bg-neutral-900 text-white text-sm font-medium hover:bg-neutral-800 disabled:opacity-50 dark:bg-neutral-100 dark:text-neutral-900"
      >
        {isPending ? "Adding…" : "Add category"}
      </button>
    </form>
  );
}
