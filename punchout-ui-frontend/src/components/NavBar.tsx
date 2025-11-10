'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import dynamic from 'next/dynamic';

// Dynamically import TradeCentricLogo to avoid SSR issues
const TradeCentricLogo = dynamic(() => import('./TradeCentricLogo'), { ssr: false });

const NavBar = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const pathname = usePathname();

  useEffect(() => {
    // Check if user is logged in
    const loggedInStatus = localStorage.getItem('isLoggedIn') === 'true';
    setIsLoggedIn(loggedInStatus);
  }, []);

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary">
      <div className="container">
        <Link href="/" className="navbar-brand d-flex align-items-center">
          <TradeCentricLogo />
        </Link>
        <button 
          className="navbar-toggler" 
          type="button" 
          data-bs-toggle="collapse" 
          data-bs-target="#navbarCollapse2" 
          aria-controls="navbarCollapse2" 
          aria-expanded="false" 
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        
        {isLoggedIn ? (
          <div className="collapse navbar-collapse" id="navbarCollapse2">
            <ul className="navbar-nav ms-auto mb-2 mb-lg-0">
              <li className="nav-item">
                <Link 
                  className="btn nav-link d-flex align-items-center" 
                  href="/"
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-home fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Dashboard</span>
                </Link>
              </li>
              
              <li className="nav-item dropdown">
                <Link 
                  id="navbarDropdown" 
                  className="btn nav-link d-flex align-items-center dropdown-toggle" 
                  role="button" 
                  data-bs-toggle="dropdown" 
                  aria-expanded="false" 
                  href="/sessions"
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-tasks fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Activity</span>
                </Link>
                <ul className="depth_2 dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
                  <li><Link className="dropdown-item" href="/sessions">PunchOut Sessions</Link></li>
                  <li><Link className="dropdown-item" href="/order-requests">Order Requests</Link></li>
                  <li><Link className="dropdown-item" href="/order-notices">Order Notices</Link></li>
                  <li><Link className="dropdown-item" href="/order-invoices">Order Invoices</Link></li>
                </ul>
              </li>
              
              <li className="nav-item dropdown">
                <Link 
                  id="navbarDropdown" 
                  className="btn nav-link d-flex align-items-center dropdown-toggle" 
                  role="button" 
                  data-bs-toggle="dropdown" 
                  aria-expanded="false" 
                  href="/configuration"
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-cog fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Configuration</span>
                </Link>
                <ul className="depth_2 dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
                  <li><Link className="dropdown-item" href="/domains">Domains</Link></li>
                  <li><Link className="dropdown-item" href="/suppliers">Supplier Accounts</Link></li>
                  <li><Link className="dropdown-item" href="/catalog-route">Catalog Routes</Link></li>
                  <li><Link className="dropdown-item" href="/datastore">Data Stores</Link></li>
                  <li><Link className="dropdown-item" href="/order-route">Purchase Order Routes</Link></li>
                </ul>
              </li>
              
              <li className="nav-item dropdown">
                <Link 
                  id="navbarDropdown" 
                  className="btn nav-link d-flex align-items-center dropdown-toggle" 
                  role="button" 
                  data-bs-toggle="dropdown" 
                  aria-expanded="false" 
                  href="/reports"
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-chart-bar fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Reports</span>
                </Link>
                <ul className="depth_2 dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
                  <li><Link className="dropdown-item" href="/reports/overview">Overview</Link></li>
                  <li><Link className="dropdown-item" href="/reports/punchout">PunchOut Sessions</Link></li>
                  <li><Link className="dropdown-item" href="/reports/order">Order Requests</Link></li>
                  <li><Link className="dropdown-item" href="/reports/invoice">Order Invoices</Link></li>
                  <li><Link className="dropdown-item" href="/reports/notice">Order Notices</Link></li>
                </ul>
              </li>
              
              <li className="nav-item dropdown">
                <Link 
                  id="navbarDropdown" 
                  className="btn nav-link d-flex align-items-center dropdown-toggle" 
                  role="button" 
                  data-bs-toggle="dropdown" 
                  aria-expanded="false" 
                  href="/developer"
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-code fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Developer</span>
                </Link>
                <ul className="depth_2 dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
                  <li><Link className="dropdown-item" href="/developer/punchout"><i className="fas fa-flask me-2 text-purple-600"></i><strong>PunchOut Testing</strong></Link></li>
                  <li><hr className="dropdown-divider"/></li>
                  <li><Link className="dropdown-item" href="/tools/punchout">PunchOut</Link></li>
                  <li><Link className="dropdown-item" href="/tools/punchoutaddress">PunchOut Addresses</Link></li>
                  <li><Link className="dropdown-item" href="/tools/orderrequest">Purchase Order Requests</Link></li>
                  <li><Link className="dropdown-item" href="/tools/orderprofile">Purchase Order Profiles</Link></li>
                  <li><Link className="dropdown-item" href="/tools/setuprequest">Setup Request</Link></li>
                  <li><Link className="dropdown-item" href="/tools/orderparse">Document Decoding and Validation</Link></li>
                </ul>
              </li>
              
              <li className="nav-item">
                <button 
                  className="btn nav-link d-flex align-items-center" 
                  onClick={() => {
                    localStorage.removeItem('isLoggedIn');
                    window.location.href = '/login';
                  }}
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-sign-out fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Logout</span>
                </button>
              </li>
            </ul>
          </div>
        ) : (
          <div className="collapse navbar-collapse" id="navbarCollapse2">
            <ul className="navbar-nav ms-auto mb-2 mb-lg-0">
              <li className="nav-item">
                <Link className="nav-link" href="/">Home</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" href="/converter">Converter</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" href="/login">Login</Link>
              </li>
            </ul>
          </div>
        )}
      </div>
    </nav>
  );
};

export default NavBar;