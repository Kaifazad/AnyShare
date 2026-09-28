import Link from "next/link";
import styles from "../page.module.css";

export const metadata = {
  title: "Privacy Policy | AnyShare",
  description: "AnyShare does not collect, store, or share any personal data. All file transfers happen locally on your network.",
};

const BackIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="19" y1="12" x2="5" y2="12"></line>
    <polyline points="12 19 5 12 12 5"></polyline>
  </svg>
);

const Section = ({ number, title, children }) => (
  <section style={{ marginBottom: '2.5rem' }}>
    <h2 style={{
      fontFamily: "'Syne', sans-serif",
      fontSize: 'clamp(1.15rem, 3vw, 1.35rem)',
      fontWeight: 700,
      color: '#0a0a0a',
      marginBottom: '1rem',
      letterSpacing: '-0.02em',
      display: 'flex',
      alignItems: 'center',
      gap: '0.75rem',
    }}>
      <span style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: '28px',
        height: '28px',
        background: '#0a0a0a',
        color: '#fffaf0',
        borderRadius: '8px',
        fontSize: '0.75rem',
        fontWeight: 800,
        flexShrink: 0,
      }}>{number}</span>
      {title}
    </h2>
    <div style={{ color: '#3a3a3a', lineHeight: 1.8, fontSize: '0.95rem' }}>
      {children}
    </div>
  </section>
);

const Pill = ({ children, color = '#b2e8e0' }) => (
  <span style={{
    display: 'inline-block',
    background: color,
    color: '#0a0a0a',
    padding: '0.25rem 0.75rem',
    borderRadius: '50px',
    fontSize: '0.82rem',
    fontWeight: 600,
    marginRight: '0.5rem',
    marginBottom: '0.5rem',
  }}>{children}</span>
);

export default function PrivacyPolicy() {
  return (
    <div className={styles.container}>
      {/* NAV */}
      <nav className={styles.nav}>
        <div className={styles.navInner}>
          <Link href="/" className={styles.navLogo}>
            <img src="/logo.png" alt="AnyShare" width="32" height="32" style={{ borderRadius: '8px' }} />
            <span>AnyShare</span>
          </Link>
          <div className={styles.navLinks}>
            <Link href="/#features">Features</Link>
            <Link href="/#how-it-works">How It Works</Link>
            <Link href="/privacy">Privacy</Link>
            <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer">GitHub</a>
            <a href="https://github.com/Kaifazad/AnyShare/releases/latest" target="_blank" rel="noopener noreferrer" className={styles.navCta}>
              Download APK
            </a>
          </div>
        </div>
      </nav>

      <main style={{ flex: 1, padding: '4rem 2rem', maxWidth: '800px', margin: '0 auto', width: '100%' }}>
        {/* Back */}
        <Link href="/" style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '0.4rem',
          color: '#3a3a3a',
          fontWeight: 600,
          fontSize: '0.88rem',
          marginBottom: '3rem',
          padding: '0.5rem 1rem',
          background: '#fff',
          border: '1px solid rgba(10,10,10,0.1)',
          borderRadius: '10px',
          transition: 'transform 0.2s',
        }}>
          <BackIcon /> Back to Home
        </Link>

        {/* Header */}
        <div style={{ marginBottom: '3.5rem' }}>
          <div style={{
            display: 'inline-block',
            background: '#b2e8e0',
            color: '#1a5c55',
            padding: '0.35rem 1rem',
            borderRadius: '50px',
            fontSize: '0.75rem',
            fontWeight: 700,
            letterSpacing: '0.04em',
            textTransform: 'uppercase',
            marginBottom: '1.25rem',
          }}>
            Last updated: September 28, 2026
          </div>
          <h1 style={{
            fontFamily: "'Syne', sans-serif",
            fontSize: 'clamp(2.2rem, 6vw, 3.5rem)',
            fontWeight: 800,
            letterSpacing: '-0.04em',
            color: '#0a0a0a',
            lineHeight: 1.05,
            marginBottom: '1.25rem',
          }}>
            Privacy Policy
          </h1>
          <p style={{ color: '#3a3a3a', fontSize: '1.05rem', lineHeight: 1.75, maxWidth: '600px' }}>
            AnyShare is built on a simple principle: your files are yours. We have no servers, no accounts, and no data collection.
          </p>
        </div>

        {/* TL;DR Card */}
        <div style={{
          background: '#0a0a0a',
          borderRadius: '20px',
          padding: '2rem',
          marginBottom: '3.5rem',
        }}>
          <p style={{
            fontFamily: "'Syne', sans-serif",
            fontSize: '0.75rem',
            fontWeight: 700,
            color: 'rgba(255,250,240,0.5)',
            textTransform: 'uppercase',
            letterSpacing: '0.08em',
            marginBottom: '1rem',
          }}>TL;DR — The Short Version</p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
            <Pill color="#b2e8e0">No data collected</Pill>
            <Pill color="#d4c5f9">No internet required</Pill>
            <Pill color="#ffd6b3">No account needed</Pill>
            <Pill color="#f7c5d5">No ads or trackers</Pill>
            <Pill color="#f5e5a0">No cloud storage</Pill>
            <Pill color="#bfedd4">100% local transfers</Pill>
          </div>
        </div>

        {/* Sections */}
        <Section number="1" title="Overview">
          <p style={{ marginBottom: '1rem' }}>
            AnyShare is an offline file sharing application designed to transfer files between devices on the same local network. We are committed to protecting your privacy. This policy explains what data the app accesses and how it is used.
          </p>
          <p>
            AnyShare never connects to the internet during file transfers. It operates entirely within your local Wi-Fi network or mobile hotspot.
          </p>
        </Section>

        <Section number="2" title="Data Collection">
          <p style={{ marginBottom: '1rem' }}>AnyShare is designed with privacy as a core principle:</p>
          <ul style={{ paddingLeft: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.625rem' }}>
            <li><strong style={{ color: '#0a0a0a' }}>No data is sent to external servers.</strong> All file transfers happen directly between devices on your local network.</li>
            <li><strong style={{ color: '#0a0a0a' }}>No analytics or tracking.</strong> The app does not collect usage statistics, device information, or any personal data for third-party services.</li>
            <li><strong style={{ color: '#0a0a0a' }}>No account required.</strong> AnyShare does not require registration, login, or any account creation.</li>
            <li><strong style={{ color: '#0a0a0a' }}>No internet required.</strong> The app works entirely offline over your local Wi-Fi or hotspot connection.</li>
          </ul>
        </Section>

        <Section number="3" title="File Access">
          <p style={{ marginBottom: '1rem' }}>To share files, AnyShare requires access to your device storage. This access is used solely to:</p>
          <ul style={{ paddingLeft: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.625rem' }}>
            <li>Browse and select files, photos, videos, and folders that you explicitly choose to share.</li>
            <li>Receive files from other devices and save them to your Downloads/AnyShare folder.</li>
            <li>Generate thumbnails for quick file preview in the web UI.</li>
          </ul>
          <p style={{ marginTop: '1rem' }}>
            Files are only accessible when you explicitly share them. The app does not access your files in the background.
          </p>
        </Section>

        <Section number="4" title="Network Communication">
          <p style={{ marginBottom: '1rem' }}>
            AnyShare creates a local HTTP server on your device to serve shared files to other devices on the same network. This server:
          </p>
          <ul style={{ paddingLeft: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.625rem' }}>
            <li>Only listens on your local network (Wi-Fi or hotspot).</li>
            <li>Does not communicate with any external servers or cloud services.</li>
            <li>Can be protected with an optional PIN code for additional security.</li>
            <li>Can optionally encrypt all file transfers using AES-256-GCM.</li>
          </ul>
        </Section>

        <Section number="5" title="Permissions">
          <p style={{ marginBottom: '1rem' }}>AnyShare may request the following Android permissions:</p>
          <ul style={{ paddingLeft: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.625rem' }}>
            <li><strong style={{ color: '#0a0a0a' }}>Storage / Media access</strong> — to browse and share files you select.</li>
            <li><strong style={{ color: '#0a0a0a' }}>Notifications</strong> — to show server status and incoming transfer alerts.</li>
            <li><strong style={{ color: '#0a0a0a' }}>Wi-Fi state</strong> — to detect your local IP address for the connection URL.</li>
            <li><strong style={{ color: '#0a0a0a' }}>Wake Lock</strong> — to keep the server running while the screen is off.</li>
          </ul>
        </Section>

        <Section number="6" title="Third-Party Services">
          <p style={{ marginBottom: '1rem' }}>
            AnyShare uses the following third-party components:
          </p>
          <ul style={{ paddingLeft: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.625rem' }}>
            <li><strong style={{ color: '#0a0a0a' }}>Google Fonts</strong> — Used to load fonts in the web UI. Google may collect font request data per their privacy policy.</li>
            <li><strong style={{ color: '#0a0a0a' }}>GitHub</strong> — Used only for in-app update checks (checking the latest release version). No personal data is sent.</li>
          </ul>
        </Section>

        <Section number="7" title="Data Storage">
          <p>
            All app settings are stored locally on your device using Android DataStore. Shared file lists and transfer history are stored in a local Room database. None of this data leaves your device.
          </p>
        </Section>

        <Section number="8" title="Children&apos;s Privacy">
          <p>
            AnyShare is not directed at children under 13. We do not knowingly collect personal information from children.
          </p>
        </Section>

        <Section number="9" title="Changes to This Policy">
          <p>
            We may update this Privacy Policy from time to time. Any changes will be posted on this page with an updated revision date.
          </p>
        </Section>

        <Section number="10" title="Contact">
          <p>
            If you have questions about this Privacy Policy, please open an issue on our{' '}
            <a
              href="https://github.com/Kaifazad/AnyShare/issues"
              target="_blank"
              rel="noopener noreferrer"
              style={{ color: '#0a0a0a', fontWeight: 600, textDecoration: 'underline', textUnderlineOffset: '3px' }}
            >
              GitHub repository
            </a>.
          </p>
        </Section>
      </main>

      {/* FOOTER */}
      <footer className={styles.footer}>
        <div className={styles.footerInner}>
          <div className={styles.footerLinks}>
            <Link href="/">Home</Link>
            <Link href="/privacy">Privacy Policy</Link>
            <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer">GitHub</a>
          </div>
          <p>&copy; {new Date().getFullYear()} Developed by{' '}
            <a href="https://kaifazad.in" target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'underline' }}>
              Kaif Azad
            </a>. Open Source under the Apache License 2.0.</p>
        </div>
      </footer>
    </div>
  );
}
