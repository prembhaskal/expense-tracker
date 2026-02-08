import { createClient } from "@supabase/supabase-js";
import type { User } from "@supabase/supabase-js";

/**
 * Create a Supabase client that uses the given access token (e.g. from Authorization: Bearer).
 * Use in API routes to authenticate mobile/clients that send the token in headers.
 */
export function createSupabaseClientWithToken(accessToken: string) {
  return createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    {
      global: {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      },
    }
  );
}

/**
 * Extract Bearer token from Authorization header or return null.
 */
export function getBearerTokenFromRequest(request: Request): string | null {
  const auth = request.headers.get("authorization");
  if (!auth?.startsWith("Bearer ")) return null;
  return auth.slice(7).trim() || null;
}

/**
 * Get the authenticated user from the request (Bearer token).
 * Returns { user, error } where error is a response to send if auth failed.
 */
export async function getUserFromApiRequest(request: Request): Promise<
  | { user: User; error: null }
  | { user: null; error: Response }
> {
  const token = getBearerTokenFromRequest(request);
  if (!token) {
    return { user: null, error: new Response(JSON.stringify({ error: "Missing or invalid Authorization header" }), { status: 401, headers: { "Content-Type": "application/json" } }) };
  }
  const supabase = createSupabaseClientWithToken(token);
  const { data: { user }, error } = await supabase.auth.getUser();
  if (error || !user) {
    return { user: null, error: new Response(JSON.stringify({ error: "Invalid or expired token" }), { status: 401, headers: { "Content-Type": "application/json" } }) };
  }
  return { user, error: null };
}
