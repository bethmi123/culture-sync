const router   = require('express').Router();
const mongoose = require('mongoose');
const auth     = require('../middleware/auth');
const Session  = require('../models/Session');
const User     = require('../models/User');

// POST /api/sessions  — upload a completed practice session
router.post('/', auth, async (req, res, next) => {
  try {
    const { techniqueId, techniqueName, startTime, endTime,
            durationSeconds, accuracyScore, frameCount } = req.body;

    if (accuracyScore == null || accuracyScore < 0 || accuracyScore > 100) {
      return res.status(400).json({ success: false, message: 'accuracyScore must be 0–100' });
    }
    if (!techniqueId || (typeof techniqueId !== 'string' && typeof techniqueId !== 'number') || String(techniqueId).trim() === '') {
      return res.status(400).json({ success: false, message: 'techniqueId is required' });
    }
    if (frameCount == null || frameCount < 0) {
      return res.status(400).json({ success: false, message: 'frameCount must be non-negative' });
    }

    const session = await Session.create({
      user: req.userId, techniqueId: String(techniqueId), techniqueName,
      startTime, endTime, durationSeconds, accuracyScore, frameCount
    });

    // Recompute user aggregate stats with a single aggregation query
    const [stats] = await Session.aggregate([
      { $match: { user: new mongoose.Types.ObjectId(req.userId) } },
      { $group: {
        _id:             '$user',
        totalSessions:   { $sum: 1 },
        averageAccuracy: { $avg: '$accuracyScore' },
        bestAccuracy:    { $max: '$accuracyScore' },
      }},
    ]);
    await User.findByIdAndUpdate(req.userId, {
      totalSessions:   stats.totalSessions,
      averageAccuracy: Math.round(stats.averageAccuracy * 10) / 10,
      bestAccuracy:    Math.round(stats.bestAccuracy    * 10) / 10,
    });

    res.status(201).json({ success: true, data: session });
  } catch (e) { next(e); }
});

// GET /api/v1/sessions?userId=x
router.get('/', auth, async (req, res, next) => {
  try {
    const userId = req.query.userId || req.userId; // fallback to self if not provided
    if (!mongoose.Types.ObjectId.isValid(userId)) {
      return res.status(400).json({ success: false, message: 'Invalid userId format' });
    }
    const sessions = await Session.find({ user: userId })
      .sort({ createdAt: -1 })
      .limit(50);
    res.json({ success: true, data: sessions });
  } catch (e) { next(e); }
});

// PUT /api/sessions/:id — update / close session
router.put('/:id', auth, async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
      return res.status(400).json({ success: false, message: 'Invalid sessionId format' });
    }

    const session = await Session.findOne({ _id: req.params.id, user: req.userId });
    if (!session) {
      return res.status(404).json({ success: false, message: 'Session not found or forbidden' });
    }

    const { endTime, durationSeconds, accuracyScore, frameCount, status } = req.body;
    
    if (endTime !== undefined) session.endTime = endTime;
    if (durationSeconds !== undefined) session.durationSeconds = durationSeconds;
    if (accuracyScore !== undefined) session.accuracyScore = accuracyScore;
    if (frameCount !== undefined) session.frameCount = frameCount;
    if (status !== undefined) session.status = status;

    await session.save();

    // Optionally update user stats if accuracy was updated
    if (accuracyScore !== undefined) {
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
    }

    res.json({ success: true, data: session });
  } catch (e) { next(e); }
});

module.exports = router;
