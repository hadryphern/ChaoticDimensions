# Aurora block-family rework

The Aurora family was rebuilt as an authored 128×128 texture set. The active pack contains fourteen
block textures shared by thirteen registered blocks. The former 16×16/32×32 recolored and green-matte
assets were replaced; their untouched backup remains in the local `.assets/Removed Assets` archive.

## Registered family

- terrain: Pastel Aurora Grass, Pastel Aurora Soil and Pastel Aurora Stone;
- wood: log, wood, stripped log, stripped wood and planks;
- foliage: pink, purple and blue leaves;
- resources: Sapphire Ore and Rosalita Ore.

Wood and stripped wood intentionally reuse their matching side textures on every face. This follows
Minecraft's wood-block semantics without duplicating identical PNGs.

## Vanilla-compatible behavior

- logs and wood retain `axis`, rotate on X/Y/Z and can be stripped with an axe;
- all wood-family blocks and leaves use vanilla-equivalent flammability values;
- leaves use `LeavesBlock`, `cutoutMipped`, real transparent pixels, distance decay, shears/Silk Touch
  harvesting, stick drops and composting;
- Aurora grass spreads only onto Aurora soil and becomes Aurora soil when covered;
- grass/soil, wood/foliage, stone/ores are assigned to shovel, axe/hoe and pickaxe tags respectively;
- Aurora Stone currently drops itself because an Aurora cobblestone variant does not exist;
- Sapphire and Rosalita ores require Fabric mining level 4 and award 3–7 experience;
- Sapphire supports Silk Touch and Fortune; Rosalita intentionally drops its ore block because no
  non-legacy Rosalita gem is currently registered;
- logs, wood, planks and leaves participate in the matching Minecraft block and item tags;
- planks and both wood variants have survival recipes.

## World generation

Aurora Stone is the island core. The surface rule places Pastel Aurora Grass on top and approximately
three blocks of Pastel Aurora Soil below it. Both ores replace only blocks from
`chaoticd:aurora_ore_replaceables`, which currently contains Aurora Stone.

Existing generated chunks are not rewritten. Validate terrain changes in a new world or unexplored
Aurora chunks.

## Verification

```bash
./gradlew build
./gradlew validateAuroraWorldgen
./gradlew runClient
```

Both Aurora validators are part of `check`. `validateAuroraAssets` verifies JSON parsing,
model/texture references, complete
block coverage, 128×128 dimensions, leaf alpha ranges, chroma-key removal, tags and all four language
files (`pt_br`, `en_us`, `es_co`, `es_mx`). The native world-generation validator samples three seeds
and distant coordinates, checks deterministic seamless generation and proves that safe arrival has a
solid surface without a generated floor.
