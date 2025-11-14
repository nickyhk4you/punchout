'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useState, useRef, useEffect } from 'react';

const NavBar = () => {
  const pathname = usePathname();
  const [openDropdown, setOpenDropdown] = useState<string | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setOpenDropdown(null);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const isActive = (href: string) => {
    if (href === '/') return pathname === '/';
    return pathname?.startsWith(href);
  };

  const isGroupActive = (items: any[]) => {
    return items.some(item => isActive(item.href));
  };

  const dataMenuItems = [
    { href: '/sessions', icon: 'tasks', label: 'Sessions' },
    { href: '/orders', icon: 'shopping-cart', label: 'Orders' },
    { href: '/invoices', icon: 'file-invoice-dollar', label: 'Invoices' },
    { href: '/datastore', icon: 'database', label: 'Datastore' },
  ];

  const devMenuItems = [
    { href: '/developer/punchout', icon: 'flask', label: 'Testing' },
    { href: '/converter', icon: 'exchange-alt', label: 'Converter' },
    { href: '/onboarding', icon: 'user-plus', label: 'Customer Onboarding' },
  ];

  const DropdownMenu = ({ title, icon, items, menuKey }: any) => {
    const isOpen = openDropdown === menuKey;
    const isMenuActive = isGroupActive(items);

    return (
      <div className="relative">
        <button
          onClick={() => setOpenDropdown(isOpen ? null : menuKey)}
          className={`px-4 py-2 rounded-lg font-semibold text-sm transition-all duration-150 flex items-center gap-2 ${
            isMenuActive || isOpen
              ? 'bg-white text-blue-600 shadow-lg'
              : 'text-white hover:bg-white hover:bg-opacity-20'
          }`}
        >
          <i className={`fas fa-${icon}`}></i>
          <span>{title}</span>
          <i className={`fas fa-chevron-down text-xs transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`}></i>
        </button>
        
        {isOpen && (
          <div className="absolute top-full mt-2 right-0 bg-white rounded-lg shadow-xl py-2 min-w-[220px] border border-gray-200 animate-dropdown">
            {items.map((item: any) => (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => setOpenDropdown(null)}
                className={`flex items-center gap-3 px-4 py-2.5 text-sm transition-colors ${
                  isActive(item.href)
                    ? 'bg-blue-50 text-blue-600 font-semibold border-l-4 border-blue-600'
                    : 'text-gray-700 hover:bg-gray-50 border-l-4 border-transparent'
                }`}
              >
                <i className={`fas fa-${item.icon} w-4 text-center ${isActive(item.href) ? 'text-blue-600' : 'text-gray-400'}`}></i>
                <span>{item.label}</span>
              </Link>
            ))}
          </div>
        )}
      </div>
    );
  };

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-gradient-to-r from-blue-600 to-purple-600 shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16" ref={dropdownRef}>
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
            <Link
              href="/"
              className={`px-4 py-2 rounded-lg font-semibold text-sm transition-all ${
                isActive('/')
                  ? 'bg-white text-blue-600 shadow-lg scale-105'
                  : 'text-white hover:bg-white hover:bg-opacity-20'
              }`}
            >
              <i className="fas fa-home mr-2"></i>
              Dashboard
            </Link>

            <DropdownMenu 
              title="Data Management" 
              icon="database"
              items={dataMenuItems}
              menuKey="data"
            />

            <DropdownMenu 
              title="Developer Tools" 
              icon="code"
              items={devMenuItems}
              menuKey="dev"
            />

            <Link
              href="/configuration"
              className={`px-4 py-2 rounded-lg font-semibold text-sm transition-all ${
                isActive('/configuration')
                  ? 'bg-white text-blue-600 shadow-lg scale-105'
                  : 'text-white hover:bg-white hover:bg-opacity-20'
              }`}
            >
              <i className="fas fa-cog mr-2"></i>
              Config
            </Link>
          </div>

          {/* Mobile - Simplified */}
          <div className="md:hidden flex items-center space-x-1">
            <Link href="/" className={`p-2 rounded-lg ${isActive('/') ? 'bg-white text-blue-600' : 'text-white'}`} title="Dashboard">
              <i className="fas fa-home"></i>
            </Link>
            <Link href="/sessions" className={`p-2 rounded-lg ${isActive('/sessions') ? 'bg-white text-blue-600' : 'text-white'}`} title="Sessions">
              <i className="fas fa-tasks"></i>
            </Link>
            <Link href="/orders" className={`p-2 rounded-lg ${isActive('/orders') ? 'bg-white text-blue-600' : 'text-white'}`} title="Orders">
              <i className="fas fa-shopping-cart"></i>
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default NavBar;
