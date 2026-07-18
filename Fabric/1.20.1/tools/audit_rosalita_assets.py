"""Audit the real Rosalita PNG resources and render a truthful contact sheet.

This tool never creates textures.  It reads the committed files used by the
Fabric resource pipeline and reports their dimensions, SHA-256 digests, JSON
references, and any copies found in Gradle/run output directories.
"""

from __future__ import annotations

import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
MOD_ID = "chaoticd"
SOURCE_ASSETS = ROOT / "src/main/resources/assets" / MOD_ID
BLOCK_TEXTURES = SOURCE_ASSETS / "textures/block"
BUILD_ASSETS = ROOT / "build/resources/main/assets" / MOD_ID
RUN_RESOURCEPACKS = ROOT / "run/resourcepacks"
REPORT = ROOT / "docs/rosalita-asset-audit.md"
CONTACT_SHEET = ROOT / "docs/previews/rosalita-final-assets.png"

ASSETS = (
    "rosalita_log.png",
    "rosalita_log_top.png",
    "stripped_rosalita_log.png",
    "stripped_rosalita_log_top.png",
    "rosalita_wood.png",
    "stripped_rosalita_wood.png",
    "rosalita_planks.png",
    "rosalita_door.png",
    "rosalita_door_cima.png",
    "rosalita_trapdoor.png",
    "rosalita_crafting_table.png",
    "rosalita_chest.png",
    "rosalita_trapped_chest.png",
    "rosalita_barrel.png",
)


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def json_strings(value: object) -> set[str]:
    if isinstance(value, str):
        return {value}
    if isinstance(value, list):
        return set().union(*(json_strings(item) for item in value))
    if isinstance(value, dict):
        return set().union(*(json_strings(item) for item in value.values()))
    return set()


def model_references() -> dict[str, list[str]]:
    references = {asset.removesuffix(".png"): [] for asset in ASSETS}
    for model in (SOURCE_ASSETS / "models").rglob("*.json"):
        values = json_strings(json.loads(model.read_text(encoding="utf-8")))
        for texture in references:
            if f"{MOD_ID}:block/{texture}" in values:
                references[texture].append(str(model.relative_to(SOURCE_ASSETS)))
    return references


def blockstate_references() -> list[str]:
    result: list[str] = []
    for state in (SOURCE_ASSETS / "blockstates").glob("rosalita*.json"):
        values = json_strings(json.loads(state.read_text(encoding="utf-8")))
        if any(value.startswith(f"{MOD_ID}:block/rosalita") for value in values):
            result.append(str(state.relative_to(SOURCE_ASSETS)))
    return sorted(result)


def candidate_copies(asset: str) -> list[tuple[str, Path, str]]:
    candidates = [("source", BLOCK_TEXTURES / asset)]
    build = BUILD_ASSETS / "textures/block" / asset
    if build.exists():
        candidates.append(("build", build))
    if RUN_RESOURCEPACKS.exists():
        for path in RUN_RESOURCEPACKS.rglob(asset):
            candidates.append(("run-resourcepack", path))
    return [(kind, path, digest(path)) for kind, path in candidates if path.exists()]


def make_contact_sheet(records: list[tuple[str, Path, str, tuple[int, int]]]) -> None:
    scale = 16
    columns = 2
    cell_width, cell_height = 560, 620
    rows = (len(records) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * cell_width, rows * cell_height), (24, 18, 27, 255))
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.load_default()
    for index, (name, path, sha, size) in enumerate(records):
        col, row = index % columns, index // columns
        x0, y0 = col * cell_width, row * cell_height
        image = Image.open(path).convert("RGBA")
        enlarged = image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST)
        sheet.alpha_composite(enlarged, (x0 + 16, y0 + 18))
        label_y = y0 + max(18 + enlarged.height + 12, 535)
        draw.text((x0 + 16, label_y), f"textures/block/{name}", fill=(255, 220, 238, 255), font=font)
        draw.text((x0 + 16, label_y + 18), f"{size[0]}x{size[1]}  SHA-256 {sha}", fill=(231, 182, 207, 255), font=font)
    CONTACT_SHEET.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(CONTACT_SHEET)


def main() -> None:
    records: list[tuple[str, Path, str, tuple[int, int]]] = []
    for asset in ASSETS:
        path = BLOCK_TEXTURES / asset
        with Image.open(path) as image:
            size = image.size
        records.append((asset, path, digest(path), size))

    make_contact_sheet(records)
    models = model_references()
    lines = [
        "# Auditoria de assets Rosalita",
        "",
        "Esta prévia foi composta diretamente dos PNGs-fonte abaixo, com ampliação nearest-neighbor; ela não usa uma imagem conceitual externa.",
        "",
        f"- Contact sheet: `{CONTACT_SHEET.relative_to(ROOT)}`",
        f"- Fonte carregada pelo Gradle: `src/main/resources/assets/{MOD_ID}/textures/block/`",
        "- `build/resources/main` é apenas a cópia de processamento de recursos do Gradle.",
        "- Somente `rosalita_leaves` possui provider de cor; madeira e derivados não recebem tint biome.",
        "",
        "## PNGs-fonte e hashes",
        "",
        "| Arquivo | Dimensão | SHA-256 |",
        "| --- | --- | --- |",
    ]
    for name, _, sha, size in records:
        lines.append(f"| `textures/block/{name}` | {size[0]}×{size[1]} | `{sha}` |")

    lines.extend(["", "## Referências de modelos", ""])
    for texture, paths in models.items():
        lines.append(f"- `block/{texture}.png`: " + (", ".join(f"`{path}`" for path in paths) if paths else "sem referência de modelo"))

    lines.extend(["", "## Blockstates Rosalita", ""])
    lines.extend(f"- `{path}`" for path in blockstate_references())

    lines.extend(["", "## Cópias encontradas", ""])
    for asset, _, source_sha, _ in records:
        copies = candidate_copies(asset)
        comparison = "idêntica à fonte" if all(sha == source_sha for _, _, sha in copies) else "CONFLITO de hash"
        lines.append(f"- `{asset}`: {comparison}")
        for kind, path, sha in copies:
            lines.append(f"  - {kind}: `{path.relative_to(ROOT)}` — `{sha}`")

    REPORT.parent.mkdir(parents=True, exist_ok=True)
    REPORT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Contact sheet: {CONTACT_SHEET}")
    print(f"Report: {REPORT}")


if __name__ == "__main__":
    main()
