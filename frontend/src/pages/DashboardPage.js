import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '../services/apiService';
import BikeCard from '../components/BikeCard';
import { 
  Container, Grid, CircularProgress, Typography, Box 
} from '@mui/material';
import { useSnackbar } from 'notistack';
import { useAuth } from '../context/AuthContext';

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
      // No need to call fetchBikes() - WebSocket will handle it
    } catch (error) {
      const message = error.response?.data?.message || 'Booking failed';
      enqueueSnackbar(message, { variant: 'error' });
    }
  };

  const handleReturn = async (bikeId) => {
    try {
      await apiClient.post('/return', { bikeId });
      enqueueSnackbar('Bike returned successfully!', { variant: 'success' });
      // No need to call fetchBikes() - WebSocket will handle it
    } catch (error)
    {
      const message = error.response?.data?.message || 'Return failed. Is this your bike?';
      enqueueSnackbar(message, { variant: 'error' });
    }
  };

  useEffect(() => {
    // 1. Fetch the initial list
    fetchBikes(); 

    // 2. Open WebSocket connection
    const ws = new WebSocket('ws://localhost:8081');

    ws.onopen = () => {
      console.log('WebSocket connected');
    };

    // 3. This runs when the server broadcasts an update
    ws.onmessage = (event) => {
      const updatedBikes = JSON.parse(event.data);
      setBikes(updatedBikes); 
      console.log('Received WebSocket update, bike list refreshed.');
    };

    ws.onclose = () => {
      console.log('WebSocket disconnected');
    };

    // 4. This now shows the error in the snackbar
    ws.onerror = (err) => {
      console.error('WebSocket error:', err);
      // Only show the error if the component isn't in the
      // process of unmounting (which is what Strict Mode does)
      if (ws.readyState !== WebSocket.CLOSING && ws.readyState !== WebSocket.CLOSED) {
        enqueueSnackbar('Real-time connection failed.', { variant: 'error' });
      }
    };

    // --- THIS IS THE FIX ---
    // 5. Cleanup: Close the connection when the component unmounts
    return () => {
      // By setting the handlers to null before closing,
      // we prevent them from firing during the Strict Mode unmount/re-mount
      ws.onopen = null;
      ws.onclose = null;
      ws.onmessage = null;
      ws.onerror = null;
      ws.close();
    };
  }, [fetchBikes, enqueueSnackbar]); // Added dependencies

  // ... (rest of the file is the same) ...
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