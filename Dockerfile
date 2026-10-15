# O pipeline executa os testes antes de construir a imagem.
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl \
    && addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /workspace/target/grupo34_atdd-*.jar /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
