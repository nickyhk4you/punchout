'use client';

import { useState, useEffect } from 'react';
import { punchOutTestAPI } from '@/lib/api';
import { PunchOutTest } from '@/types';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';

export default function PastTestsPage() {
  const [tests, setTests] = useState<PunchOutTest[]>([]);
  const [filteredTests, setFilteredTests] = useState<PunchOutTest[]>([]);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [environmentFilter, setEnvironmentFilter] = useState<string>('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTests();
  }, []);

  useEffect(() => {
    filterTests();
  }, [tests, statusFilter, environmentFilter]);

  const loadTests = async () => {
    try {
      setLoading(true);
      const data = await punchOutTestAPI.getAllTests();
      setTests(data);
      setFilteredTests(data);
    } catch (err) {
      console.error('Failed to load tests:', err);
    } finally {
      setLoading(false);
    }
  };

  const filterTests = () => {
    let filtered = tests;
    
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(t => t.status === statusFilter);
    }
    
    if (environmentFilter !== 'ALL') {
      filtered = filtered.filter(t => t.environment === environmentFilter);
    }
    
    setFilteredTests(filtered);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  };

  const breadcrumbItems = [
    { label: 'Developer', href: '/developer' },
    { label: 'PunchOut Testing', href: '/developer/punchout' },
    { label: 'Past Tests' },
  ];

  const stats = {
    total: tests.length,
    success: tests.filter(t => t.status === 'SUCCESS').length,
    failed: tests.filter(t => t.status === 'FAILED').length,
    running: tests.filter(t => t.status === 'RUNNING').length,
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <Breadcrumb items={breadcrumbItems} />

      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">
          <i className="fas fa-history mr-2 text-indigo-600"></i>
          Past Tests
        </h1>
        <p className="text-gray-600">View and analyze previous PunchOut test executions</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Total Tests</p>
              <p className="text-2xl font-bold">{stats.total}</p>
            </div>
            <i className="fas fa-flask text-blue-500 text-3xl"></i>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Successful</p>
              <p className="text-2xl font-bold text-green-600">{stats.success}</p>
            </div>
            <i className="fas fa-check-circle text-green-500 text-3xl"></i>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Failed</p>
              <p className="text-2xl font-bold text-red-600">{stats.failed}</p>
            </div>
            <i className="fas fa-times-circle text-red-500 text-3xl"></i>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Running</p>
              <p className="text-2xl font-bold text-yellow-600">{stats.running}</p>
            </div>
            <i className="fas fa-spinner text-yellow-500 text-3xl"></i>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-lg font-semibold mb-4">Filters</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Status</label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Statuses</option>
              <option value="SUCCESS">Success</option>
              <option value="FAILED">Failed</option>
              <option value="RUNNING">Running</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Environment</label>
            <select
              value={environmentFilter}
              onChange={(e) => setEnvironmentFilter(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Environments</option>
              <option value="production">Production</option>
              <option value="staging">Staging</option>
              <option value="development">Development</option>
              <option value="test">Test</option>
            </select>
          </div>
          <div className="flex items-end">
            <button
              onClick={() => { setStatusFilter('ALL'); setEnvironmentFilter('ALL'); }}
              className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition"
            >
              <i className="fas fa-redo mr-2"></i>
              Reset Filters
            </button>
          </div>
        </div>
      </div>

      {/* Tests Table */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">Test History ({filteredTests.length})</h2>
          <Link
            href="/developer/punchout/new-test"
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            <i className="fas fa-plus mr-2"></i>
            New Test
          </Link>
        </div>

        {filteredTests.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Test Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Route
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Environment
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Tester
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Duration
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredTests.map((test) => (
                  <tr key={test.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="text-sm font-medium text-gray-900">{test.testName}</div>
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
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      {test.totalDuration ? (
                        <span className={test.totalDuration > 5000 ? 'text-orange-600 font-semibold' : 'text-gray-600'}>
                          {test.totalDuration}ms
                        </span>
                      ) : '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                        test.status === 'SUCCESS' ? 'bg-green-100 text-green-800' :
                        test.status === 'FAILED' ? 'bg-red-100 text-red-800' :
                        test.status === 'RUNNING' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        <i className={`fas fa-${
                          test.status === 'SUCCESS' ? 'check' :
                          test.status === 'FAILED' ? 'times' :
                          test.status === 'RUNNING' ? 'spinner fa-spin' :
                          'ban'
                        } mr-1`}></i>
                        {test.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <Link
                        href={`/developer/punchout/tests/${test.id}`}
                        className="text-blue-600 hover:text-blue-800 font-medium"
                      >
                        <i className="fas fa-eye mr-1"></i>
                        View Details
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-12">
            <i className="fas fa-search text-gray-300 text-5xl mb-4"></i>
            <p className="text-gray-500 text-lg">No tests found matching your filters</p>
            <button
              onClick={() => { setStatusFilter('ALL'); setEnvironmentFilter('ALL'); }}
              className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Reset Filters
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
