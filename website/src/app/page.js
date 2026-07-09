import styles from "./page.module.css";

// SVG Components
const DownloadIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
    <polyline points="7 10 12 15 17 10"></polyline>
    <line x1="12" y1="15" x2="12" y2="3"></line>
  </svg>
);

const GithubIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"></path>
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

export default function Home() {
  return (
    <div className={styles.container}>
      <main className={styles.main}>
        
        {/* HERO SECTION */}
        <section className={styles.hero}>
          <h1>
            AirDrop for Everything.<br />
            <span className="gradient-text">Completely Offline.</span>
          </h1>
          <p>
            A beautifully crafted, blazing-fast, and deeply integrated offline file sharing app for Android. Share gigabytes of data in seconds without an internet connection.
          </p>
          <div className={styles.buttonGroup}>
            <a href="https://github.com/Kaifazad/LocalShare/releases/latest" target="_blank" rel="noopener noreferrer" className={styles.ctaButton}>
              <DownloadIcon /> Download Latest APK
            </a>
            <a href="https://github.com/Kaifazad/LocalShare" target="_blank" rel="noopener noreferrer" className={styles.secondaryButton}>
              <GithubIcon /> View on GitHub
            </a>
          </div>
        </section>

        {/* FEATURES GRID */}
        <section className={styles.features}>
          <div className="glass-card">
            <div className={styles.featureIcon}><SpeedIcon /></div>
            <h3 className={styles.featureTitle}>Blazing Fast</h3>
            <p className={styles.featureDesc}>Transfers files using a direct local hotspot connection, maximizing speed without relying on external internet routing.</p>
          </div>
          <div className="glass-card">
            <div className={styles.featureIcon}><WebIcon /></div>
            <h3 className={styles.featureTitle}>Cross-Platform Web UI</h3>
            <p className={styles.featureDesc}>Don't have the app on your laptop? Just open a browser. LocalShare serves a beautiful web UI for any device to connect seamlessly.</p>
          </div>
          <div className="glass-card">
            <div className={styles.featureIcon}><SecureIcon /></div>
            <h3 className={styles.featureTitle}>Secure by Design</h3>
            <p className={styles.featureDesc}>All file transfers are protected with dynamic PIN authentication and localized entirely to your physical environment.</p>
          </div>
          <div className="glass-card">
            <div className={styles.featureIcon}><DesignIcon /></div>
            <h3 className={styles.featureTitle}>Material 3 Aesthetics</h3>
            <p className={styles.featureDesc}>Built purely with Jetpack Compose, featuring a stunning glassmorphic UI, fluid animations, and dynamic colors.</p>
          </div>
        </section>

        {/* HOW IT WORKS */}
        <section className={styles.howItWorks}>
          <h2 className={styles.sectionTitle}>How It Works (A to Z)</h2>
          <div className={styles.stepsGrid}>
            <div className={`${styles.stepCard} glass-card`}>
              <span className={styles.stepNumber}>1</span>
              <div className={styles.stepContent}>
                <h3 className={styles.featureTitle}>Start the Server</h3>
                <p className={styles.featureDesc}>
                  Open LocalShare on your Android device and tap "Start". The app instantly spins up a secure local HTTP server on your phone and automatically manages your WiFi Hotspot to prepare for incoming connections.
                </p>
              </div>
            </div>
            <div className={`${styles.stepCard} glass-card`}>
              <span className={styles.stepNumber}>2</span>
              <div className={styles.stepContent}>
                <h3 className={styles.featureTitle}>Connect Any Device</h3>
                <p className={styles.featureDesc}>
                  Connect your laptop, iPhone, or another Android to the generated Hotspot. Then, either scan the provided QR Code or type the local IP address into your favorite web browser. No installation required on the receiving device!
                </p>
              </div>
            </div>
            <div className={`${styles.stepCard} glass-card`}>
              <span className={styles.stepNumber}>3</span>
              <div className={styles.stepContent}>
                <h3 className={styles.featureTitle}>Transfer & Authenticate</h3>
                <p className={styles.featureDesc}>
                  Select the files you want to share. The receiving device will be prompted to enter a secure, auto-generated PIN to authorize the transfer. Once authorized, files stream at maximum local network speeds!
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* OPEN SOURCE SECTION */}
        <section className={styles.openSource}>
          <h2 className={styles.sectionTitle}>100% Free & Open Source</h2>
          <p>
            LocalShare is built for the community, by the community. There are no ads, no trackers, and no hidden subscriptions. The entire source code is available under the MIT License.
          </p>
          <a href="https://github.com/Kaifazad/LocalShare/blob/main/CONTRIBUTING.md" target="_blank" rel="noopener noreferrer" className={styles.secondaryButton}>
            Become a Contributor
          </a>
        </section>

      </main>

      {/* FOOTER */}
      <footer className={styles.footer}>
        <div className={styles.footerLinks}>
          <a href="https://github.com/Kaifazad/LocalShare" target="_blank" rel="noopener noreferrer">GitHub Source</a>
          <a href="https://github.com/Kaifazad/LocalShare/issues/new/choose" target="_blank" rel="noopener noreferrer">Report a Bug / Request Feature</a>
        </div>
        <p>© {new Date().getFullYear()} Kaif Azad. Open Source under the MIT License.</p>
      </footer>
    </div>
  );
}
