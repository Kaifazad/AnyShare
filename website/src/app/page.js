'use client';
import { useState } from 'react';
import styles from "./page.module.css";
import Link from "next/link";

const DownloadIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
    <polyline points="7 10 12 15 17 10"></polyline>
    <line x1="12" y1="15" x2="12" y2="3"></line>
  </svg>
);
const GithubIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"></path>
  </svg>
);
const MenuIcon = () => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="3" y1="6" x2="21" y2="6"></line>
    <line x1="3" y1="12" x2="21" y2="12"></line>
    <line x1="3" y1="18" x2="21" y2="18"></line>
  </svg>
);
const CloseIcon = () => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="6" x2="6" y2="18"></line>
    <line x1="6" y1="6" x2="18" y2="18"></line>
  </svg>
);

const features = [
  {
    color: 'featureCardPeach',
    iconColor: 'featureIconPeach',
    title: 'Blazing Fast',
    desc: 'Transfers run at full Wi-Fi speed. Move a 4K video across devices in seconds — no compression, no limits.',
    stat: 'Up to 1 Gbps',
  },
  {
    color: 'featureCardTeal',
    iconColor: 'featureIconTeal',
    title: 'No App Needed',
    desc: 'The receiving device just opens a browser. Laptops, iPhones, tablets — everything works out of the box.',
    stat: 'Any browser',
  },
  {
    color: 'featureCardLavender',
    iconColor: 'featureIconLavender',
    title: 'PIN Protected',
    desc: 'Lock your server with a PIN so only trusted devices can connect. Your files, your control.',
    stat: 'Optional PIN',
  },
  {
    color: 'featureCardMint',
    iconColor: 'featureIconMint',
    title: 'AES-256 Encrypted',
    desc: 'End-to-end encrypted transfers for maximum security on shared networks. Zero data leaves your network.',
    stat: 'AES-256-GCM',
  },
  {
    color: 'featureCardPink',
    iconColor: 'featureIconPink',
    title: 'Clipboard Sync',
    desc: 'Send text from your phone to any connected browser instantly. No cables, no logins, no friction.',
    stat: 'One tap sync',
  },
  {
    color: 'featureCardOchre',
    iconColor: 'featureIconOchre',
    title: 'Share Anything',
    desc: 'Photos, videos, audio, docs, APKs, folders. Drag and drop from desktop. Stream media in the browser.',
    stat: 'All file types',
  },
];

const steps = [
  {
    n: '01',
    title: 'Start the Server',
    desc: 'Open AnyShare and tap Start. A local HTTP server starts instantly and shows you the connection URL.',
  },
  {
    n: '02',
    title: 'Connect Any Device',
    desc: 'Join the same Wi-Fi or hotspot, then open the URL in any browser. Nothing to install on the other side.',
  },
  {
    n: '03',
    title: 'Transfer Files',
    desc: 'Browse, preview, download, or upload. Stream video directly. Share clipboard text. Done in seconds.',
  },
];

export default function Home() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className={styles.container}>

      {/* NAV */}
      <nav className={styles.nav}>
        <div className={styles.navInner}>
          <Link href="/" className={styles.navLogo}>
            <img src="/logo.png" alt="AnyShare" width="30" height="30" className={styles.navLogoImg} />
            <span>AnyShare</span>
          </Link>
          <div className={styles.navLinks}>
            <a href="#features">Features</a>
            <a href="#how-it-works">How It Works</a>
            <Link href="/privacy">Privacy</Link>
            <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer">GitHub</a>
            <a href="https://github.com/Kaifazad/AnyShare/releases/latest" target="_blank" rel="noopener noreferrer" className={styles.navCta}>
              Download
            </a>
          </div>
          <button className={styles.hamburger} onClick={() => setMenuOpen(!menuOpen)} aria-label="Toggle menu">
            {menuOpen ? <CloseIcon /> : <MenuIcon />}
          </button>
        </div>
        {menuOpen && (
          <div className={styles.mobileMenu}>
            <a href="#features" onClick={() => setMenuOpen(false)}>Features</a>
            <a href="#how-it-works" onClick={() => setMenuOpen(false)}>How It Works</a>
            <Link href="/privacy" onClick={() => setMenuOpen(false)}>Privacy</Link>
            <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer" onClick={() => setMenuOpen(false)}>GitHub</a>
            <a href="https://github.com/Kaifazad/AnyShare/releases/latest" target="_blank" rel="noopener noreferrer" className={styles.navCta} onClick={() => setMenuOpen(false)}>Download APK</a>
          </div>
        )}
      </nav>

      <main className={styles.main}>

        {/* HERO */}
        <section className={styles.hero}>
          <div className={styles.heroBadge}>Free &amp; Open Source — Android</div>
          <h1 className={styles.heroHeading}>
            <span className={styles.heroLine1}>Share Files.</span>
            <span className={styles.heroLine2}>Completely Offline.</span>
          </h1>
          <p className={styles.heroSubtitle}>
            Turn your Android phone into a local server. Share photos, videos, documents, and clipboard text with any device on your Wi-Fi — no internet, no cloud, no accounts.
          </p>
          <div className={styles.buttonGroup}>
            <a href="https://github.com/Kaifazad/AnyShare/releases/latest" target="_blank" rel="noopener noreferrer" className={styles.ctaButton}>
              <DownloadIcon /> Download APK
            </a>
            <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer" className={styles.secondaryButton}>
              <GithubIcon /> View on GitHub
            </a>
          </div>

          {/* Stat pills */}
          <div className={styles.heroStats}>
            <div className={styles.heroPill}>100% Offline</div>
            <div className={styles.heroPill}>No Cloud</div>
            <div className={styles.heroPill}>No Account</div>
            <div className={styles.heroPill}>Android 8.0+</div>
          </div>
        </section>

        {/* FEATURES */}
        <section id="features" className={styles.features}>
          <div className={styles.sectionLabel}>Features</div>
          <h2 className={styles.sectionTitle}>Everything you need.<br />Nothing you don't.</h2>
          <div className={styles.featuresGrid}>
            {features.map((f) => (
              <div key={f.title} className={`${styles.featureCard} ${styles[f.color]}`}>
                <p className={styles.featureStat}>{f.stat}</p>
                <h3 className={styles.featureTitle}>{f.title}</h3>
                <p className={styles.featureDesc}>{f.desc}</p>
              </div>
            ))}
          </div>
        </section>

        {/* HOW IT WORKS */}
        <section id="how-it-works" className={styles.howItWorks}>
          <div className={styles.sectionLabel}>How It Works</div>
          <h2 className={styles.sectionTitle}>Three steps.<br />That's it.</h2>
          <div className={styles.stepsGrid}>
            {steps.map((s) => (
              <div key={s.n} className={styles.stepCard}>
                <span className={styles.stepNumber}>{s.n}</span>
                <h3 className={styles.stepTitle}>{s.title}</h3>
                <p className={styles.featureDesc}>{s.desc}</p>
              </div>
            ))}
          </div>
        </section>

        {/* OPEN SOURCE */}
        <section className={styles.openSource}>
          <div className={styles.openSourceCard}>
            <div className={styles.openSourceLabel}>Open Source</div>
            <h2 className={styles.openSourceHeading}>Built for everyone.<br />Free forever.</h2>
            <p className={styles.openSourceDesc}>
              No ads. No trackers. No subscriptions. AnyShare is open source under the Apache License 2.0.
            </p>
            <div className={styles.buttonGroup}>
              <a href="https://github.com/Kaifazad/AnyShare" target="_blank" rel="noopener noreferrer" className={styles.openSourceBtn}>
                <GithubIcon /> View on GitHub
              </a>
              <a href="https://github.com/Kaifazad/AnyShare/blob/main/CONTRIBUTING.md" target="_blank" rel="noopener noreferrer" className={styles.openSourceBtn}>
                Contribute
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
          <p>&copy; {new Date().getFullYear()} Developed by <a href="https://kaifazad.in" target="_blank" rel="noopener noreferrer" className={styles.footerAuthor}>Kaif Azad</a>. Apache License 2.0.</p>
        </div>
      </footer>
    </div>
  );
}
