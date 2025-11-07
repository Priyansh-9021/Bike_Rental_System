import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '../services/apiService'; // <-- MODIFIED IMPORT
import { 
  Container, Grid, CircularProgress, Typography, Box, Paper, Chip 
} from '@mui/material';
import { useSnackbar } from 'notistack';

const MyBikesPage = () => {
  const [myBikes, setMyBikes] = useState([]);
  const [loading, setLoading] = useState(true);
  const { enqueueSnackbar } = useSnackbar();

  const fetchMyBikes = useCallback(async () => {
    try {
      setLoading(true);
      // --- MODIFIED ---
      // Use the apiClient directly
      const response = await apiClient.get('/my-bikes'); 
      setMyBikes(response.data);
    } catch (error) {
      enqueueSnackbar('Failed to fetch your bikes.', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  }, [enqueueSnackbar]);

  useEffect(() => {
    fetchMyBikes();
  }, [fetchMyBikes]);

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
        My Listed Bikes
      </Typography>
      {myBikes.length === 0 ? (
        <Typography>You have not listed any bikes yet.</Typography>
      ) : (
        <Grid container spacing={4}>
          {myBikes.map((bike) => (
            <Grid item key={bike.id} xs={12} sm={6} md={4}>
              <Paper elevation={3} sx={{ p: 2, height: '100%' }}>
                <Typography variant="h6">{bike.model}</Typography>
                <Typography>Location: {bike.location}</Typography>
                <Box sx={{ mt: 2 }}>
                  {bike.isAvailable ? (
                    <Chip label="Available for Rent" color="success" />
                  ) : (
                    <Chip 
                      label={`Booked by: ${bike.bookedBy}`} 
                      color="error" 
                    />
                  )}
                </Box>
              </Paper>
            </Grid>
          ))}
        </Grid>
      )}
    </Container>
  );
};

export default MyBikesPage;