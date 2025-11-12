import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ProtectedRoute from './components/ProtectedRoute';
import ListBikePage from './pages/ListBikePage'; 
import MyBikesPage from './pages/MyBikesPage';   

function App() {
  return (
    <div className="App">
      <Header />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        
        <Route 
          path="/dashboard" 
          element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} 
        />
        
        {/* --- ADDED THESE ROUTES --- */}
        <Route 
          path="/list-bike" 
          element={<ProtectedRoute><ListBikePage /></ProtectedRoute>} 
        />
        <Route 
          path="/my-bikes" 
          element={<ProtectedRoute><MyBikesPage /></ProtectedRoute>} 
        />
        
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </div>
  );
}

export default App;