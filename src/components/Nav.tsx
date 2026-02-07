"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const links = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/expenses", label: "Expenses" },
  { href: "/categories", label: "Categories" },
  { href: "/reports", label: "Reports" },
];

export default function Nav() {
  const pathname = usePathname();

  return (
    <>
      {/* Top nav: hidden on small screens when bottom nav shows */}
      <nav className="hidden sm:block border-b border-neutral-200 dark:border-neutral-800 bg-white/80 dark:bg-neutral-900/80 backdrop-blur sticky top-0 z-10">
        <div className="max-w-4xl mx-auto px-4 flex items-center gap-1 py-2">
          {links.map(({ href, label }) => (
            <Link
              key={href}
              href={href}
              className="min-h-[44px] min-w-[44px] flex items-center justify-center px-4 rounded-md text-sm font-medium text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 dark:text-neutral-400 dark:hover:text-white dark:hover:bg-neutral-800 whitespace-nowrap"
            >
              {label}
            </Link>
          ))}
        </div>
      </nav>
      {/* Bottom nav: mobile only, touch-friendly */}
      <nav className="sm:hidden fixed bottom-0 left-0 right-0 z-20 border-t border-neutral-200 dark:border-neutral-800 bg-white/95 dark:bg-neutral-900/95 backdrop-blur safe-area-pb">
        <div className="max-w-4xl mx-auto px-2 flex items-center justify-around">
          {links.map(({ href, label }) => {
            const isActive =
              pathname === href ||
              (href !== "/dashboard" && pathname.startsWith(href));
            return (
              <Link
                key={href}
                href={href}
                className={`min-h-[48px] min-w-[48px] flex flex-col items-center justify-center gap-0.5 py-2 px-2 text-xs font-medium rounded-lg transition-colors ${
                  isActive
                    ? "text-neutral-900 bg-neutral-100 dark:text-white dark:bg-neutral-700"
                    : "text-neutral-600 dark:text-neutral-400"
                }`}
              >
                {label}
              </Link>
            );
          })}
        </div>
      </nav>
    </>
  );
}
