# Céu da Dimensão Aurora

## Escopo

Esta implementação é exclusiva de `chaoticd:aurora_dimension` e roda no cliente. Ela não muda a
geração das ilhas, árvores, blocos, raridades, iluminação de gameplay ou qualquer dimensão vanilla.

O projeto usa Minecraft 1.20.1, Fabric Loader 0.16.14, Fabric API 0.92.6, mappings oficiais da Mojang
e Java 17. Os hooks utilizados pertencem à API de renderização do Fabric; não há mixin de céu nem
dependência visual externa.

## Arquitetura

- `AuroraVisuals`: registra os efeitos, filtra a dimensão e controla o ciclo de vida dos buffers.
- `AuroraVisualConfig`: concentra a paleta e todos os parâmetros ajustáveis.
- `AuroraDimensionEffects`: mantém o céu vanilla completo e aplica a resposta suave da fog à luz.
- `AuroraCloudRenderer`: desenha duas camadas de nuvens pastel com buffers persistentes.
- `AuroraRainbowRenderer`: gera dois arcos procedurais pastel e os envia em uma única draw call.

O céu rosa e a cor de limpeza vêm dos campos `sky_color` e `fog_color` do bioma. O tipo da dimensão
usa o efeito `chaoticd:aurora`, registrado somente pelo inicializador client-side. O sol e o fundo
vanilla foram preservados de propósito: substituir o renderer de céu inteiro duplicaria estados de
Blindness, fluidos, horizonte e shaders sem melhorar o resultado visual.

Os arco-íris são desenhados depois do céu e antes do terreno. Assim, ilhas os ocultam naturalmente,
eles permanecem no horizonte ao jogador se mover e não parecem uma imagem sobreposta à frente dos
blocos. Blindness, Darkness, fog de boss e câmera submersa ocultam os arcos.

## Parâmetros atuais

| Efeito | Parâmetro | Valor |
| --- | --- | --- |
| Céu | RGB | `#EAB8D7` |
| Fog/clear | RGB | `#F3DDEA` |
| Nuvem principal | altura, período, opacidade | `Y 308`, `1280 blocos`, `0.64` |
| Nuvem principal | velocidade X/Z | `0.18 / 0.04 blocos/s` |
| Nuvem secundária | altura, período, opacidade | `Y 356`, `920 blocos`, `0.30` |
| Nuvem secundária | velocidade X/Z | `-0.07 / 0.13 blocos/s` |
| Arco principal | direção, distância, opacidade | `-32°`, `210`, `0.32` |
| Arco secundário | direção, distância, opacidade | `152°`, `250`, `0.14` |
| Arcos | resolução | `72 segmentos`, `3 subdivisões por faixa` |
| Arcos | escala de render | `0.38` (mantém a aparência dentro do far plane mínimo) |

As duas nuvens reutilizam a máscara tileable de nuvens do Minecraft, mas possuem escala, altura,
cor e movimento diferentes. O resultado preserva grupos e clareiras da textura em vez de preencher
o céu com uma cor opaca. O movimento usa `gameTime + tickDelta`, portanto não depende do FPS.

## Custo e ciclo de vida

- nuvens: 32.768 vértices persistentes e duas draw calls por frame quando habilitadas;
- arco-íris: 10.368 vértices persistentes e uma draw call por frame;
- nenhuma malha ou coleção é reconstruída no loop normal de renderização;
- buffers são criados de forma lazy na render thread;
- reload de recursos invalida os buffers e o próximo frame os recria;
- `Clouds: OFF` desliga completamente o renderer de nuvens;
- a filtragem por chave de dimensão evita qualquer trabalho nas dimensões vanilla.

Cada plano de nuvens é dividido em células de 128 blocos. A malha continua cacheada, mas impede que
uma camada inteira seja descartada pelo far plane quando o jogador usa distância de renderização 2.
O plano se desloca por uma fase global limitada e contínua, preservando a ancoragem da textura sem
reconstruir vértices.

## Ajustes futuros

Edite somente `AuroraVisualConfig` para alterar paleta, altura, escala, cobertura, velocidade ou
arcos. Opacidades somadas devem continuar moderadas; velocidades pequenas evitam uma atmosfera
agitada. A altura das nuvens é absoluta, então precisa acompanhar futuras mudanças grandes na faixa
vertical das ilhas.

Limitação deliberada: as nuvens são camadas planas no estilo Minecraft, não volumes 3D. Isso mantém
o efeito estável em Fast/Fancy/Fabulous e barato. Sodium/Iris não fazem parte do ambiente atual;
como a implementação usa hooks e shaders vanilla do Fabric, a compatibilidade é favorecida, mas
esses mods ainda exigem uma rodada visual própria caso sejam adicionados no futuro.

## Validação

`./gradlew validateAuroraVisuals` confere a ligação do registry, cores processadas, preservação da
iluminação, separação/movimento/opacidade das nuvens e toda a malha procedural sem abrir OpenGL. A
tarefa integra `check`. O teste em jogo deve complementar isso olhando quatro direções, abaixo,
dentro e acima das nuvens, perto/longe das ilhas e nas três dimensões vanilla.
