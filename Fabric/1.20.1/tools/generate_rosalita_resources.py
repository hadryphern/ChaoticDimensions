"""Write deterministic JSON resources for the Rosalita natural block family."""

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
MOD_ID = "chaoticd"
BLOCKS = (
    "rosalita_stone",
    "deep_rosalita_stone",
    "rosaline_stone",
    "rosalita_granite",
    "rosalita_diorite",
    "rosalita_andesite",
    "rosalita_sandstone",
)
LEAVES = "rosalita_leaves"
WOOD = (
    "rosalita_log", "stripped_rosalita_log", "rosalita_wood", "stripped_rosalita_wood",
    "rosalita_planks", "rosalita_stairs", "rosalita_slab", "rosalita_fence", "rosalita_fence_gate",
    "rosalita_door", "rosalita_trapdoor", "rosalita_pressure_plate", "rosalita_button",
    "rosalita_crafting_table", "rosalita_chest", "rosalita_trapped_chest", "rosalita_barrel", "rosalita_ladder",
)
SIGN_ITEMS = ("rosalita_sign", "rosalita_hanging_sign")
TOOLS = ("rosalita_wooden_sword", "rosalita_wooden_pickaxe", "rosalita_wooden_axe", "rosalita_wooden_shovel", "rosalita_wooden_hoe")


def write(relative: str, value: object) -> None:
    destination = ROOT / relative
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def merge_tag(relative: str, values: list[str]) -> None:
    """Add Rosalita entries without erasing other generated wood families."""
    destination = ROOT / relative
    existing: list[str] = []
    replace = False
    if destination.exists():
        current = json.loads(destination.read_text(encoding="utf-8"))
        existing = current.get("values", [])
        replace = current.get("replace", False)
    write(relative, {"replace": replace, "values": list(dict.fromkeys([*existing, *values]))})


def normal_loot(block: str) -> dict:
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"{MOD_ID}:{block}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    }


def write_blocks() -> None:
    for block in BLOCKS:
        write(f"assets/{MOD_ID}/blockstates/{block}.json", {"variants": {"": {"model": f"{MOD_ID}:block/{block}"}}})
        write(f"assets/{MOD_ID}/models/block/{block}.json", {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": f"{MOD_ID}:block/{block}"},
        })
        write(f"assets/{MOD_ID}/models/item/{block}.json", {"parent": f"{MOD_ID}:block/{block}"})
        write(f"data/{MOD_ID}/loot_tables/blocks/{block}.json", normal_loot(block))

    write(f"assets/{MOD_ID}/blockstates/{LEAVES}.json", {"variants": {"": {"model": f"{MOD_ID}:block/{LEAVES}"}}})
    write(f"assets/{MOD_ID}/models/block/{LEAVES}.json", {
        "parent": "minecraft:block/leaves",
        "textures": {"all": f"{MOD_ID}:block/{LEAVES}"},
    })
    write(f"assets/{MOD_ID}/models/item/{LEAVES}.json", {"parent": f"{MOD_ID}:block/{LEAVES}"})
    write(f"data/{MOD_ID}/loot_tables/blocks/{LEAVES}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{
                "type": "minecraft:alternatives",
                "children": [
                    {"type": "minecraft:item", "name": f"{MOD_ID}:{LEAVES}", "conditions": [{
                        "condition": "minecraft:match_tool",
                        "predicate": {"items": ["minecraft:shears"]},
                    }]},
                    {"type": "minecraft:item", "name": f"{MOD_ID}:{LEAVES}", "conditions": [{
                        "condition": "minecraft:match_tool",
                        "predicate": {"enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]},
                    }]},
                    {"type": "minecraft:empty"},
                ],
            }],
        }],
    })


def copy_legacy_asset(kind: str, source: str, destination: str) -> None:
    """Reuse Minecraft's already-correct state layouts from the recovered legacy set."""
    origin = ROOT / f"assets/{MOD_ID}/{kind}/{source}.json"
    text = origin.read_text(encoding="utf-8")
    text = text.replace(source, destination).replace("tabua_sombra", "rosalita_planks")
    text = text.replace("porta_madeira_sombra_cima", "rosalita_door_cima")
    write(f"assets/{MOD_ID}/{kind}/{destination}.json", json.loads(text))


def cube(block: str, texture: str) -> None:
    write(f"assets/{MOD_ID}/blockstates/{block}.json", {"variants": {"": {"model": f"{MOD_ID}:block/{block}"}}})
    write(f"assets/{MOD_ID}/models/block/{block}.json", {
        "parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD_ID}:block/{texture}"},
    })
    write(f"assets/{MOD_ID}/models/item/{block}.json", {"parent": f"{MOD_ID}:block/{block}"})


def write_wood_resources() -> None:
    for block, side, end in (
        ("rosalita_log", "rosalita_log", "rosalita_log_top"),
        ("stripped_rosalita_log", "stripped_rosalita_log", "stripped_rosalita_log_top"),
        ("rosalita_wood", "rosalita_wood", "rosalita_wood"),
        ("stripped_rosalita_wood", "stripped_rosalita_wood", "stripped_rosalita_wood"),
    ):
        write(f"assets/{MOD_ID}/blockstates/{block}.json", {"variants": {
            "axis=y": {"model": f"{MOD_ID}:block/{block}"},
            "axis=z": {"model": f"{MOD_ID}:block/{block}", "x": 90},
            "axis=x": {"model": f"{MOD_ID}:block/{block}", "x": 90, "y": 90},
        }})
        write(f"assets/{MOD_ID}/models/block/{block}.json", {
            "parent": "minecraft:block/cube_column", "textures": {"side": f"{MOD_ID}:block/{side}", "end": f"{MOD_ID}:block/{end}"},
        })
        write(f"assets/{MOD_ID}/models/item/{block}.json", {"parent": f"{MOD_ID}:block/{block}"})

    cube("rosalita_planks", "rosalita_planks")
    for source, destination in (
        ("escada_madeira_sombra", "rosalita_stairs"), ("slab_madeira_sombra", "rosalita_slab"),
        ("cerca_madeira_sombra", "rosalita_fence"), ("portao_madeira_sombra", "rosalita_fence_gate"),
        ("porta_madeira_sombra", "rosalita_door"), ("trap_door_madeira_sombra", "rosalita_trapdoor"),
        ("placa_pressao_madeira_sombra", "rosalita_pressure_plate"), ("botao_madeira_sombra", "rosalita_button"),
    ):
        copy_legacy_asset("blockstates", source, destination)
        for model in (ROOT / f"assets/{MOD_ID}/models/block").glob(f"{source}*.json"):
            copy_legacy_asset("models/block", model.stem, model.stem.replace(source, destination))
        write(f"assets/{MOD_ID}/models/item/{destination}.json", {
            "parent": f"{MOD_ID}:block/{destination}" if destination not in {"rosalita_fence", "rosalita_fence_gate", "rosalita_door", "rosalita_trapdoor", "rosalita_button"}
            else "minecraft:item/generated",
            **({"textures": {"layer0": f"{MOD_ID}:block/{'rosalita_door' if destination == 'rosalita_door' else 'rosalita_trapdoor' if destination == 'rosalita_trapdoor' else 'rosalita_planks'}"}}
               if destination in {"rosalita_fence", "rosalita_fence_gate", "rosalita_door", "rosalita_trapdoor", "rosalita_button"} else {}),
        })

    # The recovered Shadow templates were useful for state layouts, but their
    # lower door panel and trapdoor still referenced the Shadow plank/door
    # aliases.  Point every Rosalita model at the concrete Rosalita source PNG
    # that it is meant to render.
    models = ROOT / f"assets/{MOD_ID}/models/block"
    for model in models.glob("rosalita_door*.json"):
        data = json.loads(model.read_text(encoding="utf-8"))
        textures = data.get("textures", {})
        if textures.get("bottom") == f"{MOD_ID}:block/rosalita_planks":
            textures["bottom"] = f"{MOD_ID}:block/rosalita_door"
        write(str(model.relative_to(ROOT)), data)
    for model in models.glob("rosalita_trapdoor*.json"):
        data = json.loads(model.read_text(encoding="utf-8"))
        textures = data.get("textures", {})
        if textures.get("texture") == f"{MOD_ID}:block/rosalita_door_cima":
            textures["texture"] = f"{MOD_ID}:block/rosalita_trapdoor"
        if textures.get("particle") == f"{MOD_ID}:block/rosalita_door_cima":
            textures["particle"] = f"{MOD_ID}:block/rosalita_trapdoor"
        write(str(model.relative_to(ROOT)), data)

    cube("rosalita_crafting_table", "rosalita_crafting_table")
    for block in ("rosalita_chest", "rosalita_trapped_chest", "rosalita_barrel"):
        cube(block, block)
    write(f"assets/{MOD_ID}/blockstates/rosalita_ladder.json", {"variants": {
        "facing=north": {"model": f"{MOD_ID}:block/rosalita_ladder"},
        "facing=south": {"model": f"{MOD_ID}:block/rosalita_ladder", "y": 180},
        "facing=east": {"model": f"{MOD_ID}:block/rosalita_ladder", "y": 90},
        "facing=west": {"model": f"{MOD_ID}:block/rosalita_ladder", "y": 270},
    }})
    write(f"assets/{MOD_ID}/models/block/rosalita_ladder.json", {
        "parent": "minecraft:block/ladder", "textures": {"texture": f"{MOD_ID}:block/rosalita_ladder"},
    })
    write(f"assets/{MOD_ID}/models/item/rosalita_ladder.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD_ID}:block/rosalita_ladder"}})

    for item in (*SIGN_ITEMS, "rosalita_stick", *TOOLS):
        parent = "minecraft:item/handheld" if item in TOOLS else "minecraft:item/generated"
        write(f"assets/{MOD_ID}/models/item/{item}.json", {"parent": parent, "textures": {"layer0": f"{MOD_ID}:item/{item}"}})
    # Signs are rendered by their block-entity renderer, but Minecraft still requires
    # a fallback model for every state while it builds the block-model atlas.
    for block in ("rosalita_sign", "rosalita_wall_sign", "rosalita_hanging_sign", "rosalita_wall_hanging_sign"):
        write(f"assets/{MOD_ID}/blockstates/{block}.json", {
            "variants": {"": {"model": "minecraft:block/oak_planks"}},
        })
    sign_drops = {
        "rosalita_wall_sign": "rosalita_sign",
        "rosalita_wall_hanging_sign": "rosalita_hanging_sign",
    }
    for block in (*WOOD, "rosalita_sign", "rosalita_wall_sign", "rosalita_hanging_sign", "rosalita_wall_hanging_sign"):
        write(f"data/{MOD_ID}/loot_tables/blocks/{block}.json", normal_loot(sign_drops.get(block, block)))


def shaped(pattern: list[str], key: dict[str, str], result: str, count: int = 1) -> dict:
    return {
        "type": "minecraft:crafting_shaped", "pattern": pattern,
        "key": {symbol: {"item": item} for symbol, item in key.items()},
        "result": {"item": f"{MOD_ID}:{result}", "count": count},
    }


def write_recipes() -> None:
    recipes = {
        "rosalita_planks_from_log": {
            "type": "minecraft:crafting_shapeless",
            "ingredients": [{"item": f"{MOD_ID}:rosalita_log"}],
            "result": {"item": f"{MOD_ID}:rosalita_planks", "count": 4},
        },
        "rosalita_wood": shaped(["LL", "LL"], {"L": f"{MOD_ID}:rosalita_log"}, "rosalita_wood", 3),
        "rosalita_stick": shaped(["P", "P"], {"P": f"{MOD_ID}:rosalita_planks"}, "rosalita_stick", 4),
        "rosalita_stairs": shaped(["P  ", "PP ", "PPP"], {"P": f"{MOD_ID}:rosalita_planks"}, "rosalita_stairs", 4),
        "rosalita_slab": shaped(["PPP"], {"P": f"{MOD_ID}:rosalita_planks"}, "rosalita_slab", 6),
        "rosalita_fence": shaped(["PSP", "PSP"], {"P": f"{MOD_ID}:rosalita_planks", "S": f"{MOD_ID}:rosalita_stick"}, "rosalita_fence", 3),
        "rosalita_fence_gate": shaped(["SPS", "SPS"], {"S": f"{MOD_ID}:rosalita_stick", "P": f"{MOD_ID}:rosalita_planks"}, "rosalita_fence_gate"),
        "rosalita_door": shaped(["PP", "PP", "PP"], {"P": f"{MOD_ID}:rosalita_planks"}, "rosalita_door", 3),
        "rosalita_trapdoor": shaped(["PPP", "PPP"], {"P": f"{MOD_ID}:rosalita_planks"}, "rosalita_trapdoor", 2),
        "rosalita_pressure_plate": shaped(["PP"], {"P": f"{MOD_ID}:rosalita_planks"}, "rosalita_pressure_plate"),
        "rosalita_button": shaped(["P"], {"P": f"{MOD_ID}:rosalita_planks"}, "rosalita_button"),
        "rosalita_crafting_table": shaped(["PP", "PP"], {"P": f"{MOD_ID}:rosalita_planks"}, "rosalita_crafting_table"),
        "rosalita_chest": shaped(["PPP", "P P", "PPP"], {"P": f"{MOD_ID}:rosalita_planks"}, "rosalita_chest"),
        "rosalita_trapped_chest": shaped(["T", "C"], {"T": "minecraft:tripwire_hook", "C": f"{MOD_ID}:rosalita_chest"}, "rosalita_trapped_chest"),
        "rosalita_barrel": shaped(["PSP", "P P", "PSP"], {"P": f"{MOD_ID}:rosalita_planks", "S": f"{MOD_ID}:rosalita_slab"}, "rosalita_barrel"),
        "rosalita_ladder": shaped(["S S", "SSS", "S S"], {"S": f"{MOD_ID}:rosalita_stick"}, "rosalita_ladder", 3),
        "rosalita_sign": shaped(["PPP", "PPP", " S "], {"P": f"{MOD_ID}:rosalita_planks", "S": f"{MOD_ID}:rosalita_stick"}, "rosalita_sign", 3),
        "rosalita_hanging_sign": shaped(["C C", "PPP", "PPP"], {"C": "minecraft:chain", "P": f"{MOD_ID}:stripped_rosalita_log"}, "rosalita_hanging_sign", 6),
        "rosalita_wooden_sword": shaped(["P", "P", "S"], {"P": f"{MOD_ID}:rosalita_planks", "S": f"{MOD_ID}:rosalita_stick"}, "rosalita_wooden_sword"),
        "rosalita_wooden_pickaxe": shaped(["PPP", " S ", " S "], {"P": f"{MOD_ID}:rosalita_planks", "S": f"{MOD_ID}:rosalita_stick"}, "rosalita_wooden_pickaxe"),
        "rosalita_wooden_axe": shaped(["PP", "PS", " S"], {"P": f"{MOD_ID}:rosalita_planks", "S": f"{MOD_ID}:rosalita_stick"}, "rosalita_wooden_axe"),
        "rosalita_wooden_shovel": shaped(["P", "S", "S"], {"P": f"{MOD_ID}:rosalita_planks", "S": f"{MOD_ID}:rosalita_stick"}, "rosalita_wooden_shovel"),
        "rosalita_wooden_hoe": shaped(["PP", " S", " S"], {"P": f"{MOD_ID}:rosalita_planks", "S": f"{MOD_ID}:rosalita_stick"}, "rosalita_wooden_hoe"),
    }
    for name, recipe in recipes.items():
        write(f"data/{MOD_ID}/recipes/{name}.json", recipe)


def write_tags() -> None:
    write(f"data/{MOD_ID}/tags/blocks/forbidden_in_rosalita_underground.json", {
        "replace": False,
        "values": [
            "minecraft:coal_ore", "minecraft:deepslate_coal_ore", "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
            "minecraft:copper_ore", "minecraft:deepslate_copper_ore", "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
            "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore", "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
            "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore", "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
        ],
    })
    write(f"data/{MOD_ID}/tags/blocks/allowed_rosalita_ores.json", {"replace": False, "values": []})
    write(f"data/{MOD_ID}/tags/items/rosalita_natural_blocks.json", {
        "replace": False,
        "values": [f"{MOD_ID}:{LEAVES}", *[f"{MOD_ID}:{block}" for block in BLOCKS]],
    })
    merge_tag("data/minecraft/tags/blocks/mineable/axe.json", [f"{MOD_ID}:{block}" for block in WOOD])
    merge_tag("data/minecraft/tags/blocks/logs.json", [f"{MOD_ID}:rosalita_log", f"{MOD_ID}:stripped_rosalita_log", f"{MOD_ID}:rosalita_wood", f"{MOD_ID}:stripped_rosalita_wood"])
    write("data/minecraft/tags/items/leaves.json", {"replace": False, "values": [f"{MOD_ID}:{LEAVES}"]})
    write(f"data/{MOD_ID}/tags/entity_types/allowed_in_rosalita_biome.json", {"replace": False, "values": []})
    write(f"data/{MOD_ID}/tags/worldgen/biome/is_rosalita.json", {"replace": False, "values": [f"{MOD_ID}:rosalita_biome"]})


def tree_config(trunk: dict, foliage: dict, size: dict) -> dict:
    return {
        "type": "minecraft:tree",
        "config": {
            "decorators": [],
            "dirt_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:dirt"}},
            "foliage_placer": foliage,
            "foliage_provider": {"type": "minecraft:simple_state_provider", "state": {
                "Name": f"{MOD_ID}:rosalita_leaves",
                "Properties": {"distance": "7", "persistent": "false", "waterlogged": "false"},
            }},
            "force_dirt": False,
            "ignore_vines": True,
            "minimum_size": size,
            "trunk_placer": trunk,
            "trunk_provider": {"type": "minecraft:simple_state_provider", "state": {
                "Name": f"{MOD_ID}:rosalita_log", "Properties": {"axis": "y"},
            }},
        },
    }


def write_trees() -> None:
    two_layers = lambda limit, upper: {"type": "minecraft:two_layers_feature_size", "limit": limit, "lower_size": 0, "upper_size": upper}
    straight = lambda base, a, b: {"type": "minecraft:straight_trunk_placer", "base_height": base, "height_rand_a": a, "height_rand_b": b}
    blob = lambda radius, height: {"type": "minecraft:blob_foliage_placer", "height": height, "offset": 0, "radius": radius}
    configurations = {
        "rosalita_oak_tree": tree_config(straight(4, 2, 0), blob(2, 3), two_layers(1, 1)),
        "rosalita_birch_tree": tree_config(straight(6, 2, 1), blob(1, 4), two_layers(1, 1)),
        "rosalita_pine_tree": tree_config(straight(6, 4, 0), {
            "type": "minecraft:pine_foliage_placer",
            "height": {"type": "minecraft:uniform", "value": {"min_inclusive": 3, "max_inclusive": 4}},
            "offset": 1,
            "radius": 1,
        }, two_layers(2, 2)),
        "rosalita_acacia_tree": tree_config({
            "type": "minecraft:forking_trunk_placer", "base_height": 5, "height_rand_a": 2, "height_rand_b": 2,
        }, {"type": "minecraft:acacia_foliage_placer", "offset": 0, "radius": 2}, two_layers(1, 2)),
    }
    for name, config in configurations.items():
        write(f"data/{MOD_ID}/worldgen/configured_feature/{name}.json", config)
        write(f"data/{MOD_ID}/worldgen/placed_feature/{name}.json", {"feature": f"{MOD_ID}:{name}", "placement": []})

    selector = {
        "type": "minecraft:random_selector",
        "config": {
            "default": {"feature": f"{MOD_ID}:rosalita_acacia_tree", "placement": []},
            "features": [
                # random_selector evaluates in order. These conditional chances
                # result in 35% oak, 25% birch, 20% pine and 20% acacia overall.
                {"chance": 0.35, "feature": {"feature": f"{MOD_ID}:rosalita_oak_tree", "placement": []}},
                {"chance": 0.3846154, "feature": {"feature": f"{MOD_ID}:rosalita_birch_tree", "placement": []}},
                {"chance": 0.5, "feature": {"feature": f"{MOD_ID}:rosalita_pine_tree", "placement": []}},
            ],
        },
    }
    write(f"data/{MOD_ID}/worldgen/configured_feature/rosalita_trees.json", selector)
    write(f"data/{MOD_ID}/worldgen/placed_feature/rosalita_trees.json", {
        "feature": f"{MOD_ID}:rosalita_trees",
        "placement": [
            {"type": "minecraft:count", "count": 5},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
            {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
            {"type": "minecraft:biome"},
        ],
    })


def write_biome_worldgen() -> None:
    write(f"data/{MOD_ID}/worldgen/configured_feature/rosalita_underground.json", {
        "type": f"{MOD_ID}:rosalita_underground",
        "config": {},
    })
    write(f"data/{MOD_ID}/worldgen/placed_feature/rosalita_underground.json", {
        "feature": f"{MOD_ID}:rosalita_underground",
        "placement": [{"type": "minecraft:biome"}],
    })
    write(f"data/{MOD_ID}/worldgen/biome/rosalita_biome.json", {
        "has_precipitation": True,
        "temperature": 0.75,
        "downfall": 0.8,
        "effects": {
            "foliage_color": 0xF25592,
            "grass_color": 0xE84F8A,
            "sky_color": 0xFFC4DD,
            "fog_color": 0xF2A8C7,
            "water_color": 0xC85B89,
            "water_fog_color": 0x6D163B,
        },
        "spawners": {
            "ambient": [], "axolotls": [], "creature": [], "misc": [], "monster": [],
            "underground_water_creature": [], "water_ambient": [], "water_creature": [],
        },
        "spawn_costs": {},
        "carvers": {"air": ["minecraft:cave", "minecraft:cave_extra_underground", "minecraft:canyon"]},
        "features": [
            [],
            ["minecraft:lake_lava_underground"],
            [], [], [], [], [], [], [],
            [f"{MOD_ID}:rosalita_trees", "minecraft:patch_grass_forest", "minecraft:patch_tall_grass"],
            [f"{MOD_ID}:rosalita_underground"],
        ],
    })


if __name__ == "__main__":
    write_blocks()
    write_wood_resources()
    write_recipes()
    write_tags()
    write_trees()
    write_biome_worldgen()
