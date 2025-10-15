# Code Execution and Editing

## Cloning the repository

To start, clone the project repository and navigate into it:

```bash
git clone https://github.com/codeurjc-students/2025-WrapItUp-Planner.git
cd 2025-WrapItUp-Planner
```

## Execution

### Database and services

* The application uses **MySQL** as its database.
* Example data is automatically initialized via `DatabaseInitializer.java` when the application starts.
* Connection credentials used by the application:

  * **User:** user
  * **Password:** password
  * **Database:** wrapitup
  * **Connection URL:** jdbc:mysql://localhost:3306/wrapitup
* MySQL can be started using a Docker container or a pre-configured local server.

### Running the backend (Spring Boot)

Navigate to the backend folder and run the following commands:

```bash
cd backend/wrapitup_planner
mvn clean install
mvn spring-boot:run
```

* The backend runs by default at: `http://localhost:8080`
* In future phases, the port will change to: `https://localhost:8443`

### Running the frontend (Angular)

Navigate to the frontend folder, install dependencies, and start the development server:

```bash
cd frontend/WrapItUp-Planner
npm install
ng serve
```

* The web application can be accessed at: `http://localhost:4200`

## Using development tools

* **VSCode** is recommended as the development environment due to its versatility and support for Angular and Java/Spring.
* **Postman** (or a similar tool) is used to interact with the server’s REST API. Postman collection is located at the docs folder

## Running tests

### Backend (Server) Tests

* **Unit Tests:** verify the functionality of services using database mocks.
* **Integration Tests:** test services by connecting to the real database.
* **REST API Tests:** validate that example data for the main entity is correctly retrieved using RestAssured.

### Frontend (Client) Tests

* **Unit Tests:** test component functionality using service mocks and a virtual DOM.
* **Integration Tests:** connect the frontend to the REST API to verify correct data display.
* **End-to-End (E2E) Tests:** ensure the user interface correctly displays the data using Karma and TestBed.

### Commands to run tests

```bash
# Frontend
cd frontend/WrapItUp-Planner
npm install
npm run test:all -- --no-watch --browsers=ChromeHeadless
```   

```bash
# Backend
cd backend/wrapitup_planner
mvn test -Dgroups="unit"
mvn test -Dgroups="integration"
mvn test -Dgroups="system"
mvn test -Dgroups="client-e2e"
```
