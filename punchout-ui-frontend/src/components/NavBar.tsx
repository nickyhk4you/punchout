'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import dynamic from 'next/dynamic';

const TradeCentricLogo = dynamic(() => import('./TradeCentricLogo'), { ssr: false });

const NavBar = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const pathname = usePathname();

  useEffect(() => {
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
              {/* Dashboard */}
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
              
              {/* Sessions */}
              <li className="nav-item">
                <Link 
                  className="btn nav-link d-flex align-items-center" 
                  href="/sessions"
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-plug fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Sessions</span>
                </Link>
              </li>
              
              {/* Orders */}
              <li className="nav-item">
                <Link 
                  className="btn nav-link d-flex align-items-center" 
                  href="/orders"
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-shopping-cart fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Orders</span>
                </Link>
              </li>
              
              {/* Developer */}
              <li className="nav-item">
                <Link 
                  className="btn nav-link d-flex align-items-center" 
                  href="/developer/punchout"
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-flask fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Developer</span>
                </Link>
              </li>
              
              {/* Converter */}
              <li className="nav-item">
                <Link 
                  className="btn nav-link d-flex align-items-center" 
                  href="/converter"
                >
                  <span className="fa-stack">
                    <i className="fas fa-circle fa-stack-1x text-light"></i>
                    <i className="fal fa-exchange-alt fa-stack-1x"></i>
                  </span>
                  <span className="d-inline d-lg-none d-xl-inline">Converter</span>
                </Link>
              </li>
              
              {/* Logout */}
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
                <Link className="nav-link" href="/">
                  <i className="fas fa-home me-2"></i>Home
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" href="/sessions">
                  <i className="fas fa-plug me-2"></i>Sessions
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" href="/orders">
                  <i className="fas fa-shopping-cart me-2"></i>Orders
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" href="/converter">
                  <i className="fas fa-exchange-alt me-2"></i>Converter
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" href="/login">
                  <i className="fas fa-sign-in me-2"></i>Login
                </Link>
              </li>
            </ul>
          </div>
        )}
      </div>
    </nav>
  );
};

export default NavBar;
