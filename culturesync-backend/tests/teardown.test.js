const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const app = require('../app');
const User = require('../src/models/User');

let mongoServer;

describe('EXHAUSTIVE EDGE-CASE TEARDOWN (Zero Bypass Analytics)', () => {
  beforeAll(async () => {
    mongoServer = await MongoMemoryServer.create();
    await mongoose.connect(mongoServer.getUri());
    process.env.JWT_SECRET = 'teardown_secret';
  });

  afterAll(async () => {
    await mongoose.disconnect();
    await mongoServer.stop();
  });

  describe('Auth Edge Cases (Routes: 100% path coverage)', () => {
    it('Registration: Block missing name', async () => {
      const res = await request(app).post('/api/v1/auth/register').send({ email: 'e@t.c', password: 'p' });
      expect(res.status).toBe(400);
      expect(res.body.message).toBe('All fields required');
    });

    it('Registration: Block missing email', async () => {
      const res = await request(app).post('/api/v1/auth/register').send({ name: 'N', password: 'p' });
      expect(res.status).toBe(400);
    });

    it('Registration: Block missing password', async () => {
      const res = await request(app).post('/api/v1/auth/register').send({ name: 'N', email: 'e@t.c' });
      expect(res.status).toBe(400);
    });

    it('Registration: Block duplicate email (Route: L14)', async () => {
      await User.create({ name: 'First', email: 'dup@test.com', passwordHash: 'hash' });
      const res = await request(app).post('/api/v1/auth/register').send({ name: 'Second', email: 'dup@test.com', password: 'p' });
      expect(res.status).toBe(409);
      expect(res.body.message).toBe('Email already registered');
    });

    it('Login: Block non-existent email (Route: L34)', async () => {
      const res = await request(app).post('/api/v1/auth/login').send({ email: 'ghost@test.com', password: 'p' });
      expect(res.status).toBe(401); 
    });

    it('Login: Block incorrect password for active user', async () => {
       await request(app).post('/api/v1/auth/register').send({ name: 'P', email: 'pass@t.c', password: 'correct' });
       const res = await request(app).post('/api/v1/auth/login').send({ email: 'pass@t.c', password: 'WRONG' });
       expect(res.status).toBe(401);
       expect(res.body.message).toBe('Invalid email or password');
    });
  });

  describe('Session Sync Edge Cases (Accuracy & Boundary Logic)', () => {
    let token;
    beforeAll(async () => {
        const res = await request(app).post('/api/v1/auth/register').send({ name: 'S', email: 'session@e.x', password: 'p' });
        token = res.body.data.token;
    });

    it('Upload: Reject high accuracy (Score > 100 violation)', async () => {
      const res = await request(app).post('/api/v1/sessions').set('Authorization', `Bearer ${token}`)
        .send({ accuracyScore: 101, techniqueId: 't', frameCount: 1, durationSeconds: 1 });
      expect(res.status).toBe(400);
    });

    it('Upload: Reject negative accuracy (Score < 0 violation)', async () => {
      const res = await request(app).post('/api/v1/sessions').set('Authorization', `Bearer ${token}`)
        .send({ accuracyScore: -5, techniqueId: 't', frameCount: 1, durationSeconds: 1 });
      expect(res.status).toBe(400);
    });

    it('Upload: Reject empty techniqueId', async () => {
        const res = await request(app).post('/api/v1/sessions').set('Authorization', `Bearer ${token}`)
          .send({ accuracyScore: 90, frameCount: 1, durationSeconds: 1 });
        expect(res.status).toBe(400);
    });

    it('Upload: Reject negative frame counts', async () => {
        const res = await request(app).post('/api/v1/sessions').set('Authorization', `Bearer ${token}`)
          .send({ accuracyScore: 90, techniqueId: 't', frameCount: -1, durationSeconds: 1 });
        expect(res.status).toBe(400);
    });
  });

  describe('Leaderboard & Techniques (Route Failure Paths)', () => {
      it('GET /api/v1/leaderboard: Empty DB state handling', async () => {
          // Clear Users first to test empty fallback
          await User.deleteMany({});
          const res = await request(app).get('/api/v1/leaderboard');
          expect(res.status).toBe(200);
          expect(res.body.data.length).toBe(0);
      });

      it('GET /api/v1/techniques/:id: Mismatched ID retrieval', async () => {
          const res = await request(app).get('/api/v1/techniques/missing_999');
          expect(res.status).toBe(404);
      });
  });
});
