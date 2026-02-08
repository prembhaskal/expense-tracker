# Android app – sync architecture

## Principle: local-first, sync in background

All expense mutations (add, update, delete) are applied to **local Room first**. The UI never waits on the network. A background **sync worker** pushes local changes to the API and pulls the latest data for integrity.

## Data flow

1. **UI → Room only**  
   Add / update / delete expense always write to Room (and/or to the pending-deletes queue) and return immediately. The UI observes Room (Flow), so lists and reports update right away.

2. **Queue (Room as queue)**  
   - **Pending creates:** `expenses` rows with `pendingSync = 1`.  
   - **Pending updates:** `expenses` rows with `pendingUpdate = 1` and `pendingSync = 0`.  
   - **Pending deletes:** `pending_deletes` table (expense ids to delete on the server).

3. **Background sync (WorkManager)**  
   - Runs only when **network is available** (WorkManager constraint).  
   - **Order:** push pending creates → push pending updates → push pending deletes → **pull** expenses (and categories) from API into Room.  
   - Pull overwrites local with server state so local and server stay in sync after a successful run.

4. **When sync runs**  
   - **Periodic:** every 15 minutes (when connected).  
   - **One-time:** enqueued after login, after add/update/delete (so changes are pushed soon).  
   - One-time and periodic work both use the same worker and the same network constraint.

## Integrity

- **Queue + poll:** We both **push** our queue (creates, updates, deletes) and **pull** from the API. After a full sync, local state matches the server.  
- **Retry:** If push or pull fails, the worker returns `Result.retry()`; WorkManager will reschedule.  
- **No double apply:** Pending creates are deleted from Room after a successful API create; pending updates are cleared with `clearPendingUpdate`; pending deletes are removed from the table after a successful API delete. Pull then repopulates Room from the server.

## Components

| Component            | Role |
|----------------------|------|
| `ExpenseRepository`  | All add/update/delete write to Room (and pending_deletes). `pushPending*` methods are used only by the worker. |
| `SyncWorker`         | Runs on IO: push pending creates → push pending updates → push pending deletes → pull expenses → pull categories. |
| `SyncScheduler`      | Enqueues one-time or periodic work with `NetworkType.CONNECTED` constraint. |
| Room                 | Single source of truth for the UI; also holds the “queue” (pendingSync, pendingUpdate, pending_deletes). |

## Categories

**Add** and **delete** are local-first (same pattern as expenses): add writes to Room with `pendingSync = true`; delete removes from Room and enqueues the id in `pending_category_deletes`. The sync worker runs `pushPendingCategories()` (POST new categories, remove local rows) and `pushPendingCategoryDeletes()` (DELETE on server, clear queue), then **pulls** categories from the API. **Update** still calls the API then pull (can be made local-first with a pendingUpdate queue if needed).
