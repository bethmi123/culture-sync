const router = require('express').Router();
const User   = require('../models/User');

// GET /api/leaderboard — top 20 by average accuracy
router.get('/', async (req, res, next) => {
  try {
    const users = await User.find({ totalSessions: { $gt: 0 } })
      .select('name averageAccuracy bestAccuracy totalSessions')
      .sort({ averageAccuracy: -1 })
      .limit(20);

    const leaderboard = users.map((u, i) => ({
      userId:          u._id,
      userName:        u.name,
      averageAccuracy: u.averageAccuracy,
      bestAccuracy:    u.bestAccuracy,
      totalSessions:   u.totalSessions,
      rank:            i + 1,
    }));

    res.json({ success: true, data: leaderboard });
  } catch (e) { next(e); }
});

module.exports = router;
