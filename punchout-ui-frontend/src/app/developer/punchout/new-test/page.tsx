'use client';

import { useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { catalogRouteAPI, punchOutTestAPI } from '@/lib/api';
import { CatalogRoute, EnvironmentConfig } from '@/types';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';

export default function NewTestPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const routeId = searchParams.get('routeId');
  const env = searchParams.get('env') || 'production';

  const [catalogRoute, setCatalogRoute] = useState<CatalogRoute | null>(null);
  const [selectedEnvironment, setSelectedEnvironment] = useState<string>(env);
  const [testName, setTestName] = useState('');
  const [testerEmail, setTesterEmail] = useState('');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [executing, setExecuting] = useState(false);

  useEffect(() => {
    if (routeId) {
      loadCatalogRoute();
    } else {
      setLoading(false);
    }
  }, [routeId]);

  const loadCatalogRoute = async () => {
    try {
      const route = await catalogRouteAPI.getRouteById(routeId!);
      setCatalogRoute(route);
      setTestName(`${route.routeName} - ${selectedEnvironment} - ${new Date().toLocaleDateString()}`);
    } catch (err) {
      console.error('Failed to load catalog route:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStartTest = async () => {
    if (!catalogRoute || !testerEmail) return;

    setExecuting(true);
    try {
      const test = await punchOutTestAPI.createTest({
        testName,
        catalogRouteId: catalogRoute.id,
        catalogRouteName: catalogRoute.routeName,
        environment: selectedEnvironment,
        tester: testerEmail,
        testDate: new Date().toISOString(),
        status: 'RUNNING',
        notes,
      });

      // Redirect to test execution page
      router.push(`/developer/punchout/tests/${test.id}`);
    } catch (err) {
      console.error('Failed to start test:', err);
      setExecuting(false);
    }
  };

  const breadcrumbItems = [
    { label: 'Developer', href: '/developer' },
    { label: 'PunchOut Testing', href: '/developer/punchout' },
    { label: 'New Test' },
  ];

  const selectedEnvConfig = catalogRoute?.environments.find(
    e => e.environment === selectedEnvironment
  );

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

  if (!catalogRoute) {
    return (
      <div className="container mx-auto px-4 py-8">
        <Breadcrumb items={breadcrumbItems} />
        <div className="text-center py-12">
          <i className="fas fa-exclamation-circle text-red-500 text-5xl mb-4"></i>
          <h2 className="text-2xl font-bold mb-4">No Catalog Route Selected</h2>
          <Link href="/developer/punchout" className="text-blue-600 hover:text-blue-800">
            ← Back to PunchOut Testing
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <Breadcrumb items={breadcrumbItems} />

      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">
          <i className="fas fa-flask mr-2 text-purple-600"></i>
          New PunchOut Test
        </h1>
        <p className="text-gray-600">Configure and execute a manual PunchOut test</p>
      </div>

      {/* Test Configuration */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Test Configuration</h2>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          {/* Route Information */}
          <div className="col-span-2 bg-blue-50 border border-blue-200 rounded-lg p-4">
            <h3 className="font-semibold mb-2 text-blue-900">Selected Route</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <small className="text-blue-700">Route Name:</small>
                <p className="font-medium">{catalogRoute.routeName}</p>
              </div>
              <div>
                <small className="text-blue-700">Domain:</small>
                <p className="font-medium">{catalogRoute.domain}</p>
              </div>
              <div>
                <small className="text-blue-700">Network:</small>
                <p className="font-medium">{catalogRoute.network}</p>
              </div>
              <div>
                <small className="text-blue-700">Type:</small>
                <p className="font-medium">{catalogRoute.type.toUpperCase()}</p>
              </div>
            </div>
          </div>

          {/* Environment Selection */}
          <div className="col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Environment *
            </label>
            <div className="grid grid-cols-4 gap-2">
              {catalogRoute.environments.map((envConfig) => (
                <button
                  key={envConfig.environment}
                  onClick={() => setSelectedEnvironment(envConfig.environment)}
                  disabled={!envConfig.enabled}
                  className={`px-4 py-3 rounded-lg font-medium transition ${
                    selectedEnvironment === envConfig.environment
                      ? 'bg-blue-600 text-white shadow-lg'
                      : envConfig.enabled
                      ? 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      : 'bg-gray-50 text-gray-400 cursor-not-allowed'
                  }`}
                >
                  <div className="text-xs uppercase">{envConfig.environment}</div>
                  {!envConfig.enabled && <div className="text-xs">(Disabled)</div>}
                </button>
              ))}
            </div>
            {selectedEnvConfig && (
              <div className="mt-2 text-sm text-gray-600 bg-gray-50 p-2 rounded">
                <i className="fas fa-link mr-1"></i>
                URL: <code>{selectedEnvConfig.url}</code>
              </div>
            )}
          </div>

          {/* Test Name */}
          <div className="col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Test Name *
            </label>
            <input
              type="text"
              value={testName}
              onChange={(e) => setTestName(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter a descriptive test name"
            />
          </div>

          {/* Tester Email */}
          <div className="col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Tester Email *
            </label>
            <input
              type="email"
              value={testerEmail}
              onChange={(e) => setTesterEmail(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="your.email@waters.com"
            />
          </div>

          {/* Notes */}
          <div className="col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Test Notes (Optional)
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={3}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Add any notes about this test..."
            />
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-4 pt-4 border-t">
          <button
            onClick={handleStartTest}
            disabled={!testName || !testerEmail || !selectedEnvConfig?.enabled || executing}
            className="px-6 py-3 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition flex items-center"
          >
            {executing ? (
              <>
                <div className="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                Starting Test...
              </>
            ) : (
              <>
                <i className="fas fa-play mr-2"></i>
                Start PunchOut Test
              </>
            )}
          </button>
          <Link
            href="/developer/punchout"
            className="px-6 py-3 bg-gray-100 text-gray-700 font-semibold rounded-lg hover:bg-gray-200 transition"
          >
            <i className="fas fa-times mr-2"></i>
            Cancel
          </Link>
        </div>
      </div>

      {/* Test Process Information */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">
          <i className="fas fa-info-circle mr-2 text-blue-600"></i>
          Test Process
        </h2>
        <div className="space-y-4">
          <div className="flex items-start">
            <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3">
              <span className="text-blue-600 font-bold">1</span>
            </div>
            <div>
              <h3 className="font-semibold">PunchOut Setup Request</h3>
              <p className="text-sm text-gray-600">Send setup request to {catalogRoute.network} with buyer credentials</p>
            </div>
          </div>
          <div className="flex items-start">
            <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3">
              <span className="text-blue-600 font-bold">2</span>
            </div>
            <div>
              <h3 className="font-semibold">Catalog Access</h3>
              <p className="text-sm text-gray-600">Receive catalog URL and verify access</p>
            </div>
          </div>
          <div className="flex items-start">
            <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3">
              <span className="text-blue-600 font-bold">3</span>
            </div>
            <div>
              <h3 className="font-semibold">Order Message</h3>
              <p className="text-sm text-gray-600">Send test order back to buyer system</p>
            </div>
          </div>
          <div className="flex items-start">
            <div className="flex-shrink-0 w-8 h-8 bg-green-100 rounded-full flex items-center justify-center mr-3">
              <i className="fas fa-check text-green-600"></i>
            </div>
            <div>
              <h3 className="font-semibold">Verification</h3>
              <p className="text-sm text-gray-600">Validate responses and record results</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
