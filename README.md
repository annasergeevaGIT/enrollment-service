# Enrollment Service

This microservice student enrollments for the E-Learning Platform

## Related Services

| Service                                                               | Description                       |
|-----------------------------------------------------------------------|-----------------------------------|
| [Enrollment Service](https://github.com/annasergeevaGIT/enrollment-service-e-learning-platform)   | Manages course enrollments |
| [Course Service](https://github.com/annasergeevaGIT/course-service-e-learning-platform)   | Handles courses and content|
| [Feedback Service](https://github.com/annasergeevaGIT/eedback-service-e-learning-platform) | Manages ratings and feedback |
| [Course Aggregate Service](https://github.com/annasergeevaGIT/aggregate-service-e-learning-platform)| Aggregates course and review data |
| [Gateway Service](https://github.com/annasergeevaGIT/gateway-service-e-learning-platform)| Routing, security, rate limiting |
| [Discovery Service](https://github.com/annasergeevaGIT/discovery-service-e-learning-platform)| Eureka Service registry |
| [Dispatcher Service](https://github.com/annasergeevaGIT/dispatcher-service-e-learning-platform)| Kafka producer/consumer (event streaming) |
| [Docker Deployment](https://github.com/annasergeevaGIT/dispatcher-service-e-learning-platform)| Centralized configuration management |

## Overview

The **Enrollment Service** provides functionality for managing student enrollments, including creating new enrollments, updating enrollment statuses, and retrieving enrollment details.
It supports two runtime modes:
- **Spring WebFlux (Reactive version)**, or
- **Spring Boot with Virtual Threads (Project Loom version)**

This allows direct comparison of scalability and performance between the two concurrency models.

## Functionality

- Create, update, and delete enrollments
- Retrieve enrollments by student or course
- Handle concurrent enrollment requests
- Publish enrollment events to Kafka for asynchronous updates in other services
- Store data in PostgreSQL, with Flyway for database migrations and R2DBC for reactive access

## Endpoints

| Method | Endpoint | Description                             |
|---------|-----------|-----------------------------------------|
| `POST` | `/v1/course-enrollments` | Creates a new user enrollment. Module names are provided in the request body. Returns 500 if any module is missing in the Course Service.                    |
| `GET` | `/v1/course-enrollments` | Retrieves the list of user enrollments, sorted by creation date.                    |

## Tech Stack

- **Java 21**
- **Spring Boot 3**
- **Spring WebFlux**
- **R2DBC (Reactive DB access)**
- **PostgreSQL**
- **Flyway (DB migrations)**
- **Kafka**
- **Micrometer / Prometheus**
- **Eureka Discovery**
- **Spring Cloud Config**
- **Docker**

## Build & Run

```bash
./gradlew clean bootBuildImage
docker-compose up -d
./gradlew bootRun
