'use client'

import { useState } from 'react'
import ConverterForm from '@/components/ConverterForm'

export default function ConverterPage() {
  return (
    <div>
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-green-600 to-teal-600 text-white">
        <div className="container mx-auto px-4 py-12">
          <div className="max-w-4xl">
            <h1 className="text-4xl font-bold mb-3">
              <i className="fas fa-exchange-alt mr-3"></i>
              cXML Converter
            </h1>
            <p className="text-xl text-green-100">
              Convert between cXML and JSON formats for development and testing
            </p>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="max-w-6xl mx-auto">
          <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-8 mt-6">
            <h2 className="text-2xl font-bold mb-4">
              <i className="fas fa-file-code text-green-600 mr-2"></i>
              Convert Documents
            </h2>
            <p className="text-gray-600 mb-8">
              Paste your cXML document below and convert it to JSON format for easier inspection and debugging
            </p>
            <ConverterForm />
          </div>
        </div>
      </div>
    </div>
  )
}
