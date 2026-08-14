```markdown
# PhotosApp - Personal Media Manager

A full-stack web application for managing personal photos and videos, built with **Java, Spring Boot, PostgreSQL**.

**Live Deployment:** [https://photosapp-0kg0.onrender.com/]

## Overview

PhotosApp allows users to create an account, upload images and videos, view them in a private gallery, download files, and delete media. The application demonstrates core backend engineering skills: REST API design, relational database modeling, session-based authentication, server-side rendering, and containerized deployment.

## Features

- User registration and login with session-based authentication
- Upload photos (JPEG, PNG) and videos (MP4)
- Personal gallery displaying only the user's own media
- Download and delete media with ownership validation
- Click-to-view full-size images and video playback
- Responsive, minimalist interface
- Docker containerization for consistent deployment
- Secure file access: media is not accessible after logout (401)

## Technology Stack

- **Backend:** Java 17, Spring Boot 3.4.5, Spring MVC, Spring Data JPA, Hibernate
- **Frontend:** HTML5, CSS3, JavaScript
- **Database:** PostgreSQL (via JPA and JDBC)
- **Build Tool:** Maven
- **Deployment:** Render (cloud), Docker, Neon (cloud PostgreSQL), Supabase (Media storage)
- **Version Control:** Git, GitHub

## Project Structure

src/main/java/uz/aytjanov/googlephotosclone/
├── model/          // JPA entities (User, Photo/Media)
├── repository/     // Spring Data repositories
├── service/        // Business logic layer
├── web/            // Spring MVC controllers
└── config/         // Application configuration

src/main/resources/
├── templates/      
├── static/         // CSS, JS, (login, register, gallery, upload)
└── application.properties

## Local Development

### Prerequisites
- Java 17 or later
- Maven
- PostgreSQL database (or use the provided H2 in-memory database for testing)

### Setup
1. Clone the repository:
   git clone https://github.com/asadbekaytjanov/photosapp.git
   cd photosapp
2. Configure the database connection in `src/main/resources/application.properties` (or set environment variables). Default configuration expects environment variables:
   - `DATABASE_URL` (JDBC format, e.g., `jdbc:postgresql://host:port/dbname`)
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`
3. Build and run:
   mvn clean package -DskipTests
   java -jar target/*.jar
4. Open `http://localhost:8080` in your browser.

### Running with Docker
docker build -t photosapp .
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://... \
  -e DATABASE_USERNAME=... \
  -e DATABASE_PASSWORD=... \
  photosapp

## Deployment

The application is deployed on [Render](https://render.com) using the Dockerfile in the repository. The PostgreSQL database is hosted on [Neon](https://neon.tech). Environment variables for the database are configured in the Render dashboard.

## Roadmap

- **v1.0.0** – Core MVP: registration, upload, gallery, delete, session authentication, deployment
- **v1.1.0** – File validation, improved error feedback
- **v1.1.1** – Pagination and simple search
- **v1.2.0** *(planned)* – Spring Security + JWT authentication, unit and integration tests

## Author

**Asadbek Aytjanov**
- [LinkedIn](https://www.linkedin.com/in/aytjanov/)
- [GitHub](https://github.com/asadbekaytjanov)

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
