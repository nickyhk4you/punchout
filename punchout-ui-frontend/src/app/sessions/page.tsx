'use client';

import { useState, useEffect } from 'react';
import { sessionAPI } from '@/lib/api';
import { PunchOutSession, SessionFilter } from '@/types';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';
import Pagination from '@/components/Pagination';

export default function SessionsPage() {
  const [sessions, setSessions] = useState<PunchOutSession[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<SessionFilter>({});
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(25);

  useEffect(() => {
    loadSessions();
  }, [filters]);

  const loadSessions = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await sessionAPI.getAllSessions(filters);
      setSessions(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load sessions');
    } finally {
      setLoading(false);
    }
  };

  const toggleSort = () => {
    setSortOrder(prev => prev === 'asc' ? 'desc' : 'asc');
  };

  const sortedSessions = [...sessions].sort((a, b) => {
    const dateA = a.sessionDate ? new Date(a.sessionDate).getTime() : 0;
    const dateB = b.sessionDate ? new Date(b.sessionDate).getTime() : 0;
    return sortOrder === 'asc' ? dateA - dateB : dateB - dateA;
  });

  // Pagination
  const totalItems = sortedSessions.length;
  const totalPages = Math.ceil(totalItems / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedSessions = sortedSessions.slice(startIndex, endIndex);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleItemsPerPageChange = (newItemsPerPage: number) => {
    setItemsPerPage(newItemsPerPage);
    setCurrentPage(1);
  };

  const handleFilterChange = (key: keyof SessionFilter, value: string) => {
    setFilters(prev => ({
      ...prev,
      [key]: value || undefined,
    }));
  };

  const clearFilters = () => {
    setFilters({});
    setCurrentPage(1);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  };

  const formatCurrency = (value?: number) => {
    if (!value) return '-';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  };

  const breadcrumbItems = [
    { label: 'PunchOut Sessions' },
  ];

  return (
    <div>
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-list mr-3"></i>
              PunchOut Sessions
            </h1>
            <p className="text-xl text-blue-100">
              View, filter, and analyze all PunchOut sessions with network request logs
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <Breadcrumb items={breadcrumbItems} />

        {/* Filters */}
        <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6 mt-6">
          <h2 className="text-lg font-semibold mb-4">
            <i className="fas fa-filter text-blue-600 mr-2"></i>
            Filters
          </h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Operation
            </label>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={filters.operation || ''}
              onChange={(e) => handleFilterChange('operation', e.target.value)}
            >
              <option value="">All</option>
              <option value="CREATE">CREATE</option>
              <option value="EDIT">EDIT</option>
              <option value="INSPECT">INSPECT</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Environment
            </label>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={filters.environment || ''}
              onChange={(e) => handleFilterChange('environment', e.target.value)}
            >
              <option value="">All</option>
              <option value="PRODUCTION">PRODUCTION</option>
              <option value="STAGING">STAGING</option>
              <option value="DEVELOPMENT">DEVELOPMENT</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Route Name
            </label>
            <input
              type="text"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter route name"
              value={filters.routeName || ''}
              onChange={(e) => handleFilterChange('routeName', e.target.value)}
            />
          </div>

          <div className="flex items-end">
            <button
              onClick={clearFilters}
              className="w-full px-4 py-2 bg-gradient-to-r from-gray-200 to-gray-300 text-gray-700 rounded-lg hover:from-gray-300 hover:to-gray-400 transition-all font-semibold"
            >
              <i className="fas fa-times mr-2"></i>
              Clear Filters
            </button>
          </div>
        </div>
      </div>

      {/* Sessions Table */}
      <div className="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-4 border-blue-600"></div>
            <p className="mt-4 text-gray-600 text-lg">Loading sessions...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center text-red-600">
            <i className="fas fa-exclamation-circle text-4xl mb-4"></i>
            <p className="text-lg font-semibold">Error: {error}</p>
            <button
              onClick={loadSessions}
              className="mt-4 px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg hover:from-blue-700 hover:to-blue-800 font-semibold transition-all shadow-lg"
            >
              <i className="fas fa-redo mr-2"></i>
              Retry
            </button>
          </div>
        ) : sessions.length === 0 ? (
          <div className="p-12 text-center text-gray-600">
            <i className="fas fa-inbox text-gray-300 text-5xl mb-4"></i>
            <p className="text-lg">No sessions found</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-1/4">
                    Session Key
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-1/8">
                    Operation
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-1/8">
                    Environment
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-1/5">
                    Contact Email
                  </th>
                  <th 
                    className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100 select-none w-1/5"
                    onClick={toggleSort}
                  >
                    <div className="flex items-center space-x-1">
                      <span>Session Date</span>
                      <span className="text-gray-400">
                        {sortOrder === 'asc' ? '↑' : '↓'}
                      </span>
                    </div>
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-1/8">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {paginatedSessions.map((session) => (
                  <tr key={session.sessionKey} className="hover:bg-blue-50 transition-colors">
                    <td className="px-4 py-4 whitespace-nowrap">
                      <Link
                        href={`/sessions/${session.sessionKey}`}
                        className="text-sm font-semibold text-blue-600 hover:text-blue-900 hover:underline cursor-pointer"
                      >
                        {session.sessionKey}
                      </Link>
                    </td>
                    <td className="px-4 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        session.operation === 'CREATE' ? 'bg-green-100 text-green-800' :
                        session.operation === 'EDIT' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-blue-100 text-blue-800'
                      }`}>
                        {session.operation}
                      </span>
                    </td>
                    <td className="px-4 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        session.environment === 'PRODUCTION' ? 'bg-red-100 text-red-800' :
                        session.environment === 'STAGING' ? 'bg-orange-100 text-orange-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {session.environment}
                      </span>
                    </td>
                    <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-900">
                      {session.contactEmail || '-'}
                    </td>
                    <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(session.sessionDate)}
                    </td>
                    <td className="px-4 py-4 whitespace-nowrap text-sm font-medium">
                      <Link
                        href={`/sessions/${session.sessionKey}`}
                        className="inline-flex items-center px-3 py-1.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all font-semibold"
                      >
                        <i className="fas fa-eye mr-1"></i>
                        View
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        
        {/* Pagination */}
        {!loading && !error && sessions.length > 0 && (
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            totalItems={totalItems}
            itemsPerPage={itemsPerPage}
            onPageChange={handlePageChange}
            onItemsPerPageChange={handleItemsPerPageChange}
          />
        )}
      </div>
      </div>
    </div>
  );
}
