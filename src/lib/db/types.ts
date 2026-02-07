export type Profile = {
  id: string;
  email: string | null;
  full_name: string | null;
  avatar_url: string | null;
  created_at: string;
};

export type Category = {
  id: string;
  name: string;
  color: string | null;
  created_at: string;
};

export type Expense = {
  id: string;
  amount: number;
  description: string | null;
  date: string;
  category_id: string | null;
  user_id: string;
  created_at: string;
  updated_at: string;
  categories?: Category | null;
  profiles?: Profile | null;
};
