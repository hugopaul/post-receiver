# Configuração de Webhooks no WordPress

## Instruções para Configurar Webhooks

Este documento descreve como configurar os webhooks do WordPress para enviar posts para a aplicação Post Receiver.

## 1. Usando Plugin REST API ou Advanced Webhooks

### Opção 1: WordPress REST API (Recomendado)

A forma mais simples é usar o WordPress REST API que já vem integrado. Você pode usar ferramentas como:

- **Zapier** - Integração automática
- **IFTTT** - If This Then That
- **Webhooks do WordPress** - Usando um plugin gratuito

### Opção 2: Plugin "Webhooks"

1. Instale um plugin de webhooks como "Easy Post Notifications"
2. Configure o webhook para enviar para:
   - URL: `http://seu-dominio.com/api/webhook/acordadf` (para Acorda DF)
   - URL: `http://seu-dominio.com/api/webhook/dfmobilidade` (para DF Mobilidade)
   - Método: POST
   - Content-Type: application/json

## 2. Payload Esperado

A aplicação espera receber um JSON com a seguinte estrutura:

```json
{
  "id": 123,
  "title": "Título do Post",
  "content": "<p>Conteúdo HTML do post...</p>",
  "excerpt": "Resumo ou excerpt do post",
  "author": "Nome do Autor",
  "date": "2024-08-22T10:30:00",
  "status": "publish",
  "slug": "titulo-do-post",
  "type": "post",
  "link": "https://acordadf.com.br/titulo-do-post"
}
```

### Campo Obrigatórios:
- `id`: ID único do post
- `title`: Título do post
- `date`: Data de publicação

### Campos Opcionais:
- `content`: Conteúdo completo
- `excerpt`: Resumo do post
- `author`: Nome ou ID do autor
- `status`: Status do post (publish, draft, etc)
- `slug`: Slug amigável do post
- `type`: Tipo de conteúdo (post, page, etc)
- `link`: URL permanente do post

## 3. Usando REST API com cURL

Para testar de forma manual:

```bash
# Webhook Acorda DF
curl -X POST http://seu-dominio.com/api/webhook/acordadf \
  -H "Content-Type: application/json" \
  -d '{
    "id": 123,
    "title": "Título do Post",
    "content": "Conteúdo do post",
    "excerpt": "Resumo",
    "author": "Autor",
    "date": "2024-08-22T10:30:00",
    "status": "publish",
    "slug": "titulo-do-post",
    "type": "post",
    "link": "https://acordadf.com.br/titulo-do-post"
  }'

# Webhook DF Mobilidade
curl -X POST http://seu-dominio.com/api/webhook/dfmobilidade \
  -H "Content-Type: application/json" \
  -d '{
    "id": 124,
    "title": "Notícia de Transporte",
    "content": "Conteúdo sobre mobilidade",
    "excerpt": "Resumo",
    "author": "Redação",
    "date": "2024-08-22T11:00:00",
    "status": "publish",
    "slug": "noticia-transporte",
    "type": "post",
    "link": "https://dfmobilidade.com.br/noticia-transporte"
  }'
```

## 4. Usando WordPress REST API com Autenticação

Se você precisar de autenticação, pode usar JWT ou Basic Auth:

```php
// Exemplo em PHP para chamar o webhook
$post_data = array(
    'id' => get_the_ID(),
    'title' => get_the_title(),
    'content' => get_the_content(),
    'excerpt' => get_the_excerpt(),
    'author' => get_the_author(),
    'date' => get_the_date('c'),
    'status' => get_post_status(),
    'slug' => get_post_field('post_name'),
    'type' => get_post_type(),
    'link' => get_permalink()
);

$response = wp_remote_post('http://seu-dominio.com/api/webhook/acordadf', array(
    'method'    => 'POST',
    'headers'   => array('Content-Type' => 'application/json'),
    'body'      => json_encode($post_data),
    'timeout'   => 30
));
```

## 5. Troubleshooting

### Verificar Logs
Os logs da aplicação mostrarão todos os posts recebidos:

```
GET http://seu-dominio.com/api/health
```

Se retornar 200 OK, a aplicação está funcionando.

### Verificar Endpoint
Use o script de teste incluído:

```bash
# Linux/Mac
bash test-endpoints.sh

# Windows
test-endpoints.bat
```

## 6. Próximas Etapas

- Implementar validação de assinatura/token
- Adicionar tratamento de erros específicos
- Implementar retry logic
- Persistir os dados recebidos no banco de dados

