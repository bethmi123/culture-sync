const mongoose = require('mongoose');

const placeSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: [true, 'Place name is required'],
      trim: true,
    },
    culture: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Culture',
      required: true,
    },
    country: {
      type: String,
      required: true,
    },
    city: {
      type: String,
      default: null,
    },
    description: {
      type: String,
      required: [true, 'Description is required'],
    },
    historicalSignificance: {
      type: String,
      required: true,
    },
    culturalSignificance: {
      type: String,
      required: true,
    },
    architectureStyle: {
      type: String,
      default: null,
    },
    builtYear: {
      type: String,
      default: null,
    },
    location: {
      type: {
        type: String,
        enum: ['Point'],
        default: 'Point',
      },
      coordinates: {
        type: [Number], // [longitude, latitude]
        required: true,
      },
    },
    address: {
      type: String,
      default: null,
    },
    imageUrls: [{ type: String }],
    coverImageUrl: {
      type: String,
      default: null,
    },
    category: {
      type: String,
      enum: ['Temple', 'Monument', 'Museum', 'Palace', 'Church', 'Mosque', 'Market', 'Garden', 'Archaeological Site', 'UNESCO Site', 'Other'],
      default: 'Other',
    },
    openingHours: {
      type: String,
      default: null,
    },
    entryFee: {
      type: String,
      default: 'Free',
    },
    rating: {
      type: Number,
      min: 0,
      max: 5,
      default: 0,
    },
    reviewCount: {
      type: Number,
      default: 0,
    },
    arEnabled: {
      type: Boolean,
      default: true,
    },
    arMarkerKeywords: [{ type: String }],
    tags: [{ type: String }],
    isPublished: {
      type: Boolean,
      default: true,
    },
  },
  { timestamps: true }
);

placeSchema.index({ location: '2dsphere' });
placeSchema.index({ culture: 1 });
placeSchema.index({ country: 1 });
placeSchema.index({ category: 1 });

module.exports = mongoose.model('Place', placeSchema);
