'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { sessionAPI, orderAPI, gatewayAPI, networkRequestAPI } from '@/lib/api';
import { PunchOutSession, OrderObject, GatewayRequest, NetworkRequest } from '@/types';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';

export default function SessionDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const sessionKey = params.sessionKey as string;

  const [session, setSession] = useState<PunchOutSession | null>(null);
  const [orderObject, setOrderObject] = useState<OrderObject | null>(null);
  const [gatewayRequests, setGatewayRequests] = useState<GatewayRequest[]>([]);
  const [networkRequests, setNetworkRequests] = useState<NetworkRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'all' | 'inbound' | 'outbound'>('all');

  useEffect(() => {
    if (sessionKey) {
      loadSessionDetails();
    }
  }, [sessionKey]);

  const loadSessionDetails = async () => {
    try {
      setLoading(true);
      setError(null);

      const [sessionData, orderData, gatewayData, networkData] = await Promise.all([
        sessionAPI.getSessionByKey(sessionKey),
        orderAPI.getOrderObject(sessionKey).catch(() => null),
        gatewayAPI.getGatewayRequests(sessionKey).catch(() => []),
        networkRequestAPI.getNetworkRequests(sessionKey).catch(() => []),
      ]);

      setSession(sessionData);
      setOrderObject(orderData);
      setGatewayRequests(gatewayData);
      setNetworkRequests(networkData);
    } catch (err: any) {
      setError(err.message || 'Failed to load session details');
    } finally {
      setLoading(false);
    }
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

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading session details...</p>
        </div>
      </div>
    );
  }

  if (error || !session) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <p className="text-red-600 mb-4">Error: {error || 'Session not found'}</p>
          <Link
            href="/sessions"
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Back to Sessions
          </Link>
        </div>
      </div>
    );
  }

  const breadcrumbItems = [
    { label: 'PunchOut Sessions', href: '/sessions' },
    { label: sessionKey },
  ];

  return (
    <div>
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-file-alt mr-3"></i>
              Session Details
            </h1>
            <p className="text-xl text-blue-100">
              {sessionKey}
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <Breadcrumb items={breadcrumbItems} />

        {/* Session Information */}
        <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6 mt-6">
          <h2 className="text-xl font-semibold mb-4">
            <i className="fas fa-info-circle text-blue-600 mr-2"></i>
            Session Information
          </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div>
            <label className="text-sm font-medium text-gray-500">Session Key</label>
            <p className="text-gray-900">{session.sessionKey}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Operation</label>
            <p>
              <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                session.operation === 'CREATE' ? 'bg-green-100 text-green-800' :
                session.operation === 'EDIT' ? 'bg-yellow-100 text-yellow-800' :
                'bg-blue-100 text-blue-800'
              }`}>
                {session.operation}
              </span>
            </p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Environment</label>
            <p>
              <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                session.environment === 'PRODUCTION' ? 'bg-red-100 text-red-800' :
                session.environment === 'STAGING' ? 'bg-orange-100 text-orange-800' :
                'bg-gray-100 text-gray-800'
              }`}>
                {session.environment}
              </span>
            </p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Contact Email</label>
            <p className="text-gray-900">{session.contactEmail || '-'}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Route Name</label>
            <p className="text-gray-900">{session.routeName || '-'}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Network</label>
            <p className="text-gray-900">{session.network || '-'}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Session Date</label>
            <p className="text-gray-900">{formatDate(session.sessionDate)}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Punched In</label>
            <p className="text-gray-900">{formatDate(session.punchedIn)}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Punched Out</label>
            <p className="text-gray-900">{formatDate(session.punchedOut)}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Order ID</label>
            <p className="text-gray-900">{session.orderId || '-'}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Order Value</label>
            <p className="text-gray-900 font-semibold">{formatCurrency(session.orderValue)}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Line Items</label>
            <p className="text-gray-900">{session.lineItems || '-'}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Item Quantity</label>
            <p className="text-gray-900">{session.itemQuantity || '-'}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Catalog</label>
            <p className="text-gray-900">{session.catalog || '-'}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-gray-500">Parser</label>
            <p className="text-gray-900">{session.parser || '-'}</p>
          </div>
          <div className="md:col-span-2">
            <label className="text-sm font-medium text-gray-500">Cart Return URL</label>
            <p className="text-gray-900 break-all">{session.cartReturn || '-'}</p>
          </div>
        </div>
      </div>

      {/* Order Object */}
      <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">
          <i className="fas fa-shopping-cart text-green-600 mr-2"></i>
          Order Object
        </h2>
        {orderObject ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-500">Type</label>
              <p className="text-gray-900">{orderObject.type || '-'}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Operation</label>
              <p className="text-gray-900">{orderObject.operation || '-'}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Mode</label>
              <p className="text-gray-900">{orderObject.mode || '-'}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">User Email</label>
              <p className="text-gray-900">{orderObject.userEmail || '-'}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Company Code</label>
              <p className="text-gray-900">{orderObject.companyCode || '-'}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">User Name</label>
              <p className="text-gray-900">
                {orderObject.userFirstName && orderObject.userLastName
                  ? `${orderObject.userFirstName} ${orderObject.userLastName}`
                  : '-'}
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">From Identity</label>
              <p className="text-gray-900">{orderObject.fromIdentity || '-'}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Sold To Lookup</label>
              <p className="text-gray-900">{orderObject.soldToLookup || '-'}</p>
            </div>
          </div>
        ) : (
          <p className="text-gray-500">No order object found for this session</p>
        )}
      </div>

      {/* Network Requests */}
      <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">
          <i className="fas fa-network-wired text-purple-600 mr-2"></i>
          Network Requests ({networkRequests.length})
        </h2>
        
        {/* Tabs */}
        <div className="mb-4 border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setActiveTab('all')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'all'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              All ({networkRequests.length})
            </button>
            <button
              onClick={() => setActiveTab('inbound')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'inbound'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Inbound ({networkRequests.filter(r => r.direction === 'INBOUND').length})
            </button>
            <button
              onClick={() => setActiveTab('outbound')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'outbound'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Outbound ({networkRequests.filter(r => r.direction === 'OUTBOUND').length})
            </button>
          </nav>
        </div>

        {/* Requests Table */}
        {networkRequests.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Time
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Direction
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Method
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Type
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Source → Destination
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Duration
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {networkRequests
                  .filter(req => activeTab === 'all' || req.direction === activeTab.toUpperCase())
                  .map((request) => (
                    <tr key={request.id} className="hover:bg-blue-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {new Date(request.timestamp).toLocaleTimeString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          request.direction === 'INBOUND' 
                            ? 'bg-blue-100 text-blue-800' 
                            : 'bg-purple-100 text-purple-800'
                        }`}>
                          {request.direction}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {request.method}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {request.requestType}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">
                        <div className="max-w-xs truncate">
                          {request.source} → {request.destination}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          request.success 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {request.statusCode || '-'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {request.duration ? `${request.duration}ms` : '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <Link
                          href={`/sessions/${sessionKey}/requests/${request.id}`}
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
          <p className="text-gray-500">No network requests found for this session</p>
        )}
      </div>

      {/* Gateway Requests */}
      <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6">
        <h2 className="text-xl font-semibold mb-4">
          <i className="fas fa-server text-orange-600 mr-2"></i>
          Gateway Requests ({gatewayRequests.length})
        </h2>
        {gatewayRequests.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date/Time
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    URI
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Open Link
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {gatewayRequests.map((request) => (
                  <tr key={request.id} className="hover:bg-blue-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {request.id}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {formatDate(request.datetime)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <code className="bg-gray-100 px-2 py-1 rounded">{request.uri}</code>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      {request.openLink ? (
                        <a
                          href={request.openLink}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-blue-600 hover:text-blue-800 break-all"
                        >
                          {request.openLink}
                        </a>
                      ) : (
                        '-'
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-gray-500">No gateway requests found for this session</p>
        )}
      </div>
      </div>
    </div>
  );
}
