/**
 * middleware.test.js — Auth Middleware Comprehensive Tests
 * Covers: valid token, missing header, wrong scheme, invalid token,
 *         expired token, admin role, userId injection
 */
const jwt = require('jsonwebtoken');
const authMiddleware = require('../src/middleware/auth');

const SECRET = 'middleware_test_secret_2024';

describe('Auth Middleware — All Paths', () => {
  let req, res, next;

  beforeEach(() => {
    process.env.JWT_SECRET = SECRET;
    req = { headers: {} };
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn(),
    };
    next = jest.fn();
  });

  // ─── HAPPY PATHS ──────────────────────────────────────────────────────────

  it('calls next() for a valid learner token', () => {
    const token = jwt.sign({ id: 'user123', role: 'learner' }, SECRET);
    req.headers.authorization = `Bearer ${token}`;
    authMiddleware(req, res, next);
    expect(next).toHaveBeenCalledTimes(1);
    expect(req.userId).toBe('user123');
    expect(req.userRole).toBe('learner');
  });

  it('calls next() for a valid admin token', () => {
    const token = jwt.sign({ id: 'admin456', role: 'admin' }, SECRET);
    req.headers.authorization = `Bearer ${token}`;
    authMiddleware(req, res, next);
    expect(next).toHaveBeenCalledTimes(1);
    expect(req.userId).toBe('admin456');
    expect(req.userRole).toBe('admin');
  });

  it('correctly sets req.userId from token payload', () => {
    const token = jwt.sign({ id: 'specific_id_789', role: 'learner' }, SECRET);
    req.headers.authorization = `Bearer ${token}`;
    authMiddleware(req, res, next);
    expect(req.userId).toBe('specific_id_789');
  });

  it('correctly sets req.userRole from token payload', () => {
    const token = jwt.sign({ id: 'u1', role: 'admin' }, SECRET);
    req.headers.authorization = `Bearer ${token}`;
    authMiddleware(req, res, next);
    expect(req.userRole).toBe('admin');
  });

  it('accepts a token with expiresIn set in the future', () => {
    const token = jwt.sign({ id: 'u2', role: 'learner' }, SECRET, { expiresIn: '30d' });
    req.headers.authorization = `Bearer ${token}`;
    authMiddleware(req, res, next);
    expect(next).toHaveBeenCalled();
  });

  // ─── MISSING / MALFORMED HEADER ───────────────────────────────────────────

  it('returns 401 when Authorization header is absent', () => {
    authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({
      success: false,
      message: 'No token provided',
    }));
    expect(next).not.toHaveBeenCalled();
  });

  it('returns 401 when Authorization header is an empty string', () => {
    req.headers.authorization = '';
    authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ message: 'No token provided' }));
  });

  it('returns 401 when scheme is Basic instead of Bearer', () => {
    const token = jwt.sign({ id: 'u3', role: 'learner' }, SECRET);
    req.headers.authorization = `Basic ${token}`;
    authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({ message: 'No token provided' }));
  });

  it('returns 401 when Authorization is just "Bearer" with no token', () => {
    req.headers.authorization = 'Bearer ';
    // An empty token string is still after "Bearer ", but jwt.verify will throw
    authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
  });

  // ─── INVALID TOKENS ───────────────────────────────────────────────────────

  it('returns 401 for a completely invalid token string', () => {
    req.headers.authorization = 'Bearer not.a.valid.token';
    authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({
      message: 'Invalid or expired token',
    }));
    expect(next).not.toHaveBeenCalled();
  });

  it('returns 401 for a token signed with a different secret', () => {
    const token = jwt.sign({ id: 'u4', role: 'learner' }, 'completely_different_secret');
    req.headers.authorization = `Bearer ${token}`;
    authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({
      message: 'Invalid or expired token',
    }));
  });

  it('returns 401 for a tampered token payload', () => {
    const token = jwt.sign({ id: 'u5', role: 'learner' }, SECRET);
    const parts = token.split('.');
    // Tamper the payload
    const tamperedPayload = Buffer.from(JSON.stringify({ id: 'hacker', role: 'admin' })).toString('base64url');
    const tamperedToken = `${parts[0]}.${tamperedPayload}.${parts[2]}`;
    req.headers.authorization = `Bearer ${tamperedToken}`;
    authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
  });

  it('returns 401 for an expired token', () => {
    const token = jwt.sign({ id: 'u6', role: 'learner' }, SECRET, { expiresIn: -1 });
    req.headers.authorization = `Bearer ${token}`;
    authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith(expect.objectContaining({
      message: 'Invalid or expired token',
    }));
  });

  it('returns 401 for a random string that looks like base64', () => {
    req.headers.authorization = 'Bearer aGVsbG8ud29ybGQ.dGVzdA';
    authMiddleware(req, res, next);
    expect(res.status).toHaveBeenCalledWith(401);
  });

  // ─── DOES NOT CALL NEXT ON FAILURE ────────────────────────────────────────

  it('does not call next() when token is invalid', () => {
    req.headers.authorization = 'Bearer bad_token';
    authMiddleware(req, res, next);
    expect(next).not.toHaveBeenCalled();
  });

  it('does not call next() when header is missing', () => {
    authMiddleware(req, res, next);
    expect(next).not.toHaveBeenCalled();
  });
});
