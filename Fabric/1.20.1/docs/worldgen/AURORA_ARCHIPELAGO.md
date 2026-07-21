# Aurora Archipelago world generation

The Aurora Dimension uses Minecraft 1.20.1's native noise generator. The dimension entry point is
`data/chaoticd/dimension/aurora_dimension.json`; the complete router and surface setup is in
`data/chaoticd/worldgen/noise_settings/aurora_archipelago.json`.

## Tuning map

| Control | File/value | Initial value | Effect |
| --- | --- | ---: | --- |
| Archipelago scale and spacing | `noise/aurora_macro.json` / `firstOctave` | `-11` | Lower values make broader groups and void gaps. |
| Main island size range | `noise/aurora_islands.json` / `firstOctave` | `-8` | Controls large and medium land masses inside each group. |
| Broken-edge intensity | `primary_footprint.json` / edge multiplier | `0.20` | Raises or lowers small-scale contour breakup. |
| Overall island density | `primary_footprint.json` / final bias | `-0.22` | More negative means fewer and narrower primary islands. |
| Main average height | `primary_vertical.json` / gradient midpoint | `Y 200` | Midpoint between `112` and `288`. |
| Main vertical variation | `primary_vertical.json` / elevation multiplier | `0.26` | Smoothly moves whole archipelagos up and down. |
| Main top level | `primary_density.json` / top constant | `0.27` | Average top is near Y 224 before elevation noise. |
| Main thickness/taper | `primary_density.json` / footprint multiplier | `1.15` | Higher values deepen island cores while edges remain thin. |
| Underside irregularity | `primary_density.json` / underside multiplier | `0.16` | Breaks and points the lower silhouette in 3D. |
| Secondary island amount | `secondary_footprint.json` / final bias | `-0.48` | More negative produces fewer satellites. |
| Secondary island size | `noise/aurora_secondary_islands.json` / `firstOctave` | `-6` | Controls small islands around the main masses. |
| Secondary height spread | `secondary_vertical.json` / elevation multiplier | `0.42` | Places satellites above and below the main band. |
| Hard generation band | `final_density.json` | `Y 64..311` | Guarantees complete void above, below and between archipelagos. |

Noise parameter files control frequency and octave character. Density-function files control weights,
thresholds, height and thickness. All samples use global world coordinates and Minecraft's seeded
`RandomState`, so the result is deterministic and seamless across chunk borders.

The Chaotic Apple calls `AuroraSafeArrival`: it searches the generator's height field for an ordinary
wide surface, materializes only that normal chunk, and teleports above it. It never creates a spawn
platform or edits terrain.
