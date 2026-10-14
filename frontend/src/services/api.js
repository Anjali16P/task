import axios from 'axios';

export const TOKEN_KEY = 'fms.token';
export const USER_KEY = 'fms.user';
export const AUTH_EVENT = 'fms:auth-change';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const url = error?.config?.url || '';
    const isAuthEndpoint = url.includes('/auth/login') || url.includes('/auth/register');
    if (status === 401 && !isAuthEndpoint) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      window.dispatchEvent(new Event(AUTH_EVENT));
    }
    return Promise.reject(error);
  }
);

export function extractErrorMessage(error, fallback) {
  const data = error?.response?.data;
  if (data?.fieldErrors) {
    const first = Object.values(data.fieldErrors)[0];
    if (first) return first;
  }
  if (data?.message) return data.message;
  if (error?.code === 'ERR_NETWORK') {
    return 'Cannot reach the server. Make sure the backend is running.';
  }
  if (error?.message) return error.message;
  return fallback || 'Something went wrong. Please try again.';
}

export default api;
