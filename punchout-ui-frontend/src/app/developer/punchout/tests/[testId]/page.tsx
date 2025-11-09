'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { punchOutTestAPI } from '@/lib/api';
import { PunchOutTest } from '@/types';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';

export default function TestDetailPage() {
  const params = useParams();
  const testId = params.testId as string;

  const [test, setTest] = useState<PunchOutTest | null>(null);
  const [activeTab, setActiveTab] = useState<'setup' | 'order'>('setup');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (testId) {
      loadTest();
    }
  }, [testId]);

  const loadTest = async () => {
    try {
      setLoading(true);
      const data = await punchOutTestAPI.getTestById(testId);
      setTest(data);
    } catch (err) {
      console.error('Failed to load test:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  };

  const formatDuration = (start?: string, end?: string) => {
    if (!start || !end) return '-';
    const duration = new Date(end).getTime() - new Date(start).getTime();
    return `${duration}ms`;
  };

  const formatBody = (body?: string) => {
    if (!body) return 'No data';
    return body;
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading test details...</p>
        </div>
      </div>
    );
  }

  if (!test) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <i className="fas fa-exclamation-circle text-red-500 text-5xl mb-4"></i>
          <h2 className="text-2xl font-bold mb-4">Test Not Found</h2>
          <Link href="/developer/punchout/past-tests" className="text-blue-600 hover:text-blue-800">
            ← Back to Past Tests
          </Link>
        </div>
      </div>
    );
  }

  const breadcrumbItems = [
    { label: 'Developer', href: '/developer' },
    { label: 'PunchOut Testing', href: '/developer/punchout' },
    { label: 'Past Tests', href: '/developer/punchout/past-tests' },
    { label: test.testName },
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      <Breadcrumb items={breadcrumbItems} />

      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2 flex items-center">
          <i className="fas fa-vial mr-3 text-purple-600"></i>
          Test Details
        </h1>
        <div className="flex items-center gap-3">
          <span className={`px-3 py-1 rounded-full text-sm font-semibold ${
            test.status === 'SUCCESS' ? 'bg-green-100 text-green-800' :
            test.status === 'FAILED' ? 'bg-red-100 text-red-800' :
            test.status === 'RUNNING' ? 'bg-yellow-100 text-yellow-800' :
            'bg-gray-100 text-gray-800'
          }`}>
            {test.status}
          </span>
          <span className="text-gray-600">{test.testName}</span>
        </div>
      </div>

      {/* Test Overview */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Test Overview</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div>
            <small className="text-gray-500 block">Catalog Route</small>
            <p className="font-medium">{test.catalogRouteName}</p>
          </div>
          <div>
            <small className="text-gray-500 block">Environment</small>
            <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
              {test.environment}
            </span>
          </div>
          <div>
            <small className="text-gray-500 block">Tester</small>
            <p className="font-medium">{test.tester}</p>
          </div>
          <div>
            <small className="text-gray-500 block">Test Date</small>
            <p className="font-medium">{formatDate(test.testDate)}</p>
          </div>
          <div>
            <small className="text-gray-500 block">Total Duration</small>
            <p className={`font-medium ${test.totalDuration && test.totalDuration > 5000 ? 'text-orange-600' : ''}`}>
              {test.totalDuration ? `${test.totalDuration}ms` : '-'}
            </p>
          </div>
          <div>
            <small className="text-gray-500 block">Catalog URL</small>
            <p className="font-medium text-sm break-all">
              {test.catalogUrl ? (
                <a href={test.catalogUrl} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:text-blue-800">
                  {test.catalogUrl}
                </a>
              ) : '-'}
            </p>
          </div>
          {test.errorMessage && (
            <div className="col-span-3 bg-red-50 border border-red-200 rounded-lg p-4">
              <small className="text-red-700 font-semibold block mb-1">Error Message</small>
              <p className="text-red-800">{test.errorMessage}</p>
            </div>
          )}
          {test.notes && (
            <div className="col-span-3 bg-blue-50 border border-blue-200 rounded-lg p-4">
              <small className="text-blue-700 font-semibold block mb-1">Notes</small>
              <p className="text-blue-800">{test.notes}</p>
            </div>
          )}
        </div>
      </div>

      {/* Timeline */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Test Timeline</h2>
        <div className="space-y-4">
          <TimelineItem
            icon="paper-plane"
            iconColor="blue"
            title="Setup Request Sent"
            time={formatDate(test.setupRequestSent)}
            detail={test.setupRequestSent ? 'PunchOut setup initiated' : 'Not executed'}
          />
          <TimelineItem
            icon="reply"
            iconColor="green"
            title="Setup Response Received"
            time={formatDate(test.setupResponseReceived)}
            detail={test.setupRequestSent && test.setupResponseReceived ? formatDuration(test.setupRequestSent, test.setupResponseReceived) : '-'}
          />
          <TimelineItem
            icon="shopping-cart"
            iconColor="purple"
            title="Order Message Sent"
            time={formatDate(test.orderMessageSent)}
            detail={test.orderMessageSent ? 'Order submitted' : 'Not executed'}
          />
          <TimelineItem
            icon="check-circle"
            iconColor="green"
            title="Order Response Received"
            time={formatDate(test.orderMessageReceived)}
            detail={test.orderMessageSent && test.orderMessageReceived ? formatDuration(test.orderMessageSent, test.orderMessageReceived) : '-'}
          />
        </div>
      </div>

      {/* Request/Response Data */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Request/Response Data</h2>
        
        {/* Tabs */}
        <div className="border-b border-gray-200 mb-4">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setActiveTab('setup')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'setup'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <i className="fas fa-cog mr-2"></i>
              Setup Request/Response
            </button>
            <button
              onClick={() => setActiveTab('order')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'order'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <i className="fas fa-shopping-cart mr-2"></i>
              Order Message/Response
            </button>
          </nav>
        </div>

        {/* Content */}
        {activeTab === 'setup' ? (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div>
              <h3 className="font-semibold mb-2 flex items-center">
                <i className="fas fa-arrow-up mr-2 text-blue-600"></i>
                Setup Request
              </h3>
              <pre className="bg-gray-50 p-4 rounded-lg overflow-x-auto text-xs border border-gray-200">
                <code>{formatBody(test.setupRequest)}</code>
              </pre>
            </div>
            <div>
              <h3 className="font-semibold mb-2 flex items-center">
                <i className="fas fa-arrow-down mr-2 text-green-600"></i>
                Setup Response
              </h3>
              <pre className="bg-gray-50 p-4 rounded-lg overflow-x-auto text-xs border border-gray-200">
                <code>{formatBody(test.setupResponse)}</code>
              </pre>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div>
              <h3 className="font-semibold mb-2 flex items-center">
                <i className="fas fa-arrow-up mr-2 text-purple-600"></i>
                Order Message
              </h3>
              <pre className="bg-gray-50 p-4 rounded-lg overflow-x-auto text-xs border border-gray-200">
                <code>{formatBody(test.orderMessage)}</code>
              </pre>
            </div>
            <div>
              <h3 className="font-semibold mb-2 flex items-center">
                <i className="fas fa-arrow-down mr-2 text-green-600"></i>
                Order Response
              </h3>
              <pre className="bg-gray-50 p-4 rounded-lg overflow-x-auto text-xs border border-gray-200">
                <code>{formatBody(test.orderResponse)}</code>
              </pre>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

interface TimelineItemProps {
  icon: string;
  iconColor: string;
  title: string;
  time: string;
  detail?: string;
}

function TimelineItem({ icon, iconColor, title, time, detail }: TimelineItemProps) {
  const colorClasses = {
    blue: 'bg-blue-100 text-blue-600',
    green: 'bg-green-100 text-green-600',
    purple: 'bg-purple-100 text-purple-600',
    orange: 'bg-orange-100 text-orange-600',
  };

  return (
    <div className="flex items-start">
      <div className={`flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center mr-4 ${colorClasses[iconColor as keyof typeof colorClasses]}`}>
        <i className={`fas fa-${icon}`}></i>
      </div>
      <div className="flex-grow border-l-2 border-gray-200 pl-4 pb-4">
        <h3 className="font-semibold text-gray-900">{title}</h3>
        <p className="text-sm text-gray-600">{time}</p>
        {detail && <p className="text-xs text-gray-500 mt-1">{detail}</p>}
      </div>
    </div>
  );
}
