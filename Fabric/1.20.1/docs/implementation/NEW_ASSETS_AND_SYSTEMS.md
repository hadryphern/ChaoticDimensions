# Integração de novos assets e sistemas

Data: 2026-07-29
Alvo: Fabric 1.20.1 / `chaoticd`

## Escopo aplicado

- Os 23 arquivos recebidos em `.assets/adicionar/` foram classificados e a pasta
  foi esvaziada sem descartar nenhum original.
- Os assets de item utilizáveis foram colocados em
  `src/main/resources/assets/chaoticd/textures/item/`, com modelo, registro e
  tradução em `pt_br`, `en_us`, `es_co` e `es_mx`.
- As cinco almas animadas foram convertidas do GIF para o formato de animação de
  resource pack do Minecraft.
- Ferramentas que têm material, sprite e posição técnica definida foram
  registradas. Não receberam receita enquanto o material-base não possui fonte
  legítima no mundo.
- As regras de almas usam tags de entidades e checagem da dimensão no servidor.
  As tags começam vazias porque ainda não há mobs naturais/minibosses definidos
  para Aurora ou Shadow; portanto spawn eggs não viram uma fonte de farm.
- A receita Crystal Log -> 4 Crystal Planks foi adicionada. Os blocos Crystal
  inteiros deixaram de usar `noOcclusion`, reduzindo faces internas renderizadas
  entre blocos adjacentes sem alterar os pixels transparentes.

## Assets recebidos

| Original em `.assets/adicionar/` | Destino | Integração |
|---|---|---|
| `aurora_soul.gif` | `textures/item/aurora_soul.png`, `animation_sources/aurora_soul.gif` | Item existente; quatro frames animados |
| `crystaline_soul.gif` | `textures/item/crystaline_soul.png`, `animation_sources/crystaline_soul.gif` | Item existente; quatro frames animados |
| `demonic_soul.gif` | `textures/item/demonic_sould.png`, `animation_sources/demonic_soul.gif` | Item existente; preserva o ID legado `demonic_sould` |
| `shadow_soul.gif` | `textures/item/shadow_soul.png`, `animation_sources/shadow_soul.gif` | Item existente; quatro frames animados |
| `void_soul.gif` | `textures/item/void_soul.png`, `animation_sources/void_soul.gif` | Item existente; quatro frames animados |
| `chlorophyte_ingot.png` | `textures/item/chlorophyte_ingot.png` | Item novo registrado |
| `chlorophyte_pickaxe .png` | `textures/item/chlorophyte_pickaxe.png` | Item novo; espaço no nome normalizado |
| `derman_gem.png` | `textures/item/derman_gem.png` | Material novo registrado |
| `hero_gem.png` | `textures/item/hero_gem.png` | Material novo registrado; não é textura de minério |
| `hero_{sword,axe,pickaxe,shovel,hoe}.png` | `textures/item/hero_*.png` | Ferramentas Hero registradas |
| `shadow_gem.png`, `shadow_nugget.png` | `textures/item/` | Materiais novos registrados |
| `shadow_sword.png`, `shadow_pickaxe.png` | `textures/item/` | Ferramentas Shadow registradas |
| `sun_tear.png`, `sun_peak.png` | `textures/item/` | Itens futuros registrados sem fonte natural |
| `vylam_gem.png` | `textures/item/vylam_gem.png` | Material novo registrado; o ID segue o arquivo fornecido |
| `vortex_gem.png` | `.assets/New Assets/items/ores/imported_duplicates/vortex_gem.png` | Duplicata byte a byte do asset runtime existente; preservada sem sobrescrever |
| `vortex_sword_texture.png` | `.assets/New Assets/items/tools/pending_specification/vortex_sword_texture.png` | Preservada como pendente: não há especificação de item, tier, atributos ou obtenção |

Cada GIF original contém quatro quadros de 22x22 pixels a 100 ms. O PNG
resultante é uma folha vertical de 22x88 pixels e o arquivo `*.png.mcmeta`
configura `frametime: 2`, ordem `0..3` e repetição contínua. Isso é o formato
nativo carregado no inventário, mão, hotbar, item no chão e interfaces.

## Itens e ferramentas

Novos IDs: `shadow_gem`, `shadow_nugget`, `vylam_gem`,
`chlorophyte_ingot`, `hero_gem`, `derman_gem`, `sun_tear`, `sun_peak`,
`shadow_sword`, `shadow_pickaxe`, `chlorophyte_pickaxe`, `hero_sword`,
`hero_axe`, `hero_pickaxe`, `hero_shovel` e `hero_hoe`.

Todos aparecem na aba única **Chaotic Dimensions**, depois dos blocos e na
seção correta de ferramentas ou materiais. Materiais usam o novo limite global
quando ele estiver ativo; ferramentas continuam não empilháveis por terem
durabilidade.

### Tiers novos

O projeto já expressa seus tiers pós-Netherite como multiplicadores do Tier de
Netherite: Emerald 2x, Ruby 4x, Jaxy 8x, Titanium 40x, Rosalita 100x e
Sapphire 1000x. `FutureProgressionTier` centraliza os novos valores:

| Tier | Multiplicador aplicado | Durabilidade | Velocidade | Bônus de dano | Nível | Encantabilidade | Reparo |
|---|---:|---:|---:|---:|---:|---:|---|
| Shadow | 8x | 16.248 | 72 | 32 | 7 | 120 | Shadow Gem |
| Chlorophyte | 200x | 406.200 | 1.800 | 800 | 8 | 3.000 | Chlorophyte Ingot |
| Hero | 1000x | 2.031.000 | 9.000 | 4.000 | 9 | 15.000 | Hero Gem |

Shadow ocupa uma rota lateral no mesmo patamar Jaxy. Como não foi fornecida
uma ferramenta Vylam, Chlorophyte aplica a primeira etapa concreta de 5x após
Titanium e Hero aplica a segunda. Essa decisão evita criar uma ferramenta
inexistente, não altera os tiers existentes salvos e deixa a fórmula explícita
para o próximo rebalanceamento.

As armas seguem a proporção já adotada pelo projeto: Sword usa bônus-1, Axe
usa 1,5*bônus-1, Pickaxe usa bônus/2-1 e Shovel usa 0,625*bônus-1. Não há
receitas de ferramentas porque Shadow Gem, Chlorophyte Ingot e Hero Gem ainda
não possuem fonte de survival definida. A receita segura adicionada é:

- 1 Shadow Gem -> 9 Shadow Nuggets;
- 9 Shadow Nuggets -> 1 Shadow Gem.

## Almas e drops preparados

`DimensionSoulDrops` é chamado no evento de morte do servidor. A regra só é
ativada se o tipo da entidade estiver em uma das tags abaixo e se a entidade
morrer na dimensão correta:

| Tag | Dimensão | Drop | Chance |
|---|---|---|---:|
| `chaoticd:aurora_common_mobs` | Aurora | Aurora Soul | 5% |
| `chaoticd:aurora_common_mobs` | Aurora | Crystaline Soul | 5% |
| `chaoticd:aurora_minibosses` | Aurora | Demonic Soul (`demonic_sould`) | 15% |
| `chaoticd:shadow_common_mobs` | Shadow | Shadow Soul | 5% |
| `chaoticd:shadow_minibosses` | Shadow | Void Soul | 55% |

As duas almas comuns da Aurora têm, intencionalmente, origem e chance iguais;
os sorteios são independentes, portanto um mob pode entregar as duas. As tags
começam vazias até que os mobs naturais/minibosses sejam definidos. Isso é uma
proteção contra drops de entidades invocadas por spawn egg.

## World generation deliberadamente pendente

Não foram criados blocos ou minérios com textura provisória. Faltam sprites de
bloco para `shadow_gem_ore`, `vylam_ore`, `chlorophyte_ore` e `hero_ore` (ou
`crystal_hero_ore`). Hero Gem é somente o ícone do item. Também não existe
bioma ácido para Chlorophyte. Assim, estes materiais estão registrados e sem
obtenção natural até que os assets e as definições existam.

Quando as texturas chegarem, os pontos de extensão são:

- Shadow Gem: feature no biome Shadow, substituindo apenas `shadow_stone`;
- Vylam: deepslate entre Y -59 e -55, veio 1 e extremamente raro, nunca
  substituindo Bedrock;
- Chlorophyte: somente um futuro biome ácido do Overworld;
- Hero: somente Crystal Dimension, após existir Crystal Stone/base do minério.

Vortex Gem e Sun Tear permanecem sem fonte, pois pertencem à futura **Chaotic
Dimension** (nome consistente com `chaoticd` e Chaotic Dimensions). Sun Peak
permanece sem fonte até a Light Dimension.

## Crystal: auditoria e mudanças seguras

Já existem Crystal Dirt/Grass/Log/Planks, três folhas, quatro plantas, Furnace,
Crafting Table, menus, loot tables, tags, regra Silk Touch e spawn eggs. A
receita de planks foi completada e o culling de faces de blocos completos foi
restaurado. As PNGs atuais têm alpha binário (0/255): `cutout` preserva os
vazios; trocar para `translucent` não criaria vidro semitransparente e pioraria
o desempenho.

Não foi criada a Crystal Dimension ainda. Faltam Crystal Stone, Cobblestone,
deepslate/equivalente, sand, gravel, wood/stripped wood, sapling, minérios,
bioma, terrain/noise, cavernas, montanhas, portal/acesso, spawns, estruturas,
efeitos de céu/neblina, sons, partículas e advancements. Criar a dimensão
antes desses componentes produziria uma geração visual incompleta e impediria
o Hero Ore de ter uma base correta.

## Pendências para a próxima etapa

- [ ] textura de bloco Hero Ore e definição do bloco-base Crystal;
- [ ] texturas de bloco para Shadow Gem, Vylam e Chlorophyte ores;
- [ ] especificação/asset do biome ácido e cadeia de refino Chlorophyte;
- [ ] mobs comuns e minibosses naturais para Aurora e Shadow, depois preenchendo
  as tags de almas;
- [ ] especificações e texturas da Chaotic Dimension e Light Dimension;
- [ ] especificação de item para Vortex Sword;
- [ ] confirmação de que Aurora Soul e Crystaline Soul devem continuar com os
  mesmos 5% nos mesmos mobs.

## Arquivos alterados e finalidade

| Caminho | Finalidade da alteração |
|---|---|
| `content/ModItems.java` | Registros dos materiais e ferramentas novos; nenhum item recebe receita sem uma fonte de survival definida. |
| `content/ModItemGroups.java` | Ordena os itens novos na única aba **Chaotic Dimensions**, separando ferramentas de materiais. |
| `content/item/FutureProgressionTier.java` | Centraliza o tier das ferramentas Shadow, Chlorophyte e Hero e a calibração compatível com a progressão já lançada. |
| `content/ModTags.java`, `content/ModGameplayEvents.java`, `gameplay/DimensionSoulDrops.java` | Tags e sorteios de almas, exclusivamente no servidor e na dimensão correta. |
| `content/ModBlocks.java`, `data/chaoticd/recipes/crystal_planks.json` | Restaura a oclusão dos blocos Crystal inteiros e adiciona Crystal Log → 4 Crystal Planks. |
| `models/item/{aurora_soul,crystaline_soul,demonic_sould,shadow_soul,void_soul}.json` | Faz os cinco itens existentes usarem as folhas de animação convertidas. |
| `models/item/{chlorophyte_ingot,chlorophyte_pickaxe,derman_gem,hero_*,shadow_*,sun_*,vylam_gem}.json` | Modelos `generated` ou `handheld` para cada asset novo efetivamente registrado. |
| `textures/item/` | PNGs finais dos assets recebidos e cinco `*.png.mcmeta` de animação. |
| `data/chaoticd/recipes/shadow_*` | Conversão reversível de 1 Shadow Gem ↔ 9 Shadow Nuggets. |
| `data/chaoticd/tags/entity_types/*.json` | Pontos de extensão vazios para classificar mobs comuns/minibosses quando eles existirem. |
| `mixin/{ItemMaxStackSizeMixin,ContainerMaxStackSizeMixin,FriendlyByteBufItemStackMixin,ItemStackCountSerializationMixin,ItemEntityStackMergeMixin,StackedContentsMixin}.java` | Camada real de stack 999: item, inventário, rede, save, item no chão e livro de receitas. |
| `mixin/ServerGamePacketListenerImplMixin.java`, `chaoticd.mixins.json` | Ajusta a validação de creative para 999 e registra todos os mixins de stack. |
| `network/StackSizeProtocol.java`, `client/StackSizeProtocolClient.java` | Handshake no login: bloqueia antes do play uma conexão sem o protocolo de stack compatível. |
| `content/{entity/SirOrensEntity,menu/SirOrensTradeMenu,trade/SirOrensTrade}.java`, `client/screen/SirOrensTradeScreen.java` | XP gradual, sincronização e apresentação tradicional das trocas de Sir. Orens. |
| `models/item/underguer_sigil.json` | Corrige uma referência vanilla inexistente (`wither_skeleton_skull`) para `echo_shard`, removendo o aviso de textura ausente legado. |
| `lang/{pt_br,en_us,es_co,es_mx}.json` | Nomes dos 16 novos IDs e mensagens do protocolo de stack nas quatro línguas obrigatórias. |
| `test/.../AuroraAssetsValidator.java` | Garante modelos/traduções novos e cinco folhas animadas de almas no artefato empacotado. |

Os arquivos de referência movidos para `.assets/New Assets/...` ficam fora do
Git por escolha do projeto (`.assets/` é ignorada); os arquivos runtime acima
ficam dentro do JAR.

## Matriz de itens novos

Todos os materiais abaixo usam o limite padrão real de **999**. Ferramentas
continuam com stack **1**, pois possuem durabilidade. A política de cores de
nomes excepcional já existente não ganhou uma nova “classe de raridade”; não
foi adicionada nenhuma descrição artificial de raridade ao tooltip.

| ID | Nome visível em pt-BR | Categoria | Obtenção/receita | Situação |
|---|---|---|---|---|
| `aurora_soul` | Alma Aurora | material animado | 5% de `aurora_common_mobs` na Aurora | Tag aguardando mobs reais |
| `crystaline_soul` | Alma Cristalina | material animado | 5% de `aurora_common_mobs` na Aurora | Tag aguardando mobs reais |
| `demonic_sould` | Alma Demoníaca | material animado | 15% de `aurora_minibosses` na Aurora | ID legado preservado |
| `shadow_soul` | Alma Sombria | material animado | 5% de `shadow_common_mobs` na Shadow | Tag aguardando mobs reais |
| `void_soul` | Alma do Vazio | material animado | 55% de `shadow_minibosses` na Shadow | Tag aguardando mobs reais |
| `shadow_gem` / `shadow_nugget` | Gema/Pepita Sombria | material | 1 gema ↔ 9 pepitas; minério aguarda sprite | Registrados, sem geração provisória |
| `vylam_gem` | Gema de Vylam | material | Futuro minério próximo à Bedrock | Sem geração até existir textura de bloco |
| `chlorophyte_ingot` | Lingote de Clorofita | material | Futuro biome ácido/refino | Sem fonte inventada |
| `hero_gem` | Gema Heroica | material | Futuro minério Crystal | Ícone existe; Hero Ore não existe |
| `derman_gem` | Gema de Derman | material | Etapa futura | Registrada, sem fonte inventada |
| `vortex_gem` | Gema Vortex | material | Somente futura Chaotic Dimension | Sem trade, recipe ou drop alternativo |
| `sun_tear` | Lágrima Solar | material | Miniboss futura Chaotic Dimension | Registrada sem fonte |
| `sun_peak` | Pico Solar | material | Miniboss futura Light Dimension | Registrada sem fonte |
| `shadow_sword`, `shadow_pickaxe` | Espada/Picareta Sombria | ferramentas | Shadow Gem ainda sem fonte | Criativo/teste, sem recipe |
| `chlorophyte_pickaxe` | Picareta de Clorofita | ferramenta | Clorofita ainda sem fonte | Criativo/teste, sem recipe |
| `hero_sword`, `hero_axe`, `hero_pickaxe`, `hero_shovel`, `hero_hoe` | Ferramentas Heroicas | ferramentas | Hero Gem ainda sem fonte | Criativo/teste, sem recipe |

`vortex_sword_texture.png` não foi registrado: foi preservado em
`.assets/New Assets/items/tools/pending_specification/`, pois faltam ID,
atributos, tier, receita e método de obtenção.

## Ferramentas e armas

Os números abaixo são atributos do `Tier` e modificadores declarados pelo item.
O Minecraft ainda soma o atributo-base normal do jogador na exibição final.

| Item | Tier | Durabilidade | Vel. mineração | Bônus do tier | Modificador de dano | Ataque | Nível | Encantabilidade | Reparo | Receita |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---|---|
| Shadow Sword | Shadow | 16.248 | 72 | 32 | +31 | -2,4 | 7 | 120 | Shadow Gem | Não (sem fonte da gema) |
| Shadow Pickaxe | Shadow | 16.248 | 72 | 32 | +15 | -2,8 | 7 | 120 | Shadow Gem | Não |
| Chlorophyte Pickaxe | Chlorophyte | 101.550 | 450 | 200 | +99 | -2,8 | 8 | 750 | Chlorophyte Ingot | Não |
| Hero Sword | Hero | 121.860 | 540 | 240 | +239 | -2,4 | 9 | 900 | Hero Gem | Não |
| Hero Axe | Hero | 121.860 | 540 | 240 | +359 | -3,0 | 9 | 900 | Hero Gem | Não |
| Hero Pickaxe | Hero | 121.860 | 540 | 240 | +119 | -2,8 | 9 | 900 | Hero Gem | Não |
| Hero Shovel | Hero | 121.860 | 540 | 240 | +149 | -3,0 | 9 | 900 | Hero Gem | Não |
| Hero Hoe | Hero | 121.860 | 540 | 240 | -181 | -1,0 | 9 | 900 | Hero Gem | Não |

### Calibração da progressão

Os tiers existentes já persistidos são: Titanium 40×, Rosalita 100× e Safira
1000× da base Netherite. A sequência nova descreve vários “5×”, mas não
informa se são acumulados; cumulá-los ultrapassaria Rosalita/Safira e geraria
atributos impraticáveis. Para não alterar itens salvos, a escala provisória e
monótona é: Titanium 40× → Vylam reservado 45× → Chlorophyte 50× → Hero 60×
→ Derman reservado 70× → Vortex reservado 85× → Rosalita 100× → Safira 1000×.
Quando houver uma especificação completa de rebalanceamento, todos os tiers
devem ser migrados juntos, e não com multiplicações cegas isoladas.

## Animações

| GIF original | PNG final | Frames | Tempo | Loop | Metadados |
|---|---|---:|---:|---|---|
| `aurora_soul.gif` | `aurora_soul.png` | 4 | 100 ms (2 ticks) | Infinito, padrão Minecraft | `aurora_soul.png.mcmeta` |
| `crystaline_soul.gif` | `crystaline_soul.png` | 4 | 100 ms (2 ticks) | Infinito | `crystaline_soul.png.mcmeta` |
| `demonic_soul.gif` | `demonic_sould.png` | 4 | 100 ms (2 ticks) | Infinito | `demonic_sould.png.mcmeta` |
| `shadow_soul.gif` | `shadow_soul.png` | 4 | 100 ms (2 ticks) | Infinito | `shadow_soul.png.mcmeta` |
| `void_soul.gif` | `void_soul.png` | 4 | 100 ms (2 ticks) | Infinito | `void_soul.png.mcmeta` |

Os quadros originais têm 22×22 e foram mantidos sem reamostragem; cada folha
tem 22×88. O reload de recursos do Minecraft avisa que um sprite 22×22 reduz o
mipmap do atlas de nível 4 para 1. Não há perda de frames nem de alpha; foi
preferida fidelidade pixel a pixel ao GIF fornecido a uma ampliação irregular.
O impacto de FPS/mipmap deve ser comparado manualmente em uma sessão longa.

## Stack máximo 999

Não é apenas uma alteração visual. A implementação cobre:

- `ItemMaxStackSizeMixin`: somente o limite retornado como 64 passa a 999;
  itens explicitamente 1, 16 ou outro valor continuam nesse limite;
- `ContainerMaxStackSizeMixin`: inventário, baús, funis, fornalhas e containers
  que usam a capacidade padrão acompanham 999;
- `FriendlyByteBufItemStackMixin`: valores até 127 preservam byte vanilla;
  128–999 usam o marcador `-128` seguido de `VarInt`;
- `ItemStackCountSerializationMixin`: NBT preserva `Count` seguro e grava o
  inteiro complementar `chaoticd:stack_count`, restaurado no carregamento;
- `ItemEntityStackMergeMixin` e `StackedContentsMixin`: merge no chão e livro
  de receitas deixam de truncar em 64;
- `ServerGamePacketListenerImplMixin`: creative valida 999 e ainda recusa
  itens cujo próprio limite seja menor;
- `StackSizeProtocol`: handshake de login bloqueia cliente/servidor que não
  entenda a versão do protocolo antes de qualquer stack estendido ser enviado.

Exceções intencionais: slots especializados de armadura, poções, beacon,
bundle e brewing mantêm seus limites próprios; ferramentas, armas e armaduras
continuam não empilháveis. Itens de mods de terceiros que escolham 64
intencionalmente também receberão 999, pois o vanilla não expõe essa intenção
no getter; essa é a principal limitação de compatibilidade externa a revisar
caso a instalação passe a usar muitos mods de inventário.

## Villager Sir. Orens

Não foi adicionada dependência externa. A tela anterior era customizada porque
`MerchantOffer` vanilla aceita no máximo dois custos e os pedidos de Sir.
Orens chegam a seis custos de até 500 unidades; uma troca vanilla normal não
representaria isso sem quebrar a regra. O backend customizado preserva a
validação no servidor, enquanto a tela usa a textura vanilla
`minecraft:textures/gui/container/villager2.png`, layout de lista de trocas,
slots e barra de progresso para ficar próxima da interface tradicional.

O XP acumulado e persistente libera níveis em 0, 20, 50, 90 e 150 pontos. Só
trocas do nível atualmente aberto contam para liberar o próximo, evitando
farmar XP com ofertas antigas. A oferta de Vortex Gem foi removida, para ela
continuar exclusiva da futura Chaotic Dimension.

Cada oferta agora tem estoque persistente e sincronizado: Lava Ingot 12 usos,
Water Ingot 8, Demonith 3, cada uma das três ofertas de nível 4 tem 2, e Void
tem 1. A primeira troca inicia um relógio compartilhado de 24.000 ticks; ao
vencer, todas as ofertas usadas — inclusive as parcialmente usadas — voltam ao
estoque completo. O estado é salvo no NBT da entidade, a tela aplica a mesma
sobreposição vermelha vanilla a uma oferta esgotada e o servidor nunca remove
o pagamento se ela já estiver sem estoque.

## Testes e validações

| Verificação | Comando/método | Resultado nesta etapa |
|---|---|---|
| Compilação, testes, validadores e JAR | `./gradlew check remapJar --no-daemon --console=plain` | Aprovado; assets, árvores, visuais, worldgen Aurora e JAR remapeado aprovados |
| Paridade final de traduções/protocolo | `./gradlew validateAuroraAssets --no-daemon --console=plain` | Aprovado após incluir as quatro traduções do handshake |
| JSON de recursos | `jq` sobre todos os `*.json` novos | Aprovado |
| Modelos e PNGs novos | Validador empacotado + verificação de referências | Aprovado |
| GIFs | `identify` + `*.png.mcmeta` | 5 folhas 22×88, 4 frames, 2 ticks, aprovadas |
| Mixin/runtime anterior | Log do cliente já iniciado nesta etapa | Mixins de stack carregados sem `MixinApplyError`; não foi aberto novamente para preservar o mundo do usuário |

Testes manuais restantes, ainda necessários antes de distribuir uma versão
multiplayer pública:

- stack >127 após salvar/reabrir mundo, shift-click, divisão, hopper, fornalha
  e recipe book;
- cliente e servidor dedicado com a mesma versão, e recusa de cliente sem o
  protocolo de stack;
- fluxo completo de Sir. Orens: XP, uso máximo, reposição e persistência;
- drops depois de definir mobs reais nas quatro tags;
- FPS/carga de chunks quando a Crystal Dimension possuir terreno e transparência
  final.
