# Team Leave Calendar

A simple web application for managing team leave requests and viewing the on-call rotation schedule.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA |
| Database | H2 (in-memory) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Frontend | Angular 17 (standalone components) |

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+ and npm

## Running the Application

### 1. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

H2 Console (for debugging): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:leavecalendardb`
- Username: `sa`, Password: *(empty)*

### 2. Start the Frontend

```bash
cd frontend
npm install
npm start
```

The app will be available at: `http://localhost:4200`

## Features

- **Leave Requests** — Create, list, filter (by member / status), approve, reject, and delete leave requests
- **Overlap prevention** — A team member cannot have two overlapping leave requests
- **Calendar view** — Month view with color-coded leave events (filterable by member)
- **On-Call schedule** — 12-week rolling schedule with conflict detection (highlighted when the on-call person has approved leave)
- **Swagger UI** — Interactive REST API documentation

## API Endpoints

| Method | URL | Description |
|---|---|---|
| GET | `/api/members` | List all team members |
| GET | `/api/leave-requests` | List all leave requests (supports `?memberId=` and `?status=` filters) |
| POST | `/api/leave-requests` | Create a leave request |
| PATCH | `/api/leave-requests/{id}/status` | Approve or reject a request |
| DELETE | `/api/leave-requests/{id}` | Delete a request |
| GET | `/api/oncall/schedule?weeks=8` | Get on-call schedule |
| GET | `/api/oncall/today` | Get today's on-call person |

## Running Tests

```bash
cd backend
mvn test
```

Tests include:
- Unit tests for `LeaveRequestService` (overlap detection, status updates, validation)
- Unit tests for `OnCallService` (rotation logic, conflict detection, wrap-around)
- Integration tests for REST endpoints via MockMvc

## Assumptions

- Team members are fixed (Alice, Bob, Charlie, Diana) and seeded on startup. No member management UI is needed.
- The on-call rotation is anchored to ISO week 1 of 2024 to ensure a consistent, deterministic schedule regardless of when the app starts.
- Overlap check applies to any status (including Pending) — this prevents double-booking before approval.
- The H2 database is reset on restart. This is intentional for a demo/test setup. Switching to PostgreSQL requires only a dependency swap and updating `application.properties`.
- No user authentication is required per the spec.

## Optional Improvements Not Included

- Docker setup (not needed since the app runs with a single `java -jar`)
- Automatic on-call replacement suggestion
- Comments on leave requests
- Leave request editing (only status update is supported)
