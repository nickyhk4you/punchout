'use client';

import { useState } from 'react';
import Breadcrumb from '@/components/Breadcrumb';
import { onboardingAPI } from '@/lib/api';

type CustomerType = 'ARIBA' | 'COUPA' | 'ORACLE' | 'SAP' | 'CUSTOM';

interface OnboardingData {
  customerName: string;
  customerType: CustomerType;
  network: string;
  environment: string;
  sampleCxml: string;
  targetJson: string;
  fieldMappings: Record<string, string>;
  notes: string;
}

export default function CustomerOnboardingPage() {
  const [currentStep, setCurrentStep] = useState(1);
  const [formData, setFormData] = useState<OnboardingData>({
    customerName: '',
    customerType: 'CUSTOM',
    network: '',
    environment: 'dev',
    sampleCxml: '',
    targetJson: '',
    fieldMappings: {},
    notes: '',
  });
  const [autoMappings, setAutoMappings] = useState<any>(null);
  const [selectedEnvironments, setSelectedEnvironments] = useState<string[]>(['dev']);
  const [availableEnvironments] = useState([
    { value: 'dev', label: 'Development', color: 'blue', icon: 'laptop-code' },
    { value: 'stage', label: 'Staging', color: 'purple', icon: 'vial' },
    { value: 'prod', label: 'Production', color: 'red', icon: 'rocket' },
    { value: 's4-dev', label: 'S4 Dev', color: 'green', icon: 'server' },
  ]);

  const steps = [
    { id: 1, title: 'Customer Info', icon: 'user-circle' },
    { id: 2, title: 'cXML Sample', icon: 'code' },
    { id: 3, title: 'Target JSON', icon: 'file-code' },
    { id: 4, title: 'Field Mapping', icon: 'exchange-alt' },
    { id: 5, title: 'Review & Deploy', icon: 'check-circle' },
  ];

  const customerTypes: { value: CustomerType; label: string; icon: string }[] = [
    { value: 'ARIBA', label: 'SAP Ariba', icon: 'building' },
    { value: 'COUPA', label: 'Coupa', icon: 'cloud' },
    { value: 'ORACLE', label: 'Oracle iProcurement', icon: 'database' },
    { value: 'SAP', label: 'SAP S/4HANA', icon: 'server' },
    { value: 'CUSTOM', label: 'Custom Platform', icon: 'cog' },
  ];

  const handleNext = () => {
    if (currentStep < steps.length) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrevious = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleAutoMap = () => {
    // Simulated auto-mapping logic
    setAutoMappings({
      'cXML.Header.From': 'from.identity',
      'cXML.Header.To': 'to.identity',
      'cXML.Request.PunchOutSetupRequest.BuyerCookie': 'buyerCookie',
      'cXML.Request.PunchOutSetupRequest.BrowserFormPost.URL': 'returnUrl',
    });
  };

  const toggleEnvironment = (env: string) => {
    setSelectedEnvironments(prev => 
      prev.includes(env) 
        ? prev.filter(e => e !== env)
        : [...prev, env]
    );
  };

  const setEnvironmentPreset = (preset: 'all' | 'dev-only' | 'dev-stage' | 'prod-ready') => {
    switch (preset) {
      case 'all':
        setSelectedEnvironments(availableEnvironments.map(e => e.value));
        break;
      case 'dev-only':
        setSelectedEnvironments(['dev']);
        break;
      case 'dev-stage':
        setSelectedEnvironments(['dev', 'stage']);
        break;
      case 'prod-ready':
        setSelectedEnvironments(['stage', 'prod']);
        break;
    }
  };

  const handleDeploy = async () => {
    if (selectedEnvironments.length === 0) {
      alert('⚠️ Please select at least one environment to deploy to.');
      return;
    }

    try {
      const deploymentResults = [];
      
      // Deploy to each selected environment
      for (const env of selectedEnvironments) {
        const envFormData = { ...formData, environment: env };
        
        // Save onboarding configuration to MongoDB
        const saved = await onboardingAPI.createOnboarding(envFormData);
        
        // Deploy the converter
        await onboardingAPI.deployOnboarding(saved.id!);
        
        deploymentResults.push(env);
      }
      
      alert(`🎉 Converter deployed successfully!\n\nCustomer: ${formData.customerName}\nEnvironments: ${deploymentResults.map(e => e.toUpperCase()).join(', ')}\n\nYou can now test it in Developer Tools → Testing`);
    } catch (error: any) {
      alert('Failed to deploy converter: ' + error.message);
    }
  };

  const breadcrumbItems = [{ label: 'Customer Onboarding' }];

  return (
    <div>
      <div className="bg-gradient-to-r from-green-600 to-teal-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-user-plus mr-3"></i>
              B2B Customer Onboarding
            </h1>
            <p className="text-xl text-green-100">
              Streamline onboarding with automated cXML to JSON conversion
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <Breadcrumb items={breadcrumbItems} />

        {/* Progress Steps */}
        <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6 mt-6">
          <div className="flex items-center justify-between">
            {steps.map((step, index) => (
              <div key={step.id} className="flex items-center flex-1">
                <div className="flex flex-col items-center flex-1">
                  <div
                    className={`w-12 h-12 rounded-full flex items-center justify-center font-bold transition-all ${
                      currentStep === step.id
                        ? 'bg-gradient-to-r from-green-600 to-teal-600 text-white shadow-lg scale-110'
                        : currentStep > step.id
                        ? 'bg-green-500 text-white'
                        : 'bg-gray-200 text-gray-500'
                    }`}
                  >
                    <i className={`fas fa-${step.icon}`}></i>
                  </div>
                  <span className={`mt-2 text-xs font-medium ${currentStep === step.id ? 'text-green-600' : 'text-gray-500'}`}>
                    {step.title}
                  </span>
                </div>
                {index < steps.length - 1 && (
                  <div className={`h-1 flex-1 mx-2 ${currentStep > step.id ? 'bg-green-500' : 'bg-gray-200'}`}></div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Step Content */}
        <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-8 mb-6">
          {/* Step 1: Customer Info */}
          {currentStep === 1 && (
            <div className="space-y-6">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">
                <i className="fas fa-user-circle text-green-600 mr-2"></i>
                Customer Information
              </h2>

              <div className="grid grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Customer Name <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                    placeholder="e.g., Acme Corporation"
                    value={formData.customerName}
                    onChange={(e) => setFormData({ ...formData, customerName: e.target.value })}
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Network/Domain <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                    placeholder="e.g., acme.com"
                    value={formData.network}
                    onChange={(e) => setFormData({ ...formData, network: e.target.value })}
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-3">
                  Customer Platform Type <span className="text-red-500">*</span>
                </label>
                <div className="grid grid-cols-5 gap-4">
                  {customerTypes.map((type) => (
                    <button
                      key={type.value}
                      onClick={() => setFormData({ ...formData, customerType: type.value })}
                      className={`p-4 rounded-lg border-2 transition-all text-center ${
                        formData.customerType === type.value
                          ? 'border-green-600 bg-green-50 shadow-md'
                          : 'border-gray-200 hover:border-green-300'
                      }`}
                    >
                      <i className={`fas fa-${type.icon} text-3xl mb-2 ${formData.customerType === type.value ? 'text-green-600' : 'text-gray-400'}`}></i>
                      <div className={`text-sm font-medium ${formData.customerType === type.value ? 'text-green-600' : 'text-gray-700'}`}>
                        {type.label}
                      </div>
                    </button>
                  ))}
                </div>
              </div>

            </div>
          )}

          {/* Step 2: cXML Sample */}
          {currentStep === 2 && (
            <div className="space-y-6">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">
                <i className="fas fa-code text-green-600 mr-2"></i>
                Provide Sample cXML
              </h2>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
                <div className="flex items-start">
                  <i className="fas fa-info-circle text-blue-600 mt-1 mr-3"></i>
                  <div className="text-sm text-blue-800">
                    <p className="font-semibold mb-1">Upload or paste a sample cXML request from the customer</p>
                    <p>This will be used to analyze the structure and auto-generate field mappings to your JSON format.</p>
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Sample cXML Request <span className="text-red-500">*</span>
                </label>
                <textarea
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 font-mono text-sm"
                  rows={16}
                  placeholder="Paste cXML here..."
                  value={formData.sampleCxml}
                  onChange={(e) => setFormData({ ...formData, sampleCxml: e.target.value })}
                ></textarea>
              </div>

              <div className="flex gap-3">
                <button className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all font-semibold">
                  <i className="fas fa-upload mr-2"></i>
                  Upload cXML File
                </button>
                <button className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-all font-semibold">
                  <i className="fas fa-clipboard mr-2"></i>
                  Use Template
                </button>
              </div>
            </div>
          )}

          {/* Step 3: Target JSON */}
          {currentStep === 3 && (
            <div className="space-y-6">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">
                <i className="fas fa-file-code text-green-600 mr-2"></i>
                Define Target JSON Schema
              </h2>

              <div className="bg-purple-50 border border-purple-200 rounded-lg p-4 mb-4">
                <div className="flex items-start">
                  <i className="fas fa-lightbulb text-purple-600 mt-1 mr-3"></i>
                  <div className="text-sm text-purple-800">
                    <p className="font-semibold mb-1">Specify your expected JSON output format</p>
                    <p>This represents how you want the converted data to look in your system.</p>
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Target JSON Schema <span className="text-red-500">*</span>
                </label>
                <textarea
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 font-mono text-sm"
                  rows={16}
                  placeholder="Paste your target JSON schema here..."
                  value={formData.targetJson}
                  onChange={(e) => setFormData({ ...formData, targetJson: e.target.value })}
                ></textarea>
              </div>

              <div className="flex gap-3">
                <button className="px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-all font-semibold">
                  <i className="fas fa-file-import mr-2"></i>
                  Load from Existing Customer
                </button>
                <button className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-all font-semibold">
                  <i className="fas fa-magic mr-2"></i>
                  Generate from cXML
                </button>
              </div>
            </div>
          )}

          {/* Step 4: Field Mapping */}
          {currentStep === 4 && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <h2 className="text-2xl font-bold text-gray-900">
                  <i className="fas fa-exchange-alt text-green-600 mr-2"></i>
                  Field Mapping Configuration
                </h2>
                <button
                  onClick={handleAutoMap}
                  className="px-6 py-3 bg-gradient-to-r from-green-600 to-teal-600 text-white rounded-lg hover:from-green-700 hover:to-teal-700 transition-all font-semibold shadow-lg"
                >
                  <i className="fas fa-magic mr-2"></i>
                  Auto-Generate Mappings
                </button>
              </div>

              {autoMappings ? (
                <div className="space-y-4">
                  <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                    <div className="flex items-center">
                      <i className="fas fa-check-circle text-green-600 mr-3 text-xl"></i>
                      <span className="text-green-800 font-semibold">Auto-mapping completed! Review and adjust as needed.</span>
                    </div>
                  </div>

                  <div className="border border-gray-200 rounded-lg overflow-hidden">
                    <table className="w-full">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">cXML Field Path</th>
                          <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase w-16"></th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">JSON Field Path</th>
                          <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase w-24">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {Object.entries(autoMappings).map(([cxmlPath, jsonPath], index) => (
                          <tr key={index} className="hover:bg-gray-50">
                            <td className="px-4 py-3 text-sm font-mono text-blue-600">{cxmlPath}</td>
                            <td className="px-4 py-3 text-center">
                              <i className="fas fa-arrow-right text-gray-400"></i>
                            </td>
                            <td className="px-4 py-3 text-sm font-mono text-purple-600">{jsonPath as string}</td>
                            <td className="px-4 py-3 text-center">
                              <button className="text-blue-600 hover:text-blue-800 mr-2" title="Edit">
                                <i className="fas fa-edit"></i>
                              </button>
                              <button className="text-red-600 hover:text-red-800" title="Delete">
                                <i className="fas fa-trash"></i>
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <button className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all font-semibold">
                    <i className="fas fa-plus mr-2"></i>
                    Add Custom Mapping
                  </button>
                </div>
              ) : (
                <div className="text-center py-16 bg-gray-50 rounded-lg border-2 border-dashed border-gray-300">
                  <i className="fas fa-wand-magic text-6xl text-gray-300 mb-4"></i>
                  <p className="text-gray-600 text-lg font-medium mb-4">Click "Auto-Generate Mappings" to start</p>
                  <p className="text-gray-500 text-sm">We'll analyze your cXML and JSON to create intelligent field mappings</p>
                </div>
              )}
            </div>
          )}

          {/* Step 5: Review & Deploy */}
          {currentStep === 5 && (
            <div className="space-y-6">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">
                <i className="fas fa-check-circle text-green-600 mr-2"></i>
                Review & Deploy Converter
              </h2>

              <div className="grid grid-cols-2 gap-6">
                <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-6 border border-blue-200">
                  <h3 className="font-bold text-lg mb-4 text-blue-900">
                    <i className="fas fa-info-circle mr-2"></i>
                    Customer Details
                  </h3>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-blue-700 font-medium">Name:</span>
                      <span className="text-blue-900 font-semibold">{formData.customerName || 'N/A'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-blue-700 font-medium">Type:</span>
                      <span className="text-blue-900 font-semibold">{formData.customerType}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-blue-700 font-medium">Network:</span>
                      <span className="text-blue-900 font-semibold">{formData.network || 'N/A'}</span>
                    </div>
                  </div>
                </div>

                <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-lg p-6 border border-purple-200">
                  <h3 className="font-bold text-lg mb-4 text-purple-900">
                    <i className="fas fa-exchange-alt mr-2"></i>
                    Conversion Summary
                  </h3>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-purple-700 font-medium">cXML Size:</span>
                      <span className="text-purple-900 font-semibold">{formData.sampleCxml.length} chars</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-purple-700 font-medium">Mappings:</span>
                      <span className="text-purple-900 font-semibold">{autoMappings ? Object.keys(autoMappings).length : 0} fields</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-purple-700 font-medium">Status:</span>
                      <span className="text-green-600 font-semibold">
                        <i className="fas fa-check-circle mr-1"></i>
                        Ready to Deploy
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Environment Selection Section */}
              <div className="bg-gradient-to-br from-indigo-50 to-blue-50 border-2 border-indigo-200 rounded-xl p-6">
                <div className="mb-4">
                  <h3 className="font-bold text-lg text-gray-900 mb-2">
                    <i className="fas fa-server text-indigo-600 mr-2"></i>
                    Deploy to Environments
                  </h3>
                  <p className="text-sm text-gray-600">Select which environments to deploy this customer configuration to</p>
                </div>
                
                {/* Environment Chips */}
                <div className="grid grid-cols-2 gap-3 mb-4">
                  {availableEnvironments.map((env) => {
                    const isSelected = selectedEnvironments.includes(env.value);
                    return (
                      <button
                        key={env.value}
                        type="button"
                        onClick={() => toggleEnvironment(env.value)}
                        className={`relative flex items-center gap-3 px-5 py-4 rounded-xl border-2 transition-all duration-200 ${
                          isSelected
                            ? `border-${env.color}-500 bg-gradient-to-br from-${env.color}-500 to-${env.color}-600 text-white shadow-lg transform scale-[1.02]`
                            : 'border-gray-300 bg-white hover:border-gray-400 hover:shadow-md text-gray-700'
                        }`}
                      >
                        <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                          isSelected ? 'bg-white bg-opacity-20' : 'bg-gray-100'
                        }`}>
                          <i className={`fas fa-${env.icon} ${isSelected ? 'text-white' : `text-${env.color}-600`}`}></i>
                        </div>
                        <div className="flex-1 text-left">
                          <div className={`font-bold text-sm ${isSelected ? 'text-white' : 'text-gray-900'}`}>
                            {env.label}
                          </div>
                          <div className={`text-xs ${isSelected ? 'text-white text-opacity-90' : 'text-gray-500'}`}>
                            {env.value.toUpperCase()}
                          </div>
                        </div>
                        {isSelected && (
                          <div className="absolute top-2 right-2">
                            <i className="fas fa-check-circle text-white text-lg"></i>
                          </div>
                        )}
                      </button>
                    );
                  })}
                </div>

                {/* Quick Presets */}
                <div className="pt-4 border-t border-indigo-200">
                  <p className="text-xs text-gray-600 mb-2 font-semibold uppercase tracking-wide">Quick Presets:</p>
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => setEnvironmentPreset('dev-only')}
                      className="px-3 py-1.5 bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200 transition-all text-sm font-semibold"
                    >
                      <i className="fas fa-laptop-code mr-1"></i>
                      Dev Only
                    </button>
                    <button
                      type="button"
                      onClick={() => setEnvironmentPreset('dev-stage')}
                      className="px-3 py-1.5 bg-purple-100 text-purple-700 rounded-lg hover:bg-purple-200 transition-all text-sm font-semibold"
                    >
                      <i className="fas fa-vial mr-1"></i>
                      Dev + Staging
                    </button>
                    <button
                      type="button"
                      onClick={() => setEnvironmentPreset('prod-ready')}
                      className="px-3 py-1.5 bg-orange-100 text-orange-700 rounded-lg hover:bg-orange-200 transition-all text-sm font-semibold"
                    >
                      <i className="fas fa-rocket mr-1"></i>
                      Production Ready
                    </button>
                    <button
                      type="button"
                      onClick={() => setEnvironmentPreset('all')}
                      className="px-3 py-1.5 bg-green-100 text-green-700 rounded-lg hover:bg-green-200 transition-all text-sm font-semibold"
                    >
                      <i className="fas fa-globe mr-1"></i>
                      All Environments
                    </button>
                  </div>
                </div>

                {/* Selected Count */}
                <div className="mt-4 pt-4 border-t border-indigo-200">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-600">
                      <i className="fas fa-info-circle text-indigo-600 mr-1"></i>
                      Selected Environments:
                    </span>
                    <span className="font-bold text-indigo-600">
                      {selectedEnvironments.length} of {availableEnvironments.length}
                    </span>
                  </div>
                  {selectedEnvironments.length > 0 && (
                    <div className="mt-2 flex flex-wrap gap-2">
                      {selectedEnvironments.map(env => (
                        <span key={env} className="px-2 py-1 bg-indigo-100 text-indigo-700 rounded text-xs font-semibold">
                          {env.toUpperCase()}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                <div className="flex items-start">
                  <i className="fas fa-exclamation-triangle text-yellow-600 mt-1 mr-3"></i>
                  <div className="text-sm text-yellow-800">
                    <p className="font-semibold mb-1">Before deploying, make sure to:</p>
                    <ul className="list-disc list-inside space-y-1 ml-2">
                      <li>Verify all field mappings are correct</li>
                      <li>Select appropriate target environments</li>
                      <li>Test the converter with sample data in Developer Tools</li>
                      <li>Coordinate with the customer for testing window</li>
                    </ul>
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Deployment Notes (Optional)
                </label>
                <textarea
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                  rows={4}
                  placeholder="Add any notes about this deployment..."
                  value={formData.notes}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                ></textarea>
              </div>
            </div>
          )}
        </div>

        {/* Navigation Buttons */}
        <div className="flex justify-between items-center">
          <button
            onClick={handlePrevious}
            disabled={currentStep === 1}
            className={`px-6 py-3 rounded-lg font-semibold transition-all ${
              currentStep === 1
                ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                : 'bg-gray-300 text-gray-700 hover:bg-gray-400'
            }`}
          >
            <i className="fas fa-arrow-left mr-2"></i>
            Previous
          </button>

          {currentStep === steps.length ? (
            <button
              onClick={handleDeploy}
              className="px-8 py-3 bg-gradient-to-r from-green-600 to-teal-600 text-white rounded-lg hover:from-green-700 hover:to-teal-700 transition-all font-semibold shadow-lg text-lg"
            >
              <i className="fas fa-rocket mr-2"></i>
              Deploy Converter
            </button>
          ) : (
            <button
              onClick={handleNext}
              className="px-6 py-3 bg-gradient-to-r from-green-600 to-teal-600 text-white rounded-lg hover:from-green-700 hover:to-teal-700 transition-all font-semibold"
            >
              Next
              <i className="fas fa-arrow-right ml-2"></i>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
