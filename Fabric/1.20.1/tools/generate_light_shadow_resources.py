"""Generate models, loot, recipes and tags for the Light and Shadow biome rework."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
MOD = "chaoticd"
FAMILIES = ("light", "shadow")

def write(path, value):
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")

def merge_tag(path, values):
    target = ROOT / path
    existing, replace = [], False
    if target.exists():
        current = json.loads(target.read_text(encoding="utf-8"))
        existing, replace = current.get("values", []), current.get("replace", False)
    write(path, {"replace": replace, "values": list(dict.fromkeys([*existing, *values]))})

def loot(name, drop=None):
    return {"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"{MOD}:{drop or name}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]}

def model(name, parent, textures):
    write(f"assets/{MOD}/models/block/{name}.json", {"parent":parent,"textures":textures})
    write(f"assets/{MOD}/models/item/{name}.json", {"parent":f"{MOD}:block/{name}"})

def cube(name, texture):
    write(f"assets/{MOD}/blockstates/{name}.json", {"variants":{"":{"model":f"{MOD}:block/{name}"}}})
    model(name, "minecraft:block/cube_all", {"all":f"{MOD}:block/{texture}"})

def log(name, side, end):
    write(f"assets/{MOD}/blockstates/{name}.json", {"variants":{
        "axis=y":{"model":f"{MOD}:block/{name}"}, "axis=z":{"model":f"{MOD}:block/{name}","x":90}, "axis=x":{"model":f"{MOD}:block/{name}","x":90,"y":90}}})
    model(name, "minecraft:block/cube_column", {"side":f"{MOD}:block/{side}","end":f"{MOD}:block/{end}"})

def legacy_copy(source, target, texture):
    base = ROOT / f"assets/{MOD}"
    state = json.loads((base / "blockstates" / f"{source}.json").read_text())
    text = json.dumps(state).replace(source, target).replace("tabua_sombra", texture).replace("porta_madeira_sombra_cima", f"{target}_detail")
    write(f"assets/{MOD}/blockstates/{target}.json", json.loads(text))
    for path in (base / "models/block").glob(f"{source}*.json"):
        data = path.read_text().replace(source, target).replace("tabua_sombra", texture).replace("porta_madeira_sombra_cima", f"{target}_detail")
        write(f"assets/{MOD}/models/block/{path.stem.replace(source,target)}.json", json.loads(data))
    write(f"assets/{MOD}/models/item/{target}.json", {"parent":"minecraft:item/generated","textures":{"layer0":f"{MOD}:block/{texture}"}})

def recipe(name, pattern, key, count=1):
    write(f"data/{MOD}/recipes/{name}.json", {"type":"minecraft:crafting_shaped","pattern":pattern,"key":{k:{"item":v} for k,v in key.items()},"result":{"item":f"{MOD}:{name}","count":count}})

def family(wood):
    planks = "bloco_madeira_branco" if wood == "light" else "tabua_sombra"
    plank_texture = "biomabranco_tabuamadeira" if wood == "light" else "tabua_sombra"
    log_id = "madeira_bruta_branca" if wood == "light" else "madeira_sombra"
    leaves = "bloco_folha_branca" if wood == "light" else "folha_sombra"
    log(f"{wood}_log", f"{wood}_log", f"{wood}_log_top")
    log(f"stripped_{wood}_log", f"stripped_{wood}_log", f"stripped_{wood}_log_top")
    log(f"{wood}_wood", f"{wood}_wood", f"{wood}_wood")
    log(f"stripped_{wood}_wood", f"stripped_{wood}_wood", f"stripped_{wood}_wood")
    log(f"{wood}_pillar", f"{wood}_pillar", f"{wood}_pillar_top")
    for name, texture in ((f"{wood}_carved_planks",f"{wood}_carved_planks"),(f"{wood}_panel",f"{wood}_panel"),(f"{wood}_mosaic",f"{wood}_mosaic"),(f"{wood}_crafting_table",f"{wood}_crafting_table"),(f"{wood}_chest",f"{wood}_chest"),(f"{wood}_trapped_chest",f"{wood}_trapped_chest"),(f"{wood}_barrel",f"{wood}_barrel")):
        cube(name, texture)
    write(f"assets/{MOD}/blockstates/{wood}_leaves.json", {"variants":{"":{"model":f"{MOD}:block/{wood}_leaves"}}})
    model(f"{wood}_leaves", "minecraft:block/leaves", {"all":f"{MOD}:block/{wood}_leaves"})
    write(f"assets/{MOD}/models/item/{wood}_leaves.json", {"parent":f"{MOD}:block/{wood}_leaves"})
    cube(f"{wood}_sapling", f"{wood}_sapling")
    write(f"assets/{MOD}/models/block/{wood}_sapling.json", {"parent":"minecraft:block/cross","textures":{"cross":f"{MOD}:block/{wood}_sapling"}})
    for source,target in (("escada_madeira_sombra",f"{wood}_stairs"),("slab_madeira_sombra",f"{wood}_slab"),("cerca_madeira_sombra",f"{wood}_fence"),("portao_madeira_sombra",f"{wood}_fence_gate"),("porta_madeira_sombra",f"{wood}_door"),("trap_door_madeira_sombra",f"{wood}_trapdoor"),("placa_pressao_madeira_sombra",f"{wood}_pressure_plate"),("botao_madeira_sombra",f"{wood}_button")):
        legacy_copy(source,target,plank_texture)
    legacy_copy("escada_madeira_sombra",f"{wood}_mosaic_stairs",f"{wood}_mosaic")
    legacy_copy("slab_madeira_sombra",f"{wood}_mosaic_slab",f"{wood}_mosaic")
    for name in (f"{wood}_lattice",f"{wood}_ladder"):
        cube(name,name)
    for sign in (f"{wood}_sign",f"{wood}_wall_sign",f"{wood}_hanging_sign",f"{wood}_wall_hanging_sign"):
        write(f"assets/{MOD}/blockstates/{sign}.json", {"variants":{"":{"model":"minecraft:block/oak_planks"}}})
        write(f"data/{MOD}/loot_tables/blocks/{sign}.json", loot(sign, f"{wood}_hanging_sign" if "hanging" in sign else f"{wood}_sign"))
    for item in (f"{wood}_sign",f"{wood}_hanging_sign",f"{wood}_stick",*[f"{wood}_wooden_{x}" for x in ("sword","pickaxe","axe","shovel","hoe")]):
        write(f"assets/{MOD}/models/item/{item}.json", {"parent":"minecraft:item/handheld" if "wooden" in item else "minecraft:item/generated","textures":{"layer0":f"{MOD}:item/{item}"}})
    blocks = (f"{wood}_log",f"stripped_{wood}_log",f"{wood}_wood",f"stripped_{wood}_wood",f"{wood}_leaves",f"{wood}_sapling",f"{wood}_stairs",f"{wood}_slab",f"{wood}_fence",f"{wood}_fence_gate",f"{wood}_door",f"{wood}_trapdoor",f"{wood}_pressure_plate",f"{wood}_button",f"{wood}_crafting_table",f"{wood}_chest",f"{wood}_trapped_chest",f"{wood}_barrel",f"{wood}_ladder",f"{wood}_mosaic",f"{wood}_mosaic_stairs",f"{wood}_mosaic_slab",f"{wood}_carved_planks",f"{wood}_pillar",f"{wood}_panel",f"{wood}_lattice")
    for b in blocks: write(f"data/{MOD}/loot_tables/blocks/{b}.json", loot(b))
    p=f"{MOD}:{planks}"; s=f"{MOD}:{wood}_stick"
    write(f"data/{MOD}/recipes/{wood}_planks_from_log.json", {"type":"minecraft:crafting_shapeless","ingredients":[{"item":f"{MOD}:{log_id}"}],"result":{"item":p,"count":4}})
    recipe(f"{wood}_stick",["P","P"],{"P":p},4); recipe(f"{wood}_stairs",["P  ","PP ","PPP"],{"P":p},4); recipe(f"{wood}_slab",["PPP"],{"P":p},6)
    recipe(f"{wood}_fence",["PSP","PSP"],{"P":p,"S":s},3); recipe(f"{wood}_fence_gate",["SPS","SPS"],{"P":p,"S":s})
    recipe(f"{wood}_door",["PP","PP","PP"],{"P":p},3); recipe(f"{wood}_trapdoor",["PPP","PPP"],{"P":p},2); recipe(f"{wood}_pressure_plate",["PP"],{"P":p}); recipe(f"{wood}_button",["P"],{"P":p})
    recipe(f"{wood}_crafting_table",["PP","PP"],{"P":p}); recipe(f"{wood}_chest",["PPP","P P","PPP"],{"P":p}); recipe(f"{wood}_barrel",["PSP","P P","PSP"],{"P":p,"S":f"{MOD}:{wood}_slab"}); recipe(f"{wood}_ladder",["S S","SSS","S S"],{"S":s},3)
    recipe(f"{wood}_wooden_sword",["P","P","S"],{"P":p,"S":s}); recipe(f"{wood}_wooden_pickaxe",["PPP"," S "," S "],{"P":p,"S":s}); recipe(f"{wood}_wooden_axe",["PP","PS"," S"],{"P":p,"S":s}); recipe(f"{wood}_wooden_shovel",["P","S","S"],{"P":p,"S":s}); recipe(f"{wood}_wooden_hoe",["PP"," S"," S"],{"P":p,"S":s})

def main():
    for wood in FAMILIES: family(wood)
    merge_tag("data/minecraft/tags/blocks/leaves.json", ["chaoticd:bloco_folha_branca","chaoticd:folha_sombra","chaoticd:light_leaves","chaoticd:shadow_leaves"])
    merge_tag("data/minecraft/tags/blocks/logs.json", ["chaoticd:madeira_bruta_branca","chaoticd:madeira_sombra","chaoticd:light_log","chaoticd:stripped_light_log","chaoticd:light_wood","chaoticd:stripped_light_wood","chaoticd:shadow_log","chaoticd:stripped_shadow_log","chaoticd:shadow_wood","chaoticd:stripped_shadow_wood"])
    merge_tag("data/minecraft/tags/blocks/mineable/axe.json", ["#minecraft:logs","chaoticd:bloco_madeira_branco","chaoticd:tabua_sombra"])

if __name__ == "__main__": main()
