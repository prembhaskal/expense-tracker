# Expense Tracker – Android App

Android (Compose) app for the shared expense tracker. Syncs with the Next.js API; sign-in via Google.

## Google Sign-In setup

Follow these steps so "Sign in with Google" works in the app.

### 1. Google Cloud Console – Web client (for Supabase)

- In [Google Cloud Console](https://console.cloud.google.com/) → your project → **APIs & Services** → **Credentials**.
- Create or use an **OAuth 2.0 Client ID** of type **Web application**.
- Use this **Client ID** in:
  - **Supabase**: Authentication → Providers → Google (Client ID and Client Secret).
  - **Android app**: `local.properties` as `google.web.client.id` (see below).

The app uses the **same Web client ID** (not an Android-type client) so the backend can exchange the ID token with Supabase.

### 2. Google Cloud Console – Android client (SHA-1 + package)

Google returns **ApiException: 10 (DEVELOPER_ERROR)** or **RESULT_CANCELED** if the Android app is not registered.

1. **Get your app’s SHA-1**
   - From the repo root:
     ```bash
     cd android-app && ./gradlew signingReport
     ```
   - Or in Android Studio: **Gradle** → **android-app** → **app** → **Tasks** → **android** → **signingReport**.
   - Copy the **SHA-1** for **Variant: release** (and **debug** if you use debug sign-in).  
   - If release isn’t listed (no `keystore.properties`), get it from your release keystore:
     ```bash
     keytool -list -v -keystore android-app/release.keystore -alias expense-tracker
     ```
     Use the **SHA1** line (e.g. `AA:BB:CC:...`).

2. **Create an Android OAuth client (per signing key)**
   - Google Cloud Console → **APIs & Services** → **Credentials**.
   - **+ Create credentials** → **OAuth client ID**.
   - Application type: **Android**.
   - Package name: **`com.prembhaskal.expensetracker`** (must match `applicationId` in `app/build.gradle.kts`).
   - SHA-1 certificate fingerprint: paste the value from step 1.
   - Create.
   - **Release builds:** Add a **second** Android OAuth client with the **same** package name and the **release** SHA-1 (from your release keystore). Without it, release installs get “Failed to get ID token”.

You do **not** put these Android client IDs in the app. They only link your app (package + SHA-1) to the project so Google can issue ID tokens for the Web client.

### 3. App config – `local.properties`

Create or edit **`android-app/local.properties`** (this file is gitignored):

```properties
# Required: Web client ID (same as in Supabase Google provider)
google.web.client.id=YOUR_WEB_CLIENT_ID.apps.googleusercontent.com

# Optional: override API base URL (default is in app/build.gradle.kts)
# api.base.url=https://your-app.vercel.app
```

- **`google.web.client.id`** – The **Web application** OAuth client ID from step 1.
- **`api.base.url`** – Only if your API is not at the default Vercel URL (e.g. another deployment or `http://10.0.2.2:3000` for emulator → local Next.js).

### 4. Permissions

The app already declares in **`app/src/main/AndroidManifest.xml`**:

- **`INTERNET`** – required for API calls and Google Sign-In.
- **`ACCESS_NETWORK_STATE`** – optional, for connectivity checks.

No runtime permission requests are needed.

### 5. Build and run

- Open **android-app** in Android Studio or run from CLI:
  ```bash
  cd android-app && ./gradlew assembleDebug
  ```
- Install and run on a device or emulator; use **Sign in with Google** and ensure the device has Google Play Services.

---

## Troubleshooting

| Symptom | What to check |
|--------|----------------|
| **Sign-in cancelled** / **RESULT_CANCELED** | Add Android OAuth client (package + SHA-1) in Google Cloud (step 2). Wait a few minutes and retry. |
| **ApiException: 10** | Same as above: Android app not registered or wrong package/SHA-1. |
| **Failed to get ID token** | (1) Confirm `google.web.client.id` in `local.properties` is the **Web** client ID. (2) **Release build:** add the **release** SHA-1 to an Android OAuth client (same package) in Google Cloud – see step 2 above. |
| **Permission denied (missing INTERNET?)** | Ensure `AndroidManifest.xml` includes `<uses-permission android:name="android.permission.INTERNET" />`. Rebuild and reinstall. |
| **403 / Access restricted** | Backend allowlist: your Google email must be in `ALLOWED_EMAILS` (or equivalent) on the server. |

Logs: filter Logcat by **LoginScreen** or **GoogleAuthHelper** for sign-in and API-related messages.
