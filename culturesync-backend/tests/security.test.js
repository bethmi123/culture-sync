/**
 * security.test.js — Security Headers, CORS, Rate Limiting, Auth Enforcement
 * Covers: Helmet headers, CORS preflight, protected route enforcement, malformed JSON
 */
const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');

const app = require('../app');
const User = require('../src/models/User');

let mongoServer;

beforeAll(async () => {
  process.env.JWT_SECRET = 'security_test_secret';
  mongoServer = await MongoMemoryServer.create();
  await mongoose.connect(mongoServer.getUri());
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  await User.deleteMany({});
});

describe('Security Headers (Helmet)', () => {
  it('sets X-Frame-Options header', async () => {
    const res = await request(app).get('/health');
    expect(res.headers['x-frame-options']).toBeDefined();
  });

  it('sets X-DNS-Prefetch-Control header', async () => {
    const res = await request(app).get('/health');
    expect(res.headers['x-dns-prefetch-control']).toBeDefined();
  });

  it('sets X-Content-Type-Options header', async () => {
    const res = await request(app).get('/health');
    expect(res.headers['x-content-type-options']).toBeDefined();
  });

  it('removes X-Powered-By header', async () => {
    const res = await request(app).get('/health');
    expect(res.headers['x-powered-by']).toBeUndefined();
  });
});

describe('CORS Headers', () => {
  it('responds to CORS preflight OPTIONS request', async () => {
    const res = await request(app)
      .options('/api/v1/auth/login')
      .set('Origin', 'http://localhost:3000')
      .set('Access-Control-Request-Method', 'POST');
    expect(res.status).toBe(204);
  });

  it('includes Access-Control-Allow-Origin in preflight response', async () => {
    const res = await request(app)
      .options('/api/v1/auth/login')
      .set('Origin', 'http://localhost:3000')
      .set('Access-Control-Request-Method', 'POST');
    expect(res.headers['access-control-allow-origin']).toBeDefined();
  });

  it('includes CORS header on normal GET requests', async () => {
    const res = await request(app)
      .get('/health')
      .set('Origin', 'http://example.com');
    expect(res.headers['access-control-allow-origin']).toBeDefined();
  });
});

describe('Request Parsing', () => {
  it('rejects malformed JSON on login endpoint', async () => {
    const res = await request(app)
      .post('/api/v1/auth/login')
      .set('Content-Type', 'application/json')
      .send('{"malformed json here');
    expect(res.status).toBe(400);
  });

  it('rejects malformed JSON on register endpoint', async () => {
    const res = await request(app)
      .post('/api/v1/auth/register')
      .set('Content-Type', 'application/json')
      .send('{broken:json}');
    expect(res.status).toBe(400);
  });

  it('handles empty body as valid JSON (returns 400 from route, not parser)', async () => {
    const res = await request(app)
      .post('/api/v1/auth/login')
      .set('Content-Type', 'application/json')
      .send('{}');
    expect(res.status).toBe(400); // missing email/password, not parse error
  });
});

describe('Protected Route Enforcement', () => {
  it('POST /api/v1/sessions rejects request without token', async () => {
    const res = await request(app)
      .post('/api/v1/sessions')
      .send({ techniqueId: 't', accuracyScore: 80, frameCount: 1 });
    expect(res.status).toBe(401);
    expect(res.body.message).toBe('No token provided');
  });

  it('GET /api/v1/sessions/user/:id rejects request without token', async () => {
    const res = await request(app).get('/api/v1/sessions?userId=someUserId');
    expect(res.status).toBe(401);
    expect(res.body.message).toBe('No token provided');
  });

  it('protected routes reject expired JWT', async () => {
    const jwt = require('jsonwebtoken');
    const expiredToken = jwt.sign({ id: 'u1', role: 'learner' }, process.env.JWT_SECRET, { expiresIn: -1 });
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${expiredToken}`)
      .send({ techniqueId: 't', accuracyScore: 80, frameCount: 1 });
    expect(res.status).toBe(401);
    expect(res.body.message).toBe('Invalid or expired token');
  });

  it('protected routes reject tokens signed with wrong secret', async () => {
    const jwt = require('jsonwebtoken');
    const wrongToken = jwt.sign({ id: 'u2', role: 'learner' }, 'wrong_secret');
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${wrongToken}`)
      .send({ techniqueId: 't', accuracyScore: 80, frameCount: 1 });
    expect(res.status).toBe(401);
  });

  it('public routes do not require auth — GET /api/v1/techniques', async () => {
    const res = await request(app).get('/api/v1/techniques');
    expect(res.status).toBe(200);
  });

  it('public routes do not require auth — GET /api/v1/leaderboard', async () => {
    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.status).toBe(200);
  });
});

describe('Input Size Limits', () => {
  it('rejects payloads over 5MB', async () => {
    const largeBody = { data: 'x'.repeat(6 * 1024 * 1024) }; // 6MB
    const res = await request(app)
      .post('/api/v1/auth/register')
      .set('Content-Type', 'application/json')
      .send(largeBody);
    expect(res.status).toBe(413);
  });
});
