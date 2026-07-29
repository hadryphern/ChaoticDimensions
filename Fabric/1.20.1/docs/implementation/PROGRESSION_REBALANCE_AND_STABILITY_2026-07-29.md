# Rebalanceamento de progressão e estabilidade — 2026-07-29

## Regra vigente de progressão

`ProgressionMaterial` é a fonte única de atributos das ferramentas e
armaduras pós-Netherite. Cada passo de material é **exatamente duas vezes** o
anterior, sem compressão, fórmulas logarítmicas ou saltos escondidos.

| Material | Multiplicador em relação à Netherite |
| --- | ---: |
| Netherite | 1× |
| Emerald | 2× |
| Ruby | 4× |
| Jaxy | 8× |
| Chlorophyte | 16× |
| Titanium | 32× |
| Vylam | 64× |
| Hero | 128× |
| Rosalita | 256× |
| Sapphire | 512× |
| Shadow | 1.024× |
| Vortex | 2.048× |

A regra vale para durabilidade, velocidade de mineração, dano-base,
encantabilidade, durabilidade de armadura, pontos de armadura, toughness e a
proteção de conjunto completo. A resistência a knockback também dobra até o
limite nativo de 100%; não existe valor válido acima desse limite no Minecraft.

As ferramentas registradas recebem os modificadores diretamente dessa fonte;
as classes antigas de `Tier` e `ArmorMaterial` são somente fachadas que também
delegam para ela. Assim, não há um atributo diferente escondido em uma espada,
picareta ou peça de armadura do mesmo material. Os efeitos e encantamentos
especiais já pedidos para Safira continuam separados dos atributos-base do
material.

Os níveis de mineração permanecem ativos para controlar quais blocos cada
picareta pode quebrar, mas a descrição `Nível X` foi removida dos tooltips.

## Inventário criativo

`Chaotic Dimensions` usa a ordem estável:

1. blocos;
2. espadas, picaretas, machados, pás e enxadas, cada tipo em sequência;
3. armaduras agrupadas por peça (todos os capacetes, depois peitorais etc.);
4. minérios, lingotes, gemas e almas;
5. itens úteis, encantamentos, poções e ovos;
6. comida.

`Chaotic Test` é a aba separada para conteúdo legado ou ainda sem fonte,
textura final, atributo final ou progressão survival concluída. Conteúdo pronto
não deve ser adicionado nela.

## Remoção de raridades

O sistema visual de raridades foi removido: não há mais classe de raridade,
nome RGB, glint forçado ou linha de tooltip `Common`, `Rare`, `Legendary` e
similares. Os encantamentos próprios usam `Rarity.COMMON` apenas porque é um
parâmetro técnico obrigatório da API 1.20.1; ele não cria texto, cor ou
classificação visual. Filtros `rarity_filter` de worldgen não foram alterados,
pois controlam frequência de geração de minérios e não têm relação com a UI de
raridade.

## Estabilidade e desempenho

Foram removidas duas causas confirmadas de crash:

- classes auxiliares de stack foram movidas para fora do pacote `mixin`,
  evitando `IllegalClassLoadError` durante serialização de NBT/inventário;
- o tooltip deixou de gerar uma classe sintética de `switch` por enum dentro de
  um mixin, eliminando o `NoClassDefFoundError` ao abrir inventários/tooltips.

Também foram extraídas as classes auxiliares de pacotes de rede e de inventário
para fora de mixins, impedindo novas classes sintéticas internas no JAR.

As rotinas com maior custo agora têm cache ou intervalo:

- chegada segura Aurora/Shadow é reutilizada e tentativas de resgate do void
  recebem cooldown;
- busca do Dream Fluid ocorre em intervalos, com alcance menor;
- o golpe de Safira não executa duas buscas de área para o mesmo efeito;
- mobs sob Sapphiric recalculam rota com menor frequência;
- raycast de alcance longo é reutilizado no mesmo tick de cliente;
- Crystaline See recebe cooldown para buscas de estruturas;
- Sir. Orens não força chunks da casa em verificações recorrentes.

O sistema de pilhas até 999 continua sendo uma alteração ampla de rede, NBT e
inventário. Antes de um lançamento, testar manualmente save/reload, morte,
baús, hopper, fornalha e pilhas acima de 127 em singleplayer e multiplayer.

## Validação

As tarefas abaixo devem passar antes de publicar uma mudança de progressão ou
worldgen:

```bash
./gradlew clean check remapJar remapSourcesJar --no-daemon --console=plain
./gradlew validateProgression --no-daemon --console=plain
./gradlew validateAuroraWorldgen --no-daemon --console=plain
```
