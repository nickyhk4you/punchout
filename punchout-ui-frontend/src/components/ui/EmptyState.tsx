/**
 * Empty state component for displaying when no data is available
 */

import React from 'react';

interface EmptyStateProps {
  icon?: string;
  title: string;
  description?: string;
  action?: React.ReactNode;
}

export default function EmptyState({
  icon = 'fa-inbox',
  title,
  description,
  action,
}: EmptyStateProps) {
  return (
    <div className="p-12 text-center text-gray-600">
      <i className={`fas ${icon} text-gray-300 text-5xl mb-4`}></i>
      <p className="text-lg font-medium">{title}</p>
      {description && <p className="text-sm mt-2 text-gray-500">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
