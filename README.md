# LocalShare

LocalShare is a modern, fast, and open-source Android application that enables you to share files across devices securely, completely offline. Utilizing Wi-Fi direct connections, it provides high-speed file transfers without relying on external cloud servers or internet connectivity.

## Features
- **High-Speed Transfers:** Experience lightning-fast file sharing between devices on the same Wi-Fi network.
- **Web UI:** Access a beautifully designed, responsive Web UI on any connected device (PC, Tablet, iPhone, or another Android) to download or stream files.
- **Secure:** Protect your local server with a custom 4-digit PIN to prevent unauthorized access.
- **Custom Folders:** Easily select specific folders to share their entire contents instantly.
- **Modern UI:** Built with Material 3, featuring fluid animations, a pill-shaped floating navigation bar, and automatic Dark/Light mode support.
- **Media Streaming:** Stream videos and audio files directly from the Web UI without needing to download them first.

## Screenshots
*(Add your screenshots here)*

## How it Works
1. Connect your Android device and the receiving device to the same Wi-Fi network (or use a Mobile Hotspot).
2. Open LocalShare on your Android device and select the files, apps, or folders you want to share.
3. Open the provided Web UI URL (e.g., `http://192.168.x.x:8080`) on the receiving device's browser.
4. Enter the 4-digit PIN (if enabled) and browse, download, or stream your shared files!

## Building from Source
1. Clone this repository: `git clone https://github.com/Kaifazad/LocalShare.git`
2. Open the project in Android Studio.
3. Build and run the project on your Android device or emulator.

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3
- **Server:** NanoHTTPD (Embedded HTTP Server)
- **Web UI:** Vanilla HTML/CSS/JS (embedded)

## Author
**Kaif Azad**
- GitHub: [@kaifazad](https://github.com/kaifazad)
- Instagram: [@kaif.azad](https://instagram.com/kaif.azad)

## License
This project is open-sourced under the MIT License.
