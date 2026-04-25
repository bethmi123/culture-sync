/**
 * edge-cases.test.js — Supplementary Edge Case Tests
 * NOTE: Comprehensive coverage is in auth.test.js, sessions.test.js,
 * techniques.test.js, leaderboard.test.js. These are additional route sanity checks.
 */
const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const jwt = require('jsonwebtoken');

const app = require('../app');
const User = require('../src/models/User');
const Technique = require('../src/models/Technique');

let mongoServer;

beforeAll(async () => {
  process.env.JWT_SECRET = 'edge_case_test_secret';
  mongoServer = await MongoMemoryServer.create();
  await mongoose.connect(mongoServer.getUri());
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  await User.deleteMany({});
  await Technique.deleteMany({});
});

describe('Edge Case Testing (For 90%+ Coverage)', () => {

  describe('Backend Route Logic Integrity', () => {
    it('should return 404 for invalid routes', async () => {
      const res = await request(app).get('/api/v1/invalid-resource-not-found');
      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
    });

    it('should return 400 for malformed session uploads (accuracyScore > 100)', async () => {
      const hash = require('bcryptjs').hashSync('pass', 10);
      const u = await User.create({ name: 'fail', email: 'fail@test.com', passwordHash: hash });
      const token = jwt.sign({ id: u._id, role: 'learner' }, process.env.JWT_SECRET);

      const res = await request(app)
        .post('/api/v1/sessions')
        .set('Authorization', `Bearer ${token}`)
        .send({ accuracyScore: 101, techniqueId: 'beeralu_basic', frameCount: 1 });

      expect(res.status).toBe(400);
      expect(res.body.message).toContain('accuracyScore');
    }, 15000);

    it('should return 404 for techniques that do not exist', async () => {
      // DB is connected so Mongoose executes the query and returns null → 404
      const res = await request(app).get('/api/v1/techniques/does_not_exist_technique_id');
      expect(res.status).toBe(404);
      expect(res.body.success).toBe(false);
    }, 15000);
  });
});
