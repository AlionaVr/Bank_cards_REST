# Bank Cards REST API

A secure Spring Boot REST service for managing users, bank cards, and money transfers between a user’s own cards.
The project includes JWT-based authentication, role-based access control, AES-encrypted card numbers, and Liquibase
migrations for PostgreSQL/MySQL.

---

## Features

- User registration and authentication with JWT
- Create, activate, block, and delete bank cards
- View and filter user cards with pagination
- Transfer funds between user’s own cards
- Encryption for card numbers and masked display (**** **** **** 1234)
- Role-based access (ADMIN, USER)
- Liquibase migrations for PostgreSQL
- Lightweight Docker setup

---

## Tech Stack

- **Java 17+**
- **Spring Boot 3**
- **Spring Security + JWT**
- **Spring Data JPA**
- **PostgreSQL**
- **Liquibase**
- **Docker & Docker Compose**
- **JUnit / Mockito**
- **Swagger UI**

---

## Running with Docker

### Build the application

```bash
./mvnw clean package -DskipTests
```

### Start containers

```bash
docker compose up --build
```

By default, the service runs on **http://localhost:8080** and connects to PostgreSQL on **port 5432**.

---

## API Documentation (Swagger UI)

Interactive documentation is available via Swagger UI.

Swagger UI: **http://localhost:8080/swagger-ui.html**

OpenAPI JSON: **http://localhost:8080/v3/api-docs**

You can use it to:

- Explore all endpoints
- Test requests directly from the browser
  To authorize, click “Authorize” and enter:

```bash
 Bearer <jwt_token> 
 ```

---

## Running Tests

```bash
./mvnw test
```
