import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import '@/styles/globals.css'
import dynamic from 'next/dynamic'

// Import CSS files in a client component to avoid SSR issues
const StylesLoader = dynamic(() => import('@/components/StylesLoader'), { ssr: false })
const NavBar = dynamic(() => import('@/components/NavBar'), { ssr: false })
const BootstrapClient = dynamic(() => import('@/components/BootstrapClient'), { ssr: false })

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'Waters Punchout Platform',
  description: 'Test, monitor, and debug your PunchOut integrations across all environments',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        {/* Components will be loaded client-side */}
        <div suppressHydrationWarning>
          {typeof window === 'undefined' ? null : <StylesLoader />}
          {typeof window === 'undefined' ? null : <NavBar />}
          {typeof window === 'undefined' ? null : <BootstrapClient />}
        </div>
        <main style={{minHeight: '100vh', backgroundColor: '#f8f9fa', paddingTop: '60px'}}>
          {children}
        </main>
        <footer className="bg-primary text-white py-4 mt-5">
          <div className="container text-center">
            <p className="mb-0">&copy; 2025 Waters Corporation - Punchout Platform. All rights reserved.</p>
          </div>
        </footer>
      </body>
    </html>
  )
}
