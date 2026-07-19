# Inventário de fontes manuais — Rosalita

Fonte auditada: `.assets/New Assets/`. Os arquivos abaixo pertencem ao autor,
não são alterados nem carregados diretamente pelo mod. Esta tabela foi criada
antes de qualquer cópia para `src/main/resources`.

| Arquivo-fonte | Propriedade | Finalidade detectada | Nome/destino final | Utilizado por | Estado |
| --- | --- | --- | --- | --- | --- |
| `Blocks/Rosalita_Door_Bottom.png` | PNG RGBA, 16×16, transparência | Parte de porta; nome indica inferior | candidato `textures/block/rosalita_door_bottom.png` | modelos inferiores da porta, após confirmar a dupla | aguardando confirmação da dupla |
| `Blocks/Rosalita_Door_Buttom.png` | PNG RGBA, 16×16, transparência | Segunda parte de porta; `Buttom` é provável erro de escrita | candidato `textures/block/rosalita_door_top.png` | modelos superiores da porta, após confirmar a dupla | aguardando confirmação da dupla |
| `Blocks/Rosalita_Leaves.png` | PNG RGBA, 16×16, transparência | Folhas Rosalita | `textures/block/rosalita_leaves.png` | `rosalita_leaves` | aplicado diretamente |
| `Blocks/Rosalita_Oak.png` | PNG RGBA, 16×16, opaco | Casca lateral do tronco/madeira | `textures/block/rosalita_log.png`, `textures/block/rosalita_wood.png` | tronco e madeira Rosalita | aplicado diretamente |
| `Blocks/Rosalita_Oak_Buttom.png` | PNG RGBA, 16×16, opaco | Extremidade de tronco; mesmo SHA-256 que `Rosalita_Oak_Top.png` | sem cópia necessária | referência duplicada preservada | não utilizado |
| `Blocks/Rosalita_Oak_Top.png` | PNG RGBA, 16×16, opaco | Extremidade de tronco | `textures/block/rosalita_log_top.png` | faces superior/inferior de `rosalita_log` | aplicado diretamente |
| `Blocks/Rosalita_Planks.png` | PNG RGBA, 16×16, opaco | Tábuas Rosalita | `textures/block/rosalita_planks.png` | tábuas, escadas, lajes, cerca, portão, botão e placa de pressão | aplicado diretamente |
| `Blocks/Rosalita_Sapling.png` | PNG RGBA, 16×16, transparência | Muda Rosalita | candidato `textures/block/rosalita_sapling.png` | exige registro do bloco/modelo de muda | ainda não utilizado: bloco não existe |
| `Blocks/Rosalita_Trapdoor.png` | PNG RGBA, 16×16, transparência | Alçapão Rosalita | `textures/block/rosalita_trapdoor.png` | três modelos de alçapão e item | aplicado diretamente |
| `entity/chest/Rosalita_Normal.png` | PNG RGBA, 64×64, transparência | Folha de textura do baú simples | candidato `textures/entity/chest/rosalita_normal.png` | renderer próprio de baú | ainda não utilizado: renderer não existe |
| `entity/chest/Rosalita_Normal_Left.png` | PNG RGBA, 64×64, transparência | Folha do lado esquerdo de baú duplo | candidato `textures/entity/chest/rosalita_normal_left.png` | renderer próprio de baú duplo | ainda não utilizado: renderer não existe |
| `entity/chest/Rosalita_Normal_Right.png` | PNG RGBA, 64×64, transparência | Folha do lado direito de baú duplo | candidato `textures/entity/chest/rosalita_normal_right.png` | renderer próprio de baú duplo | ainda não utilizado: renderer não existe |

## Mapeamentos aplicados

Os cinco assets sem ambiguidade foram copiados sem recoloração, redimensionamento
ou redesenho. O gerador de recursos também reaplica essas cópias literais quando
`.assets/New Assets` estiver presente, para impedir que uma futura regeneração
substitua os pixels do autor.

## Assets ainda necessários

- confirmação de qual arquivo de porta é a metade superior e qual é a inferior;
- variantes descascadas de tronco e madeira;
- assets de bancada, barril, placas, barco e itens;
- registro/modelo da muda Rosalita;
- renderer e block entity próprios para baú simples e duplo. As três folhas
  64×64 não podem ser aplicadas corretamente em um cubo JSON comum.
