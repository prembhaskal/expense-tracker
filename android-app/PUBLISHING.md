# Publishing the Android app to Google Play

## 1. Google Play Developer account

- Go to [Google Play Console](https://play.google.com/console) and sign in with a Google account.
- Pay the **one-time registration fee** (as of 2025, typically $25) to create a developer account.
- Complete identity and payment profile if prompted.

---

## 2. Release signing (keystore)

You must sign the release build with a keystore. **Keep the keystore and passwords safe**; losing them means you cannot update the app on Play Store.

### Create a keystore (one time)

From your machine, run (replace with your own alias/passwords and path):

```bash
keytool -genkey -v -keystore android-app/release.keystore -alias expense-tracker -keyalg RSA -keysize 2048 -validity 10000
```

Use a strong password and store it securely. Do **not** commit `release.keystore` or passwords to git (add `release.keystore` and `keystore.properties` to `.gitignore`).

### Configure the project to use it

Create **`android-app/keystore.properties`** (add this file to `.gitignore`; do not commit):

```properties
storeFile=release.keystore
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=expense-tracker
keyPassword=YOUR_KEY_PASSWORD
```

- **storePassword** = password for the keystore file.
- **keyPassword** = password for the key (alias) inside the keystore. If you didn’t set a separate one when creating the keystore (you pressed Enter when keytool asked for “key password”), use the **same value as storePassword**.

If the keystore is in `android-app/`, use `storeFile=release.keystore`. If it’s elsewhere, use an absolute path or a path relative to the project root.

The build is already set up to read `keystore.properties` and use it for the `release` build type when the file exists.

---

## 3. Build the release App Bundle (AAB)

Google Play requires the **Android App Bundle** (.aab), not the APK.

From the repo root:

```bash
cd android-app
./gradlew bundleRelease
```

Output: **`app/build/outputs/bundle/release/app-release.aab`**

### If release install fails with `INSTALL_BASELINE_PROFILE_FAILED`

Common on Android 15+ when installing the release build from Android Studio:

1. **Uninstall the app** from the device/emulator (e.g. the existing debug build), then run the release build again.
2. Or in Android Studio: **Run → Edit Configurations** → for your app, set the deployment option to **APK from app bundle** (or equivalent), then run.
3. The build already excludes baseline profile resources where possible to reduce this issue.

- Ensure **`local.properties`** has production values (or leave defaults):
  - `api.base.url` = your production API URL (e.g. Vercel).
  - `google.web.client.id` = your Web client ID used in Supabase/Google.

---

## 4. Play Console: create the app and upload

1. In [Play Console](https://play.google.com/console), click **Create app**.
2. Fill in:
   - **App name:** e.g. "Expense Tracker"
   - **Default language**
   - **App or game:** App
   - **Free or paid:** Free (or Paid if you choose)
3. Accept declarations and create the app.

### First release (Production or Testing)

1. In the app, go to **Release** → **Production** (or **Testing** → **Internal testing** to try first).
2. **Create new release**.
3. **Upload** the `app-release.aab` you built.
4. **Release name:** e.g. "1.0 (1)" (optional; can match versionName/versionCode).
5. Add **Release notes** (what’s new for users).
6. Save and then **Review release** → **Start rollout** (or **Save** and add testers if using Internal testing).

---

## 5. Required Play Console sections

Before the app can go live, complete at least:

| Section | What to do |
|--------|------------|
| **App content** | Declare privacy policy if you collect data; complete “App access”, “Ads” (if no ads, say no), “Content ratings”, “Target audience”, etc. |
| **Store listing** | Short/long description, screenshots (phone 16:9 or 9:16, min 2), app icon 512×512, feature graphic 1024×500. |
| **Content rating** | Run the questionnaire; submit. |
| **Target audience** | Age groups. |
| **News app / COVID-19** | Answer as applicable (usually no). |
| **Data safety** | Declare what data is collected (e.g. email for sign-in, expense data). |

---

## 9. (Optional) Change applicationId for production

The app uses `com.prembhaskal.expensetracker`. To use a different package name:

1. In **`app/build.gradle.kts`**, set:
   ```kotlin
   applicationId = "com.yourname.yourapp"
   ```
2. Refactor the Kotlin package in the project to match (optional but keeps things consistent).
3. Use the **same package name** in Google Cloud for the Android OAuth client (and optionally in Play Console).
4. Rebuild and upload a new AAB; this will be a **new** app on Play Store (different applicationId = different app).

If you keep `com.prembhaskal.expensetracker`, no change is needed.

---

## 7. Version management for new releases

**Before each new release**, you must update the version codes in `app/build.gradle.kts`:

### versionCode
- **What it is:** An integer that represents the app version internally to Google Play.
- **Rules:** Must be **incremented by at least 1** for each new release. Google Play uses this to determine if a new version is available.
- **Current value:** See `versionCode = X` in `app/build.gradle.kts` (currently: 3)
- **Update:** Increment to 4, then 5, then 6, etc. for each new release.

### versionName
- **What it is:** A human-readable version string shown to users (e.g., "1.0", "0.3", "1.2.1").
- **Format:** Typically semantic versioning (MAJOR.MINOR.PATCH) or your preferred scheme.
- **Rules:** No rules enforced by Google Play; can stay the same, go backwards, or skip numbers. However, it's best practice to follow semantic versioning.
- **Current value:** See `versionName = "X"` in `app/build.gradle.kts` (currently: "0.3")
- **Update:** Increment to "0.4", "1.0", "1.1", etc. based on the significance of changes.

### Example workflow for a new release

1. **Identify the current versions:**
   ```kotlin
   versionCode = 3
   versionName = "0.3"
   ```

2. **Decide on the new version:**
   - Bug fixes or minor improvements → bump patch: `0.3` → `0.4`
   - New features → bump minor: `0.3` → `0.4` or `0.3` → `1.0`
   - Major redesign → bump major: `0.3` → `1.0`

3. **Update `app/build.gradle.kts`:**
   ```kotlin
   versionCode = 4         // Always increment by at least 1
   versionName = "0.4"     // Update based on changes
   ```

4. **Commit this change:**
   ```bash
   git add android-app/app/build.gradle.kts
   git commit -m "Release v0.4 (versionCode 4)"
   ```

5. **Build and upload the AAB** (see section 3) with the new versions.

**Important:** Each release to Google Play must have a unique, higher `versionCode`. Failure to increment will cause the upload to be rejected.

---

## 8. After publishing

- **Updates:** Bump `versionCode` (and optionally `versionName`) in `app/build.gradle.kts`, build a new AAB, then create a new release in the same track and upload the new AAB.
- **Staged rollout:** You can start with a percentage of users (e.g. 20%) and increase gradually.
- **Signing:** Play App Signing is recommended: Play will use your upload key to verify you, then sign the app with Google’s key for distribution. The first time you upload an AAB, Play may prompt you to enroll in Play App Signing if not already done.

---

## Quick checklist

- [ ] Google Play Developer account created
- [ ] Release keystore created and stored safely
- [ ] `keystore.properties` created (and in `.gitignore`), `app/build.gradle.kts` configured for release signing
- [ ] `local.properties` (or defaults) point to production API and Google Web client ID
- [ ] `./gradlew bundleRelease` succeeds and produces `app-release.aab`
- [ ] App created in Play Console; AAB uploaded to a release (e.g. Internal testing first)
- [ ] Store listing, content rating, target audience, and data safety completed
- [ ] Release rolled out (or sent to testers)
