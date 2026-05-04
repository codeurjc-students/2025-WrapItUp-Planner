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
- `0.2` - Stable release version 0.2
- `1.0` - Stable release version 1.0

### Quick Deployment

To deploy the application pull and run:

To run the application, create a `.env` file in the same directory as the `docker-compose.yml` and define the required environment variables there. The file should include entries for the MySQL credentials, Spring datasource settings, and server SSL configuration using this format:

```env
MYSQL_ROOT_PASSWORD=<value>
MYSQL_DATABASE=<value>
MYSQL_USER=<value>
MYSQL_PASSWORD=<value>

SPRING_DATASOURCE_URL=<value>
SPRING_DATASOURCE_USERNAME=<value>
SPRING_DATASOURCE_PASSWORD=<value>
SPRING_JPA_HIBERNATE_DDL_AUTO=<value>
SERVER_PORT=<value>
SERVER_SSL_KEY_STORE_PASSWORD=<value>

OPENAI_API_KEY=<value>
```

```bash
docker pull arturox2500/wrapitup_planner:1.0
docker pull arturox2500/wrapitup_planner-compose:1.0
docker create --name temp-compose arturox2500/wrapitup_planner-compose:1.0 cmd.exe
docker cp temp-compose:/docker-compose.yml ./docker-compose.yml
docker rm temp-compose
docker compose up -d
```

Access the application at: `https://localhost:443`

### CI/CD Pipeline

The Docker images are automatically built and published using GitHub Actions:
- **Development builds:** Triggered on every push to `main`
- **Release builds:** Triggered when a new GitHub release is published
- All builds are pushed to DockerHub with appropriate version tags
