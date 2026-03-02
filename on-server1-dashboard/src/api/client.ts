import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Add auth token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth
export const authApi = {
  login: (email: string, password: string) =>
    apiClient.post('/auth/login', { email, password }),
};

// Dashboard
export const adminApi = {
  getStats: () => apiClient.get('/admin/stats'),
  getUsers: (page = 1) => apiClient.get(`/admin/users?page=${page}`),
  updateUserBalance: (userId: string, amount: number, description?: string) =>
    apiClient.put(`/admin/users/${userId}/balance`, { amount, description }),
  toggleUserStatus: (userId: string, isActive: boolean) =>
    apiClient.put(`/admin/users/${userId}/status`, { isActive }),
  getBanners: () => apiClient.get('/admin/banners'),
  createBanner: (data: any) => apiClient.post('/admin/banners', data),
  updateBanner: (id: string, data: any) => apiClient.put(`/admin/banners/${id}`, data),
  deleteBanner: (id: string) => apiClient.delete(`/admin/banners/${id}`),
  getSettings: () => apiClient.get('/admin/settings'),
  updateSettings: (data: Record<string, string>) => apiClient.put('/admin/settings', data),
  // Provider (DHRU FUSION)
  syncProducts: (markupPercent: number) => apiClient.post('/admin/provider/sync', { markupPercent }),
  getProviderBalance: () => apiClient.get('/admin/provider/balance'),
  getProviderSettings: () => apiClient.get('/admin/provider/settings'),
  updateProviderSettings: (data: { url?: string; username?: string; apiKey?: string }) =>
    apiClient.put('/admin/provider/settings', data),
};

// Products
export const productsApi = {
  getAll: (params?: any) => apiClient.get('/products', { params }),
  getById: (id: string) => apiClient.get(`/products/${id}`),
  create: (data: any) => apiClient.post('/products', data),
  update: (id: string, data: any) => apiClient.put(`/products/${id}`, data),
  delete: (id: string) => apiClient.delete(`/products/${id}`),
  getCategories: () => apiClient.get('/products/categories'),
  createCategory: (data: any) => apiClient.post('/products/categories', data),
  updateCategory: (id: string, data: any) => apiClient.put(`/products/categories/${id}`, data),
};

// Orders
export const ordersApi = {
  getAll: (params?: any) => apiClient.get('/orders/admin/all', { params }),
  getById: (id: string) => apiClient.get(`/orders/${id}`),
  updateStatus: (id: string, status: string) => apiClient.put(`/orders/${id}/status`, { status }),
};

// Deposits
export const depositsApi = {
  getAll: (params?: any) => apiClient.get('/deposits/admin/all', { params }),
  getStats: () => apiClient.get('/deposits/admin/stats'),
  approve: (id: string, adminNote?: string) =>
    apiClient.put(`/deposits/admin/${id}/approve`, { adminNote }),
  reject: (id: string, adminNote?: string) =>
    apiClient.put(`/deposits/admin/${id}/reject`, { adminNote }),
};

export default apiClient;
