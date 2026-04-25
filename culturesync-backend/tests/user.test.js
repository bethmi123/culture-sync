const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const bcrypt = require('bcryptjs');
const User = require('../src/models/User');

describe('User Model Unit Tests', () => {
  let mongoServer;

  beforeAll(async () => {
    mongoServer = await MongoMemoryServer.create();
    await mongoose.connect(mongoServer.getUri());
  });

  afterAll(async () => {
    await mongoose.disconnect();
    await mongoServer.stop();
  });

  it('should hash password when saved', async () => {
    const passwordHash = await bcrypt.hash('password123', 12);
    const user = new User({
      name: 'Test',
      email: 'hashing@example.com',
      passwordHash: passwordHash
    });
    await user.save();
    
    const saved = await User.findOne({ email: 'hashing@example.com' }).select('+passwordHash');
    expect(saved.passwordHash).toBeDefined();
    expect(saved.passwordHash).not.toBe('password123');
  });

  it('should correctly compare passwords', async () => {
    const passwordHash = await bcrypt.hash('password123', 12);
    const user = new User({
      name: 'Tester',
      email: 'compare@example.com',
      passwordHash: passwordHash
    });
    
    const isMatch = await user.comparePassword('password123');
    const isNotMatch = await user.comparePassword('wrongpass');
    
    expect(isMatch).toBe(true);
    expect(isNotMatch).toBe(false);
  });
});
