# AnyShare — Code Review Summary

Scope: 50 Kotlin files, server layer, encryption, UI (23 files), data/Room, utils, widget, build & manifest config. Findings below were independently cross-checked; security claims were adversarially verified against the source.

> **Context that shapes every finding:** AnyShare is a *LAN-only, trust-the-network* app. Several "critical" issues are only exploitable by someone already on your Wi-Fi. That doesn't make them non-issues — it means the fixes should be prioritized by "how hostile do I expect my network to be," not by worst-case internet exposure.

---

## 1. What's genuinely good

- **Streaming/Range handling is the strongest part of the codebase.** `RangeRequestHandler` + `BoundedInputStream` solve a real, subtle bug (NanoHTTPD trusting `Content-Length` while a positioned `FileInputStream` reads to EOF). The comments explain *why*, not just *what*. Suffix ranges, >2 GB chunked fallback, and 206/200 paths are all handled correctly.
- **Secrets are managed correctly in the build.** Keystore, passwords, and `local.properties` are all gitignored and confirmed *not* tracked; the build reads signing creds from `local.properties`/env and only attaches release signing when present. R8/ProGuard + resource shrinking are enabled with a real rules file.
- **The encryption design intent is sound.** AES-256-GCM with a random per-message IV, key *not* embedded in the share URL (fetched from `/api/encryption-key` after the PIN gate), and a size cap to avoid OOM. The *design* is right — the *transport* undermines it (see below).
- **The foreground service handles the hard Android edge case.** On `startForeground` failure it still calls `startForeground` with an error notification to avoid `ForegroundServiceDidNotStartInTimeException`. That's the kind of detail most apps get wrong.
- **Good small utilities.** `AccessLogBuffer` is a clean thread-safe ring buffer; the semver comparator in `UpdateChecker` is defensively correct; the FileProvider + `InstallActivity` trampoline plumbing is the *right* pattern (it's the missing verification that sinks it).
- **The Material You theming** (`colorSchemeFromSeed`, tonal palettes, AMOLED override) is thorough, idiomatic M3 dynamic-color work.

---

## 2. Issues, ranked

### Release-blocking (one logical fix)

**A. The in-app updater installs unverified APKs.**
`UpdateManager.startDownload()` enqueues whatever URL the GitHub API returns and `launchInstaller()` hands the file straight to the package installer — **no SHA-256 check, no APK signature comparison.** `UpdateChecker` takes `browser_download_url` verbatim with no HTTPS/host allowlist. On top of that, the manifest **does not declare `REQUEST_INSTALL_PACKAGES`**, so the flow is both insecure *and* likely broken on Android 8+. A MITM or a compromised release asset → the app silently installs arbitrary code with its own permissions.
*Fix: pin an expected SHA-256 (or compare the new APK's signing cert to the installed one), enforce `https://` + host allowlist, add the permission, and only then install.*

### High

**B. Plaintext HTTP defeats the headline "AES-256 encryption."**
The server is `NanoHTTPD(port)` with no TLS. The PIN, all file bytes, and the base64 AES key from `/api/encryption-key` cross the LAN in cleartext. Any on-path sniffer recovers the key and PIN. The GCM layer protects *at-rest decryption in the browser*, not *in transit* — so the security marketing overstates what's actually protected.

**C. Pre-auth information leak on `/api/sessions` + `/api/session/status`.**
The gate at `FileShareServer.kt:261` relies on `&&` binding tighter than `||`, so the bare `uri == "/api/sessions"` and `uri == "/api/session/status"` branches are reachable **before** the PIN gate. Any unauthenticated device on the LAN can read sender IPs, device names, file counts, and sizes of in-flight transfers.

**D. IP-based "authentication" with no session token.**
A correct PIN only records the source IP (`authenticatedIps`, 24 h). There's no cookie/token — so every device behind the same NAT IP inherits the session, and there's nothing to revoke per-device. Combined with **CORS `Access-Control-Allow-Origin: *`** on most authenticated GETs, any website open in a victim's browser can read the file list/status cross-origin.

**E. Unbounded resource use in ZIP paths.**
`/api/download-zip` (encrypted) buffers the *entire* ZIP into memory (`ByteArrayOutputStream` → `toByteArray()` → encrypt); folder download zips a whole tree to disk and, encrypted, reads it all back into memory. No aggregate size cap anywhere → trivial memory/disk exhaustion from one request.

**F. Update/install attack surface details.**
`InstallActivity` is exported and fires `ACTION_VIEW` with `FLAG_GRANT_READ_URI_PERMISSION` on an intent-extra URI it doesn't validate — a caller can get a read grant to a file it couldn't otherwise read.

### Medium

- **Encryption fails open.** On upload, if decrypt throws it silently stores the still-encrypted blob under the original filename; on download, files > 50 MB are served **plaintext** despite encryption being on. Both fail *open* rather than *closed*.
- **Room: `fallbackToDestructiveMigration()`** — the next schema change wipes all transfer history without warning. **REPLACE + `onDelete=CASCADE`** can cascade-delete a session's file rows; the session+files write isn't wrapped in a `@Transaction`.
- **Crash handler blocks with `runBlocking`** on the very thread that's dying — risks an ANR on top of the crash and lost reports. The Activity crash handler also `Thread.sleep(2000)` on the crashing thread.
- **A privacy setting that doesn't stick.** `clipboardSyncEnabled` is never persisted by `SettingsRepository`, so it resets to ON every restart — silently re-enabling a privacy-sensitive sync the user turned off.
- **Dead/broken navigation.** `HomeScreen` and `FilesScreen` (~2,000 lines) are never composed; `file_preview/{id}` and `uri_preview` are navigated to but have no NavHost destinations → tap either crashes or no-ops.
- **Polling instead of Flow.** `FileShareViewModel` runs `while(true)` loops (logs every 3 s, clipboard every 2 s) for the whole process lifetime; every screen uses `collectAsState()` instead of `collectAsStateWithLifecycle()` → constant CPU wake and background recomposition.
- **No CSP + online font in an offline app.** `serveWebUI` sets no `Content-Security-Policy`, and the Web UI pulls Roboto from Google Fonts — a privacy leak and a broken page when truly offline.
- **Cleartext permitted globally** in `network_security_config` (`base-config cleartextTrafficPermitted="true"`). Needed for the LAN server, but it's scoped to *everything*, not just the local origin.

### Low

- File IDs derive from `uri.toString().hashCode()` — 32-bit collisions silently drop files from the share list.
- `isWifiConnected()` returns true on cellular — a "Wi-Fi only" gate can burn mobile data.
- QR bitmap built pixel-by-pixel (`setPixel` × 262k) on the main thread.
- All UI strings hardcoded — not localizable; many functional icons lack `contentDescription` (TalkBack).
- **Zero tests** — no `test`/`androidTest` source sets at all, for a networking/crypto-heavy app.
- `zipDocumentFile` doesn't sanitize `..` in entry names (latent hygiene only — names are device-controlled, *not* a client zip-slip).

---

## 3. Top recommendations, in order

1. **Gate the updater behind integrity verification** (pinned SHA-256 or signing-cert match) + `REQUEST_INSTALL_PACKAGES`. This is the single most dangerous path and should block the next release.
2. **Stop claiming transit encryption you don't have**, or add it. Either serve over TLS (self-signed for LAN) or reword the UI/marketing so users know AES-GCM only protects browser-side decryption, not the wire. At minimum, never return the key on an endpoint reachable pre-auth.
3. **Fix the auth model:** move the PIN gate *before* every sensitive route (kill the `&&`/`||` bug), issue a real per-session token cookie instead of trusting source IP, and scope CORS off `*` for authenticated endpoints.
4. **Cap and stream the ZIP paths** — enforce a total-size limit and stream (the non-encrypted path already does this correctly with `PipedInputStream`).
5. **Make encryption fail closed** — on decrypt/encrypt failure or oversized files, return an error, don't silently serve plaintext/ciphertext.
6. **Clean up the UI layer:** delete or wire `HomeScreen`/`FilesScreen`, add the missing NavHost destinations, replace `while(true)` polling with Flow collection, and switch to `collectAsStateWithLifecycle()`.
7. **Add a migration and a `@Transaction`** for Room, and persist `clipboardSyncEnabled`.
8. **Add even a thin test layer** for `RangeRequestHandler` parsing, the semver comparator, and the encryption round-trip — these are pure, high-value, easy to test.

Want me to fix the top item (the update-verification path), or turn sections 2–3 into a prioritized issue list with per-file line references?
