# si t vx travailler avec la racine

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/notification-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENV JAVA_OPTS="-Xmx512m -Xms256m"
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]




# ===========================================
# Dockerfile - Analytics Service
# ===========================================

# Stage 1: Build
#FROM openjdk:17-jdk-slim as builder
#
#WORKDIR /app
#
## Copy Maven wrapper and pom.xml first (for better layer caching)
#COPY mvnw .
#COPY .mvn .mvn
#COPY pom.xml .
#
## Download dependencies (cached if pom.xml doesn't change)
#RUN ./mvnw dependency:go-offline -B
#
## Copy source code
#COPY src src
#
## Build the application
#RUN ./mvnw clean package -DskipTests -B
#
## Stage 2: Runtime
#FROM openjdk:17-jre-slim
#
## Create non-root user for security
#RUN groupadd -r appuser && useradd -r -g appuser appuser
#
## Install required packages
#RUN apt-get update && apt-get install -y \
#    curl \
#    && rm -rf /var/lib/apt/lists/*
#
#WORKDIR /app
#
## Create directory for reports
#RUN mkdir -p /app/reports && \
#    chown -R appuser:appuser /app/reports
#
## Copy JAR from build stage
#COPY --from=builder /app/target/analytics-service-0.0.1-SNAPSHOT.jar app.jar
#
## Change ownership to non-root user
#RUN chown -R appuser:appuser /app
#USER appuser
#
## Create volume for reports storage
#VOLUME ["/app/reports"]
#
## Expose port
#EXPOSE 8085
#
## Health check
#HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
#  CMD curl -f http://localhost:8085/actuator/health || exit 1
#
## Set JVM options for containerized environment (higher memory for analytics processing)
#ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"
#
## Run the application
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
#