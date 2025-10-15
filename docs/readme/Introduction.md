# Introduction

**Wrap-It-Up Planner** is a Single Page Application (SPA) built with **Angular** on the frontend and **Spring Boot** on the backend within a Maven project. An SPA is a type of web application that loads a single page, and all user interactions occur within it. The main goal is to avoid full page reloads, providing a smoother and snappier user experience.

The system is composed of three main components:

1. **Client:** The web interface developed in Angular, responsible for presenting content and handling user interactions.  
2. **Server:** The backend developed in Spring Boot, responsible for business logic and exposing the REST API, whose documentation is automatically generated using OpenAPI.  
3. **Database:** A SQL database connected to the backend via MySQL Connector, used to store all application data.  

In later stages of the project, **artificial intelligence** will be integrated to generate content dynamically within the application.

The development process follows an **iterative and incremental methodology**, with version control handled via **Git** and **GitHub**, and continuous integration and DevOps workflows implemented using **GitHub Actions**.

---

# Summary Table/List

| Category             | Details                                                                                      |
|---------------------|----------------------------------------------------------------------------------------------|
| **Type**             | Web SPA with Spring Boot backend and REST API                                               |
| **Technologies**     | Angular (frontend), Spring Boot (backend), Java 21, SQL, OpenAPI                             |
| **Tools**            | VSCode with Spring Boot and Java extensions, Postman, GitHub, GitHub Projects, SonarQube Cloud   |
| **Quality Control**  | Unit and integration tests with RestAssured (Java), frontend tests with Karma and Jasmine, e2e testing with Selenium, coverage with JaCoCo, static code analysis with SonarQube, CI with GitHub Actions |
| **Deployment**       | Currently run locally; continuous integration controlled with GitHub Actions               |
| **Development Process** | Iterative and incremental, Git/GitHub, continuous integration with GitHub Actions       |
