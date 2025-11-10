# Stage 1: build frontend
FROM node:18 AS node_builder
WORKDIR /app/frontend

# Copia ficheros necesarios del proyecto Angular
COPY frontend/WrapItUp-Planner/package*.json ./
COPY frontend/WrapItUp-Planner/angular.json frontend/WrapItUp-Planner/tsconfig.json frontend/WrapItUp-Planner/tsconfig.app.json ./ 
COPY frontend/WrapItUp-Planner/ ./

RUN npm ci
RUN npm run build -- --configuration production --base-href=/


# Stage 2: build backend (maven) and inject frontend build into resources/static
FROM maven:3.9.4-eclipse-temurin-21 AS maven_builder
WORKDIR /app/backend

# Copy backend sources
COPY backend/wrapitup_planner/pom.xml ./
COPY backend/wrapitup_planner/src ./src
# Copy the generated frontend build into backend resources
COPY --from=node_builder /app/frontend/dist/wrap-it-up-planner/browser ./src/main/resources/static


# Package the app (skip tests for faster builds in CI; remove -DskipTests for strict builds)
RUN mvn -B -DskipTests package

# Stage 3: runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the packaged jar
COPY --from=maven_builder /app/backend/target/*.jar app.jar

EXPOSE 443
ENTRYPOINT ["java","-jar","/app.jar"]