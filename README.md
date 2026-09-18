# Book Management Microservice

Simple RESTful API for managing books built with **Spring Boot** and **PostgreSQL**.
This project is built as a technical assessment for the Java Developer position.

Repository: https://github.com/anwarsyaifudin/springboot-microservice-task--Anwar-Syaifudin-

## Overview

Architecture:

```
Client / Postman
      |
      v
REST Controller ............ BookController
      |
      v
Service Layer .............. BookService / BookServiceImpl
      |
      v
Repository Layer ........... BookRepository (Spring Data JPA)
      |
      v
PostgreSQL
```

## Tech Stack

- Java 17
- Spring Boot 3.3.5
- Maven
- Spring Web
- Spring Data JPA
- Bean Validation (Jakarta Validation)
- PostgreSQL
- H2 (test scope, for repository integration tests)
- JUnit 5 + Mockito + MockMvc
- Postman
- Git

## Features

- Full CRUD REST API: `POST`, `GET`, `GET/{id}`, `PUT`, `PATCH`, `DELETE`
- `PUT` replaces **all** fields, `PATCH` updates **only provided** fields
- Bean Validation with friendly `400` responses (field-to-message error map)
- `404` handling with a consistent JSON error body
- ISBN uniqueness enforced both by the service layer (`409 Conflict`) and the
  database unique constraint
- Layered architecture: Controller → Service → Repository
- Unit + integration tests (22 tests)
- Simple demo web frontend served by the same application
- Credentials are not hardcoded (environment variables)

## Quick Start

```bash
# 1. Set database env vars, then run
export DB_URL=jdbc:postgresql://localhost:5432/book_management
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

mvn spring-boot:run          # API  -> http://localhost:8080/api/books
                             # Web  -> http://localhost:8080/
```

## Requirements

- JDK 17+
- Maven 3.9+
- PostgreSQL (running with a database named `book_management`)

## Configuration

Database credentials are **not hardcoded**. They are read from environment
variables with sensible defaults:

| Variable      | Default value                                      |
|---------------|----------------------------------------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/book_management` |
| `DB_USERNAME` | `postgres`                                         |
| `DB_PASSWORD` | `postgres`                                         |

Set them before running, for example:

```bash
# Windows (PowerShell)
$env:DB_URL="jdbc:postgresql://localhost:5432/book_management"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"

# Linux / macOS
export DB_URL=jdbc:postgresql://localhost:5432/book_management
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

The schema is created/updated automatically by Hibernate
(`spring.jpa.hibernate.ddl-auto=update`). Table name is `books`.

## Run Application

```bash
mvn spring-boot:run
```

The service starts on port `8080`.

Build & run tests:

```bash
mvn clean test
```

Package an executable JAR:

```bash
mvn package
java -jar target/bookmanagement-0.0.1-SNAPSHOT.jar
```

## Web UI (Demo Frontend)

A simple demo frontend is served by the same application at
`http://localhost:8080/`. It is a single HTML page (`src/main/resources/static/index.html`)
that lets you create, view, update, partially update (PATCH), and delete books
directly from the browser.

## API Endpoints

| Method | Endpoint              | Description             | Success |
|--------|-----------------------|-------------------------|---------|
| POST   | `/api/books`          | Create a book           | 201     |
| GET    | `/api/books`          | Get all books           | 200     |
| GET    | `/api/books/{id}`     | Get a book by id        | 200     |
| PUT    | `/api/books/{id}`     | Full update (all fields required) | 200 |
| PATCH  | `/api/books/{id}`     | Partial update (only provided fields) | 200 |
| DELETE | `/api/books/{id}`     | Delete a book           | 204     |

### Create Book — `POST /api/books`

Request:

```json
{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "publishedDate": "2008-08-01"
}
```

Response `201 Created`:

```json
{
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "publishedDate": "2008-08-01"
}
```

### Partial Update — `PATCH /api/books/1`

With `PUT`, every field is required. With `PATCH`, only the fields provided in
the request body are updated:

Request:

```json
{
    "title": "Clean Code - Updated Edition"
}
```

Response:

```json
{
    "id": 1,
    "title": "Clean Code - Updated Edition",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "publishedDate": "2008-08-01"
}
```

### Validation — `400 Bad Request`

Request without required fields:

```
{
    "author": "Robert C. Martin"
}
```

Response:

```json
{
    "status": 400,
    "message": "Validation failed",
    "errors": {
        "title": "Title is required",
        "isbn": "ISBN is required"
    },
    "timestamp": "2026-09-18T03:00:00.000Z"
}
```

### Not Found — `404`

`GET /api/books/999` (no such book):

```json
{
    "status": 404,
    "message": "Book with id 999 not found",
    "timestamp": "2026-09-18T03:00:00.000Z"
}
```

### Duplicate ISBN — `409 Conflict`

Creating a second book with `isbn = 9780132350884`:

```json
{
    "status": 409,
    "message": "Book with ISBN 9780132350884 already exists",
    "timestamp": "2026-09-18T03:00:00.000Z"
}
```

## Database Schema (reference)

```sql
CREATE TABLE books (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    author         VARCHAR(255) NOT NULL,
    isbn           VARCHAR(20)  NOT NULL UNIQUE,
    published_date DATE
);
```

> The table is created automatically by Hibernate. The SQL above is only a
> reference of the resulting schema.

## ER Diagram

```
+------------------------------+
|            books             |
+------------------------------+
| PK id            BIGSERIAL   |
|    title         VARCHAR(255)|
|    author        VARCHAR(255)|
| UK isbn          VARCHAR(20) |
|    published_date DATE       |
+------------------------------+
```

## Project Structure

```
src/main/java/com/anwar/bookmanagement/
│
├── controller
│   └── BookController.java
│
├── service
│   ├── BookService.java
│   └── BookServiceImpl.java
│
├── repository
│   └── BookRepository.java
│
├── entity
│   └── Book.java
│
├── dto
│   ├── BookRequest.java
│   ├── BookPatchRequest.java
│   ├── BookResponse.java
│   └── ApiError.java
│
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── DuplicateIsbnException.java
│
└── BookManagementApplication.java
```

## Testing

```bash
mvn test
```

Test classes:

| Test                      | Scope                                          |
|---------------------------|------------------------------------------------|
| `BookServiceImplTest`     | 11 service unit tests with Mockito            |
| `BookControllerTest`      | 8 web layer tests with MockMvc (`@WebMvcTest`)|
| `BookRepositoryTest`      | 3 JPA integration tests with in-memory H2     |

Highlights covered:

- Create / read / update / partial-update / delete
- `404` when a book is not found
- `409` when the ISBN is duplicated
- `PUT` replaces all fields vs `PATCH` only updates provided fields
- Validation errors return a field-to-message `errors` map

## Postman

A ready-to-use collection is included under `postman/`:

- `Book Management API.postman_collection.json`
- `Book Management API (Local).postman_environment.json`

### Postman (GUI)

1. Open Postman → Import → select the collection JSON.
2. Import → select the environment JSON.
3. Select environment **Book Management API (Local)**.
4. Run the collection from the Collection Runner, or execute each request
   manually.

| Variable   | Value                 |
|------------|-----------------------|
| `baseUrl`  | `http://localhost:8080` |
| `bookId`   | `1`                   |

> `bookId` is updated automatically by the *Create Book* request, so the other
> requests always target the book that was just created.

### Postman CLI (Newman)

```bash
npm install -g newman
newman run "postman/Book Management API.postman_collection.json" \
  -e "postman/Book Management API (Local).postman_environment.json"
```

The collection includes the 6 CRUD operations plus error-case requests
(duplicate ISBN and non-existent book).

## Author

**Anwar Syaifudin**

- GitHub: https://github.com/anwarsyaifudin