'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const NavBar = () => {
  const pathname = usePathname();

  const navItems = [
    { href: '/', icon: 'home', label: 'Dashboard' },
    { href: '/sessions', icon: 'tasks', label: 'Sessions' },
    { href: '/orders', icon: 'shopping-cart', label: 'Orders' },
    { href: '/invoices', icon: 'file-invoice-dollar', label: 'Invoices' },
    { href: '/datastore', icon: 'database', label: 'Datastore' },
    { href: '/developer/punchout', icon: 'code', label: 'Developer' },
  ];

  const isActive = (href: string) => {
    if (href === '/') return pathname === '/';
    return pathname?.startsWith(href);
  };

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-gradient-to-r from-blue-600 to-purple-600 shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo/Brand */}
          <Link href="/" className="flex items-center space-x-3 hover:opacity-90 transition-opacity">
            <div className="bg-white rounded-lg p-2 shadow-md">
              <i className="fas fa-rocket text-2xl text-blue-600"></i>
            </div>
            <span className="text-white font-bold text-xl hidden md:block">
              Waters Punchout Platform
            </span>
          </Link>
          
          {/* Navigation Menu */}
          <div className="hidden md:flex items-center space-x-2">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`px-4 py-2 rounded-lg font-semibold text-sm transition-all ${
                  isActive(item.href)
                    ? 'bg-white text-blue-600 shadow-lg scale-105'
                    : 'text-white hover:bg-white hover:bg-opacity-20'
                }`}
              >
                <i className={`fas fa-${item.icon} mr-2`}></i>
                {item.label}
              </Link>
            ))}
          </div>

          {/* Mobile - Simplified */}
          <div className="md:hidden flex items-center space-x-1">
            {navItems.slice(0, 4).map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`p-2 rounded-lg transition-all ${
                  isActive(item.href)
                    ? 'bg-white text-blue-600'
                    : 'text-white hover:bg-white hover:bg-opacity-20'
                }`}
                title={item.label}
              >
                <i className={`fas fa-${item.icon}`}></i>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default NavBar;
