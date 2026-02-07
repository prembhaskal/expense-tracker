"use client";

import { useRouter } from "next/navigation";
import { useTransition, useState } from "react";
import { updateCategory, deleteCategory } from "@/app/actions/categories";

type Category = { id: string; name: string; color: string | null };

export default function CategoryList({
  categories,
}: {
  categories: Category[];
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const [editingId, setEditingId] = useState<string | null>(null);

  if (!categories.length) {
    return (
      <p className="text-neutral-500 text-sm py-4">No categories yet.</p>
    );
  }

  return (
    <ul className="space-y-2">
      {categories.map((c) =>
        editingId === c.id ? (
          <CategoryEditRow
            key={c.id}
            category={c}
            onSave={(formData) => {
              startTransition(async () => {
                await updateCategory(c.id, formData);
                setEditingId(null);
                router.refresh();
              });
            }}
            onCancel={() => setEditingId(null)}
            isPending={isPending}
          />
        ) : (
          <li
            key={c.id}
            className="flex items-center justify-between gap-4 py-2 px-3 rounded-lg bg-neutral-100 dark:bg-neutral-800"
          >
            <span
              className="flex items-center gap-2"
              style={c.color ? { color: c.color } : undefined}
            >
              {c.color && (
                <span
                  className="w-3 h-3 rounded-full shrink-0"
                  style={{ backgroundColor: c.color }}
                />
              )}
              {c.name}
            </span>
            <div className="flex items-center gap-2 shrink-0">
              <button
                type="button"
                onClick={() => setEditingId(c.id)}
                className="touch-target text-sm text-blue-600 dark:text-blue-400"
              >
                Edit
              </button>
              <button
                type="button"
                onClick={() => {
                  if (!confirm(`Delete category "${c.name}"? Expenses using it will have no category.`))
                    return;
                  startTransition(async () => {
                    await deleteCategory(c.id);
                    router.refresh();
                  });
                }}
                disabled={isPending}
                className="touch-target text-sm text-red-600 dark:text-red-400 disabled:opacity-50"
              >
                Delete
              </button>
            </div>
          </li>
        )
      )}
    </ul>
  );
}

function CategoryEditRow({
  category,
  onSave,
  onCancel,
  isPending,
}: {
  category: Category;
  onSave: (formData: FormData) => void;
  onCancel: () => void;
  isPending: boolean;
}) {
  return (
    <li className="py-2 px-3 rounded-lg bg-neutral-100 dark:bg-neutral-800">
      <form
        action={onSave}
        className="flex flex-wrap items-center gap-2"
      >
        <input
          name="name"
          type="text"
          required
          defaultValue={category.name}
          className="touch-target rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-2 py-1 text-sm w-32"
        />
        <input
          name="color"
          type="text"
          defaultValue={category.color ?? ""}
          placeholder="#hex"
          className="touch-target rounded-md border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 px-2 py-1 text-sm w-20"
        />
        <button
          type="submit"
          disabled={isPending}
          className="touch-target text-sm text-blue-600 dark:text-blue-400"
        >
          Save
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="touch-target text-sm text-neutral-500"
        >
          Cancel
        </button>
      </form>
    </li>
  );
}
