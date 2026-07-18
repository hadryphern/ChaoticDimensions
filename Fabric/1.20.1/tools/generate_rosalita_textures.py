"""Generate deterministic 16x16 Rosalita block textures.

This script is a development tool only. The generated PNGs are committed in
src/main/resources and are never required when the mod runs.
"""

from pathlib import Path
from random import Random

from PIL import Image


OUTPUT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/chaoticd/textures/block"
PALETTE = {
    "deep": (0x46, 0x00, 0x1E),
    "shadow": (0x77, 0x00, 0x32),
    "stone": (0xA8, 0x00, 0x46),
    "ruby": (0xD1, 0x00, 0x58),
    "highlight": (0xFF, 0x00, 0x6B),
    "soft": (0xFC, 0x6F, 0xAA),
    "light": (0xFF, 0xA3, 0xCA),
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


if __name__ == "__main__":
    main()
