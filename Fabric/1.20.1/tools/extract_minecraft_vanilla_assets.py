"""Extract local Minecraft 1.20.1 client assets for private mod-reference use.

The output preserves Mojang's resource-pack paths exactly, e.g.
``assets/minecraft/textures/block/oak_planks.png``.  It is intentionally
ignored by Git: these are copyrighted game resources and are not mod assets.
"""

from __future__ import annotations

import csv
import hashlib
import json
import zipfile
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
CLIENT_JAR = Path.home() / ".gradle/caches/fabric-loom/1.20.1/minecraft-client.jar"
ASSET_INDEX = Path.home() / ".gradle/caches/fabric-loom/assets/indexes/1.20.1-5.json"
ASSET_OBJECTS = Path.home() / ".gradle/caches/fabric-loom/assets/objects"
OUTPUT = ROOT / "minecraft_vanilla_1.20.1"
RESOURCE_PREFIX = "assets/minecraft/"


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> None:
    if not CLIENT_JAR.is_file() or not ASSET_INDEX.is_file():
        raise SystemExit(f"Minecraft 1.20.1 client jar not found: {CLIENT_JAR}")
    OUTPUT.mkdir(exist_ok=True)
    extracted: list[Path] = []
    with zipfile.ZipFile(CLIENT_JAR) as archive:
        for info in archive.infolist():
            if info.is_dir() or not info.filename.startswith(RESOURCE_PREFIX):
                continue
            destination = OUTPUT / info.filename
            destination.parent.mkdir(parents=True, exist_ok=True)
            if destination.exists() and destination.read_bytes() != archive.read(info):
                raise SystemExit(f"Refusing to overwrite a different local file: {destination}")
            if not destination.exists():
                destination.write_bytes(archive.read(info))
            extracted.append(destination)

    external_assets = json.loads(ASSET_INDEX.read_text(encoding="utf-8"))["objects"]
    external_count = 0
    retained_jar_paths: list[str] = []
    for relative, metadata in external_assets.items():
        if not relative.startswith("minecraft/"):
            continue
        source = ASSET_OBJECTS / metadata["hash"][:2] / metadata["hash"]
        if not source.is_file():
            raise SystemExit(f"Indexed Minecraft asset not found locally: {source}")
        destination = OUTPUT / "assets" / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        source_bytes = source.read_bytes()
        if destination.exists() and destination.read_bytes() != source_bytes:
            # The client JAR is the canonical source for models/textures.  A
            # few index entries intentionally share a path (currently font
            # data); keep the JAR copy instead of mutating it in place.
            retained_jar_paths.append(relative)
            continue
        if not destination.exists():
            destination.write_bytes(source_bytes)
        external_count += 1

    texture_rows: list[dict[str, str]] = []
    for texture in sorted((OUTPUT / RESOURCE_PREFIX / "textures").rglob("*.png")):
        with Image.open(texture) as image:
            width, height = image.size
        texture_rows.append({
            "path": str(texture.relative_to(OUTPUT)),
            "width": str(width),
            "height": str(height),
            "sha256": sha256(texture),
        })

    with (OUTPUT / "texture-catalog.csv").open("w", newline="", encoding="utf-8") as catalog:
        writer = csv.DictWriter(catalog, fieldnames=("path", "width", "height", "sha256"))
        writer.writeheader()
        writer.writerows(texture_rows)

    summary = {
        "minecraft_version": "1.20.1",
        "source": str(CLIENT_JAR),
        "jar_resource_files": len(extracted),
        "indexed_resource_files": external_count,
        "jar_paths_retained_when_indexed_path_differs": retained_jar_paths,
        "textures_png": len(texture_rows),
        "resource_root": "assets/minecraft",
        "git_ignored": True,
    }
    (OUTPUT / "asset-summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    (OUTPUT / "README.md").write_text(
        "# Minecraft Vanilla 1.20.1 — referência local\n\n"
        "Extraído do `minecraft-client.jar` local usado pelo ambiente Fabric. "
        "Os caminhos, nomes, dimensões e pixels são os originais do Minecraft 1.20.1.\n\n"
        "- Blocos: `assets/minecraft/textures/block/`\n"
        "- Itens: `assets/minecraft/textures/item/`\n"
        "- Modelos: `assets/minecraft/models/`\n"
        "- Blockstates: `assets/minecraft/blockstates/`\n"
        "- Textos: `assets/minecraft/lang/en_us.json`\n"
        "- Sons: `assets/minecraft/sounds/`\n"
        "- Catálogo de texturas, dimensões e SHA-256: `texture-catalog.csv`\n\n"
        "Uso: referência local para desenvolvimento. Esta pasta é ignorada pelo Git e não deve ser redistribuída com o mod.\n",
        encoding="utf-8",
    )
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
