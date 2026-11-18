FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
ENV VITE_API_BASE_URL=/api
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /app/frontend/dist ./src/main/resources/static
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/target/*-SNAPSHOT.jar app.jar
ENV PORT=80 APP_UPLOADS_STORAGE=database \
    SPRING_MAIN_LAZY_INITIALIZATION=true \
    SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=0 \
    SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=3 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:TieredStopAtLevel=1"
EXPOSE 80
ENTRYPOINT ["java", "-jar", "app.jar"]
