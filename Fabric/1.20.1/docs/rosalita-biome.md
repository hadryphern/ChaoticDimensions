# Rosalita Biome — implementação Fabric 1.20.1

## Registro e geração

- Bioma: `chaoticd:rosalita_biome`.
- A geração natural é feita por uma região TerraBlender `chaoticd:rosalita_overworld` de peso `3`.
- A região cobre clima temperado/úmido, interior, erosão baixa a média e relevo de superfície. Ela não substitui todos os biomas vanilla.
- O bioma tem listas de spawn vazias e não inclui minérios, monster rooms, geodos ou estruturas nas suas features.

## Paleta e recursos

As constantes estão em `RosalitaConstants`:

| Grupo | Valores |
| --- | --- |
| Pedra | `#46001E`, `#770032`, `#A80046`, `#D10058` |
| Realces | `#FF006B`, `#FC6FAA`, `#FFA3CA` |
| Ambiente | grama `#D10058`, folhas `#FF006B`, céu `#FFC4DD`, neblina `#F2A8C7`, água `#C85B89`, neblina d'água `#6D163B` |

`tools/generate_rosalita_textures.py` cria as oito texturas finais de 16×16 com seed fixa. Ele não é usado em runtime.

## Árvores

As configured features `rosalita_oak_tree`, `rosalita_birch_tree`, `rosalita_pine_tree` e `rosalita_acacia_tree` usam apenas `minecraft:oak_log` e `chaoticd:rosalita_leaves`.

O seletor `rosalita_trees` usa as probabilidades efetivas 35% / 25% / 20% / 20% e a densidade de cinco tentativas por chunk. As chances do `random_selector` são condicionais para preservar essa distribuição.

## Domínio subterrâneo

`RosalitaBiomeArea.isInsideRosalitaSurfaceColumn` resolve o bioma na superfície da mesma coluna X/Z. A feature `rosalita_underground` trabalha uma vez durante a geração de chunks novos e nunca roda em ticks normais.

Ela preserva ar, líquidos, bedrock e blocos que não sejam pedra natural. Troca stone/deepslate e suas variantes por pedras Rosalita, além de trocar todos os blocos da tag `chaoticd:forbidden_in_rosalita_underground`. A tag `chaoticd:allowed_rosalita_ores` começa vazia e será a allowlist de minérios Rosalita futuros.

## Política de mobs

`NaturalSpawnerMixin` intercepta exclusivamente a validação de spawn feita pelo `NaturalSpawner`. Em colunas Rosalita, toda tentativa natural é negada, exceto entity types na tag vazia `chaoticd:allowed_in_rosalita_biome`.

O mixin não intercepta `/summon`, spawn eggs, pets, entidades que já existam ou entidades que entrem caminhando no bioma.

## Teste manual em jogo

1. Abra `Fabric/1.20.1` no Cursor e execute `./gradlew runClient`.
2. Crie **um mundo novo** com cheats.
3. Execute `/locate biome chaoticd:rosalita_biome` e teleporte para o resultado.
4. Confirme céu e grama rosados, folhas sem fallback verde e os quatro formatos de árvore.
5. Entre em cavernas abaixo do bioma e verifique as pedras Rosalita, a ausência de minérios vanilla e de spawns naturais.
6. Teste `/summon minecraft:zombie ~ ~ ~` e um spawn egg: ambos devem continuar funcionando manualmente.

## Validações automatizadas executadas

- `./gradlew runDatagen`: sucesso; o projeto não declara um entrypoint de datagen, portanto não gerou arquivos.
- `./gradlew build`: sucesso.
- `./gradlew runServer`: carregou Fabric, TerraBlender, região Rosalita e o mod; parou somente porque a EULA ainda não foi aceita neste ambiente.

Não foi criado nem modificado um mundo de teste durante essa validação.
