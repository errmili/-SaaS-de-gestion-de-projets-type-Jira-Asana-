# ===========================================
# Dockerfile - Auth Service (JAR pré-compilé)
# ===========================================

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier directement le JAR pré-compilé (ton JAR local)
COPY target/auth-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xmx512m -Xms256m"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]