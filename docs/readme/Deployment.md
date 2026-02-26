# Deployment

## Packaging and Distribution

The application is packaged and distributed using **Docker** for consistent deployment across different environments.

### Docker Image

A single **multi-stage Docker image** contains both the Angular frontend and Spring Boot backend, built using the `Dockerfile` in the `docker/` directory.

### Docker Compose

**Docker Compose** coordinates all the aplication's components (frontend, backend, and MySQL database) with configured networking, environment variables, and port mapping (443 for HTTPS).

### Distribution

The application is publicly available through **DockerHub**:

**Repository:** [arturox2500/wrapitup_planner](https://hub.docker.com/r/arturox2500/wrapitup_planner)

**Available tags:**
- `latest` - Most recent development build
- `dev` - Development version (auto-deployed from `main` branch)
- `0.1` - Stable release version 0.1

### Quick Deployment

To deploy the application pull and run:

```bash
docker pull arturox2500/wrapitup_planner:0.1
docker pull arturox2500/wrapitup_planner-compose:0.1
docker create --name temp-compose arturox2500/wrapitup_planner-compose:0.1 cmd.exe
docker cp temp-compose:/docker-compose.yml ./docker-compose.yml
docker cp temp-compose:/.env ./.env
docker rm temp-compose
docker compose up -d
```

Access the application at: `https://localhost:443`

### CI/CD Pipeline

The Docker images are automatically built and published using GitHub Actions:
- **Development builds:** Triggered on every push to `main`
- **Release builds:** Triggered when a new GitHub release is published
- All builds are pushed to DockerHub with appropriate version tags
