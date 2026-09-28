# Privacy Policy for AnyShare

**Last Updated:** September 1, 2026

Welcome to **AnyShare** ("we", "our", or "us"). We are committed to protecting your privacy and ensuring you have complete control over your data. This Privacy Policy explains how AnyShare handles your information.

---

## 1. Summary: Our Core Privacy Principle
**AnyShare is a local-only, peer-to-peer file sharing and media streaming application.** 
- We do **not** collect, store, or transmit your personal data.
- We do **not** use analytics, tracking SDKs, or third-party advertising frameworks.
- Your files never pass through external cloud servers. All transfers and streaming happen directly between devices connected to the same local Wi-Fi network or hotspot.

---

## 2. Information We Handle

### A. Local Files & Media
When you choose to share or stream files (photos, videos, audio, documents, or apps):
- Files are accessed only upon your explicit selection using Android's system photo picker or file chooser.
- Files are served directly from your device over a local HTTP socket connection to devices on your local network.
- No files, metadata, or logs are uploaded to any external server.

### B. Device & Network Information
To establish local network transfers, AnyShare accesses:
- **Local Wi-Fi / IP Address:** Used solely to display the local connection URL (e.g., `http://192.168.x.x:8080`) so other devices can connect via their web browser.
- **Device Name:** A customizable local identifier shown to devices connecting to your transfer session.

---

## 3. Permissions Used and Why

AnyShare requests only the minimum permissions necessary for local peer-to-peer functionality:

| Permission | Purpose |
| :--- | :--- |
| `INTERNET` | To run the local HTTP socket server for peer-to-peer transfers and in-browser streaming over your local Wi-Fi. |
| `ACCESS_WIFI_STATE` & `ACCESS_NETWORK_STATE` | To detect your local Wi-Fi network and determine the local IP address for peer connections. |
| `CHANGE_WIFI_MULTICAST_STATE` | Used for local mDNS / NSD discovery so nearby devices can discover your AnyShare server. |
| `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_DATA_SYNC` | Keeps the local transfer/streaming server active in the background while you navigate to other apps during large transfers. |
| `POST_NOTIFICATIONS` | Displays an active transfer notification allowing you to pause, stop, or view transfer progress from the system shade. |
| `VIBRATE` | Provides subtle haptic feedback for user interactions within the app. |

*Note: AnyShare does not require invasive broad storage permissions (`READ_EXTERNAL_STORAGE` or `READ_MEDIA_*`). Media is accessed exclusively through the privacy-respecting Android System Photo Picker and Storage Access Framework (SAF).*

---

## 4. Third-Party Services
AnyShare does **not** integrate with any third-party analytics (e.g., Google Analytics, Firebase Analytics), advertising networks, or user tracking services.

---

## 5. Security of Your Transfers
- All communication is restricted to your local area network (LAN).
- You can optionally enable PIN Protection in the app settings to require connecting devices to enter a secure code before accessing shared files or streams.

---

## 6. Open Source & Transparency
AnyShare is open source. You can inspect the complete source code, review our security architecture, and verify our privacy practices at:
[https://github.com/Kaifazad/AnyShare](https://github.com/Kaifazad/AnyShare)

---

## 7. Contact Us
If you have any questions or feedback regarding this Privacy Policy, please open an issue on our GitHub repository or contact:
- **Developer:** Kaif Azad
- **GitHub:** [https://github.com/Kaifazad/AnyShare](https://github.com/Kaifazad/AnyShare)
- **Instagram:** [https://instagram.com/kaif.azad](https://instagram.com/kaif.azad)
