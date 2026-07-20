# LocalShare Fix Log

Chronological record of fixes applied after safety checkpoint `safe-checkpoint-v1.2.0`.

---

## 2026-07-20 — Safety checkpoint

- **Tag:** `safe-checkpoint-v1.2.0` on commit `16538ae` (v1.2.0)
- **Purpose:** Restorable baseline before security and stability work
- **Restore:** `git checkout safe-checkpoint-v1.2.0` (see `RESTORE.md`)

---

## 2026-07-20 — Docs & ignore hygiene

- Added `RESTORE.md` (how to roll back)
- Added this `FIXES.md` log
- `.gitignore`: ignore `*.apk`, `*.aab`, and related binaries so release dumps stay local

---

## 2026-07-20 — Security: release signing secrets

- **Issue:** Keystore passwords were hardcoded in `app/build.gradle.kts` (`android123`)
- **Fix:** Read `RELEASE_STORE_*` / `RELEASE_KEY_*` from `local.properties` or environment; only sign release when passwords are present
- **Local:** Passwords remain in gitignored `local.properties` so Android Studio release builds still work on this machine
- **Files:** `app/build.gradle.kts`, `local.properties` (not committed)

---

## 2026-07-20 — Security: encryption key out of share URL

- **Issue:** AES session key was appended as `?key=...` on the public URL (QR, clipboard, widget, logs)
- **Fix:**
  - Share URL is always clean `http://ip:port`
  - New authenticated endpoint `GET /api/encryption-key` (behind PIN gate)
  - Phone receive path fetches key from that API (still accepts legacy `?key=` if present)
- **Files:** `ServerForegroundService.kt`, `FileShareServer.kt`, `ReceiveScreen.kt`, `HowToUseScreen.kt`, `SettingsScreen.kt`

---

## 2026-07-20 — Stability: logo resources

- **Issue:** Lint `ResourceType` error — `openRawResource(R.drawable.logo)`
- **Fix:** Copy logos to `res/raw/` and open `R.raw.logo` / `R.raw.logo_dark`
- **Files:** `app/src/main/res/raw/*`, `FileShareServer.kt`

---

## 2026-07-20 — Stability: update download receiver

- **Issue:** BroadcastReceiver registration needs an explicit exported flag on modern Android
- **Fix:** Use `ContextCompat.registerReceiver(..., RECEIVER_EXPORTED)` for `DOWNLOAD_COMPLETE`
- **Files:** `UpdateManager.kt`

---

## 2026-07-20 — Stability: encryption OOM guard

- **Issue:** Encrypted downloads used `readBytes()` on the whole file → OOM on large media
- **Fix:** Cap in-memory encrypt at 50MB; larger files fall back to plain stream with a log warning
- **Files:** `FileShareServer.kt`

---

## 2026-07-20 — Security: no exception leakage to HTTP clients

- **Issue:** Server responses included `e.message` (internal details)
- **Fix:** Generic error strings for clients; full stack still logged with `Log.e`
- **Files:** `FileShareServer.kt`
