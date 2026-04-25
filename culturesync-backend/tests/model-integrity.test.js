const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const Session = require('../src/models/Session');
const User = require('../src/models/User');

describe('Session Model: Zero-Fake Integrity Teardown', () => {
    let mongoServer;

    beforeAll(async () => {
        mongoServer = await MongoMemoryServer.create();
        await mongoose.connect(mongoServer.getUri());
    });

    afterAll(async () => {
        await mongoose.disconnect();
        await mongoServer.stop();
    });

    it('Happy Path: Should successfully save a valid session vector', async () => {
        const u = await User.create({ name: 'U', email: 'v@t.c', passwordHash: 'h' });
        const s = new Session({
            user: u._id, techniqueId: 't', techniqueName: 'n',
            startTime: 0, endTime: 100, durationSeconds: 0.1,
            accuracyScore: 90, frameCount: 10
        });
        const saved = await s.save();
        expect(saved._id).toBeDefined();
    });

    it('Edge Case: Reject sessions with accuracyScore > 100 (Constraint: L11)', async () => {
        const s = new Session({ accuracyScore: 101 });
        let err;
        try { await s.save(); } catch (e) { err = e; }
        expect(err.errors.accuracyScore).toBeDefined();
    });

    it('Edge Case: Reject sessions with accuracyScore < 0 (Constraint: L11)', async () => {
        const s = new Session({ accuracyScore: -1 });
        let err;
        try { await s.save(); } catch (e) { err = e; }
        expect(err.errors.accuracyScore).toBeDefined();
    });

    it('Boundary: Reject missing mandatory user ref (Schema: L5)', async () => {
        const s = new Session({ techniqueId: 't' });
        let err;
        try { await s.save(); } catch (e) { err = e; }
        expect(err.errors.user).toBeDefined();
    });

    it('Boundary: Reject missing mandatory techniqueId (Schema: L6)', async () => {
        const s = new Session({ user: new mongoose.Types.ObjectId() });
        let err;
        try { await s.save(); } catch (e) { err = e; }
        expect(err.errors.techniqueId).toBeDefined();
    });
});
