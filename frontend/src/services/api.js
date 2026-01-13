import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  withCredentials: true,
});

// Add a response interceptor
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // Redirect to Google OAuth login page
      window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    }
    return Promise.reject(error);
  }
);

export const extensionApi = {
  getFixedExtensions: () => api.get('/extensions/fixed'),
  updateFixedExtension: (id, isBlocked) => 
    api.patch(`/extensions/fixed/${id}?isBlocked=${isBlocked}`),
  getCustomExtensions: () => api.get('/extensions/custom'),
  addCustomExtension: (extension) => 
    api.post(`/extensions/custom?extension=${extension}`),
  deleteCustomExtension: (id) => api.delete(`/extensions/custom/${id}`),
};
