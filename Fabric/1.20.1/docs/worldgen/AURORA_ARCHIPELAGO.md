# Aurora Archipelago world generation

The Aurora Dimension uses Minecraft 1.20.1's native, seed-driven noise generator. Its dimension entry
point is `data/chaoticd/dimension/aurora_dimension.json`; the noise router, surface rules and terrain
band are defined by `data/chaoticd/worldgen/noise_settings/aurora_archipelago.json`.

The horizontal distribution is deliberately multi-scale. It does not place islands from fixed
coordinates, a grid or a list of structures. Every field is sampled in global world coordinates, so
the result is deterministic for a seed, continuous across chunk borders and valid at arbitrary
distances from the origin.

## Distribution architecture

The final terrain is the maximum of three density branches. They share the established tapered-island
construction, while each branch has a different distribution role:

1. `primary_density` creates the large and medium masses that define an archipelago.
2. `secondary_density` adds smaller satellites, short bridge gaps and intermediate islands inside or
   around a cluster.
3. `isolated_density` sparsely permits an island in otherwise negative cluster territory, producing
   long-distance landmarks without filling the large voids.

`final_density.json` combines the branches with `max` and applies the existing hard generation band at
Y 64 through Y 311. The vertical gradients, elevation noises and underside noise remain independent
from the horizontal cluster selection. Consequently, distribution can be tuned without redesigning
the islands' established upper surface and tapered underside.

## Macro cluster field

`cluster_field.json` is the common large-scale control used by the primary and secondary branches:

```text
clamp(1.25 * macro(-11) + 0.45 * cluster_detail(-9), -1.0, 1.0)
```

- `aurora_macro` (`firstOctave: -11`) creates broad dense, moderate and empty regions.
- `aurora_cluster_detail` (`firstOctave: -9`) breaks up the macro field so cluster boundaries do not
  look circular, regular or predictable.
- Clamping prevents extreme noise values from destabilizing downstream density functions.

This stronger macro contrast is what makes one sampled region a compact archipelago and another an
extensive void. It is intentionally not a global reduction of island spacing.

## Tuning map

| Control | File/value | Current value | Effect |
| --- | --- | ---: | --- |
| Broad cluster scale | `noise/aurora_macro.json` / `firstOctave` | `-11` | Lower values create broader cluster and void regimes. |
| Cluster boundary detail | `noise/aurora_cluster_detail.json` / `firstOctave` | `-9` | Adds medium-scale variation to the macro regions. |
| Cluster field weights | `cluster_field.json` | `1.25 / 0.45` | Controls macro dominance versus local cluster breakup. |
| Main island scale | `noise/aurora_islands.json` / `firstOctave` | `-8` | Controls large and medium land masses inside clusters. |
| Primary distribution | `primary_footprint.json` | `0.92 cluster + 0.88 islands + 0.20 edges - 0.23` | Raises or lowers the main archipelago coverage. |
| Primary positive cap | `primary_footprint.json` / `max` | `0.82` | Preserves a tapered underside in very dense cluster cores. |
| Satellite scale | `noise/aurora_secondary_islands.json` / `firstOctave` | `-6` | Controls small islands and short gaps around main masses. |
| Secondary distribution | `secondary_footprint.json` | `0.70 cluster + 1.02 islands + 0.15 edges - 0.52` | Controls intermediate and nearly connected islands. |
| Secondary positive cap | `secondary_footprint.json` / `max` | `0.72` | Prevents satellites from becoming deep vertical masses. |
| Isolated-island scale | `noise/aurora_isolated_islands.json` / `firstOctave` | `-7` | Controls the shape and spacing of rare isolated islands. |
| Isolated distribution | `isolated_footprint.json` | `1.20 islands - 0.20 cluster + 0.10 edges - 0.82` | Favours rare islands on the negative side of the cluster field. |
| Isolated positive cap | `isolated_footprint.json` / `max` | `0.70` | Keeps isolated islands within the established silhouette. |
| Broken-edge scale | `noise/aurora_edges.json` / `firstOctave` | `-5` | Adds small-scale contour breakup to every branch. |
| Main average height | `primary_vertical.json` / gradient | `Y 112..288` | Places the main midpoint around Y 200. |
| Main vertical variation | `primary_vertical.json` / elevation multiplier | `0.26` | Smoothly moves primary masses up and down. |
| Secondary height spread | `secondary_vertical.json` / elevation multiplier | `0.42` | Gives satellites and isolated islands a broader height range. |
| Hard generation band | `final_density.json` | `Y 64..311` | Guarantees void above and below the archipelagos. |

The footprint weights and biases are density thresholds, not literal distances in blocks. Approximate
distance classes emerge from the interaction among the macro field and the `-8`, `-7`, `-6` and `-5`
noise scales. To make a branch rarer, make its final bias more negative. To alter island shape rather
than frequency, tune that branch's island or edge noise instead of its macro weight.

The positive footprint caps are important. Before they were introduced, an exceptionally positive
cluster core could make a solid column 161 blocks deep and weaken the conical underside. The caps do
not change the X/Z land mask—the zero crossing still occurs at the same place—but reduced the worst
validated contiguous thickness to 95 blocks.

## Acceptance validator

`src/test/java/net/blue/chaoticd/validation/AuroraWorldgenValidator.java` loads the real registries and
datapack, then samples three fixed test seeds. For every seed it scans five 2048×2048 regions centred
at `(0, 0)`, `(1000, 1000)`, `(-1000, -1000)`, `(5000, 5000)` and `(-5000, -5000)` using a 32-block
sampling step.

The validator measures:

- total land coverage and variation among origin, ±1000 and ±5000;
- connected land groups with eight-neighbour sampling;
- almost-connected gaps up to 48 blocks and nearby gaps up to 128 blocks;
- small intermediate islands, represented by at most 12 sampled columns;
- isolated components whose nearest sampled neighbour is at least 224 blocks away;
- dense and empty 9×9 sample windows, scanned with a four-sample stride;
- the largest entirely empty sampled square;
- vertical band, average thickness, maximum contiguous thickness and absence of a lower floor;
- safe arrival within 2048 blocks, deterministic reloads and different signatures between seeds.

Current safeguards reject a regional suspended supercontinent at 82% coverage, require at least ten
percentage points of coverage variation, require dense and empty windows, require a void span of at
least 256 blocks, and require nearby, almost-connected, intermediate and isolated island examples.
Contiguous thickness must remain between the established minimum and a 128-block upper safety limit.

### Current validation result

| Seed | Coverage range | Nearby / almost connected | Small intermediates | Isolated | Largest empty span |
| ---: | ---: | ---: | ---: | ---: | ---: |
| `1247901281` | `4.7%..39.8%` | `304 / 97` | `182` | `14` | `768` blocks |
| `71972686885473` | `6.9%..54.3%` | `384 / 125` | `244` | `5` | `576` blocks |
| `-18973591130630499` | `7.8%..40.8%` | `354 / 107` | `234` | `4` | `608` blocks |

Run the acceptance test after any terrain tuning:

```bash
./gradlew validateAuroraWorldgen
```

Use `--args='--render'` to additionally write an overhead map and cross-section to `/tmp`. A tuning
change should be accepted only if the validator still demonstrates both compact archipelagos and
meaningful empty/isolated regions across all three seeds.

## Safe arrival

The Chaotic Apple calls `AuroraSafeArrival`. It searches the generator's native height field for an
ordinary wide surface, materializes only that normal chunk and teleports above it. It does not create
a platform or edit terrain, so safe arrival remains representative of the procedural archipelago.
