import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import { 
  Container, Paper, Tabs, Tab, TextField, Button, Box, Typography 
} from '@mui/material';

const LoginPage = () => {
  const [tabIndex, setTabIndex] = useState(0); // 0 for Login, 1 for Register
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  const handleTabChange = (event, newValue) => {
    setTabIndex(newValue);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (tabIndex === 0) {
        // --- Login Logic ---
        await login(username, password);
        enqueueSnackbar('Login successful!', { variant: 'success' });
        navigate('/dashboard');
      } else {
        // --- Register Logic ---
        await register(username, password);
        enqueueSnackbar('Registration successful! Please log in.', { variant: 'success' });
        setTabIndex(0); // Switch to login tab
        setUsername('');
        setPassword('');
      }
    } catch (error) {
      const message = error.response?.data?.message || 'An error occurred';
      enqueueSnackbar(message, { variant: 'error' });
    }
  };

  return (
    <Container component="main" maxWidth="xs" sx={{ mt: 8 }}>
      <Paper elevation={6} sx={{ p: 4 }}>
        <Typography component="h1" variant="h5" align="center">
          Bike Rental
        </Typography>
        <Tabs
          value={tabIndex}
          onChange={handleTabChange}
          indicatorColor="primary"
          textColor="primary"
          variant="fullWidth"
          sx={{ mb: 3 }}
        >
          <Tab label="Login" />
          <Tab label="Register" />
        </Tabs>

        <Box component="form" onSubmit={handleSubmit} noValidate>
          <TextField
            margin="normal"
            required
            fullWidth
            id="username"
            label="Username"
            name="username"
            autoComplete="username"
            autoFocus
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="Password"
            type="password"
            id="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
          >
            {tabIndex === 0 ? 'Sign In' : 'Sign Up'}
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default LoginPage;