'use client';

import { useState } from 'react';
import Link from 'next/link';
import { NetworkRequest } from '@/types';

interface NetworkRequestsTableProps {
  requests: NetworkRequest[];
  sessionKey: string;
}

type TabType = 'all' | 'inbound' | 'outbound';

export default function NetworkRequestsTable({ requests, sessionKey }: NetworkRequestsTableProps) {
  const [activeTab, setActiveTab] = useState<TabType>('all');

  const filteredRequests = requests.filter(req => 
    activeTab === 'all' || req.direction === activeTab.toUpperCase()
  );

  const inboundCount = requests.filter(r => r.direction === 'INBOUND').length;
  const outboundCount = requests.filter(r => r.direction === 'OUTBOUND').length;

  return (
    <div className="card shadow-sm mb-4">
      <div className="card-body">
        <h5 className="card-title d-flex align-items-center mb-4">
          <i className="fas fa-network-wired me-2 text-primary"></i>
          Network Requests ({requests.length})
        </h5>
        
        {/* Tabs */}
        <ul className="nav nav-tabs mb-3">
          <li className="nav-item">
            <button
              className={`nav-link ${activeTab === 'all' ? 'active' : ''}`}
              onClick={() => setActiveTab('all')}
            >
              All ({requests.length})
            </button>
          </li>
          <li className="nav-item">
            <button
              className={`nav-link ${activeTab === 'inbound' ? 'active' : ''}`}
              onClick={() => setActiveTab('inbound')}
            >
              <i className="fas fa-arrow-down me-1"></i>
              Inbound ({inboundCount})
            </button>
          </li>
          <li className="nav-item">
            <button
              className={`nav-link ${activeTab === 'outbound' ? 'active' : ''}`}
              onClick={() => setActiveTab('outbound')}
            >
              <i className="fas fa-arrow-up me-1"></i>
              Outbound ({outboundCount})
            </button>
          </li>
        </ul>

        {/* Requests Table */}
        {filteredRequests.length > 0 ? (
          <div className="table-responsive">
            <table className="table table-hover">
              <thead className="table-light">
                <tr>
                  <th>Time</th>
                  <th>Direction</th>
                  <th>Method</th>
                  <th>Type</th>
                  <th>Source → Destination</th>
                  <th>Status</th>
                  <th>Duration</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredRequests.map((request) => (
                  <tr key={request.id}>
                    <td className="text-nowrap">
                      <small>{new Date(request.timestamp).toLocaleTimeString()}</small>
                    </td>
                    <td>
                      <span className={`badge ${
                        request.direction === 'INBOUND' 
                          ? 'bg-info' 
                          : 'bg-secondary'
                      }`}>
                        <i className={`fas fa-arrow-${request.direction === 'INBOUND' ? 'down' : 'up'} me-1`}></i>
                        {request.direction}
                      </span>
                    </td>
                    <td>
                      <code className="text-dark">{request.method}</code>
                    </td>
                    <td>
                      <span className="badge bg-light text-dark border">{request.requestType}</span>
                    </td>
                    <td>
                      <small className="text-truncate d-inline-block" style={{maxWidth: '300px'}}>
                        {request.source} <i className="fas fa-arrow-right mx-1 text-muted"></i> {request.destination}
                      </small>
                    </td>
                    <td>
                      <span className={`badge ${
                        request.success 
                          ? 'bg-success' 
                          : 'bg-danger'
                      }`}>
                        {request.statusCode || '-'}
                      </span>
                    </td>
                    <td>
                      {request.duration ? (
                        <span className={request.duration > 1000 ? 'text-warning fw-bold' : ''}>
                          <small>{request.duration}ms</small>
                        </span>
                      ) : '-'}
                    </td>
                    <td>
                      <Link
                        href={`/sessions/${sessionKey}/requests/${request.id}`}
                        className="btn btn-sm btn-outline-primary"
                      >
                        <i className="fas fa-eye me-1"></i>
                        View
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-5">
            <i className="fas fa-inbox text-muted fs-1 mb-3 d-block"></i>
            <p className="text-muted">No network requests found for this session</p>
          </div>
        )}
      </div>
    </div>
  );
}
