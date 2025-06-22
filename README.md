# Credit API
Spring Boot REST API for managing customer loans, installments, and payments

## Features
- Create and list loans
- List and pay installments
- Centralized exception handling with detailed error responses
- OpenAPI (Swagger) documentation
- In-memory H2 database with seeded users (1 admin, 2 users)
- JWT-based authentication and authorization
- Admin and customer role

## Deployment

The API is also deployed and accessible at:

- https://dev.credit-api.atakanbal.com

![alt text](<Screenshot 2025-06-23 at 16.44.15.png>)

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- (Optional) Docker

### Build & Run

**With Docker:**
```sh
docker-compose -f docker-compose.local up --build
```
Access the API at [http://localhost:8080](http://localhost:8080)

Access the DB from http://localhost:8080/h2-console

**Without Docker:**
1. Ensure Java and Maven are installed:
   ```sh
   mvn -version
   java -version
   ```
   (Install via `brew install maven openjdk@21` if needed)
2. Build the project:
   ```sh
   mvn clean package
   # or to skip tests:
   mvn clean package -DskipTests
   ```
3. Run the app:
   ```sh
   export SPRING_PROFILES_ACTIVE=local
   java -jar target/credit-api-0.0.1-SNAPSHOT.jar
   # or for hot reload:
   mvn spring-boot:run
   ```

## API Documentation
- OpenAPI spec [http://localhost:8080/openapi.yaml](http://localhost:8080/openapi.yaml)
- Swagger UI http://localhost:8080/swagger-ui/index.html


## Authentication
- Obtain a JWT token via `/auth/login` (see OpenAPI for request/response format)
- Use the token in the `Authorization: Bearer <token>` header for all protected endpoints
- Pre-seeded users:
  - **Admin:**
    - username: `admin` / password: `admin`
  - **User:**
    - username: `user1` / password: `user1`
    - username: `user2` / password: `user2`

-  user1 has 5 pre-seeded loans for testing with each one having 6 installments

    1. all unpaid, createDate = today (all due in next 6 months). 
    2. all paid, createDate = 6 months ago
    3. 3 paid, 3 unpaid, createDate = 6 months ago
    4. all unpaid, all due, 3 discount, 3 reward if paid
    5. loan.isPaid = true createDate = 6 months ago
 
 


## Example Requests

### Login
```json
POST /auth/login
{
  "username": "user1",
  "password": "user1"
}
```

### Create Loan 
> ⚠️  Admin only
```json
POST /loans
Authorization: Bearer <token>
{
  "customerId": "...",
  "loanAmount": 1200,
  "interestRate": 0.2,
  "numberOfInstallments": 6
}
```

### Pay Installment
```json
POST /loans/{loanId}/pay
Authorization: Bearer <token>
{
  "amount": 100
}
```

## Error Handling
All errors return a structured JSON response:
```json
{
  "timestamp": "2025-06-23T12:34:56Z",
  "status": 400,
  "error": "Bad Request",
  "message": "amount must be greater than 0",
  "path": "/loans/123/pay"
}
```

## Testing
Run all tests:
```sh
mvn test
```

## Contact
atakannbal@gmail.com


## License
MIT