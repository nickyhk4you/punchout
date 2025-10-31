'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { sessionAPI, orderAPI, gatewayAPI } from '@/lib/api';
import { PunchOutSession, OrderObject, GatewayRequest } from '@/types';
import Link from 'next/link';

export default function SessionDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const sessionKey = params.sessionKey as string;

  const [session, setSession] = useState<PunchOutSession | null>(null);
  const [orderObject, setOrderObject] = useState<OrderObject | null>(null);
  const [gatewayRequests, setGatewayRequests] = useState<GatewayRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (sessionKey) {
      loadSessionDetails();
    }
  }, [sessionKey]);

  const loadSessionDetails = async () => {
    try {
      setLoading(true);
      setError(null);

      const [sessionData, orderData, gatewayData] = await Promise.all([
        sessionAPI.getSessionByKey(sessionKey),
        orderAPI.getOrderObject(sessionKey),
        gatewayAPI.getGatewayRequests(sessionKey),
      ]);

      setSession(sessionData);
      setOrderObject(orderData);
      setGatewayRequests(gatewayData);
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

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6">
        <Link
          href="/sessions"
          className="text-blue-600 hover:text-blue-800 mb-2 inline-block"
        >
          ← Back to Sessions
        </Link>
        <h1 className="text-3xl font-bold mb-2">Session Details</h1>
        <p className="text-gray-600">{sessionKey}</p>
      </div>

      {/* Session Information */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Session Information</h2>
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
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Order Object</h2>
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

      {/* Gateway Requests */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Gateway Requests ({gatewayRequests.length})</h2>
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
                  <tr key={request.id} className="hover:bg-gray-50">
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
  );
}
