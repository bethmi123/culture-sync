/**
 * errors.test.js — Global Error Handler Middleware Tests
 * Covers: default 500, custom status codes, custom messages, fallback message
 */
jest.mock('../src/utils/logger', () => ({ error: jest.fn() }));
const logger = require('../src/utils/logger');
const { errorHandler } = require('../src/middleware/errorHandler');

describe('Global Error Handler — All Branches', () => {
  let req, res, next;

  beforeEach(() => {
    req = { url: '/test', method: 'GET' };
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn(),
    };
    next = jest.fn();
    logger.error.mockClear();
  });

  it('returns HTTP 500 by default when no error status is set', () => {
    const err = new Error('Something exploded');
    errorHandler(err, req, res, next);
    expect(res.status).toHaveBeenCalledWith(500);
  });

  it('returns the error message from the Error object', () => {
    const err = new Error('Something exploded');
    errorHandler(err, req, res, next);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({
      success: false,
      message: 'Something exploded',
    }));
  });

  it('uses a custom status code from err.status', () => {
    const err = new Error('Forbidden');
    err.status = 403;
    errorHandler(err, req, res, next);
    expect(res.status).toHaveBeenCalledWith(403);
  });

  it('uses err.status = 400 for bad request errors', () => {
    const err = new Error('Bad input');
    err.status = 400;
    errorHandler(err, req, res, next);
    expect(res.status).toHaveBeenCalledWith(400);
  });

  it('uses err.status = 404 for not found errors', () => {
    const err = new Error('Not found');
    err.status = 404;
    errorHandler(err, req, res, next);
    expect(res.status).toHaveBeenCalledWith(404);
  });

  it('uses err.status = 401 for unauthorized errors', () => {
    const err = new Error('Unauthorized');
    err.status = 401;
    errorHandler(err, req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
  });

  it('falls back to "Internal server error" when err.message is empty', () => {
    const err = new Error();
    errorHandler(err, req, res, next);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({
      message: 'Internal server error',
    }));
  });

  it('response always has success: false', () => {
    const err = new Error('Any error');
    errorHandler(err, req, res, next);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ success: false }));
  });

  it('logs the error via logger.error with message, stack, url, and method', () => {
    const err = new Error('Trace me');
    errorHandler(err, req, res, next);
    expect(logger.error).toHaveBeenCalledWith(expect.objectContaining({
      message: 'Trace me',
      url: '/test',
      method: 'GET',
    }));
  });
});
