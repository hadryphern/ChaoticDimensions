# Relatório técnico — referência local OreSpawn / Chaotic Dimensions

Data da auditoria: 29 de julho de 2026
Escopo: inspeção estática e extração local controlada; nenhuma classe do JAR
foi executada ou carregada pelo Fabric.

## Diagnóstico do projeto atual

| Item | Resultado |
|---|---|
| Loader | Fabric |
| Minecraft | 1.20.1 |
| Fabric Loader | 0.16.14 |
| Fabric API | 0.92.6+1.20.1 |
| Java | 17 |
| Mappings | Official Mojang mappings via Fabric Loom |
| Mod ID | `chaoticd` |
| Pacote-base | `net.blue.chaoticd` |
| Dependências adicionais | GeckoLib Fabric 4.4.9 e mclib 20 |

Os registries principais permanecem em `content/`, a inicialização comum está
em `ChaoticDimensions`, e o código de renderização fica em
`client/ChaoticDimensionsClient`. A infraestrutura nova respeita essa divisão:
apenas `test/orespawn/client/` referencia APIs de cliente.

## Arquivo legado analisado

| Item | Resultado |
|---|---|
| Nome | `orespawn-1.7.10-20.3 (1).jar` |
| Tamanho | 17.541.969 bytes |
| SHA-256 | `5b93a7c75323cb3021f5c9e5d19c4885721ec1376ff7fd24527156d0120ed7eb` |
| Integridade ZIP | `unzip -t` aprovado |
| Entradas ZIP | 1.974 |
| Arquivos não-diretório | 1.965 |
| Classes | 594 |
| Bytecode | major 50 / Java 6 |
| Namespace principal | `danger/orespawn` |
| Metadata | `mcmod.info` legado |
| Mod ID declarado | `orespawn` |
| Versão Minecraft declarada | 1.7.10 |
| Versão interna declarada | 1.7.10-20.2 |

Há uma divergência importante: o nome do arquivo diz `20.3`, enquanto o
metadata interno diz `20.2`. Sem uma fonte primária, não se deve afirmar qual
release é o correto.

O manifest contém apenas `Manifest-Version: 1.0`; não há assinatura embutida.
Não foram encontradas bibliotecas externas incorporadas. As referências de
classes confirmam dependência de Forge/FML e da API Minecraft 1.7.10, que não
é compatível diretamente com Fabric 1.20.1. Os nomes encontrados são em geral
legíveis; as poucas classes anônimas aparentam ser auxiliares usuais de
renderização, não ofuscação significativa.

## Licença e limites

Não foi encontrado `LICENSE`, `NOTICE`, `COPYING`, `README` ou autorização de
redistribuição no arquivo. O metadata atribui autoria/crédito a
`TheyCallMeDanger`, mas isso não concede licença de port, cópia ou publicação.

Por esse motivo, o JAR, seus bytes brutos, arquivos extraídos, código
decompilado e qualquer asset ficam apenas em `.assets/Orespawn/` local, fora do
Git e do pacote gerado. Esta decisão bloqueia qualquer port real até haver
autorização verificável.

## Extração local e inventário

Estrutura local criada:

```text
.assets/Orespawn/
├── manifest/
├── inventory/
├── reports/
├── raw/
├── decompiled-reference/
├── converted/
└── mappings/
```

O conteúdo foi lido como ZIP e validado por CRC; ele não foi executado. O
inventário local tem arquivos JSON por categoria e marca os resultados como
`DISCOVERED`, `EXTRACTED`, `BLOCKED` ou `HEURISTIC_ONLY` quando a inferência é
apenas baseada no nome/caminho.

| Categoria inventariada localmente | Quantidade candidata |
|---|---:|
| Texturas PNG | 1.058 |
| Áudios OGG | 310 |
| Texturas de item | 474 |
| Texturas de bloco | 248 |
| Texturas/modelos de entidade | 336 |
| Eventos de som | 126 |
| Itens (heurística) | 90 |
| Blocos (heurística) | 59 |
| Entidades (heurística) | 134 |
| Bosses (heurística) | 17 |
| Modelos (heurística) | 235 |
| Dimensões/sistemas de dimensão (heurística) | 16 |
| Estruturas (heurística) | 18 |
| Sistemas de geração (heurística) | 41 |

Esses números não significam que o conteúdo foi convertido, testado ou que os
nomes/atributos foram confirmados por decompilação. Os inventários completos
são deliberadamente locais para não redistribuir um índice detalhado de
conteúdo potencialmente protegido.

## Compatibilidade técnica

Uma conversão direta é bloqueada por diferenças fundamentais entre Forge
1.7.10 e Fabric 1.20.1: registries, DataFixer, mappings, entidades, IA,
renderização, rede, NBT, mundo, dimensões, geração de terreno, sons, modelos e
datapacks. O bytecode Java 6 do JAR não é carregável nem reutilizável como
implementação moderna.

Qualquer conteúdo futuro autorizado deve ser reimplementado do zero com APIs
modernas, em etapas isoladas: item simples, bloco simples, som autorizado,
entidade simples, entidade complexa, boss e, por último, dimensão/worldgen.

## Implementação desta etapa

O pacote `net.blue.chaoticd.test.orespawn` contém somente infraestrutura
original e removível:

| Elemento | ID | Estado |
|---|---|---|
| Marcador inerte | `chaoticd:orespawn_test_reference_marker` | Testável, código original |
| Spawn egg de proxy | `chaoticd:orespawn_test_reference_proxy_spawn_egg` | Testável, código original |
| Proxy de referência | `chaoticd:orespawn_test_reference_proxy` | Testável, herda comportamento vanilla de Pig |
| Aba | `chaoticd:orespawn` | Vazia com configuração desligada |
| Sons/assets/classes do JAR | — | 0 carregados e 0 portados |

O item marcador usa o ícone vanilla `minecraft:item/barrier` de maneira
consciente, como sinal de conteúdo de teste. O proxy usa apenas renderer,
modelo, textura, AI e sons do porco vanilla. Não é uma aproximação de qualquer
mob legado.

### Toggle e registries

`config/chaoticd-orespawn-test.properties` contém
`enableOrespawnTestContent=false` por padrão. Com `false`, a aba fica sem
entradas e as ações de entregar/invocar/listar são bloqueadas. `status` e
`validate` continuam disponíveis para diagnóstico.

Os três IDs inertes e a aba são registrados dos dois lados, mesmo com o toggle
desligado. Isso é uma proteção contra registries assimétricos: Fabric exige que
cliente e servidor concordem com o conjunto de registries antes da conexão.
Os IDs não carregam assets nem código legado e não alteram as dimensões,
worldgen, receitas, tags ou progressão do mod principal.

## Aba Chaotic Test

O critério foi corrigido para ser exclusivamente visual. A fonte única é
`PlaceholderCatalog`: uma entrada só aparece em `Chaotic Test` se utilizar
asset alias legado do próprio projeto, textura vanilla provisória, modelo
provisório, debug ou textura final ausente. Item funcional sem receita, item de
progressão futura ou conteúdo ainda sem uso não entra por esse motivo apenas.

Foram mantidos 11 blocos pastel que usam aliases visuais provisórios. Foram
movidos para a aba de teste 11 itens cuja aparência ainda é fallback vanilla.
Itens que já possuem modelos e PNGs próprios deixaram a aba de teste e seguem
na aba principal organizada.

## Validações e testes

| Teste | Resultado desta etapa |
|---|---|
| Inspeção ZIP sem execução | aprovado |
| Integridade ZIP | aprovado |
| Compilação Java + recursos | aprovado |
| Validador de isolamento Orespawn | incluído; executado em `./gradlew check` |
| Validador do catálogo Chaotic Test | incluído; executado em `./gradlew check` |
| Abrir cliente/servidor com toggle false | requer execução manual/ambiente gráfico ou servidor com EULA já aceita |
| Teste funcional com toggle true | requer mundo descartável e mesma configuração nos dois lados |

Nenhum teste de boss, dimensão, áudio ou conteúdo legado é declarado como
concluído, pois nenhum deles foi convertido ou carregado.

## Pendências e próximo passo seguro

| Item | Status | Motivo |
|---|---|---|
| Itens/blocos legados | bloqueado | não há autorização de redistribuição confirmada |
| Entidades/bosses legados | bloqueado | mesma limitação legal e port complexo |
| Áudio/modelos/texturas legados | bloqueado | material protegido, não copiado |
| Dimensões/worldgen legados | bloqueado | dependem de reimplementação autorizada e análise individual |
| Proxy original de teste | pronto para teste local | independente do JAR |

Caso exista autorização verificável para um conteúdo específico, o próximo
passo deve ser um pedido limitado a um único item/bloco original do Chaotic
Dimensions inspirado no comportamento estudado, com implementação e assets
novos. Não se deve tratar este relatório como autorização para converter ou
distribuir o OreSpawn.
