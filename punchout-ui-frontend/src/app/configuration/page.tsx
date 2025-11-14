'use client';

import { useState, useEffect } from 'react';
import Breadcrumb from '@/components/Breadcrumb';

export default function ConfigurationPage() {
  const [activeTab, setActiveTab] = useState('environment');

  const breadcrumbItems = [{ label: 'Configuration' }];

  const configSections = [
    { id: 'environment', icon: 'server', label: 'Environment Settings', color: 'blue' },
    { id: 'api', icon: 'plug', label: 'API Configuration', color: 'green' },
    { id: 'security', icon: 'shield-alt', label: 'Security & Auth', color: 'red' },
    { id: 'monitoring', icon: 'chart-line', label: 'Monitoring & Logs', color: 'purple' },
    { id: 'notifications', icon: 'bell', label: 'Notifications', color: 'yellow' },
  ];

  return (
    <div>
      <div className="bg-gradient-to-r from-indigo-600 to-purple-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-cog mr-3"></i>
              System Configuration
            </h1>
            <p className="text-xl text-indigo-100">
              Manage environment settings, API configurations, and system preferences
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <Breadcrumb items={breadcrumbItems} />

        <div className="grid grid-cols-12 gap-6 mt-6">
          {/* Sidebar */}
          <div className="col-span-3">
            <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-4">
              <h3 className="text-sm font-semibold text-gray-500 uppercase mb-3 px-2">Settings</h3>
              <nav className="space-y-1">
                {configSections.map((section) => (
                  <button
                    key={section.id}
                    onClick={() => setActiveTab(section.id)}
                    className={`w-full text-left px-3 py-2.5 rounded-lg transition-all ${
                      activeTab === section.id
                        ? `bg-${section.color}-50 text-${section.color}-600 font-semibold border-l-4 border-${section.color}-600`
                        : 'text-gray-600 hover:bg-gray-50 border-l-4 border-transparent'
                    }`}
                  >
                    <i className={`fas fa-${section.icon} mr-3 w-4`}></i>
                    {section.label}
                  </button>
                ))}
              </nav>
            </div>
          </div>

          {/* Main Content */}
          <div className="col-span-9">
            <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-8">
              {activeTab === 'environment' && (
                <div>
                  <h2 className="text-2xl font-bold text-gray-900 mb-6">
                    <i className="fas fa-server text-blue-600 mr-2"></i>
                    Environment Settings
                  </h2>
                  
                  <div className="space-y-6">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Current Environment
                      </label>
                      <select className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                        <option value="dev">Development</option>
                        <option value="stage">Staging</option>
                        <option value="prod">Production</option>
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Gateway Service URL
                      </label>
                      <input
                        type="text"
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        defaultValue="http://localhost:9090"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        UI Backend URL
                      </label>
                      <input
                        type="text"
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        defaultValue="http://localhost:8080"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Mock Service URL
                      </label>
                      <input
                        type="text"
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        defaultValue="http://localhost:8082"
                      />
                    </div>

                    <div className="pt-4">
                      <button className="px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-lg hover:from-blue-700 hover:to-indigo-700 transition-all font-semibold shadow-lg">
                        <i className="fas fa-save mr-2"></i>
                        Save Changes
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {activeTab === 'api' && (
                <div>
                  <h2 className="text-2xl font-bold text-gray-900 mb-6">
                    <i className="fas fa-plug text-green-600 mr-2"></i>
                    API Configuration
                  </h2>
                  <p className="text-gray-600 mb-4">Configure API endpoints and authentication settings.</p>
                  <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                    <p className="text-green-800">API configuration coming soon...</p>
                  </div>
                </div>
              )}

              {activeTab === 'security' && (
                <div>
                  <h2 className="text-2xl font-bold text-gray-900 mb-6">
                    <i className="fas fa-shield-alt text-red-600 mr-2"></i>
                    Security & Authentication
                  </h2>
                  <p className="text-gray-600 mb-4">Manage security policies and authentication methods.</p>
                  <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                    <p className="text-red-800">Security settings coming soon...</p>
                  </div>
                </div>
              )}

              {activeTab === 'monitoring' && (
                <div>
                  <h2 className="text-2xl font-bold text-gray-900 mb-6">
                    <i className="fas fa-chart-line text-purple-600 mr-2"></i>
                    Monitoring & Logs
                  </h2>
                  <p className="text-gray-600 mb-4">Configure logging levels and monitoring settings.</p>
                  <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
                    <p className="text-purple-800">Monitoring configuration coming soon...</p>
                  </div>
                </div>
              )}

              {activeTab === 'notifications' && (
                <div>
                  <h2 className="text-2xl font-bold text-gray-900 mb-6">
                    <i className="fas fa-bell text-yellow-600 mr-2"></i>
                    Notification Settings
                  </h2>
                  <p className="text-gray-600 mb-4">Manage email and webhook notifications.</p>
                  <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                    <p className="text-yellow-800">Notification settings coming soon...</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
