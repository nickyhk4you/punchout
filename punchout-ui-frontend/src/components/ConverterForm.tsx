'use client'

import { useState } from 'react'
import axios from 'axios'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'

const sampleXML = `<cXML>
  <OrderID>12345</OrderID>
  <OrderDate>2025-10-30</OrderDate>
  <BuyerCookie>ACME Corp</BuyerCookie>
  <ItemOut lineNumber="1">
    <ItemID>PROD-001</ItemID>
    <Description>Product 1</Description>
    <Quantity>10</Quantity>
    <UnitPrice>99.99</UnitPrice>
    <UnitOfMeasure>EA</UnitOfMeasure>
  </ItemOut>
</cXML>`

export default function ConverterForm() {
  const [customerId, setCustomerId] = useState('DEFAULT')
  const [documentType, setDocumentType] = useState('ORDER')
  const [cxmlContent, setCxmlContent] = useState('')
  const [result, setResult] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const response = await axios.post(`${API_BASE_URL}/api/v1/cxml/convert`, {
        customerId,
        documentType,
        cxmlContent,
      })
      setResult(response.data)
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'An error occurred')
    } finally {
      setLoading(false)
    }
  }

  const loadSample = () => {
    setCustomerId('DEFAULT')
    setDocumentType('ORDER')
    setCxmlContent(sampleXML)
  }

  return (
    <div>
      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label htmlFor="customerId" className="block text-sm font-medium text-gray-700 mb-2">
            Customer ID
          </label>
          <input
            type="text"
            id="customerId"
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value)}
            placeholder="e.g., CUSTOMER_A or DEFAULT"
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-secondary focus:border-transparent"
            required
          />
        </div>

        <div>
          <label htmlFor="documentType" className="block text-sm font-medium text-gray-700 mb-2">
            Document Type
          </label>
          <select
            id="documentType"
            value={documentType}
            onChange={(e) => setDocumentType(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-secondary focus:border-transparent"
            required
          >
            <option value="ORDER">ORDER</option>
            <option value="INVOICE">INVOICE</option>
            <option value="CATALOG">CATALOG</option>
          </select>
        </div>

        <div>
          <label htmlFor="cxmlContent" className="block text-sm font-medium text-gray-700 mb-2">
            cXML Content
          </label>
          <textarea
            id="cxmlContent"
            value={cxmlContent}
            onChange={(e) => setCxmlContent(e.target.value)}
            placeholder="Paste your cXML content here..."
            rows={12}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg font-mono text-sm focus:ring-2 focus:ring-secondary focus:border-transparent"
            required
          />
        </div>

        <div className="flex gap-4">
          <button
            type="submit"
            disabled={loading}
            className="bg-secondary text-white px-6 py-3 rounded-lg hover:bg-blue-600 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Converting...' : 'Convert'}
          </button>
          <button
            type="button"
            onClick={loadSample}
            className="bg-gray-500 text-white px-6 py-3 rounded-lg hover:bg-gray-600 transition"
          >
            Load Sample
          </button>
        </div>
      </form>

      {error && (
        <div className="mt-8 p-4 bg-red-50 border border-red-200 rounded-lg">
          <h3 className="text-lg font-semibold text-red-800 mb-2">Error</h3>
          <p className="text-red-700">{error}</p>
          <p className="text-sm text-red-600 mt-2">
            Make sure the backend service is running on {API_BASE_URL}
          </p>
        </div>
      )}

      {result && (
        <div className="mt-8 p-4 bg-gray-50 border border-gray-200 rounded-lg">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Result:</h3>
          <pre className="bg-white p-4 rounded border border-gray-300 overflow-x-auto text-sm">
            {JSON.stringify(result, null, 2)}
          </pre>
        </div>
      )}
    </div>
  )
}
