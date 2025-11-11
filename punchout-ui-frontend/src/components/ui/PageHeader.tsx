/**
 * Reusable page header component with hero section
 */

import React from 'react';

interface PageHeaderProps {
  icon?: string;
  title: string;
  description?: string;
  gradient?: string;
  children?: React.ReactNode;
}

export default function PageHeader({
  icon,
  title,
  description,
  gradient = 'from-blue-600 to-purple-600',
  children,
}: PageHeaderProps) {
  return (
    <div className={`bg-gradient-to-r ${gradient} text-white`}>
      <div className="container mx-auto px-4 py-12">
        <div className="max-w-4xl">
          <h1 className="text-4xl font-bold mb-3">
            {icon && <i className={`fas ${icon} mr-3`}></i>}
            {title}
          </h1>
          {description && (
            <p className="text-xl opacity-90">{description}</p>
          )}
          {children}
        </div>
      </div>
    </div>
  );
}
