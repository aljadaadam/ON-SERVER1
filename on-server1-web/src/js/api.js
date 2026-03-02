/**
 * ON-SERVER1 API Client
 * Handles all API communication for the web frontend
 */

const API_BASE = '/api';

const api = {
  /**
   * Generic fetch wrapper
   */
  async request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const config = {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    };

    // Add auth token if available
    const token = localStorage.getItem('user_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, config);
      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'حدث خطأ في الاتصال');
      }

      return data;
    } catch (error) {
      if (error.message === 'Failed to fetch') {
        throw new Error('فشل الاتصال بالخادم');
      }
      throw error;
    }
  },

  /**
   * GET request
   */
  async get(endpoint, params = {}) {
    const queryString = new URLSearchParams(params).toString();
    const url = queryString ? `${endpoint}?${queryString}` : endpoint;
    return this.request(url);
  },

  /**
   * POST request
   */
  async post(endpoint, body = {}) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body),
    });
  },

  // ============================================
  // Products API
  // ============================================
  products: {
    async getAll(params = {}) {
      return api.get('/products', params);
    },

    async getById(id) {
      return api.get(`/products/${id}`);
    },

    async getFeatured() {
      return api.get('/products/featured');
    },

    async getCategories() {
      return api.get('/products/categories');
    },
  },

  // ============================================
  // Banners API
  // ============================================
  banners: {
    async getAll() {
      return api.get('/banners');
    },
  },

  // ============================================
  // Orders API
  // ============================================
  orders: {
    async create(items) {
      return api.post('/orders', { items });
    },

    async getAll(params = {}) {
      return api.get('/orders', params);
    },

    async getById(id) {
      return api.get(`/orders/${id}`);
    },
  },
};

// Make API globally available
window.api = api;
