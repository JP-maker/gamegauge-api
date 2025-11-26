# Étape 1 : Build (Compilation)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Run (Exécution légère)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# On récupère uniquement le jar compilé de l'étape précédente
COPY --from=build /app/target/*.jar app.jar

# On crée un utilisateur non-root pour la sécurité
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]