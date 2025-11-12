import React from 'react';
import { AppBar, Toolbar, Typography, Button } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { Link as RouterLink } from 'react-router-dom'; 

const Header = () => {
  const { isAuthenticated, logout } = useAuth();

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          {/* Title a link to the dashboard */}
          <Button 
            color="inherit" 
            component={RouterLink} 
            to="/dashboard" 
            sx={{ fontSize: '1.25rem', textTransform: 'none' }}
          >
            Bike Rental System
          </Button>
        </Typography>

        {isAuthenticated && (
          <>
            {/* --- BUTTONS --- */}
            <Button color="inherit" component={RouterLink} to="/my-bikes">
              My Bikes
            </Button>
            <Button color="inherit" component={RouterLink} to="/list-bike">
              List a Bike
            </Button>
            
            <Button color="inherit" onClick={logout}>
              Logout
            </Button>
          </>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default Header;