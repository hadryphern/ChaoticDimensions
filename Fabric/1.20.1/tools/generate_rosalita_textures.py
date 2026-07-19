"""Generate deterministic 16x16 Rosalita block textures.

This script is a development tool only. The generated PNGs are committed in
src/main/resources and are never required when the mod runs.
"""

from pathlib import Path
from random import Random
from shutil import copyfile

from PIL import Image


OUTPUT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/chaoticd/textures/block"
USER_ASSET_ROOT = Path(__file__).resolve().parents[1] / ".assets/New Assets"
PALETTE = {
    "deep": (0xB9, 0x3B, 0x6C),
    "shadow": (0xC9, 0x47, 0x7B),
    "stone": (0xD9, 0x5B, 0x90),
    "ruby": (0xE8, 0x74, 0xA5),
    "highlight": (0xF2, 0x55, 0x92),
    "soft": (0xEE, 0x8D, 0xB8),
    "light": (0xF3, 0xA3, 0xC7),
    "pale": (0xFF, 0xBF, 0xD8),
}


def write_stone(name: str, seed: int, colors: list[tuple[int, int, int]], accent_period: int) -> None:
    random = Random(seed)
    image = Image.new("RGBA", (16, 16))
    pixels = image.load()
    for y in range(16):
        for x in range(16):
            wave = (x * 11 + y * 17 + (x * y) * 3 + seed) % 23
            index = 0 if wave < 12 else 1 if wave < 19 else 2
            color = colors[index]
            if (x * 7 + y * 13 + seed) % accent_period == 0:
                color = colors[min(3, len(colors) - 1)]
            if random.randrange(19) == 0:
                color = colors[min(2, len(colors) - 1)]
            pixels[x, y] = (*color, 255)
    image.save(OUTPUT / f"{name}.png")


def write_sandstone() -> None:
    image = Image.new("RGBA", (16, 16))
    pixels = image.load()
    bands = [PALETTE["soft"], PALETTE["ruby"], PALETTE["stone"], PALETTE["soft"]]
    for y in range(16):
        band = bands[(y // 4) % len(bands)]
        for x in range(16):
            shade = ((x * 3 + y * 5) % 7 == 0)
            pixels[x, y] = (*(PALETTE["light"] if shade else band), 255)
    image.save(OUTPUT / "rosalita_sandstone.png")


def write_leaves() -> None:
    random = Random(0xC0FFEE)
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    pixels = image.load()
    colors = [PALETTE["stone"], PALETTE["ruby"], PALETTE["highlight"], PALETTE["soft"]]
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            hole = (x * 5 + y * 7 + x * y) % 13 == 0 or random.randrange(23) == 0
            if hole and (edge or (x + y) % 3 != 0):
                continue
            color = colors[(x * 3 + y * 5 + random.randrange(3)) % len(colors)]
            pixels[x, y] = (*color, 255)
    image.save(OUTPUT / "rosalita_leaves.png")


def write_wood(name: str, seed: int, stripped: bool = False) -> None:
    """Write bark with deliberate vertical fibres, never procedural speckle.

    The first version mixed x and y in the colour calculation, producing the
    diagonal/noisy panels that were visible in-game.  Every motif below is a
    vertical stroke or a short, horizontal knot; this keeps the bark readable
    at Minecraft's actual 16x16 resolution.
    """
    image = Image.new("RGBA", (16, 16))
    pixels = image.load()
    columns = (
        ("deep", "shadow", "stone", "stone", "ruby", "soft", "ruby", "stone",
         "shadow", "deep", "shadow", "stone", "ruby", "soft", "stone", "shadow")
        if not stripped else
        ("shadow", "stone", "stone", "ruby", "ruby", "soft", "soft", "ruby",
         "stone", "stone", "ruby", "soft", "soft", "ruby", "stone", "shadow")
    )
    for y in range(16):
        for x in range(16):
            color = PALETTE[columns[x]]
            # Bark grooves are continuous vertical bands.  The small changes
            # create natural irregularity without becoming diagonal noise.
            if not stripped and x in (0, 5, 9, 15):
                color = PALETTE["deep"]
            if stripped and x in (0, 15):
                color = PALETTE["shadow"]
            if x in (3, 12) and 3 <= y <= 12:
                color = PALETTE["light"] if stripped else PALETTE["ruby"]
            if x in (6, 7) and y in (5, 6, 11):
                color = PALETTE["pale"] if stripped else PALETTE["soft"]
            pixels[x, y] = (*color, 255)
    image.save(OUTPUT / f"{name}.png")


def write_log_top(name: str, seed: int) -> None:
    """Write readable rounded growth rings for the log end."""
    image = Image.new("RGBA", (16, 16), (*PALETTE["soft"], 255))
    pixels = image.load()
    rings = [PALETTE["pale"], PALETTE["light"], PALETTE["ruby"], PALETTE["stone"], PALETTE["shadow"]]
    for y in range(16):
        for x in range(16):
            # An oval distance gives rounded rings rather than square frames.
            dx = (x - 7.5) / 1.03
            dy = (y - 7.5) / 0.92
            distance = (dx * dx + dy * dy) ** 0.5
            ring = min(4, int(distance / 1.65))
            color = rings[ring]
            # A stable, compact heartwood centre; no random pixels.
            if 6 <= x <= 9 and 6 <= y <= 9:
                color = PALETTE["ruby"] if (x + y) % 2 else PALETTE["pale"]
            pixels[x, y] = (*color, 255)
    image.save(OUTPUT / f"{name}.png")


def write_planks() -> None:
    image = Image.new("RGBA", (16, 16))
    pixels = image.load()
    for y in range(16):
        plank = y // 4
        for x in range(16):
            if y % 4 == 0:
                color = PALETTE["deep"]
            else:
                base = ("soft", "ruby", "light", "ruby")[plank]
                color = PALETTE[base]
                # Horizontal grain is intentionally aligned with the board.
                if y % 4 == 2:
                    color = PALETTE["pale"] if plank % 2 == 0 else PALETTE["soft"]
                if y % 4 == 3 and x in range(3, 8):
                    color = PALETTE["stone"]
                if y % 4 == 1 and x in range(10, 14):
                    color = PALETTE["light"]
            pixels[x, y] = (*color, 255)
    image.save(OUTPUT / "rosalita_planks.png")


def write_door_and_trapdoor() -> None:
    """Create plank-based door halves and a matching framed trapdoor."""
    def door_half(top: bool) -> Image.Image:
        image = Image.new("RGBA", (16, 32), (*PALETTE["ruby"], 255))
        pixels = image.load()
        for y in range(32):
            for x in range(16):
                local_y = y % 16
                frame = x in (0, 1, 14, 15) or local_y in (0, 1, 14, 15)
                rail = local_y in (5, 10)
                panel = 3 <= x <= 12 and 3 <= local_y <= 12
                if frame:
                    color = PALETTE["deep"]
                elif rail:
                    color = PALETTE["shadow"]
                elif panel:
                    color = PALETTE["soft"] if top else PALETTE["ruby"]
                    if local_y in (4, 8, 12):
                        color = PALETTE["pale"] if top else PALETTE["light"]
                else:
                    color = PALETTE["stone"]
                if top and x == 11 and local_y == 11:
                    color = PALETTE["pale"]
                pixels[x, y] = (*color, 255)
        return image

    door_half(False).save(OUTPUT / "rosalita_door.png")
    door_half(True).save(OUTPUT / "rosalita_door_cima.png")

    trapdoor = Image.new("RGBA", (16, 16), (*PALETTE["ruby"], 255))
    pixels = trapdoor.load()
    for y in range(16):
        for x in range(16):
            frame = x in (0, 1, 14, 15) or y in (0, 1, 14, 15)
            brace = x in (4, 5, 10, 11) or y in (6, 7, 10, 11)
            color = PALETTE["deep"] if frame else PALETTE["shadow"] if brace else PALETTE["soft"]
            if not frame and not brace and y in (3, 12):
                color = PALETTE["pale"]
            pixels[x, y] = (*color, 255)
    trapdoor.save(OUTPUT / "rosalita_trapdoor.png")


def write_utility_blocks() -> None:
    for name, base, trim in (
        ("rosalita_crafting_table", PALETTE["ruby"], PALETTE["deep"]),
        ("rosalita_chest", PALETTE["soft"], PALETTE["deep"]),
        ("rosalita_trapped_chest", PALETTE["ruby"], PALETTE["highlight"]),
        ("rosalita_barrel", PALETTE["stone"], PALETTE["deep"]),
    ):
        image = Image.new("RGBA", (16, 16), (*base, 255))
        pixels = image.load()
        for y in range(16):
            for x in range(16):
                if x in (0, 1, 14, 15) or y in (0, 1, 14, 15):
                    pixels[x, y] = (*trim, 255)
                elif y in (5, 10):
                    pixels[x, y] = (*PALETTE["pale"], 255)
        image.save(OUTPUT / f"{name}.png")

    ladder = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    pixels = ladder.load()
    for y in range(16):
        for x in range(16):
            if x in (2, 3, 12, 13) or y in (3, 4, 8, 9, 13, 14):
                pixels[x, y] = (*PALETTE["ruby" if (x + y) % 2 else "light"], 255)
    ladder.save(OUTPUT / "rosalita_ladder.png")


def write_item_icons() -> None:
    item_output = OUTPUT.parent / "item"
    item_output.mkdir(parents=True, exist_ok=True)
    def icon(name: str, pixels_on: set[tuple[int, int]], color: tuple[int, int, int]) -> None:
        image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        pixels = image.load()
        for x, y in pixels_on:
            if 0 <= x < 16 and 0 <= y < 16:
                pixels[x, y] = (*color, 255)
        image.save(item_output / f"{name}.png")

    stick = {(7, y) for y in range(3, 14)} | {(8, y) for y in range(5, 12)}
    icon("rosalita_stick", stick, PALETTE["ruby"])
    for tool, head in {
        "rosalita_wooden_sword": {(7, y) for y in range(1, 12)} | {(8, y) for y in range(2, 11)} | {(x, 11) for x in range(5, 11)},
        "rosalita_wooden_pickaxe": {(x, 2) for x in range(3, 13)} | {(7, y) for y in range(2, 14)} | {(8, y) for y in range(3, 13)},
        "rosalita_wooden_axe": {(x, y) for x in range(3, 10) for y in range(2, 7)} | {(7, y) for y in range(5, 14)} | {(8, y) for y in range(6, 13)},
        "rosalita_wooden_shovel": {(x, y) for x in range(5, 11) for y in range(1, 6)} | {(7, y) for y in range(5, 14)} | {(8, y) for y in range(6, 13)},
        "rosalita_wooden_hoe": {(x, y) for x in range(4, 10) for y in range(2, 5)} | {(7, y) for y in range(4, 14)} | {(8, y) for y in range(5, 13)},
    }.items():
        icon(tool, head, PALETTE["light"])
    for name, color in (("rosalita_sign", PALETTE["soft"]), ("rosalita_hanging_sign", PALETTE["ruby"])):
        icon(name, {(x, y) for x in range(2, 14) for y in range(3, 11)} | {(7, y) for y in range(11, 15)}, color)


def write_sign_textures() -> None:
    entity_output = OUTPUT.parent / "entity/signs"
    hanging_output = entity_output / "hanging"
    entity_output.mkdir(parents=True, exist_ok=True)
    hanging_output.mkdir(parents=True, exist_ok=True)
    for output, base in ((entity_output, PALETTE["soft"]), (hanging_output, PALETTE["ruby"])):
        image = Image.new("RGBA", (64, 32), (*base, 255))
        pixels = image.load()
        for y in range(32):
            for x in range(64):
                if x < 3 or x > 60 or y < 3 or y > 28 or (x * 3 + y * 5) % 19 == 0:
                    pixels[x, y] = (*PALETTE["deep"], 255)
        image.save(output / "rosalita.png")


def apply_manual_rosalita_sources() -> None:
    """Copy author-provided files literally after generated fallback textures.

    `.assets/New Assets` is a private source library.  Nothing here is edited;
    the selected files are only copied to the standard Fabric resource path.
    Missing files deliberately leave the deterministic fallback intact.
    """
    sources = {
        "Blocks/Rosalita_Leaves.png": ("rosalita_leaves.png",),
        "Blocks/Rosalita_Oak.png": ("rosalita_log.png", "rosalita_wood.png"),
        "Blocks/Rosalita_Oak_Top.png": ("rosalita_log_top.png",),
        "Blocks/Rosalita_Planks.png": ("rosalita_planks.png",),
        "Blocks/Rosalita_Trapdoor.png": ("rosalita_trapdoor.png",),
    }
    for relative_source, destinations in sources.items():
        source = USER_ASSET_ROOT / relative_source
        if not source.is_file():
            continue
        for destination in destinations:
            copyfile(source, OUTPUT / destination)


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    write_stone("rosalita_stone", 101, [PALETTE["shadow"], PALETTE["stone"], PALETTE["ruby"], PALETTE["highlight"]], 29)
    write_stone("deep_rosalita_stone", 211, [PALETTE["deep"], PALETTE["shadow"], PALETTE["stone"], PALETTE["ruby"]], 37)
    write_stone("rosaline_stone", 307, [PALETTE["ruby"], PALETTE["soft"], PALETTE["light"], PALETTE["stone"]], 31)
    write_stone("rosalita_granite", 401, [PALETTE["shadow"], PALETTE["stone"], PALETTE["ruby"], PALETTE["soft"]], 17)
    write_stone("rosalita_diorite", 503, [PALETTE["soft"], PALETTE["light"], PALETTE["ruby"], PALETTE["stone"]], 19)
    write_stone("rosalita_andesite", 601, [PALETTE["deep"], PALETTE["shadow"], PALETTE["stone"], PALETTE["soft"]], 41)
    write_sandstone()
    write_leaves()
    write_wood("rosalita_log", 811)
    write_log_top("rosalita_log_top", 823)
    write_wood("stripped_rosalita_log", 827, stripped=True)
    write_log_top("stripped_rosalita_log_top", 829)
    write_wood("rosalita_wood", 839)
    write_wood("stripped_rosalita_wood", 853, stripped=True)
    write_planks()
    write_door_and_trapdoor()
    write_utility_blocks()
    write_item_icons()
    write_sign_textures()
    apply_manual_rosalita_sources()


if __name__ == "__main__":
    main()
