import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  withCredentials: true,
});

export const extensionApi = {
  getFixedExtensions: () => api.get('/extensions/fixed'),
  updateFixedExtension: (id, isBlocked) => 
    api.patch(`/extensions/fixed/${id}?isBlocked=${isBlocked}`),
  getCustomExtensions: () => api.get('/extensions/custom'),
  addCustomExtension: (extension) => 
    api.post(`/extensions/custom?extension=${extension}`),
  deleteCustomExtension: (id) => api.delete(`/extensions/custom/${id}`),
};
