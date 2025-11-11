'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { Invoice, NetworkRequest } from '@/types';
import { invoiceAPI } from '@/lib/api';
import Link from 'next/link';
import Breadcrumb from '@/components/Breadcrumb';
import { formatCurrency, formatDate, formatDateTime } from '@/lib/utils/formatters';

export default function InvoiceDetailPage() {
  const params = useParams();
  const invoiceNumber = params.invoiceNumber as string;
  
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [networkRequests, setNetworkRequests] = useState<NetworkRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'all' | 'inbound' | 'outbound'>('all');

  useEffect(() => {
    loadInvoiceDetails();
  }, [invoiceNumber]);

  const loadInvoiceDetails = async () => {
    try {
      setLoading(true);
      const [invoiceData, requestsData] = await Promise.all([
        invoiceAPI.getInvoiceByNumber(invoiceNumber),
        invoiceAPI.getInvoiceNetworkRequests(invoiceNumber)
      ]);
      setInvoice(invoiceData);
      setNetworkRequests(requestsData);
    } catch (error) {
      console.error('Failed to load invoice details:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const statusColors: Record<string, string> = {
      'PAID': 'bg-green-100 text-green-800',
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'CONFIRMED': 'bg-blue-100 text-blue-800',
      'FAILED': 'bg-red-100 text-red-800'
    };
    
    return (
      <span className={`px-3 py-1 rounded-full text-sm font-medium ${statusColors[status] || 'bg-gray-100 text-gray-800'}`}>
        {status}
      </span>
    );
  };

  const handleDownloadPDF = () => {
    alert('PDF download feature coming soon!');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-orange-600"></div>
          <p className="mt-4 text-gray-600">Loading invoice details...</p>
        </div>
      </div>
    );
  }

  if (!invoice) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <p className="text-red-600 mb-4">Error: Invoice not found</p>
          <Link
            href="/invoices"
            className="px-4 py-2 bg-orange-600 text-white rounded-md hover:bg-orange-700"
          >
            Back to Invoices
          </Link>
        </div>
      </div>
    );
  }

  const breadcrumbItems = [
    { label: 'Invoices', href: '/invoices' },
    { label: invoiceNumber },
  ];

  return (
    <div>
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-orange-600 to-amber-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-file-invoice mr-3"></i>
              Invoice Details
            </h1>
            <p className="text-xl text-orange-100">
              {invoiceNumber}
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <Breadcrumb items={breadcrumbItems} />
        
        {/* PDF Download Button */}
        <div className="mb-6 flex justify-end">
          <button
            onClick={handleDownloadPDF}
            className="px-6 py-3 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-all font-semibold shadow-lg"
          >
            <i className="fas fa-file-pdf mr-2"></i>
            Download PDF
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-xl font-semibold mb-4 text-gray-800">Invoice Information</h2>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600">Invoice Number:</span>
                <span className="font-medium">{invoice.invoiceNumber}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">PO Number:</span>
                <span className="font-medium">{invoice.poNumber}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Invoice Date:</span>
                <span className="font-medium">{formatDate(invoice.invoiceDate)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Received Date:</span>
                <span className="font-medium">{formatDateTime(invoice.receivedDate)}</span>
              </div>
              {invoice.dueDate && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Due Date:</span>
                  <span className="font-medium">{formatDate(invoice.dueDate)}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-gray-600">Status:</span>
                {getStatusBadge(invoice.status)}
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Route Name:</span>
                <span className="font-medium">{invoice.routeName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Environment:</span>
                <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                  invoice.environment === 'PRODUCTION' ? 'bg-red-100 text-red-800' :
                  invoice.environment === 'STAGING' ? 'bg-orange-100 text-orange-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {invoice.environment}
                </span>
              </div>
              {invoice.sessionKey && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Session Key:</span>
                  <Link href={`/sessions/${invoice.sessionKey}`} className="font-medium text-orange-600 hover:text-orange-800">
                    {invoice.sessionKey}
                  </Link>
                </div>
              )}
              {invoice.orderId && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Order ID:</span>
                  <Link href={`/orders/${invoice.orderId}`} className="font-medium text-orange-600 hover:text-orange-800">
                    {invoice.orderId}
                  </Link>
                </div>
              )}
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-xl font-semibold mb-4">
              <i className="fas fa-info-circle text-orange-600 mr-2"></i>
              Invoice Summary
            </h2>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600">Customer:</span>
                <span className="font-medium">{invoice.customerName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Customer ID:</span>
                <span className="font-medium">{invoice.customerId}</span>
              </div>
              {invoice.supplierName && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Supplier:</span>
                  <span className="font-medium">{invoice.supplierName}</span>
                </div>
              )}
              {invoice.subtotal && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Subtotal:</span>
                  <span className="font-medium">{formatCurrency(invoice.subtotal, invoice.currency)}</span>
                </div>
              )}
              {invoice.taxAmount && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Tax:</span>
                  <span className="font-medium">{formatCurrency(invoice.taxAmount, invoice.currency)}</span>
                </div>
              )}
              {invoice.shippingAmount && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Shipping:</span>
                  <span className="font-medium">{formatCurrency(invoice.shippingAmount, invoice.currency)}</span>
                </div>
              )}
              <div className="flex justify-between border-t pt-3">
                <span className="text-gray-600 font-semibold">Total:</span>
                <span className="font-bold text-lg text-orange-700">{formatCurrency(invoice.invoiceTotal, invoice.currency)}</span>
              </div>
              {invoice.paymentTerms && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Payment Terms:</span>
                  <span className="font-medium">{invoice.paymentTerms}</span>
                </div>
              )}
            </div>
          </div>
        </div>

        {invoice.shipTo && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h2 className="text-xl font-semibold mb-4 text-gray-800">Shipping Information</h2>
              <div className="text-gray-700 space-y-1">
                <p className="font-medium">{invoice.shipTo.name}</p>
                <p>{invoice.shipTo.street}</p>
                <p>{invoice.shipTo.city}, {invoice.shipTo.state} {invoice.shipTo.postalCode}</p>
                <p>{invoice.shipTo.country}</p>
                {invoice.shipTo.email && <p className="mt-2 text-sm">Email: {invoice.shipTo.email}</p>}
                {invoice.shipTo.phone && <p className="text-sm">Phone: {invoice.shipTo.phone}</p>}
              </div>
            </div>

            {invoice.billTo && (
              <div className="bg-white rounded-xl shadow-lg p-6">
                <h2 className="text-xl font-semibold mb-4 text-gray-800">Billing Information</h2>
                <div className="text-gray-700 space-y-1">
                  <p className="font-medium">{invoice.billTo.name}</p>
                  <p>{invoice.billTo.street}</p>
                  <p>{invoice.billTo.city}, {invoice.billTo.state} {invoice.billTo.postalCode}</p>
                  <p>{invoice.billTo.country}</p>
                </div>
              </div>
            )}
          </div>
        )}

        {invoice.lineItems && invoice.lineItems.length > 0 && (
          <div className="bg-white rounded-xl shadow-lg overflow-hidden mb-6">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-xl font-semibold">Line Items ({invoice.lineItems.length})</h2>
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
                  {invoice.lineItems.map((item: any, index: number) => (
                    <tr key={index} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">{item.lineNumber || index + 1}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-mono">{item.supplierPartId || item.partNumber}</td>
                      <td className="px-6 py-4 text-sm">{item.description}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-right">{item.quantity}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-right">
                        {formatCurrency(item.unitPrice, item.currency || invoice.currency)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-right">
                        {formatCurrency(item.extendedAmount || (item.quantity * item.unitPrice), item.currency || invoice.currency)}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="bg-gray-50">
                  {invoice.subtotal && (
                    <tr>
                      <td colSpan={5} className="px-6 py-4 text-right font-medium">Subtotal:</td>
                      <td className="px-6 py-4 text-right font-medium">{formatCurrency(invoice.subtotal, invoice.currency)}</td>
                    </tr>
                  )}
                  {invoice.taxAmount && invoice.taxAmount > 0 && (
                    <tr>
                      <td colSpan={5} className="px-6 py-4 text-right font-medium">Tax:</td>
                      <td className="px-6 py-4 text-right font-medium">{formatCurrency(invoice.taxAmount, invoice.currency)}</td>
                    </tr>
                  )}
                  {invoice.shippingAmount && invoice.shippingAmount > 0 && (
                    <tr>
                      <td colSpan={5} className="px-6 py-4 text-right font-medium">Shipping:</td>
                      <td className="px-6 py-4 text-right font-medium">{formatCurrency(invoice.shippingAmount, invoice.currency)}</td>
                    </tr>
                  )}
                  <tr className="text-lg">
                    <td colSpan={5} className="px-6 py-4 text-right font-bold">Total:</td>
                    <td className="px-6 py-4 text-right font-bold">
                      {formatCurrency(invoice.invoiceTotal, invoice.currency)}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        )}

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
                    ? 'border-orange-500 text-orange-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                All ({networkRequests.length})
              </button>
              <button
                onClick={() => setActiveTab('inbound')}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'inbound'
                    ? 'border-orange-500 text-orange-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Inbound ({networkRequests.filter(r => r.direction === 'INBOUND').length})
              </button>
              <button
                onClick={() => setActiveTab('outbound')}
                className={`py-2 px-1 border-b-2 font-medium text-sm ${
                  activeTab === 'outbound'
                    ? 'border-orange-500 text-orange-600'
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
                      <tr key={request.id} className="hover:bg-orange-50 transition-colors">
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
                            href={`/invoices/${invoiceNumber}/requests/${request.id}`}
                            className="inline-flex items-center px-3 py-1.5 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-all font-semibold"
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
            <p className="text-gray-500">No network requests found for this invoice</p>
          )}
        </div>

        {invoice.notes && (
          <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
            <h2 className="text-xl font-semibold mb-4">
              <i className="fas fa-sticky-note text-blue-600 mr-2"></i>
              Notes
            </h2>
            <p className="text-gray-700">{invoice.notes}</p>
          </div>
        )}
      </div>
    </div>
  );
}
