# Shared Expense Trakcer

Application to track expenses done by one family.


Users:
- multiple users can access it.
- login with their google account

Application allows to do following
- Add/Update/Delete an expense
- Expense has following items:
  - Amount
  - Description
  - Date
  - Category (pre-defined categories)

- Option to add/update/delete categories

Report View
- view monthly expenses
- expense by category

Tech stack:
- Next JS
- React for frontend (optimize for mobile users)
- use Vercel
- use postgresDB from vercel (and/or supabase)


Clarifications:
- All expenses goes to single account
- store each expense by user, so that we can track later