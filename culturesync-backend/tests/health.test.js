/**
 * health.test.js — Health Check & 404 Fallback Routes
 */
const request = require('supertest');
const app = require('../app');

describe('Health Check & Infrastructure Routes', () => {
  it('GET /health returns status ok', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
    expect(res.body.app).toBe('CultureSync API');
    expect(res.body.version).toBe('1.0.0');
  });

  it('GET /health responds quickly (basic liveness probe)', async () => {
    const start = Date.now();
    await request(app).get('/health');
    expect(Date.now() - start).toBeLessThan(500);
  });

  it('returns 404 for completely unknown routes', async () => {
    const res = await request(app).get('/api/v1/this-does-not-exist');
    expect(res.status).toBe(404);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toBe('Route not found');
  });

  it('returns 404 for unknown POST routes', async () => {
    const res = await request(app).post('/api/v1/nonexistent');
    expect(res.status).toBe(404);
    expect(res.body.success).toBe(false);
  });

  it('returns 404 for unknown nested routes', async () => {
    const res = await request(app).get('/api/v1/auth/unknown-endpoint');
    expect(res.status).toBe(404);
  });

  it('GET /health returns JSON content-type', async () => {
    const res = await request(app).get('/health');
    expect(res.headers['content-type']).toMatch(/application\/json/);
  });
});
