# Multi-stage build para otimizar o tamanho da imagem
# Stage 1: Build
FROM eclipse-temurin:25-jdk-jammy as builder

WORKDIR /build

# Copiar arquivos de projeto
COPY pom.xml .
COPY src ./src

# Install Maven e compilar o projeto
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN mvn clean package -DskipTests

# Stage 2: Runtime (imagem leve)
FROM eclipse-temurin:25-jre-jammy

# Metadados
LABEL maintainer="Post Receiver Team"
LABEL description="Spring Boot application to receive WordPress posts"

# Criando diretório de trabalho
WORKDIR /app

# Copiando o JAR compilado do stage anterior
COPY --from=builder /build/target/*.jar app.jar

# Criar usuário não-root para segurança
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

