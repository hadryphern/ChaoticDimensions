# Aurora procedural trees

Aurora trees use a code-backed, data-driven feature for Fabric/Minecraft 1.20.1. A single
configured feature can produce young, tall, branching, ancient and monumental silhouettes without
structure templates or fixed coordinates. The existing Aurora log and pink, purple and blue leaves
remain the only blocks used by the generator.

## Architecture

| Layer | Responsibility |
| --- | --- |
| `ModWorldgenFeatures` | Registers the `chaoticd:aurora_tree` feature before worldgen datapacks are decoded. |
| `AuroraTreeConfiguration` | Defines the Codec for the block palette, weighted profiles and safety radius. |
| `AuroraTreePlanner` | Pure deterministic planner. It calculates all logs, leaves and required ground without reading or changing a world. |
| `AuroraTreeFeature` | Validates the complete plan against the world, then places it atomically. |
| `pastel_aurora_trees.json` | Central tuning file for every profile and the three foliage colors. |
| `AuroraTreeValidator` | Headless statistical and structural acceptance test for the planner. |

Runtime flow:

1. Minecraft selects a surface position through the placed feature.
2. The planner mixes the world seed, block position and a stable Aurora salt into a local random
   source.
3. It selects one weighted profile and one weighted foliage color.
4. It plans the tapered trunk, connected curves, branches, optional roots and irregular canopy.
5. It calculates leaf distance from the final log graph.
6. The feature validates the complete plan. If any required position is unsafe, nothing is placed.
7. Logs are placed from bottom to top, followed by leaves with their calculated state.

The biome continues to reference `chaoticd:pastel_aurora_trees`. Its configured feature now points
directly to `chaoticd:aurora_tree`; the three obsolete vanilla-style configured trees were removed.

## Weighted profiles

The weights total 100 and are intentionally non-uniform. Small and ordinary trees remain common,
while trees that require more flat ground and clearance become progressively rarer.

| Profile | Weight | Height | Trunk | Segments | Branches | Branch length | Canopy radius | Canopy blobs |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `young_simple` | 36% | 4–7 | 1×1 | 2–3 | 0–2 | 2–3 | 2–3 | 2–4 |
| `slender_high` | 24% | 8–13 | 1×1 | 4–6 | 2–5 | 2–4 | 2–4 | 3–6 |
| `branching_mature` | 23% | 7–12 | 1×1–2×2 | 4–7 | 5–9 | 3–6 | 3–5 | 5–9 |
| `ancient_wide` | 15% | 10–17 | 2×2–3×3 | 6–9 | 8–13 | 4–6 | 4–6 | 8–13 |
| `monumental` | 2% | 18–28 | irregular 3×3 | 8–12 | 13–20 | 5–7 | 5–7 | 12–20 |

Profile-specific curve, root, secondary-branch and canopy settings produce additional variation
inside every row. A profile does not correspond to a separately registered tree type.

## JSON tuning reference

The active configuration is
`data/chaoticd/worldgen/configured_feature/pastel_aurora_trees.json`.

### Root fields

| Field | Purpose |
| --- | --- |
| `trunk_state` | Log state used by trunks, branches and roots. Axis is corrected for each generated segment. |
| `ground_states` | Blocks allowed below every required trunk/root support position. Currently Pastel Grass and Pastel Soil. |
| `foliage_palette` | Weighted leaf states. Pink, purple and blue currently use equal weights. |
| `profiles` | Weighted list of procedural shape profiles. |
| `max_horizontal_reach` | Maximum X/Z distance from the origin. It is `8`, keeping writes inside the neighboring-chunk worldgen region. |

### `size` fields

| Field | Purpose |
| --- | --- |
| `min_height`, `max_height` | Inclusive trunk-height interval. |
| `min_trunk_width`, `max_trunk_width` | Inclusive width interval, restricted to 1–3 blocks. |
| `min_segments`, `max_segments` | Number of smooth directional sections in the trunk. |

### `shape` fields

| Field | Purpose |
| --- | --- |
| `inclination_chance` | Chance that the trunk begins with a horizontal growth vector. |
| `bend_chance` | Chance that later segments adjust their direction. |
| `bend_strength` | Maximum horizontal drift applied gradually between segments. |
| `min_branches`, `max_branches` | Inclusive number of primary branch attempts. |
| `branch_start_fraction` | Fraction of trunk height above which branches may begin. |
| `min_branch_length`, `max_branch_length` | Inclusive primary branch length. |
| `max_branch_width` | Allows the lower portion of some mature branches to become two blocks thick. |
| `secondary_branch_chance` | Chance for a primary branch to split once. |
| `root_chance` | Chance to add connected surface roots. |
| `max_root_length` | Maximum length of each surface root. |

### `canopy` fields

| Field | Purpose |
| --- | --- |
| `min_radius`, `max_radius` | Main canopy radius interval. |
| `min_blobs`, `max_blobs` | Number of overlapping leaf volumes used to break up the silhouette. |
| `density` | Per-position leaf density. The core is slightly denser and the boundary remains ragged. |
| `vertical_scale` | Stretches or compresses leaf volumes vertically. |
| `min_tip_radius`, `max_tip_radius` | Leaf-blob radius at primary and secondary branch tips. |

All ranges are Codec-validated. Cross-field checks reject inverted minimum/maximum pairs and empty
palettes or profile lists during datapack loading.

## Geometry and connectivity

- Trunks grow one vertical layer at a time through smooth directional segments. Drift is clamped and
  eased back toward the origin before it can exceed the configured reach.
- When a curve changes both X and Z, connector logs create a face-connected path rather than a
  diagonal corner contact.
- A 3×3 trunk always retains its structural center and cross. Selected corners are omitted using a
  stable positional hash, preventing perfect cylinders while preserving connectivity.
- Thick trunks taper from 3×3 to 2×2 to 1×1, or from 2×2 to 1×1, as they rise.
- Branches are distributed around the trunk with angular jitter, variable rise or descent and
  optional secondary splits. Every horizontal step receives the corresponding X or Z log axis.
- Roots start inside the trunk and use the same face-connected line builder as branches.
- Main and branch-tip canopies overlap several noisy ellipsoidal blobs. Sparse boundaries and
  partial overlap avoid perfect spheres, cubes and fully sealed crowns.

## Leaves and decay

The planner first creates the complete log graph and a larger set of possible leaf positions. It
then performs a breadth-first search outward from every log through those candidates.

- Only leaves reached within distances 1–6 are retained.
- The exact BFS distance is written to `LeavesBlock.DISTANCE`.
- `persistent` is always `false` and `waterlogged` is always `false` for world-generated leaves.
- Candidates disconnected from all logs are discarded instead of becoming floating foliage.
- Branch tips are required to have attached foliage; completely enclosed branch endpoints are not
  treated as exposed tips.
- Removing supporting logs therefore allows the existing `LeavesBlock` decay behavior to work
  normally.

Each generated tree selects one of the existing pink, purple or blue leaf blocks. Their rendering,
transparency, loot and block behavior are not replaced by the generator.

## Placement safety

No blocks are written while planning or validation is in progress. The complete tree is rejected if
any of these conditions fails:

- a planned block is outside the dimension build height;
- `WorldGenLevel.ensureCanWrite` rejects a position;
- any required trunk or root support is not an allowed Aurora surface block;
- a destination contains fluid;
- a destination is tagged `features_cannot_replace`;
- a destination already contains logs or leaves from another tree;
- a destination is neither air nor tagged `replaceable_by_trees`.

This all-or-nothing check prevents clipped crowns, half-generated monumental trees, logs through
ores or terrain, and trees that merge unpredictably with an existing canopy. Thick and monumental
profiles naturally fail more often on narrow or uneven island edges, making their effective rarity
slightly lower than their configured selection weight.

The placed feature uses `WORLD_SURFACE_WG`, a zero-water-depth filter, a valid-sapling support
predicate and the biome filter. It retains the previous weighted count of one or two attempts per
chunk.

## Determinism

The planner does not consume shared mutable world randomness. Its local seed is derived from:

- the Aurora world seed;
- `BlockPos.asLong()` for the selected surface position;
- a constant Aurora-tree salt.

The same world seed and position therefore produce the same profile, color and geometry across
reloads. Different positions do not depend on feature execution order. Irregular 3×3 corner removal
also uses a stable coordinate hash, so it remains repeatable.

## Validator results

`AuroraTreeValidator` decoded the active JSON and inspected 6,000 deterministic tree plans. It
checked replay equality, log connectivity by six-direction flood fill, horizontal branch axes,
ground-to-log correspondence, branch-tip foliage, maximum reach, leaf persistence and exact BFS
distance.

| Metric | Result |
| --- | ---: |
| `young_simple` | 2,129 (35.48%) |
| `slender_high` | 1,470 (24.50%) |
| `branching_mature` | 1,400 (23.33%) |
| `ancient_wide` | 878 (14.63%) |
| `monumental` | 123 (2.05%) |
| Completely straight trunks | 2,729 |
| Curved or inclined trunks | 3,271 |
| Generated height range | 4–28 |
| Log-count range | 4–337 |
| Leaf-count range | 18–1,515 |
| Foliage colors observed | 3/3 |
| Trunk widths observed | 1×1, 2×2 and 3×3 |
| Normalized structural signatures | 6,000/6,000 unique |

The configured feature also decoded successfully during `validateAuroraWorldgen`, alongside the
dimension's registry, seed, cluster and safe-arrival checks.
