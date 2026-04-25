/**
 * integration.test.js — Full End-to-End Workflow Tests
 * Uses real MongoMemoryServer (no mocks). Exercises the entire API stack.
 * Covers: complete user journey, stats aggregation, cross-route consistency
 */
const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const jwt = require('jsonwebtoken');

const app = require('../app');
const User = require('../src/models/User');
const Session = require('../src/models/Session');
const Technique = require('../src/models/Technique');

let mongoServer;

const JWT_SECRET = 'integration_test_secret_culturesync';

beforeAll(async () => {
  process.env.JWT_SECRET = JWT_SECRET;
  mongoServer = await MongoMemoryServer.create();
  await mongoose.connect(mongoServer.getUri());
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  const cols = mongoose.connection.collections;
  for (const key in cols) await cols[key].deleteMany();
});

// ─── HELPERS ─────────────────────────────────────────────────────────────────

async function registerUser(name, email, password = 'TestPass123') {
  const res = await request(app).post('/api/v1/auth/register').send({ name, email, password });
  return { token: res.body.data?.token, user: res.body.data?.user, res };
}

async function uploadSession(token, overrides = {}) {
  return request(app)
    .post('/api/v1/sessions')
    .set('Authorization', `Bearer ${token}`)
    .send({
      techniqueId: 'beeralu_basic',
      techniqueName: 'Basic Crossing',
      startTime: Date.now() - 60000,
      endTime: Date.now(),
      durationSeconds: 60,
      accuracyScore: 80,
      frameCount: 150,
      ...overrides,
    });
}

// ─── COMPLETE USER JOURNEY ────────────────────────────────────────────────────

describe('Full User Journey — Register → Login → Practice → History', () => {
  it('registers, logs in, uploads sessions, and retrieves history in sequence', async () => {
    // 1. Register
    const { token, user } = await registerUser('Bethmi Artisan', 'bethmi@journey.lk');
    expect(token).toBeDefined();
    expect(user.id).toBeDefined();

    // 2. Verify user exists in DB
    const inDb = await User.findById(user.id);
    expect(inDb).not.toBeNull();
    expect(inDb.totalSessions).toBe(0);

    // 3. Login with same credentials
    const loginRes = await request(app).post('/api/v1/auth/login').send({
      email: 'bethmi@journey.lk',
      password: 'TestPass123',
    });
    expect(loginRes.status).toBe(200);
    const loginToken = loginRes.body.data.token;
    expect(jwt.verify(loginToken, JWT_SECRET).id).toBe(user.id);

    // 4. Upload first session
    const s1 = await uploadSession(token, { accuracyScore: 72, techniqueId: 'beeralu_basic' });
    expect(s1.status).toBe(201);
    expect(s1.body.data.accuracyScore).toBe(72);

    // 5. Upload second session with different technique
    const s2 = await uploadSession(token, { accuracyScore: 88, techniqueId: 'beeralu_cross', techniqueName: 'Cross Twist' });
    expect(s2.status).toBe(201);

    // 6. Verify DB stats updated correctly
    const stats = await User.findById(user.id);
    expect(stats.totalSessions).toBe(2);
    expect(stats.averageAccuracy).toBe(80); // (72+88)/2
    expect(stats.bestAccuracy).toBe(88);

    // 7. Retrieve session history
    const historyRes = await request(app)
      .get(`/api/v1/sessions?userId=${user.id}`)
      .set('Authorization', `Bearer ${token}`);
    expect(historyRes.status).toBe(200);
    expect(historyRes.body.data).toHaveLength(2);

    // 8. Sessions are returned newest first
    const first = historyRes.body.data[0];
    expect(first.techniqueId).toBe('beeralu_cross'); // last uploaded
  });
});

// ─── STATS AGGREGATION ACCURACY ──────────────────────────────────────────────

describe('User Stats Aggregation — Real Data Verification', () => {
  it('calculates averageAccuracy and bestAccuracy across multiple sessions', async () => {
    const { token, user } = await registerUser('Stats User', 'stats@test.lk');

    const scores = [60, 70, 80, 90, 100];
    for (const score of scores) {
      await uploadSession(token, { accuracyScore: score });
    }

    const u = await User.findById(user.id);
    expect(u.totalSessions).toBe(5);
    expect(u.bestAccuracy).toBe(100);
    // Average = (60+70+80+90+100)/5 = 80
    expect(u.averageAccuracy).toBe(80);
  });

  it('bestAccuracy only reflects the single highest session score', async () => {
    const { token, user } = await registerUser('Best User', 'best@test.lk');
    await uploadSession(token, { accuracyScore: 55 });
    await uploadSession(token, { accuracyScore: 99.5 });
    await uploadSession(token, { accuracyScore: 42 });

    const u = await User.findById(user.id);
    expect(u.bestAccuracy).toBe(99.5);
  });

  it('averageAccuracy is rounded to one decimal place', async () => {
    const { token, user } = await registerUser('Round User', 'round@test.lk');
    await uploadSession(token, { accuracyScore: 100 });
    await uploadSession(token, { accuracyScore: 0 });
    await uploadSession(token, { accuracyScore: 100 });

    const u = await User.findById(user.id);
    // (100+0+100)/3 = 66.666... → rounded to 66.7
    expect(u.averageAccuracy).toBeCloseTo(66.7, 1);
  });
});

// ─── LEADERBOARD INTEGRATION ──────────────────────────────────────────────────

describe('Leaderboard — Live Ranking from Real Sessions', () => {
  it('leaderboard reflects live session uploads without manual DB writes', async () => {
    const { token: t1, user: u1 } = await registerUser('Top Artisan', 'top@test.lk');
    const { token: t2, user: u2 } = await registerUser('Mid Artisan', 'mid@test.lk');
    const { token: t3, user: u3 } = await registerUser('Low Artisan', 'low@test.lk');

    // Upload sessions to set real averages
    await uploadSession(t1, { accuracyScore: 95 });
    await uploadSession(t1, { accuracyScore: 85 }); // avg = 90

    await uploadSession(t2, { accuracyScore: 75 }); // avg = 75

    await uploadSession(t3, { accuracyScore: 55 }); // avg = 55

    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.status).toBe(200);
    expect(res.body.data).toHaveLength(3);
    expect(res.body.data[0].userName).toBe('Top Artisan');
    expect(res.body.data[0].rank).toBe(1);
    expect(res.body.data[1].userName).toBe('Mid Artisan');
    expect(res.body.data[2].userName).toBe('Low Artisan');
    expect(res.body.data[2].rank).toBe(3);
  });

  it('new user with 0 sessions does not appear on leaderboard', async () => {
    const { token: t1 } = await registerUser('Active', 'active@test.lk');
    await registerUser('Inactive', 'inactive@test.lk'); // no sessions

    await uploadSession(t1, { accuracyScore: 80 });

    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.body.data).toHaveLength(1);
    expect(res.body.data[0].userName).toBe('Active');
  });
});

// ─── TECHNIQUES + SESSIONS CROSS-ROUTE ────────────────────────────────────────

describe('Techniques + Sessions Cross-Route Consistency', () => {
  it('uploads session for a technique and retrieves both independently', async () => {
    // Seed technique
    await Technique.create({
      techniqueId: 'beeralu_flower',
      name: 'Flower Motif',
      description: 'Advanced floral beeralu pattern',
      difficulty: 'Advanced',
      durationMinutes: 30,
      videoUrl: 'https://cdn.beeralu.app/flower.mp4',
    });

    const { token, user } = await registerUser('Artisan', 'artisan@test.lk');
    const sessionRes = await uploadSession(token, { techniqueId: 'beeralu_flower', techniqueName: 'Flower Motif', accuracyScore: 91 });
    expect(sessionRes.status).toBe(201);

    // Technique is still independently retrievable
    const techRes = await request(app).get('/api/v1/techniques/beeralu_flower');
    expect(techRes.status).toBe(200);
    expect(techRes.body.data.name).toBe('Flower Motif');

    // Session history shows the correct technique
    const histRes = await request(app)
      .get(`/api/v1/sessions?userId=${user.id}`)
      .set('Authorization', `Bearer ${token}`);
    expect(histRes.body.data[0].techniqueId).toBe('beeralu_flower');
    expect(histRes.body.data[0].accuracyScore).toBe(91);
  });
});

// ─── MULTI-USER ISOLATION ─────────────────────────────────────────────────────

describe('Multi-User Data Isolation', () => {
  it('user A cannot see user B sessions', async () => {
    const { token: tA, user: uA } = await registerUser('User A', 'a@iso.lk');
    const { token: tB, user: uB } = await registerUser('User B', 'b@iso.lk');

    await uploadSession(tA, { accuracyScore: 88 });
    await uploadSession(tB, { accuracyScore: 77 });

    const resA = await request(app)
      .get(`/api/v1/sessions?userId=${uA.id}`)
      .set('Authorization', `Bearer ${tA}`);
    expect(resA.body.data).toHaveLength(1);
    expect(resA.body.data[0].accuracyScore).toBe(88);

    const resB = await request(app)
      .get(`/api/v1/sessions?userId=${uB.id}`)
      .set('Authorization', `Bearer ${tB}`);
    expect(resB.body.data).toHaveLength(1);
    expect(resB.body.data[0].accuracyScore).toBe(77);
  });

  it('stats updates for user A do not affect user B', async () => {
    const { token: tA, user: uA } = await registerUser('Stat A', 'stata@test.lk');
    const { token: tB, user: uB } = await registerUser('Stat B', 'statb@test.lk');

    await uploadSession(tA, { accuracyScore: 100 });
    await uploadSession(tA, { accuracyScore: 50 });

    const uAStats = await User.findById(uA.id);
    const uBStats = await User.findById(uB.id);

    expect(uAStats.totalSessions).toBe(2);
    expect(uBStats.totalSessions).toBe(0); // untouched
    expect(uBStats.averageAccuracy).toBe(0);
  });
});

// ─── EDGE CASES IN FULL STACK ─────────────────────────────────────────────────

describe('Edge Cases — Full Stack', () => {
  it('cannot register same email twice', async () => {
    await registerUser('First', 'dup@test.lk');
    const { res } = await registerUser('Second', 'dup@test.lk');
    expect(res.status).toBe(409);
  });

  it('token from registration works immediately for session upload', async () => {
    const { token } = await registerUser('Immediate', 'imm@test.lk');
    const sessionRes = await uploadSession(token, { accuracyScore: 77 });
    expect(sessionRes.status).toBe(201);
  });

  it('session history is empty for brand new user', async () => {
    const { token, user } = await registerUser('Fresh', 'fresh@test.lk');
    const res = await request(app)
      .get(`/api/v1/sessions?userId=${user.id}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.body.data).toHaveLength(0);
  });

  it('50 session uploads for one user return max 50 in history', async () => {
    const { token, user } = await registerUser('Heavy', 'heavy@test.lk');
    for (let i = 0; i < 55; i++) {
      await uploadSession(token, { accuracyScore: i });
    }
    const res = await request(app)
      .get(`/api/v1/sessions?userId=${user.id}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.body.data.length).toBeLessThanOrEqual(50);
  });
});
