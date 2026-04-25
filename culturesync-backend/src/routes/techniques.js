const router    = require('express').Router();
const Technique = require('../models/Technique');

// GET /api/techniques — sorted Beginner → Intermediate → Advanced
router.get('/', async (req, res, next) => {
  try {
    const techniques = await Technique.aggregate([
      { $match: { isPublished: true } },
      { $addFields: {
        _diffOrder: { $switch: {
          branches: [
            { case: { $eq: ['$difficulty', 'Beginner'] },     then: 1 },
            { case: { $eq: ['$difficulty', 'Intermediate'] }, then: 2 },
            { case: { $eq: ['$difficulty', 'Advanced'] },     then: 3 },
          ],
          default: 4,
        }},
      }},
      { $sort: { _diffOrder: 1, techniqueId: 1 } },
      { $project: { _diffOrder: 0 } },
    ]);
    res.json({ success: true, data: techniques });
  } catch (e) { next(e); }
});

// GET /api/techniques/:id
router.get('/:techniqueId', async (req, res, next) => {
  try {
    const t = await Technique.findOne({ techniqueId: req.params.techniqueId });
    if (!t) return res.status(404).json({ success: false, message: 'Technique not found' });
    res.json({ success: true, data: t });
  } catch (e) { next(e); }
});

module.exports = router;
