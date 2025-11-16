'use client';

import { useState, useEffect } from 'react';
import Breadcrumb from '@/components/Breadcrumb';

interface NetworkRequest {
  id: string;
  timestamp: string;
  direction: string;
  requestType: string;
  destination: string;
  success: boolean;
  duration: number;
  statusCode: number;
  sessionKey: string;
}

interface SystemMetrics {
  totalSessions: number;
  totalOrders: number;
  totalRequests: number;
  recentSessions24h: number;
  recentOrders24h: number;
}

interface RequestMetrics {
  totalRequests: number;
  successCount: number;
  failureCount: number;
  successRate: number;
  avgResponseTime: number;
  byDirection: { [key: string]: number };
  byType: { [key: string]: number };
}

interface LogEntry {
  timestamp: string;
  level: string;
  service: string;
  message: string;
  sessionKey?: string;
  requestId?: string;
}

export default function ConfigurationPage() {
  const [activeTab, setActiveTab] = useState('environment');
  const [networkRequests, setNetworkRequests] = useState<NetworkRequest[]>([]);
  const [systemMetrics, setSystemMetrics] = useState<SystemMetrics | null>(null);
  const [requestMetrics, setRequestMetrics] = useState<RequestMetrics | null>(null);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [logFilter, setLogFilter] = useState('ALL');
  const [autoRefresh, setAutoRefresh] = useState(false);

  const breadcrumbItems = [{ label: 'Configuration' }];

  const configSections = [
    { id: 'environment', icon: 'server', label: 'Environment Settings', color: 'blue' },
    { id: 'api', icon: 'plug', label: 'API Configuration', color: 'green' },
    { id: 'security', icon: 'shield-alt', label: 'Security & Auth', color: 'red' },
    { id: 'monitoring', icon: 'chart-line', label: 'Monitoring & Logs', color: 'purple' },
    { id: 'notifications', icon: 'bell', label: 'Notifications', color: 'yellow' },
  ];

  const fetchMonitoringData = async () => {
    setLoading(true);
    try {
      const [metricsRes, requestMetricsRes, logsRes, networkReqRes] = await Promise.all([
        fetch('http://localhost:9090/api/monitoring/metrics'),
        fetch('http://localhost:9090/api/monitoring/metrics/requests?hours=24'),
        fetch('http://localhost:9090/api/monitoring/logs/recent?limit=50'),
        fetch('http://localhost:9090/api/monitoring/network-requests?limit=20')
      ]);

      if (metricsRes.ok) setSystemMetrics(await metricsRes.json());
      if (requestMetricsRes.ok) setRequestMetrics(await requestMetricsRes.json());
      if (logsRes.ok) setLogs(await logsRes.json());
      if (networkReqRes.ok) setNetworkRequests(await networkReqRes.json());
    } catch (error) {
      console.error('Error fetching monitoring data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'monitoring') {
      fetchMonitoringData();
    }
  }, [activeTab]);

  useEffect(() => {
    if (autoRefresh && activeTab === 'monitoring') {
      const interval = setInterval(fetchMonitoringData, 10000);
      return () => clearInterval(interval);
    }
  }, [autoRefresh, activeTab]);

  const filteredLogs = logFilter === 'ALL' 
    ? logs 
    : logs.filter(log => log.level === logFilter);

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
                  <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-bold text-gray-900">
                      <i className="fas fa-chart-line text-purple-600 mr-2"></i>
                      Monitoring & Logs
                    </h2>
                    <div className="flex gap-3">
                      <button
                        onClick={fetchMonitoringData}
                        disabled={loading}
                        className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-all disabled:opacity-50"
                      >
                        <i className={`fas fa-sync-alt mr-2 ${loading ? 'animate-spin' : ''}`}></i>
                        Refresh
                      </button>
                      <button
                        onClick={() => setAutoRefresh(!autoRefresh)}
                        className={`px-4 py-2 rounded-lg transition-all ${
                          autoRefresh 
                            ? 'bg-green-600 text-white hover:bg-green-700' 
                            : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                      >
                        <i className={`fas fa-${autoRefresh ? 'pause' : 'play'} mr-2`}></i>
                        Auto-refresh {autoRefresh ? 'ON' : 'OFF'}
                      </button>
                    </div>
                  </div>

                  {/* System Metrics Dashboard */}
                  <div className="grid grid-cols-5 gap-4 mb-6">
                    <div className="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-lg p-4 shadow-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-blue-100 text-sm">Total Sessions</p>
                          <p className="text-3xl font-bold mt-1">{systemMetrics?.totalSessions || 0}</p>
                        </div>
                        <i className="fas fa-tasks text-4xl text-blue-200 opacity-50"></i>
                      </div>
                      <p className="text-xs text-blue-100 mt-2">
                        <i className="fas fa-arrow-up mr-1"></i>
                        {systemMetrics?.recentSessions24h || 0} in 24h
                      </p>
                    </div>

                    <div className="bg-gradient-to-br from-green-500 to-green-600 text-white rounded-lg p-4 shadow-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-green-100 text-sm">Total Orders</p>
                          <p className="text-3xl font-bold mt-1">{systemMetrics?.totalOrders || 0}</p>
                        </div>
                        <i className="fas fa-shopping-cart text-4xl text-green-200 opacity-50"></i>
                      </div>
                      <p className="text-xs text-green-100 mt-2">
                        <i className="fas fa-arrow-up mr-1"></i>
                        {systemMetrics?.recentOrders24h || 0} in 24h
                      </p>
                    </div>

                    <div className="bg-gradient-to-br from-purple-500 to-purple-600 text-white rounded-lg p-4 shadow-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-purple-100 text-sm">Total Requests</p>
                          <p className="text-3xl font-bold mt-1">{systemMetrics?.totalRequests || 0}</p>
                        </div>
                        <i className="fas fa-exchange-alt text-4xl text-purple-200 opacity-50"></i>
                      </div>
                      <p className="text-xs text-purple-100 mt-2">
                        <i className="fas fa-network-wired mr-1"></i>
                        Network activity
                      </p>
                    </div>

                    <div className="bg-gradient-to-br from-emerald-500 to-emerald-600 text-white rounded-lg p-4 shadow-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-emerald-100 text-sm">Success Rate</p>
                          <p className="text-3xl font-bold mt-1">{requestMetrics?.successRate.toFixed(1) || 0}%</p>
                        </div>
                        <i className="fas fa-check-circle text-4xl text-emerald-200 opacity-50"></i>
                      </div>
                      <p className="text-xs text-emerald-100 mt-2">
                        {requestMetrics?.successCount || 0} / {requestMetrics?.totalRequests || 0} requests
                      </p>
                    </div>

                    <div className="bg-gradient-to-br from-orange-500 to-orange-600 text-white rounded-lg p-4 shadow-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-orange-100 text-sm">Avg Response</p>
                          <p className="text-3xl font-bold mt-1">{requestMetrics?.avgResponseTime?.toFixed(0) || 0}<span className="text-sm">ms</span></p>
                        </div>
                        <i className="fas fa-tachometer-alt text-4xl text-orange-200 opacity-50"></i>
                      </div>
                      <p className="text-xs text-orange-100 mt-2">
                        <i className="fas fa-clock mr-1"></i>
                        Last 24 hours
                      </p>
                    </div>
                  </div>

                  {/* Recent Network Requests */}
                  <div className="mb-6">
                    <h3 className="text-lg font-semibold text-gray-900 mb-3">
                      <i className="fas fa-network-wired text-purple-600 mr-2"></i>
                      Recent Network Requests
                    </h3>
                    <div className="bg-gray-50 rounded-lg border border-gray-200 overflow-hidden">
                      <div className="overflow-x-auto">
                        <table className="w-full">
                          <thead className="bg-gray-100 border-b border-gray-200">
                            <tr>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Time</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Direction</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Type</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Destination</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Status</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Duration</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Session</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-gray-200">
                            {networkRequests.length === 0 ? (
                              <tr>
                                <td colSpan={7} className="px-4 py-8 text-center text-gray-500">
                                  <i className="fas fa-inbox text-3xl mb-2 block text-gray-300"></i>
                                  No network requests found
                                </td>
                              </tr>
                            ) : (
                              networkRequests.map((req) => (
                                <tr key={req.id} className="hover:bg-gray-50 transition-colors">
                                  <td className="px-4 py-3 text-sm text-gray-600">
                                    {new Date(req.timestamp).toLocaleTimeString()}
                                  </td>
                                  <td className="px-4 py-3">
                                    <span className={`px-2 py-1 rounded text-xs font-semibold ${
                                      req.direction === 'INBOUND' 
                                        ? 'bg-blue-100 text-blue-700' 
                                        : 'bg-purple-100 text-purple-700'
                                    }`}>
                                      {req.direction}
                                    </span>
                                  </td>
                                  <td className="px-4 py-3 text-sm text-gray-700 font-medium">{req.requestType}</td>
                                  <td className="px-4 py-3 text-sm text-gray-600 truncate max-w-xs">{req.destination}</td>
                                  <td className="px-4 py-3">
                                    {req.success ? (
                                      <span className="flex items-center text-green-600 text-sm">
                                        <i className="fas fa-check-circle mr-1"></i>
                                        {req.statusCode}
                                      </span>
                                    ) : (
                                      <span className="flex items-center text-red-600 text-sm">
                                        <i className="fas fa-times-circle mr-1"></i>
                                        {req.statusCode || 'Error'}
                                      </span>
                                    )}
                                  </td>
                                  <td className="px-4 py-3 text-sm text-gray-600">{req.duration}ms</td>
                                  <td className="px-4 py-3 text-sm text-gray-500 font-mono truncate max-w-xs">
                                    {req.sessionKey?.substring(0, 8)}...
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </div>

                  {/* Application Logs */}
                  <div>
                    <div className="flex justify-between items-center mb-3">
                      <h3 className="text-lg font-semibold text-gray-900">
                        <i className="fas fa-file-alt text-purple-600 mr-2"></i>
                        Application Logs
                      </h3>
                      <div className="flex gap-2">
                        {['ALL', 'INFO', 'ERROR'].map((level) => (
                          <button
                            key={level}
                            onClick={() => setLogFilter(level)}
                            className={`px-3 py-1 rounded text-sm font-semibold transition-all ${
                              logFilter === level
                                ? 'bg-purple-600 text-white'
                                : 'bg-gray-200 text-gray-600 hover:bg-gray-300'
                            }`}
                          >
                            {level}
                          </button>
                        ))}
                      </div>
                    </div>
                    <div className="bg-gray-900 rounded-lg p-4 font-mono text-sm max-h-96 overflow-y-auto">
                      {filteredLogs.length === 0 ? (
                        <p className="text-gray-400 text-center py-4">No logs available</p>
                      ) : (
                        filteredLogs.map((log, index) => (
                          <div 
                            key={index} 
                            className={`mb-2 pb-2 border-b border-gray-800 ${
                              log.level === 'ERROR' ? 'text-red-400' : 'text-green-400'
                            }`}
                          >
                            <div className="flex items-start gap-3">
                              <span className="text-gray-500 text-xs whitespace-nowrap">
                                {new Date(log.timestamp).toLocaleString()}
                              </span>
                              <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                                log.level === 'ERROR' 
                                  ? 'bg-red-900 text-red-200' 
                                  : 'bg-green-900 text-green-200'
                              }`}>
                                {log.level}
                              </span>
                              <span className="text-blue-400 text-xs">[{log.service}]</span>
                              <span className="text-gray-300 flex-1">{log.message}</span>
                            </div>
                            {log.sessionKey && (
                              <div className="ml-32 mt-1 text-xs text-gray-500">
                                Session: {log.sessionKey}
                              </div>
                            )}
                          </div>
                        ))
                      )}
                    </div>
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
