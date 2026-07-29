# Sincronização dos dois repositórios

Este projeto possui dois destinos Git que devem ser atualizados ao final de
cada alteração concluída e validada.

| Destino | Diretório local | Conteúdo |
| --- | --- | --- |
| Projeto completo | `Chaotic_Dimensions` | Código-fonte, recursos do mod, assets autorais, Gradle, testes e documentação. Remote: `hadryphern/ChaoticDimensionsMod-1.20.1`. |
| Repositório organizado | `../ChaoticDimensions/Fabric/1.20.1` | Instantâneo curado e compilável da versão Fabric 1.20.1. Remote: `hadryphern/ChaoticDimensions`. |

## Regras de escopo

Os dois repositórios recebem arquivos de projeto, nunca estado de execução:

- incluir: `src/`, `docs/`, `gradle/`, wrapper Gradle, arquivos de build,
  traduções, modelos, texturas autorais e testes;
- excluir: `build/`, `.gradle/`, `run/`, `logs/`, `bin/`, configurações locais
  da IDE, caches e mundos de teste;
- não publicar novamente assets vanilla do Minecraft sem uma decisão explícita
  de licença. Eles são apenas referências locais e não são necessários para
  compilar o mod.

O repositório organizado espelha somente a árvore Fabric 1.20.1 e remove
arquivos obsoletos daquela versão durante a sincronização. Forge, NeoForge e
Quilt mantêm somente suas pastas/README até receberem implementações próprias.

## Ordem obrigatória

1. Executar `./gradlew check remapJar --no-daemon --console=plain` no projeto
   completo.
2. Fazer commit e push do projeto completo.
3. Atualizar `../ChaoticDimensions/Fabric/1.20.1` com a árvore curada.
4. Verificar o status, fazer commit e push do repositório organizado.

Se algum push falhar, não considerar a entrega concluída: informar claramente
qual dos dois destinos ficou pendente.
