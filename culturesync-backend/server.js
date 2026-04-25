require('dotenv').config();
const mongoose = require('mongoose');
const app = require('./app');

const PORT = process.env.PORT || 3000;
const MONGODB_URI = process.env.MONGODB_URI;
const JWT_SECRET  = process.env.JWT_SECRET;

if (!MONGODB_URI) { console.error('FATAL: MONGODB_URI not set'); process.exit(1); }
if (!JWT_SECRET)  { console.error('FATAL: JWT_SECRET not set');  process.exit(1); }

mongoose.connect(MONGODB_URI)
  .then(() => {
    console.log('✅ MongoDB connected');
    app.listen(PORT, () =>
      console.log(`🚀 CultureSync API running on http://localhost:${PORT}`)
    );
  })
  .catch((err) => {
    console.error('MongoDB connection failed:', err.message);
    process.exit(1);
  });
