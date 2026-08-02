# Orespawn Test — infraestrutura local removível

Esta pasta documenta uma infraestrutura de estudo para um JAR legado do
OreSpawn 1.7.10. Ela **não é um port do OreSpawn** e não inclui, empacota ou
publica código, imagens, modelos, sons, dados, traduções ou outros arquivos
do mod legado.

O material extraído para inspeção fica somente em `.assets/Orespawn/`, que já
é ignorada pelo Git. A implementação pública contém apenas código original do
Chaotic Dimensions para verificar registro, aba criativa, comandos e a divisão
cliente/servidor de forma segura.

## Estado atual

- Conteúdo legado convertido: **0** itens, blocos, entidades, bosses, sons ou
  dimensões.
- Conteúdo de teste original: 1 marcador inerte, 1 spawn egg e 1 proxy baseado
  no porco vanilla.
- Assets legados carregados em runtime: **0**.
- Áudio legado carregado em runtime: **0**.
- Classes legadas carregadas em runtime: **0**.

O proxy não representa, imita ou reutiliza uma entidade do OreSpawn. Ele usa
intencionalmente comportamento, modelo, textura e sons do `Pig` vanilla para
testar o caminho completo de uma entidade Fabric sem colocar material legado
no projeto.

## Ativação local

Na primeira inicialização, o mod cria:

```text
config/chaoticd-orespawn-test.properties
```

O valor seguro padrão é:

```properties
enableOrespawnTestContent=false
```

Para habilitar a área de teste em um mundo de desenvolvimento descartável,
altere o valor para `true` e reinicie o cliente ou servidor. Em ambiente
cliente-servidor, use a mesma configuração nos dois lados.

Enquanto a opção está `false`, não há entradas na aba Orespawn e as ações de
teste ficam bloqueadas. Os IDs inertes continuam registrados em ambos os lados
por segurança de rede: omitir registries conforme uma configuração local pode
fazer cliente e servidor divergirem antes de uma conexão. Isso é uma limitação
documentada de registries estáticos do Fabric, não conteúdo legado ativo.

## Itens e aba criativa

ID da aba: `chaoticd:orespawn`

Ordem quando habilitada:

1. `chaoticd:orespawn_test_reference_marker` — item original, inerte e com
   modelo que aponta conscientemente para o ícone vanilla de barreira.
2. `chaoticd:orespawn_test_reference_proxy_spawn_egg` — spawn egg original da
   entidade proxy.

Não há textura, áudio, modelo ou classe do OreSpawn em `src/main/resources` ou
`src/main/java`.

## Comandos de desenvolvimento

Todos exigem nível de permissão 2.

```mcfunction
/orespawntest status
/orespawntest list items
/orespawntest list entities
/orespawntest list bosses
/orespawntest list blocked
/orespawntest give reference_marker
/orespawntest summon reference_proxy
/orespawntest validate
```

`status` e `validate` são diagnósticos seguros. As ações de listar, entregar e
invocar requerem `enableOrespawnTestContent=true`.

## Remoção limpa

Para remover a área experimental, remova em conjunto:

- `src/main/java/net/blue/chaoticd/test/orespawn/`;
- as chamadas de inicialização em `ChaoticDimensions` e
  `ChaoticDimensionsClient`;
- os dois modelos `orespawn_test_*`;
- as chaves `orespawn` dos quatro arquivos de idioma;
- as tarefas/validadores `validateOrespawnTest` e
  `validatePlaceholderCatalog`;
- esta pasta de documentação;
- opcionalmente, `.assets/Orespawn/` local.

Não há dependência do conteúdo principal nessas classes. Execute `./gradlew
clean build` depois da remoção para confirmar que não restou bytecode antigo.

## Limites legais e de distribuição

O JAR analisado não apresentou uma licença, `NOTICE`, `COPYING` ou autorização
de redistribuição verificável. Portanto, nenhuma conversão de conteúdo do JAR
deve ser publicada sem permissão explícita do titular. Consulte o relatório
sanitizado em [ORESPAWN_REFERENCE_REPORT_2026-07-29.md](ORESPAWN_REFERENCE_REPORT_2026-07-29.md).
