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
  title: 'punchout B2B',
  description: 'Convert cXML documents to JSON format',
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
        <main className="min-h-screen bg-gray-50">
          {children}
        </main>
        <footer className="bg-primary text-white py-6 mt-12">
          <div className="container mx-auto px-4 text-center">
            <p>&copy; 2025 punchout B2B. All rights reserved.</p>
          </div>
        </footer>
      </body>
    </html>
  )
}
