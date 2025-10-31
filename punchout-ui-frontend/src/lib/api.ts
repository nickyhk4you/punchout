import axios from 'axios';
import { PunchOutSession, OrderObject, GatewayRequest, SessionFilter } from '@/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const sessionAPI = {
  getAllSessions: async (filters?: SessionFilter): Promise<PunchOutSession[]> => {
    const params = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value) params.append(key, value);
      });
    }
    const response = await apiClient.get<PunchOutSession[]>('/punchout-sessions', { params });
    return response.data;
  },

  getSessionByKey: async (sessionKey: string): Promise<PunchOutSession> => {
    const response = await apiClient.get<PunchOutSession>(`/punchout-sessions/${sessionKey}`);
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

export default apiClient;
