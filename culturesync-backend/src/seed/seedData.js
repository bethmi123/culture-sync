/**
 * Seed script — populates MongoDB with real Beeralu technique data.
 * Run: npm run seed
 */
require('dotenv').config({ path: require('path').resolve(__dirname, '../../.env') });
const mongoose  = require('mongoose');
const Technique = require('../models/Technique');

const techniques = [
  {
    techniqueId: 'beeralu_basic',
    name: 'Basic Crossing (Lena Gassima)',
    description: 'The foundational cross-over movement of two bobbins. This is the building block of every Beeralu lace pattern. A beginner must master this before attempting any other technique.',
    difficulty: 'Beginner',
    durationMinutes: 10,
    steps: [
      'Hold two bobbins in each hand, threads hanging down from the pillow pins',
      'Cross the right bobbin over the left bobbin (this is the "lena" — the cross)',
      'Twist both pairs once in opposite directions (right pair clockwise, left anti-clockwise)',
      'Pull gently downward to tighten the thread and set the crossing',
      'Move the pins one position forward on the pattern and repeat',
    ],
  },
  {
    techniqueId: 'beeralu_cross',
    name: 'Cross & Twist (Gassima & Perawima)',
    description: 'Combines crossing and twisting in a repeating sequence to form the core grid structure of Beeralu lace. The tension must be even throughout.',
    difficulty: 'Beginner',
    durationMinutes: 15,
    steps: [
      'Start from the basic crossing position',
      'Twist the right bobbin pair once clockwise',
      'Twist the left bobbin pair once anti-clockwise',
      'Cross again (right over left)',
      'Repeat in a steady rhythm — cross, twist, cross, twist',
    ],
  },
  {
    techniqueId: 'beeralu_twist',
    name: 'Double Twist (Rassaya)',
    description: 'A double twist used in the border edges of the lace pattern. Requires precise finger control and even tension. This technique forms the raised edge bead of traditional Beeralu.',
    difficulty: 'Intermediate',
    durationMinutes: 20,
    steps: [
      'Hold the bobbin pair loosely between thumb and index finger',
      'Rotate the pair clockwise twice without crossing over the other pair',
      'Pinch firmly at the base of the twist to lock it',
      'Move to the next pin on the border',
      'The double twist creates a thicker, more visible border line',
    ],
  },
  {
    techniqueId: 'beeralu_diamond',
    name: 'Diamond Pattern (Tharanama)',
    description: 'Creates the classic diamond grid found in traditional Sri Lankan Beeralu tablecloths and saree borders. Requires coordinating four bobbins simultaneously.',
    difficulty: 'Intermediate',
    durationMinutes: 25,
    steps: [
      'Set up four bobbins in two pairs on adjacent pins',
      'Cross the inner bobbins (middle two) — one cross',
      'Now cross the outer left with the inner left — diagonal left',
      'Cross the outer right with the inner right — diagonal right',
      'Bring the two inner bobbins back together and cross again',
      'This completes one diamond unit — pin and continue',
    ],
  },
  {
    techniqueId: 'beeralu_flower',
    name: 'Flower Motif (Mal Athura)',
    description: 'A decorative floral pattern used in traditional saree borders and tablecloths. This is an advanced technique requiring mastery of all previous movements.',
    difficulty: 'Advanced',
    durationMinutes: 35,
    steps: [
      'Begin with six bobbins arranged in a fan pattern on the pricking',
      'Work a diamond with the centre four bobbins',
      'Using the outer two bobbins, arc over the diamond with a loose crossing',
      'Bring all six bobbins together at the base and pin',
      'The resulting shape forms one flower petal — rotate the pillow 60° and repeat five times',
    ],
  },
  {
    techniqueId: 'beeralu_wave',
    name: 'Wave Border (Dhara Seema)',
    description: 'A flowing wave edge finish applied to complete a Beeralu lace piece. Uses a diagonal cross pattern that creates a gentle scallop edge.',
    difficulty: 'Advanced',
    durationMinutes: 30,
    steps: [
      'Work four bobbins along the outer edge of the pricking',
      'Cross the first and second bobbins diagonally toward the edge pin',
      'Twist the outer bobbin pair twice to create the scallop curve',
      'Cross back diagonally inward and pin at the trough of the wave',
      'Alternate the direction on each repeat to produce the wave rhythm',
    ],
  },
];

async function seed() {
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('Connected to MongoDB');

    await Technique.deleteMany({});
    await Technique.insertMany(techniques);
    console.log(`Seeded ${techniques.length} Beeralu techniques`);

    process.exit(0);
  } catch (err) {
    console.error('Seed failed:', err.message);
    process.exit(1);
  }
}

seed();
