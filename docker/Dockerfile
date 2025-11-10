# Stage 1: build frontend
FROM node:18 AS node_builder
WORKDIR /app/frontend


COPY frontend/WrapItUp-Planner/package*.json ./
COPY frontend/WrapItUp-Planner/angular.json frontend/WrapItUp-Planner/tsconfig*.json ./
COPY frontend/WrapItUp-Planner/src ./src

RUN npm ci --silent
RUN npm run build -- --configuration production --output-path=dist/browser --base-href=/

# Stage 2: build backend (maven) and inject frontend build into resources/static
FROM maven:3.9.4-eclipse-temurin-21 AS maven_builder
WORKDIR /app/backend

# Copy backend sources
COPY backend/wrapitup_planner/pom.xml ./
COPY backend/wrapitup_planner/src ./src

# Copy the generated frontend build into backend resources/static
COPY --from=node_builder /app/frontend/dist/browser ./src/main/resources/static

# Package the app (skip tests for CI)
RUN mvn -B -DskipTests package

# Stage 3: runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=maven_builder /app/backend/target/*.jar app.jar

EXPOSE 443
ENTRYPOINT ["java","-jar","/app/app.jar"]