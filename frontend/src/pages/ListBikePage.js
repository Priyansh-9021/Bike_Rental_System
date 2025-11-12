import React, { useState } from 'react';
import { Container, Paper, Typography, TextField, Button, Box } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useNavigate } from 'react-router-dom';
import apiClient from '../services/apiService';

const ListBikePage = () => {
    const [model, setModel] = useState('');
    const [location, setLocation] = useState('');
    const [modelYear, setModelYear] = useState('');
    const [rentRate, setRentRate] = useState('');
    const [contactNumber, setContactNumber] = useState('');
    const [photoUrl, setPhotoUrl] = useState('');

    const { enqueueSnackbar } = useSnackbar();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const bikeData = {
                model,
                location,
                modelYear: parseInt(modelYear),
                rentRate: parseFloat(rentRate),
                contactNumber,
                photoUrl
            };
            
            await apiClient.post('/list-bike', bikeData);
            enqueueSnackbar('Bike listed successfully!', { variant: 'success' });
            navigate('/my-bikes');
        } catch (error) {
            const message = error.response?.data?.message || 'Failed to list bike';
            enqueueSnackbar(message, { variant: 'error' });
        }
    };

    return (
        <Container component="main" maxWidth="sm" sx={{ mt: 8 }}>
            <Paper elevation={6} sx={{ p: 4 }}>
                <Typography component="h1" variant="h5" align="center">
                    List Your Bike
                </Typography>
                <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 3 }}>
                    <TextField
                        margin="normal" required fullWidth
                        label="Bike Model (e.g., Mountain Bike)"
                        value={model} onChange={(e) => setModel(e.target.value)}
                    />
                    <TextField
                        margin="normal" required fullWidth
                        label="Location (e.g., Alpha)"
                        value={location} onChange={(e) => setLocation(e.target.value)}
                    />
                    {/* --- NEW FIELDS --- */}
                    <TextField
                        margin="normal" required fullWidth
                        label="Model Year (e.g., 2023)"
                        type="number"
                        value={modelYear} onChange={(e) => setModelYear(e.target.value)}
                    />
                    <TextField
                        margin="normal" required fullWidth
                        label="Rent Rate (per day, e.g., 25.00)"
                        type="number"
                        value={rentRate} onChange={(e) => setRentRate(e.target.value)}
                    />
                    <TextField
                        margin="normal" required fullWidth
                        label="Contact Number"
                        type="tel"
                        value={contactNumber} onChange={(e) => setContactNumber(e.target.value)}
                    />
                    <TextField
                        margin="normal" required fullWidth
                        label="Photo URL (e.g., https://i.imgur.com/image.png)"
                        value={photoUrl} onChange={(e) => setPhotoUrl(e.target.value)}
                    />
                    
                    <Button
                        type="submit" fullWidth variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                    >
                        List My Bike
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
};

export default ListBikePage;