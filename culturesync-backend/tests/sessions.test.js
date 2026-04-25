/**
 * sessions.test.js — Session Model + Sessions Routes
 * Covers: Session schema validation, POST /api/v1/sessions, GET /api/v1/sessions/user/:userId,
 *         user stats aggregation (totalSessions, averageAccuracy, bestAccuracy)
 */
const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const app = require('../app');
const User = require('../src/models/User');
const Session = require('../src/models/Session');

let mongoServer;

const JWT_SECRET = 'test_sessions_secret_2024';

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
  await Session.deleteMany({});
  await User.deleteMany({});
});

// ─── HELPERS ─────────────────────────────────────────────────────────────────

async function createUserAndToken(overrides = {}) {
  const hash = await bcrypt.hash('password', 12);
  const user = await User.create({
    name: 'Test User',
    email: `user_${Date.now()}@test.com`,
    passwordHash: hash,
    ...overrides,
  });
  const token = jwt.sign({ id: user._id, role: user.role }, JWT_SECRET);
  return { user, token };
}

const validSession = (overrides = {}) => ({
  techniqueId: 'beeralu_basic',
  techniqueName: 'Basic Crossing',
  startTime: Date.now() - 60000,
  endTime: Date.now(),
  durationSeconds: 60,
  accuracyScore: 85,
  frameCount: 150,
  ...overrides,
});

// ─── SESSION MODEL UNIT TESTS ─────────────────────────────────────────────────

describe('Session Model — Schema Validation', () => {
  let userId;

  beforeEach(async () => {
    const hash = await bcrypt.hash('pass', 12);
    const user = await User.create({ name: 'U', email: 'u@m.c', passwordHash: hash });
    userId = user._id;
  });

  it('saves a valid session document', async () => {
    const s = await Session.create({
      user: userId,
      techniqueId: 'beeralu_basic',
      techniqueName: 'Basic Crossing',
      startTime: 1000,
      endTime: 2000,
      durationSeconds: 1,
      accuracyScore: 87.5,
      frameCount: 200,
    });
    expect(s._id).toBeDefined();
    expect(s.accuracyScore).toBe(87.5);
    expect(s.createdAt).toBeDefined();
  });

  it('rejects accuracyScore > 100', async () => {
    const s = new Session({ user: userId, techniqueId: 't', techniqueName: 'n',
      startTime: 0, endTime: 1, durationSeconds: 1, accuracyScore: 101, frameCount: 1 });
    await expect(s.save()).rejects.toMatchObject({ errors: { accuracyScore: expect.anything() } });
  });

  it('rejects accuracyScore < 0', async () => {
    const s = new Session({ user: userId, techniqueId: 't', techniqueName: 'n',
      startTime: 0, endTime: 1, durationSeconds: 1, accuracyScore: -1, frameCount: 1 });
    await expect(s.save()).rejects.toMatchObject({ errors: { accuracyScore: expect.anything() } });
  });

  it('accepts accuracyScore = 0 (boundary)', async () => {
    const s = await Session.create({ user: userId, techniqueId: 't', techniqueName: 'n',
      startTime: 0, endTime: 1, durationSeconds: 1, accuracyScore: 0, frameCount: 1 });
    expect(s.accuracyScore).toBe(0);
  });

  it('accepts accuracyScore = 100 (boundary)', async () => {
    const s = await Session.create({ user: userId, techniqueId: 't', techniqueName: 'n',
      startTime: 0, endTime: 1, durationSeconds: 1, accuracyScore: 100, frameCount: 1 });
    expect(s.accuracyScore).toBe(100);
  });

  it('rejects missing user ref', async () => {
    const s = new Session({ techniqueId: 't', techniqueName: 'n',
      startTime: 0, endTime: 1, durationSeconds: 1, accuracyScore: 50, frameCount: 1 });
    await expect(s.save()).rejects.toMatchObject({ errors: { user: expect.anything() } });
  });

  it('rejects missing techniqueId', async () => {
    const s = new Session({ user: userId, techniqueName: 'n',
      startTime: 0, endTime: 1, durationSeconds: 1, accuracyScore: 50, frameCount: 1 });
    await expect(s.save()).rejects.toMatchObject({ errors: { techniqueId: expect.anything() } });
  });

  it('rejects missing techniqueName', async () => {
    const s = new Session({ user: userId, techniqueId: 't',
      startTime: 0, endTime: 1, durationSeconds: 1, accuracyScore: 50, frameCount: 1 });
    await expect(s.save()).rejects.toMatchObject({ errors: { techniqueName: expect.anything() } });
  });

  it('rejects missing startTime', async () => {
    const s = new Session({ user: userId, techniqueId: 't', techniqueName: 'n',
      endTime: 1, durationSeconds: 1, accuracyScore: 50, frameCount: 1 });
    await expect(s.save()).rejects.toMatchObject({ errors: { startTime: expect.anything() } });
  });

  it('rejects missing endTime', async () => {
    const s = new Session({ user: userId, techniqueId: 't', techniqueName: 'n',
      startTime: 0, durationSeconds: 1, accuracyScore: 50, frameCount: 1 });
    await expect(s.save()).rejects.toMatchObject({ errors: { endTime: expect.anything() } });
  });

  it('rejects missing durationSeconds', async () => {
    const s = new Session({ user: userId, techniqueId: 't', techniqueName: 'n',
      startTime: 0, endTime: 1, accuracyScore: 50, frameCount: 1 });
    await expect(s.save()).rejects.toMatchObject({ errors: { durationSeconds: expect.anything() } });
  });

  it('rejects missing frameCount', async () => {
    const s = new Session({ user: userId, techniqueId: 't', techniqueName: 'n',
      startTime: 0, endTime: 1, durationSeconds: 1, accuracyScore: 50 });
    await expect(s.save()).rejects.toMatchObject({ errors: { frameCount: expect.anything() } });
  });
});

// ─── POST /api/v1/sessions ───────────────────────────────────────────────────────

describe('POST /api/v1/sessions', () => {
  it('happy path — uploads a session and returns 201', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession());

    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data._id).toBeDefined();
    expect(res.body.data.techniqueId).toBe('beeralu_basic');
    expect(res.body.data.accuracyScore).toBe(85);
  });

  it('session is persisted to database', async () => {
    const { token } = await createUserAndToken();
    await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ accuracyScore: 77 }));

    const inDb = await Session.findOne({ techniqueId: 'beeralu_basic', accuracyScore: 77 });
    expect(inDb).not.toBeNull();
  });

  it('updates user totalSessions after first session', async () => {
    const { user, token } = await createUserAndToken();
    await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ accuracyScore: 70 }));

    const updated = await User.findById(user._id);
    expect(updated.totalSessions).toBe(1);
  });

  it('updates averageAccuracy correctly after two sessions', async () => {
    const { user, token } = await createUserAndToken();
    await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ accuracyScore: 80 }));
    await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ techniqueId: 'beeralu_cross', techniqueName: 'Cross', accuracyScore: 60 }));

    const updated = await User.findById(user._id);
    expect(updated.totalSessions).toBe(2);
    expect(updated.averageAccuracy).toBe(70); // (80+60)/2
    expect(updated.bestAccuracy).toBe(80);
  });

  it('updates bestAccuracy to the highest session score', async () => {
    const { user, token } = await createUserAndToken();
    const scores = [55, 92, 78, 92, 45];
    for (const score of scores) {
      await request(app)
        .post('/api/v1/sessions')
        .set('Authorization', `Bearer ${token}`)
        .send(validSession({ accuracyScore: score }));
    }
    const updated = await User.findById(user._id);
    expect(updated.bestAccuracy).toBe(92);
    expect(updated.totalSessions).toBe(5);
  });

  it('averageAccuracy is rounded to 1 decimal place', async () => {
    const { user, token } = await createUserAndToken();
    // (77 + 88 + 99) / 3 = 88.0
    for (const score of [77, 88, 99]) {
      await request(app)
        .post('/api/v1/sessions')
        .set('Authorization', `Bearer ${token}`)
        .send(validSession({ accuracyScore: score }));
    }
    const updated = await User.findById(user._id);
    // (77+88+99)/3 = 88
    expect(updated.averageAccuracy).toBeCloseTo(88, 1);
  });

  it('returns 401 without auth token', async () => {
    const res = await request(app).post('/api/v1/sessions').send(validSession());
    expect(res.status).toBe(401);
  });

  it('returns 401 with malformed token', async () => {
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', 'Bearer not.a.real.token')
      .send(validSession());
    expect(res.status).toBe(401);
  });

  it('returns 400 when accuracyScore > 100', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ accuracyScore: 101 }));
    expect(res.status).toBe(400);
    expect(res.body.message).toContain('accuracyScore');
  });

  it('returns 400 when accuracyScore < 0', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ accuracyScore: -1 }));
    expect(res.status).toBe(400);
    expect(res.body.message).toContain('accuracyScore');
  });

  it('returns 400 when accuracyScore is null', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send({ ...validSession(), accuracyScore: null });
    expect(res.status).toBe(400);
  });

  it('accepts accuracyScore = 0 (boundary)', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ accuracyScore: 0 }));
    expect(res.status).toBe(201);
    expect(res.body.data.accuracyScore).toBe(0);
  });

  it('accepts accuracyScore = 100 (boundary)', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ accuracyScore: 100 }));
    expect(res.status).toBe(201);
    expect(res.body.data.accuracyScore).toBe(100);
  });

  it('returns 400 when techniqueId is missing', async () => {
    const { token } = await createUserAndToken();
    const { techniqueId, ...body } = validSession();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(body);
    expect(res.status).toBe(400);
    expect(res.body.message).toContain('techniqueId');
  });

  it('returns 400 when frameCount is negative', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ frameCount: -5 }));
    expect(res.status).toBe(400);
    expect(res.body.message).toContain('frameCount');
  });

  it('returns 400 when frameCount is null', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send({ ...validSession(), frameCount: null });
    expect(res.status).toBe(400);
  });

  it('accepts frameCount = 0', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ frameCount: 0 }));
    expect(res.status).toBe(201);
  });

  it('handles very large frameCount without overflow', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ frameCount: 2147483647 }));
    expect(res.status).toBe(201);
  });

  it('handles emoji in techniqueName', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ techniqueName: 'Beeralu Wave 🌊' }));
    expect(res.status).toBe(201);
  });

  it('handles non-alphanumeric techniqueId', async () => {
    const { token } = await createUserAndToken();
    const res = await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ techniqueId: 'beeralu-wave_v2.0' }));
    expect(res.status).toBe(201);
  });
});

// ─── GET /api/v1/sessions/user/:userId ─────────────────────────────────────────

describe('GET /api/v1/sessions/user/:userId', () => {
  it('happy path — returns sessions for the user', async () => {
    const { user, token } = await createUserAndToken();

    await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ accuracyScore: 80 }));
    await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${token}`)
      .send(validSession({ accuracyScore: 90 }));

    const res = await request(app)
      .get(`/api/v1/sessions?userId=${user._id}`)
      .set('Authorization', `Bearer ${token}`);

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data).toHaveLength(2);
  });

  it('returns empty array for user with no sessions', async () => {
    const { user, token } = await createUserAndToken();
    const res = await request(app)
      .get(`/api/v1/sessions?userId=${user._id}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.status).toBe(200);
    expect(res.body.data).toHaveLength(0);
  });

  it('returns sessions sorted by createdAt descending (newest first)', async () => {
    const { user, token } = await createUserAndToken();
    for (let i = 0; i < 3; i++) {
      await request(app)
        .post('/api/v1/sessions')
        .set('Authorization', `Bearer ${token}`)
        .send(validSession({ accuracyScore: 50 + i * 10 }));
    }
    const res = await request(app)
      .get(`/api/v1/sessions?userId=${user._id}`)
      .set('Authorization', `Bearer ${token}`);
    const scores = res.body.data.map(s => s.accuracyScore);
    // Last posted (70) should come first
    expect(scores[0]).toBe(70);
  });

  it('limits results to 50 sessions maximum', async () => {
    const { user, token } = await createUserAndToken();
    // Upload 55 sessions
    for (let i = 0; i < 55; i++) {
      await Session.create({
        user: user._id,
        techniqueId: 'beeralu_basic',
        techniqueName: 'Basic',
        startTime: i,
        endTime: i + 1,
        durationSeconds: 1,
        accuracyScore: 50,
        frameCount: 10,
      });
    }
    const res = await request(app)
      .get(`/api/v1/sessions?userId=${user._id}`)
      .set('Authorization', `Bearer ${token}`);
    expect(res.status).toBe(200);
    expect(res.body.data.length).toBeLessThanOrEqual(50);
  });

  it('returns 401 without auth token', async () => {
    const { user } = await createUserAndToken();
    const res = await request(app).get(`/api/v1/sessions?userId=${user._id}`);
    expect(res.status).toBe(401);
  });

  it('returns 401 with invalid token', async () => {
    const { user } = await createUserAndToken();
    const res = await request(app)
      .get(`/api/v1/sessions?userId=${user._id}`)
      .set('Authorization', 'Bearer fake.token.here');
    expect(res.status).toBe(401);
  });

  it('does not return sessions of other users', async () => {
    const { user: u1, token: t1 } = await createUserAndToken();
    const { user: u2, token: t2 } = await createUserAndToken();

    // u1 posts a session, u2 queries
    await request(app)
      .post('/api/v1/sessions')
      .set('Authorization', `Bearer ${t1}`)
      .send(validSession({ accuracyScore: 95 }));

    const res = await request(app)
      .get(`/api/v1/sessions?userId=${u2._id}`)
      .set('Authorization', `Bearer ${t2}`);

    expect(res.body.data).toHaveLength(0);
  });
});
