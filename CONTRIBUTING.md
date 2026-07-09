# Contributing to LocalShare

First off, thank you for considering contributing to LocalShare! It's people like you that make LocalShare such a great app. We welcome contributions from everyone, whether it's fixing a bug, adding a new feature, or improving documentation.

## How Can I Contribute?

### 1. Reporting Bugs
If you find a bug, please check the [Issues](https://github.com/Kaifazad/LocalShare/issues) page to see if it's already been reported. If not, open a new issue using the **Bug Report** template. Please include as much detail as possible to help us reproduce the bug!

### 2. Suggesting Enhancements (New Features)
Have a great idea for LocalShare? We'd love to hear it!
- Check if someone else has already suggested it.
- If not, open an issue using the **Feature Request** template.
- Explain the feature clearly, why it's needed, and how it improves the app.

### 3. Code Contributions (Pull Requests)
We welcome code contributions! If you're ready to write some code:
1. **Fork** the repository.
2. **Clone** your fork locally: `git clone https://github.com/YOUR-USERNAME/LocalShare.git`
3. **Create a branch** for your feature or bug fix: `git checkout -b feature/AmazingNewFeature`
4. **Write your code!** Try to follow the existing Kotlin coding conventions and Material 3 design guidelines.
5. **Commit your changes** with a clear and descriptive commit message.
6. **Push** to your fork: `git push origin feature/AmazingNewFeature`
7. **Open a Pull Request** to the `main` branch of this repository.

## Coding Guidelines
- **Language**: This project uses **Kotlin** exclusively.
- **UI Framework**: All UI is built using **Jetpack Compose**.
- **Architecture**: We try to follow MVVM (Model-View-ViewModel). Please keep UI logic out of the Compose functions and put it in ViewModels.
- **Testing**: If you add a feature, try to verify it works across different Android API versions (Minimum API 26).

## Development Setup
1. Download and install the latest **Android Studio**.
2. Open the project folder.
3. Wait for Gradle to sync.
4. Run the app on an emulator or physical device.

We look forward to reviewing your contributions! Let's build the best offline sharing app together! 🚀
