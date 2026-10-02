# Pale Lord artwork

The model uses solid branches, shoulder knots, articulated asymmetric arms,
root claws, root feet, and an amber resin heart. The boss and its clone share
the existing renderer and texture resource. Entity attributes and hitboxes
are unchanged.

The texture was redrawn with the built-in imagegen tool. `source.png` preserves
the generated artwork. The generator did not place the UV islands precisely,
so `pack-texture.ps1` extracts its faces and packs them with nearest-neighbour
sampling into the model's 128 × 128 atlas. A manual palette adjustment slightly
reduces bark color variation in 24-step brightness bands while retaining
amber and moss accents. Only the
six faces of each UV island are opaque; all unused pixels are transparent,
including the empty spaces beside the top and bottom faces.

Run from the repository root to regenerate the game texture:

```powershell
./docs/art/pale-lord/pack-texture.ps1
```

The script overwrites the tracked texture; preserve local edits before running.

| Part | UV origin | Cuboid width × height × depth |
| --- | --- | --- |
| Head | 0, 0 | 6 × 10 × 6 |
| Body | 0, 24 | 10 × 14 × 6 |
| Right arm / forearm | 40, 0 | 4 × 10 × 4 |
| Left arm / forearm | 60, 0 | 4 × 9 × 4 |
| Right leg | 40, 28 | 4 × 18 × 4 |
| Left leg | 60, 28 | 4 × 18 × 4 |
| Crown / forks | 84, 0 | 2 × 7 × 2 |
| Main branches | 84, 12 | 2 × 10 × 2 |
| Shoulder knots | 0, 50 | 6 × 4 × 6 |
| Root claws | 84, 28 | 1 × 5 × 1 |
| Right foot | 40, 54 | 5 × 3 × 8 |
| Left foot | 72, 54 | 5 × 3 × 8 |
| Resin heart | 0, 64 | 4 × 6 × 1 |

Upper arms and forearms intentionally share their UV islands. Repeated
branches, claws, and shoulder knots also share material islands.

In-game review: inspect the boss and clone from every side, confirm the eyes
and chest texture, then check standing, walking and head tracking.
The resin colors are painted highlights and do not emit light.

## Imagegen prompt

Use case: stylized-concept. Asset type: production Minecraft entity UV texture atlas, not a character drawing. Redraw the provided Pale Lord texture in a refined vanilla Minecraft pixel art aesthetic: weathered pale ivory / ash grey bark, charcoal vertical fissures, subtle desaturated moss, amber-orange eyes and heart. Output square atlas, treat it as EXACTLY 128 by 128 logical pixels; render each logical pixel as an 8x8 flat square (1024 square output), NO smoothing, NO labels, NO perspective, NO lighting gradients, NO grid lines. All coordinates below are logical 128x128 coordinates, measured from top left. Background solid dark grey. Keep all specified material rectangles completely opaque and filled to their edges.
Cuboid UV nets use top row two caps at (u+d,v) and (u+d+w,v), each w*d, then below at y=v+d a horizontal strip of four faces widths d,w,d,w and height h.
Place these nets precisely:
head at u=0 v=0 w=6 h=10 d=6 (24x16 bounding area): pale bark carved mask. Front face x=6..11 y=6..15 has two tiny amber eyes at (7,9),(10,9), dark sockets, black narrow mouth. Other faces bark.
torso at u=0 v=24 w=10 h=14 d=6 (32x20), bark rib-like grooves, dark hollow center front x=6..15 y=30..43.
right arm u=40 v=0 w=4 h=20 d=4 (16x24), bark;
left arm u=60 v=0 w=4 h=18 d=4 (16x22), bark;
right leg u=40 v=28 w=4 h=18 d=4 (16x22), bark;
left leg u=60 v=28 w=4 h=18 d=4 (16x22), bark;
crown branch u=84 v=0 w=2 h=7 d=2 (8x9), pale bark dark tips;
antler branch u=84 v=12 w=2 h=10 d=2 (8x12), pale bark;
shoulder knot u=0 v=50 w=6 h=4 d=6 (24x10), gnarled bark;
claw u=84 v=28 w=1 h=5 d=1 (4x6), dark root;
right root foot u=40 v=54 w=5 h=3 d=8 (26x11), dark grey bark;
left root foot u=72 v=54 w=5 h=3 d=8 (26x11), dark grey bark;
heart u=0 v=64 w=4 h=6 d=1 (10x7): bright amber resin, dark border. Front x=1..4 y=65..70.
Everywhere else dark grey. The purpose is an actual game texture whose coordinates must correspond to the nets above. Preserve crisp subdued bark identity of reference, improve readability.
