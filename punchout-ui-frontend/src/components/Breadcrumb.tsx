'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

export interface BreadcrumbItem {
  label: string;
  href?: string;
}

interface BreadcrumbProps {
  items?: BreadcrumbItem[];
}

export default function Breadcrumb({ items }: BreadcrumbProps) {
  const pathname = usePathname();

  // Auto-generate breadcrumbs from pathname if items not provided
  const breadcrumbItems = items || generateBreadcrumbsFromPath(pathname);

  return (
    <nav className="mb-4">
      <ol className="flex items-center space-x-2 text-sm bg-white rounded-lg shadow px-4 py-3">
        <li>
          <Link href="/" className="text-blue-600 hover:text-blue-800 flex items-center">
            <i className="fas fa-home mr-1"></i>
            Home
          </Link>
        </li>
        {breadcrumbItems.map((item, index) => {
          const isLast = index === breadcrumbItems.length - 1;
          return (
            <li key={index} className="flex items-center">
              <span className="mx-2 text-gray-400">/</span>
              {isLast || !item.href ? (
                <span className="text-gray-700 font-medium">{item.label}</span>
              ) : (
                <Link href={item.href} className="text-blue-600 hover:text-blue-800">
                  {item.label}
                </Link>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}

function generateBreadcrumbsFromPath(pathname: string): BreadcrumbItem[] {
  const paths = pathname.split('/').filter(Boolean);
  const breadcrumbs: BreadcrumbItem[] = [];

  let currentPath = '';
  paths.forEach((path, index) => {
    currentPath += `/${path}`;
    
    // Format the label nicely
    let label = path.charAt(0).toUpperCase() + path.slice(1);
    label = label.replace(/-/g, ' ');
    
    // Special cases for better labels
    if (path === 'sessions') label = 'PunchOut Sessions';
    if (path === 'requests') label = 'Network Requests';
    
    breadcrumbs.push({
      label,
      href: index === paths.length - 1 ? undefined : currentPath,
    });
  });

  return breadcrumbs;
}
