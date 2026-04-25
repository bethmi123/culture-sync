const mongoose = require('mongoose');

const techniqueSchema = new mongoose.Schema(
  {
    techniqueId:    { type: String, required: true, unique: true },
    name:           { type: String, required: true },
    description:    { type: String, required: true },
    difficulty:     { type: String, enum: ['Beginner', 'Intermediate', 'Advanced'], required: true },
    durationMinutes:{ type: Number, required: true },
    steps:          [{ type: String }],
    videoUrl:       { type: String, default: null },
    thumbnailUrl:   { type: String, default: null },
    isPublished:    { type: Boolean, default: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Technique', techniqueSchema);
