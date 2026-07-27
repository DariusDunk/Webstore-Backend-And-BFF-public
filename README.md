# Online Shop — Backend & BFF Services

Server-side component of a full-stack e-commerce platform developed as a master's thesis project.

The repository contains a central Spring Boot backend and dedicated Backend-for-Frontend (BFF) services for the React web client and Android application.

## Architecture

```
                   HTTPS
                     │
                     ▼
                ┌────────┐
                │ NGINX  │
                └───┬────┘
                    │
       ┌────────────┼────────────┐
       ▼            ▼            ▼
  React BFF    Android BFF    MinIO
       │            │            ▲
       └──────┬─────┘            │
              ▼                  │
       Spring Boot Backend───────┘
              │
     ┌────────┼─────────┐
     ▼        ▼         ▼
PostgreSQL Keycloak TensorFlow
                       Serving
```

## Features

* Product catalogue, search, categories, manufacturers, sales, and discounts
* Shopping cart and favourites functionality
* Guest and authenticated user sessions
* User accounts, authentication, authorization, and profile management
* Purchase and order management
* Product reviews
* Administrative management functionality
* AI-powered product search using image classification
* Product image storage through MinIO
* Email-based system functionality
* REST APIs for web and mobile clients

## Technologies

**Backend:** Java 21, Spring Boot, Spring Data JPA, Hibernate, Gradle
**Database:** PostgreSQL
**Authentication:** Keycloak, Spring Security, JWT
**BFF services:** Node.js
**Object storage:** MinIO
**AI:** TensorFlow, TensorFlow Serving
**Infrastructure:** Docker, Docker Compose, NGINX
**Testing:** JUnit, Mockito, Spring Boot Test

## Repository Structure

├── Spring Boot Backend
├── React BFF
└── Android BFF

The BFF services are currently maintained within the same repository as the central backend.

## Running the Project

### Requirements

* Java JDK 21
* Docker and Docker Compose
* Node.js
* PostgreSQL
* Keycloak
* MinIO
* TensorFlow Serving

The Gradle Wrapper is included in the repository.

```bash
./gradlew build
./gradlew test
```

On Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat test
```

The application requires configuration for its external services. Environment-specific configuration and credentials are not included in the public repository.

## Testing

The project contains unit tests.

## Project Context

This repository contains the server-side components of a multi-client e-commerce system developed as a master's thesis project. The complete system consists of this repository, a React web client, and an Android client.

