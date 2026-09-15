# Estágio 1: build da aplicação
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: imagem final, só com o necessário pra rodar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]