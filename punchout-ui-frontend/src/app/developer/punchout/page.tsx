'use client';

import { useState, useEffect } from 'react';
import { catalogRouteAPI, punchOutTestAPI } from '@/lib/api';
import { CatalogRoute, PunchOutTest } from '@/types';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';

export default function DeveloperPunchOutPage() {
  const [catalogRoutes, setCatalogRoutes] = useState<CatalogRoute[]>([]);
  const [recentTests, setRecentTests] = useState<PunchOutTest[]>([]);
  const [selectedEnvironment, setSelectedEnvironment] = useState<string>('production');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [routes, tests] = await Promise.all([
        catalogRouteAPI.getActiveRoutes(),
        punchOutTestAPI.getAllTests(),
      ]);
      setCatalogRoutes(routes);
      setRecentTests(tests.slice(0, 10));
    } catch (err: any) {
      setError(err.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const filteredRoutes = catalogRoutes.filter(route =>
    route.environments.some(env => env.environment === selectedEnvironment && env.enabled)
  );

  const breadcrumbItems = [
    { label: 'Developer', href: '/developer' },
    { label: 'PunchOut Testing' },
  ];

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <Breadcrumb items={breadcrumbItems} />

      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">
          <i className="fas fa-code mr-2 text-purple-600"></i>
          Test PunchOut Catalog
        </h1>
        <p className="text-gray-600">Manually test punchout integrations with various catalog routes</p>
      </div>

      {/* Action Buttons */}
      <div className="mb-6 flex gap-4">
        <Link
          href="/developer/punchout/new-test"
          className="inline-flex items-center px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg shadow hover:bg-blue-700 transition"
        >
          <i className="fas fa-play mr-2"></i>
          New Test
        </Link>
        <Link
          href="/developer/punchout/past-tests"
          className="inline-flex items-center px-6 py-3 bg-gray-100 text-gray-700 font-semibold rounded-lg shadow hover:bg-gray-200 transition"
        >
          <i className="fas fa-history mr-2"></i>
          Past Tests
        </Link>
      </div>

      {/* Environment Selector */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Select Your Catalog Route</h2>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">Environment</label>
          <div className="flex gap-2">
            {['production', 'staging', 'development', 'test'].map(env => (
              <button
                key={env}
                onClick={() => setSelectedEnvironment(env)}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  selectedEnvironment === env
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {env.charAt(0).toUpperCase() + env.slice(1)}
              </button>
            ))}
          </div>
        </div>

        {/* Catalog Routes Table */}
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Domain
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Network
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredRoutes.map((route) => {
                const envConfig = route.environments.find(e => e.environment === selectedEnvironment);
                return (
                  <tr key={route.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="font-medium text-gray-900">{route.routeName}</div>
                      {route.description && (
                        <div className="text-sm text-gray-500">{route.description}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {route.domain}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {route.network}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                        {route.type.toUpperCase()}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <Link
                        href={`/developer/punchout/new-test?routeId=${route.id}&env=${selectedEnvironment}`}
                        className="text-blue-600 hover:text-blue-800 font-medium mr-4"
                      >
                        <i className="fas fa-play mr-1"></i>
                        Start Test
                      </Link>
                      <Link
                        href={`/developer/punchout/routes/${route.id}`}
                        className="text-gray-600 hover:text-gray-800"
                      >
                        <i className="fas fa-info-circle mr-1"></i>
                        Details
                      </Link>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {filteredRoutes.length === 0 && (
          <div className="text-center py-8">
            <i className="fas fa-inbox text-gray-300 text-4xl mb-3"></i>
            <p className="text-gray-500">No active routes found for {selectedEnvironment} environment</p>
          </div>
        )}
      </div>

      {/* Recent Tests */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Recent Tests</h2>
        {recentTests.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Test Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Route
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Environment
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tester
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {recentTests.map((test) => (
                  <tr key={test.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">
                      {test.testName}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">
                      {test.catalogRouteName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">
                        {test.environment}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">
                      {test.tester}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {formatDate(test.testDate)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                        test.status === 'SUCCESS' ? 'bg-green-100 text-green-800' :
                        test.status === 'FAILED' ? 'bg-red-100 text-red-800' :
                        test.status === 'RUNNING' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {test.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <Link
                        href={`/developer/punchout/tests/${test.id}`}
                        className="text-blue-600 hover:text-blue-800 font-medium"
                      >
                        View Details
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-8">
            <i className="fas fa-flask text-gray-300 text-4xl mb-3"></i>
            <p className="text-gray-500">No tests found. Start a new test to begin!</p>
          </div>
        )}
      </div>
    </div>
  );
}
