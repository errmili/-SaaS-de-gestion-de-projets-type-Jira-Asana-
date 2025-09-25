FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Créer le dossier de stockage
RUN mkdir -p /app/temp-storage

# CORRECTION : Utiliser le bon nom de JAR
COPY target/file-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENV JAVA_OPTS="-Xmx1024m -Xms512m"
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]