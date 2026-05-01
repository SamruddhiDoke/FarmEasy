# FarmEasy Backend Service

The backend module acts as the secure API gateway for the FarmEasy ecosystem, constructed using Java 17 and Spring Boot 3.2.2.

## Technical Specifications

- **Security & CORS Integration:** Configured a secure JWT filter chain and established robust `CorsConfiguration` utilizing `allowedOriginPatterns` to resolve pre-flight `OPTIONS` requests during Cloud Run deployment routing.
- **Transactional Consistency:** `AgreementService` encapsulates logic under `@Transactional` boundaries. Upon agreement finalization, the system issues a `200 OK`, immediately sends an automated PDF via `JavaMailSender`, and securely updates the associated JPA entity (`Equipment` or `Land`) status to "Rented".
- **Global Error Handling:** Implemented a centralized `@ControllerAdvice` (`GlobalExceptionHandler`) to capture and securely format unexpected runtime exceptions (500 Internal Server Error) and invalid arguments (400 Bad Request), preventing stack-trace leaks during evaluation.
- **Deployment Mechanics:** Shipped with a multi-stage Maven/Alpine Dockerfile optimized for containerized environments like Google Cloud Run. Includes environment variable fallbacks for dynamic Cloud SQL injection (`SPRING_DATASOURCE_URL`).
