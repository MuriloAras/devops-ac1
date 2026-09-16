# Etapa 1: Build da aplicação Spring Boot
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia pom.xml e código-fonte
COPY pom.xml .
COPY src ./src

# Compila o JAR do projeto
RUN mvn clean package -DskipTests

# Etapa 2: Execução com JRE otimizado e leve
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia apenas o artefato gerado
COPY --from=build /app/target/*.jar app.jar

# Porta padrão do Spring Boot
EXPOSE 8080

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]
