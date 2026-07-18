# Forge 1.20.1 to Fabric migration

## Source of truth

- Reference archive: `.migration/forge-1.20.1/original-forge.jar`
- Decompiled reference: `.migration/forge-1.20.1/decompiled/`
- Original loader: Forge 47+ (Minecraft 1.20.1)
- Original namespace: `chaosentitymod`
- Fabric namespace: `chaoticd`

The original is an MCreator-generated Forge project. Its generated classes are
kept outside `src/main` as a read-only reference: copying them directly would
reintroduce Forge APIs and make the Fabric project unbuildable.

## Imported now

- 514 client resources in `src/main/resources/assets/chaoticd/`
- 257 loader-neutral datapack files in `src/main/resources/data/chaoticd/`
- 38 vanilla tag files in `src/main/resources/data/minecraft/`
- The resource namespace has been changed from `chaosentitymod` to `chaoticd`.

The original Forge biome modifiers are intentionally kept in
`.migration/forge-1.20.1/forge-biome-modifiers/`. They need to become Fabric
`BiomeModifications` registrations and must not be packaged as Forge data.

## Porting order

1. `content/block` — 53 block definitions and block items.
2. `content/item` — 60 standalone items, tools and armor.
3. `content/entity` and `client/render` — 7 entities, spawn eggs, attributes
   and GeckoLib renderers.
4. `world` — dimensions, teleport items, worldgen and biome modifiers.
5. `menu` and `client/screen` — the leather backpack menu and GUI.
6. `network` and `procedure` — replace Forge events, capabilities and packets
   with Fabric events, inventory implementations and networking.

## Target package layout

```text
net.blue.chaoticd
├── content/{block,item,entity,menu,tab}
├── client/{render,screen}
├── world/{dimension,worldgen}
├── network
├── procedure
└── registry
```

Only code that has been converted and compiled belongs in this layout.
