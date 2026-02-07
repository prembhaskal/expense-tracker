/**
 * Parse ALLOWED_EMAILS env var and check if an email is allowed.
 * If ALLOWED_EMAILS is unset or empty, no one is allowed (must be set for sign-in to work).
 */
export function getAllowedEmails(): string[] | null {
  const raw = process.env.ALLOWED_EMAILS;
  if (!raw || typeof raw !== "string") return null;
  const list = raw
    .split(",")
    .map((e) => e.trim().toLowerCase())
    .filter(Boolean);
  return list.length > 0 ? list : null;
}

export function isEmailAllowed(email: string | undefined): boolean {
  if (!email) return false;
  const allowed = getAllowedEmails();
  if (!allowed) return false; // no list = no one allowed
  return allowed.includes(email.trim().toLowerCase());
}
