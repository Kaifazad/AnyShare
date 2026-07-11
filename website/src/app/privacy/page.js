import styles from "../page.module.css";
import Link from "next/link";

export const metadata = {
  title: "Privacy Policy | LocalShare",
  description: "Privacy Policy for LocalShare - Offline file sharing app for Android.",
};

export default function PrivacyPolicy() {
  return (
    <div className={styles.container}>
      <main className={styles.main} style={{ maxWidth: 800 }}>
        <Link href="/" style={{ color: "var(--accent-primary)", fontWeight: 600, marginBottom: "2rem", display: "inline-block" }}>
          &larr; Back to Home
        </Link>

        <h1 style={{ fontSize: "clamp(2rem, 5vw, 3rem)", fontWeight: 800, marginBottom: "1rem" }}>Privacy Policy</h1>
        <p style={{ color: "var(--foreground-secondary)", marginBottom: "0.5rem" }}>Last updated: July 10, 2026</p>

        <div style={{ marginTop: "3rem", lineHeight: 1.8, color: "var(--foreground-secondary)" }}>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>1. Overview</h2>
          <p style={{ marginBottom: "1.5rem" }}>
            LocalShare is an offline file sharing application designed to transfer files between devices on the same local network. We are committed to protecting your privacy. This policy explains what data the app accesses and how it is used.
          </p>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>2. Data Collection</h2>
          <p style={{ marginBottom: "1rem" }}>LocalShare is designed with privacy as a core principle. Here is what you should know:</p>
          <ul style={{ marginBottom: "1.5rem", paddingLeft: "1.5rem" }}>
            <li style={{ marginBottom: "0.5rem" }}><strong style={{ color: "var(--foreground)" }}>No data is sent to external servers.</strong> All file transfers happen directly between devices on your local network.</li>
            <li style={{ marginBottom: "0.5rem" }}><strong style={{ color: "var(--foreground)" }}>No analytics or tracking.</strong> The app does not collect usage statistics, device information, or any personal data for third-party services.</li>
            <li style={{ marginBottom: "0.5rem" }}><strong style={{ color: "var(--foreground)" }}>No account required.</strong> LocalShare does not require registration, login, or any account creation.</li>
            <li style={{ marginBottom: "0.5rem" }}><strong style={{ color: "var(--foreground)" }}>No internet required.</strong> The app works entirely offline over your local Wi-Fi or hotspot connection.</li>
          </ul>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>3. File Access</h2>
          <p style={{ marginBottom: "1.5rem" }}>
            To share files, LocalShare requires access to your device storage. This access is used solely to:
          </p>
          <ul style={{ marginBottom: "1.5rem", paddingLeft: "1.5rem" }}>
            <li style={{ marginBottom: "0.5rem" }}>Browse and select files, photos, videos, and folders that you explicitly choose to share.</li>
            <li style={{ marginBottom: "0.5rem" }}>Receive files from other devices and save them to your Downloads/LocalShare folder.</li>
            <li style={{ marginBottom: "0.5rem" }}>Generate thumbnails for quick file preview in the web UI.</li>
          </ul>
          <p style={{ marginBottom: "1.5rem" }}>
            Files are only accessible when you explicitly share them. The app does not access your files in the background.
          </p>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>4. Network Communication</h2>
          <p style={{ marginBottom: "1.5rem" }}>
            LocalShare creates a local HTTP server on your device to serve shared files to other devices on the same network. This server:
          </p>
          <ul style={{ marginBottom: "1.5rem", paddingLeft: "1.5rem" }}>
            <li style={{ marginBottom: "0.5rem" }}>Only listens on your local network (Wi-Fi or hotspot).</li>
            <li style={{ marginBottom: "0.5rem" }}>Does not communicate with any external servers or cloud services.</li>
            <li style={{ marginBottom: "0.5rem" }}>Can be protected with an optional PIN code for additional security.</li>
            <li style={{ marginBottom: "0.5rem" }}>Can optionally encrypt all file transfers using AES-256-GCM.</li>
          </ul>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>5. Notifications</h2>
          <p style={{ marginBottom: "1.5rem" }}>
            LocalShare may request permission to send notifications. Notifications are used only to inform you about:
          </p>
          <ul style={{ marginBottom: "1.5rem", paddingLeft: "1.5rem" }}>
            <li style={{ marginBottom: "0.5rem" }}>Server status (running/stopped).</li>
            <li style={{ marginBottom: "0.5rem" }}>Incoming file transfer requests from other devices.</li>
            <li style={{ marginBottom: "0.5rem" }}>File access events (when someone downloads a shared file).</li>
          </ul>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>6. Third-Party Services</h2>
          <p style={{ marginBottom: "1.5rem" }}>
            LocalShare uses the following third-party components, each with their own privacy policies:
          </p>
          <ul style={{ marginBottom: "1.5rem", paddingLeft: "1.5rem" }}>
            <li style={{ marginBottom: "0.5rem" }}><strong style={{ color: "var(--foreground)" }}>Google Fonts</strong> &mdash; Used to load the DM Sans font in the web UI. Google may collect font request data per their privacy policy.</li>
            <li style={{ marginBottom: "0.5rem" }}><strong style={{ color: "var(--foreground)" }}>GitHub</strong> &mdash; Used only for in-app update checks (checking the latest release version). No personal data is sent.</li>
          </ul>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>7. Data Storage</h2>
          <p style={{ marginBottom: "1.5rem" }}>
            All app settings are stored locally on your device using Android DataStore. Shared file lists and transfer history are stored in a local Room database. None of this data leaves your device.
          </p>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>8. Children&apos;s Privacy</h2>
          <p style={{ marginBottom: "1.5rem" }}>
            LocalShare is not directed at children under 13. We do not knowingly collect personal information from children.
          </p>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>9. Changes to This Policy</h2>
          <p style={{ marginBottom: "1.5rem" }}>
            We may update this Privacy Policy from time to time. Any changes will be posted on this page with an updated revision date.
          </p>

          <h2 style={{ fontSize: "1.5rem", fontWeight: 700, color: "var(--foreground)", marginBottom: "1rem", marginTop: "2.5rem" }}>10. Contact</h2>
          <p style={{ marginBottom: "1.5rem" }}>
            If you have questions about this Privacy Policy, please open an issue on our{' '}
            <a href="https://github.com/Kaifazad/LocalShare/issues" target="_blank" rel="noopener noreferrer" style={{ color: "var(--accent-primary)", fontWeight: 600 }}>GitHub repository</a>.
          </p>
        </div>
      </main>

      <footer className={styles.footer}>
        <div className={styles.footerLinks}>
          <Link href="/">Home</Link>
          <Link href="/privacy">Privacy Policy</Link>
          <a href="https://github.com/Kaifazad/LocalShare" target="_blank" rel="noopener noreferrer">GitHub</a>
        </div>
        <p>&copy; {new Date().getFullYear()} Developed by <a href="https://kaifazad.in" target="_blank" rel="noopener noreferrer" style={{textDecoration: 'underline', color: 'var(--foreground)'}}>Kaif Azad</a>. Open Source under the MIT License.</p>
      </footer>
    </div>
  );
}
