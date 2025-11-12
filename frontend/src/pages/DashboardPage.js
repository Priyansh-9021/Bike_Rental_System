import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '../services/apiService';
import BikeCard from '../components/BikeCard';
import { 
  Container, Grid, CircularProgress, Typography, Box 
} from '@mui/material';
import { useSnackbar } from 'notistack';
import { useAuth } from '../context/AuthContext';

const serverIp = '10.89.34.36'; 

const DashboardPage = () => {
  const [bikes, setBikes] = useState([]);
  const [loading, setLoading] = useState(true);
  const { enqueueSnackbar } = useSnackbar();
  const { currentUser } = useAuth();

  const fetchBikes = useCallback(async () => {
    try {
      const response = await apiClient.get('/bikes');
      setBikes(response.data);
    } catch (error) {
      enqueueSnackbar('Failed to fetch bikes.', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  }, [enqueueSnackbar]);

  const handleBook = async (bikeId) => {
    try {
      await apiClient.post('/book', { bikeId });
      enqueueSnackbar('Bike booked successfully!', { variant: 'success' });

    } catch (error) {
      const message = error.response?.data?.message || 'Booking failed';
      enqueueSnackbar(message, { variant: 'error' });
    }
  };

  const handleReturn = async (bikeId) => {
    try {
      await apiClient.post('/return', { bikeId });
      enqueueSnackbar('Bike returned successfully!', { variant: 'success' });
    } catch (error)
    {
      const message = error.response?.data?.message || 'Return failed. Is this your bike?';
      enqueueSnackbar(message, { variant: 'error' });
    }
  };

  useEffect(() => {
    fetchBikes(); 

    const ws = new WebSocket(`ws://${serverIp}:8081`);

    ws.onopen = () => {
      console.log('WebSocket connected');
    };

    ws.onmessage = (event) => {
      const updatedBikes = JSON.parse(event.data);
      setBikes(updatedBikes); 
      console.log('Received WebSocket update, bike list refreshed.');
    };

    ws.onclose = () => {
      console.log('WebSocket disconnected');
    };

    ws.onerror = (err) => {
      console.error('WebSocket error:', err);
      if (ws.readyState !== WebSocket.CLOSING && ws.readyState !== WebSocket.CLOSED) {
        enqueueSnackbar('Real-time connection failed.', { variant: 'error' });
      }
    };

    return () => {
      ws.onopen = null;
      ws.onclose = null;
      ws.onmessage = null;
      ws.onerror = null;
      ws.close();
    };
  }, [fetchBikes, enqueueSnackbar]);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container sx={{ py: 4 }} maxWidth="lg">
      <Typography variant="h4" component="h1" gutterBottom>
        Available Bikes
      </Typography>
      
      <Grid container spacing={4}>
        {bikes.map((bike) => (
          <Grid key={bike.id} xs={12} sm={6} md={4}>
            <BikeCard 
              bike={bike}
              currentUser={currentUser}
              onBook={handleBook} 
              onReturn={handleReturn} 
            />
          </Grid>
        ))}
      </Grid>
      
    </Container>
  );
};

export default DashboardPage;