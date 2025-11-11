'use client';

import { useState, useEffect } from 'react';
import { sessionAPI, cxmlTemplateAPI } from '@/lib/api';
import { PunchOutSession, CxmlTemplate } from '@/types';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';

// Mock customer data - in real app, this would come from an API
const CUSTOMERS = [
  { id: 'CUST001', name: 'Acme Corporation', domain: 'acme.com', buyerId: 'buyer123' },
  { id: 'CUST002', name: 'TechCorp Industries', domain: 'techcorp.com', buyerId: 'buyer456' },
  { id: 'CUST003', name: 'Global Solutions Inc', domain: 'globalsolutions.com', buyerId: 'buyer789' },
  { id: 'CUST004', name: 'Enterprise Partners', domain: 'enterprise.com', buyerId: 'buyer321' },
  { id: 'CUST005', name: 'Innovation Labs', domain: 'innovationlabs.com', buyerId: 'buyer654' },
];

export default function DeveloperPunchOutPage() {
  const [sessions, setSessions] = useState<PunchOutSession[]>([]);
  const [selectedEnvironment, setSelectedEnvironment] = useState<string>('dev');
  const [loading, setLoading] = useState(false);
  const [executing, setExecuting] = useState<string | null>(null);
  const [testResult, setTestResult] = useState<any>(null);
  const [showPayloadModal, setShowPayloadModal] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<any>(null);
  const [cxmlPayload, setCxmlPayload] = useState<string>('');

  useEffect(() => {
    loadRecentSessions();
  }, []);

  const loadRecentSessions = async () => {
    try {
      setLoading(true);
      const data = await sessionAPI.getAllSessions({});
      setSessions(data.slice(0, 10));
    } catch (err: any) {
      console.error('Failed to load sessions:', err);
    } finally {
      setLoading(false);
    }
  };

  const generateCxmlPayload = async (customer: any, environment: string) => {
    const timestamp = new Date().toISOString();
    const sessionKey = `SESSION_${environment.toUpperCase()}_${customer.id}_${Date.now()}`;
    const payloadId = Math.floor(Math.random() * 1000000);
    
    // Try to fetch template from MongoDB
    let template: CxmlTemplate | null = null;
    
    try {
      // First try to get customer-specific template
      template = await cxmlTemplateAPI.getTemplateByEnvironmentAndCustomer(environment, customer.id);
      
      // If not found, get default template for environment
      if (!template) {
        template = await cxmlTemplateAPI.getDefaultTemplate(environment);
      }
    } catch (error) {
      console.error('Failed to fetch cXML template:', error);
    }
    
    // If template exists, replace placeholders
    if (template && template.cxmlTemplate) {
      return template.cxmlTemplate
        .replace(/\{\{PAYLOAD_ID\}\}/g, payloadId.toString())
        .replace(/\{\{TIMESTAMP\}\}/g, timestamp)
        .replace(/\{\{SESSION_KEY\}\}/g, sessionKey)
        .replace(/\{\{BUYER_ID\}\}/g, customer.buyerId)
        .replace(/\{\{DOMAIN\}\}/g, customer.domain)
        .replace(/\{\{CUSTOMER_NAME\}\}/g, customer.name);
    }
    
    // Fallback to hardcoded template if MongoDB template not found
    return `<?xml version="1.0" encoding="UTF-8"?>
<cXML payloadID="${payloadId}" timestamp="${timestamp}">
  <Header>
    <From>
      <Credential domain="NetworkID">
        <Identity>${customer.buyerId}</Identity>
      </Credential>
    </From>
    <To>
      <Credential domain="NetworkID">
        <Identity>supplier456</Identity>
      </Credential>
    </To>
    <Sender>
      <Credential domain="NetworkID">
        <Identity>${customer.domain}</Identity>
        <SharedSecret>secret123</SharedSecret>
      </Credential>
      <UserAgent>BuyerApp 1.0</UserAgent>
    </Sender>
  </Header>
  <Request>
    <PunchOutSetupRequest operation="create">
      <BuyerCookie>${sessionKey}</BuyerCookie>
      <Extrinsic name="User">developer@waters.com</Extrinsic>
      <Extrinsic name="Environment">${environment}</Extrinsic>
      <Extrinsic name="CustomerName">${customer.name}</Extrinsic>
      <BrowserFormPost>
        <URL>https://${customer.domain}/punchout/return</URL>
      </BrowserFormPost>
      <Contact role="buyer">
        <Name xml:lang="en">Developer Test</Name>
        <Email>developer@waters.com</Email>
      </Contact>
    </PunchOutSetupRequest>
  </Request>
</cXML>`;
  };

  const handlePunchOut = async (customer: any, useCustomPayload = false) => {
    setExecuting(customer.id);
    setTestResult(null);
    
    try {
      const gatewayUrl = 'http://localhost:9090/punchout/setup';
      const payload = useCustomPayload ? cxmlPayload : await generateCxmlPayload(customer, selectedEnvironment);
      
      const response = await fetch(gatewayUrl, {
        method: 'POST',
        headers:  {
          'Content-Type': 'text/xml',
        },
        body: payload,
      });

      const responseText = await response.text();
      
      const sessionKeyMatch = responseText.match(/<BuyerCookie>([^<]+)<\/BuyerCookie>/);
      const sessionKey = sessionKeyMatch ? sessionKeyMatch[1] : null;
      
      await new Promise(resolve => setTimeout(resolve, 1000));
      
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
        customer: customer.name,
        environment: selectedEnvironment,
        responseXml: responseText,
        networkRequests,
        timestamp: new Date().toISOString(),
      });
      
      await loadRecentSessions();
      
    } catch (err: any) {
      console.error('Failed to execute punchout:', err);
      setTestResult({
        success: false,
        error: err.message,
        timestamp: new Date().toISOString(),
      });
    } finally {
      setExecuting(null);
      setShowPayloadModal(false);
      setSelectedCustomer(null);
      setCxmlPayload('');
    }
  };

  const handleEditPayload = async (customer: any) => {
    const payload = await generateCxmlPayload(customer, selectedEnvironment);
    setCxmlPayload(payload);
    setSelectedCustomer(customer);
    setShowPayloadModal(true);
  };

  const handleExecuteWithPayload = async () => {
    if (selectedCustomer) {
      await handlePunchOut(selectedCustomer, true);
    }
  };

  const handleCloseModal = () => {
    setShowPayloadModal(false);
    setSelectedCustomer(null);
    setCxmlPayload('');
  };

  const breadcrumbItems = [
    { label: 'Developer', href: '/developer' },
    { label: 'PunchOut Testing' },
  ];

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString();
  };



  return (
    <div>
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-purple-600 to-blue-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-rocket mr-3"></i>
              Developer PunchOut Testing
            </h1>
            <p className="text-xl text-purple-100">
              Execute live PunchOut tests across DEV, STAGE, PROD, and S4-DEV environments
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <Breadcrumb items={breadcrumbItems} />

        {/* Environment Selector & Customers */}
        <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6 mt-6">
          <h2 className="text-xl font-semibold mb-4">
            <i className="fas fa-users text-purple-600 mr-2"></i>
            Select Customer & Environment
          </h2>
        
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">Environment</label>
          <div className="flex gap-2">
            {['dev', 'stage', 'prod', 's4-dev'].map(env => (
            <button
            key={env}
            onClick={() => {
            setSelectedEnvironment(env);
            setTestResult(null);
            }}
            className={`px-6 py-3 rounded-lg font-semibold transition-all transform hover:scale-105 ${
            selectedEnvironment === env
            ? 'bg-gradient-to-r from-purple-600 to-blue-600 text-white shadow-lg'
            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
            >
            {env.toUpperCase()}
            </button>
            ))}
          </div>
        </div>

        {/* Customers Table */}
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Customer Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Domain
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Buyer ID
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {CUSTOMERS.map((customer) => (
                <tr key={customer.id} className="hover:bg-purple-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="font-medium text-gray-900">{customer.name}</div>
                    <div className="text-xs text-gray-500">{customer.id}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                    {customer.domain}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <code className="text-xs bg-gray-100 px-2 py-1 rounded">{customer.buyerId}</code>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm space-x-2">
                    <button
                      onClick={() => handlePunchOut(customer, false)}
                      disabled={executing === customer.id}
                      className="inline-flex items-center px-4 py-2 bg-gradient-to-r from-purple-600 to-blue-600 text-white font-semibold rounded-lg hover:from-purple-700 hover:to-blue-700 disabled:bg-gray-300 transition-all shadow-md transform hover:scale-105"
                    >
                      {executing === customer.id ? (
                        <>
                          <div className="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                          PunchOut...
                        </>
                      ) : (
                        <>
                          <i className="fas fa-rocket mr-2"></i>
                          PunchOut
                        </>
                      )}
                    </button>
                    <button
                      onClick={() => handleEditPayload(customer)}
                      disabled={executing === customer.id}
                      className="text-purple-600 hover:text-purple-800 font-semibold disabled:text-gray-400"
                    >
                      <i className="fas fa-edit mr-1"></i>
                      Edit Payload
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Payload Editor Modal */}
      {showPayloadModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-2xl max-w-4xl w-full max-h-[90vh] flex flex-col">
            {/* Modal Header */}
            <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <h2 className="text-xl font-semibold">
                <i className="fas fa-code mr-2 text-blue-600"></i>
                Edit cXML Payload
              </h2>
              <button
                onClick={handleCloseModal}
                className="text-gray-400 hover:text-gray-600 transition"
              >
                <i className="fas fa-times text-xl"></i>
              </button>
            </div>

            {/* Modal Body */}
            <div className="px-6 py-4 flex-1 overflow-y-auto">
              {selectedCustomer && (
                <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded">
                  <div className="flex items-center gap-4 text-sm">
                    <div>
                      <span className="text-gray-600">Customer:</span>
                      <span className="font-medium ml-2">{selectedCustomer.name}</span>
                    </div>
                    <div>
                      <span className="text-gray-600">Environment:</span>
                      <span className="font-medium ml-2 uppercase">{selectedEnvironment}</span>
                    </div>
                  </div>
                </div>
              )}
              <textarea
                value={cxmlPayload}
                onChange={(e) => setCxmlPayload(e.target.value)}
                rows={25}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono text-sm"
                placeholder="cXML payload..."
              />
            </div>

            {/* Modal Footer */}
            <div className="px-6 py-4 border-t border-gray-200 flex gap-3 justify-end">
              <button
                onClick={handleCloseModal}
                className="px-6 py-2 bg-gray-100 text-gray-700 font-semibold rounded-lg hover:bg-gray-200 transition"
              >
                <i className="fas fa-times mr-2"></i>
                Cancel
              </button>
              <button
                onClick={handleExecuteWithPayload}
                disabled={executing === selectedCustomer?.id}
                className="px-6 py-2 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 disabled:bg-gray-300 transition"
              >
                {executing === selectedCustomer?.id ? (
                  <>
                    <div className="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    Executing...
                  </>
                ) : (
                  <>
                    <i className="fas fa-rocket mr-2"></i>
                    Execute PunchOut
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Test Results */}
      {testResult && (
        <div className={`rounded-xl shadow-lg p-6 mb-6 ${testResult.success ? 'bg-gradient-to-br from-green-50 to-green-100 border-2 border-green-300' : 'bg-gradient-to-br from-red-50 to-red-100 border-2 border-red-300'}`}>
          <h2 className="text-xl font-semibold mb-4">
            {testResult.success ? (
              <><i className="fas fa-check-circle mr-2 text-green-600"></i>PunchOut Successful</>
            ) : (
              <><i className="fas fa-times-circle mr-2 text-red-600"></i>PunchOut Failed</>
            )}
          </h2>
          
          <div className="space-y-4">
            {testResult.sessionKey && (
              <div className="bg-white rounded p-4">
                <h3 className="font-semibold mb-2">Session Information</h3>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="text-gray-600">Customer:</span>
                    <p className="font-medium">{testResult.customer}</p>
                  </div>
                  <div>
                    <span className="text-gray-600">Environment:</span>
                    <p className="font-medium uppercase">{testResult.environment}</p>
                  </div>
                  <div className="col-span-2">
                    <span className="text-gray-600">Session Key:</span>
                    <div className="flex items-center gap-2 mt-1">
                      <code className="bg-gray-100 px-3 py-1 rounded flex-1">{testResult.sessionKey}</code>
                      <Link
                        href={`/sessions/${testResult.sessionKey}`}
                        className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 font-medium"
                      >
                        <i className="fas fa-external-link-alt mr-1"></i>
                        View Dashboard
                      </Link>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {testResult.networkRequests && testResult.networkRequests.length > 0 && (
              <div className="bg-white rounded p-4">
                <h3 className="font-semibold mb-3">
                  Network Requests Logged ({testResult.networkRequests.length})
                </h3>
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
                          <span className="text-gray-600 ml-2 text-sm">{req.url || req.endpoint}</span>
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

            {testResult.error && (
              <div className="bg-white rounded p-4">
                <h3 className="font-semibold text-red-600 mb-2">Error</h3>
                <code className="text-sm text-red-600">{testResult.error}</code>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Recent Sessions */}
      <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6">
        <h2 className="text-xl font-semibold mb-4">
          <i className="fas fa-history text-blue-600 mr-2"></i>
          Recent PunchOut Sessions
        </h2>
        {sessions.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Session Key
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Operation
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Environment
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Contact
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {sessions.map((session) => (
                  <tr key={session.sessionKey} className="hover:bg-blue-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <code className="text-xs bg-gray-100 px-2 py-1 rounded">{session.sessionKey}</code>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                        {session.operation}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800 uppercase">
                        {session.environment}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600">
                      {session.contactEmail || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {formatDate(session.sessionDate)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
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
        ) : (
          <div className="text-center py-8">
            <i className="fas fa-rocket text-gray-300 text-4xl mb-3"></i>
            <p className="text-gray-500">No sessions yet. Click PunchOut on a customer to start!</p>
          </div>
        )}
      </div>
      </div>
    </div>
  );
}
