# API interna de raridades

Este documento descreve o sistema de raridades do Chaotic Dimensions para Minecraft 1.20.1,
Fabric Loader 0.16.14, mappings oficiais da Mojang e Java 17. A implementação não utiliza
dependências externas além do Fabric API já presente no projeto.

O sistema é visual e classificatório. Ele não altera dano, atributos, drops, compatibilidade de
encantamentos ou qualquer outra regra de gameplay.

## Arquitetura

| Classe | Responsabilidade |
| --- | --- |
| `RarityDefinition` | Fonte imutável de verdade para identidade, progressão, cor, animação e estilo de uma raridade. |
| `RarityStyle` | Define quais propriedades tipográficas pertencem à raridade e quais devem ser preservadas do `Component`. |
| `RarityContext` | Transporta o `ItemStack` e, quando aplicável, o encantamento e seu nível para os providers. |
| `RarityRegistry` | Registra definições, itens, tags, encantamentos e providers; publica snapshots imutáveis para leitura. |
| `RarityResolver` | Executa a ordem de resolução, lê override NBT e calcula a raridade final do stack. |
| `ItemRarityProvider` | Ponto de extensão para classificação dependente de NBT, Potion NBT ou estado em runtime. |
| `EnchantmentRarityProvider` | Ponto de extensão para classificação dependente do stack e do nível do encantamento. |
| `ModRarities` | Declara as doze raridades e registra todas as classificações atuais do mod. |
| `AnimatedColorProvider` | Calcula cores client-side a partir de um relógio monotônico injetável. |
| `TooltipRarityRenderer` | Copia e estiliza `Component`s sem compartilhar estado entre linhas. |
| `TooltipEnchantmentStyler` | Relaciona cada entrada NBT de encantamento à linha exata criada pelo Minecraft. |

As classes de resolução ficam em `net.blue.chaoticd.rarity`. A implementação exclusivamente visual
fica em `net.blue.chaoticd.client.rarity`.

`ModItemRarities` e `RarityText` continuam disponíveis como fachadas públicas compatíveis com o
código anterior. Elas estão marcadas como deprecated para que implementações novas usem a API
central, mas não foram removidas.

## Definições atuais

Todos os ciclos animados duram 3.600 ms. Animações de paleta usam interpolação RGB com
`smoothstep`. As raridades animadas usam gradiente por code point; raridades estáticas aplicam uma
cor uniforme.

| ID | Cor base | Prioridade | Threshold | Score por encantamento | Animação/paleta |
| --- | ---: | ---: | ---: | ---: | --- |
| `chaoticd:common` | `#55DFFF` | 0 | 0 | 1 | Estática |
| `chaoticd:uncommon` | `#55FF55` | 1 | 4 | 2 | Estática |
| `chaoticd:rare` | `#55FFFF` | 2 | 10 | 6 | Estática |
| `chaoticd:very_rare` | `#FF55FF` | 3 | 20 | 16 | Estática |
| `chaoticd:extremely_rare` | `#5555FF` | 4 | 36 | 25 | Estática |
| `chaoticd:ultra_rare` | `#AA00FF` | 5 | 60 | 40 | Estática |
| `chaoticd:impossible` | `#FF5555` | 6 | 100 | 65 | Estática |
| `chaoticd:forbidden` | `#AAAAAA` | 7 | 170 | 100 | `#AAAAAA → #FFFFFF` |
| `chaoticd:legendary` | `#FFD700` | 8 | 280 | 150 | Rainbow HSV contínuo, saturação 2/3 e brilho 1 |
| `chaoticd:extravagant` | `#FFD700` | 9 | 430 | 210 | `#FFD700 → #55DFFF` |
| `chaoticd:god` | `#FFD700` | 10 | 620 | 300 | `#FFD700 → #FFFFFF` |
| `chaoticd:endgame` | `#AA00FF` | 11 | 850 | 420 | `#AA00FF → #FF55FF → #5555FF → #111111` |

O threshold é o ponto mínimo da progressão agregada. O score é a contribuição de um encantamento
daquela raridade quando ele participa da classificação geral de um item.

## Ordem de resolução de itens

`RarityResolver.resolveItem(stack)` aplica esta ordem:

1. override explícito e válido no NBT do stack;
2. raridade registrada para o item exato;
3. provider especializado;
4. regra por `TagKey<Item>`;
5. provider de categoria ou material;
6. `ItemStack.getRarity()` vanilla;
7. fallback global `Common`.

O override NBT é autoritativo e não recebe promoção por encantamentos. Sem override, a raridade final
começa no threshold da raridade base e soma o score individual de cada encantamento. O maior
threshold alcançado define o resultado.

Encantamentos nível 50 ou superior no kit Sapphire já fazem parte da classificação Legendary desse
kit e, portanto, não somam score novamente. Isso evita uma promoção artificial dupla.

### Classificações específicas atuais

- Sapphire Gem, Sword, Pickaxe, Axe, Shovel e Hoe: `Legendary`;
- Potion, Splash Potion, Lingering Potion e Tipped Arrow com Potion NBT Sapphiric: `Ultra Rare`;
- IDs de item contendo `netherite`: `Extremely Rare`;
- IDs contendo `diamond`: `Rare`;
- IDs contendo `iron` ou `golden`: `Uncommon`;
- demais itens: raridade vanilla convertida para a progressão e, por fim, `Common`.

Itens de bloco seguem o mesmo pipeline. Sem registro, tag ou provider específico, eles usam sua
raridade vanilla.

## Ordem de resolução de encantamentos

Cada linha chama `RarityResolver.resolveEnchantment(stack, enchantment, level)` isoladamente:

1. providers contextuais em ordem numérica de prioridade;
2. registro exato do encantamento;
3. `Enchantment.Rarity` vanilla;
4. fallback da progressão.

O provider de prioridade 200 classifica como `Legendary` os encantamentos de nível 50 ou superior
em ferramentas Sapphire. Sapphiric, Dheathic, Big Bertha, Royal e Disparada são registrados como
`Ultra Rare`. Consequentemente, Sapphiric I e Dheathic I continuam roxos em uma Sapphire Sword; eles
não herdam a classificação Legendary do item.

Um provider separado classifica como `Very Rare` os níveis vanilla estendidos além destes limites:

- Sharpness, Smite, Bane of Arthropods e Efficiency: 5;
- Unbreaking, Looting, Fortune, Sweeping Edge e Thorns: 3;
- Protection, Fire Protection, Blast Protection, Projectile Protection e Feather Falling: 4;
- Knockback e Fire Aspect: 2.

Livros encantados calculam uma raridade geral para o nome e a linha final usando o mesmo sistema de
score. Cada encantamento armazenado mantém, simultaneamente, sua própria raridade visual.

## Override NBT no Minecraft 1.20.1

Minecraft 1.20.1 ainda não possui Data Components de item. O override utiliza um compound próprio:

```nbt
{ChaoticDimensions:{Rarity:"chaoticd:legendary"}}
```

Exemplo de comando:

```mcfunction
/give @s minecraft:diamond_sword{ChaoticDimensions:{Rarity:"chaoticd:legendary"}}
```

API correspondente:

```java
RarityResolver resolver = RarityResolver.global();
resolver.setExplicitRarity(stack, ModRarities.LEGENDARY);
resolver.explicitRarity(stack); // Optional<RarityDefinition>
resolver.clearExplicitRarity(stack);
```

O ID precisa existir em `RarityRegistry`. Strings inválidas ou IDs desconhecidos são ignorados e o
pipeline normal continua. O override afeta somente a classificação visual: inserir Legendary por
comando não concede atributos, encantamentos ou poder ao item.

## API de registro e consulta

Inicialização e consultas globais:

```java
ModRarities.bootstrap();
RarityRegistry registry = RarityRegistry.global();
RarityResolver resolver = RarityResolver.global();

RarityDefinition itemRarity = resolver.resolveItem(stack);
RarityDefinition baseRarity = resolver.resolveBaseItem(stack);
RarityDefinition enchantmentRarity = resolver.resolveEnchantment(stack, enchantment, level);
int score = itemRarity.progressionThreshold();
```

Pontos de extensão disponíveis:

```java
registry.registerDefinition(definition);
registry.registerItem(item, definition);
registry.registerItemTag(tag, definition);
registry.registerItemProvider(priority, provider);
registry.registerCategoryProvider(priority, provider);
registry.registerEnchantment(enchantment, definition);
registry.registerEnchantmentProvider(priority, provider);
```

Uma definição deve ser registrada antes de ser referenciada por outra regra. Registros específicos
devem ser preferidos para identidades fixas; tags servem para famílias data-driven; providers são
reservados a decisões que realmente dependem do `ItemStack`, NBT ou contexto do encantamento.

Exemplo mínimo de definição adicional:

```java
RarityDefinition example = RarityDefinition.builder(
        new ResourceLocation("examplemod", "example"),
        "rarity.examplemod.example",
        0x44CCFF)
    .priority(12)
    .progression(1_000, 500)
    .paletteAnimation(4_000L, RarityDefinition.Easing.SMOOTHSTEP,
        0x44CCFF, 0xFFFFFF)
    .style(RarityStyle.PRESERVE_WITH_GRADIENT)
    .fallback(ModRarities.COMMON.id())
    .build();

registry.registerDefinition(example);
registry.registerItem(exampleItem, example);
```

Registrar uma definição com ID duplicado gera erro deliberadamente. Isso impede que a fonte de
verdade seja substituída silenciosamente.

## Pipeline client de tooltip

Os três mixins visuais estão na lista `client` de `chaoticd.mixins.json`; eles não carregam em um
servidor dedicado.

1. `ItemStackNameMixin` estiliza o `Component` retornado por `getHoverName`. Nomes customizados
   mantêm itálico, hover, click, insertion e demais propriedades não controladas pela raridade.
2. `ItemStackTooltipMixin` intercepta a chamada vanilla a `ItemStack.appendEnchantmentNames` dentro
   de `getTooltipLines`. Cada `CompoundTag` conhecido é relacionado à linha que ele acabou de criar.
3. `EnchantedBookTooltipMixin` cobre separadamente `StoredEnchantments`, usado por
   `EnchantedBookItem`.
4. A linha final de raridade é criada como `Component.translatable` e estilizada com a definição
   geral do stack.

Não existe comparação por nome traduzido e nenhuma linha de encantamento é reconstruída a partir de
`getString()`. Encantamentos repetidos, incompatíveis obtidos por comando e idiomas diferentes não
fazem a cor avançar para a linha errada.

Atributos, lore e linhas acrescentadas por outros mods não entram no renderer de encantamentos.
Assim, a cor de um encantamento não vaza para Attack Damage, Attack Speed, descrição ou lore.

Inventário, baús, creative inventory e telas que utilizam `ItemStack.getTooltipLines` compartilham
esse pipeline automaticamente.

## Animações e interpolação

`AnimatedColorProvider` usa `System.nanoTime`, nunca horário civil, contador de frames, ticks ou
quantidade de itens renderizados. O construtor aceita um `LongSupplier`, permitindo testes com tempo
determinístico.

- a fase é calculada com `floorMod(nowNanos, cycleDurationNanos)`;
- mudança de FPS não altera a velocidade;
- mover o cursor ou reabrir a tooltip não reinicia o ciclo;
- nenhuma cor animada é enviada pela rede;
- a animação continua de forma visual enquanto a interface do cliente é renderizada.

Paletas comuns usam interpolação RGB por segmento. Antes da mistura, o progresso passa por
`smoothstep(t) = t²(3 − 2t)`, produzindo derivada suave nas trocas entre segmentos. O rainbow
Legendary usa rotação contínua no hue HSV, mantendo saturação e brilho estáveis e evitando cinza nas
transições.

Para gradiente textual, o renderer percorre `String.codePoints()`, não unidades UTF-16. Emojis,
acentos e caracteres suplementares não são divididos. Espaços participam da posição do gradiente;
códigos de formatação permanecem em `Style` e não contam como caracteres visíveis.

Em cores uniformes, a árvore original de `Component` e seu `TranslatableContents` são copiados. Em
gradientes por caractere, o texto já localizado é visitado e convertido em componentes por code
point, preservando o `Style` efetivo de cada trecho, incluindo hover, click, insertion, negrito,
itálico, underline, strikethrough e fonte.

## Segurança e desempenho

- definições e paletas são imutáveis;
- o registry sincroniza escrita e publica mapas/listas imutáveis para leitura;
- a progressão ordenada é calculada durante registro, não a cada frame;
- IDs, itens fixos, tags e providers ficam armazenados centralmente;
- paletas são lidas por índice durante renderização, sem clone por caractere;
- cores animadas nunca são armazenadas como cor final em cache;
- stacks completos não são cacheados, pois NBT, encantamentos e conteúdo podem mudar em runtime;
- a animação é client-side e não cria tráfego ou estado sincronizado por tick;
- override NBT desconhecido não causa crash nem aceita uma definição não registrada.

Providers devem ser baratos e não devem alterar o stack. Regras que consultem mundo, rede ou dados
mutáveis caros precisam de cache e invalidação próprios antes de serem registradas.

## Validação executada

`RarityApiValidator` valida de forma determinística:

- cores base, thresholds, scores e paletas preservados;
- prioridade e remoção do override NBT;
- fallback seguro para NBT malformado;
- clock monotônico injetável e fechamento correto do ciclo;
- midpoint RGB com smoothstep;
- rainbow HSV e saturação estabelecida;
- code points Unicode, incluindo emoji;
- preservação de tradução, insertion, negrito, itálico e underline;
- ausência de mutação ou compartilhamento de cor entre componentes.

Resultados da implementação atual:

```text
RARITY API VALIDATION PASSED: balance, NBT priority, smooth time, HSV, Unicode and Style isolation.
BUILD SUCCESSFUL
```

O cliente de desenvolvimento também foi aberto e testado no creative inventory:

- Sapphire Axe: nome e encantamentos nível L com animação Legendary; atributos vanilla continuaram
  verdes e independentes;
- livro Sapphiric III: nome/raridade geral `Extremely Rare` em azul e linha Sapphiric `Ultra Rare`
  em roxo;
- carregamento, mundo integrado e encerramento ocorreram sem erro de mixin ou raridade no log.

## Limitações atuais

- JEI, REI e EMI não estão instalados no ambiente de desenvolvimento e, portanto, não receberam um
  teste direto. Telas que delegam ao pipeline padrão de `ItemStack` devem funcionar; uma tela que
  reconstrua strings manualmente precisará de integração própria.
- um componente animado criado uma única vez fora de uma tooltip, como uma mensagem persistida no
  chat, mantém a amostra de cor daquele instante. Tooltips são reconstruídas durante renderização e
  permanecem animadas.
- em gradientes por code point, o resultado visual mantém a tradução localizada e os estilos, mas o
  componente produzido deixa de expor o `TranslatableContents` original a mods que inspecionem sua
  estrutura depois da estilização.
- classificações são registradas em código, tags e providers. Não existe ainda um reload listener
  JSON para raridades; adicionar um exige definir sincronização cliente/servidor e invalidação segura
  durante reload de datapacks.
