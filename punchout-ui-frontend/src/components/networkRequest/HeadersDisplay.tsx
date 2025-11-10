interface HeadersDisplayProps {
  title: string;
  headers?: Record<string, string>;
  icon?: string;
}

export default function HeadersDisplay({ title, headers, icon = 'list' }: HeadersDisplayProps) {
  const hasHeaders = headers && Object.keys(headers).length > 0;

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-xl font-semibold mb-4 flex items-center">
        <i className={`fas fa-${icon} mr-2 text-indigo-600`}></i>
        {title}
      </h2>
      {hasHeaders ? (
        <div className="space-y-2">
          {Object.entries(headers).map(([key, value]) => (
            <div key={key} className="border-b border-gray-200 pb-2 last:border-0">
              <p className="text-sm font-medium text-gray-700 mb-1">{key}</p>
              <p className="text-sm text-gray-600 break-all font-mono bg-gray-50 p-2 rounded">{value}</p>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-8">
          <i className="fas fa-inbox text-gray-300 text-3xl mb-2"></i>
          <p className="text-gray-500">No headers</p>
        </div>
      )}
    </div>
  );
}
