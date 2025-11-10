'use client';

import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';

export default function DeveloperPage() {
  const breadcrumbItems = [
    { label: 'Developer' },
  ];

  const tools = [
    {
      title: 'PunchOut Testing',
      description: 'Manually test PunchOut catalog integrations across multiple environments and customer networks',
      icon: 'code',
      href: '/developer/punchout',
      color: 'purple',
      features: [
        'Test multiple catalog routes',
        'Switch between environments',
        'View test history',
        'Debug request/response data',
      ],
    },
    {
      title: 'Network Request Inspector',
      description: 'Monitor and inspect all network requests between customers and third-party services',
      icon: 'network-wired',
      href: '/sessions',
      color: 'blue',
      features: [
        'Real-time request monitoring',
        'Filter by direction',
        'View full request/response',
        'Performance metrics',
      ],
    },
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      <Breadcrumb items={breadcrumbItems} />

      {/* Header */}
      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-2">
          <i className="fas fa-terminal mr-3 text-indigo-600"></i>
          Developer Tools
        </h1>
        <p className="text-gray-600 text-lg">
          Testing, debugging, and monitoring tools for PunchOut integrations
        </p>
      </div>

      {/* Tools Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {tools.map((tool) => (
          <Link
            key={tool.href}
            href={tool.href}
            className="bg-white rounded-lg shadow-lg p-6 hover:shadow-xl transition-shadow border-l-4 border-purple-500 group"
          >
            <div className="flex items-start mb-4">
              <div className={`w-12 h-12 rounded-lg bg-${tool.color}-100 flex items-center justify-center mr-4`}>
                <i className={`fas fa-${tool.icon} text-${tool.color}-600 text-2xl`}></i>
              </div>
              <div className="flex-grow">
                <h3 className="text-xl font-bold mb-1 group-hover:text-blue-600 transition">
                  {tool.title}
                  <i className="fas fa-arrow-right ml-2 text-sm opacity-0 group-hover:opacity-100 transition"></i>
                </h3>
                <p className="text-gray-600 text-sm">{tool.description}</p>
              </div>
            </div>
            
            <ul className="space-y-2">
              {tool.features.map((feature, idx) => (
                <li key={idx} className="text-sm text-gray-700 flex items-center">
                  <i className="fas fa-check-circle text-green-500 mr-2"></i>
                  {feature}
                </li>
              ))}
            </ul>
          </Link>
        ))}
      </div>

      {/* Quick Stats */}
      <div className="mt-8 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-lg shadow-lg p-6 text-white">
        <h2 className="text-2xl font-bold mb-4">
          <i className="fas fa-chart-line mr-2"></i>
          Developer Resources
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div>
            <h3 className="font-semibold mb-2">Documentation</h3>
            <ul className="space-y-1 text-sm">
              <li><i className="fas fa-book mr-2"></i> API Reference</li>
              <li><i className="fas fa-file-code mr-2"></i> Integration Guide</li>
              <li><i className="fas fa-graduation-cap mr-2"></i> Best Practices</li>
            </ul>
          </div>
          <div>
            <h3 className="font-semibold mb-2">Tools</h3>
            <ul className="space-y-1 text-sm">
              <li><i className="fas fa-flask mr-2"></i> Test Environments</li>
              <li><i className="fas fa-bug mr-2"></i> Debug Console</li>
              <li><i className="fas fa-chart-bar mr-2"></i> Analytics</li>
            </ul>
          </div>
          <div>
            <h3 className="font-semibold mb-2">Support</h3>
            <ul className="space-y-1 text-sm">
              <li><i className="fas fa-life-ring mr-2"></i> Help Center</li>
              <li><i className="fas fa-comments mr-2"></i> Community</li>
              <li><i className="fas fa-envelope mr-2"></i> Contact Support</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
