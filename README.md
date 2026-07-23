# Smart Campus Navigator

Smart Campus Navigator is a full-stack campus routing application that helps students find the shortest walking route between two campus locations. It combines a Spring Boot REST API, a React/Vite frontend, JWT authentication, seeded campus data, and custom graph/search algorithms.

## Features

- Student route finder with shortest path, distance, walking-time estimate, and visual route highlighting
- Admin graph management for campus nodes and walkable edges
- Custom Dijkstra implementation for weighted campus routing
- Custom Trie implementation for fast autocomplete suggestions
- JWT-based authentication with student and admin roles
- Seeded demo users, buildings, graph nodes, and paths on first startup
- Swagger UI for interactive API testing

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 22, Spring Boot 3.3.5 |
| Security | Spring Security, JWT |
| Database | PostgreSQL |
| ORM | Spring Data JPA, Hibernate |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Frontend | React 18, Vite, Tailwind CSS |
| Algorithms | Custom Dijkstra shortest path, custom Trie autocomplete |
| Build Tools | Maven, npm |

## Project Structure

```text
smart-campus-navigator/
├── src/main/java/com/megh/smartcampus/
│   ├── algorithm/
│   │   ├── graph/          # Dijkstra graph model and graph service
│   │   └── trie/           # Trie autocomplete model and search service
│   ├── config/             # Security, OpenAPI, and seed data configuration
│   ├── controller/         # REST API controllers
│   ├── dto/                # Request and response DTOs
│   ├── entity/             # JPA entities
│   ├── exception/          # Application exception handling
│   ├── repository/         # Spring Data repositories
│   ├── security/           # JWT filter, token utility, user details service
│   └── service/            # Application service interfaces/implementations
├── src/main/resources/
│   └── application.yml     # Backend configuration
├── src/test/java/          # Unit and validation tests
├── frontend/
│   ├── src/
│   │   ├── components/     # Campus graph visualization
│   │   ├── pages/          # Login, register, home, and admin pages
│   │   ├── api.js          # Frontend API client
│   │   └── main.jsx        # React router and auth guards
│   ├── package.json
│   └── vite.config.js
├── pom.xml
└── README.md
```

## Prerequisites

- Java 22 or newer
- Maven 3.9 or newer
- PostgreSQL 14 or newer
- Node.js 20 or newer
- npm

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/smart-campus-navigator.git
cd smart-campus-navigator
```

### 2. Create the Database

Create a PostgreSQL database named `smart_campus_db`.

```sql
CREATE DATABASE smart_campus_db;
```

### 3. Configure the Backend

Update `src/main/resources/application.yml` with your local PostgreSQL credentials.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smart_campus_db
    username: postgres
    password: postgres
```

For production use, move database credentials and JWT secrets to environment variables or a private profile file.

### 4. Run the Backend

```bash
mvn spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

On startup, `DataSeeder` creates demo users, campus buildings, graph nodes, and walkable edges if the database is empty.

### 5. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:3000
```

## Demo Accounts

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@smartcampus.com` | `Admin@123` |
| Student | `student@university.edu` | `Student@123` |

## API Documentation

After starting the backend, open Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

### Main Endpoints

| Method | Endpoint | Description | Access |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Register a user | Public |
| POST | `/api/v1/auth/login` | Log in and receive tokens | Public |
| POST | `/api/v1/auth/refresh` | Refresh an access token | Public |
| GET | `/api/v1/health` | Health check | Public |
| GET | `/api/v1/nodes` | List campus graph nodes | Authenticated |
| POST | `/api/v1/nodes` | Create a graph node | Admin |
| PUT | `/api/v1/nodes/{id}` | Update a graph node | Admin |
| DELETE | `/api/v1/nodes/{id}` | Delete a graph node | Admin |
| GET | `/api/v1/edges` | List graph edges | Authenticated |
| POST | `/api/v1/edges` | Create a graph edge | Admin |
| DELETE | `/api/v1/edges/{id}` | Delete a graph edge | Admin |
| GET | `/api/v1/navigate/route?from=1&to=8` | Find the shortest path | Authenticated |
| GET | `/api/v1/navigate/nearest?from=1&type=LIBRARY` | Find nearest facility by type | Authenticated |
| POST | `/api/v1/graph/reload` | Rebuild in-memory graph | Admin |
| GET | `/api/v1/search/suggest?q=lib` | Autocomplete campus locations | Authenticated |
| GET | `/api/v1/buildings` | List buildings | Authenticated |
| POST | `/api/v1/buildings` | Create a building | Admin |
| PUT | `/api/v1/buildings/{id}` | Update a building | Admin |
| DELETE | `/api/v1/buildings/{id}` | Delete a building | Admin |
| GET | `/api/v1/users/profile` | Get current user profile | Authenticated |

### Authentication Flow

1. Call `POST /api/v1/auth/login`.
2. Copy the returned access token.
3. Send the token in secured requests:

```text
Authorization: Bearer <access-token>
```

In Swagger UI, click `Authorize` and enter the bearer token.

## Algorithms

### Dijkstra Shortest Path

The backend stores campus walkways as weighted edges and uses Dijkstra's algorithm to find the minimum-distance path between two graph nodes. Edge weights represent walking distance in meters, so the selected route is based on real path cost rather than hop count.

### Trie Autocomplete

Campus location search uses a Trie so prefix suggestions can be resolved quickly. After the service reaches the node matching the typed prefix, it collects matching location names below that point without running a database query for every keystroke.

## Testing

Run backend tests:

```bash
mvn test
```

Run focused algorithm and validation tests:

```bash
mvn test -Dtest="CampusGraphTest,BusinessRuleTest"
```

Build the frontend:

```bash
cd frontend
npm run build
```

## Seeded Campus Data

The initial dataset includes demo buildings such as Main Gate, Admin Block, CSE Block, ECE Block, Central Library, Cafeteria, Auditorium, Medical Center, Parking Area, and Washroom Block A. It also creates graph nodes and bidirectional walkway edges so routing works immediately after startup.

## Security Notes

- Passwords are hashed with BCrypt.
- JWT access tokens are signed with HMAC-SHA256.
- Student and admin routes are protected by role checks.
- Demo credentials are for local development only.
- The checked-in `application.yml` uses local development defaults. Replace secrets and database credentials before deployment.

## Useful Commands

```bash
# Start backend
mvn spring-boot:run

# Run backend tests
mvn test

# Start frontend
cd frontend
npm run dev

# Build frontend
cd frontend
npm run build
```

## License

This project is available for learning and personal project use.
