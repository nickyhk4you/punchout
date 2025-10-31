'use client'

import { useState } from 'react'
import ConverterForm from '@/components/ConverterForm'

export default function ConverterPage() {
  return (
    <div className="container mx-auto px-4 py-12">
      <div className="max-w-6xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-8">
          <h1 className="text-3xl font-bold text-primary mb-6">cXML Converter</h1>
          <p className="text-gray-600 mb-8">
            Convert cXML documents to JSON format
          </p>
          <ConverterForm />
        </div>
      </div>
    </div>
  )
}
