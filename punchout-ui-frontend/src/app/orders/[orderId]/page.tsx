'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { Order, NetworkRequest } from '@/types';
import { orderAPIv2 } from '@/lib/api';
import Link from 'next/link';

export default function OrderDetailPage() {
  const params = useParams();
  const orderId = params.orderId as string;
  
  const [order, setOrder] = useState<Order | null>(null);
  const [networkRequests, setNetworkRequests] = useState<NetworkRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedRequest, setSelectedRequest] = useState<NetworkRequest | null>(null);

  useEffect(() => {
    loadOrderDetails();
  }, [orderId]);

  const loadOrderDetails = async () => {
    try {
      setLoading(true);
      const [orderData, requestsData] = await Promise.all([
        orderAPIv2.getOrderById(orderId),
        orderAPIv2.getOrderNetworkRequests(orderId)
      ]);
      setOrder(orderData);
      setNetworkRequests(requestsData);
    } catch (error) {
      console.error('Failed to load order details:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number, currency: string = 'USD') => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusBadge = (status: string) => {
    const statusColors: Record<string, string> = {
      'RECEIVED': 'bg-blue-100 text-blue-800',
      'CONFIRMED': 'bg-green-100 text-green-800',
      'PROCESSING': 'bg-yellow-100 text-yellow-800',
      'FAILED': 'bg-red-100 text-red-800'
    };
    
    return (
      <span className={`px-3 py-1 rounded-full text-sm font-medium ${statusColors[status] || 'bg-gray-100 text-gray-800'}`}>
        {status}
      </span>
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
          <p className="mt-4 text-gray-600">Loading order details...</p>
        </div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-xl text-gray-600">Order not found</p>
          <Link href="/orders" className="text-green-600 hover:text-green-800 mt-4 inline-block">
            ← Back to Orders
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50">
      <div className="bg-gradient-to-r from-green-600 to-teal-600 text-white py-12 px-6 shadow-lg">
        <div className="max-w-7xl mx-auto">
          <Link href="/orders" className="text-green-100 hover:text-white mb-4 inline-block">
            ← Back to Orders
          </Link>
          <h1 className="text-4xl font-bold mb-2">Order {order.orderId}</h1>
          <div className="flex items-center gap-4 text-green-100">
            <span>{order.customerName}</span>
            <span>·</span>
            <span>{formatDate(order.orderDate)}</span>
            <span>·</span>
            <span className="font-semibold">{formatCurrency(order.total, order.currency)}</span>
            <span>·</span>
            {getStatusBadge(order.status)}
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-xl font-semibold mb-4 text-gray-800">Order Information</h2>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600">Order ID:</span>
                <span className="font-medium">{order.orderId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Date:</span>
                <span className="font-medium">{formatDate(order.orderDate)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Type:</span>
                <span className="font-medium">{order.orderType}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Version:</span>
                <span className="font-medium">{order.orderVersion}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Status:</span>
                {getStatusBadge(order.status)}
              </div>
              {order.muleOrderId && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Mule Order ID:</span>
                  <span className="font-medium font-mono text-sm">{order.muleOrderId}</span>
                </div>
              )}
              {order.sessionKey && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Session Key:</span>
                  <Link href={`/sessions/${order.sessionKey}`} className="font-medium text-green-600 hover:text-green-800">
                    {order.sessionKey}
                  </Link>
                </div>
              )}
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-xl font-semibold mb-4 text-gray-800">Network Requests</h2>
            <div className="space-y-3">
              {networkRequests.length === 0 ? (
                <p className="text-gray-500 text-sm">No network requests found</p>
              ) : (
                networkRequests.map((req) => (
                  <div 
                    key={req.id}
                    onClick={() => setSelectedRequest(req)}
                    className="p-3 border border-gray-200 rounded-lg hover:border-green-500 cursor-pointer transition-colors"
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className={`text-xs font-medium ${req.direction === 'INBOUND' ? 'text-blue-600' : 'text-purple-600'}`}>
                        {req.direction === 'INBOUND' ? '📥 INBOUND' : '📤 OUTBOUND'}
                      </span>
                      <span className="text-xs text-gray-500">{req.duration}ms</span>
                    </div>
                    <div className="text-sm font-medium">{req.requestType}</div>
                    <div className="text-xs text-gray-500">{req.source} → {req.destination}</div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        {order.shipTo && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h2 className="text-xl font-semibold mb-4 text-gray-800">Shipping Information</h2>
              <div className="text-gray-700 space-y-1">
                <p className="font-medium">{order.shipTo.name}</p>
                <p>{order.shipTo.street}</p>
                <p>{order.shipTo.city}, {order.shipTo.state} {order.shipTo.postalCode}</p>
                <p>{order.shipTo.country}</p>
                {order.shipTo.email && <p className="mt-2 text-sm">Email: {order.shipTo.email}</p>}
                {order.shipTo.phone && <p className="text-sm">Phone: {order.shipTo.phone}</p>}
              </div>
            </div>

            {order.billTo && (
              <div className="bg-white rounded-xl shadow-lg p-6">
                <h2 className="text-xl font-semibold mb-4 text-gray-800">Billing Information</h2>
                <div className="text-gray-700 space-y-1">
                  <p className="font-medium">{order.billTo.name}</p>
                  <p>{order.billTo.street}</p>
                  <p>{order.billTo.city}, {order.billTo.state} {order.billTo.postalCode}</p>
                  <p>{order.billTo.country}</p>
                </div>
              </div>
            )}
          </div>
        )}

        <div className="bg-white rounded-xl shadow-lg overflow-hidden mb-6">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-xl font-semibold">Line Items ({order.items?.length || 0})</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Line</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Part Number</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Description</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Qty</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Unit Price</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Extended</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {order.items?.map((item) => (
                  <tr key={item.lineNumber} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">{item.lineNumber}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-mono">{item.supplierPartId}</td>
                    <td className="px-6 py-4 text-sm">{item.description}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right">{item.quantity}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-right">
                      {formatCurrency(item.unitPrice, item.currency)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-right">
                      {formatCurrency(item.extendedAmount, item.currency)}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot className="bg-gray-50">
                <tr>
                  <td colSpan={5} className="px-6 py-4 text-right font-medium">Subtotal:</td>
                  <td className="px-6 py-4 text-right font-medium">{formatCurrency(order.total, order.currency)}</td>
                </tr>
                {order.taxAmount && order.taxAmount > 0 && (
                  <tr>
                    <td colSpan={5} className="px-6 py-4 text-right font-medium">Tax:</td>
                    <td className="px-6 py-4 text-right font-medium">{formatCurrency(order.taxAmount, order.currency)}</td>
                  </tr>
                )}
                <tr className="text-lg">
                  <td colSpan={5} className="px-6 py-4 text-right font-bold">Total:</td>
                  <td className="px-6 py-4 text-right font-bold">
                    {formatCurrency(order.total + (order.taxAmount || 0), order.currency)}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>

        {order.extrinsics && Object.keys(order.extrinsics).length > 0 && (
          <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4 text-gray-800">Extrinsics</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {Object.entries(order.extrinsics).map(([key, value]) => (
                <div key={key} className="flex justify-between border-b border-gray-100 pb-2">
                  <span className="text-gray-600 font-medium">{key}:</span>
                  <span className="text-gray-900">{value}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {selectedRequest && (
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-xl font-semibold mb-4 text-gray-800">Network Request Details</h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div>
                  <span className={`text-sm font-medium ${selectedRequest.direction === 'INBOUND' ? 'text-blue-600' : 'text-purple-600'}`}>
                    {selectedRequest.direction}
                  </span>
                  <p className="text-lg font-semibold mt-1">{selectedRequest.requestType}</p>
                  <p className="text-sm text-gray-600">{selectedRequest.source} → {selectedRequest.destination}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-gray-600">Duration</p>
                  <p className="text-2xl font-bold text-green-600">{selectedRequest.duration}ms</p>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <div>
                  <h3 className="font-semibold mb-2">Request Headers</h3>
                  <pre className="bg-gray-50 p-4 rounded text-xs overflow-auto max-h-64">
                    {JSON.stringify(selectedRequest.headers, null, 2)}
                  </pre>
                </div>
                <div>
                  <h3 className="font-semibold mb-2">Response Headers</h3>
                  <pre className="bg-gray-50 p-4 rounded text-xs overflow-auto max-h-64">
                    {JSON.stringify(selectedRequest.responseHeaders, null, 2)}
                  </pre>
                </div>
              </div>

              <div>
                <h3 className="font-semibold mb-2">Request Body</h3>
                <pre className="bg-gray-50 p-4 rounded text-xs overflow-auto max-h-96">
                  {selectedRequest.requestBody}
                </pre>
              </div>

              {selectedRequest.responseBody && (
                <div>
                  <h3 className="font-semibold mb-2">Response Body</h3>
                  <pre className="bg-gray-50 p-4 rounded text-xs overflow-auto max-h-96">
                    {selectedRequest.responseBody}
                  </pre>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
