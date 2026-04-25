const mongoose = require('mongoose');

const cultureSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: [true, 'Culture name is required'],
      trim: true,
    },
    country: {
      type: String,
      required: [true, 'Country is required'],
      trim: true,
    },
    countryCode: {
      type: String,
      required: true,
      uppercase: true,
      maxlength: 3,
    },
    continent: {
      type: String,
      required: true,
      enum: ['Asia', 'Europe', 'Africa', 'North America', 'South America', 'Oceania', 'Middle East'],
    },
    description: {
      type: String,
      required: [true, 'Description is required'],
    },
    language: {
      type: [String],
      required: true,
    },
    religion: {
      type: [String],
      default: [],
    },
    traditions: [
      {
        name: { type: String, required: true },
        description: { type: String, required: true },
        imageUrl: { type: String, default: null },
      },
    ],
    cuisine: [
      {
        name: { type: String, required: true },
        description: { type: String, required: true },
        imageUrl: { type: String, default: null },
      },
    ],
    festivals: [
      {
        name: { type: String, required: true },
        description: { type: String, required: true },
        month: { type: String },
        imageUrl: { type: String, default: null },
      },
    ],
    music: {
      type: String,
      default: null,
    },
    art: {
      type: String,
      default: null,
    },
    flagUrl: {
      type: String,
      default: null,
    },
    coverImageUrl: {
      type: String,
      default: null,
    },
    tags: [{ type: String }],
    popularityScore: {
      type: Number,
      default: 0,
    },
    isPublished: {
      type: Boolean,
      default: true,
    },
  },
  { timestamps: true }
);

cultureSchema.index({ country: 1, name: 1 });
cultureSchema.index({ continent: 1 });
cultureSchema.index({ tags: 1 });

module.exports = mongoose.model('Culture', cultureSchema);
