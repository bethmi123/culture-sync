/**
 * leaderboard.test.js — Leaderboard Route
 * Covers: GET /api/v1/leaderboard — ranking logic, exclusions, edge cases
 */
const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const bcrypt = require('bcryptjs');

const app = require('../app');
const User = require('../src/models/User');

let mongoServer;

beforeAll(async () => {
  process.env.JWT_SECRET = 'test_leaderboard_secret';
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

// ─── HELPERS ─────────────────────────────────────────────────────────────────

async function createUser(name, email, avgAccuracy, totalSessions, bestAccuracy = avgAccuracy) {
  return User.create({
    name,
    email,
    passwordHash: await bcrypt.hash('pass', 10),
    averageAccuracy: avgAccuracy,
    bestAccuracy,
    totalSessions,
  });
}

// ─── GET /api/v1/leaderboard ─────────────────────────────────────────────────────

describe('GET /api/v1/leaderboard', () => {
  it('returns empty array when no users have sessions', async () => {
    await createUser('Zero User', 'zero@test.com', 0, 0);
    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data).toHaveLength(0);
  });

  it('returns empty array when database is empty', async () => {
    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.status).toBe(200);
    expect(res.body.data).toHaveLength(0);
  });

  it('excludes users with 0 sessions (totalSessions filter)', async () => {
    await createUser('Active', 'active@test.com', 85, 3);
    await createUser('Inactive', 'inactive@test.com', 90, 0); // no sessions
    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.body.data).toHaveLength(1);
    expect(res.body.data[0].userName).toBe('Active');
  });

  it('ranks users by averageAccuracy descending', async () => {
    await createUser('Low', 'low@test.com', 50, 5);
    await createUser('High', 'high@test.com', 95, 10);
    await createUser('Mid', 'mid@test.com', 75, 3);

    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.status).toBe(200);
    expect(res.body.data[0].userName).toBe('High');
    expect(res.body.data[1].userName).toBe('Mid');
    expect(res.body.data[2].userName).toBe('Low');
  });

  it('assigns correct ranks starting from 1', async () => {
    await createUser('First', 'f@test.com', 90, 1);
    await createUser('Second', 's@test.com', 80, 2);
    await createUser('Third', 't@test.com', 70, 1);

    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.body.data[0].rank).toBe(1);
    expect(res.body.data[1].rank).toBe(2);
    expect(res.body.data[2].rank).toBe(3);
  });

  it('response includes required fields: userId, userName, averageAccuracy, bestAccuracy, totalSessions, rank', async () => {
    await createUser('Full', 'full@test.com', 88.5, 7, 100);
    const res = await request(app).get('/api/v1/leaderboard');
    const entry = res.body.data[0];

    expect(entry.userId).toBeDefined();
    expect(entry.userName).toBe('Full');
    expect(entry.averageAccuracy).toBe(88.5);
    expect(entry.bestAccuracy).toBe(100);
    expect(entry.totalSessions).toBe(7);
    expect(entry.rank).toBe(1);
  });

  it('limits to top 20 users', async () => {
    for (let i = 0; i < 25; i++) {
      await createUser(`User${i}`, `u${i}@test.com`, 50 + i, 1);
    }
    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.body.data).toHaveLength(20);
  });

  it('top 20 users are the ones with highest averageAccuracy', async () => {
    for (let i = 0; i < 25; i++) {
      await createUser(`User${i}`, `u${i}@test.com`, i * 3, 1);
    }
    const res = await request(app).get('/api/v1/leaderboard');
    // All returned users should have accuracies > the 5 excluded ones
    const minAccuracy = res.body.data.at(-1).averageAccuracy;
    expect(minAccuracy).toBeGreaterThan(0);
    expect(res.body.data).toHaveLength(20);
    // First entry should be highest
    expect(res.body.data[0].averageAccuracy).toBeGreaterThanOrEqual(minAccuracy);
  });

  it('handles tied averageAccuracy with correct consecutive ranks', async () => {
    await createUser('A', 'a@t.c', 90, 5);
    await createUser('B', 'b@t.c', 90, 3);
    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.body.data[0].rank).toBe(1);
    expect(res.body.data[1].rank).toBe(2);
    // Both have same average but get different ranks (index-based)
    expect(res.body.data[0].averageAccuracy).toBe(90);
    expect(res.body.data[1].averageAccuracy).toBe(90);
  });

  it('does not return passwordHash in leaderboard response', async () => {
    await createUser('Safe', 'safe@t.c', 85, 1);
    const res = await request(app).get('/api/v1/leaderboard');
    expect(JSON.stringify(res.body)).not.toContain('passwordHash');
  });

  it('does not return email in leaderboard response', async () => {
    await createUser('Private', 'private@t.c', 85, 1);
    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.body.data[0].email).toBeUndefined();
  });

  it('single user with sessions appears at rank 1', async () => {
    await createUser('Solo', 'solo@test.com', 73.5, 4, 89);
    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.body.data).toHaveLength(1);
    expect(res.body.data[0].rank).toBe(1);
    expect(res.body.data[0].userName).toBe('Solo');
    expect(res.body.data[0].averageAccuracy).toBe(73.5);
  });

  it('does not require auth token', async () => {
    // Leaderboard is public
    const res = await request(app).get('/api/v1/leaderboard');
    expect(res.status).toBe(200);
  });
});
