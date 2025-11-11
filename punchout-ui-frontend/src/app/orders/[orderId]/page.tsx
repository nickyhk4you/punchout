'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { Order, NetworkRequest } from '@/types';
import { orderAPIv2 } from '@/lib/api';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';

export default function OrderDetailPage() {
  const params = useParams();
  const orderId = params.orderId as string;
  
  const [order, setOrder] = useState<Order | null>(null);
  const [networkRequests, setNetworkRequests] = useState<NetworkRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'all' | 'inbound' | 'outbound'>('all');

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
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <p className="text-red-600 mb-4">Error: Order not found</p>
          <Link
            href="/orders"
            className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
          >
            Back to Orders
          </Link>
        </div>
      </div>
    );
  }

  const breadcrumbItems = [
    { label: 'Orders', href: '/orders' },
    { label: orderId },
  ];

  return (
    <div>
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-green-600 to-teal-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-shopping-cart mr-3"></i>
              Order Details
            </h1>
            <p className="text-xl text-green-100">
              {orderId}
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <Breadcrumb items={breadcrumbItems} />
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
            <h2 className="text-xl font-semibold mb-4">
              <i className="fas fa-info-circle text-green-600 mr-2"></i>
              Order Summary
            </h2>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600">Customer:</span>
                <span className="font-medium">{order.customerName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Total:</span>
                <span className="font-semibold text-green-600">{formatCurrency(order.total, order.currency)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Tax:</span>
                <span className="font-medium">{formatCurrency(order.taxAmount || 0, order.currency)}</span>
              </div>
              <div className="flex justify-between border-t pt-3">
                <span className="text-gray-600 font-semibold">Grand Total:</span>
                <span className="font-bold text-lg text-green-700">{formatCurrency(order.total + (order.taxAmount || 0), order.currency)}</span>
              </div>
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
                    ? 'border-green-500 text-green-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                All ({networkRequests.length})
              </button>
              <button
                onClick={() => setActiveTab('inbound')}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'inbound'
                    ? 'border-green-500 text-green-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Inbound ({networkRequests.filter(r => r.direction === 'INBOUND').length})
              </button>
              <button
                onClick={() => setActiveTab('outbound')}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'outbound'
                    ? 'border-green-500 text-green-600'
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
                      <tr key={request.id} className="hover:bg-green-50 transition-colors">
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
                            href={`/orders/${orderId}/requests/${request.id}`}
                            className="inline-flex items-center px-3 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-all font-semibold"
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
            <p className="text-gray-500">No network requests found for this order</p>
          )}
        </div>

        {order.extrinsics && Object.keys(order.extrinsics).length > 0 && (
          <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4">
              <i className="fas fa-tags text-orange-600 mr-2"></i>
              Extrinsics
            </h2>
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
      </div>
    </div>
  );
}
