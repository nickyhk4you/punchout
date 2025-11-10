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
  const [cxmlPayload, setCxmlPayload] = useState('');
  const [showPayloadEditor, setShowPayloadEditor] = useState(false);
  const [testResult, setTestResult] = useState<any>(null);

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
      
      // Initialize default cXML payload
      const defaultPayload = generateDefaultCxmlPayload(route, selectedEnvironment);
      setCxmlPayload(defaultPayload);
    } catch (err) {
      console.error('Failed to load catalog route:', err);
    } finally {
      setLoading(false);
    }
  };

  const generateDefaultCxmlPayload = (route: CatalogRoute, environment: string) => {
    const timestamp = new Date().toISOString();
    const sessionKey = `SESSION_TEST_${Date.now()}`;
    
    return `<?xml version="1.0" encoding="UTF-8"?>
<cXML payloadID="${Math.floor(Math.random() * 1000000)}" timestamp="${timestamp}">
  <Header>
    <From>
      <Credential domain="NetworkID">
        <Identity>buyer123</Identity>
      </Credential>
    </From>
    <To>
      <Credential domain="NetworkID">
        <Identity>supplier456</Identity>
      </Credential>
    </To>
    <Sender>
      <Credential domain="NetworkID">
        <Identity>buyerApp</Identity>
        <SharedSecret>secret123</SharedSecret>
      </Credential>
      <UserAgent>BuyerApp 1.0</UserAgent>
    </Sender>
  </Header>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>${sessionKey}</BuyerCookie>
      <Extrinsic name="User">${testerEmail || 'test@waters.com'}</Extrinsic>
      <Extrinsic name="Environment">${environment}</Extrinsic>
      <Extrinsic name="RouteName">${route.routeName}</Extrinsic>
      <BrowserFormPost>
        <URL>https://buyer.example.com/punchout/return</URL>
      </BrowserFormPost>
      <Contact role="buyer">
        <Name xml:lang="en">Test User</Name>
        <Email>${testerEmail || 'test@waters.com'}</Email>
      </Contact>
    </PunchOutSetupRequest>
  </Request>
</cXML>`;
  };

  const handleStartTest = async () => {
    if (!catalogRoute || !testerEmail) return;

    setExecuting(true);
    setTestResult(null);
    
    try {
      // Send cXML to Gateway
      const gatewayUrl = 'http://localhost:9090/punchout/setup';
      
      const response = await fetch(gatewayUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'text/xml',
        },
        body: cxmlPayload,
      });

      const responseText = await response.text();
      
      // Extract session key from response
      const sessionKeyMatch = responseText.match(/<BuyerCookie>([^<]+)<\/BuyerCookie>/);
      const sessionKey = sessionKeyMatch ? sessionKeyMatch[1] : null;
      
      // Wait 1 second for MongoDB to save
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Fetch network requests
      let networkRequests = [];
      if (sessionKey) {
        try {
          const requestsResponse = await fetch(`http://localhost:8080/api/v1/sessions/${sessionKey}/network-requests`);
          networkRequests = await requestsResponse.json();
        } catch (err) {
          console.error('Failed to fetch network requests:', err);
        }
      }
      
      setTestResult({
        success: response.ok,
        status: response.status,
        sessionKey,
        responseXml: responseText,
        networkRequests,
        timestamp: new Date().toISOString(),
      });
      
      // Save test to MongoDB
      await punchOutTestAPI.createTest({
        testName,
        catalogRouteId: catalogRoute.id,
        catalogRouteName: catalogRoute.routeName,
        environment: selectedEnvironment,
        tester: testerEmail,
        testDate: new Date().toISOString(),
        status: response.ok ? 'SUCCESS' : 'FAILED',
        notes,
        sessionKey,
      });
      
    } catch (err: any) {
      console.error('Failed to execute test:', err);
      setTestResult({
        success: false,
        error: err.message,
        timestamp: new Date().toISOString(),
      });
    } finally {
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
    <div>
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-purple-600 to-pink-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-flask mr-3"></i>
              New PunchOut Test
            </h1>
            <p className="text-xl text-purple-100">
              Configure and execute a manual PunchOut test with customizable payload
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <Breadcrumb items={breadcrumbItems} />

        {/* Test Configuration */}
        <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6 mt-6">
          <h2 className="text-xl font-semibold mb-4">
            <i className="fas fa-cog text-purple-600 mr-2"></i>
            Test Configuration
          </h2>
        
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
                  className={`px-4 py-3 rounded-lg font-semibold transition-all transform hover:scale-105 ${
                    selectedEnvironment === envConfig.environment
                      ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-lg'
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

          {/* cXML Payload Editor */}
          <div className="col-span-2">
            <div className="flex items-center justify-between mb-2">
              <label className="block text-sm font-medium text-gray-700">
                cXML Request Payload
              </label>
              <button
                onClick={() => setShowPayloadEditor(!showPayloadEditor)}
                className="text-sm text-blue-600 hover:text-blue-800"
              >
                {showPayloadEditor ? 'Hide' : 'Edit'} Payload
              </button>
            </div>
            {showPayloadEditor && (
              <textarea
                value={cxmlPayload}
                onChange={(e) => setCxmlPayload(e.target.value)}
                rows={15}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono text-sm"
                placeholder="cXML payload..."
              />
            )}
            {!showPayloadEditor && (
              <div className="text-sm text-gray-500 bg-gray-50 p-3 rounded border">
                Click "Edit Payload" to customize the cXML request
              </div>
            )}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-4 pt-4 border-t">
          <button
            onClick={handleStartTest}
            disabled={!testName || !testerEmail || !selectedEnvConfig?.enabled || executing}
            className="px-6 py-3 bg-gradient-to-r from-green-600 to-green-700 text-white font-semibold rounded-lg hover:from-green-700 hover:to-green-800 disabled:bg-gray-300 disabled:cursor-not-allowed transition-all shadow-lg transform hover:scale-105 flex items-center"
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
            className="px-6 py-3 bg-gradient-to-r from-gray-200 to-gray-300 text-gray-700 font-semibold rounded-lg hover:from-gray-300 hover:to-gray-400 transition-all"
          >
            <i className="fas fa-times mr-2"></i>
            Cancel
          </Link>
        </div>
      </div>

      {/* Test Results */}
      {testResult && (
        <div className={`rounded-xl shadow-lg p-6 mb-6 ${testResult.success ? 'bg-gradient-to-br from-green-50 to-green-100 border-2 border-green-300' : 'bg-gradient-to-br from-red-50 to-red-100 border-2 border-red-300'}`}>
          <h2 className="text-xl font-semibold mb-4">
            {testResult.success ? (
              <><i className="fas fa-check-circle mr-2 text-green-600"></i>Test Completed Successfully</>
            ) : (
              <><i className="fas fa-times-circle mr-2 text-red-600"></i>Test Failed</>
            )}
          </h2>
          
          <div className="space-y-4">
            {/* Session Key */}
            {testResult.sessionKey && (
              <div className="bg-white rounded p-4">
                <h3 className="font-semibold mb-2">Session Created</h3>
                <div className="flex items-center gap-2">
                  <code className="bg-gray-100 px-3 py-1 rounded">{testResult.sessionKey}</code>
                  <Link
                    href={`/sessions/${testResult.sessionKey}`}
                    className="text-blue-600 hover:text-blue-800 font-medium"
                  >
                    <i className="fas fa-external-link-alt mr-1"></i>
                    View Session Dashboard
                  </Link>
                </div>
              </div>
            )}

            {/* Network Requests */}
            {testResult.networkRequests && testResult.networkRequests.length > 0 && (
              <div className="bg-white rounded p-4">
                <h3 className="font-semibold mb-3">Network Requests Logged ({testResult.networkRequests.length})</h3>
                <div className="space-y-2">
                  {testResult.networkRequests.map((req: any, index: number) => (
                    <div key={index} className="border-l-4 border-blue-500 pl-3 py-2 bg-gray-50">
                      <div className="flex items-center justify-between">
                        <div>
                          <span className={`px-2 py-1 text-xs font-semibold rounded mr-2 ${
                            req.direction === 'INBOUND' ? 'bg-green-100 text-green-800' : 'bg-blue-100 text-blue-800'
                          }`}>
                            {req.direction}
                          </span>
                          <span className="font-medium">{req.method}</span>
                          <span className="text-gray-600 ml-2">{req.url || req.endpoint}</span>
                        </div>
                        <span className={`px-2 py-1 text-xs font-semibold rounded ${
                          req.statusCode === 200 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                        }`}>
                          {req.statusCode}
                        </span>
                      </div>
                      <div className="text-xs text-gray-500 mt-1">
                        {req.source} → {req.destination} | {req.duration}ms
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Error Message */}
            {testResult.error && (
              <div className="bg-white rounded p-4">
                <h3 className="font-semibold text-red-600 mb-2">Error</h3>
                <code className="text-sm text-red-600">{testResult.error}</code>
              </div>
            )}

            {/* Response XML */}
            {testResult.responseXml && (
              <div className="bg-white rounded p-4">
                <h3 className="font-semibold mb-2">Gateway Response</h3>
                <pre className="bg-gray-100 p-3 rounded text-xs overflow-x-auto">
                  {testResult.responseXml}
                </pre>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Test Process Information */}
      <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6">
        <h2 className="text-xl font-semibold mb-4">
          <i className="fas fa-info-circle mr-2 text-purple-600"></i>
          Test Process
        </h2>
        <div className="space-y-4">
          <div className="flex items-start">
            <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3">
              <span className="text-blue-600 font-bold">1</span>
            </div>
            <div>
              <h3 className="font-semibold">PunchOut Setup Request</h3>
              <p className="text-sm text-gray-600">Send cXML setup request to Gateway with customizable payload</p>
            </div>
          </div>
          <div className="flex items-start">
            <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3">
              <span className="text-blue-600 font-bold">2</span>
            </div>
            <div>
              <h3 className="font-semibold">Gateway Processing</h3>
              <p className="text-sm text-gray-600">Gateway authenticates and calls Mock Service to get catalog URL</p>
            </div>
          </div>
          <div className="flex items-start">
            <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3">
              <span className="text-blue-600 font-bold">3</span>
            </div>
            <div>
              <h3 className="font-semibold">Network Logging</h3>
              <p className="text-sm text-gray-600">All INBOUND and OUTBOUND requests are logged to MongoDB</p>
            </div>
          </div>
          <div className="flex items-start">
            <div className="flex-shrink-0 w-8 h-8 bg-green-100 rounded-full flex items-center justify-center mr-3">
              <i className="fas fa-check text-green-600"></i>
            </div>
            <div>
              <h3 className="font-semibold">View Dashboard</h3>
              <p className="text-sm text-gray-600">See all network requests with payloads in the UI Dashboard</p>
            </div>
          </div>
        </div>
      </div>
      </div>
    </div>
  );
}
