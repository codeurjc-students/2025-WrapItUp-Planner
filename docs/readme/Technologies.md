# Technologies

1. **Angular** – A JavaScript framework for building Single Page Applications (SPAs). It provides users with a dynamic and responsive interface while also handling user interactions. Angular’s component-based architecture allows developers to create independent, reusable, and maintainable components that enhance development productivity. Angular serves as the front end interface of WrapItUp Planner and it communicates with the application's backend through different API calls to provide users with up-to-date information.    
URL: [Angular](https://angular.io)

2. **Spring Boot** – A Java based framework designed to simplify the process of building backend applications. It streamlines the creation of RESTful APIs and supports the system's main functionality. Springboot provides powerful features such as dependency injection and integration with Spring Data JPA, which eases the communication with the database and guarantees a better scalability. Spring Boot serves as a provider for backend operations, it also handles user requests from the frontend through the Restful API and a connection to the database.  
   URL: [Spring Boot](https://spring.io/projects/spring-boot)

3. **Java 21** – A modern, versatile programming language used for the backend development of WrapItUp planner in conjuction with Springboot. Java provides strong type safety and an extensive standard library which provides developers plenty ease of use. Java is well suited for developing scalable and mantainable server side applications.  
   URL: [Java](https://www.oracle.com/java/)

4. **Maven** – A build automation and dependency management tool for Java projects. In WrapItUp Planner, Maven manages the project dependencies, build lifecycle, and packaging for the Spring Boot backend, ensuring that the application is consistently built, tested, and deployable.  
   URL: [Maven](https://maven.apache.org)

6. **SQL** – A relational database language used to manage and query the application’s data. In WrapItUp Planner, SQL (via MySQL) stores all of the application's data. The backend connects to the database through MySQL Connector, performing CRUD operations and ensuring data consistency, integrity, and persistence throughout the system.   
   URL: [MySQL](https://www.mysql.com)

7. **Testing Frameworks** – The project implements unit, integration, and end-to-end testing to guarantee reliability and maintainability. The backend uses **REST Assured** and **Mockito** to test business logic and RESTful API endpoints. The frontend relies on **Karma** and **Jasmine** to test Angular components and user interactions, while **Selenium** is used for end-to-end (E2E) testing to simulate real user behavior.  
**URLs:** [Mockito](https://site.mockito.org), [REST Assured](https://rest-assured.io/), [Karma](https://karma-runner.github.io/latest/index.html), [Jasmine](https://jasmine.github.io), [Selenium](https://www.selenium.dev)
