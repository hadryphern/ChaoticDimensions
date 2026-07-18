"""Fast local integrity checks for the Light/Shadow rework assets."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/chaoticd/textures"
errors = []
for family, high in (("light", True), ("shadow", False)):
    textures = [path for path in (ROOT / "block").glob(f"{family}_*.png")]
    if not textures:
        errors.append(f"{family}: no block textures")
    for path in textures:
        with Image.open(path) as image:
            if image.width not in (16, 64) or image.height not in (16, 32):
                errors.append(f"{path.name}: unexpected size {image.size}")
            pixels = [pixel[:3] for pixel in list(image.convert("RGBA").getdata()) if pixel[3] > 0]
            if not pixels or len(set(pixels)) < 2:
                errors.append(f"{path.name}: insufficient pixel variation")
            mean = sum(sum(pixel) / 3 for pixel in pixels) / len(pixels)
            if high and mean < 180:
                errors.append(f"{path.name}: Light texture too dark ({mean:.1f})")
            if not high and mean > 45:
                errors.append(f"{path.name}: Shadow texture too bright ({mean:.1f})")
if errors:
    print("ASSET VALIDATION FAILED")
    print("\n".join(errors))
    raise SystemExit(1)
print("ASSET VALIDATION PASSED: Light textures are bright and Shadow textures remain near-black with detail.")
