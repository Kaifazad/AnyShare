<div align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="AnyShare Logo" width="120"/>
  <h1>AnyShare - Stream</h1>
  <p><strong>Blazing-fast, offline file sharing for Android — no internet, no cloud, no tracking. Stream your movies locally.</strong></p>

  <p>
    <a href="https://github.com/Kaifazad/AnyShare/releases/latest">
      <img src="https://img.shields.io/badge/⬇️%20Download-Latest%20Release-0070F3?style=for-the-badge" alt="Download Latest Release"/>
    </a>
    <img src="https://img.shields.io/badge/▶️%20Google%20Play-Coming%20Soon-black?style=for-the-badge&logo=googleplay" alt="Coming Soon on Play Store"/>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=for-the-badge&logo=android" alt="Platform: Android 8.0+"/>
    <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin" alt="Language: Kotlin"/>
    <img src="https://img.shields.io/github/license/Kaifazad/AnyShare?style=for-the-badge&color=blue" alt="License: Apache 2.0"/>
  </p>
</div>

---

**AnyShare** is a free, open-source Android app that lets you share files with any device on your local network — no internet required. Your phone hosts a clean web server; anyone on the same Wi-Fi opens a browser, types the URL, and downloads or uploads files instantly. No app needed on the other end.

---

## 📸 Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center">
        <img src="docs/screenshots/screenshot_home.png" alt="Home Screen" width="220"/>
        <br/><sub><b>Home Screen</b></sub>
      </td>
      <td align="center">
        <img src="docs/screenshots/screenshot_files.png" alt="Shared Files" width="220"/>
        <br/><sub><b>Shared Files</b></sub>
      </td>
      <td align="center">
        <img src="docs/screenshots/screenshot_webui.png" alt="Web UI" width="220"/>
        <br/><sub><b>Web UI (Browser)</b></sub>
      </td>
    </tr>
  </table>
</div>

> **To add screenshots:** Create a `docs/screenshots/` folder in the repo root and add `screenshot_home.png`, `screenshot_files.png`, and `screenshot_webui.png`.

---

## ✨ Features

- 🚀 **Blazing Fast** — Transfers run at full local Wi-Fi speed with no internet routing or cloud middlemen
- 🌐 **Universal Web UI** — Any browser on any device (PC, iPhone, tablet) can browse, download, and upload files
- 🖱️ **Drag & Drop Upload** — On desktop, drag files straight into the browser to send them to your phone
- 🔒 **PIN Protection** — Optional 4-digit PIN locks your server so only trusted devices connect
- 🔐 **End-to-End Encryption** — Optional AES-256-GCM encryption for extra-sensitive transfers
- 📱 **Deep Android Integration** — Share from any app via the native Share Menu; Quick Settings Tile & Home Screen Widget
- 📺 **In-Browser Streaming** — Stream video and audio directly in the browser without downloading
- 📂 **Share Anything** — Files, folders, multiple items, APKs, and clipboard text
- 🎨 **Material You Design** — Dynamic colors, dark mode, smooth animations built with Jetpack Compose

---

## 📥 Download

**[⬇️ Download AnyShare-v1.3.0.apk](https://github.com/Kaifazad/AnyShare/releases/latest)**

1. Download the APK from the Releases page
2. Open the file on your Android phone
3. Enable **"Install from unknown sources"** if prompted and install

**Minimum:** Android 8.0 (API 26)

---

## 🛠️ How It Works

1. **Start the server** — Open AnyShare and tap the Start button. The app shows you a local URL (e.g. `http://192.168.1.5:8080`)
2. **Connect** — Make sure the receiving device is on the same Wi-Fi or hotspot
3. **Open the browser** — Type the URL into any browser on the receiving device
4. **Transfer files** — Browse and download shared files, or drag & drop files to upload back to your phone

---

## 💻 Tech Stack

| Layer | Technology |
|---|---|
| Language | 100% Kotlin |
| UI | Jetpack Compose + Material 3 (Material You) |
| Architecture | MVVM + Kotlin Coroutines & Flow |
| Local Server | NanoHTTPD (embedded HTTP server) |
| Encryption | AES-256-GCM (end-to-end) |
| Preferences | DataStore |
| Media | Coil (images) + Media3 ExoPlayer (video) |
| Web UI | Vanilla HTML / CSS / JS |

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Check the [issues page](https://github.com/Kaifazad/AnyShare/issues) or read [CONTRIBUTING.md](CONTRIBUTING.md).

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'Add my feature'`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 👤 Author

**Kaif Azad**
- GitHub: [@Kaifazad](https://github.com/Kaifazad)
- Instagram: [@kaif.azad](https://instagram.com/kaif.azad)
- Website: [anyshare.kaifazad.in](https://anyshare.kaifazad.in)

---

## 📝 License

Distributed under the MIT License. See [LICENSE](LICENSE) for details.
