'use client';

import LoginForm from '@/components/LoginForm';

export default function LoginPage() {
  return (
    <div className="container mx-auto px-4 py-12">
      <div className="max-w-md mx-auto">
        <LoginForm />
        <div className="mt-6 text-center">
          <p className="text-gray-600">
            Need an account? Contact your administrator.
          </p>
        </div>
      </div>
    </div>
  );
}