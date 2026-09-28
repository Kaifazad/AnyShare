import "./globals.css";

export const metadata = {
  title: "AnyShare | Share Files. Completely Offline.",
  description: "A blazing-fast, beautifully crafted offline file sharing app for Android. No internet, no cloud, no accounts.",
  keywords: "file sharing, android, offline, wifi, anyshare",
  icons: {
    icon: "/logo.png",
    apple: "/logo.png",
  },
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <head>
        <link rel="icon" href="/logo.png" type="image/png" />
      </head>
      <body>{children}</body>
    </html>
  );
}
