import styles from "./page.module.css";

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
          <a href="https://github.com/Kaifazad/LocalShare/releases/latest" target="_blank" rel="noopener noreferrer" className={styles.ctaButton}>
            Download APK
          </a>
        </section>

        {/* FEATURES GRID */}
        <section className={styles.features}>
          
          <div className="glass-card">
            <div className={styles.featureIcon}>⚡</div>
            <h3 className={styles.featureTitle}>Blazing Fast</h3>
            <p className={styles.featureDesc}>Transfers files using a direct local hotspot connection, maximizing speed without relying on the internet.</p>
          </div>

          <div className="glass-card">
            <div className={styles.featureIcon}>🌐</div>
            <h3 className={styles.featureTitle}>Cross-Platform Web UI</h3>
            <p className={styles.featureDesc}>Don't have the app on your laptop? Just open a browser. LocalShare serves a beautiful web UI for any device to connect.</p>
          </div>

          <div className="glass-card">
            <div className={styles.featureIcon}>🔒</div>
            <h3 className={styles.featureTitle}>Secure by Design</h3>
            <p className={styles.featureDesc}>All file transfers are protected with PIN authentication and localized entirely to your physical environment.</p>
          </div>

          <div className="glass-card">
            <div className={styles.featureIcon}>🎨</div>
            <h3 className={styles.featureTitle}>Material 3 Aesthetics</h3>
            <p className={styles.featureDesc}>Built purely with Jetpack Compose, featuring a stunning glassmorphic UI, fluid animations, and dynamic colors.</p>
          </div>

        </section>

      </main>

      {/* FOOTER */}
      <footer className={styles.footer}>
        <div className={styles.footerLinks}>
          <a href="https://github.com/Kaifazad/LocalShare" target="_blank" rel="noopener noreferrer">GitHub Source</a>
          <a href="https://github.com/Kaifazad/LocalShare/issues" target="_blank" rel="noopener noreferrer">Report a Bug</a>
        </div>
        <p>© {new Date().getFullYear()} Kaif Azad. Open Source under the MIT License.</p>
      </footer>
    </div>
  );
}
