const express = require('express');
const path = require('path');
const app = express();
const PORT = 3001;

// Route for the login page
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'login.html'));
});

// Route for the dashboard
app.get('/dashboard', (req, res) => {
    res.sendFile(path.join(__dirname, 'manager-dashboard.html'));
});

// Serve static files
app.use(express.static(path.join(__dirname)));

app.listen(PORT, () => {
    console.log(`🎯 Frontend server running at http://localhost:${PORT}`);
    console.log(`🔗 Connected to Backend: http://localhost:8080/ipl-backend/api`);
});
