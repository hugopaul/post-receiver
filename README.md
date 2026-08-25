# Post Receiver

Aplicação Spring Boot para receber webhooks de posts do WordPress de múltiplos sites.

## Requisitos

- Java 25+
- Maven 3.6+
- Docker (opcional)

## Configuração

### Build Local

```bash
mvn clean install
```

### Executar Localmente

```bash
mvn spring-boot:run
```

A aplicação iniciará na porta `8080`.

## Endpoints

### Health Check

```
GET http://localhost:8080/api/health
```

Retorna o status da aplicação.

### Webhook Acorda DF

```
POST http://localhost:8080/api/webhook/acordadf
Content-Type: application/json

{
  "id": 1,
  "title": "Título do Post",
  "content": "Conteúdo do post...",
  "excerpt": "Resumo do post...",
  "author": "Nome do Autor",
  "date": "2024-08-22T10:30:00",
  "status": "publish",
  "slug": "titulo-do-post",
  "type": "post",
  "link": "https://acordadf.com.br/titulo-do-post"
}
```

Retorna: `Post do Acorda DF recebido com sucesso`

### Webhook DF Mobilidade

```
POST http://localhost:8080/api/webhook/dfmobilidade
Content-Type: application/json

{
  "id": 2,
  "title": "Título do Post",
  "content": "Conteúdo do post...",
  "excerpt": "Resumo do post...",
  "author": "Nome do Autor",
  "date": "2024-08-22T10:30:00",
  "status": "publish",
  "slug": "titulo-do-post",
  "type": "post",
  "link": "https://dfmobilidade.com.br/titulo-do-post"
}
```

Retorna: `Post do DF Mobilidade recebido com sucesso`

## Logging

Os logs da aplicação são exibidos no console e salvos em `logs/post-receiver.log`.

Todas as informações dos posts recebidos são registradas nos logs da aplicação.

## Docker

### Build da Imagem Docker

```bash
docker build -t post-receiver:latest .
```

### Executar Contêiner

```bash
docker run -p 8080:8080 post-receiver:latest
```

### Usando Docker Compose

```bash
docker-compose up -d
```

## Estrutura do Projeto

```
post-receiver/
├── src/
│   ├── main/
│   │   ├── java/com/post/receiver/
│   │   │   ├── PostReceiverApplication.java      # Main Application
│   │   │   ├── controller/
│   │   │   │   ├── WebhookController.java        # Endpoints dos webhooks
│   │   │   │   └── HealthController.java         # Health check
│   │   │   └── dto/
│   │   │       └── WordPressPostDTO.java         # DTO para posts
│   │   └── resources/
│   │       └── application.yml                   # Configuração
│   └── test/                                     # Testes (não inclusos)
├── pom.xml                                       # Dependências Maven
├── Dockerfile                                    # Build da imagem Docker
├── docker-compose.yml                            # Compose para desenvolvimento
└── README.md                                     # Este arquivo
```

## Próximas Etapas

- Integração com banco de dados
- Persistência dos posts recebidos
- Validação e processamento adicional dos dados
- Adição de testes unitários
- Implementação de retry logic
- Adição de autenticação/autorização

## Versões

- Spring Boot: 3.3.3 (LTS)
- Java: 25
- Maven: 3.6+
