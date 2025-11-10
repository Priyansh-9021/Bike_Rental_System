import axios from 'axios';

// --- MODIFIED LINE ---
// Replace 'localhost' with your server laptop's IP address
const serverIp = '10.168.46.36'; // <-- ⚠️ CHANGE THIS TO YOUR IP

// Create an Axios instance
const apiClient = axios.create({
  baseURL: `http://${serverIp}:8080/api` // Use the IP address
});

// Add a request interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Get the token from localStorage
    const token = localStorage.getItem('authToken');
    if (token) {
      // If the token exists, add it to the Authorization header
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Export the client as the default
export default apiClient;