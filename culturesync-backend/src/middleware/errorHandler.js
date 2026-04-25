const logger = require('../utils/logger');

module.exports.errorHandler = (err, req, res, next) => {
  logger.error({ message: err.message, stack: err.stack, url: req.url, method: req.method });
  const status = err.status || 500;
  res.status(status).json({
    success: false,
    message: err.message || 'Internal server error',
  });
};
