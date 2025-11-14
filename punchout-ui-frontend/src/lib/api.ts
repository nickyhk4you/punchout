import axios from 'axios';
import { PunchOutSession, OrderObject, GatewayRequest, SessionFilter, CxmlTemplate, Order, NetworkRequest } from '@/types';

// API Base URL - configurable via environment variable
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
const GATEWAY_BASE_URL = process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:9090/api';

console.log('API Base URL:', API_BASE_URL);
console.log('Gateway Base URL:', GATEWAY_BASE_URL);

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

const gatewayClient = axios.create({
  baseURL: GATEWAY_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

// Add request interceptor for logging (development only)
apiClient.interceptors.request.use(
  (config) => {
    console.log(`API Request: ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      console.error('API Error:', error.response.status, error.response.data);
    } else if (error.request) {
      console.error('Network Error: No response received from server');
    } else {
      console.error('Error:', error.message);
    }
    return Promise.reject(error);
  }
);

export const sessionAPI = {
  // Get all sessions from MongoDB
  getAllSessions: async (filters?: SessionFilter): Promise<PunchOutSession[]> => {
    const params = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value) params.append(key, value);
      });
    }
    // Use MongoDB endpoint /v1/sessions
    const response = await apiClient.get<PunchOutSession[]>('/v1/sessions', { params });
    return response.data;
  },

  // Get session by key from MongoDB
  getSessionByKey: async (sessionKey: string): Promise<PunchOutSession> => {
    // Use MongoDB endpoint /v1/sessions/{sessionKey}
    const response = await apiClient.get<PunchOutSession>(`/v1/sessions/${sessionKey}`);
    return response.data;
  },

  createSession: async (session: Partial<PunchOutSession>): Promise<PunchOutSession> => {
    const response = await apiClient.post<PunchOutSession>('/punchout-sessions', session);
    return response.data;
  },

  updateSession: async (sessionKey: string, session: Partial<PunchOutSession>): Promise<PunchOutSession> => {
    const response = await apiClient.put<PunchOutSession>(`/punchout-sessions/${sessionKey}`, session);
    return response.data;
  },
};

export const orderAPI = {
  getOrderObject: async (sessionKey: string): Promise<OrderObject | null> => {
    try {
      const response = await apiClient.get<OrderObject>(`/punchout-sessions/${sessionKey}/order-object`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  createOrderObject: async (sessionKey: string, order: Partial<OrderObject>): Promise<OrderObject> => {
    const response = await apiClient.post<OrderObject>(`/punchout-sessions/${sessionKey}/order-object`, order);
    return response.data;
  },
};

export const gatewayAPI = {
  getGatewayRequests: async (sessionKey: string): Promise<GatewayRequest[]> => {
    const response = await apiClient.get<GatewayRequest[]>(`/punchout-sessions/${sessionKey}/gateway-requests`);
    return response.data;
  },

  createGatewayRequest: async (request: Partial<GatewayRequest>): Promise<GatewayRequest> => {
    const response = await apiClient.post<GatewayRequest>('/gateway-requests', request);
    return response.data;
  },
};

export const networkRequestAPI = {
  // Get all network requests for a session
  getNetworkRequests: async (sessionKey: string): Promise<import('@/types').NetworkRequest[]> => {
    const response = await apiClient.get<import('@/types').NetworkRequest[]>(`/v1/sessions/${sessionKey}/network-requests`);
    return response.data;
  },

  // Get inbound requests only
  getInboundRequests: async (sessionKey: string): Promise<import('@/types').NetworkRequest[]> => {
    const response = await apiClient.get<import('@/types').NetworkRequest[]>(`/v1/sessions/${sessionKey}/network-requests/inbound`);
    return response.data;
  },

  // Get outbound requests only
  getOutboundRequests: async (sessionKey: string): Promise<import('@/types').NetworkRequest[]> => {
    const response = await apiClient.get<import('@/types').NetworkRequest[]>(`/v1/sessions/${sessionKey}/network-requests/outbound`);
    return response.data;
  },

  // Get specific network request by ID
  getNetworkRequestById: async (id: string): Promise<import('@/types').NetworkRequest> => {
    const response = await apiClient.get<import('@/types').NetworkRequest>(`/v1/network-requests/${id}`);
    return response.data;
  },
};

export const catalogRouteAPI = {
  // Get all catalog routes
  getAllRoutes: async (): Promise<import('@/types').CatalogRoute[]> => {
    const response = await apiClient.get<import('@/types').CatalogRoute[]>('/v1/catalog-routes');
    return response.data;
  },

  // Get active catalog routes only
  getActiveRoutes: async (): Promise<import('@/types').CatalogRoute[]> => {
    const response = await apiClient.get<import('@/types').CatalogRoute[]>('/v1/catalog-routes/active');
    return response.data;
  },

  // Get catalog route by ID
  getRouteById: async (id: string): Promise<import('@/types').CatalogRoute> => {
    const response = await apiClient.get<import('@/types').CatalogRoute>(`/v1/catalog-routes/${id}`);
    return response.data;
  },
};

export const punchOutTestAPI = {
  // Get all punchout tests
  getAllTests: async (): Promise<import('@/types').PunchOutTest[]> => {
    const response = await apiClient.get<import('@/types').PunchOutTest[]>('/v1/punchout-tests');
    return response.data;
  },

  // Get punchout test by ID
  getTestById: async (id: string): Promise<import('@/types').PunchOutTest> => {
    const response = await apiClient.get<import('@/types').PunchOutTest>(`/v1/punchout-tests/${id}`);
    return response.data;
  },

  // Get tests by catalog route ID
  getTestsByCatalogRoute: async (routeId: string): Promise<import('@/types').PunchOutTest[]> => {
    const response = await apiClient.get<import('@/types').PunchOutTest[]>(`/v1/catalog-routes/${routeId}/tests`);
    return response.data;
  },

  // Create a new punchout test
  createTest: async (test: Partial<import('@/types').PunchOutTest>): Promise<import('@/types').PunchOutTest> => {
    const response = await apiClient.post<import('@/types').PunchOutTest>('/v1/punchout-tests', test);
    return response.data;
  },

  // Update an existing punchout test
  updateTest: async (id: string, test: Partial<import('@/types').PunchOutTest>): Promise<import('@/types').PunchOutTest> => {
    const response = await apiClient.put<import('@/types').PunchOutTest>(`/v1/punchout-tests/${id}`, test);
    return response.data;
  },
};

export const orderAPIv2 = {
  // Get all orders
  getAllOrders: async (filters?: { status?: string; customerId?: string; environment?: string }): Promise<Order[]> => {
    const params = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value) params.append(key, value);
      });
    }
    const response = await apiClient.get<Order[]>('/v1/orders', { params });
    return response.data;
  },

  // Get order by ID
  getOrderById: async (orderId: string): Promise<Order> => {
    const response = await apiClient.get<Order>(`/v1/orders/${orderId}`);
    return response.data;
  },

  // Get network requests for order
  getOrderNetworkRequests: async (orderId: string): Promise<NetworkRequest[]> => {
    const response = await apiClient.get<NetworkRequest[]>(`/v1/orders/${orderId}/network-requests`);
    return response.data;
  },

  // Get order statistics
  getOrderStats: async (): Promise<{
    totalOrders: number;
    totalValue: number;
    ordersByStatus: Record<string, number>;
    ordersByCustomer: Record<string, number>;
    ordersByEnvironment: Record<string, number>;
  }> => {
    const response = await apiClient.get('/v1/orders/stats');
    return response.data;
  },
};

export const cxmlTemplateAPI = {
  // Get all cXML templates
  getAllTemplates: async (): Promise<CxmlTemplate[]> => {
    const response = await apiClient.get<CxmlTemplate[]>('/v1/cxml-templates');
    return response.data;
  },

  // Get templates by environment
  getTemplatesByEnvironment: async (environment: string): Promise<CxmlTemplate[]> => {
    const response = await apiClient.get<CxmlTemplate[]>(`/v1/cxml-templates/environment/${environment}`);
    return response.data;
  },

  // Get template for specific environment and customer
  getTemplateByEnvironmentAndCustomer: async (environment: string, customerId: string): Promise<CxmlTemplate | null> => {
    try {
      const response = await apiClient.get<CxmlTemplate>(`/v1/cxml-templates/environment/${environment}/customer/${customerId}`);
      return response.data;
    } catch (error) {
      return null;
    }
  },

  // Get default template for environment
  getDefaultTemplate: async (environment: string): Promise<CxmlTemplate | null> => {
    try {
      const response = await apiClient.get<CxmlTemplate>(`/v1/cxml-templates/environment/${environment}/default`);
      return response.data;
    } catch (error) {
      return null;
    }
  },

  // Save template
  saveTemplate: async (template: Partial<CxmlTemplate>): Promise<CxmlTemplate> => {
    const response = await apiClient.post<CxmlTemplate>('/v1/cxml-templates', template);
    return response.data;
  },
};

export const invoiceAPI = {
  // Get all invoices
  getAllInvoices: async (filters?: { status?: string; environment?: string }): Promise<import('@/types').Invoice[]> => {
    const params = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value) params.append(key, value);
      });
    }
    const response = await apiClient.get<import('@/types').Invoice[]>('/v1/invoices', { params });
    return response.data;
  },

  // Get invoice by invoice number
  getInvoiceByNumber: async (invoiceNumber: string): Promise<import('@/types').Invoice> => {
    const response = await apiClient.get<import('@/types').Invoice>(`/v1/invoices/${invoiceNumber}`);
    return response.data;
  },

  // Get network requests for invoice
  getInvoiceNetworkRequests: async (invoiceNumber: string): Promise<NetworkRequest[]> => {
    const response = await apiClient.get<NetworkRequest[]>(`/v1/invoices/${invoiceNumber}/network-requests`);
    return response.data;
  },
};

export const datastoreAPI = {
  // Get all datastores
  getAllDatastores: async (): Promise<import('@/types').CustomerDatastore[]> => {
    const response = await gatewayClient.get<import('@/types').CustomerDatastore[]>('/datastore');
    return response.data;
  },

  // Get datastore by ID
  getDatastoreById: async (id: string): Promise<import('@/types').CustomerDatastore> => {
    const response = await gatewayClient.get<import('@/types').CustomerDatastore>(`/datastore/${id}`);
    return response.data;
  },

  // Get datastores by customer
  getDatastoresByCustomer: async (customer: string): Promise<import('@/types').CustomerDatastore[]> => {
    const response = await gatewayClient.get<import('@/types').CustomerDatastore[]>(`/datastore/customer/${customer}`);
    return response.data;
  },

  // Get datastores by environment
  getDatastoresByEnvironment: async (environment: string): Promise<import('@/types').CustomerDatastore[]> => {
    const response = await gatewayClient.get<import('@/types').CustomerDatastore[]>(`/datastore/environment/${environment}`);
    return response.data;
  },

  // Get datastore by customer and environment
  getDatastoreByCustomerAndEnvironment: async (customer: string, environment: string): Promise<import('@/types').CustomerDatastore> => {
    const response = await gatewayClient.get<import('@/types').CustomerDatastore>(`/datastore/customer/${customer}/environment/${environment}`);
    return response.data;
  },

  // Create datastore
  createDatastore: async (datastore: Partial<import('@/types').CustomerDatastore>): Promise<import('@/types').CustomerDatastore> => {
    const response = await gatewayClient.post<import('@/types').CustomerDatastore>('/datastore', datastore);
    return response.data;
  },

  // Update datastore
  updateDatastore: async (id: string, datastore: Partial<import('@/types').CustomerDatastore>): Promise<import('@/types').CustomerDatastore> => {
    const response = await gatewayClient.put<import('@/types').CustomerDatastore>(`/datastore/${id}`, datastore);
    return response.data;
  },

  // Delete datastore
  deleteDatastore: async (id: string): Promise<void> => {
    await gatewayClient.delete(`/datastore/${id}`);
  },

  // Add or update key-value
  addOrUpdateKeyValue: async (id: string, key: string, value: string): Promise<import('@/types').CustomerDatastore> => {
    const response = await gatewayClient.put<import('@/types').CustomerDatastore>(`/datastore/${id}/key/${key}`, { value });
    return response.data;
  },

  // Remove key
  removeKey: async (id: string, key: string): Promise<import('@/types').CustomerDatastore> => {
    const response = await gatewayClient.delete<import('@/types').CustomerDatastore>(`/datastore/${id}/key/${key}`);
    return response.data;
  },
};

export const onboardingAPI = {
  // Get all onboardings
  getAllOnboardings: async (): Promise<import('@/types').CustomerOnboarding[]> => {
    const response = await gatewayClient.get<import('@/types').CustomerOnboarding[]>('/onboarding');
    return response.data;
  },

  // Get onboarding by ID
  getOnboardingById: async (id: string): Promise<import('@/types').CustomerOnboarding> => {
    const response = await gatewayClient.get<import('@/types').CustomerOnboarding>(`/onboarding/${id}`);
    return response.data;
  },

  // Get deployed onboardings
  getDeployedOnboardings: async (): Promise<import('@/types').CustomerOnboarding[]> => {
    const response = await gatewayClient.get<import('@/types').CustomerOnboarding[]>('/onboarding/deployed');
    return response.data;
  },

  // Create onboarding
  createOnboarding: async (onboarding: Partial<import('@/types').CustomerOnboarding>): Promise<import('@/types').CustomerOnboarding> => {
    const response = await gatewayClient.post<import('@/types').CustomerOnboarding>('/onboarding', onboarding);
    return response.data;
  },

  // Update onboarding
  updateOnboarding: async (id: string, onboarding: Partial<import('@/types').CustomerOnboarding>): Promise<import('@/types').CustomerOnboarding> => {
    const response = await gatewayClient.put<import('@/types').CustomerOnboarding>(`/onboarding/${id}`, onboarding);
    return response.data;
  },

  // Deploy onboarding
  deployOnboarding: async (id: string): Promise<import('@/types').CustomerOnboarding> => {
    const response = await gatewayClient.post<import('@/types').CustomerOnboarding>(`/onboarding/${id}/deploy`);
    return response.data;
  },

  // Generate converter
  generateConverter: async (id: string): Promise<import('@/types').CustomerOnboarding> => {
    const response = await gatewayClient.post<import('@/types').CustomerOnboarding>(`/onboarding/${id}/generate-converter`);
    return response.data;
  },

  // Delete onboarding
  deleteOnboarding: async (id: string): Promise<void> => {
    await gatewayClient.delete(`/onboarding/${id}`);
  },

  // Test conversion
  testConversion: async (id: string, testData: any): Promise<any> => {
    const response = await gatewayClient.post(`/onboarding/${id}/test`, testData);
    return response.data;
  },
};

export default apiClient;
