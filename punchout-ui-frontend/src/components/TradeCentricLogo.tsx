import React from 'react';

const TradeCentricLogo = () => {
  return (
    <svg width="140" height="40" viewBox="0 0 140 40" fill="none" xmlns="http://www.w3.org/2000/svg">
      <circle cx="20" cy="20" r="18" stroke="#3498db" strokeWidth="4" fill="none" />
      <path d="M20 10 L20 30" stroke="#3498db" strokeWidth="4" strokeLinecap="round" />
      <path d="M10 20 L30 20" stroke="#3498db" strokeWidth="4" strokeLinecap="round" />
      <path d="M45 15 H65" stroke="#2c3e50" strokeWidth="3" strokeLinecap="round" />
      <path d="M45 20 H60" stroke="#2c3e50" strokeWidth="3" strokeLinecap="round" />
      <path d="M45 25 H55" stroke="#2c3e50" strokeWidth="3" strokeLinecap="round" />
      <text x="70" y="25" fontFamily="Arial" fontSize="16" fontWeight="bold" fill="#2c3e50">TradeCentric</text>
    </svg>
  );
};

export default TradeCentricLogo;