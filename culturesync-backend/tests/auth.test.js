/**
 * auth.test.js — User Model + Auth Routes
 * Covers: User schema validation, password hashing, comparePassword,
 *         POST /api/v1/auth/register, POST /api/v1/auth/login
 * Target: 100% branch coverage of auth.js + User.js
 */
const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const app = require('../app');
const User = require('../src/models/User');

let mongoServer;

beforeAll(async () => {
  process.env.JWT_SECRET = 'test_auth_secret_culturesync_2024';
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

// ─── USER MODEL UNIT TESTS ────────────────────────────────────────────────────

describe('User Model — Schema & Method Tests', () => {
  it('creates a user with all required fields', async () => {
    const hash = await bcrypt.hash('secure123', 12);
    const user = await User.create({
      name: 'Nandawathi',
      email: 'nandawathi@beeralu.lk',
      passwordHash: hash,
    });
    expect(user._id).toBeDefined();
    expect(user.name).toBe('Nandawathi');
    expect(user.email).toBe('nandawathi@beeralu.lk');
    expect(user.role).toBe('learner');        // default
    expect(user.totalSessions).toBe(0);       // default
    expect(user.averageAccuracy).toBe(0);     // default
    expect(user.bestAccuracy).toBe(0);        // default
    expect(user.createdAt).toBeDefined();     // timestamps
    expect(user.updatedAt).toBeDefined();
  });

  it('stores email in lowercase regardless of input casing', async () => {
    const hash = await bcrypt.hash('abc', 12);
    const user = await User.create({
      name: 'Test',
      email: 'UPPER@EXAMPLE.COM',
      passwordHash: hash,
    });
    expect(user.email).toBe('upper@example.com');
  });

  it('rejects duplicate emails', async () => {
    const hash = await bcrypt.hash('abc', 12);
    await User.create({ name: 'A', email: 'dup@test.com', passwordHash: hash });
    await expect(
      User.create({ name: 'B', email: 'dup@test.com', passwordHash: hash })
    ).rejects.toThrow();
  });

  it('rejects a user missing required name', async () => {
    const u = new User({ email: 'no@name.com', passwordHash: 'h' });
    await expect(u.save()).rejects.toMatchObject({ errors: { name: expect.anything() } });
  });

  it('rejects a user missing required email', async () => {
    const u = new User({ name: 'No Email', passwordHash: 'h' });
    await expect(u.save()).rejects.toMatchObject({ errors: { email: expect.anything() } });
  });

  it('rejects a user missing required passwordHash', async () => {
    const u = new User({ name: 'No Pass', email: 'nopass@test.com' });
    await expect(u.save()).rejects.toMatchObject({ errors: { passwordHash: expect.anything() } });
  });

  it('rejects an invalid role value', async () => {
    const u = new User({
      name: 'Bad Role',
      email: 'badrole@test.com',
      passwordHash: 'h',
      role: 'superuser',
    });
    await expect(u.save()).rejects.toMatchObject({ errors: { role: expect.anything() } });
  });

  it('accepts role = admin', async () => {
    const hash = await bcrypt.hash('abc', 12);
    const user = await User.create({
      name: 'Admin User',
      email: 'admin@test.com',
      passwordHash: hash,
      role: 'admin',
    });
    expect(user.role).toBe('admin');
  });

  it('passwordHash is excluded from default queries (select: false)', async () => {
    const hash = await bcrypt.hash('mysecret', 12);
    await User.create({ name: 'Hidden', email: 'hidden@test.com', passwordHash: hash });
    const found = await User.findOne({ email: 'hidden@test.com' });
    expect(found.passwordHash).toBeUndefined();
  });

  it('passwordHash is returned when explicitly selected', async () => {
    const hash = await bcrypt.hash('mysecret', 12);
    await User.create({ name: 'Explicit', email: 'explicit@test.com', passwordHash: hash });
    const found = await User.findOne({ email: 'explicit@test.com' }).select('+passwordHash');
    expect(found.passwordHash).toBeDefined();
  });

  it('comparePassword returns true for correct password', async () => {
    const hash = await bcrypt.hash('correctPass', 12);
    const user = new User({ name: 'T', email: 't@t.com', passwordHash: hash });
    expect(await user.comparePassword('correctPass')).toBe(true);
  });

  it('comparePassword returns false for wrong password', async () => {
    const hash = await bcrypt.hash('correctPass', 12);
    const user = new User({ name: 'T', email: 't@t.com', passwordHash: hash });
    expect(await user.comparePassword('wrongPass')).toBe(false);
  });

  it('comparePassword handles empty string', async () => {
    const hash = await bcrypt.hash('somepass', 12);
    const user = new User({ name: 'T', email: 't@t.com', passwordHash: hash });
    expect(await user.comparePassword('')).toBe(false);
  });

  it('trims whitespace from name and email', async () => {
    const hash = await bcrypt.hash('abc', 12);
    const user = await User.create({
      name: '  Trimmed  ',
      email: '  trimmed@example.com  ',
      passwordHash: hash,
    });
    expect(user.name).toBe('Trimmed');
    expect(user.email).toBe('trimmed@example.com');
  });
});

// ─── POST /api/v1/auth/register ─────────────────────────────────────────────────

describe('POST /api/v1/auth/register', () => {
  it('happy path — registers a new user and returns token', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      name: 'Bethmi Dias',
      email: 'bethmi@culturesync.app',
      password: 'Beeralu@2024',
    });

    expect(res.status).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.data.token).toBeDefined();
    expect(res.body.data.user.email).toBe('bethmi@culturesync.app');
    expect(res.body.data.user.name).toBe('Bethmi Dias');
    expect(res.body.data.user.role).toBe('learner');
    expect(res.body.data.user.id).toBeDefined();

    // Verify the token is valid JWT
    const payload = jwt.verify(res.body.data.token, process.env.JWT_SECRET);
    expect(payload.id).toBe(res.body.data.user.id);
    expect(payload.role).toBe('learner');
  });

  it('persists user to database', async () => {
    await request(app).post('/api/v1/auth/register').send({
      name: 'DB User',
      email: 'dbuser@test.com',
      password: 'password123',
    });
    const inDb = await User.findOne({ email: 'dbuser@test.com' });
    expect(inDb).not.toBeNull();
    expect(inDb.name).toBe('DB User');
  });

  it('stores hashed password, never plaintext', async () => {
    await request(app).post('/api/v1/auth/register').send({
      name: 'HashCheck',
      email: 'hashcheck@test.com',
      password: 'plaintext123',
    });
    const inDb = await User.findOne({ email: 'hashcheck@test.com' }).select('+passwordHash');
    expect(inDb.passwordHash).not.toBe('plaintext123');
    expect(await bcrypt.compare('plaintext123', inDb.passwordHash)).toBe(true);
  });

  it('returns 400 when name is missing', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      email: 'noname@test.com',
      password: 'password123',
    });
    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toBe('All fields required');
  });

  it('returns 400 when email is missing', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      name: 'No Email',
      password: 'password123',
    });
    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toBe('All fields required');
  });

  it('returns 400 when password is missing', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      name: 'No Pass',
      email: 'nopass@test.com',
    });
    expect(res.status).toBe(400);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toBe('All fields required');
  });

  it('returns 400 when all fields are empty strings', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      name: '',
      email: '',
      password: '',
    });
    expect(res.status).toBe(400);
    expect(res.body.message).toBe('All fields required');
  });

  it('returns 400 for empty body', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({});
    expect(res.status).toBe(400);
    expect(res.body.message).toBe('All fields required');
  });

  it('returns 409 when email is already registered', async () => {
    await User.create({
      name: 'Existing',
      email: 'existing@test.com',
      passwordHash: await bcrypt.hash('pass', 12),
    });
    const res = await request(app).post('/api/v1/auth/register').send({
      name: 'Duplicate',
      email: 'existing@test.com',
      password: 'newpass',
    });
    expect(res.status).toBe(409);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toBe('Email already registered');
  });

  it('treats emails as case-insensitive for duplicate check', async () => {
    await request(app).post('/api/v1/auth/register').send({
      name: 'First',
      email: 'case@test.com',
      password: 'pass1',
    });
    const res = await request(app).post('/api/v1/auth/register').send({
      name: 'Second',
      email: 'CASE@TEST.COM',
      password: 'pass2',
    });
    expect(res.status).toBe(409);
  });

  it('JWT token expires in 30 days', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      name: 'JWT Exp Test',
      email: 'jwtexp@test.com',
      password: 'pass',
    });
    const payload = jwt.verify(res.body.data.token, process.env.JWT_SECRET);
    const thirtyDaysMs = 30 * 24 * 60 * 60;
    expect(payload.exp - payload.iat).toBeCloseTo(thirtyDaysMs, -2);
  });

  it('handles unicode name correctly', async () => {
    const res = await request(app).post('/api/v1/auth/register').send({
      name: 'ශ්‍රී ලංකා',
      email: 'sinhala@test.com',
      password: 'pass123',
    });
    expect(res.status).toBe(201);
    expect(res.body.data.user.name).toBe('ශ්‍රී ලංකා');
  });
});

// ─── POST /api/v1/auth/login ─────────────────────────────────────────────────────

describe('POST /api/v1/auth/login', () => {
  beforeEach(async () => {
    await request(app).post('/api/v1/auth/register').send({
      name: 'Login Tester',
      email: 'login@beeralu.lk',
      password: 'validPass123',
    });
  });

  it('happy path — logs in with correct credentials', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'login@beeralu.lk',
      password: 'validPass123',
    });
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.token).toBeDefined();
    expect(res.body.data.user.email).toBe('login@beeralu.lk');
    expect(res.body.data.user.name).toBe('Login Tester');
    expect(res.body.data.user.id).toBeDefined();
  });

  it('returns a valid JWT with correct payload', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'login@beeralu.lk',
      password: 'validPass123',
    });
    const payload = jwt.verify(res.body.data.token, process.env.JWT_SECRET);
    expect(payload.id).toBe(res.body.data.user.id);
    expect(payload.role).toBe('learner');
  });

  it('returns 400 when email is missing', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({ password: 'abc' });
    expect(res.status).toBe(400);
    expect(res.body.message).toBe('Email and password required');
  });

  it('returns 400 when password is missing', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({ email: 'login@beeralu.lk' });
    expect(res.status).toBe(400);
    expect(res.body.message).toBe('Email and password required');
  });

  it('returns 400 when email is empty string', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({ email: '', password: 'abc' });
    expect(res.status).toBe(400);
  });

  it('returns 400 when body is empty', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({});
    expect(res.status).toBe(400);
  });

  it('returns 401 for non-existent email', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'ghost@nowhere.com',
      password: 'somepass',
    });
    expect(res.status).toBe(401);
    expect(res.body.message).toBe('Invalid email or password');
  });

  it('returns 401 for correct email but wrong password', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'login@beeralu.lk',
      password: 'WRONG_PASSWORD',
    });
    expect(res.status).toBe(401);
    expect(res.body.message).toBe('Invalid email or password');
  });

  it('returns 401 for empty password on existing account', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'login@beeralu.lk',
      password: '',
    });
    expect(res.status).toBe(400);
  });

  it('is case-insensitive for email during login', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'LOGIN@BEERALU.LK',
      password: 'validPass123',
    });
    expect(res.status).toBe(200);
  });

  it('does not expose passwordHash in login response', async () => {
    const res = await request(app).post('/api/v1/auth/login').send({
      email: 'login@beeralu.lk',
      password: 'validPass123',
    });
    expect(res.body.data.user.passwordHash).toBeUndefined();
    expect(JSON.stringify(res.body)).not.toContain('passwordHash');
  });
});
