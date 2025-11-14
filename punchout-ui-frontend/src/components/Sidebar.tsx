'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

interface NavItem {
  href: string;
  icon: string;
  label: string;
  badge?: number;
}

const Sidebar = () => {
  const pathname = usePathname();

  const navItems: NavItem[] = [
    { href: '/', icon: 'tachometer-alt', label: 'Dashboard' },
    { href: '/sessions', icon: 'tasks', label: 'PunchOut Sessions' },
    { href: '/order-requests', icon: 'shopping-cart', label: 'Order Requests' },
    { href: '/order-notices', icon: 'bell', label: 'Order Notices' },
    { href: '/datastore', icon: 'database', label: 'Datastore' },
    { href: '/configuration', icon: 'cog', label: 'Configuration' },
    { href: '/reports', icon: 'chart-bar', label: 'Reports' },
  ];

  const developerItems: NavItem[] = [
    { href: '/developer/punchout', icon: 'code', label: 'PunchOut Testing' },
  ];

  const isActive = (href: string) => {
    if (href === '/') {
      return pathname === '/';
    }
    return pathname.startsWith(href);
  };

  return (
    <div 
      className="sidebar bg-light border-end" 
      style={{ 
        width: '250px', 
        height: '100vh', 
        position: 'fixed', 
        left: 0, 
        top: 0, 
        paddingTop: '56px',
        overflowY: 'auto'
      }}
    >
      {/* Company Header */}
      <div className="p-3 border-bottom bg-secondary text-white">
        <h5 className="mb-0 d-flex align-items-center">
          <i className="fas fa-building me-2"></i>
          Waters
        </h5>
        <small className="text-white-50">Punchout Manager</small>
      </div>

      {/* Navigation Menu */}
      <div className="p-3">
        <div className="list-group list-group-flush">
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={`list-group-item list-group-item-action border-0 d-flex align-items-center ${
                isActive(item.href) ? 'active bg-primary text-white' : ''
              }`}
              style={{
                transition: 'all 0.2s ease',
                borderLeft: isActive(item.href) ? '4px solid #0d6efd' : '4px solid transparent',
              }}
            >
              <i className={`fas fa-${item.icon} me-2`} style={{ width: '20px' }}></i>
              <span className="flex-grow-1">{item.label}</span>
              {item.badge && (
                <span className="badge bg-danger rounded-pill">{item.badge}</span>
              )}
            </Link>
          ))}
          
          {/* Developer Section */}
          <div className="border-top mt-3 pt-3">
            <small className="text-muted px-2 d-block mb-2 text-uppercase" style={{fontSize: '0.7rem'}}>
              Developer Tools
            </small>
            {developerItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`list-group-item list-group-item-action border-0 d-flex align-items-center ${
                  isActive(item.href) ? 'active bg-primary text-white' : ''
                }`}
                style={{
                  transition: 'all 0.2s ease',
                  borderLeft: isActive(item.href) ? '4px solid #0d6efd' : '4px solid transparent',
                }}
              >
                <i className={`fas fa-${item.icon} me-2`} style={{ width: '20px' }}></i>
                <span className="flex-grow-1">{item.label}</span>
              </Link>
            ))}
          </div>
        </div>
      </div>

      {/* Footer / Help Section */}
      <div className="p-3 border-top mt-auto" style={{ position: 'absolute', bottom: 0, width: '250px' }}>
        <div className="text-center text-muted small">
          <i className="fas fa-question-circle me-1"></i>
          <a href="/help" className="text-muted text-decoration-none">Help & Support</a>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
