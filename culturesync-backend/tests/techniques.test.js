/**
 * techniques.test.js — Technique Model + Techniques Routes
 * Covers: Technique schema, GET /api/v1/techniques, GET /api/v1/techniques/:techniqueId
 */
const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');

const app = require('../app');
const Technique = require('../src/models/Technique');

let mongoServer;

beforeAll(async () => {
  process.env.JWT_SECRET = 'test_tech_secret';
  mongoServer = await MongoMemoryServer.create();
  await mongoose.connect(mongoServer.getUri());
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  await Technique.deleteMany({});
});

// ─── HELPERS ─────────────────────────────────────────────────────────────────

const makeTechnique = (overrides = {}) => ({
  techniqueId: `tech_${Date.now()}_${Math.random()}`,
  name: 'Test Technique',
  description: 'A test description',
  difficulty: 'Beginner',
  durationMinutes: 15,
  isPublished: true,
  ...overrides,
});

// ─── TECHNIQUE MODEL UNIT TESTS ───────────────────────────────────────────────

describe('Technique Model — Schema Validation', () => {
  it('saves a valid technique with all required fields', async () => {
    const t = await Technique.create(makeTechnique({
      techniqueId: 'beeralu_basic',
      name: 'Basic Crossing',
      description: 'The foundational Beeralu movement',
      difficulty: 'Beginner',
      durationMinutes: 10,
    }));
    expect(t._id).toBeDefined();
    expect(t.techniqueId).toBe('beeralu_basic');
    expect(t.isPublished).toBe(true);     // default
    expect(t.videoUrl).toBeNull();         // default
    expect(t.thumbnailUrl).toBeNull();     // default
    expect(t.createdAt).toBeDefined();
  });

  it('rejects missing techniqueId', async () => {
    const t = new Technique(makeTechnique({ techniqueId: undefined }));
    await expect(t.save()).rejects.toMatchObject({ errors: { techniqueId: expect.anything() } });
  });

  it('rejects duplicate techniqueId', async () => {
    await Technique.create(makeTechnique({ techniqueId: 'dup_tech' }));
    await expect(
      Technique.create(makeTechnique({ techniqueId: 'dup_tech' }))
    ).rejects.toThrow();
  });

  it('rejects missing name', async () => {
    const t = new Technique(makeTechnique({ name: undefined }));
    await expect(t.save()).rejects.toMatchObject({ errors: { name: expect.anything() } });
  });

  it('rejects missing description', async () => {
    const t = new Technique(makeTechnique({ description: undefined }));
    await expect(t.save()).rejects.toMatchObject({ errors: { description: expect.anything() } });
  });

  it('rejects missing durationMinutes', async () => {
    const t = new Technique(makeTechnique({ durationMinutes: undefined }));
    await expect(t.save()).rejects.toMatchObject({ errors: { durationMinutes: expect.anything() } });
  });

  it('rejects invalid difficulty value', async () => {
    const t = new Technique(makeTechnique({ difficulty: 'Expert' }));
    await expect(t.save()).rejects.toMatchObject({ errors: { difficulty: expect.anything() } });
  });

  it('accepts difficulty = Beginner', async () => {
    const t = await Technique.create(makeTechnique({ difficulty: 'Beginner' }));
    expect(t.difficulty).toBe('Beginner');
  });

  it('accepts difficulty = Intermediate', async () => {
    const t = await Technique.create(makeTechnique({ difficulty: 'Intermediate' }));
    expect(t.difficulty).toBe('Intermediate');
  });

  it('accepts difficulty = Advanced', async () => {
    const t = await Technique.create(makeTechnique({ difficulty: 'Advanced' }));
    expect(t.difficulty).toBe('Advanced');
  });

  it('rejects missing required difficulty', async () => {
    const t = new Technique(makeTechnique({ difficulty: undefined }));
    await expect(t.save()).rejects.toMatchObject({ errors: { difficulty: expect.anything() } });
  });

  it('stores steps as array of strings', async () => {
    const steps = ['Step 1: Hold threads', 'Step 2: Cross left over right'];
    const t = await Technique.create(makeTechnique({ steps }));
    expect(t.steps).toEqual(steps);
  });

  it('stores videoUrl when provided', async () => {
    const t = await Technique.create(makeTechnique({ videoUrl: 'https://cdn.beeralu.app/basic.mp4' }));
    expect(t.videoUrl).toBe('https://cdn.beeralu.app/basic.mp4');
  });

  it('isPublished defaults to true', async () => {
    const t = await Technique.create(makeTechnique({ isPublished: undefined }));
    expect(t.isPublished).toBe(true);
  });

  it('isPublished can be set to false', async () => {
    const t = await Technique.create(makeTechnique({ isPublished: false }));
    expect(t.isPublished).toBe(false);
  });

  it('saves all 6 real Beeralu technique IDs', async () => {
    const ids = [
      'beeralu_basic', 'beeralu_cross', 'beeralu_twist',
      'beeralu_diamond', 'beeralu_flower', 'beeralu_wave',
    ];
    const difficulties = ['Beginner', 'Beginner', 'Intermediate', 'Intermediate', 'Advanced', 'Advanced'];
    for (let i = 0; i < ids.length; i++) {
      await Technique.create(makeTechnique({ techniqueId: ids[i], difficulty: difficulties[i] }));
    }
    const all = await Technique.find();
    expect(all).toHaveLength(6);
  });
});

// ─── GET /api/v1/techniques ──────────────────────────────────────────────────────

describe('GET /api/v1/techniques', () => {
  it('returns empty array when no techniques exist', async () => {
    const res = await request(app).get('/api/v1/techniques');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data).toEqual([]);
  });

  it('returns only published techniques', async () => {
    await Technique.create(makeTechnique({ techniqueId: 'pub1', isPublished: true }));
    await Technique.create(makeTechnique({ techniqueId: 'pub2', isPublished: true }));
    await Technique.create(makeTechnique({ techniqueId: 'draft1', isPublished: false }));

    const res = await request(app).get('/api/v1/techniques');
    expect(res.status).toBe(200);
    expect(res.body.data).toHaveLength(2);
    expect(res.body.data.every(t => t.isPublished === true)).toBe(true);
  });

  it('returns zero results when all techniques are unpublished', async () => {
    await Technique.create(makeTechnique({ techniqueId: 'draft2', isPublished: false }));
    const res = await request(app).get('/api/v1/techniques');
    expect(res.status).toBe(200);
    expect(res.body.data).toHaveLength(0);
  });

  it('returns techniques sorted by pedagogical difficulty (Beginner → Intermediate → Advanced)', async () => {
    await Technique.create(makeTechnique({ techniqueId: 'adv', difficulty: 'Advanced', isPublished: true }));
    await Technique.create(makeTechnique({ techniqueId: 'int', difficulty: 'Intermediate', isPublished: true }));
    await Technique.create(makeTechnique({ techniqueId: 'beg', difficulty: 'Beginner', isPublished: true }));

    const res = await request(app).get('/api/v1/techniques');
    expect(res.status).toBe(200);
    expect(res.body.data).toHaveLength(3);
    expect(res.body.data[0].difficulty).toBe('Beginner');
    expect(res.body.data[1].difficulty).toBe('Intermediate');
    expect(res.body.data[2].difficulty).toBe('Advanced');
  });

  it('includes videoUrl in response', async () => {
    await Technique.create(makeTechnique({
      techniqueId: 'vid_tech',
      videoUrl: 'https://cdn.beeralu.app/wave.mp4',
    }));
    const res = await request(app).get('/api/v1/techniques');
    expect(res.body.data[0].videoUrl).toBe('https://cdn.beeralu.app/wave.mp4');
  });

  it('includes steps array in response', async () => {
    const steps = ['Hold bobbins', 'Cross right over left'];
    await Technique.create(makeTechnique({ techniqueId: 'steps_tech', steps }));
    const res = await request(app).get('/api/v1/techniques');
    expect(res.body.data[0].steps).toEqual(steps);
  });

  it('returns techniqueId in response body', async () => {
    await Technique.create(makeTechnique({ techniqueId: 'known_id' }));
    const res = await request(app).get('/api/v1/techniques');
    expect(res.body.data[0].techniqueId).toBe('known_id');
  });
});

// ─── GET /api/v1/techniques/:techniqueId ────────────────────────────────────────

describe('GET /api/v1/techniques/:techniqueId', () => {
  it('happy path — returns a specific technique by ID', async () => {
    await Technique.create(makeTechnique({
      techniqueId: 'beeralu_diamond',
      name: 'Diamond Pattern',
      difficulty: 'Intermediate',
      durationMinutes: 20,
      videoUrl: 'https://cdn.beeralu.app/diamond.mp4',
    }));

    const res = await request(app).get('/api/v1/techniques/beeralu_diamond');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.techniqueId).toBe('beeralu_diamond');
    expect(res.body.data.name).toBe('Diamond Pattern');
    expect(res.body.data.videoUrl).toBe('https://cdn.beeralu.app/diamond.mp4');
  });

  it('returns 404 for non-existent techniqueId', async () => {
    const res = await request(app).get('/api/v1/techniques/does_not_exist');
    expect(res.status).toBe(404);
    expect(res.body.success).toBe(false);
    expect(res.body.message).toBe('Technique not found');
  });

  it('returns 404 for empty-string-like ID that matches nothing', async () => {
    const res = await request(app).get('/api/v1/techniques/missing_999');
    expect(res.status).toBe(404);
  });

  it('returns unpublished techniques by ID (no filter on single get)', async () => {
    await Technique.create(makeTechnique({ techniqueId: 'unpub_direct', isPublished: false }));
    const res = await request(app).get('/api/v1/techniques/unpub_direct');
    expect(res.status).toBe(200);
    expect(res.body.data.techniqueId).toBe('unpub_direct');
  });

  it('returns all 6 real Beeralu techniques individually', async () => {
    const techs = [
      { id: 'beeralu_basic', name: 'Basic Crossing', difficulty: 'Beginner' },
      { id: 'beeralu_cross', name: 'Cross Twist', difficulty: 'Beginner' },
      { id: 'beeralu_twist', name: 'Double Twist', difficulty: 'Intermediate' },
      { id: 'beeralu_diamond', name: 'Diamond Pattern', difficulty: 'Intermediate' },
      { id: 'beeralu_flower', name: 'Flower Motif', difficulty: 'Advanced' },
      { id: 'beeralu_wave', name: 'Wave', difficulty: 'Advanced' },
    ];

    for (const t of techs) {
      await Technique.create(makeTechnique({ techniqueId: t.id, name: t.name, difficulty: t.difficulty }));
    }

    for (const t of techs) {
      const res = await request(app).get(`/api/v1/techniques/${t.id}`);
      expect(res.status).toBe(200);
      expect(res.body.data.name).toBe(t.name);
    }
  });
});
