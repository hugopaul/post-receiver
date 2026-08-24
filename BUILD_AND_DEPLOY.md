# Build e Deploy

Guia completo para compilar, testar e fazer deploy da aplicação Post Receiver.

## 1. Build Local

### Pré-requisitos
- Java 25+ instalado
- Maven 3.6+ instalado

### Compilação

```bash
# Clonar o repositório
git clone <repositório>
cd post-receiver

# Build com Maven
mvn clean package

# Resultado
# O arquivo JAR será gerado em: target/post-receiver-1.0.0.jar
```

### Verificar Build
```bash
mvn test  # Se testes estiverem implementados
```

## 2. Executar Localmente

### Via Maven
```bash
mvn spring-boot:run
```

### Via JAR
```bash
java -jar target/post-receiver-1.0.0.jar
```

### Com Variáveis de Ambiente
```bash
JAVA_OPTS="-Xmx512m -Xms256m" java -jar target/post-receiver-1.0.0.jar
```

## 3. Build Docker

### Build da Imagem

```bash
# Build padrão
docker build -t post-receiver:latest .

# Build com tag específica
docker build -t post-receiver:1.0.0 .

# Build com múltiplas tags
docker build -t post-receiver:latest -t post-receiver:1.0.0 .
```

### Verificar Imagem
```bash
docker images | grep post-receiver
```

### Tamanho da Imagem
A imagem com multi-stage build deve ter aproximadamente 400-500MB (sem otimizações adicionais).

## 4. Executar Contêiner

### Comando Básico
```bash
docker run -p 8080:8080 post-receiver:latest
```

### Com Variáveis de Ambiente
```bash
docker run -p 8080:8080 \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  post-receiver:latest
```

### Com Volume para Logs
```bash
docker run -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  post-receiver:latest
```

### Background
```bash
docker run -d \
  --name post-receiver \
  -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  post-receiver:latest

# Ver status
docker ps | grep post-receiver

# Ver logs
docker logs post-receiver

# Parar
docker stop post-receiver

# Remover
docker rm post-receiver
```

## 5. Docker Compose

### Iniciar
```bash
docker-compose up -d
```

### Ver Status
```bash
docker-compose ps
```

### Ver Logs
```bash
docker-compose logs -f post-receiver
```

### Parar
```bash
docker-compose down
```

## 6. Otimizações da Imagem Docker

### Reduza Ainda Mais o Tamanho

#### Usando JRE Slim
```dockerfile
FROM eclipse-temurin:25-jre-jammy-slim
```

#### Remover Componentes Desnecessários
```dockerfile
RUN apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false
```

#### Usar Alpine (Não recomendado para Java 25)
Alpine possui problemas com Java 25, use apenas com versões LTS mais antigas.

### Tamanho Esperado
- Base image (jre-jammy): ~200MB
- Aplicação Spring Boot: ~60MB
- Total: ~260-300MB

## 7. Health Check

### Teste Manual
```bash
curl http://localhost:8080/api/health
```

### Docker Health Check
O Dockerfile já contém health check automático.

Visualizar status:
```bash
docker ps --no-trunc | grep post-receiver
```

## 8. Deploy em Produção

### Variáveis de Ambiente Recomendadas
```bash
export JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Network
```bash
# Criar rede customizada
docker network create post-receiver-network

# Usar na imagem
docker run --network post-receiver-network ...
```

### Reverse Proxy (Nginx)
```nginx
upstream post_receiver {
    server post-receiver:8080;
}

server {
    listen 80;
    server_name api.exemplo.com;

    location / {
        proxy_pass http://post_receiver;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 9. Monitoramento

### Ver Logs em Tempo Real
```bash
docker logs -f post-receiver --tail 50
```

### Executar Comando no Contêiner
```bash
docker exec -it post-receiver bash
```

### Verificar Recursos
```bash
docker stats post-receiver
```

## 10. CI/CD (GitHub Actions Exemplo)

```yaml
name: Build and Deploy

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 25
        uses: actions/setup-java@v2
        with:
          java-version: '25'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: mvn clean package
      
      - name: Build Docker Image
        run: docker build -t post-receiver:${{ github.sha }} .
      
      - name: Push to Registry
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker push post-receiver:${{ github.sha }}
```

## 11. Troubleshooting

### Aplicação não inicia
```bash
# Ver logs completos
docker logs post-receiver

# Verificar argumentos JVM
docker exec post-receiver jps -lm
```

### Porta já está em uso
```bash
# Linux/Mac
lsof -i :8080

# Windows
netstat -ano | findstr :8080
```

### Out of Memory
Aumentar heap:
```bash
JAVA_OPTS="-Xmx2048m -Xms1024m"
```

## 12. Rollback

```bash
# Se houver problema, voltar para versão anterior
docker run -d -p 8080:8080 post-receiver:anterior-version

# Remover imagem problemática
docker rmi post-receiver:latest
docker tag post-receiver:anterior-version post-receiver:latest
```

