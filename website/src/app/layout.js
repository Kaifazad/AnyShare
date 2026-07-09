import "./globals.css";

export const metadata = {
  title: "LocalShare | AirDrop for Everything",
  description: "A beautifully crafted, blazing-fast, and deeply integrated offline file sharing app for Android.",
  keywords: "file sharing, android, offline, jetpack compose, localshare",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
