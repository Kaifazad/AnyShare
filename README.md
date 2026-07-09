<div align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="LocalShare Logo" width="120"/>
  <h1>LocalShare</h1>
  <p><strong>A beautifully crafted, blazing-fast, and deeply integrated offline file sharing app for Android.</strong></p>
  
  <p>
    <a href="https://github.com/Kaifazad/LocalShare/releases/latest"><img src="https://img.shields.io/github/v/release/Kaifazad/LocalShare?style=for-the-badge&color=00BFA5" alt="Latest Release"/></a>
    <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android" alt="Platform: Android"/>
    <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin" alt="Language: Kotlin"/>
    <img src="https://img.shields.io/github/license/Kaifazad/LocalShare?style=for-the-badge&color=blue" alt="License: MIT"/>
  </p>
</div>

---

**LocalShare** is a powerful, open-source Android application designed to seamlessly share files across any device completely offline. By utilizing direct Wi-Fi and hotspot connections alongside a beautifully crafted web interface, LocalShare brings the magic of AirDrop to any device with a browser—no app required on the receiving end.

## ✨ Key Features

- 🚀 **Blazing Fast Speeds:** Share gigabytes of data in seconds utilizing local Wi-Fi speeds without internet routing or cloud servers.
- 🎨 **Stunning UI (Material You):** A premium, fluid interface built entirely in Jetpack Compose, supporting dynamic colors, dark mode, and sleek micro-animations.
- 🌐 **Universal Web Interface:** The receiving device (PC, iPhone, Tablet, etc.) only needs a web browser. The app hosts a gorgeous, responsive Web UI to browse and download shared files.
- 🔒 **Secure by Default:** Protect your local server with an auto-generated or custom 4-digit PIN. Advanced end-to-end encryption coming soon.
- 📱 **Seamless Android Integration:** 
  - Send files directly from other apps via Android's native Share Menu.
  - Automatically pulls your Phone's Clipboard to seamlessly share text links.
  - Quick Settings Tile and Home Screen Widget to start the server instantly.
- 📺 **In-Browser Streaming:** Stream high-quality videos and listen to music directly from the Web UI without needing to download the files first.
- 📂 **Share Anything:** Individual files, multiple files, folders, or even installed Apps (APKs) directly from your device.

## 📥 Installation

**[Download the latest APK from the Releases tab!](https://github.com/Kaifazad/LocalShare/releases/latest)**

1. Download the `app-release.apk` file from the latest release.
2. Open the file on your Android device to install it.
3. *Note: You may need to enable "Install unknown apps" in your Android settings.*

## 🛠️ How It Works

1. **Connect** your Android device and the receiving device (e.g., your laptop) to the same Wi-Fi network (or simply turn on your phone's Mobile Hotspot).
2. **Select files** you wish to share inside the LocalShare app.
3. **Open the browser** on your receiving device and type in the local address provided by the app (e.g., `http://192.168.1.5:8080`).
4. **Enter the PIN** (if enabled) and instantly download or stream your files!

## 💻 Tech Stack & Architecture

LocalShare is built using modern Android development practices and libraries to ensure top-tier performance and maintainability:

- **Language:** 100% Kotlin
- **UI Toolkit:** Jetpack Compose, Material 3
- **Architecture:** MVVM (Model-View-ViewModel) + Kotlin Coroutines & Flow
- **Local Server:** NanoHTTPD (Embedded Java HTTP Server)
- **Database:** Room Database (for transfer history logging)
- **Preferences:** DataStore (for type-safe settings)
- **Media Loading:** Coil (Images) & Media3 ExoPlayer (Video)
- **Web UI:** Vanilla HTML/CSS/JS (embedded efficiently as raw resources)

## 🤝 Contributing

Contributions, issues, and feature requests are always welcome! Feel free to check the [issues page](https://github.com/Kaifazad/LocalShare/issues).

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 👤 Author

**Kaif Azad**
- GitHub: [@kaifazad](https://github.com/kaifazad)
- Instagram: [@kaif.azad](https://instagram.com/kaif.azad)

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.
