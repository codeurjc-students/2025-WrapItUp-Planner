# Stage 1: build frontend
FROM node:18 AS node_builder
WORKDIR /app/frontend

# Copia ficheros necesarios del proyecto Angular
COPY frontend/WrapItUp-Planner/package.json frontend/WrapItUp-Planner/package-lock.json* ./
COPY frontend/WrapItUp-Planner/angular.json frontend/WrapItUp-Planner/tsconfig.json frontend/WrapItUp-Planner/tsconfig.app.json ./ 
COPY frontend/WrapItUp-Planner/src ./src

RUN npm ci
RUN npm run build --silent --base-href=/

# Stage 2: build backend (maven) and inject frontend build into resources/static
FROM maven:3.9.4-eclipse-temurin-21 AS maven_builder
WORKDIR /app/backend

# Copy backend sources
COPY backend/wrapitup_planner/pom.xml ./pom.xml
COPY backend/wrapitup_planner/src ./src
# Copy the generated frontend build into backend resources
COPY --from=node_builder /app/frontend/dist/wrap-it-up-planner ./src/main/resources/static


# Package the app (skip tests for faster builds in CI; remove -DskipTests for strict builds)
RUN mvn -B -DskipTests package

# Stage 3: runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Recommended environment variables (can be overridden via docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod \
    SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/wrapitup \
    SPRING_DATASOURCE_USERNAME=user \
    SPRING_DATASOURCE_PASSWORD=password

# Copy the packaged jar
COPY --from=maven_builder /app/backend/target/*.jar app.jar

EXPOSE 443
ENTRYPOINT ["java","-jar","/app/app.jar"]