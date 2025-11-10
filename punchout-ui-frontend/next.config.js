/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: 'standalone',
  
  env: {
    NEXT_PUBLIC_ENV: process.env.NEXT_PUBLIC_ENV || 'local',
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
    NEXT_PUBLIC_GATEWAY_URL: process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:9090',
    NEXT_PUBLIC_APP_NAME: process.env.NEXT_PUBLIC_APP_NAME || 'PunchOut Testing Platform - Local',
  },
  
  async rewrites() {
    // Only use rewrites in local development
    if (process.env.NEXT_PUBLIC_ENV === 'local' || !process.env.NEXT_PUBLIC_ENV) {
      return [
        {
          source: '/api/:path*',
          destination: 'http://localhost:8080/api/:path*',
        },
      ];
    }
    return [];
  },
  
  images: {
    domains: ['localhost', 'punchout.waters.com'],
  },
}

module.exports = nextConfig
