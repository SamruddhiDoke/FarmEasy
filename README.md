# FarmEasy - Enterprise Cloud Deployment

FarmEasy is a robust, full-stack platform designed for the seamless renting and trading of agricultural equipment and land. This project leverages Spring Boot for a high-performance backend and React.js for a responsive, modern frontend interface.

## System Architecture

- **Backend (Spring Boot):** Provides RESTful APIs, JWT-based authentication, and transactional integrity for all rental and trade agreements. It integrates automated email dispatch mechanisms using `JavaMailSender` to immediately notify parties upon deal closure.
- **Frontend (React.js + Vite):** A secure, localized, and highly responsive user interface designed with WCAG accessibility compliance and modern glassmorphism aesthetics.
- **Deployment Strategy:** The application is fully containerized using multi-stage Docker builds. The infrastructure is configured for Google Cloud Run deployment, utilizing environment-injected database configurations and a reverse-proxy Nginx server for the frontend static assets.

## Core Features
1. **Transactional Rental Modules:** Rental agreements maintain strict ACID properties, immediately updating database states to prevent duplicate transactions.
2. **Dynamic UI/UX Updates:** Real-time visibility restrictions on inventory marked as "Rented", with robust `react-toastify` notifications providing immediate system feedback.
3. **Resilient Security Policies:** Hardened `SecurityConfig` resolving complex CORS pre-flight anomalies, combined with robust error handling mapping to appropriate HTTP standard status codes (e.g., 200 OK, 400 Bad Request, 500 Internal Server Error).
