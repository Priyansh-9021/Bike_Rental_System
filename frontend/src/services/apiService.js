import axios from 'axios';
const serverIp = '10.89.34.36'; // <- CHANGE THIS TO YOUR IP

const apiClient = axios.create({
  baseURL: `http://${serverIp}:8080/api` 
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
export default apiClient;