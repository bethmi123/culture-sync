const router = require('express').Router();
const mongoose = require('mongoose');
const auth = require('../middleware/auth');
const User = require('../models/User');

// GET /api/v1/users/:id
router.get('/:id', auth, async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
      return res.status(400).json({ success: false, message: 'Invalid userId format' });
    }
    const user = await User.findById(req.params.id).select('-password');
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    res.json({ success: true, data: user });
  } catch (e) {
    next(e);
  }
});

// PUT /api/v1/users/:id
router.put('/:id', auth, async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
      return res.status(400).json({ success: false, message: 'Invalid userId format' });
    }
    
    // Ensure the authenticated user matches the updated record (basic authorization)
    if (req.userId !== req.params.id) {
      return res.status(403).json({ success: false, message: 'Forbidden: Cannot update other users' });
    }

    const { name, email, userType, language, syncStatus } = req.body;
    let updateFields = {};
    if (name) updateFields.name = name;
    if (email) updateFields.email = email;
    if (userType) updateFields.userType = userType;
    if (language) updateFields.language = language;
    if (syncStatus) updateFields.syncStatus = syncStatus;

    const updatedUser = await User.findByIdAndUpdate(
      req.params.id,
      { $set: updateFields },
      { new: true, runValidators: true }
    ).select('-password');

    if (!updatedUser) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    
    res.json({ success: true, data: updatedUser });
  } catch (e) {
    if (e.code === 11000) {
      return res.status(400).json({ success: false, message: 'Email already exists' });
    }
    next(e);
  }
});

module.exports = router;
