import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  withCredentials: true,
});

// Add a response interceptor
api.interceptors.response.use(
  response => response,
  error => {
    // If a 401 is received, App.js will handle rendering the Login component
    // No need for a redirect here, as it causes a loop.
    return Promise.reject(error);
  }
);

export const extensionApi = {
  getFixedExtensions: () => api.get('/extensions/fixed'),
  updateFixedExtension: (id, isBlocked) => 
    api.patch(`/extensions/fixed/${id}/block?isBlocked=${isBlocked}`),
  getCustomExtensions: () => api.get('/extensions/custom'),
  addCustomExtension: (extension) => 
    api.post(`/extensions/custom?extension=${extension}`),
  deleteCustomExtension: (id) => api.delete(`/extensions/custom/${id}`),
  getBlockedExtensions: () => api.get('/extensions/blocked'),
  getAllExtensions: () => api.get('/extensions/fixed').then(fixed => 
    api.get('/extensions/custom').then(custom => ({
      data: {
        data: [...fixed.data.data, ...custom.data.data]
      }
    }))
  ),
};

export const authApi = {
  checkAuthStatus: () => api.get('/user/me'),
};

export const fileApi = {
  checkFileExtension: (file) => api.post('/files/check', file, { headers: { 'Content-Type': 'multipart/form-data' } }),
};
