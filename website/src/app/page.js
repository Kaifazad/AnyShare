import styles from "./page.module.css";
import Link from "next/link";

const DownloadIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
    <polyline points="7 10 12 15 17 10"></polyline>
    <line x1="12" y1="15" x2="12" y2="3"></line>
  </svg>
);

const GithubIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"></path>
  </svg>
);

const PhoneIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
    <line x1="12" y1="18" x2="12.01" y2="18"></line>
  </svg>
);

const SpeedIcon = () => (
  <svg width="100%" height="100%" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
  </svg>
);

const WebIcon = () => (
  <svg width="100%" height="100%" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10"></circle>
    <line x1="2" y1="12" x2="22" y2="12"></line>
    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
  </svg>
);

const SecureIcon = () => (
  <svg width="100%" height="100%" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
  </svg>
);

const DesignIcon = () => (
  <svg width="100%" height="100%" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 19l7-7 3 3-7 7-3-3z"></path>
    <path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z"></path>
    <path d="M2 2l7.586 7.586"></path>
    <circle cx="11" cy="11" r="2"></circle>
  </svg>
);

const ShieldIcon = () => (
  <svg width="100%" height="100%" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
  </svg>
);

const UploadIcon = () => (
  <svg width="100%" height="100%" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="16 16 12 12 8 16"></polyline>
    <line x1="12" y1="12" x2="12" y2="21"></line>
    <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"></path>
  </svg>
);

export default function Home() {
  return (
    <div className={styles.container}>
      {/* NAVIGATION */}
      <nav className={styles.nav}>
        <div className={styles.navInner}>
          <Link href="/" className={styles.navLogo}>
            <img src="/logo.png" alt="AnyShare Logo" width="32" height="32" style={{ marginRight: '8px' }} />
            <span>AnyShare</span>
          </Link>
          <div className={styles.navLinks}>
            <a href="#features">Features</a>
            <a href="#how-it-works">How It Works</a>
            <Link href="/privacy">Privacy</Link>
            <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer">GitHub</a>
          </div>
        </div>
      </nav>

      <main className={styles.main}>

        {/* HERO SECTION */}
        <section className={styles.hero}>
          <div className={styles.heroBadge}>100% Free &amp; Open Source</div>
          <h1>
            Share Files.<br />
            <span className={styles.heroHighlight}>Completely Offline.</span>
          </h1>
          <p className={styles.heroSubtitle}>
            A blazing-fast, beautifully crafted file sharing app for Android. Share photos, videos, documents, and entire folders with any device on your network &mdash; no internet required.
          </p>
          <div className={styles.buttonGroup}>
            <a href="https://github.com/Kaifazad/AnyShare/releases/latest" target="_blank" rel="noopener noreferrer" className={styles.ctaButton}>
              <DownloadIcon /> Download APK
            </a>
            <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer" className={styles.secondaryButton}>
              <GithubIcon /> View Source
            </a>
          </div>
          <div className={styles.heroVisual}>
            <div className={styles.heroPhone}>
              <div className={styles.heroPhoneScreen}>
                <div className={styles.heroPhoneHeader}>
                  <div className={styles.heroPhoneDot}></div>
                  <span>AnyShare</span>
                </div>
                <div className={styles.heroPhoneContent}>
                  <div className={styles.heroPhoneStatus}>
                    <div className={styles.heroPhoneStatusDot}></div>
                    Server Running
                  </div>
                  <div className={styles.heroPhoneUrl}>192.168.1.5:8080</div>
                  <div className={styles.heroPhoneFiles}>
                    <div className={styles.heroPhoneFile}>
                      <span>vacation.mp4</span><span>245 MB</span>
                    </div>
                    <div className={styles.heroPhoneFile}>
                      <span>photo.jpg</span><span>3.2 MB</span>
                    </div>
                    <div className={styles.heroPhoneFile}>
                      <span>document.pdf</span><span>1.1 MB</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* FEATURES GRID */}
        <section id="features" className={styles.features}>
          <h2 className={styles.sectionTitle}>Why AnyShare?</h2>
          <div className={styles.featuresGrid}>
            <div className={`${styles.featureCard} surface-card`}>
              <div className={`${styles.featureIcon} ${styles.featureIconBlue}`}><SpeedIcon /></div>
              <h3 className={styles.featureTitle}>Blazing Fast</h3>
              <p className={styles.featureDesc}>Transfer gigabytes in seconds. Uses direct local Wi-Fi or hotspot connections for maximum speed.</p>
            </div>
            <div className={`${styles.featureCard} surface-card`}>
              <div className={`${styles.featureIcon} ${styles.featureIconGreen}`}><WebIcon /></div>
              <h3 className={styles.featureTitle}>Cross-Platform Web UI</h3>
              <p className={styles.featureDesc}>No app needed on the other device. Just open a browser and connect. Works on laptops, iPhones, tablets, and more.</p>
            </div>
            <div className={`${styles.featureCard} surface-card`}>
              <div className={`${styles.featureIcon} ${styles.featureIconPurple}`}><SecureIcon /></div>
              <h3 className={styles.featureTitle}>PIN Protected</h3>
              <p className={styles.featureDesc}>Optional PIN authentication keeps your files secure. Only devices with the PIN can access shared content.</p>
            </div>
            <div className={`${styles.featureCard} surface-card`}>
              <div className={`${styles.featureIcon} ${styles.featureIconOrange}`}><ShieldIcon /></div>
              <h3 className={styles.featureTitle}>Encrypted Transfers</h3>
              <p className={styles.featureDesc}>Optional AES-256-GCM encryption for end-to-end secure file transfers. Your data never leaves your network.</p>
            </div>
            <div className={`${styles.featureCard} surface-card`}>
              <div className={`${styles.featureIcon} ${styles.featureIconPink}`}><PhoneIcon /></div>
              <h3 className={styles.featureTitle}>Share Anything</h3>
              <p className={styles.featureDesc}>Photos, videos, audio, documents, APKs, folders, and even clipboard text. Share it all with a few taps.</p>
            </div>
            <div className={`${styles.featureCard} surface-card`}>
              <div className={`${styles.featureIcon} ${styles.featureIconTeal}`}><DesignIcon /></div>
              <h3 className={styles.featureTitle}>Beautiful Design</h3>
              <p className={styles.featureDesc}>Built with Jetpack Compose and Material 3. Multiple themes, dark mode, and a stunning glassmorphic web UI.</p>
            </div>
            <div className={`${styles.featureCard} surface-card`}>
              <div className={`${styles.featureIcon} ${styles.featureIconBlue}`}><UploadIcon /></div>
              <h3 className={styles.featureTitle}>Drag & Drop Upload</h3>
              <p className={styles.featureDesc}>Desktop users can drag files directly into the browser to upload them to the phone. No clicking required.</p>
            </div>
          </div>
        </section>

        {/* HOW IT WORKS */}
        <section id="how-it-works" className={styles.howItWorks}>
          <h2 className={styles.sectionTitle}>How It Works</h2>
          <div className={styles.stepsGrid}>
            <div className={`${styles.stepCard} surface-card`}>
              <span className={styles.stepNumber}>1</span>
              <div className={styles.stepContent}>
                <h3 className={styles.featureTitle}>Start the Server</h3>
                <p className={styles.featureDesc}>
                  Open AnyShare and tap Start. The app spins up a secure local HTTP server and shows you the URL to connect.
                </p>
              </div>
            </div>
            <div className={`${styles.stepCard} surface-card`}>
              <span className={styles.stepNumber}>2</span>
              <div className={styles.stepContent}>
                <h3 className={styles.featureTitle}>Connect Any Device</h3>
                <p className={styles.featureDesc}>
                  Connect to the same Wi-Fi or hotspot, then open the URL in any browser. No app install needed on the receiving device.
                </p>
              </div>
            </div>
            <div className={`${styles.stepCard} surface-card`}>
              <span className={styles.stepNumber}>3</span>
              <div className={styles.stepContent}>
                <h3 className={styles.featureTitle}>Transfer Files</h3>
                <p className={styles.featureDesc}>
                  Browse, preview, and download shared files. Or upload files from the browser back to your phone. It&apos;s that simple.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* OPEN SOURCE SECTION */}
        <section className={styles.openSource}>
          <div className={`${styles.openSourceCard} surface-card`}>
            <h2 className={styles.sectionTitle}>100% Free &amp; Open Source</h2>
            <p>
              No ads, no trackers, no hidden subscriptions. AnyShare is built for the community, by the community. The entire source code is available under the Apache License 2.0.
            </p>
            <div className={styles.buttonGroup} style={{ justifyContent: "center" }}>
              <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer" className={styles.secondaryButton}>
                <GithubIcon /> View on GitHub
              </a>
              <a href="https://github.com/Kaifazad/AnyShare/blob/main/CONTRIBUTING.md" target="_blank" rel="noopener noreferrer" className={styles.secondaryButton}>
                Become a Contributor
              </a>
            </div>
          </div>
        </section>

      </main>

      {/* FOOTER */}
      <footer className={styles.footer}>
        <div className={styles.footerInner}>
          <div className={styles.footerLinks}>
            <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer">GitHub</a>
            <a href="https://github.com/Kaifazad/AnyShare/issues/new/choose" target="_blank" rel="noopener noreferrer">Report a Bug</a>
            <Link href="/privacy">Privacy Policy</Link>
            <a href="https://github.com/Kaifazad/AnyShare/blob/main/CONTRIBUTING.md" target="_blank" rel="noopener noreferrer">Contributing</a>
          </div>
          <p>&copy; {new Date().getFullYear()} Developed by <a href="https://kaifazad.in" target="_blank" rel="noopener noreferrer" style={{textDecoration: 'underline', color: 'var(--foreground)'}}>Kaif Azad</a>. Open Source under the Apache License 2.0.</p>
        </div>
      </footer>
    </div>
  );
}
