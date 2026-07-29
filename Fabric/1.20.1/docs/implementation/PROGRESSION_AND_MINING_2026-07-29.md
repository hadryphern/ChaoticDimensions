# Progressão de ferramentas, armaduras e mineração — 2026-07-29

## Diagnóstico inicial

Os tiers pós-Netherite estavam distribuídos em várias classes (`EmeraldTier`,
`RubyTier`, `JaxyTier`, `TitaniumTier`, `RosalitaTier`, `SapphireTier` e
`FutureProgressionTier`) e os valores dos construtores das ferramentas estavam
repetidos em `ModItems`. Os números seguiam uma progressão antiga, que não
usava o multiplicador do material imediatamente anterior.

Os requisitos de mineração já usavam o caminho correto do Fabric: blocos com
`requiresCorrectToolForDrops()`, tag `minecraft:mineable/pickaxe` e as tags
dinâmicas `fabric:needs_tool_level_N`. Porém Titanium estava no nível 7,
Rosalita e Safira no 8, e não havia tags para os níveis 9–12. A picareta de
ouro vanilla é uma exceção da engine: seu tier interno é 0, apesar de a regra
do mod exigir nível 2.

Materiais/itens efetivamente registrados antes desta revisão: Emerald, Ruby,
Jaxy, Chlorophyte (apenas picareta), Titanium, Hero, Rosalita, Safira e Shadow
(espada e picareta). Vylam e Vortex possuem materiais/itens-base, mas ainda não
possuem ferramentas registradas. Armaduras completas existem somente para
Emerald, Ruby, Jaxy, Titanium e Rosalita.

## Implementação central

`ProgressionMaterial` é agora a fonte única da cadeia pós-Netherite. Ela
armazena predecessor, multiplicador direto, multiplicador acumulado, nível de
mineração, item de reparo, disponibilidade real de itens/armaduras e os valores
seguros para `Tier` e `ArmorMaterial`. As classes de tier antigas permanecem
somente como fachadas compatíveis e delegam para ela; nenhum ID salvo foi
trocado.

Multiplicadores acumulados oficiais (sempre em relação à Netherite):

```text
Netherite 1× → Emerald 2× → Ruby 6× → Jaxy 12× → Chlorophyte 60×
→ Titanium 600× → Vylam 3.000× → Hero 75.000× → Rosalita 5.625.000×
→ Sapphire 843.750.000× → Shadow 126.562.500.000×
→ Vortex 31.640.625.000.000×
```

Até Hero, durabilidade, velocidade e bônus de dano são literais. A partir de
Rosalita, as fórmulas usam compressão logarítmica monotônica ancorada em Hero,
pois os valores literais deixam de caber com segurança em `int` ou tornam a
mineração impraticável. A ordem de poder nunca é invertida.

Limites técnicos aplicados: 1.500.000.000 usos, 1.000.000 de velocidade de
mineração, 1.000.000 de bônus de dano, 100.000 de encantabilidade, 90.000.000
de multiplicador de durabilidade de armadura, 20 de toughness e 1,0 de
resistência a knockback. A velocidade de ataque não é multiplicada: cada tipo
de ferramenta preserva o valor de ataque já definido no item, evitando
cooldowns negativos.

## Ferramentas e tiers

| ID/material | Nível | Anterior | Multiplicador direto / acumulado | Usos aplicados | Velocidade de mineração | Bônus de dano do tier | Encantabilidade | Reparo | Estado |
| --- | ---: | --- | --- | ---: | ---: | ---: | ---: | --- | --- |
| `netherite` | 4 | — | 1× / 1× | 2.031 | 9 | 4 | 15 | Netherite Ingot | vanilla base |
| `emerald` | 5 | Netherite | 2× / 2× | 4.062 | 18 | 8 | 30 | Emerald Ingot | conjunto completo; Luck V e sistemas Emerald Luck |
| `ruby` | 6 | Emerald | 3× / 6× | 12.186 | 54 | 24 | 90 | Ruby | conjunto completo |
| `jaxy` | 7 | Ruby | 2× / 12× | 24.372 | 108 | 48 | 180 | Jaxy Gem | conjunto completo |
| `chlorophyte` | 8 | Jaxy | 5× / 60× | 121.860 | 540 | 240 | 900 | Chlorophyte Ingot | somente picareta registrada |
| `titanium` | 8 | Chlorophyte | 10× / 600× | 1.218.600 | 5.400 | 2.400 | 9.354 | Titanium Ingot | conjunto completo; efeito específico pendente |
| `vylam` | 8 | Titanium | 5× / 3.000× | 6.093.000 | 27.000 | 12.000 | 15.263 | Vylam Gem | sem ferramentas registradas |
| `hero` | 9 | Vylam | 25× / 75.000× | 152.325.000 | 675.000 | 300.000 | 27.082 | Hero Gem | ferramentas registradas; sem armadura |
| `rosalita` | 10 | Hero | 75× / 5.625.000× | 445.301.156 | 745.653 | 452.176 | 42.934 | Rosalita Gem | conjunto completo; valor comprimido |
| `sapphire` | 11 | Rosalita | 150× / 843.750.000× | 785.312.900 | 827.649 | 628.782 | 61.331 | Sapphire Gem | ferramentas; valor comprimido; espada preserva 589 de dano direto |
| `shadow` | 12 | Sapphire | 150× / 126.562.500.000× | 1.125.324.645 | 909.645 | 805.389 | 79.728 | Shadow Gem | espada e picareta; valor comprimido |
| `vortex` | 12 | Shadow | 250× / 31.640.625.000.000× | 1.500.000.000 | 1.000.000 | 1.000.000 | 100.000 | Vortex Gem | reservado, sem ferramentas registradas |

O campo “bônus de dano” é o valor do `Tier`; os construtores de espada,
picareta, machado, pá e enxada derivam seus modificadores deste valor de modo
centralizado. A Sapphire Sword continua sendo uma exceção intencional:
`SapphireSwordItem` calcula seu modificador para manter exatamente 589 de dano
direto, independentemente do limite de tier.

Cada picareta registrada informa discretamente o material e o nível de
mineração no tooltip. A chave agora aponta para o nome traduzido real do item,
em `pt_br`, `en_us`, `es_co` e `es_mx`.

## Armaduras existentes

Defesa nativa é mantida na forma Netherite (capacete 3, peitoral 8, calças 6,
botas 3), pois a fórmula vanilla perde eficiência acima de 20 pontos. A
progressão adicional é aplicada somente para conjunto completo, no servidor,
sem tocar dano de void/kill. O divisor é finito e ordenado.

| Material | Capacete / Peitoral / Calças / Botas (usos) | Toughness | Knockback | Encant. | Reparo | Divisor de conjunto |
| --- | --- | ---: | ---: | ---: | --- | ---: |
| Emerald | 814 / 1.184 / 1.110 / 962 | 6,00 | 0,20 | 30 | Emerald Ingot | 2 |
| Ruby | 2.442 / 3.552 / 3.330 / 2.886 | 10,75 | 0,36 | 90 | Ruby | 6 |
| Jaxy | 4.884 / 7.104 / 6.660 / 5.772 | 13,75 | 0,46 | 180 | Jaxy Gem | 12 |
| Titanium | 244.200 / 355.200 / 333.000 / 288.600 | 20,00 (limite) | 1,00 (limite) | 9.354 | Titanium Ingot | 600 |
| Rosalita | 239.108.892 / 347.794.752 / 326.057.580 / 282.583.236 | 20,00 (limite) | 1,00 (limite) | 42.934 | Rosalita Gem | 757 |

Não foram registradas armaduras fictícias para Chlorophyte, Vylam, Hero,
Sapphire, Shadow ou Vortex: faltam modelos/texturas/itens correspondentes.

## Mineração, drops e encantamentos

`MiningProgression` contém a matriz canônica de picareta e minério. A regra é
literalmente `nivelFerramenta >= nivelExigido`. Todas as variantes já existentes
continuam exclusivamente em `minecraft:mineable/pickaxe` e mantêm
`requiresCorrectToolForDrops()`. Portanto mão vazia, espada, machado, pá e
enxada não passam pelo gate nativo de drop.

| Bloco/família existente | Nível exigido | Tag responsável | Drop normal | Fortune | Silk Touch | XP |
| --- | ---: | --- | --- | --- | --- | --- |
| Emerald Ore, Deepslate Emerald Ore, Emerald Block (vanilla) | 4 | `fabric:needs_tool_level_4` | comportamento vanilla | sim | sim | vanilla |
| Ruby Ore, Deepslate/Nether/Aurora Ruby Ore, Ruby Block | 5 | `fabric:needs_tool_level_5` | Ruby | sim | sim | 3–7 / 4–8 Nether / 5–10 Aurora |
| Jax Ore, Deepslate/Nether/Aurora Jax Ore, Jaxy Block | 6 | `fabric:needs_tool_level_6` | Jaxy Gem | sim | sim | não (bloco simples) |
| Titanium Ore, Deepslate Titanium Ore, Titanium Block | 8 | `fabric:needs_tool_level_8` | Titanium Ingot | sim | sim | 3–7 |
| Rosalita Ore, Deepslate/Nether/Aurora Rosalita Ore, Rosalita Block | 9 | `fabric:needs_tool_level_9` | Rosalita Gem | sim | sim | 3–7 / 5–9 Nether / 6–11 Aurora |
| Sapphire Ore, Aurora Sapphire Ore | 10 | `fabric:needs_tool_level_10` | Sapphire Gem | sim | sim | 3–7 / 6–12 Aurora |

As loot tables já usam a alternativa Silk Touch antes do drop normal e
`minecraft:apply_bonus` com `minecraft:fortune`. O gate de ferramenta correta
ocorre antes da loot table no fluxo nativo do bloco/Fabric; por isso Fortune e
Silk Touch não permitem burlar nível ou tipo de ferramenta. O bônus de Emerald
Luck passou a verificar `hasCorrectToolForDrops`, fechando uma brecha onde os
drops extras poderiam aparecer mesmo em uma mineração inválida.

Não há blocos/minérios registrados para Chlorophyte, Vylam, Hero, Shadow ou
Vortex. As tags vazias 7, 11 e 12 existem como reservas documentadas e não
registram conteúdo inexistente. Quando os assets e blocos forem adicionados,
eles devem entrar respectivamente nos níveis 7, 8, 8, 11 e 12.

## Arquivos alterados

| Caminho | Alteração | Impacto |
| --- | --- | --- |
| `content/item/ProgressionMaterial.java` | novo catálogo central de progressão e fórmulas seguras | uma única fonte para tiers, armaduras e atributos |
| `content/item/*Tier.java` | fachadas delegam ao catálogo | preserva IDs/classes existentes sem números divergentes |
| `content/item/*ArmorMaterial.java` | durabilidade, defesa, reparo e resistências delegadas | armaduras existentes seguem a mesma progressão |
| `content/ModItems.java` | modificadores das ferramentas derivados do material | remove dano espalhado e mantém a Sapphire Sword especial |
| `content/progression/MiningProgression.java` | níveis canônicos de ferramenta/minério | matriz reutilizável e testável |
| `mixin/DiggerItemMiningLevelMixin.java` | ajuste mínimo para Gold Pickaxe | ouro passa a nível 2 sem alterar o tier vanilla inteiro |
| `resources/data/fabric/tags/blocks/needs_tool_level_*.json` | níveis 4–12 atualizados/adicionados | requisitos nativos de drop e mineração |
| `gameplay/EmeraldLuckSystems.java` | gate de ferramenta correta no bônus | não há drop extra com ferramenta inválida |
| `gameplay/ProgressionArmorProtection.java` | divisores centralizados | mitigação de conjunto limitada e ordenada |
| `mixin/ItemStackTooltipMixin.java` e traduções | tooltip de nível com nomes localizados | informação útil sem chave crua |
| `validation/ProgressionValidator.java` e `build.gradle` | validador headless e tarefa Gradle | regressões de cadeia, tags, loot e traduções falham no build |

## Testes executados

| Comando | Cenário | Resultado |
| --- | --- | --- |
| `./gradlew validateProgression --no-daemon --console=plain` | cadeia cumulativa, limites numéricos, todos os pares de nível, casos de fronteira, tags, pickaxe tag, Fortune/Silk nas 16 loot tables e traduções | passou |
| `./gradlew check remapJar --no-daemon --console=plain` | compilação completa, validadores existentes, nova validação e JAR remapeado | passou em 2 min 26 s; apenas aviso conhecido de depreciação do Gradle 9 |

O validador cobre sistematicamente todos os pares `picareta × minério`, os
casos exatamente abaixo/igual/acima e todos os casos de fronteira da
especificação. A validação de Fortune/Silk verifica que cada loot table mantém
as duas regras e que todos os blocos estão no tag nativo de picareta. O teste
de cliente dedicado, servidor dedicado e save/load exige uma sessão Minecraft
real; ele não é simulado pelo validador headless e deve ser feito antes de um
release público grande.

## Pendências explícitas

- O efeito especial próprio de Titanium ainda não foi definido no projeto. A
  estrutura central o registra como pendência; não foi inventado nenhum buff.
- Não há Copper Pickaxe, nem assets de ferramentas/armaduras para Vylam ou
  Vortex. Copper participa da matriz como nível 1, conforme solicitado.
- Não existem minérios/blocos/assets para Chlorophyte, Vylam, Hero, Shadow ou
  Vortex. As regras futuras estão prontas, mas nenhum bloco inexistente foi
  inventado.
- O ID já salvo é `vylam`; o nome “Vylan” da especificação é tratado como
  apresentação/documentação para não quebrar mundos existentes.
- Alterar Emerald Ore vanilla para nível 4 é uma mudança intencional da regra
  do mod e afeta toda mineração de esmeralda enquanto Chaotic Dimensions estiver
  carregado.
