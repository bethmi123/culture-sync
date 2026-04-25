const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema(
  {
    name:         { type: String,  required: true, trim: true },
    email:        { type: String,  required: true, unique: true, lowercase: true, trim: true },
    passwordHash: { type: String,  required: true, select: false },
    role:         { type: String,  enum: ['learner', 'admin'], default: 'learner' },
    totalSessions:{ type: Number,  default: 0 },
    averageAccuracy: { type: Number, default: 0 },
    bestAccuracy: { type: Number,  default: 0 },
  },
  { timestamps: true }
);

userSchema.methods.comparePassword = function (plain) {
  return bcrypt.compare(plain, this.passwordHash);
};

module.exports = mongoose.model('User', userSchema);
