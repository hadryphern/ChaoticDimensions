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


if __name__ == "__main__":
    write_blocks()
    write_tags()
