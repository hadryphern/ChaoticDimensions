# Auditoria de assets Rosalita

Esta prévia foi composta diretamente dos PNGs-fonte abaixo, com ampliação nearest-neighbor; ela não usa uma imagem conceitual externa.

- Contact sheet: `docs/previews/rosalita-final-assets.png`
- Fonte carregada pelo Gradle: `src/main/resources/assets/chaoticd/textures/block/`
- `build/resources/main` é apenas a cópia de processamento de recursos do Gradle.
- Somente `rosalita_leaves` possui provider de cor; madeira e derivados não recebem tint biome.

## PNGs-fonte e hashes

| Arquivo | Dimensão | SHA-256 |
| --- | --- | --- |
| `textures/block/rosalita_log.png` | 16×16 | `78c80a0d12765559c2dc77f9abf27451417355b5179127118743acc3c89e8664` |
| `textures/block/rosalita_log_top.png` | 16×16 | `f61da7e7fb02d7c4f98c459705eb2fb3ab2668a81caeb1405d2aa4cfbee987c0` |
| `textures/block/stripped_rosalita_log.png` | 16×16 | `ba4949b406a492ce87e0ccf3de3d8f0a1b9f2105af9fb779139e6015bf4e6fe2` |
| `textures/block/stripped_rosalita_log_top.png` | 16×16 | `f61da7e7fb02d7c4f98c459705eb2fb3ab2668a81caeb1405d2aa4cfbee987c0` |
| `textures/block/rosalita_wood.png` | 16×16 | `78c80a0d12765559c2dc77f9abf27451417355b5179127118743acc3c89e8664` |
| `textures/block/stripped_rosalita_wood.png` | 16×16 | `ba4949b406a492ce87e0ccf3de3d8f0a1b9f2105af9fb779139e6015bf4e6fe2` |
| `textures/block/rosalita_planks.png` | 16×16 | `185241aa8f4b71e875e0eaf85d05ed9e53197bc56e02795debd8433bd833c9aa` |
| `textures/block/rosalita_door.png` | 16×32 | `d9ca3e35dd79622ddf7aed0f9ad8263ec4575341f0cc9ef94921093b658806d5` |
| `textures/block/rosalita_door_cima.png` | 16×32 | `8ae41622a1b87b76b957cd200e7b1a5f4fcdf6ebe061691cf1546cc45a0d78da` |
| `textures/block/rosalita_trapdoor.png` | 16×16 | `14c0a24b0948facffd80a02db1fa4242c2aad755bf5ae9a11baf7c4db18ec7f6` |
| `textures/block/rosalita_crafting_table.png` | 16×16 | `27315cfb5e8bdb6d72922c4357f182dfbd6363d1f3a980fb1e2a9605ee2bc27c` |
| `textures/block/rosalita_chest.png` | 16×16 | `3d26950689e546193cbefea9cd5c6d30d8d41d23d63d1a865f16ac818ef6486a` |
| `textures/block/rosalita_trapped_chest.png` | 16×16 | `442a88f1da111ef451e8c65b73ac1667e1e765def02a1390f658b47081f0c8d1` |
| `textures/block/rosalita_barrel.png` | 16×16 | `b1b8c919bf2752ce963346ef58448e864af04d080b77bf270849da4ac82d6dd3` |

## Referências de modelos

- `block/rosalita_log.png`: `models/block/rosalita_log.json`, `models/item/rosalita_log.json`
- `block/rosalita_log_top.png`: `models/block/rosalita_log.json`
- `block/stripped_rosalita_log.png`: `models/block/stripped_rosalita_log.json`, `models/item/stripped_rosalita_log.json`
- `block/stripped_rosalita_log_top.png`: `models/block/stripped_rosalita_log.json`
- `block/rosalita_wood.png`: `models/block/rosalita_wood.json`, `models/item/rosalita_wood.json`
- `block/stripped_rosalita_wood.png`: `models/block/stripped_rosalita_wood.json`, `models/item/stripped_rosalita_wood.json`
- `block/rosalita_planks.png`: `models/block/rosalita_planks.json`, `models/block/rosalita_stairs.json`, `models/block/rosalita_stairs_inner.json`, `models/block/rosalita_stairs_outer.json`, `models/block/rosalita_slab.json`, `models/block/rosalita_slab_full.json`, `models/block/rosalita_slab_top.json`, `models/block/rosalita_fence.json`, `models/block/rosalita_fence_inventory.json`, `models/block/rosalita_fence_post.json`, `models/block/rosalita_fence_gate.json`, `models/block/rosalita_fence_gate_open.json`, `models/block/rosalita_fence_gate_wall.json`, `models/block/rosalita_fence_gate_wall_open.json`, `models/block/rosalita_door_bottom_left.json`, `models/block/rosalita_door_bottom_left_open.json`, `models/block/rosalita_door_bottom_right.json`, `models/block/rosalita_door_bottom_right_open.json`, `models/block/rosalita_door_top_left.json`, `models/block/rosalita_door_top_left_open.json`, `models/block/rosalita_door_top_right.json`, `models/block/rosalita_door_top_right_open.json`, `models/block/rosalita_pressure_plate.json`, `models/block/rosalita_pressure_plate_down.json`, `models/block/rosalita_button.json`, `models/block/rosalita_button_inventory.json`, `models/block/rosalita_button_pressed.json`, `models/item/rosalita_planks.json`, `models/item/rosalita_fence.json`, `models/item/rosalita_fence_gate.json`, `models/item/rosalita_button.json`
- `block/rosalita_door.png`: `models/block/rosalita_door_bottom_left.json`, `models/block/rosalita_door_bottom_left_open.json`, `models/block/rosalita_door_bottom_right.json`, `models/block/rosalita_door_bottom_right_open.json`, `models/block/rosalita_door_top_left.json`, `models/block/rosalita_door_top_left_open.json`, `models/block/rosalita_door_top_right.json`, `models/block/rosalita_door_top_right_open.json`, `models/item/rosalita_door.json`
- `block/rosalita_door_cima.png`: `models/block/rosalita_door_bottom_left.json`, `models/block/rosalita_door_bottom_left_open.json`, `models/block/rosalita_door_bottom_right.json`, `models/block/rosalita_door_bottom_right_open.json`, `models/block/rosalita_door_top_left.json`, `models/block/rosalita_door_top_left_open.json`, `models/block/rosalita_door_top_right.json`, `models/block/rosalita_door_top_right_open.json`
- `block/rosalita_trapdoor.png`: `models/block/rosalita_trapdoor.json`, `models/block/rosalita_trapdoor_open.json`, `models/block/rosalita_trapdoor_top.json`, `models/item/rosalita_trapdoor.json`
- `block/rosalita_crafting_table.png`: `models/block/rosalita_crafting_table.json`, `models/item/rosalita_crafting_table.json`
- `block/rosalita_chest.png`: `models/block/rosalita_chest.json`, `models/item/rosalita_chest.json`
- `block/rosalita_trapped_chest.png`: `models/block/rosalita_trapped_chest.json`, `models/item/rosalita_trapped_chest.json`
- `block/rosalita_barrel.png`: `models/block/rosalita_barrel.json`, `models/item/rosalita_barrel.json`

## Blockstates Rosalita

- `blockstates/rosalita_andesite.json`
- `blockstates/rosalita_barrel.json`
- `blockstates/rosalita_button.json`
- `blockstates/rosalita_chest.json`
- `blockstates/rosalita_crafting_table.json`
- `blockstates/rosalita_diorite.json`
- `blockstates/rosalita_door.json`
- `blockstates/rosalita_fence.json`
- `blockstates/rosalita_fence_gate.json`
- `blockstates/rosalita_granite.json`
- `blockstates/rosalita_ladder.json`
- `blockstates/rosalita_leaves.json`
- `blockstates/rosalita_log.json`
- `blockstates/rosalita_planks.json`
- `blockstates/rosalita_pressure_plate.json`
- `blockstates/rosalita_sandstone.json`
- `blockstates/rosalita_slab.json`
- `blockstates/rosalita_stairs.json`
- `blockstates/rosalita_stone.json`
- `blockstates/rosalita_trapdoor.json`
- `blockstates/rosalita_trapped_chest.json`
- `blockstates/rosalita_wood.json`
- `blockstates/rosalitabloco.json`

## Cópias encontradas

- `rosalita_log.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_log.png` — `78c80a0d12765559c2dc77f9abf27451417355b5179127118743acc3c89e8664`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_log.png` — `78c80a0d12765559c2dc77f9abf27451417355b5179127118743acc3c89e8664`
- `rosalita_log_top.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_log_top.png` — `f61da7e7fb02d7c4f98c459705eb2fb3ab2668a81caeb1405d2aa4cfbee987c0`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_log_top.png` — `f61da7e7fb02d7c4f98c459705eb2fb3ab2668a81caeb1405d2aa4cfbee987c0`
- `stripped_rosalita_log.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/stripped_rosalita_log.png` — `ba4949b406a492ce87e0ccf3de3d8f0a1b9f2105af9fb779139e6015bf4e6fe2`
  - build: `build/resources/main/assets/chaoticd/textures/block/stripped_rosalita_log.png` — `ba4949b406a492ce87e0ccf3de3d8f0a1b9f2105af9fb779139e6015bf4e6fe2`
- `stripped_rosalita_log_top.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/stripped_rosalita_log_top.png` — `f61da7e7fb02d7c4f98c459705eb2fb3ab2668a81caeb1405d2aa4cfbee987c0`
  - build: `build/resources/main/assets/chaoticd/textures/block/stripped_rosalita_log_top.png` — `f61da7e7fb02d7c4f98c459705eb2fb3ab2668a81caeb1405d2aa4cfbee987c0`
- `rosalita_wood.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_wood.png` — `78c80a0d12765559c2dc77f9abf27451417355b5179127118743acc3c89e8664`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_wood.png` — `78c80a0d12765559c2dc77f9abf27451417355b5179127118743acc3c89e8664`
- `stripped_rosalita_wood.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/stripped_rosalita_wood.png` — `ba4949b406a492ce87e0ccf3de3d8f0a1b9f2105af9fb779139e6015bf4e6fe2`
  - build: `build/resources/main/assets/chaoticd/textures/block/stripped_rosalita_wood.png` — `ba4949b406a492ce87e0ccf3de3d8f0a1b9f2105af9fb779139e6015bf4e6fe2`
- `rosalita_planks.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_planks.png` — `185241aa8f4b71e875e0eaf85d05ed9e53197bc56e02795debd8433bd833c9aa`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_planks.png` — `185241aa8f4b71e875e0eaf85d05ed9e53197bc56e02795debd8433bd833c9aa`
- `rosalita_door.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_door.png` — `d9ca3e35dd79622ddf7aed0f9ad8263ec4575341f0cc9ef94921093b658806d5`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_door.png` — `d9ca3e35dd79622ddf7aed0f9ad8263ec4575341f0cc9ef94921093b658806d5`
- `rosalita_door_cima.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_door_cima.png` — `8ae41622a1b87b76b957cd200e7b1a5f4fcdf6ebe061691cf1546cc45a0d78da`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_door_cima.png` — `8ae41622a1b87b76b957cd200e7b1a5f4fcdf6ebe061691cf1546cc45a0d78da`
- `rosalita_trapdoor.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_trapdoor.png` — `14c0a24b0948facffd80a02db1fa4242c2aad755bf5ae9a11baf7c4db18ec7f6`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_trapdoor.png` — `14c0a24b0948facffd80a02db1fa4242c2aad755bf5ae9a11baf7c4db18ec7f6`
- `rosalita_crafting_table.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_crafting_table.png` — `27315cfb5e8bdb6d72922c4357f182dfbd6363d1f3a980fb1e2a9605ee2bc27c`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_crafting_table.png` — `27315cfb5e8bdb6d72922c4357f182dfbd6363d1f3a980fb1e2a9605ee2bc27c`
- `rosalita_chest.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_chest.png` — `3d26950689e546193cbefea9cd5c6d30d8d41d23d63d1a865f16ac818ef6486a`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_chest.png` — `3d26950689e546193cbefea9cd5c6d30d8d41d23d63d1a865f16ac818ef6486a`
- `rosalita_trapped_chest.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_trapped_chest.png` — `442a88f1da111ef451e8c65b73ac1667e1e765def02a1390f658b47081f0c8d1`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_trapped_chest.png` — `442a88f1da111ef451e8c65b73ac1667e1e765def02a1390f658b47081f0c8d1`
- `rosalita_barrel.png`: idêntica à fonte
  - source: `src/main/resources/assets/chaoticd/textures/block/rosalita_barrel.png` — `b1b8c919bf2752ce963346ef58448e864af04d080b77bf270849da4ac82d6dd3`
  - build: `build/resources/main/assets/chaoticd/textures/block/rosalita_barrel.png` — `b1b8c919bf2752ce963346ef58448e864af04d080b77bf270849da4ac82d6dd3`
