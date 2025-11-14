'use client';

import { useState, useEffect } from 'react';
import { datastoreAPI } from '@/lib/api';
import { CustomerDatastore } from '@/types';
import Breadcrumb from '@/components/Breadcrumb';

export default function DatastorePage() {
  const [datastores, setDatastores] = useState<CustomerDatastore[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [editingDatastore, setEditingDatastore] = useState<CustomerDatastore | null>(null);
  const [formData, setFormData] = useState<Partial<CustomerDatastore>>({
    customer: '',
    environment: '',
    keyValuePairs: {},
    description: '',
    enabled: true,
  });
  const [newKey, setNewKey] = useState('');
  const [newValue, setNewValue] = useState('');

  useEffect(() => {
    loadDatastores();
  }, []);

  const loadDatastores = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await datastoreAPI.getAllDatastores();
      setDatastores(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load datastores');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingDatastore(null);
    setFormData({
      customer: '',
      environment: '',
      keyValuePairs: {},
      description: '',
      enabled: true,
    });
    setShowModal(true);
  };

  const handleEdit = (datastore: CustomerDatastore) => {
    setEditingDatastore(datastore);
    setFormData(datastore);
    setShowModal(true);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this datastore?')) return;
    
    try {
      await datastoreAPI.deleteDatastore(id);
      await loadDatastores();
    } catch (err: any) {
      alert('Failed to delete datastore: ' + err.message);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (editingDatastore && editingDatastore.id) {
        await datastoreAPI.updateDatastore(editingDatastore.id, formData as CustomerDatastore);
      } else {
        await datastoreAPI.createDatastore(formData);
      }
      setShowModal(false);
      await loadDatastores();
    } catch (err: any) {
      alert('Failed to save datastore: ' + err.message);
    }
  };

  const handleAddKeyValue = () => {
    if (!newKey || !newValue) return;
    
    setFormData({
      ...formData,
      keyValuePairs: {
        ...formData.keyValuePairs,
        [newKey]: newValue,
      },
    });
    setNewKey('');
    setNewValue('');
  };

  const handleRemoveKey = (key: string) => {
    const updatedPairs = { ...formData.keyValuePairs };
    delete updatedPairs[key];
    setFormData({
      ...formData,
      keyValuePairs: updatedPairs,
    });
  };

  const breadcrumbItems = [{ label: 'Customer Datastore' }];

  return (
    <div>
      <div className="bg-gradient-to-r from-purple-600 to-indigo-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-database mr-3"></i>
              Customer Datastore
            </h1>
            <p className="text-xl text-purple-100">
              Manage key-value mappings for customers across different environments
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <Breadcrumb items={breadcrumbItems} />

        <div className="mb-6 mt-6 flex justify-end">
          <button
            onClick={handleCreate}
            className="px-6 py-3 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-lg hover:from-purple-700 hover:to-indigo-700 transition-all font-semibold shadow-lg"
          >
            <i className="fas fa-plus mr-2"></i>
            Add New Datastore
          </button>
        </div>

        <div className="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden">
          {loading ? (
            <div className="p-8 text-center">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-4 border-purple-600"></div>
              <p className="mt-4 text-gray-600 text-lg">Loading datastores...</p>
            </div>
          ) : error ? (
            <div className="p-8 text-center text-red-600">
              <i className="fas fa-exclamation-circle text-4xl mb-4"></i>
              <p className="text-lg font-semibold">Error: {error}</p>
              <button
                onClick={loadDatastores}
                className="mt-4 px-6 py-3 bg-gradient-to-r from-purple-600 to-purple-700 text-white rounded-lg hover:from-purple-700 hover:to-purple-800 font-semibold transition-all shadow-lg"
              >
                <i className="fas fa-redo mr-2"></i>
                Retry
              </button>
            </div>
          ) : datastores.length === 0 ? (
            <div className="p-12 text-center text-gray-600">
              <i className="fas fa-inbox text-gray-300 text-5xl mb-4"></i>
              <p className="text-lg">No datastores found</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Customer
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Environment
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Key-Value Pairs
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Description
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {datastores.map((datastore) => (
                    <tr key={datastore.id} className="hover:bg-purple-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900">
                        {datastore.customer}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                          {datastore.environment}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">
                        <div className="space-y-1">
                          {Object.entries(datastore.keyValuePairs || {}).slice(0, 3).map(([key, value]) => (
                            <div key={key} className="text-xs">
                              <span className="font-medium text-purple-600">{key}</span>: {value}
                            </div>
                          ))}
                          {Object.keys(datastore.keyValuePairs || {}).length > 3 && (
                            <div className="text-xs text-gray-500">
                              +{Object.keys(datastore.keyValuePairs).length - 3} more
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500">
                        {datastore.description || '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                          datastore.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                        }`}>
                          {datastore.enabled ? 'Enabled' : 'Disabled'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                        <button
                          onClick={() => handleEdit(datastore)}
                          className="inline-flex items-center px-3 py-1.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all font-semibold"
                        >
                          <i className="fas fa-edit mr-1"></i>
                          Edit
                        </button>
                        <button
                          onClick={() => datastore.id && handleDelete(datastore.id)}
                          className="inline-flex items-center px-3 py-1.5 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-all font-semibold"
                        >
                          <i className="fas fa-trash mr-1"></i>
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
            <div className="bg-gradient-to-r from-purple-600 to-indigo-600 text-white px-6 py-4 rounded-t-xl">
              <h2 className="text-2xl font-bold">
                {editingDatastore ? 'Edit Datastore' : 'Create New Datastore'}
              </h2>
            </div>
            
            <form onSubmit={handleSubmit} className="p-6 space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Customer <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                    value={formData.customer || ''}
                    onChange={(e) => setFormData({ ...formData, customer: e.target.value })}
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Environment <span className="text-red-500">*</span>
                  </label>
                  <select
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                    value={formData.environment || ''}
                    onChange={(e) => setFormData({ ...formData, environment: e.target.value })}
                  >
                    <option value="">Select Environment</option>
                    <option value="dev">Development</option>
                    <option value="stage">Staging</option>
                    <option value="prod">Production</option>
                    <option value="s4-dev">S4 Development</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Description
                </label>
                <textarea
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                  rows={2}
                  value={formData.description || ''}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>

              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="enabled"
                  className="h-4 w-4 text-purple-600 focus:ring-purple-500 border-gray-300 rounded"
                  checked={formData.enabled || false}
                  onChange={(e) => setFormData({ ...formData, enabled: e.target.checked })}
                />
                <label htmlFor="enabled" className="ml-2 block text-sm text-gray-900">
                  Enabled
                </label>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-3">
                  Key-Value Pairs
                </label>
                
                <div className="flex gap-2 mb-3">
                  <input
                    type="text"
                    placeholder="Key"
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                    value={newKey}
                    onChange={(e) => setNewKey(e.target.value)}
                  />
                  <input
                    type="text"
                    placeholder="Value"
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                    value={newValue}
                    onChange={(e) => setNewValue(e.target.value)}
                  />
                  <button
                    type="button"
                    onClick={handleAddKeyValue}
                    className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-all font-semibold"
                  >
                    <i className="fas fa-plus"></i>
                  </button>
                </div>

                <div className="border border-gray-200 rounded-lg p-4 space-y-2 max-h-60 overflow-y-auto">
                  {Object.entries(formData.keyValuePairs || {}).length === 0 ? (
                    <p className="text-sm text-gray-500 text-center py-4">No key-value pairs added yet</p>
                  ) : (
                    Object.entries(formData.keyValuePairs || {}).map(([key, value]) => (
                      <div key={key} className="flex items-center justify-between bg-gray-50 p-3 rounded-lg">
                        <div className="flex-1">
                          <span className="font-medium text-purple-600">{key}</span>
                          <span className="text-gray-500 mx-2">:</span>
                          <span className="text-gray-900">{value}</span>
                        </div>
                        <button
                          type="button"
                          onClick={() => handleRemoveKey(key)}
                          className="ml-2 px-2 py-1 bg-red-600 text-white rounded hover:bg-red-700 transition-all"
                        >
                          <i className="fas fa-times"></i>
                        </button>
                      </div>
                    ))
                  )}
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-6 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition-all font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-6 py-2 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-lg hover:from-purple-700 hover:to-indigo-700 transition-all font-semibold"
                >
                  {editingDatastore ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
