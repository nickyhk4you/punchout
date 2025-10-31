'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

// Pre-filled credentials for demonstration purposes
const DEMO_USERNAME = 'admin';
const DEMO_PASSWORD = 'admin';

export default function LoginForm() {
  const router = useRouter();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    // Simple validation
    if (!username || !password) {
      setError('Please enter both username and password');
      setLoading(false);
      return;
    }

    // For now, we'll just check against pre-filled credentials
    // In a real app, this would be an API call to a backend authentication service
    if (username === DEMO_USERNAME && password === DEMO_PASSWORD) {
      // Simulate a delay for the "login" process
      setTimeout(() => {
        // In a real app, you would store the auth token in localStorage or cookies
        localStorage.setItem('isLoggedIn', 'true');
        // Redirect to home page after successful login
        router.push('/');
      }, 1000);
    } else {
      setError('Invalid username or password');
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-8">
      <h2 className="text-2xl font-bold text-primary mb-6">Login</h2>
      
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded">
          {error}
        </div>
      )}
      
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
            Username
          </label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder={DEMO_USERNAME}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-secondary focus:border-transparent"
            disabled={loading}
          />
        </div>
        
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
            Password
          </label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter password"
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-secondary focus:border-transparent"
            disabled={loading}
          />
        </div>
        
        <div className="pt-2">
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-secondary text-white px-6 py-3 rounded-lg hover:bg-blue-600 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </div>
        
        <div className="text-center text-sm text-gray-500 mt-4">
          <p>Demo credentials:</p>
          <p>Username: {DEMO_USERNAME}</p>
          <p>Password: {DEMO_PASSWORD}</p>
        </div>
      </form>
    </div>
  );
}