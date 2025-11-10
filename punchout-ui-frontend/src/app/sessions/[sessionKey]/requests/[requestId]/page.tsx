'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { networkRequestAPI } from '@/lib/api';
import { NetworkRequest } from '@/types';
import Breadcrumb from '@/components/Breadcrumb';
import RequestOverview from '@/components/networkRequest/RequestOverview';
import HeadersDisplay from '@/components/networkRequest/HeadersDisplay';
import BodyDisplay from '@/components/networkRequest/BodyDisplay';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import ErrorMessage from '@/components/ui/ErrorMessage';

export default function NetworkRequestDetailPage() {
  const params = useParams();
  const sessionKey = params.sessionKey as string;
  const requestId = params.requestId as string;

  const [request, setRequest] = useState<NetworkRequest | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (requestId) {
      loadRequestDetails();
    }
  }, [requestId]);

  const loadRequestDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await networkRequestAPI.getNetworkRequestById(requestId);
      setRequest(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load request details');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading request details..." />;
  }

  if (error || !request) {
    return (
      <ErrorMessage 
        message={error || 'Request not found'} 
        backLink={`/sessions/${sessionKey}`}
        backLabel="Back to Session"
      />
    );
  }

  const breadcrumbItems = [
    { label: 'PunchOut Sessions', href: '/sessions' },
    { label: sessionKey, href: `/sessions/${sessionKey}` },
    { label: 'Network Requests' },
    { label: request.requestId },
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Breadcrumb Navigation */}
      <Breadcrumb items={breadcrumbItems} />

      {/* Page Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2 flex items-center">
          <i className="fas fa-network-wired mr-3 text-purple-600"></i>
          Network Request Details
        </h1>
        <p className="text-gray-600 text-lg flex items-center">
          <span className={`px-3 py-1 rounded-full text-sm font-semibold mr-3 ${
            request.direction === 'INBOUND' 
              ? 'bg-blue-100 text-blue-800' 
              : 'bg-purple-100 text-purple-800'
          }`}>
            {request.direction}
          </span>
          {request.requestId}
        </p>
      </div>

      {/* Request Overview */}
      <RequestOverview request={request} />

      {/* Headers Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <HeadersDisplay 
          title="Request Headers" 
          headers={request.headers}
          icon="arrow-up"
        />
        <HeadersDisplay 
          title="Response Headers" 
          headers={request.responseHeaders}
          icon="arrow-down"
        />
      </div>

      {/* Body Display */}
      <BodyDisplay 
        requestBody={request.requestBody}
        responseBody={request.responseBody}
      />
    </div>
  );
}
