import axios, { AxiosInstance } from 'axios';

export const createClient = (baseURL: string): AxiosInstance => {
  const client = axios.create({
    baseURL,
    headers: {
      'Content-Type': 'application/json',
    },
    timeout: 10000,
  });

  // Add interceptors only in development
  if (process.env.NODE_ENV === 'development') {
    client.interceptors.request.use(
      (config) => {
        console.debug(`HTTP ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
        return config;
      },
      (error) => Promise.reject(error)
    );
  }

  // Response error handling
  client.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response) {
        console.error(`API Error ${error.response.status}:`, error.response.data);
      } else if (error.request) {
        console.error('Network Error: No response received from server');
      } else {
        console.error('Error:', error.message);
      }
      return Promise.reject(error);
    }
  );

  return client;
};
