# BenchPilot

Fresh multi-tenant repair shop application built with Spring Boot, Thymeleaf, PostgreSQL, and tenant-first URL routing.

## Phase 1 included

- public shop onboarding at `/shops/new`
- slug-based tenant login at `/{shopSlug}/login`
- root fail-closed routing behavior
- shop-rooted domain model and repositories
- multi-tenant security foundation with cross-tenant URL blocking
- initial protected dashboard landing page

## Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 15+

## Local run

1. Create a PostgreSQL database named `benchpilot`.
2. Set environment variables if you are not using the defaults:
   - `APP_DATASOURCE_URL`
   - `APP_DATASOURCE_USERNAME`
   - `APP_DATASOURCE_PASSWORD`
3. Start the app with `mvn spring-boot:run`.
4. Open `http://localhost:8080/`.
