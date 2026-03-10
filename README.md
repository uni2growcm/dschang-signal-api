# Dschang's Signal — API

> REST API backend for the Dschang's Signal citizen issue reporting platform.  
> Built with **Spring Boot 4 · Java 21 · PostgreSQL**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://openjdk.org)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://docs.docker.com/compose)

**Repository:** https://github.com/uni2growcm/dschang-signal-api  
**Frontend counterpart:** https://github.com/uni2growcm/dschang-signal-fe

---

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Available Scripts](#available-scripts)
- [Project Structure](#project-structure)
- [API Reference](#api-reference)
- [Contributing](#contributing)

---

## Overview

Dschang's Signal is a web application where citizens report urban problems and city administrators review and track their resolution. This repository contains the Spring Boot REST API.

**Key responsibilities:**

- JWT-based authentication (access + refresh tokens) for Citizen and Admin roles.
- Report submission, moderation (accept/reject), and status lifecycle management.
- Paginated, filterable public feed of accepted reports.
- Photo upload handling via local filesystem or S3-compatible storage.

---

## Prerequisites

| Tool           | Minimum version | Notes                                 |
| -------------- | --------------- | ------------------------------------- |
| Java (JDK)     | 21              | Required to run/build the app locally |
| Docker         | 24.x            | Required to run the database          |
| Docker Compose | 2.x             | Bundled with Docker Desktop           |

> You do **not** need a local PostgreSQL installation. The database runs in Docker.

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/uni2growcm/dschang-signal-api.git
cd dschang-signal-api
```

### 2. Start the database

```bash
docker compose up -d dschang-signal
```

This starts a PostgreSQL 15 container and exposes it on `localhost:5432`. Flyway migrations run automatically on the first application startup.

### 3. Configure environment variables

```bash
cp .env.example .env
```

Edit `.env` and fill in the required values (see [Environment Variables](#environment-variables) below).

### 4. Run the application

```bash
./gradlew bootRun
```

The API will be available at **http://localhost:8080/api/v1**.

> On first run, Flyway will apply all pending migrations and create the schema automatically.

---

## Environment Variables

Copy `.env.example` to `.env` and configure the values below. The application reads these at startup via Spring's `application.yml`.

| Variable                | Description                          | Example                         |
| ----------------------- | ------------------------------------ | ------------------------------- |
| `DB_HOST`               | PostgreSQL host                      | `localhost`                     |
| `DB_PORT`               | PostgreSQL port                      | `5432`                          |
| `DB_NAME`               | Database name                        | `dschang_signal`                |
| `DB_USER`               | Database username                    | `dsignal_user`                  |
| `DB_PASSWORD`           | Database password                    | `changeme`                      |
| `JWT_SECRET`            | HS256 signing secret (min 32 chars)  | `a-very-long-random-secret-key` |
| `JWT_ACCESS_EXPIRY_MS`  | Access token TTL in milliseconds     | `900000` (15 min)               |
| `JWT_REFRESH_EXPIRY_MS` | Refresh token TTL in milliseconds    | `604800000` (7 days)            |
| `STORAGE_TYPE`          | `local` or `s3`                      | `local`                         |
| `STORAGE_LOCAL_PATH`    | Absolute path for local file uploads | `/tmp/dschang-uploads`          |
| `CORS_ALLOWED_ORIGINS`  | Comma-separated frontend origin(s)   | `http://localhost:5173`         |

**S3 variables** (only required when `STORAGE_TYPE=s3`):

| Variable        | Description                            |
| --------------- | -------------------------------------- |
| `S3_BUCKET`     | S3 bucket name                         |
| `S3_REGION`     | AWS region                             |
| `S3_ACCESS_KEY` | AWS access key ID                      |
| `S3_SECRET_KEY` | AWS secret access key                  |
| `S3_ENDPOINT`   | Custom endpoint (for MinIO / local S3) |

> Never commit `.env` or any file containing real secrets. It is already listed in `.gitignore`.

---

## Available Scripts

All commands use the Gradle wrapper (`./gradlew`) — no global Gradle installation required.

| Command                                | Description                                   |
| -------------------------------------- | --------------------------------------------- |
| `./gradlew bootRun`                    | Start the application in development mode     |
| `./gradlew build`                      | Compile and package the application as a JAR  |
| `./gradlew test`                       | Run the full test suite                       |
| `./gradlew test --tests "*ClassName*"` | Run a specific test class                     |
| `docker compose up -d db`              | Start only the PostgreSQL container           |
| `docker compose down`                  | Stop and remove all containers                |
| `docker compose down -v`               | Stop containers and delete volumes (wipes DB) |

---

## Project Structure

```
src/
└── main/
    └── java/com/dschangssignal/
        ├── auth/            # JWT filter, token service, AuthController
        ├── config/          # SecurityConfig, CorsConfig, bean definitions
        ├── report/          # Report entity, repository, service, controller
        ├── user/            # User entity, repository, UserDetailsService
        ├── common/          # GlobalExceptionHandler, ApiResponse wrapper
        └── storage/         # StorageService abstraction (local + S3 impls)
    └── resources/
        ├── application.yml  # Main configuration (reads from env vars)
        └── db/migration/    # Flyway SQL migration scripts (V1__, V2__, …)
```

---

## API Reference

Base URL: `http://localhost:8080/api/v1`

All protected endpoints require an `Authorization: Bearer <access_token>` header.

### Authentication

| Method | Path             | Auth                   | Description                            |
| ------ | ---------------- | ---------------------- | -------------------------------------- |
| `POST` | `/auth/register` | None                   | Register a new Citizen account         |
| `POST` | `/auth/login`    | None                   | Login; returns access + refresh tokens |
| `POST` | `/auth/refresh`  | Refresh token (cookie) | Issue a new access token               |
| `POST` | `/auth/logout`   | Bearer                 | Revoke refresh token                   |

### Reports

| Method   | Path            | Auth                  | Description                              |
| -------- | --------------- | --------------------- | ---------------------------------------- |
| `GET`    | `/reports`      | None                  | Paginated public feed (accepted reports) |
| `GET`    | `/reports/{id}` | None                  | Get a single accepted report             |
| `POST`   | `/reports`      | Citizen / Admin       | Submit a new report                      |
| `DELETE` | `/reports/{id}` | Citizen (own) / Admin | Delete a report                          |

### Admin

| Method  | Path                         | Auth  | Description                                     |
| ------- | ---------------------------- | ----- | ----------------------------------------------- |
| `GET`   | `/admin/reports`             | Admin | List all reports including pending and rejected |
| `PATCH` | `/admin/reports/{id}/accept` | Admin | Accept a pending report                         |
| `PATCH` | `/admin/reports/{id}/reject` | Admin | Reject a report (body: `{ "reason": "..." }`)   |
| `PATCH` | `/admin/reports/{id}/status` | Admin | Update status: `IN_PROGRESS` or `RESOLVED`      |

**Feed query parameters** (`GET /reports`):

| Parameter  | Description                                      | Default          |
| ---------- | ------------------------------------------------ | ---------------- |
| `category` | Filter by category                               | —                |
| `status`   | Filter by `PENDING`, `IN_PROGRESS`, `RESOLVED`   | —                |
| `page`     | Zero-based page index                            | `0`              |
| `size`     | Page size (max 100)                              | `20`             |
| `sort`     | Sort field and direction (e.g. `createdAt,desc`) | `createdAt,desc` |

---

## Contributing

Thank you for considering a contribution to Dschang's Signal!

### Workflow

1. **Fork** the repository and create your branch from `main`:
   ```bash
   git checkout -b feat/your-feature-name
   ```
2. **Make your changes.** Keep commits small and focused.
3. **Write or update tests** for any logic you add or change. New service methods require unit tests; new endpoints require at least one integration test.
4. **Run the full test suite** before opening a PR:
   ```bash
   ./gradlew test
   ```
5. **Open a Pull Request** against the `main` branch with a clear description of what was changed and why.

### Branch naming

| Prefix      | Use for                                     |
| ----------- | ------------------------------------------- |
| `feat/`     | New features                                |
| `fix/`      | Bug fixes                                   |
| `chore/`    | Tooling, dependencies, config               |
| `docs/`     | Documentation only                          |
| `refactor/` | Code restructuring without behaviour change |

### Code style

- Follow standard Java conventions and the existing code structure.
- All new endpoints must be secured — never rely solely on URL-pattern matching. Re-validate ownership and role inside the service layer.
- Database changes must be introduced via a new **Flyway migration script** (`V{n}__description.sql`). Never modify existing migration files.
- Write meaningful commit messages following [Conventional Commits](https://www.conventionalcommits.org/).

### Reporting bugs

Open a [GitHub Issue](https://github.com/uni2growcm/dschang-signal-api/issues) with a clear title, steps to reproduce, expected vs. actual behaviour, and relevant log output.
