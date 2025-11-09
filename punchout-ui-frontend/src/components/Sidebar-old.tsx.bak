'use client';

import React from 'react';
import Link from 'next/link';

const Sidebar = () => {
  return (
    <div className="sidebar bg-light border-end" style={{ width: '250px', height: '100vh', position: 'fixed', left: 0, top: 0, paddingTop: '56px' }}>
      <div className="p-3 border-bottom bg-secondary text-white">
        <h5 className="mb-0 d-flex align-items-center">
          <i className="fas fa-building me-2"></i>
          Waters
        </h5>
      </div>
      <div className="p-3">
        <div className="list-group list-group-flush">
          <Link href="/" className="list-group-item list-group-item-action border-0">
            <i className="fas fa-tachometer-alt me-2"></i> Dashboard
          </Link>
          <Link href="/sessions" className="list-group-item list-group-item-action border-0">
            <i className="fas fa-tasks me-2"></i> PunchOut Sessions
          </Link>
          <Link href="/order-requests" className="list-group-item list-group-item-action border-0">
            <i className="fas fa-shopping-cart me-2"></i> Order Requests
          </Link>
          <Link href="/order-notices" className="list-group-item list-group-item-action border-0">
            <i className="fas fa-bell me-2"></i> Order Notices
          </Link>
          <Link href="/configuration" className="list-group-item list-group-item-action border-0">
            <i className="fas fa-cog me-2"></i> Configuration
          </Link>
          <Link href="/reports" className="list-group-item list-group-item-action border-0">
            <i className="fas fa-chart-bar me-2"></i> Reports
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;