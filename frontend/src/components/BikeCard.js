import React, { useState } from 'react';
import { 
  Card, CardContent, CardActions, Typography, Button, Box, Chip, CircularProgress, 
  CardMedia 
} from '@mui/material';

const BikeCard = ({ bike, currentUser, onBook, onReturn }) => {
  const [loading, setLoading] = useState(false);

  const handleBookClick = async () => {
    setLoading(true);
    await onBook(bike.id);
    setLoading(false);
  };

  const handleReturnClick = async () => {
    setLoading(true);
    await onReturn(bike.id);
    setLoading(false);
  };

  const renderStatus = () => {
    if (bike.isAvailable) {
      return (
        <>
          <Chip label="Available" color="success" />
          <Button 
            size="small" variant="contained" 
            onClick={handleBookClick} disabled={loading}
            sx={{ ml: 'auto' }}
          >
            Book
          </Button>
        </>
      );
    }
    if (bike.bookedBy === currentUser) {
      return (
        <>
          <Chip label="Booked by You" color="primary" />
          <Button 
            size="small" variant="outlined" 
            onClick={handleReturnClick} disabled={loading}
            sx={{ ml: 'auto' }}
          >
            Return
          </Button>
        </>
      );
    }
    return (
      <Chip label="Booked" color="error" variant="outlined" />
    );
  };

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      
      {/* --- BIKE IMAGE --- */}
      <CardMedia
        component="img"
        height="160"
        image={bike.photoUrl || "https://i.imgur.com/g8fVwGr.png"} 
        alt={bike.model}
      />

      <CardContent sx={{ flexGrow: 1, position: 'relative' }}>
        {/* Loading Spinner Overlay */}
        {loading && (
          <Box sx={{
              position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              backgroundColor: 'rgba(255, 255, 255, 0.7)', zIndex: 10
          }}>
            <CircularProgress size={24} />
          </Box>
        )}

        {/* --- UPDATED CARD INFO --- */}
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography gutterBottom variant="h5" component="h2">
            {bike.model}
          </Typography>
          <Typography variant="h6" color="primary">
            Rs.{bike.rentRate}/day
          </Typography>
        </Box>
        <Typography color="text.secondary">
          Year: {bike.modelYear}
        </Typography>
        <Typography color="text.secondary">
          Location: {bike.location}
        </Typography>
        <Typography color="text.secondary" sx={{ fontSize: '0.9rem' }}>
          Owner: {bike.owner} (Contact: {bike.contactNumber})
        </Typography>
      </CardContent>

      <CardActions sx={{ p: 2, display: 'flex', justifyContent: 'flex-start' }}>
        {renderStatus()}
      </CardActions>
    </Card>
  );
};

export default BikeCard;