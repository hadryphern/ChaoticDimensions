"""Synchronize author-provided Rosalita assets without changing any pixels.

This is deliberately not an image generator.  It validates and copies files
from the private `.assets/New Assets` library into normal Fabric resource paths.
"""

from __future__ import annotations

import hashlib
from pathlib import Path
from shutil import copyfile

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / ".assets/New Assets"
BLOCK_OUTPUT = ROOT / "src/main/resources/assets/chaoticd/textures/block"
ENTITY_OUTPUT = ROOT / "src/main/resources/assets/chaoticd/textures/entity/chest"

# `Rosalita_Door_Buttom` is the visually upper half despite its typo: its
# brighter header aligns over the darker framed lower panel in Door_Bottom.
ASSETS = {
    "Blocks/Rosalita_Door_Bottom.png": (BLOCK_OUTPUT / "rosalita_door_bottom.png", (16, 16)),
    "Blocks/Rosalita_Door_Buttom.png": (BLOCK_OUTPUT / "rosalita_door_top.png", (16, 16)),
    "Blocks/Rosalita_Leaves.png": (BLOCK_OUTPUT / "rosalita_leaves.png", (16, 16)),
    "Blocks/Rosalita_Oak.png": (BLOCK_OUTPUT / "rosalita_log.png", (16, 16)),
    "Blocks/Rosalita_Oak_Top.png": (BLOCK_OUTPUT / "rosalita_log_top.png", (16, 16)),
    "Blocks/Rosalita_Planks.png": (BLOCK_OUTPUT / "rosalita_planks.png", (16, 16)),
    "Blocks/Rosalita_Sapling.png": (BLOCK_OUTPUT / "rosalita_sapling.png", (16, 16)),
    "Blocks/Rosalita_Trapdoor.png": (BLOCK_OUTPUT / "rosalita_trapdoor.png", (16, 16)),
    "entity/chest/Rosalita_Normal.png": (ENTITY_OUTPUT / "rosalita_normal.png", (64, 64)),
    "entity/chest/Rosalita_Normal_Left.png": (ENTITY_OUTPUT / "rosalita_normal_left.png", (64, 64)),
    "entity/chest/Rosalita_Normal_Right.png": (ENTITY_OUTPUT / "rosalita_normal_right.png", (64, 64)),
}


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def validate(source: Path, expected_size: tuple[int, int]) -> None:
    with Image.open(source) as image:
        if image.size != expected_size:
            raise ValueError(f"{source}: expected {expected_size}, got {image.size}")


def main() -> None:
    for relative, (destination, expected_size) in ASSETS.items():
        source = SOURCE / relative
        if not source.is_file():
            raise FileNotFoundError(source)
        validate(source, expected_size)
        destination.parent.mkdir(parents=True, exist_ok=True)
        copyfile(source, destination)
        if sha256(source) != sha256(destination):
            raise RuntimeError(f"Hash mismatch after copy: {source} -> {destination}")
        print(f"copied {relative} -> {destination.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
