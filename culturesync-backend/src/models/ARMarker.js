const mongoose = require('mongoose');

// AR markers are visual anchors tied to real-world places or cultural artifacts.
// When the app detects a keyword/label from ML Kit, it queries the matching ARMarker
// and returns rich cultural overlay data.
const arMarkerSchema = new mongoose.Schema(
  {
    place: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Place',
      required: true,
    },
    culture: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Culture',
      required: true,
    },
    // Keywords returned by Google ML Kit Vision / Landmark Detection
    detectionKeywords: [
      {
        type: String,
        lowercase: true,
        trim: true,
      },
    ],
    // GPS radius in meters — if user is within this range, marker activates
    activationRadiusMeters: {
      type: Number,
      default: 500,
    },
    overlayTitle: {
      type: String,
      required: true,
    },
    overlaySubtitle: {
      type: String,
      default: null,
    },
    // Short text shown as AR pop-up bubble
    arBubbleText: {
      type: String,
      required: true,
      maxlength: 280,
    },
    // AI-generated full description (cached, refreshed periodically)
    aiDescription: {
      type: String,
      default: null,
    },
    aiDescriptionUpdatedAt: {
      type: Date,
      default: null,
    },
    // 3D model URL (optional, for future AR model rendering)
    modelUrl: {
      type: String,
      default: null,
    },
    thumbnailUrl: {
      type: String,
      default: null,
    },
    facts: [
      {
        label: { type: String, required: true },
        value: { type: String, required: true },
      },
    ],
    isActive: {
      type: Boolean,
      default: true,
    },
  },
  { timestamps: true }
);

arMarkerSchema.index({ place: 1 });
arMarkerSchema.index({ detectionKeywords: 1 });

module.exports = mongoose.model('ARMarker', arMarkerSchema);
