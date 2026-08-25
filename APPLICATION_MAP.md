# Mapeamento da aplicação Post Receiver

Este documento consolida o mapeamento da aplicação, descrevendo pacotes, classes principais, endpoints, configuração e fluxo de execução.

## Visão geral
- Nome do projeto: post-receiver
- Propósito: receber webhooks de publicações (posts) de sites WordPress fonte, sincronizar categorias/tags/mídia/metadados e criar/atualizar posts em um WordPress destino via REST API.
- Stack: Java + Spring Boot, cliente HTTP custom, suporte opcional a leitura/gravação no schema MySQL do WordPress.

## Fluxo principal (resumido)
1. O WordPress fonte envia um webhook para um dos endpoints expostos pela aplicação.
2. O `WebhookController` valida o payload e invoca `PostReplicationService`.
3. `PostReplicationService` orquestra:
   - sincronização de termos (categorias/tags) via `CategorySyncService` / `TagSyncService`;
   - download e upload de mídia via `ImageDownloader` e `MediaSyncService`;
   - criação/atualização de post no WordPress destino via `PostSyncService` e `WordPressApiClient`;
   - persistência/atualização de metas no WordPress (opcional) via `MetaSyncService` e `WordPressMetaRepository`.
4. Resposta é retornada ao chamador (ex.: SyncResult com created/updated/skipped).

## Pacotes e responsabilidades

`com.post.receiver`
- `PostReceiverApplication.java` — entrada Spring Boot da aplicação.

`controller`
- `WebhookController.java` — endpoints HTTP para receber webhooks (ex.: `/api/webhook/acordadf` e `/api/webhook/dfmobilidade`). Recebe payload e dispara processo de replicação.
- `HealthController.java` — endpoint de health (`/api/health`).

`client`
- `WordPressApiClient.java` — cliente HTTP responsável por chamadas à REST API do WordPress destino: CRUD de posts, termos e upload de mídia. Implementa autenticação (application password) e mapeamento de respostas/erros.
- `ImageDownloader.java` — baixa imagens externas para upload posterior (determina filename, content-type e bytes).

`config`
- `HttpClientConfig.java` — configuração dos beans HTTP (RestClient/RestTemplate) usados pelo cliente WordPress e download de imagens; timeouts controlados por propriedades.
- `WordPressMySqlConfig.java` — configura `DataSource` (Hikari) e `JdbcTemplate` quando `wordpress.mysql.enabled=true`.
- `WordPressProperties.java` — propriedades custom (prefixo `wordpress`) contendo sub-blocos: `destination`, `mysql`, `meta` e timeouts.

`service`
- `PostReplicationService.java` — orquestrador principal da sincronização; coordena demais serviços e aplica regras (ignorar rascunhos, reusar posts por slug ou meta, etc.).
- `PostSyncService.java` — trata criação/atualização de posts no WordPress destino (montagem do `WpPostRequest`).
- `CategorySyncService.java` / `TagSyncService.java` — garantem que termos fontes existam no destino (busca por slug e cria se necessário).
- `MediaSyncService.java` — faz download da imagem destacada e realiza upload no WP destino (evita duplicar mídia se já existir meta).
- `MetaSyncService.java` — controla quais metas serão persistidas/no WordPress destino; faz upsert de metadados quando `wordpress.mysql.enabled=true`.

`repository`
- `WordPressMetaRepository.java` — acessa o schema do WordPress via JDBC para localizar posts por meta (_source_post_id/_source_site) e inserir/atualizar postmeta. Usado somente quando `wordpress.mysql.enabled=true`.

`dto`
- `SyncResult.java` — DTO de retorno do endpoint de webhook.
- `dto.webhook` — classes que representam o payload recebido (post fonte, terms, featured image, etc.).
- `dto.wordpress` — DTOs para requests/responses do WordPress (WpPostRequest, WpPostResponse, WpMediaResponse, WpTermRequest/Response).
- `dto.client.DownloadedImage` — represents bytes, filename e content-type de uma imagem baixada.

`domain`
- `SourceSite.java` — enum/classe representando sites fonte (ex.: ACORDA_DF, DF_MOBILIDADE). Usado para meta `_source_site` e identificação do site origem.

`exception`
- `WordPressSyncException.java` — exceção custom para erros ao comunicar com o WP destino.
- `InvalidWebhookException.java` — payload inválido / erro de validação (mapeado para 400).
- `GlobalExceptionHandler.java` — handler que centraliza a resposta de erros HTTP.

`util`
- `WordPressDates.java` — utilitários para conversão/normalização de datas ao montar payloads para o WP.

## Arquivos de configuração e recursos
- `src/main/resources/application.yml` — propriedades da aplicação. Chaves importantes (exemplos):
  - `wordpress.destination.baseUrl` — URL base do WordPress destino.
  - `wordpress.destination.username` / `password` — credenciais (application password) para autenticação Basic.
  - `wordpress.mysql.enabled` — booleano: habilita uso do `WordPressMetaRepository`.
  - `wordpress.meta.allowed` / `ignored` / `prefixes` — controla quais metadados serão persistidos.
  - timeouts do cliente HTTP.
- `src/main/resources/logback-spring.xml` — configuração de logging.

## Endpoints HTTP (visão geral)
- `GET /api/health` — health check.
- `POST /api/webhook/acordadf` — webhook para o site Acorda DF (exemplo).
- `POST /api/webhook/dfmobilidade` — webhook para o site DF Mobilidade (exemplo).

Cada endpoint de webhook retorna um `SyncResult` informando o resultado (created, updated ou skipped) e pode retornar erro estruturado quando há falha.

## Regras e comportamentos importantes
- Identificação de post destino:
  - Preferencialmente por meta `_source_post_id` + `_source_site` quando `wordpress.mysql.enabled=true` (consulta via `WordPressMetaRepository`).
  - Caso contrário, busca por slug no WordPress destino (menos confiável).
- Sincronização de termos: quando um termo (category/tag) não existe no destino, o serviço o cria e mapeia ids fonte→destino.
- Mídia: imagem destacada é baixada pelo `ImageDownloader` e enviada ao WP destino; o serviço tenta evitar duplicação consultando metas ou resposta do WP.
- Metadados: somente as metas permitidas em `wordpress.meta.allowed` são gravadas; há regras de prefixos e metadados ignorados.

## Build e execução
- Build com Maven:

  mvn clean package

- Executar localmente (exemplo):

  mvn spring-boot:run

- Docker:
  - `docker build -t post-receiver:latest .`
  - `docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod post-receiver:latest`
  - Ou `docker-compose up -d` caso queira orquestrar com MySQL (se configurado no `docker-compose.yml`).

## Onde alterar credenciais / WP destino
- Editar `src/main/resources/application.yml` ou usar variáveis de ambiente para sobrescrever as propriedades: `wordpress.destination.baseUrl`, `wordpress.destination.username`, `wordpress.destination.password`.

## Logs e troubleshooting
- Logs principais vão para `logs/post-receiver.log` (conforme configuração). Em caso de erro de API, verifique as mensagens lançadas por `WordPressApiClient` e as exceções `WordPressSyncException`.

## Observações finais
- O repositório SQL (`WordPressMetaRepository`) e as operações em `mysql` são opcionais e controladas por `wordpress.mysql.enabled` — manter `false` evita dependência direta do schema do WP.
- Para auditoria ou debug pesado, habilite logs em nível DEBUG para os pacotes `com.post.receiver`.

---

Arquivo gerado automaticamente por consolidação de documentação existente. Para detalhes de implementação, ver os arquivos fonte em `src/main/java/com/post/receiver`.

