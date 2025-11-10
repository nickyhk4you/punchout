import { NetworkRequest } from '@/types';

interface RequestOverviewProps {
  request: NetworkRequest;
}

export default function RequestOverview({ request }: RequestOverviewProps) {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  return (
    <div className="bg-white rounded-lg shadow p-6 mb-6">
      <h2 className="text-xl font-semibold mb-4 flex items-center">
        <i className="fas fa-info-circle mr-2 text-blue-600"></i>
        Request Overview
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <InfoItem label="Request ID" value={request.requestId} />
        <InfoItem label="Timestamp" value={formatDate(request.timestamp)} />
        
        <InfoItem 
          label="Direction" 
          value={
            <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
              request.direction === 'INBOUND' 
                ? 'bg-blue-100 text-blue-800' 
                : 'bg-purple-100 text-purple-800'
            }`}>
              <i className={`fas fa-arrow-${request.direction === 'INBOUND' ? 'down' : 'up'} mr-1`}></i>
              {request.direction}
            </span>
          }
        />
        
        <InfoItem label="Method" value={<span className="font-mono font-semibold">{request.method}</span>} />
        <InfoItem label="Request Type" value={request.requestType} />
        
        <InfoItem 
          label="Status Code" 
          value={
            <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
              request.success 
                ? 'bg-green-100 text-green-800' 
                : 'bg-red-100 text-red-800'
            }`}>
              {request.success ? <i className="fas fa-check-circle mr-1"></i> : <i className="fas fa-times-circle mr-1"></i>}
              {request.statusCode || 'N/A'}
            </span>
          }
        />
        
        <InfoItem 
          label="Duration" 
          value={request.duration ? (
            <span className={request.duration > 1000 ? 'text-orange-600 font-semibold' : ''}>
              {request.duration}ms
              {request.duration > 1000 && <i className="fas fa-exclamation-triangle ml-1"></i>}
            </span>
          ) : '-'}
        />
        
        <InfoItem label="Source" value={request.source} />
        <InfoItem label="Destination" value={request.destination} />
        
        <div className="md:col-span-3">
          <InfoItem 
            label="URL" 
            value={<span className="font-mono text-sm bg-gray-50 p-2 rounded block break-all">{request.url}</span>} 
          />
        </div>
        
        {request.errorMessage && (
          <div className="md:col-span-3">
            <InfoItem 
              label="Error Message" 
              value={<div className="text-red-600 bg-red-50 p-3 rounded flex items-start">
                <i className="fas fa-exclamation-triangle mt-1 mr-2"></i>
                <span>{request.errorMessage}</span>
              </div>} 
            />
          </div>
        )}
      </div>
    </div>
  );
}

interface InfoItemProps {
  label: string;
  value?: string | React.ReactNode;
}

function InfoItem({ label, value }: InfoItemProps) {
  return (
    <div>
      <label className="text-sm font-medium text-gray-500 mb-1 block">{label}</label>
      <div className="text-gray-900">{value || '-'}</div>
    </div>
  );
}
