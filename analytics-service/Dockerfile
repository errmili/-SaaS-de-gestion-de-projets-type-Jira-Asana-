FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
# Variables d'environnement
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8080

# Configuration base de données
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/movie
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=root
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# Configuration Eureka - ACTIVÉ pour les tests
ENV EUREKA_CLIENT_ENABLED=true
ENV EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://discovery-server:8761/eureka

# Désactiver config server (pas nécessaire pour ce test)
ENV SPRING_CLOUD_CONFIG_ENABLED=false
ENV SPRING_CONFIG_IMPORT=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


## ============================
## Étape de construction
## ============================
#FROM maven:3.8.7-openjdk-18 AS build
#
## Définir le répertoire de travail
#WORKDIR /build
#
## Copier le fichier pom.xml et télécharger les dépendances
#COPY pom.xml .
#RUN mvn dependency:go-offline
#
## Copier le code source et construire l'application
#COPY src ./src
#RUN mvn clean package -DskipTests
#
## ============================
## Étape d'exécution (Runtime)
## ============================
#FROM amazoncorretto:17
#
## Définir le répertoire de travail pour l'exécution de l'application
#WORKDIR /app
#
## Copier le fichier JAR généré par l'étape de build dans le conteneur
#COPY target/movies-0.0.1-SNAPSHOT.jar /app/app.jar
#
## Exposer le port 8088
#EXPOSE 8088
#
## Définir la variable d'environnement pour la base de données
#ENV DB_URL=jdbc:postgresql://postgres-sql-bsn:5432/movies
#
## Commande de démarrage de l'application avec la configuration de la base de données
#CMD ["java", "-jar", "-Dspring.datasource.url=${DB_URL}", "/app/app.jar"]























#si t vx travailler avec racine
#FROM eclipse-temurin:17-jre-alpine
#WORKDIR /app
#
## Créer le dossier de rapports
#RUN mkdir -p /app/reports
#
#COPY target/analytics-service-1.0.0-SNAPSHOT.jar app.jar
#EXPOSE 8085
#ENV JAVA_OPTS="-Xmx1024m -Xms512m"
#CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]