import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  withCredentials: true,
});

// CSRF 토큰을 쿠키에서 읽는 헬퍼 함수
const getCsrfToken = () => {
  const name = 'XSRF-TOKEN=';
  const decodedCookie = decodeURIComponent(document.cookie);
  const cookieArray = decodedCookie.split(';');

  for (let cookie of cookieArray) {
    cookie = cookie.trim();
    if (cookie.indexOf(name) === 0) {
      return cookie.substring(name.length);
    }
  }
  return null;
};

// Request 인터셉터: CSRF 토큰을 헤더에 추가
api.interceptors.request.use(
  config => {
    // GET, HEAD, OPTIONS 요청은 CSRF 토큰 불필요
    if (['post', 'put', 'patch', 'delete'].includes(config.method.toLowerCase())) {
      const csrfToken = getCsrfToken();
      if (csrfToken) {
        config.headers['X-XSRF-TOKEN'] = csrfToken;
      }
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// Response 인터셉터
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
  checkAuthStatus: () => api.get('/auth/user'),
  logout: () => api.post('/auth/logout'),
};

export const fileApi = {
  checkFileExtension: (file) => api.post('/files/check', file, { headers: { 'Content-Type': 'multipart/form-data' } }),
};
