'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const NavBar = () => {
  const pathname = usePathname();

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-gradient-to-r from-blue-600 to-purple-600 shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo/Brand */}
          <Link href="/" className="flex items-center space-x-3">
            <div className="bg-white rounded-lg p-2 shadow">
              <i className="fas fa-rocket text-2xl text-blue-600"></i>
            </div>
            <span className="text-white font-bold text-xl hidden md:block">
              Waters Punchout Platform
            </span>
          </Link>
          
          {/* Navigation Menu */}
          <div className="flex items-center space-x-1">
            <Link 
              href="/" 
              className={`px-4 py-2 rounded-lg font-medium transition-all ${
                pathname === '/' 
                  ? 'bg-white text-blue-600 shadow-lg' 
                  : 'text-white hover:bg-white hover:bg-opacity-10'
              }`}
            >
              <i className="fas fa-home mr-2"></i>
              <span className="hidden md:inline">Dashboard</span>
            </Link>
            
            <Link 
              href="/sessions" 
              className={`px-4 py-2 rounded-lg font-medium transition-all ${
                pathname?.startsWith('/sessions') 
                  ? 'bg-white text-blue-600 shadow-lg' 
                  : 'text-white hover:bg-white hover:bg-opacity-10'
              }`}
            >
              <i className="fas fa-plug mr-2"></i>
              <span className="hidden md:inline">Sessions</span>
            </Link>
            
            <Link 
              href="/orders" 
              className={`px-4 py-2 rounded-lg font-medium transition-all ${
                pathname?.startsWith('/orders') 
                  ? 'bg-white text-green-600 shadow-lg' 
                  : 'text-white hover:bg-white hover:bg-opacity-10'
              }`}
            >
              <i className="fas fa-shopping-cart mr-2"></i>
              <span className="hidden md:inline">Orders</span>
            </Link>
            
            <Link 
              href="/invoices" 
              className={`px-4 py-2 rounded-lg font-medium transition-all ${
                pathname?.startsWith('/invoices') 
                  ? 'bg-white text-orange-600 shadow-lg' 
                  : 'text-white hover:bg-white hover:bg-opacity-10'
              }`}
            >
              <i className="fas fa-file-invoice-dollar mr-2"></i>
              <span className="hidden md:inline">Invoices</span>
            </Link>
            
            <Link 
              href="/developer/punchout" 
              className={`px-4 py-2 rounded-lg font-medium transition-all ${
                pathname?.startsWith('/developer') 
                  ? 'bg-white text-purple-600 shadow-lg' 
                  : 'text-white hover:bg-white hover:bg-opacity-10'
              }`}
            >
              <i className="fas fa-flask mr-2"></i>
              <span className="hidden md:inline">Developer</span>
            </Link>
            
            <Link 
              href="/converter" 
              className={`px-4 py-2 rounded-lg font-medium transition-all ${
                pathname === '/converter' 
                  ? 'bg-white text-green-600 shadow-lg' 
                  : 'text-white hover:bg-white hover:bg-opacity-10'
              }`}
            >
              <i className="fas fa-exchange-alt mr-2"></i>
              <span className="hidden md:inline">Converter</span>
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default NavBar;
