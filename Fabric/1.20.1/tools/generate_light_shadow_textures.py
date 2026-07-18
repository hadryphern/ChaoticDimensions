"""Deterministic original 16px pixel-art textures for Light and Shadow biome assets."""
from pathlib import Path
from PIL import Image

OUT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/chaoticd/textures"
PALETTES = {
 "light": [(255,255,255),(250,250,248),(245,245,242),(238,238,234),(230,232,234),(218,221,223),(247,243,255),(255,248,238)],
 "shadow": [(0,0,0),(3,3,3),(6,6,7),(10,10,12),(13,14,19),(17,17,22),(23,23,29)],
}
def image(path,w=16,h=16,alpha=False):
 p=OUT/path; p.parent.mkdir(parents=True,exist_ok=True); return p,Image.new("RGBA",(w,h),(0,0,0,0) if alpha else (*PALETTES['light'][0],255))
def save(name,im):
 p=OUT/name; p.parent.mkdir(parents=True,exist_ok=True); im.save(p)
def texture_set(wood):
 c=PALETTES[wood]
 def put(name, fn, w=16,h=16,alpha=False):
  _,im=image(f"block/{name}.png",w,h,alpha); px=im.load(); fn(px,w,h); save(f"block/{name}.png",im)
 def grain(px,w,h):
  for y in range(h):
   for x in range(w):
    v=(x*5+y*3+(x*y)%7)%len(c); px[x,y]=(*c[v],255)
    if (x+y*2)%9==0: px[x,y]=(*c[-1],255)
 def bark(px,w,h):
  for y in range(h):
   for x in range(w):
    v=(x*3+y+(x//4)*2)%len(c); px[x,y]=(*c[v],255)
    if wood=='light' and (x in (3,12) or (x+y)%19==0): px[x,y]=(*c[0],255)
    if wood=='shadow' and x in (2,7,13): px[x,y]=(*c[0],255)
 def top(px,w,h):
  for y in range(h):
   for x in range(w):
    d=max(abs(x-7),abs(y-7)); px[x,y]=(*c[min(len(c)-1,d//2)],255)
    if wood=='light' and (x==7 or y==7): px[x,y]=(*c[0],255)
    if wood=='shadow' and (x-y)%8==0: px[x,y]=(*c[-1],255)
 def leaves(px,w,h):
  for y in range(h):
   for x in range(w):
    if (x*5+y*7+x*y)%13==0: continue
    px[x,y]=(*c[(x+y*2)%min(len(c),5)],255)
 def door(px,w,h):
  for y in range(h):
   for x in range(w):
    frame=x in (0,1,14,15) or y%16 in (0,1,14,15); motif=(abs(x-7)+abs((y%16)-7) in (2,5) if wood=='light' else abs(x-7)==abs((y%16)-7))
    px[x,y]=(*(c[-2] if frame else c[1 if wood=='light' else 3] if motif else c[2]),255)
 def lattice(px,w,h):
  for y in range(h):
   for x in range(w):
    if x in (1,2,13,14) or y in (2,3,12,13) or ((x-y)%6==0 and 3<x<12 and 3<y<12): px[x,y]=(*c[-2 if (x+y)%3 else -1],255)
 def tool(px,w,h,kind):
  for y in range(h):
   for x in range(w):
    head= (3<=x<=11 and 1<=y<=5) if kind!='sword' else (6<=x<=9 and 1<=y<=10)
    handle= x in (7,8) and y>=5
    if head or handle: px[x,y]=(*c[-1 if head else 3],255)
 for n in (f"{wood}_log",f"stripped_{wood}_log",f"{wood}_wood",f"stripped_{wood}_wood",f"{wood}_pillar"): put(n,bark)
 for n in (f"{wood}_log_top",f"stripped_{wood}_log_top",f"{wood}_pillar_top"): put(n,top)
 for n in (f"{wood}_carved_planks",f"{wood}_panel",f"{wood}_mosaic",f"{wood}_crafting_table",f"{wood}_chest",f"{wood}_trapped_chest",f"{wood}_barrel"): put(n,grain)
 put(f"{wood}_leaves",leaves,alpha=True); put(f"{wood}_sapling",leaves,alpha=True); put(f"{wood}_lattice",lattice,alpha=True); put(f"{wood}_ladder",lattice,alpha=True)
 put(f"{wood}_door",door,16,32); put(f"{wood}_door_detail",door,16,32); put(f"{wood}_door_cima",door,16,32); put(f"{wood}_trapdoor",door); put(f"{wood}_trapdoor_detail",door)
 # Existing legacy visuals reworked without changing their registry/model IDs.
 if wood=='light':
  for n in ("biomabranco_folha","biomabranco_grama","biomabranco_gramabaixo","biomabranco_gramacima","biomabranco_madeirabruta","biomabranco_madeirabruta_cima","biomabranco_tabuamadeira"): put(n, leaves if 'folha' in n else (top if 'cima' in n else bark if 'madeirabruta' in n else grain))
 else:
  for n in ("folha_da_arvore_sombra","madeira_de_sombra","madeira_da_sombra_bootom","tabua_sombra","porta_madeira_sombra_cima","biomasombra_terra","bloco_de_sombra","cobblestone_sombra","shadow_ore"):
   put(n, leaves if 'folha' in n else (top if 'bootom' in n else door if 'porta' in n else bark if 'madeira' in n else grain),16,32 if 'porta' in n else 16, 'folha' in n)
 for kind in ("sword","pickaxe","axe","shovel","hoe"):
  _,im=image(f"item/{wood}_wooden_{kind}.png",alpha=True); tool(im.load(),16,16,kind); save(f"item/{wood}_wooden_{kind}.png",im)
 for n in (f"{wood}_stick",f"{wood}_sign",f"{wood}_hanging_sign"):
  _,im=image(f"item/{n}.png",alpha=True); tool(im.load(),16,16,"sword"); save(f"item/{n}.png",im)
 for path in (f"entity/signs/{wood}.png",f"entity/signs/hanging/{wood}.png"):
  _,im=image(path,64,32); px=im.load()
  for y in range(32):
   for x in range(64): px[x,y]=(*c[-2 if x in (0,1,62,63) or y in (0,1,30,31) else 2],255)
  save(path,im)
def main():
 for wood in PALETTES: texture_set(wood)
if __name__=='__main__': main()
