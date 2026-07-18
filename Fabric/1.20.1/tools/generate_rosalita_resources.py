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


def write(relative: str, value: object) -> None:
    destination = ROOT / relative
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
                "Name": "minecraft:oak_log", "Properties": {"axis": "y"},
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
                {"chance": 0.35, "feature": {"feature": f"{MOD_ID}:rosalita_oak_tree", "placement": []}},
                {"chance": 0.25, "feature": {"feature": f"{MOD_ID}:rosalita_birch_tree", "placement": []}},
                {"chance": 0.20, "feature": {"feature": f"{MOD_ID}:rosalita_pine_tree", "placement": []}},
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
            "foliage_color": 0xFF006B,
            "grass_color": 0xD10058,
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
    write_tags()
    write_trees()
    write_biome_worldgen()
