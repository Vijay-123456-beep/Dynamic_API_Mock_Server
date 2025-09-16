# Dynamic API Mock Server

Spring Boot app that lets developers define mock API endpoints per user, with JWT auth, dynamic routing, delay/status simulation, versioning, Swagger docs, and WebSocket hooks.

## Quick start

1. Ensure Java 17+, Maven, Docker.
2. Start Postgres via Docker Compose:

```bash
docker compose up -d db
```

3. Run app locally:

```bash
mvn spring-boot:run
```

App: `http://localhost:8080`  | Swagger: `http://localhost:8080/swagger-ui/index.html`

## Auth

- POST `/auth/register` { username, password, email } -> { token }
- POST `/auth/login` { username, password } -> { token }

Use `Authorization: Bearer <token>` for protected routes.

## Mock endpoints CRUD

- GET `/mock/endpoints`
- POST `/mock/endpoints` body:

```json
{
  "endpoint": "/users",
  "method": "GET",
  "responseJson": "{\"id\":1,\"name\":\"Vijay\"}",
  "statusCode": 200,
  "delayMs": 200
}
```

## Dynamic serving

- Request `GET /mock/users`
- Optional headers:
  - `X-Mock-User`: username (if not authenticated)
  - `X-Delay-Override`: ms
  - `X-Status-Override`: http code

## Docker all-in-one

```bash
docker compose up --build
```

## Notes

- Change `app.jwt.secret` in `application.yml`.
- Current WebSocket hook placeholder; extend as needed.


