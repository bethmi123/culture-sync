/**
 * models.test.js — Culture, Place, ARMarker Model Schema Validation
 * Covers required fields, enum validations, defaults, nested arrays, GeoJSON, indexes
 */
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');

const Culture = require('../src/models/Culture');
const Place = require('../src/models/Place');
const ARMarker = require('../src/models/ARMarker');

let mongoServer;

beforeAll(async () => {
  mongoServer = await MongoMemoryServer.create();
  await mongoose.connect(mongoServer.getUri());
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

afterEach(async () => {
  await Culture.deleteMany({});
  await Place.deleteMany({});
  await ARMarker.deleteMany({});
});

// ─── CULTURE MODEL ────────────────────────────────────────────────────────────

describe('Culture Model — Schema Validation', () => {
  const validCulture = () => ({
    name: 'Sri Lankan',
    country: 'Sri Lanka',
    countryCode: 'LK',
    continent: 'Asia',
    description: 'Rich cultural heritage of Sri Lanka',
    language: ['Sinhala', 'Tamil', 'English'],
    religion: ['Buddhism', 'Hinduism', 'Islam', 'Christianity'],
  });

  it('saves a valid culture document', async () => {
    const c = await Culture.create(validCulture());
    expect(c._id).toBeDefined();
    expect(c.name).toBe('Sri Lankan');
    expect(c.countryCode).toBe('LK');
    expect(c.popularityScore).toBe(0);   // default
    expect(c.isPublished).toBe(true);    // default
    expect(c.tags).toEqual([]);
    expect(c.createdAt).toBeDefined();
  });

  it('rejects missing name', async () => {
    const c = new Culture({ ...validCulture(), name: undefined });
    await expect(c.save()).rejects.toMatchObject({ errors: { name: expect.anything() } });
  });

  it('rejects missing country', async () => {
    const c = new Culture({ ...validCulture(), country: undefined });
    await expect(c.save()).rejects.toMatchObject({ errors: { country: expect.anything() } });
  });

  it('rejects missing countryCode', async () => {
    const c = new Culture({ ...validCulture(), countryCode: undefined });
    await expect(c.save()).rejects.toMatchObject({ errors: { countryCode: expect.anything() } });
  });

  it('rejects countryCode longer than 3 characters', async () => {
    const c = new Culture({ ...validCulture(), countryCode: 'ABCD' });
    await expect(c.save()).rejects.toThrow();
  });

  it('stores countryCode in uppercase', async () => {
    const c = await Culture.create({ ...validCulture(), countryCode: 'lk' });
    expect(c.countryCode).toBe('LK');
  });

  it('rejects invalid continent', async () => {
    const c = new Culture({ ...validCulture(), continent: 'Antarctica' });
    await expect(c.save()).rejects.toMatchObject({ errors: { continent: expect.anything() } });
  });

  it('accepts all valid continent values', async () => {
    const validContinents = ['Asia', 'Europe', 'Africa', 'North America', 'South America', 'Oceania', 'Middle East'];
    for (let i = 0; i < validContinents.length; i++) {
      const c = await Culture.create({
        ...validCulture(),
        continent: validContinents[i],
        countryCode: 'XX',
        name: `Culture ${i}`,
      });
      expect(c.continent).toBe(validContinents[i]);
    }
  });

  it('rejects missing description', async () => {
    const c = new Culture({ ...validCulture(), description: undefined });
    await expect(c.save()).rejects.toMatchObject({ errors: { description: expect.anything() } });
  });

  it('initialises language to empty array when undefined (Mongoose array default)', async () => {
    // Mongoose typed [String] arrays default to [] when field is undefined
    const c = await Culture.create({ ...validCulture(), language: undefined });
    expect(Array.isArray(c.language)).toBe(true);
  });

  it('saves nested traditions array', async () => {
    const c = await Culture.create({
      ...validCulture(),
      traditions: [
        { name: 'Beeralu Lace Making', description: 'Traditional lace craft from Sri Lanka' },
        { name: 'Esala Perahera', description: 'Grand Buddhist procession' },
      ],
    });
    expect(c.traditions).toHaveLength(2);
    expect(c.traditions[0].name).toBe('Beeralu Lace Making');
  });

  it('saves nested cuisine array', async () => {
    const c = await Culture.create({
      ...validCulture(),
      cuisine: [
        { name: 'Rice and Curry', description: 'Staple Sri Lankan meal' },
      ],
    });
    expect(c.cuisine[0].name).toBe('Rice and Curry');
  });

  it('saves nested festivals array with month', async () => {
    const c = await Culture.create({
      ...validCulture(),
      festivals: [
        { name: 'Sinhala New Year', description: 'Traditional New Year', month: 'April' },
        { name: 'Vesak', description: 'Buddhist holiday', month: 'May' },
      ],
    });
    expect(c.festivals).toHaveLength(2);
    expect(c.festivals[0].month).toBe('April');
  });

  it('saves tags as an array of strings', async () => {
    const c = await Culture.create({ ...validCulture(), tags: ['craft', 'heritage', 'UNESCO'] });
    expect(c.tags).toEqual(['craft', 'heritage', 'UNESCO']);
  });

  it('trims whitespace from name and country', async () => {
    const c = await Culture.create({ ...validCulture(), name: '  Sri Lankan  ', country: '  Sri Lanka  ' });
    expect(c.name).toBe('Sri Lankan');
    expect(c.country).toBe('Sri Lanka');
  });
});

// ─── PLACE MODEL ──────────────────────────────────────────────────────────────

describe('Place Model — Schema Validation', () => {
  let cultureId;

  beforeEach(async () => {
    const c = await Culture.create({
      name: 'Test Culture',
      country: 'Sri Lanka',
      countryCode: 'LK',
      continent: 'Asia',
      description: 'Test',
      language: ['Sinhala'],
    });
    cultureId = c._id;
  });

  const validPlace = (overrides = {}) => ({
    name: 'Temple of the Tooth',
    culture: cultureId,
    country: 'Sri Lanka',
    description: 'Sacred Buddhist temple in Kandy',
    historicalSignificance: 'Houses the relic of the tooth of Buddha',
    culturalSignificance: 'Most important Buddhist site in Sri Lanka',
    location: { type: 'Point', coordinates: [80.6413, 7.2948] }, // [lng, lat]
    category: 'Temple',
    ...overrides,
  });

  it('saves a valid place document', async () => {
    const p = await Place.create(validPlace());
    expect(p._id).toBeDefined();
    expect(p.name).toBe('Temple of the Tooth');
    expect(p.arEnabled).toBe(true);     // default
    expect(p.isPublished).toBe(true);  // default
    expect(p.rating).toBe(0);           // default
    expect(p.entryFee).toBe('Free');    // default
    expect(p.category).toBe('Temple');
  });

  it('rejects missing name', async () => {
    const p = new Place(validPlace({ name: undefined }));
    await expect(p.save()).rejects.toMatchObject({ errors: { name: expect.anything() } });
  });

  it('rejects missing culture reference', async () => {
    const p = new Place(validPlace({ culture: undefined }));
    await expect(p.save()).rejects.toMatchObject({ errors: { culture: expect.anything() } });
  });

  it('rejects missing country', async () => {
    const p = new Place(validPlace({ country: undefined }));
    await expect(p.save()).rejects.toMatchObject({ errors: { country: expect.anything() } });
  });

  it('rejects missing description', async () => {
    const p = new Place(validPlace({ description: undefined }));
    await expect(p.save()).rejects.toMatchObject({ errors: { description: expect.anything() } });
  });

  it('rejects missing historicalSignificance', async () => {
    const p = new Place(validPlace({ historicalSignificance: undefined }));
    await expect(p.save()).rejects.toMatchObject({ errors: { historicalSignificance: expect.anything() } });
  });

  it('rejects missing culturalSignificance', async () => {
    const p = new Place(validPlace({ culturalSignificance: undefined }));
    await expect(p.save()).rejects.toMatchObject({ errors: { culturalSignificance: expect.anything() } });
  });

  it('rejects missing location coordinates', async () => {
    const p = new Place(validPlace({ location: { type: 'Point' } }));
    await expect(p.save()).rejects.toThrow();
  });

  it('stores GeoJSON coordinates correctly', async () => {
    const p = await Place.create(validPlace({
      location: { type: 'Point', coordinates: [80.6413, 7.2948] },
    }));
    expect(p.location.coordinates[0]).toBeCloseTo(80.6413, 4);
    expect(p.location.coordinates[1]).toBeCloseTo(7.2948, 4);
  });

  it('accepts all valid category values', async () => {
    const categories = ['Temple', 'Monument', 'Museum', 'Palace', 'Church',
      'Mosque', 'Market', 'Garden', 'Archaeological Site', 'UNESCO Site', 'Other'];
    for (let i = 0; i < categories.length; i++) {
      const p = await Place.create(validPlace({ name: `Place ${i}`, category: categories[i] }));
      expect(p.category).toBe(categories[i]);
    }
  });

  it('rejects invalid category', async () => {
    const p = new Place(validPlace({ category: 'Stadium' }));
    await expect(p.save()).rejects.toMatchObject({ errors: { category: expect.anything() } });
  });

  it('defaults category to Other when not provided', async () => {
    const p = await Place.create(validPlace({ category: undefined }));
    expect(p.category).toBe('Other');
  });

  it('rejects rating > 5', async () => {
    const p = new Place(validPlace({ rating: 6 }));
    await expect(p.save()).rejects.toMatchObject({ errors: { rating: expect.anything() } });
  });

  it('rejects rating < 0', async () => {
    const p = new Place(validPlace({ rating: -1 }));
    await expect(p.save()).rejects.toMatchObject({ errors: { rating: expect.anything() } });
  });

  it('accepts rating boundary = 5', async () => {
    const p = await Place.create(validPlace({ rating: 5 }));
    expect(p.rating).toBe(5);
  });

  it('accepts rating boundary = 0', async () => {
    const p = await Place.create(validPlace({ rating: 0 }));
    expect(p.rating).toBe(0);
  });

  it('saves arMarkerKeywords as array', async () => {
    const p = await Place.create(validPlace({ arMarkerKeywords: ['tooth relic', 'kandy temple'] }));
    expect(p.arMarkerKeywords).toEqual(['tooth relic', 'kandy temple']);
  });
});

// ─── ARMARKER MODEL ───────────────────────────────────────────────────────────

describe('ARMarker Model — Schema Validation', () => {
  let cultureId, placeId;

  beforeEach(async () => {
    const c = await Culture.create({
      name: 'LK Culture',
      country: 'Sri Lanka',
      countryCode: 'LK',
      continent: 'Asia',
      description: 'Test',
      language: ['Sinhala'],
    });
    cultureId = c._id;

    const p = await Place.create({
      name: 'Test Place',
      culture: cultureId,
      country: 'Sri Lanka',
      description: 'Test Place Desc',
      historicalSignificance: 'Sig',
      culturalSignificance: 'CSig',
      location: { type: 'Point', coordinates: [80.0, 7.0] },
    });
    placeId = p._id;
  });

  const validMarker = (overrides = {}) => ({
    place: placeId,
    culture: cultureId,
    overlayTitle: 'Temple of the Tooth',
    arBubbleText: 'The most sacred Buddhist site in Sri Lanka, housing the relic of the Tooth of Buddha.',
    ...overrides,
  });

  it('saves a valid ARMarker document', async () => {
    const m = await ARMarker.create(validMarker());
    expect(m._id).toBeDefined();
    expect(m.activationRadiusMeters).toBe(500); // default
    expect(m.isActive).toBe(true);              // default
    expect(m.aiDescription).toBeNull();          // default
    expect(m.modelUrl).toBeNull();               // default
    expect(m.createdAt).toBeDefined();
  });

  it('rejects missing place reference', async () => {
    const m = new ARMarker(validMarker({ place: undefined }));
    await expect(m.save()).rejects.toMatchObject({ errors: { place: expect.anything() } });
  });

  it('rejects missing culture reference', async () => {
    const m = new ARMarker(validMarker({ culture: undefined }));
    await expect(m.save()).rejects.toMatchObject({ errors: { culture: expect.anything() } });
  });

  it('rejects missing overlayTitle', async () => {
    const m = new ARMarker(validMarker({ overlayTitle: undefined }));
    await expect(m.save()).rejects.toMatchObject({ errors: { overlayTitle: expect.anything() } });
  });

  it('rejects missing arBubbleText', async () => {
    const m = new ARMarker(validMarker({ arBubbleText: undefined }));
    await expect(m.save()).rejects.toMatchObject({ errors: { arBubbleText: expect.anything() } });
  });

  it('rejects arBubbleText exceeding 280 characters', async () => {
    const longText = 'A'.repeat(281);
    const m = new ARMarker(validMarker({ arBubbleText: longText }));
    await expect(m.save()).rejects.toMatchObject({ errors: { arBubbleText: expect.anything() } });
  });

  it('accepts arBubbleText exactly 280 characters', async () => {
    const text280 = 'B'.repeat(280);
    const m = await ARMarker.create(validMarker({ arBubbleText: text280 }));
    expect(m.arBubbleText.length).toBe(280);
  });

  it('lowercases detectionKeywords', async () => {
    const m = await ARMarker.create(validMarker({
      detectionKeywords: ['TOOTH RELIC', 'KANDY', 'Temple'],
    }));
    expect(m.detectionKeywords).toEqual(['tooth relic', 'kandy', 'temple']);
  });

  it('trims whitespace from detectionKeywords', async () => {
    const m = await ARMarker.create(validMarker({
      detectionKeywords: ['  beeralu  ', '  lace  '],
    }));
    expect(m.detectionKeywords).toEqual(['beeralu', 'lace']);
  });

  it('saves facts array with label+value pairs', async () => {
    const m = await ARMarker.create(validMarker({
      facts: [
        { label: 'Built', value: '16th century' },
        { label: 'UNESCO', value: 'World Heritage Site' },
      ],
    }));
    expect(m.facts).toHaveLength(2);
    expect(m.facts[0].label).toBe('Built');
    expect(m.facts[1].value).toBe('World Heritage Site');
  });

  it('saves AI description when provided', async () => {
    const aiDesc = 'The Temple of the Tooth Relic is a magnificent Buddhist temple in Kandy, Sri Lanka.';
    const m = await ARMarker.create(validMarker({ aiDescription: aiDesc }));
    expect(m.aiDescription).toBe(aiDesc);
  });

  it('activationRadiusMeters defaults to 500', async () => {
    const m = await ARMarker.create(validMarker());
    expect(m.activationRadiusMeters).toBe(500);
  });

  it('activationRadiusMeters can be customised', async () => {
    const m = await ARMarker.create(validMarker({ activationRadiusMeters: 1000 }));
    expect(m.activationRadiusMeters).toBe(1000);
  });

  it('isActive can be set to false', async () => {
    const m = await ARMarker.create(validMarker({ isActive: false }));
    expect(m.isActive).toBe(false);
  });
});
