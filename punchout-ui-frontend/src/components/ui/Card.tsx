/**
 * Reusable card components
 */

import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  padding?: 'none' | 'small' | 'medium' | 'large';
}

/**
 * Basic card wrapper
 */
export function Card({ children, className = '', padding = 'medium' }: CardProps) {
  const paddingClass = {
    none: '',
    small: 'p-4',
    medium: 'p-6',
    large: 'p-8',
  }[padding];

  return (
    <div className={`bg-white rounded-xl shadow-lg border border-gray-200 ${paddingClass} ${className}`}>
      {children}
    </div>
  );
}

interface CardHeaderProps {
  icon?: string;
  iconColor?: string;
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}

/**
 * Card header component
 */
export function CardHeader({ icon, iconColor = 'text-blue-600', title, subtitle, action }: CardHeaderProps) {
  return (
    <div className={`flex items-center justify-between ${subtitle ? 'mb-4' : 'mb-6'}`}>
      <div>
        <h2 className="text-xl font-semibold">
          {icon && <i className={`fas ${icon} ${iconColor} mr-2`}></i>}
          {title}
        </h2>
        {subtitle && <p className="text-sm text-gray-600 mt-1">{subtitle}</p>}
      </div>
      {action && <div>{action}</div>}
    </div>
  );
}

/**
 * Card with header
 */
export function CardWithHeader({
  icon,
  iconColor,
  title,
  subtitle,
  action,
  children,
  className = '',
}: CardHeaderProps & { children: React.ReactNode; className?: string }) {
  return (
    <Card className={className}>
      <CardHeader icon={icon} iconColor={iconColor} title={title} subtitle={subtitle} action={action} />
      {children}
    </Card>
  );
}
