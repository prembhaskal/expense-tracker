/**
 * Parse ALLOWED_EMAILS env var and check if an email is allowed.
 * If ALLOWED_EMAILS is unset or empty, all emails are allowed.
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
  if (!allowed) return true; // no restriction
  return allowed.includes(email.trim().toLowerCase());
}
