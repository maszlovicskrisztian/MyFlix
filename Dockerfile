FROM node:24.18.0-alpine AS web
WORKDIR /web
COPY myflix-frontend/package*.json ./
RUN npm ci
COPY myflix-frontend/ .
RUN npm run build

FROM maven:3.9-eclipse-temurin-25 AS api
WORKDIR /api
COPY myflix-core/pom.xml .
RUN mvn -q dependency:go-offline
COPY myflix-core/src ./src
COPY --from=web /web/dist/*/browser/ ./src/main/resources/static/
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:25-jre
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    intel-media-va-driver-non-free \
    mesa-va-drivers \
    vainfo \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=api /api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]