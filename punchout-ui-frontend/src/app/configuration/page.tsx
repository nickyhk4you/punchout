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

interface SystemEnvConfig {
  id?: string;
  environment: string;
  authServiceUrl: string;
  authEmail?: string;
  authPassword?: string;
  muleServiceUrl: string;
  catalogBaseUrl: string;
  timeout?: number;
  retryAttempts?: number;
  healthCheckUrl?: string;
  description?: string;
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

interface ApiKey {
  id: string;
  keyValue: string;
  customerName: string;
  description: string;
  permissions: string[];
  environment: string;
  enabled: boolean;
  createdAt: string;
  expiresAt?: string;
  lastUsedAt?: string;
  usageCount: number;
  createdBy?: string;
  revokedAt?: string;
}

interface SecurityAuditLog {
  id: string;
  timestamp: string;
  eventType: string;
  severity: string;
  customerName?: string;
  description: string;
  metadata?: {[key: string]: string};
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
  
  // Environment config states
  const [envConfigs, setEnvConfigs] = useState<SystemEnvConfig[]>([]);
  const [currentEnv, setCurrentEnv] = useState<SystemEnvConfig | null>(null);
  const [editingEnv, setEditingEnv] = useState<SystemEnvConfig | null>(null);
  const [showEnvModal, setShowEnvModal] = useState(false);
  const [testingUrl, setTestingUrl] = useState<string | null>(null);
  const [urlTestResults, setUrlTestResults] = useState<{[key: string]: {status: string, latency?: number}}>({});
  const [expandedEnvCard, setExpandedEnvCard] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<'cards' | 'table'>('cards');
  const [showPassword, setShowPassword] = useState(false);
  
  // Security states
  const [apiKeys, setApiKeys] = useState<ApiKey[]>([]);
  const [securityStats, setSecurityStats] = useState<any>(null);
  const [auditLogs, setAuditLogs] = useState<SecurityAuditLog[]>([]);
  const [showApiKeyModal, setShowApiKeyModal] = useState(false);
  const [newApiKey, setNewApiKey] = useState<Partial<ApiKey>>({
    customerName: '',
    description: '',
    permissions: ['PUNCHOUT', 'ORDER'],
    environment: 'dev',
    enabled: true
  });
  const [jwtConfig, setJwtConfig] = useState<any>(null);
  const [securityTab, setSecurityTab] = useState<'api-keys' | 'jwt' | 'audit'>('api-keys');

  const breadcrumbItems = [{ label: 'Configuration' }];

  const configSections = [
    { id: 'environment', icon: 'server', label: 'Environment Settings', color: 'blue' },
    { id: 'api', icon: 'plug', label: 'API Configuration', color: 'green' },
    { id: 'security', icon: 'shield-alt', label: 'Security & Auth', color: 'red' },
    { id: 'monitoring', icon: 'chart-line', label: 'Monitoring & Logs', color: 'purple' },
    { id: 'notifications', icon: 'bell', label: 'Notifications', color: 'yellow' },
  ];

  const fetchEnvironmentConfigs = async () => {
    try {
      const [configsRes, currentRes] = await Promise.all([
        fetch('http://localhost:9090/api/environment-config'),
        fetch('http://localhost:9090/api/environment-config/current')
      ]);
      
      if (configsRes.ok) setEnvConfigs(await configsRes.json());
      if (currentRes.ok) setCurrentEnv(await currentRes.json());
    } catch (error) {
      console.error('Error fetching environment configs:', error);
    }
  };

  const saveEnvironmentConfig = async (config: SystemEnvConfig) => {
    try {
      const method = config.id ? 'PUT' : 'POST';
      const url = config.id 
        ? `http://localhost:9090/api/environment-config/${config.environment}`
        : 'http://localhost:9090/api/environment-config';
      
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config)
      });
      
      if (response.ok) {
        await fetchEnvironmentConfigs();
        setShowEnvModal(false);
        setEditingEnv(null);
      }
    } catch (error) {
      console.error('Error saving environment config:', error);
    }
  };

  const deleteEnvironmentConfig = async (environment: string) => {
    if (!confirm(`Delete environment config for "${environment}"?`)) return;
    
    try {
      const response = await fetch(`http://localhost:9090/api/environment-config/${environment}`, {
        method: 'DELETE'
      });
      
      if (response.ok) {
        await fetchEnvironmentConfigs();
      }
    } catch (error) {
      console.error('Error deleting environment config:', error);
    }
  };

  const testUrl = async (url: string, label: string) => {
    setTestingUrl(label);
    const startTime = Date.now();
    
    try {
      const response = await fetch(url, { method: 'HEAD', mode: 'no-cors' });
      const latency = Date.now() - startTime;
      setUrlTestResults(prev => ({
        ...prev,
        [label]: { status: 'success', latency }
      }));
    } catch (error) {
      setUrlTestResults(prev => ({
        ...prev,
        [label]: { status: 'error' }
      }));
    } finally {
      setTestingUrl(null);
    }
  };

  const exportConfigs = () => {
    const dataStr = JSON.stringify(envConfigs, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `environment-configs-${new Date().toISOString().split('T')[0]}.json`;
    link.click();
  };

  const fetchSecurityData = async () => {
    try {
      const [keysRes, statsRes, logsRes, jwtRes] = await Promise.all([
        fetch('http://localhost:9090/api/security/api-keys'),
        fetch('http://localhost:9090/api/security/statistics'),
        fetch('http://localhost:9090/api/security/audit-logs?limit=50'),
        fetch('http://localhost:9090/api/security/jwt/config')
      ]);
      
      if (keysRes.ok) setApiKeys(await keysRes.json());
      if (statsRes.ok) setSecurityStats(await statsRes.json());
      if (logsRes.ok) setAuditLogs(await logsRes.json());
      if (jwtRes.ok) setJwtConfig(await jwtRes.json());
    } catch (error) {
      console.error('Error fetching security data:', error);
    }
  };

  const generateApiKey = async () => {
    try {
      const response = await fetch('http://localhost:9090/api/security/api-keys', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newApiKey)
      });
      
      if (response.ok) {
        await fetchSecurityData();
        setShowApiKeyModal(false);
        setNewApiKey({
          customerName: '',
          description: '',
          permissions: ['PUNCHOUT', 'ORDER'],
          environment: 'dev',
          enabled: true
        });
      }
    } catch (error) {
      console.error('Error generating API key:', error);
    }
  };

  const revokeApiKey = async (keyId: string) => {
    if (!confirm('Revoke this API key? This action cannot be undone.')) return;
    
    try {
      const response = await fetch(`http://localhost:9090/api/security/api-keys/${keyId}/revoke`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ revokedBy: 'admin' })
      });
      
      if (response.ok) await fetchSecurityData();
    } catch (error) {
      console.error('Error revoking API key:', error);
    }
  };

  const rotateApiKey = async (keyId: string) => {
    if (!confirm('Rotate this API key? A new key will be generated and this one will be revoked.')) return;
    
    try {
      const response = await fetch(`http://localhost:9090/api/security/api-keys/${keyId}/rotate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rotatedBy: 'admin' })
      });
      
      if (response.ok) {
        const newKey = await response.json();
        alert(`New API Key Generated:\n\n${newKey.keyValue}\n\nPlease save this key - it won't be shown again!`);
        await fetchSecurityData();
      }
    } catch (error) {
      console.error('Error rotating API key:', error);
    }
  };

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
    } else if (activeTab === 'environment') {
      fetchEnvironmentConfigs();
    } else if (activeTab === 'security') {
      fetchSecurityData();
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
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      {/* Modern Header */}
      <div className="bg-white border-b border-gray-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-6 py-6">
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <div className="bg-gradient-to-br from-indigo-500 to-purple-600 p-3 rounded-xl shadow-lg">
                  <i className="fas fa-sliders-h text-2xl text-white"></i>
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-gray-900">Configuration Center</h1>
                  <p className="text-sm text-gray-500 mt-1">Manage your system settings and environments</p>
                </div>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="bg-gradient-to-br from-blue-50 to-indigo-50 px-4 py-2 rounded-lg border border-blue-200">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                  <span className="text-sm font-semibold text-gray-700">System Online</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 py-8">
        <Breadcrumb items={breadcrumbItems} />

        <div className="grid grid-cols-12 gap-6 mt-6">
          {/* Modern Sidebar */}
          <div className="col-span-3">
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden sticky top-6">
              <div className="bg-gradient-to-r from-gray-50 to-gray-100 px-4 py-3 border-b border-gray-200">
                <h3 className="text-xs font-bold text-gray-600 uppercase tracking-wider">Navigation</h3>
              </div>
              <nav className="p-2">
                {configSections.map((section) => {
                  const isActive = activeTab === section.id;
                  return (
                    <button
                      key={section.id}
                      onClick={() => setActiveTab(section.id)}
                      className={`w-full text-left px-4 py-3 rounded-xl transition-all duration-200 mb-1 group ${
                        isActive
                          ? 'bg-gradient-to-r from-blue-500 to-indigo-600 text-white shadow-md transform scale-[1.02]'
                          : 'text-gray-700 hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-lg flex items-center justify-center transition-all ${
                          isActive 
                            ? 'bg-white bg-opacity-20' 
                            : 'bg-gray-100 group-hover:bg-gray-200'
                        }`}>
                          <i className={`fas fa-${section.icon} ${isActive ? 'text-white' : `text-${section.color}-600`}`}></i>
                        </div>
                        <div className="flex-1">
                          <div className={`font-semibold text-sm ${isActive ? 'text-white' : 'text-gray-900'}`}>
                            {section.label}
                          </div>
                        </div>
                        {isActive && (
                          <i className="fas fa-chevron-right text-white text-xs"></i>
                        )}
                      </div>
                    </button>
                  );
                })}
              </nav>
            </div>
          </div>

          {/* Main Content */}
          <div className="col-span-9">
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
              {activeTab === 'environment' && (
                <div className="p-8">
                  <div className="flex justify-between items-center mb-8">
                    <div>
                      <h2 className="text-2xl font-bold text-gray-900">
                        <i className="fas fa-server text-blue-600 mr-2"></i>
                        Environment Management
                      </h2>
                      <p className="text-sm text-gray-600 mt-1">Configure and manage environment endpoints for different deployment stages</p>
                    </div>
                    <div className="flex gap-2">
                      <div className="flex bg-white rounded-lg p-1 border border-gray-200 shadow-sm">
                        <button
                          onClick={() => setViewMode('cards')}
                          className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                            viewMode === 'cards' 
                              ? 'bg-gradient-to-r from-blue-500 to-indigo-600 text-white shadow-sm' 
                              : 'text-gray-600 hover:bg-gray-50'
                          }`}
                        >
                          <i className="fas fa-th-large mr-2"></i>
                          Cards
                        </button>
                        <button
                          onClick={() => setViewMode('table')}
                          className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                            viewMode === 'table' 
                              ? 'bg-gradient-to-r from-blue-500 to-indigo-600 text-white shadow-sm' 
                              : 'text-gray-600 hover:bg-gray-50'
                          }`}
                        >
                          <i className="fas fa-table mr-2"></i>
                          Table
                        </button>
                      </div>
                      <button
                        onClick={exportConfigs}
                        className="px-5 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-all font-semibold shadow-sm"
                      >
                        <i className="fas fa-download mr-2"></i>
                        Export
                      </button>
                      <button
                        onClick={() => {
                          setEditingEnv({ 
                            environment: '', 
                            authServiceUrl: '', 
                            muleServiceUrl: '', 
                            catalogBaseUrl: '', 
                            timeout: 30000,
                            retryAttempts: 3,
                            enabled: true 
                          });
                          setShowEnvModal(true);
                          setShowPassword(false);
                        }}
                        className="px-5 py-2 bg-gradient-to-r from-blue-500 to-indigo-600 text-white rounded-lg hover:from-blue-600 hover:to-indigo-700 transition-all shadow-md font-semibold"
                      >
                        <i className="fas fa-plus mr-2"></i>
                        Add Environment
                      </button>
                    </div>
                  </div>

                  {/* Current Environment Info */}
                  {currentEnv && (
                    <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border-l-4 border-blue-600 rounded-xl p-5 mb-6 shadow-sm">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4">
                          <div className="bg-blue-600 text-white rounded-lg p-3">
                            <i className="fas fa-rocket text-2xl"></i>
                          </div>
                          <div>
                            <p className="text-xs text-blue-600 font-semibold uppercase tracking-wide">Active Environment</p>
                            <p className="text-3xl font-bold text-blue-900 mt-1">{currentEnv.environment.toUpperCase()}</p>
                            {currentEnv.description && <p className="text-sm text-blue-700 mt-1">{currentEnv.description}</p>}
                          </div>
                        </div>
                        <div className="text-right flex items-center gap-3">
                          <div className="text-right">
                            <p className="text-xs text-gray-500">Last Updated</p>
                            <p className="text-sm font-semibold text-gray-700">
                              {currentEnv.updatedAt ? new Date(currentEnv.updatedAt).toLocaleDateString() : 'N/A'}
                            </p>
                          </div>
                          <span className={`px-4 py-2 rounded-full text-sm font-bold ${
                            currentEnv.enabled ? 'bg-green-500 text-white' : 'bg-red-500 text-white'
                          }`}>
                            <i className={`fas fa-${currentEnv.enabled ? 'check-circle' : 'times-circle'} mr-1`}></i>
                            {currentEnv.enabled ? 'Active' : 'Inactive'}
                          </span>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Cards View */}
                  {viewMode === 'cards' && (
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                      {envConfigs.length === 0 ? (
                        <div className="col-span-2 text-center py-12 bg-gray-50 rounded-lg border-2 border-dashed border-gray-300">
                          <i className="fas fa-inbox text-5xl text-gray-300 mb-3"></i>
                          <p className="text-gray-500 font-semibold">No environments configured</p>
                          <p className="text-sm text-gray-400 mt-1">Click &quot;Add Environment&quot; to create your first configuration</p>
                        </div>
                      ) : (
                        envConfigs.map((config) => {
                          const isExpanded = expandedEnvCard === config.environment;
                          const isCurrent = currentEnv?.environment === config.environment;
                          
                          return (
                            <div 
                              key={config.id} 
                              className={`bg-white rounded-xl border-2 transition-all ${
                                isCurrent 
                                  ? 'border-blue-500 shadow-lg' 
                                  : 'border-gray-200 hover:border-gray-300 shadow-md'
                              }`}
                            >
                              <div className="p-5">
                                {/* Header */}
                                <div className="flex items-start justify-between mb-4">
                                  <div className="flex items-center gap-3">
                                    <div className={`rounded-lg p-3 ${
                                      isCurrent ? 'bg-blue-100' : 'bg-gray-100'
                                    }`}>
                                      <i className={`fas fa-${isCurrent ? 'star' : 'server'} text-2xl ${
                                        isCurrent ? 'text-blue-600' : 'text-gray-600'
                                      }`}></i>
                                    </div>
                                    <div>
                                      <div className="flex items-center gap-2">
                                        <h3 className="text-xl font-bold text-gray-900">{config.environment.toUpperCase()}</h3>
                                        {isCurrent && (
                                          <span className="px-2 py-1 bg-blue-100 text-blue-700 text-xs font-bold rounded">
                                            CURRENT
                                          </span>
                                        )}
                                      </div>
                                      {config.description && (
                                        <p className="text-sm text-gray-600 mt-1">{config.description}</p>
                                      )}
                                    </div>
                                  </div>
                                  <div className="flex items-center gap-2">
                                    <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                                      config.enabled 
                                        ? 'bg-green-100 text-green-700' 
                                        : 'bg-gray-100 text-gray-600'
                                    }`}>
                                      {config.enabled ? 'Active' : 'Inactive'}
                                    </span>
                                  </div>
                                </div>

                                {/* Quick Info */}
                                <div className="space-y-3 mb-4">
                                  <div className="flex items-center gap-2 text-sm">
                                    <i className="fas fa-shield-alt w-4 text-blue-600"></i>
                                    <span className="text-gray-500 font-medium">Auth:</span>
                                    <span className="text-gray-700 truncate flex-1">{config.authServiceUrl}</span>
                                    <button
                                      onClick={() => testUrl(config.authServiceUrl, `auth-${config.environment}`)}
                                      disabled={testingUrl === `auth-${config.environment}`}
                                      className="text-blue-600 hover:text-blue-700 disabled:opacity-50"
                                      title="Test Connection"
                                    >
                                      {testingUrl === `auth-${config.environment}` ? (
                                        <i className="fas fa-spinner fa-spin"></i>
                                      ) : (
                                        <i className="fas fa-vial"></i>
                                      )}
                                    </button>
                                    {urlTestResults[`auth-${config.environment}`] && (
                                      <span className={`text-xs font-semibold ${
                                        urlTestResults[`auth-${config.environment}`].status === 'success' 
                                          ? 'text-green-600' 
                                          : 'text-red-600'
                                      }`}>
                                        {urlTestResults[`auth-${config.environment}`].status === 'success' 
                                          ? `✓ ${urlTestResults[`auth-${config.environment}`].latency}ms` 
                                          : '✗ Failed'}
                                      </span>
                                    )}
                                  </div>
                                  
                                  <div className="flex items-center gap-2 text-sm">
                                    <i className="fas fa-exchange-alt w-4 text-purple-600"></i>
                                    <span className="text-gray-500 font-medium">Mule:</span>
                                    <span className="text-gray-700 truncate flex-1">{config.muleServiceUrl}</span>
                                    <button
                                      onClick={() => testUrl(config.muleServiceUrl, `mule-${config.environment}`)}
                                      disabled={testingUrl === `mule-${config.environment}`}
                                      className="text-blue-600 hover:text-blue-700 disabled:opacity-50"
                                      title="Test Connection"
                                    >
                                      {testingUrl === `mule-${config.environment}` ? (
                                        <i className="fas fa-spinner fa-spin"></i>
                                      ) : (
                                        <i className="fas fa-vial"></i>
                                      )}
                                    </button>
                                    {urlTestResults[`mule-${config.environment}`] && (
                                      <span className={`text-xs font-semibold ${
                                        urlTestResults[`mule-${config.environment}`].status === 'success' 
                                          ? 'text-green-600' 
                                          : 'text-red-600'
                                      }`}>
                                        {urlTestResults[`mule-${config.environment}`].status === 'success' 
                                          ? `✓ ${urlTestResults[`mule-${config.environment}`].latency}ms` 
                                          : '✗ Failed'}
                                      </span>
                                    )}
                                  </div>

                                  {isExpanded && (
                                    <div className="flex items-center gap-2 text-sm pt-2 border-t border-gray-200">
                                      <i className="fas fa-globe w-4 text-green-600"></i>
                                      <span className="text-gray-500 font-medium">Catalog:</span>
                                      <span className="text-gray-700 truncate flex-1">{config.catalogBaseUrl}</span>
                                    </div>
                                  )}
                                </div>

                                {/* Metadata */}
                                {isExpanded && (
                                  <div className="grid grid-cols-2 gap-3 mb-4 p-3 bg-gray-50 rounded-lg text-xs">
                                    <div>
                                      <span className="text-gray-500">Created:</span>
                                      <p className="font-semibold text-gray-700">
                                        {config.createdAt ? new Date(config.createdAt).toLocaleDateString() : 'N/A'}
                                      </p>
                                    </div>
                                    <div>
                                      <span className="text-gray-500">Updated:</span>
                                      <p className="font-semibold text-gray-700">
                                        {config.updatedAt ? new Date(config.updatedAt).toLocaleDateString() : 'N/A'}
                                      </p>
                                    </div>
                                    {config.createdBy && (
                                      <div>
                                        <span className="text-gray-500">Created By:</span>
                                        <p className="font-semibold text-gray-700">{config.createdBy}</p>
                                      </div>
                                    )}
                                    {config.updatedBy && (
                                      <div>
                                        <span className="text-gray-500">Updated By:</span>
                                        <p className="font-semibold text-gray-700">{config.updatedBy}</p>
                                      </div>
                                    )}
                                  </div>
                                )}

                                {/* Actions */}
                                <div className="flex items-center justify-between pt-3 border-t border-gray-200">
                                  <button
                                    onClick={() => setExpandedEnvCard(isExpanded ? null : config.environment)}
                                    className="text-sm text-blue-600 hover:text-blue-700 font-semibold"
                                  >
                                    <i className={`fas fa-chevron-${isExpanded ? 'up' : 'down'} mr-1`}></i>
                                    {isExpanded ? 'Show Less' : 'Show More'}
                                  </button>
                                  <div className="flex gap-2">
                                    <button
                                      onClick={() => {
                                        setEditingEnv(config);
                                        setShowEnvModal(true);
                                        setShowPassword(false);
                                      }}
                                      className="px-3 py-1.5 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-all font-semibold text-sm"
                                    >
                                      <i className="fas fa-edit mr-1"></i>
                                      Edit
                                    </button>
                                    <button
                                      onClick={() => deleteEnvironmentConfig(config.environment)}
                                      className="px-3 py-1.5 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition-all font-semibold text-sm"
                                    >
                                      <i className="fas fa-trash mr-1"></i>
                                      Delete
                                    </button>
                                  </div>
                                </div>
                              </div>
                            </div>
                          );
                        })
                      )}
                    </div>
                  )}

                  {/* Table View */}
                  {viewMode === 'table' && (
                  <div className="bg-white rounded-lg border border-gray-200 overflow-hidden mb-6">
                    <table className="w-full">
                      <thead className="bg-gray-50 border-b border-gray-200">
                       <tr>
                         <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Environment</th>
                         <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Auth Service</th>
                         <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Auth Email</th>
                         <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Timeout</th>
                         <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Retries</th>
                         <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Status</th>
                         <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Actions</th>
                       </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-200">
                        {envConfigs.length === 0 ? (
                          <tr>
                            <td colSpan={7} className="px-4 py-8 text-center text-gray-500">
                              <i className="fas fa-inbox text-3xl mb-2 block text-gray-300"></i>
                              No environment configurations found
                            </td>
                          </tr>
                        ) : (
                          envConfigs.map((config) => (
                            <tr key={config.id} className="hover:bg-gray-50 transition-colors">
                              <td className="px-4 py-3">
                                <div className="font-semibold text-gray-900">{config.environment}</div>
                                {config.description && <div className="text-xs text-gray-500 mt-1">{config.description}</div>}
                              </td>
                              <td className="px-4 py-3">
                                <div className="flex items-center gap-2">
                                  <span className="text-sm text-gray-600 truncate max-w-xs">{config.authServiceUrl}</span>
                                  <button
                                    onClick={() => testUrl(config.authServiceUrl, `auth-${config.environment}`)}
                                    disabled={testingUrl === `auth-${config.environment}`}
                                    className="text-blue-600 hover:text-blue-700 disabled:opacity-50"
                                    title="Test URL"
                                  >
                                    {testingUrl === `auth-${config.environment}` ? (
                                      <i className="fas fa-spinner fa-spin text-xs"></i>
                                    ) : (
                                      <i className="fas fa-vial text-xs"></i>
                                    )}
                                  </button>
                                  {urlTestResults[`auth-${config.environment}`] && (
                                    <span className={`text-xs ${urlTestResults[`auth-${config.environment}`].status === 'success' ? 'text-green-600' : 'text-red-600'}`}>
                                      {urlTestResults[`auth-${config.environment}`].status === 'success' 
                                        ? `✓ ${urlTestResults[`auth-${config.environment}`].latency}ms` 
                                        : '✗'}
                                    </span>
                                  )}
                                </div>
                              </td>
                              <td className="px-4 py-3 text-sm text-gray-600">
                                {config.authEmail || '-'}
                              </td>
                              <td className="px-4 py-3 text-sm text-gray-600">
                                {config.timeout ? `${config.timeout}ms` : '-'}
                              </td>
                              <td className="px-4 py-3 text-sm text-gray-600">
                                {config.retryAttempts ?? '-'}
                              </td>
                              <td className="px-4 py-3">
                                <span className={`px-2 py-1 rounded text-xs font-semibold ${
                                  config.enabled ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                                }`}>
                                  {config.enabled ? 'Enabled' : 'Disabled'}
                                </span>
                              </td>
                              <td className="px-4 py-3">
                                <div className="flex gap-2">
                                  <button
                                    onClick={() => {
                                      setEditingEnv(config);
                                      setShowEnvModal(true);
                                      setShowPassword(false);
                                    }}
                                    className="text-blue-600 hover:text-blue-700"
                                    title="Edit"
                                  >
                                    <i className="fas fa-edit"></i>
                                  </button>
                                  <button
                                    onClick={() => deleteEnvironmentConfig(config.environment)}
                                    className="text-red-600 hover:text-red-700"
                                    title="Delete"
                                  >
                                    <i className="fas fa-trash"></i>
                                  </button>
                                </div>
                              </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                  )}

                  {/* Environment Modal */}
                  {showEnvModal && editingEnv && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                      <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
                        <div className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white p-6 rounded-t-xl">
                          <h3 className="text-2xl font-bold">
                            <i className="fas fa-server mr-2"></i>
                            {editingEnv.id ? 'Edit Environment' : 'Add New Environment'}
                          </h3>
                        </div>
                        
                        <div className="p-6 space-y-4">
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Environment Name <span className="text-red-500">*</span>
                            </label>
                            <input
                              type="text"
                              value={editingEnv.environment}
                              onChange={(e) => setEditingEnv({...editingEnv, environment: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                              placeholder="dev, stage, prod, s4-dev, etc."
                              disabled={!!editingEnv.id}
                            />
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Auth Service URL <span className="text-red-500">*</span>
                            </label>
                            <input
                              type="text"
                              value={editingEnv.authServiceUrl}
                              onChange={(e) => setEditingEnv({...editingEnv, authServiceUrl: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                              placeholder="http://localhost:8082/auth"
                            />
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Mule Service URL <span className="text-red-500">*</span>
                            </label>
                            <input
                              type="text"
                              value={editingEnv.muleServiceUrl}
                              onChange={(e) => setEditingEnv({...editingEnv, muleServiceUrl: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                              placeholder="http://localhost:8082/catalog"
                            />
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Catalog Base URL <span className="text-red-500">*</span>
                            </label>
                            <input
                              type="text"
                              value={editingEnv.catalogBaseUrl}
                              onChange={(e) => setEditingEnv({...editingEnv, catalogBaseUrl: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                              placeholder="http://localhost:3000/catalog"
                            />
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Auth Email
                            </label>
                            <input
                              type="text"
                              value={editingEnv.authEmail || ''}
                              onChange={(e) => setEditingEnv({...editingEnv, authEmail: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                              placeholder="auth-email@example.com"
                            />
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Auth Password
                            </label>
                            <div className="relative">
                              <input
                                type={showPassword && editingEnv.authPassword?.startsWith('ENC(') ? 'text' : 'password'}
                                value={editingEnv.authPassword || ''}
                                onChange={(e) => setEditingEnv({...editingEnv, authPassword: e.target.value})}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder={editingEnv.authPassword?.startsWith('ENC(') ? '•••••••• (encrypted)' : 'Enter password'}
                              />
                              {editingEnv.authPassword?.startsWith('ENC(') && (
                                <button
                                  type="button"
                                  onClick={() => setShowPassword(!showPassword)}
                                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-600 hover:text-gray-800"
                                  title={showPassword ? 'Hide password' : 'Show password'}
                                >
                                  <i className={`fas fa-${showPassword ? 'eye-slash' : 'eye'}`}></i>
                                </button>
                              )}
                            </div>
                            {editingEnv.authPassword?.startsWith('ENC(') && (
                              <p className="text-xs text-gray-500 mt-1">
                                <i className="fas fa-lock mr-1"></i>
                                Password is encrypted
                              </p>
                            )}
                          </div>

                          <div className="grid grid-cols-2 gap-4">
                            <div>
                              <label className="block text-sm font-medium text-gray-700 mb-2">
                                Timeout (ms)
                              </label>
                              <input
                                type="number"
                                value={editingEnv.timeout ?? 30000}
                                onChange={(e) => setEditingEnv({...editingEnv, timeout: parseInt(e.target.value) || 30000})}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="30000"
                                min="1000"
                              />
                            </div>

                            <div>
                              <label className="block text-sm font-medium text-gray-700 mb-2">
                                Retry Attempts
                              </label>
                              <input
                                type="number"
                                value={editingEnv.retryAttempts ?? 3}
                                onChange={(e) => setEditingEnv({...editingEnv, retryAttempts: parseInt(e.target.value) || 3})}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="3"
                                min="0"
                                max="10"
                              />
                            </div>
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Health Check URL
                            </label>
                            <input
                              type="text"
                              value={editingEnv.healthCheckUrl || ''}
                              onChange={(e) => setEditingEnv({...editingEnv, healthCheckUrl: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                              placeholder="http://localhost:8082/health (optional)"
                            />
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Description
                            </label>
                            <textarea
                              value={editingEnv.description || ''}
                              onChange={(e) => setEditingEnv({...editingEnv, description: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                              rows={3}
                              placeholder="Optional description..."
                            />
                          </div>

                          <div className="flex items-center">
                            <input
                              type="checkbox"
                              checked={editingEnv.enabled}
                              onChange={(e) => setEditingEnv({...editingEnv, enabled: e.target.checked})}
                              className="mr-2 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                            />
                            <label className="text-sm font-medium text-gray-700">
                              Enabled
                            </label>
                          </div>
                        </div>

                        <div className="bg-gray-50 px-6 py-4 rounded-b-xl flex justify-end gap-3">
                          <button
                            onClick={() => {
                              setShowEnvModal(false);
                              setEditingEnv(null);
                            }}
                            className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-all"
                          >
                            Cancel
                          </button>
                          <button
                            onClick={() => saveEnvironmentConfig(editingEnv)}
                            disabled={!editingEnv.environment || !editingEnv.authServiceUrl || !editingEnv.muleServiceUrl || !editingEnv.catalogBaseUrl}
                            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            <i className="fas fa-save mr-2"></i>
                            Save Environment
                          </button>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'api' && (
                <div className="p-8">
                  <div className="mb-8">
                    <h2 className="text-2xl font-bold text-gray-900 mb-2">
                      <i className="fas fa-plug text-green-600 mr-2"></i>
                      API Configuration
                    </h2>
                    <p className="text-gray-600">Configure API endpoints and authentication settings for external integrations.</p>
                  </div>
                  <div className="bg-gradient-to-br from-green-50 to-emerald-50 border border-green-200 rounded-xl p-8 text-center">
                    <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 rounded-full mb-4">
                      <i className="fas fa-code text-2xl text-green-600"></i>
                    </div>
                    <h3 className="text-lg font-bold text-gray-900 mb-2">Coming Soon</h3>
                    <p className="text-gray-600">API configuration panel is under development</p>
                  </div>
                </div>
              )}

              {activeTab === 'security' && (
                <div className="p-8">
                  <div className="flex justify-between items-center mb-8">
                    <div>
                      <h2 className="text-2xl font-bold text-gray-900">
                        <i className="fas fa-shield-alt text-red-600 mr-2"></i>
                        Security & Authentication
                      </h2>
                      <p className="text-sm text-gray-600 mt-1">Manage API keys, authentication methods, and monitor security events</p>
                    </div>
                    <button
                      onClick={fetchSecurityData}
                      className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-all"
                    >
                      <i className="fas fa-sync-alt mr-2"></i>
                      Refresh
                    </button>
                  </div>

                  {/* Security Statistics */}
                  <div className="grid grid-cols-4 gap-4 mb-6">
                    <div className="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-lg p-4 shadow-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-blue-100 text-sm">Total API Keys</p>
                          <p className="text-3xl font-bold mt-1">{securityStats?.totalApiKeys || 0}</p>
                        </div>
                        <i className="fas fa-key text-4xl text-blue-200 opacity-50"></i>
                      </div>
                    </div>

                    <div className="bg-gradient-to-br from-green-500 to-green-600 text-white rounded-lg p-4 shadow-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-green-100 text-sm">Active Keys</p>
                          <p className="text-3xl font-bold mt-1">{securityStats?.activeApiKeys || 0}</p>
                        </div>
                        <i className="fas fa-check-circle text-4xl text-green-200 opacity-50"></i>
                      </div>
                    </div>

                    <div className="bg-gradient-to-br from-yellow-500 to-orange-600 text-white rounded-lg p-4 shadow-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-yellow-100 text-sm">Auth Success</p>
                          <p className="text-3xl font-bold mt-1">{securityStats?.authSuccesses24h || 0}</p>
                        </div>
                        <i className="fas fa-user-check text-4xl text-yellow-200 opacity-50"></i>
                      </div>
                      <p className="text-xs text-yellow-100 mt-2">Last 24 hours</p>
                    </div>

                    <div className="bg-gradient-to-br from-red-500 to-red-600 text-white rounded-lg p-4 shadow-lg">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-red-100 text-sm">Auth Failures</p>
                          <p className="text-3xl font-bold mt-1">{securityStats?.authFailures24h || 0}</p>
                        </div>
                        <i className="fas fa-exclamation-triangle text-4xl text-red-200 opacity-50"></i>
                      </div>
                      <p className="text-xs text-red-100 mt-2">Last 24 hours</p>
                    </div>
                  </div>

                  {/* Security Tabs */}
                  <div className="flex gap-2 mb-6 border-b border-gray-200">
                    {[
                      { id: 'api-keys', label: 'API Keys', icon: 'key' },
                      { id: 'jwt', label: 'JWT Configuration', icon: 'shield-alt' },
                      { id: 'audit', label: 'Audit Logs', icon: 'list-alt' }
                    ].map((tab) => (
                      <button
                        key={tab.id}
                        onClick={() => setSecurityTab(tab.id as any)}
                        className={`px-4 py-3 font-semibold text-sm transition-all border-b-2 ${
                          securityTab === tab.id
                            ? 'border-red-600 text-red-600'
                            : 'border-transparent text-gray-600 hover:text-gray-900'
                        }`}
                      >
                        <i className={`fas fa-${tab.icon} mr-2`}></i>
                        {tab.label}
                      </button>
                    ))}
                  </div>

                  {/* API Keys Tab */}
                  {securityTab === 'api-keys' && (
                    <div>
                      <div className="flex justify-between items-center mb-4">
                        <h3 className="text-lg font-semibold text-gray-900">
                          <i className="fas fa-key text-red-600 mr-2"></i>
                          API Key Management
                        </h3>
                        <button
                          onClick={() => setShowApiKeyModal(true)}
                          className="px-4 py-2 bg-gradient-to-r from-red-500 to-red-600 text-white rounded-lg hover:from-red-600 hover:to-red-700 transition-all shadow-md font-semibold"
                        >
                          <i className="fas fa-plus mr-2"></i>
                          Generate API Key
                        </button>
                      </div>

                      <div className="grid grid-cols-1 gap-4">
                        {apiKeys.length === 0 ? (
                          <div className="text-center py-12 bg-gray-50 rounded-lg border-2 border-dashed border-gray-300">
                            <i className="fas fa-key text-5xl text-gray-300 mb-3"></i>
                            <p className="text-gray-500 font-semibold">No API keys generated</p>
                            <p className="text-sm text-gray-400 mt-1">Click &quot;Generate API Key&quot; to create your first key</p>
                          </div>
                        ) : (
                          apiKeys.map((key) => (
                            <div key={key.id} className="bg-white border-2 border-gray-200 rounded-xl p-5 hover:border-gray-300 transition-all">
                              <div className="flex items-start justify-between">
                                <div className="flex-1">
                                  <div className="flex items-center gap-3 mb-3">
                                    <div className="bg-red-100 p-2 rounded-lg">
                                      <i className="fas fa-user-shield text-red-600 text-xl"></i>
                                    </div>
                                    <div>
                                      <h4 className="font-bold text-gray-900">{key.customerName}</h4>
                                      <p className="text-sm text-gray-600">{key.description}</p>
                                    </div>
                                    <span className={`ml-2 px-3 py-1 rounded-full text-xs font-bold ${
                                      key.enabled ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                                    }`}>
                                      {key.enabled ? 'Active' : 'Revoked'}
                                    </span>
                                  </div>

                                  <div className="grid grid-cols-2 gap-4 mb-3">
                                    <div>
                                      <p className="text-xs text-gray-500">API Key</p>
                                      <div className="flex items-center gap-2 mt-1">
                                        <code className="text-sm bg-gray-100 px-3 py-1 rounded font-mono text-gray-700">
                                          {key.keyValue.substring(0, 20)}...
                                        </code>
                                        <button
                                          onClick={() => {
                                            navigator.clipboard.writeText(key.keyValue);
                                            alert('API Key copied to clipboard!');
                                          }}
                                          className="text-blue-600 hover:text-blue-700"
                                          title="Copy to clipboard"
                                        >
                                          <i className="fas fa-copy"></i>
                                        </button>
                                      </div>
                                    </div>
                                    <div>
                                      <p className="text-xs text-gray-500">Environment</p>
                                      <p className="text-sm font-semibold text-gray-700 mt-1">{key.environment.toUpperCase()}</p>
                                    </div>
                                  </div>

                                  <div className="grid grid-cols-4 gap-3 text-xs">
                                    <div>
                                      <span className="text-gray-500">Permissions:</span>
                                      <div className="flex flex-wrap gap-1 mt-1">
                                        {key.permissions.map(p => (
                                          <span key={p} className="px-2 py-0.5 bg-blue-100 text-blue-700 rounded font-semibold">
                                            {p}
                                          </span>
                                        ))}
                                      </div>
                                    </div>
                                    <div>
                                      <span className="text-gray-500">Created:</span>
                                      <p className="font-semibold text-gray-700 mt-1">
                                        {new Date(key.createdAt).toLocaleDateString()}
                                      </p>
                                    </div>
                                    <div>
                                      <span className="text-gray-500">Last Used:</span>
                                      <p className="font-semibold text-gray-700 mt-1">
                                        {key.lastUsedAt ? new Date(key.lastUsedAt).toLocaleString() : 'Never'}
                                      </p>
                                    </div>
                                    <div>
                                      <span className="text-gray-500">Usage Count:</span>
                                      <p className="font-semibold text-gray-700 mt-1">{key.usageCount || 0} requests</p>
                                    </div>
                                  </div>
                                </div>

                                <div className="flex flex-col gap-2 ml-4">
                                  {key.enabled && (
                                    <>
                                      <button
                                        onClick={() => rotateApiKey(key.id)}
                                        className="px-3 py-2 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-all text-sm font-semibold whitespace-nowrap"
                                      >
                                        <i className="fas fa-sync-alt mr-1"></i>
                                        Rotate
                                      </button>
                                      <button
                                        onClick={() => revokeApiKey(key.id)}
                                        className="px-3 py-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition-all text-sm font-semibold whitespace-nowrap"
                                      >
                                        <i className="fas fa-ban mr-1"></i>
                                        Revoke
                                      </button>
                                    </>
                                  )}
                                </div>
                              </div>

                              {key.expiresAt && (
                                <div className="mt-3 pt-3 border-t border-gray-200 flex items-center gap-2 text-sm">
                                  <i className="fas fa-clock text-orange-600"></i>
                                  <span className="text-gray-600">
                                    Expires: <span className="font-semibold">{new Date(key.expiresAt).toLocaleDateString()}</span>
                                  </span>
                                </div>
                              )}
                            </div>
                          ))
                        )}
                      </div>
                    </div>
                  )}

                  {/* JWT Configuration Tab */}
                  {securityTab === 'jwt' && (
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900 mb-4">
                        <i className="fas fa-shield-alt text-red-600 mr-2"></i>
                        JWT Token Configuration
                      </h3>

                      <div className="grid grid-cols-2 gap-6">
                        <div className="bg-gray-50 border border-gray-200 rounded-xl p-6">
                          <h4 className="font-bold text-gray-900 mb-4">Current Settings</h4>
                          <div className="space-y-3">
                            <div>
                              <label className="text-sm text-gray-600">Algorithm</label>
                              <p className="font-semibold text-gray-900">{jwtConfig?.algorithm || 'HS256'}</p>
                            </div>
                            <div>
                              <label className="text-sm text-gray-600">Token Expiration</label>
                              <p className="font-semibold text-gray-900">{jwtConfig?.expirationMinutes || 30} minutes</p>
                            </div>
                            <div>
                              <label className="text-sm text-gray-600">Issuer</label>
                              <p className="font-semibold text-gray-900">{jwtConfig?.issuer || 'waters-punchout-platform'}</p>
                            </div>
                            <div>
                              <label className="text-sm text-gray-600">Active Tokens</label>
                              <p className="font-semibold text-gray-900">{jwtConfig?.activeTokensCount || 0}</p>
                            </div>
                          </div>
                        </div>

                        <div className="bg-gradient-to-br from-red-50 to-rose-50 border border-red-200 rounded-xl p-6">
                          <h4 className="font-bold text-gray-900 mb-4">Update Configuration</h4>
                          <div className="space-y-4">
                            <div>
                              <label className="block text-sm font-medium text-gray-700 mb-2">
                                Token Expiration (minutes)
                              </label>
                              <input
                                type="number"
                                defaultValue={jwtConfig?.expirationMinutes || 30}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                              />
                            </div>
                            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 text-sm text-yellow-800">
                              <i className="fas fa-exclamation-triangle mr-2"></i>
                              Changing JWT settings will invalidate all existing tokens
                            </div>
                            <button className="w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-all font-semibold">
                              <i className="fas fa-save mr-2"></i>
                              Update Configuration
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Audit Logs Tab */}
                  {securityTab === 'audit' && (
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900 mb-4">
                        <i className="fas fa-list-alt text-red-600 mr-2"></i>
                        Security Audit Logs
                      </h3>

                      <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
                        <table className="w-full">
                          <thead className="bg-gray-50 border-b border-gray-200">
                            <tr>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Time</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Event</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Severity</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Customer</th>
                              <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">Description</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-gray-200">
                            {auditLogs.length === 0 ? (
                              <tr>
                                <td colSpan={5} className="px-4 py-8 text-center text-gray-500">
                                  <i className="fas fa-inbox text-3xl mb-2 block text-gray-300"></i>
                                  No audit logs found
                                </td>
                              </tr>
                            ) : (
                              auditLogs.map((log) => (
                                <tr key={log.id} className="hover:bg-gray-50 transition-colors">
                                  <td className="px-4 py-3 text-sm text-gray-600 whitespace-nowrap">
                                    {new Date(log.timestamp).toLocaleString()}
                                  </td>
                                  <td className="px-4 py-3">
                                    <span className="text-sm font-medium text-gray-700">{log.eventType}</span>
                                  </td>
                                  <td className="px-4 py-3">
                                    <span className={`px-2 py-1 rounded text-xs font-bold ${
                                      log.severity === 'CRITICAL' ? 'bg-red-100 text-red-700' :
                                      log.severity === 'ERROR' ? 'bg-orange-100 text-orange-700' :
                                      log.severity === 'WARNING' ? 'bg-yellow-100 text-yellow-700' :
                                      'bg-blue-100 text-blue-700'
                                    }`}>
                                      {log.severity}
                                    </span>
                                  </td>
                                  <td className="px-4 py-3 text-sm text-gray-700">
                                    {log.customerName || '-'}
                                  </td>
                                  <td className="px-4 py-3 text-sm text-gray-600">
                                    {log.description}
                                  </td>
                                </tr>
                              ))
                            )}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  )}

                  {/* API Key Generation Modal */}
                  {showApiKeyModal && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                      <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full mx-4">
                        <div className="bg-gradient-to-r from-red-600 to-rose-600 text-white p-6 rounded-t-xl">
                          <h3 className="text-2xl font-bold">
                            <i className="fas fa-key mr-2"></i>
                            Generate New API Key
                          </h3>
                        </div>
                        
                        <div className="p-6 space-y-4">
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Customer Name <span className="text-red-500">*</span>
                            </label>
                            <input
                              type="text"
                              value={newApiKey.customerName}
                              onChange={(e) => setNewApiKey({...newApiKey, customerName: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                              placeholder="e.g., Acme Corporation"
                            />
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Description
                            </label>
                            <input
                              type="text"
                              value={newApiKey.description}
                              onChange={(e) => setNewApiKey({...newApiKey, description: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                              placeholder="e.g., Production API key"
                            />
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Environment <span className="text-red-500">*</span>
                            </label>
                            <select
                              value={newApiKey.environment}
                              onChange={(e) => setNewApiKey({...newApiKey, environment: e.target.value})}
                              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500"
                            >
                              <option value="dev">Development</option>
                              <option value="stage">Staging</option>
                              <option value="prod">Production</option>
                              <option value="s4-dev">S4 Dev</option>
                            </select>
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Permissions
                            </label>
                            <div className="flex flex-wrap gap-2">
                              {['PUNCHOUT', 'ORDER', 'INVOICE', 'READ', 'WRITE'].map((perm) => (
                                <label key={perm} className="flex items-center gap-2 px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-100">
                                  <input
                                    type="checkbox"
                                    checked={newApiKey.permissions?.includes(perm)}
                                    onChange={(e) => {
                                      const perms = newApiKey.permissions || [];
                                      setNewApiKey({
                                        ...newApiKey,
                                        permissions: e.target.checked 
                                          ? [...perms, perm]
                                          : perms.filter(p => p !== perm)
                                      });
                                    }}
                                    className="rounded text-red-600"
                                  />
                                  <span className="text-sm font-semibold text-gray-700">{perm}</span>
                                </label>
                              ))}
                            </div>
                          </div>
                        </div>

                        <div className="bg-gray-50 px-6 py-4 rounded-b-xl flex justify-end gap-3">
                          <button
                            onClick={() => {
                              setShowApiKeyModal(false);
                              setNewApiKey({
                                customerName: '',
                                description: '',
                                permissions: ['PUNCHOUT', 'ORDER'],
                                environment: 'dev',
                                enabled: true
                              });
                            }}
                            className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-all"
                          >
                            Cancel
                          </button>
                          <button
                            onClick={generateApiKey}
                            disabled={!newApiKey.customerName}
                            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            <i className="fas fa-key mr-2"></i>
                            Generate API Key
                          </button>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'monitoring' && (
                <div className="p-8">
                  <div className="flex justify-between items-center mb-8">
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
                <div className="p-8">
                  <div className="mb-8">
                    <h2 className="text-2xl font-bold text-gray-900 mb-2">
                      <i className="fas fa-bell text-yellow-600 mr-2"></i>
                      Notification Settings
                    </h2>
                    <p className="text-gray-600">Configure email alerts, webhook notifications, and system event triggers.</p>
                  </div>
                  <div className="bg-gradient-to-br from-yellow-50 to-amber-50 border border-yellow-200 rounded-xl p-8 text-center">
                    <div className="inline-flex items-center justify-center w-16 h-16 bg-yellow-100 rounded-full mb-4">
                      <i className="fas fa-envelope text-2xl text-yellow-600"></i>
                    </div>
                    <h3 className="text-lg font-bold text-gray-900 mb-2">Coming Soon</h3>
                    <p className="text-gray-600">Notification settings panel is under development</p>
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
