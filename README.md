# Credit API
A Spring Boot REST API for managing customer loans

## Build & Run
 
If you have docker installed on your system `docker-compose -f docker-compose.local up --build` then access via `localhost:8080`

If you don't have docker:

* Make sure you have maven and java installed via `mvn -version`, if not `brew install maven`

* Make sure you have java installed via `java -version`, if not `brew install openjdk@21`

* From the project's root directory, build the project with `mvn package` or `mvn install`. You can add `-DskipTests` flag to skip tests

* Run the app via `java -jar target/credit-api-0.0.1-SNAPSHOT.jar` or if you want hot reload `mvn spring-boot:run`