import { createClient } from "@/lib/supabase/server";
import AddCategoryForm from "./AddCategoryForm";
import CategoryList from "./CategoryList";

export default async function CategoriesPage() {
  const supabase = await createClient();
  const { data: categories } = await supabase
    .from("categories")
    .select("id, name, color")
    .order("name");

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Categories</h1>
      <AddCategoryForm />
      <section>
        <h2 className="text-sm font-medium text-neutral-500 mb-2">
          All categories
        </h2>
        <CategoryList categories={categories ?? []} />
      </section>
    </div>
  );
}
