'use client';

import { useState } from 'react';

interface BodyDisplayProps {
  requestBody?: string;
  responseBody?: string;
}

type TabType = 'request' | 'response';

export default function BodyDisplay({ requestBody, responseBody }: BodyDisplayProps) {
  const [activeTab, setActiveTab] = useState<TabType>('request');

  const formatBody = (body?: string) => {
    if (!body) return null;
    try {
      const parsed = JSON.parse(body);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return body;
    }
  };

  const currentBody = activeTab === 'request' ? requestBody : responseBody;
  const formattedBody = formatBody(currentBody);

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-xl font-semibold mb-4 flex items-center">
        <i className="fas fa-code mr-2 text-green-600"></i>
        Message Body
      </h2>
      
      {/* Tabs */}
      <div className="mb-4 border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('request')}
            className={`py-2 px-1 border-b-2 font-medium text-sm inline-flex items-center ${
              activeTab === 'request'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <i className="fas fa-arrow-right mr-2"></i>
            Request Body
          </button>
          <button
            onClick={() => setActiveTab('response')}
            className={`py-2 px-1 border-b-2 font-medium text-sm inline-flex items-center ${
              activeTab === 'response'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <i className="fas fa-arrow-left mr-2"></i>
            Response Body
          </button>
        </nav>
      </div>

      {/* Body Content */}
      <div className="mt-4">
        {formattedBody ? (
          <div className="relative">
            <pre className="bg-gray-50 p-4 rounded-lg overflow-x-auto text-sm border border-gray-200">
              <code className="language-json">{formattedBody}</code>
            </pre>
            <button
              onClick={() => navigator.clipboard.writeText(formattedBody)}
              className="absolute top-2 right-2 px-3 py-1 bg-white border border-gray-300 rounded text-xs hover:bg-gray-50 flex items-center"
              title="Copy to clipboard"
            >
              <i className="fas fa-copy mr-1"></i>
              Copy
            </button>
          </div>
        ) : (
          <div className="text-center py-12 bg-gray-50 rounded-lg">
            <i className="fas fa-file-alt text-gray-300 text-4xl mb-3"></i>
            <p className="text-gray-500">No {activeTab} body</p>
          </div>
        )}
      </div>
    </div>
  );
}
