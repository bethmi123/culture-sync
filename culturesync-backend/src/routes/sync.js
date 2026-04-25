const router   = require('express').Router();
const mongoose = require('mongoose');
const auth     = require('../middleware/auth');
const Session  = require('../models/Session');
const User     = require('../models/User');

// POST /api/sync/push — batch upsert sessions (PRD §5.3)
router.post('/push', auth, async (req, res, next) => {
  try {
    const { sessions } = req.body;
    if (!Array.isArray(sessions) || sessions.length === 0) {
      return res.status(400).json({ success: false, message: 'sessions array required' });
    }

    let synced = 0;
    for (const s of sessions) {
      const { techniqueId, techniqueName, startTime, endTime,
              durationSeconds, accuracyScore, frameCount } = s;

      if (accuracyScore == null || accuracyScore < 0 || accuracyScore > 100) continue;

      await Session.findOneAndUpdate(
        { user: req.userId, startTime },
        {
          techniqueId: String(techniqueId),
          techniqueName: techniqueName || `Technique ${techniqueId}`,
          endTime,
          durationSeconds,
          accuracyScore,
          frameCount: frameCount || 0
        },
        { upsert: true, new: true }
      );
      synced++;
    }

    // Recompute user aggregate stats
    const [stats] = await Session.aggregate([
      { $match: { user: new mongoose.Types.ObjectId(req.userId) } },
      { $group: {
        _id:             '$user',
        totalSessions:   { $sum: 1 },
        averageAccuracy: { $avg: '$accuracyScore' },
        bestAccuracy:    { $max: '$accuracyScore' },
      }},
    ]);
    if (stats) {
      await User.findByIdAndUpdate(req.userId, {
        totalSessions:   stats.totalSessions,
        averageAccuracy: Math.round(stats.averageAccuracy * 10) / 10,
        bestAccuracy:    Math.round(stats.bestAccuracy    * 10) / 10,
      });
    }

    res.status(201).json({ success: true, data: { synced } });
  } catch (e) { next(e); }
});

module.exports = router;
