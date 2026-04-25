const mongoose = require('mongoose');

const sessionSchema = new mongoose.Schema(
  {
    user:            { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    techniqueId:     { type: String, required: true },
    techniqueName:   { type: String, required: true },
    startTime:       { type: Number, required: true },  // Unix ms
    endTime:         { type: Number, required: true },
    durationSeconds: { type: Number, required: true },
    accuracyScore:   { type: Number, required: true, min: 0, max: 100 }, // real DTW score
    frameCount:      { type: Number, required: true },
  },
  { timestamps: true }
);

sessionSchema.index({ user: 1, createdAt: -1 });

module.exports = mongoose.model('Session', sessionSchema);
