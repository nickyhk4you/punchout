'use client';

import { useState, useEffect } from 'react';
import { sessionAPI } from '@/lib/api';
import { PunchOutSession } from '@/types';
import Link from 'next/link';

export default function HomePage() {
  const [stats, setStats] = useState({
    totalSessions: 0,
    devSessions: 0,
    stageSessions: 0,
    prodSessions: 0,
    recentSessions: [] as PunchOutSession[],
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const allSessions = await sessionAPI.getAllSessions();
      
      const devSessions = allSessions.filter(s => s.environment === 'DEVELOPMENT');
      const stageSessions = allSessions.filter(s => s.environment === 'STAGING');
      const prodSessions = allSessions.filter(s => s.environment === 'PRODUCTION');
      const recentSessions = allSessions
        .sort((a, b) => new Date(b.sessionDate).getTime() - new Date(a.sessionDate).getTime())
        .slice(0, 5);

      setStats({
        totalSessions: allSessions.length,
        devSessions: devSessions.length,
        stageSessions: stageSessions.length,
        prodSessions: prodSessions.length,
        recentSessions,
      });
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div>
      {loading ? (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-4 border-blue-600"></div>
            <p className="mt-4 text-gray-600 text-lg">Loading dashboard...</p>
          </div>
        </div>
      ) : (
        <>
          {/* Hero Section */}
          <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white">
            <div className="container mx-auto px-4 py-16">
              <div className="max-w-4xl">
                <h1 className="text-5xl font-bold mb-4">
                  <i className="fas fa-rocket mr-4"></i>
                  PunchOut Testing Platform
                </h1>
                <p className="text-xl text-blue-100 mb-8">
                  Test, monitor, and debug your PunchOut integrations across all environments
                </p>
                <div className="flex gap-4">
                  <Link
                    href="/developer/punchout"
                    className="inline-flex items-center px-6 py-3 bg-white text-blue-600 font-semibold rounded-lg shadow-lg hover:bg-blue-50 transition transform hover:scale-105"
                  >
                    <i className="fas fa-play mr-2"></i>
                    Start Testing
                  </Link>
                  <Link
                    href="/sessions"
                    className="inline-flex items-center px-6 py-3 bg-blue-500 text-white font-semibold rounded-lg shadow-lg hover:bg-blue-400 transition transform hover:scale-105"
                  >
                    <i className="fas fa-list mr-2"></i>
                    View Sessions
                  </Link>
                </div>
              </div>
            </div>
          </div>

          {/* Stats Overview */}
          <div className="bg-white border-b shadow-sm">
            <div className="container mx-auto px-4 py-8">
              <h2 className="text-2xl font-bold mb-6 text-gray-800">Overview</h2>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-6 border border-blue-200">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-blue-600 uppercase">Total Sessions</p>
                      <p className="text-4xl font-bold text-blue-900 mt-2">{stats.totalSessions}</p>
                    </div>
                    <div className="bg-blue-500 rounded-full p-4 shadow-lg">
                      <i className="fas fa-database text-2xl text-white"></i>
                    </div>
                  </div>
                </div>

                <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-6 border border-green-200">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-green-600 uppercase">DEV</p>
                      <p className="text-4xl font-bold text-green-900 mt-2">{stats.devSessions}</p>
                    </div>
                    <div className="bg-green-500 rounded-full p-4 shadow-lg">
                      <i className="fas fa-code text-2xl text-white"></i>
                    </div>
                  </div>
                </div>

                <div className="bg-gradient-to-br from-orange-50 to-orange-100 rounded-xl p-6 border border-orange-200">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-orange-600 uppercase">STAGE</p>
                      <p className="text-4xl font-bold text-orange-900 mt-2">{stats.stageSessions}</p>
                    </div>
                    <div className="bg-orange-500 rounded-full p-4 shadow-lg">
                      <i className="fas fa-vial text-2xl text-white"></i>
                    </div>
                  </div>
                </div>

                <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-6 border border-red-200">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-red-600 uppercase">PROD</p>
                      <p className="text-4xl font-bold text-red-900 mt-2">{stats.prodSessions}</p>
                    </div>
                    <div className="bg-red-500 rounded-full p-4 shadow-lg">
                      <i className="fas fa-check-circle text-2xl text-white"></i>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Main Content */}
          <div className="container mx-auto px-4 py-8">
            {/* Quick Actions */}
            <div className="mb-8">
              <h2 className="text-2xl font-bold mb-6 text-gray-800">Quick Actions</h2>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <Link
                  href="/developer/punchout"
                  className="group bg-white rounded-xl shadow-lg hover:shadow-xl transition-all border border-gray-200 overflow-hidden"
                >
                  <div className="bg-gradient-to-r from-purple-500 to-purple-600 p-6">
                    <i className="fas fa-rocket text-4xl text-white mb-2"></i>
                    <h3 className="text-2xl font-bold text-white">Start Testing</h3>
                  </div>
                  <div className="p-6">
                    <p className="text-gray-600 mb-4">
                      Execute PunchOut tests across DEV, STAGE, PROD, and S4-DEV environments
                    </p>
                    <div className="flex items-center text-purple-600 font-semibold group-hover:translate-x-2 transition-transform">
                      Launch Developer Testing
                      <i className="fas fa-arrow-right ml-2"></i>
                    </div>
                  </div>
                </Link>

                <Link
                  href="/sessions"
                  className="group bg-white rounded-xl shadow-lg hover:shadow-xl transition-all border border-gray-200 overflow-hidden"
                >
                  <div className="bg-gradient-to-r from-blue-500 to-blue-600 p-6">
                    <i className="fas fa-list text-4xl text-white mb-2"></i>
                    <h3 className="text-2xl font-bold text-white">All Sessions</h3>
                  </div>
                  <div className="p-6">
                    <p className="text-gray-600 mb-4">
                      View, filter, and analyze all PunchOut sessions with network request logs
                    </p>
                    <div className="flex items-center text-blue-600 font-semibold group-hover:translate-x-2 transition-transform">
                      Browse Sessions
                      <i className="fas fa-arrow-right ml-2"></i>
                    </div>
                  </div>
                </Link>

                <Link
                  href="/converter"
                  className="group bg-white rounded-xl shadow-lg hover:shadow-xl transition-all border border-gray-200 overflow-hidden"
                >
                  <div className="bg-gradient-to-r from-green-500 to-green-600 p-6">
                    <i className="fas fa-exchange-alt text-4xl text-white mb-2"></i>
                    <h3 className="text-2xl font-bold text-white">Converter</h3>
                  </div>
                  <div className="p-6">
                    <p className="text-gray-600 mb-4">
                      Convert between cXML and JSON formats for development and testing
                    </p>
                    <div className="flex items-center text-green-600 font-semibold group-hover:translate-x-2 transition-transform">
                      Open Converter
                      <i className="fas fa-arrow-right ml-2"></i>
                    </div>
                  </div>
                </Link>
              </div>
            </div>

            {/* Recent Sessions */}
            <div className="bg-white rounded-xl shadow-lg border border-gray-200">
              <div className="px-6 py-5 border-b border-gray-200 flex items-center justify-between">
                <h2 className="text-2xl font-bold text-gray-800">
                  <i className="fas fa-clock text-blue-600 mr-3"></i>
                  Recent Sessions
                </h2>
                <Link
                  href="/sessions"
                  className="text-blue-600 hover:text-blue-800 font-semibold text-sm"
                >
                  View All →
                </Link>
              </div>
              {stats.recentSessions.length > 0 ? (
                <div className="divide-y divide-gray-100">
                  {stats.recentSessions.map((session) => (
                    <Link
                      key={session.sessionKey}
                      href={`/sessions/${session.sessionKey}`}
                      className="block px-6 py-4 hover:bg-blue-50 transition-colors"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <code className="text-sm font-semibold bg-gray-100 px-3 py-1 rounded">
                              {session.sessionKey}
                            </code>
                            <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                              session.operation === 'CREATE' ? 'bg-green-100 text-green-800' :
                              session.operation === 'EDIT' ? 'bg-yellow-100 text-yellow-800' :
                              'bg-blue-100 text-blue-800'
                            }`}>
                              {session.operation}
                            </span>
                            <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                              session.environment === 'PRODUCTION' ? 'bg-red-100 text-red-800' :
                              session.environment === 'STAGING' ? 'bg-orange-100 text-orange-800' :
                              'bg-gray-100 text-gray-800'
                            }`}>
                              {session.environment}
                            </span>
                          </div>
                          <div className="flex items-center gap-4 text-sm text-gray-600">
                            <span>
                              <i className="fas fa-envelope mr-1 text-gray-400"></i>
                              {session.contactEmail || 'No contact'}
                            </span>
                            <span>
                              <i className="fas fa-clock mr-1 text-gray-400"></i>
                              {formatDate(session.sessionDate)}
                            </span>
                          </div>
                        </div>
                        <div className="text-blue-600 group-hover:translate-x-1 transition-transform">
                          <i className="fas fa-chevron-right"></i>
                        </div>
                      </div>
                    </Link>
                  ))}
                </div>
              ) : (
                <div className="px-6 py-12 text-center">
                  <i className="fas fa-inbox text-gray-300 text-5xl mb-4"></i>
                  <p className="text-gray-500 text-lg">No sessions yet</p>
                  <Link
                    href="/developer/punchout"
                    className="inline-block mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-semibold"
                  >
                    Start Your First Test
                  </Link>
                </div>
              )}
            </div>

            {/* Features */}
            <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white rounded-xl p-6 shadow border border-gray-200">
                <div className="bg-blue-100 rounded-full w-12 h-12 flex items-center justify-center mb-4">
                  <i className="fas fa-network-wired text-blue-600 text-xl"></i>
                </div>
                <h3 className="text-lg font-bold mb-2">Network Request Logging</h3>
                <p className="text-gray-600 text-sm">
                  Track all INBOUND and OUTBOUND requests with full payloads, headers, and timing
                </p>
              </div>

              <div className="bg-white rounded-xl p-6 shadow border border-gray-200">
                <div className="bg-purple-100 rounded-full w-12 h-12 flex items-center justify-center mb-4">
                  <i className="fas fa-layer-group text-purple-600 text-xl"></i>
                </div>
                <h3 className="text-lg font-bold mb-2">Multi-Environment</h3>
                <p className="text-gray-600 text-sm">
                  Test across DEV, STAGE, PROD, and S4-DEV with environment-specific templates
                </p>
              </div>

              <div className="bg-white rounded-xl p-6 shadow border border-gray-200">
                <div className="bg-green-100 rounded-full w-12 h-12 flex items-center justify-center mb-4">
                  <i className="fas fa-file-code text-green-600 text-xl"></i>
                </div>
                <h3 className="text-lg font-bold mb-2">Custom Templates</h3>
                <p className="text-gray-600 text-sm">
                  Edit cXML payloads with customer-specific templates stored in MongoDB
                </p>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
