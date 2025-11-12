import React, { createContext, useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../services/apiService';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(
    !!localStorage.getItem('authToken')
  );
  const [currentUser, setCurrentUser] = useState(localStorage.getItem('username'));
  const navigate = useNavigate();

  const login = async (username, password) => {
    const response = await apiClient.post('/login', { username, password });
    if (response.data.success) {
      localStorage.setItem('authToken', response.data.token);
      localStorage.setItem('username', response.data.username); 
      setIsAuthenticated(true);
      setCurrentUser(response.data.username); 
      navigate('/dashboard');
    }
    return response.data;
  };

  const register = async (username, password) => {
    return await apiClient.post('/register', { username, password });
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('username'); 
    setIsAuthenticated(false);
    setCurrentUser(null); 
    navigate('/login');
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, currentUser, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};