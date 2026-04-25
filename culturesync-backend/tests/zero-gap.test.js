/**
 * zero-gap.test.js — Additional Route Logic Coverage
 * NOTE: Primary coverage is in techniques.test.js and leaderboard.test.js.
 * These tests are supplementary sanity checks.
 */
const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');

const app = require('../app');
const Technique = require('../src/models/Technique');
const User = require('../src/models/User');

let mongoServer;

beforeAll(async () => {
  process.env.JWT_SECRET = 'zero_gap_test_secret';
  mongoServer = await MongoMemoryServer.create();
  await mongoose.connect(mongoServer.getUri());
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  await Technique.deleteMany({});
  await User.deleteMany({});
});

describe('Zero-Gap Route Logic Analysis (Uncovering the 90%+ Path)', () => {

  describe('Techniques Route - Deep Logic Coverage', () => {
    it('GET /api/v1/techniques: Empty state handling', async () => {
      const res = await request(app).get('/api/v1/techniques');
      expect(res.status).toBe(200);
      expect(res.body.data).toEqual([]);
    });

    it('GET /api/v1/techniques: Sorted list with multiple difficulty levels', async () => {
      await Technique.create({ techniqueId: 't1', name: 'A', description: 'Desc A', difficulty: 'Advanced', durationMinutes: 10, isPublished: true });
      await Technique.create({ techniqueId: 't2', name: 'B', description: 'Desc B', difficulty: 'Beginner', durationMinutes: 5, isPublished: true });

      const res = await request(app).get('/api/v1/techniques');
      expect(res.status).toBe(200);
      expect(res.body.data.length).toBe(2);
      // Pedagogical sort: Beginner before Advanced
      expect(res.body.data[0].difficulty).toBe('Beginner');
      expect(res.body.data[1].difficulty).toBe('Advanced');
    });

    it('GET /api/v1/techniques/:id: Valid retrieval', async () => {
      await Technique.create({ techniqueId: 'target', name: 'T', description: 'D', difficulty: 'Beginner', durationMinutes: 5, isPublished: true });
      const res = await request(app).get('/api/v1/techniques/target');
      expect(res.status).toBe(200);
      expect(res.body.data.techniqueId).toBe('target');
    });
  });

  describe('Leaderboard Route - Logic Edge Cases', () => {
    it('GET /api/v1/leaderboard: Correct rank mapping for tied users', async () => {
      // Must set totalSessions > 0 so users appear in leaderboard query
      const hash = require('bcryptjs').hashSync('p', 10);
      await User.create({ name: 'A', email: 'a@t.c', passwordHash: hash, averageAccuracy: 90, totalSessions: 2 });
      await User.create({ name: 'B', email: 'b@t.c', passwordHash: hash, averageAccuracy: 90, totalSessions: 1 });

      const res = await request(app).get('/api/v1/leaderboard');
      expect(res.status).toBe(200);
      expect(res.body.data.length).toBe(2);
      expect(res.body.data[0].rank).toBe(1);
      expect(res.body.data[1].rank).toBe(2);
    });

    it('GET /api/v1/leaderboard: Excludes users with 0 sessions', async () => {
      const hash = require('bcryptjs').hashSync('p', 10);
      await User.create({ name: 'Active', email: 'active@t.c', passwordHash: hash, averageAccuracy: 80, totalSessions: 3 });
      await User.create({ name: 'Inactive', email: 'inactive@t.c', passwordHash: hash, averageAccuracy: 90, totalSessions: 0 });

      const res = await request(app).get('/api/v1/leaderboard');
      expect(res.status).toBe(200);
      expect(res.body.data.length).toBe(1);
      expect(res.body.data[0].userName).toBe('Active');
    });
  });
});
