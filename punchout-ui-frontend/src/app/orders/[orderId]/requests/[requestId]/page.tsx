'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { NetworkRequest } from '@/types';
import { networkRequestAPI } from '@/lib/api';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';

export default function OrderNetworkRequestDetailPage() {
  const params = useParams();
  const orderId = params.orderId as string;
  const requestId = params.requestId as string;
  
  const [request, setRequest] = useState<NetworkRequest | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadRequestDetails();
  }, [requestId]);

  const loadRequestDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await networkRequestAPI.getNetworkRequestById(requestId);
      setRequest(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load network request');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  const formatJson = (jsonString: string) => {
    try {
      const parsed = JSON.parse(jsonString);
      return JSON.stringify(parsed, null, 2);
    } catch (e) {
      return jsonString;
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
          <p className="mt-4 text-gray-600">Loading request details...</p>
        </div>
      </div>
    );
  }

  if (error || !request) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <p className="text-red-600 mb-4">Error: {error || 'Request not found'}</p>
          <Link
            href={`/orders/${orderId}`}
            className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
          >
            Back to Order
          </Link>
        </div>
      </div>
    );
  }

  const breadcrumbItems = [
    { label: 'Orders', href: '/orders' },
    { label: orderId, href: `/orders/${orderId}` },
    { label: 'Request Details' },
  ];

  return (
    <div>
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-green-600 to-teal-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-network-wired mr-3"></i>
              Network Request Details
            </h1>
            <p className="text-xl text-green-100">
              {request.requestType} - {request.direction}
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <Breadcrumb items={breadcrumbItems} />

        {/* Request Overview */}
        <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6 mt-6">
          <h2 className="text-xl font-semibold mb-4">
            <i className="fas fa-info-circle text-green-600 mr-2"></i>
            Request Overview
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-500">Request ID</label>
              <p className="text-gray-900 font-mono text-sm">{request.requestId || request.id}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Timestamp</label>
              <p className="text-gray-900">{formatDate(request.timestamp)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Direction</label>
              <p>
                <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                  request.direction === 'INBOUND' 
                    ? 'bg-blue-100 text-blue-800' 
                    : 'bg-purple-100 text-purple-800'
                }`}>
                  {request.direction}
                </span>
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Method</label>
              <p className="text-gray-900 font-semibold">{request.method}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Request Type</label>
              <p className="text-gray-900">{request.requestType}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Status Code</label>
              <p>
                <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                  request.success 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
                }`}>
                  {request.statusCode || '-'}
                </span>
              </p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Duration</label>
              <p className="text-gray-900 font-semibold">{request.duration ? `${request.duration}ms` : '-'}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Source</label>
              <p className="text-gray-900">{request.source}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Destination</label>
              <p className="text-gray-900">{request.destination}</p>
            </div>
            {request.url && (
              <div className="md:col-span-2 lg:col-span-3">
                <label className="text-sm font-medium text-gray-500">URL</label>
                <p className="text-gray-900 font-mono text-sm break-all">{request.url}</p>
              </div>
            )}
          </div>
        </div>

        {/* Headers */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6">
            <h2 className="text-xl font-semibold mb-4">
              <i className="fas fa-file-code text-blue-600 mr-2"></i>
              Request Headers
            </h2>
            <pre className="bg-gray-50 p-4 rounded-lg text-xs overflow-auto max-h-96 border border-gray-200">
              {request.headers ? formatJson(JSON.stringify(request.headers)) : 'No headers'}
            </pre>
          </div>

          <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6">
            <h2 className="text-xl font-semibold mb-4">
              <i className="fas fa-file-code text-purple-600 mr-2"></i>
              Response Headers
            </h2>
            <pre className="bg-gray-50 p-4 rounded-lg text-xs overflow-auto max-h-96 border border-gray-200">
              {request.responseHeaders ? formatJson(JSON.stringify(request.responseHeaders)) : 'No headers'}
            </pre>
          </div>
        </div>

        {/* Bodies */}
        <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">
            <i className="fas fa-file-alt text-green-600 mr-2"></i>
            Request Body
          </h2>
          <pre className="bg-gray-50 p-4 rounded-lg text-xs overflow-auto max-h-96 border border-gray-200">
            {request.requestBody || 'No request body'}
          </pre>
        </div>

        {request.responseBody && (
          <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4">
              <i className="fas fa-file-alt text-orange-600 mr-2"></i>
              Response Body
            </h2>
            <pre className="bg-gray-50 p-4 rounded-lg text-xs overflow-auto max-h-96 border border-gray-200">
              {request.responseBody}
            </pre>
          </div>
        )}

        {request.errorMessage && (
          <div className="bg-red-50 border border-red-200 rounded-xl shadow-lg p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4 text-red-800">
              <i className="fas fa-exclamation-triangle mr-2"></i>
              Error Message
            </h2>
            <p className="text-red-700">{request.errorMessage}</p>
          </div>
        )}

        {/* Back Button */}
        <div className="flex justify-center">
          <Link
            href={`/orders/${orderId}`}
            className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-all font-semibold"
          >
            <i className="fas fa-arrow-left mr-2"></i>
            Back to Order
          </Link>
        </div>
      </div>
    </div>
  );
}
