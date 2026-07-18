# [Oficial] Chaotic Dimensions

Repositório-fonte oficial e multi-loader do mod **Chaotic Dimensions** para Minecraft Java Edition.

## Estrutura

| Loader | Versões publicadas |
| --- | --- |
| [Fabric](Fabric/) | [1.20.1](Fabric/1.20.1/) |
| [Forge](Forge/) | Em preparação |
| [NeoForge](NeoForge/) | Em preparação |
| [Quilt](Quilt/) | Em preparação |

Cada versão é mantida em sua própria pasta para que código, dependências e recursos de um loader não sejam misturados com os demais.

## Estado atual

O primeiro código publicado é a migração em andamento para **Fabric 1.20.1**. Os recursos legados foram organizados e os conteúdos seguintes serão implementados nativamente para Fabric.

### Idiomas

Todas as versões do mod devem disponibilizar: `pt_br`, `en_us`, `es_co` e `es_mx`.

## Desenvolvimento

Para trabalhar na versão Fabric 1.20.1, abra a pasta `Fabric/1.20.1` no Cursor/VS Code. O Gradle requer **JDK 21** para executar o Fabric Loom; o mod é compilado com alvo Java 17.

```bash
cd Fabric/1.20.1
./gradlew build
./gradlew runClient
```

Arquivos gerados, caches, backups locais e os JARs de referência não são publicados aqui.
