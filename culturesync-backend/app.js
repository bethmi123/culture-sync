const express    = require('express');
const cors       = require('cors');
const helmet     = require('helmet');
const morgan     = require('morgan');
const rateLimit  = require('express-rate-limit');
const { errorHandler } = require('./src/middleware/errorHandler');

const app = express();

app.use(helmet());
app.use(cors({ origin: process.env.CORS_ORIGIN || '*' }));

if (process.env.NODE_ENV !== 'test') {
  app.use(rateLimit({ windowMs: 15 * 60 * 1000, max: 100 }));  // PRD: max 100
  app.use(morgan('dev'));
}

app.use(express.json({ limit: '5mb' }));

app.get('/health', (_, res) => res.json({ status: 'ok', app: 'CultureSync API', version: '1.0.0' }));

const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const swaggerDocument = YAML.load('./swagger.yaml');
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

app.use('/api/v1/auth',        require('./src/routes/auth'));
app.use('/api/v1/users',       require('./src/routes/users'));
app.use('/api/v1/sessions',    require('./src/routes/sessions'));
app.use('/api/v1/techniques',  require('./src/routes/techniques'));
app.use('/api/v1/leaderboard', require('./src/routes/leaderboard'));
app.use('/api/v1/sync',        require('./src/routes/sync'));

app.use((req, res) => res.status(404).json({ success: false, message: 'Route not found' }));
app.use(errorHandler);

module.exports = app;
