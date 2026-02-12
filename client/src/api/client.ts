import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const client = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Important for Refresh Token Cookie
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach Access Token
client.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      config.headers['Authorization'] = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle Token Reissue
client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // IF 401 Unauthorized AND not already retrying
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Attempt Reissue
        const { data } = await client.post('/v1/auth/reissue');
        const newAccessToken = data.accessToken;
        
        // Update Token and Retry
        localStorage.setItem('accessToken', newAccessToken);
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
        return client(originalRequest);
      } catch (reissueError) {
        // Reissue Failed -> Logout
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
        return Promise.reject(reissueError);
      }
    }

    return Promise.reject(error);
  }
);

export default client;
