# DropNest — Secure File Sharing

DropNest is a full-stack web application for secure personal file storage and sharing.  
Users can register, upload files, manage their library, and download files through authenticated access.

**Live Demo:** https://dropnest-eae7.onrender.com/

---

## What DropNest Does

DropNest focuses on a simple and reliable file workflow:

- Create account and log in
- Upload files to private cloud storage
- Browse your files with search, filtering, sorting, and pagination
- Download files securely
- Delete files with ownership checks
- Clean, responsive UI for desktop and mobile

---

## Core Features

- **Authentication:** JWT-based login flow
- **Private storage:** Supabase Storage (private bucket)
- **Ownership enforcement:** Users can only access their own files
- **File management:** Upload, list, download, delete
- **UX enhancements:** Upload progress, drag-and-drop upload, retry-friendly states
- **Production deployment:** Dockerized app on Render + Neon PostgreSQL

---

## Tech Stack

- **Backend:** Java 17, Spring Boot, Spring Web, Spring Data JPA, Spring Security
- **Frontend:** HTML, CSS, Vanilla JavaScript
- **Database:** PostgreSQL (Neon)
- **Object Storage:** Supabase Storage
- **Build Tool:** Maven
- **Deployment:** Docker, Render
- **Version Control:** Git, GitHub

---

## Architecture

- **API Layer:** REST controllers for auth and file operations
- **Service Layer:** Business rules (validation, ownership, storage integration)
- **Persistence Layer:** JPA entities and repositories (users, file metadata)
- **Storage Layer:** Supabase object storage for file binaries

---

## API Overview

> Base URL: `/api`

### Auth
- `POST /api/auth/signup` — create account
- `POST /api/auth/login` — authenticate and receive JWT

### Files
- `GET /api/files` — list user files (search + pagination)
- `POST /api/files` — upload file
- `GET /api/files/{id}/download` — download file
- `DELETE /api/files/{id}` — delete file (DB + storage object)

---

## Local Setup

### Prerequisites
- Java 17+
- Maven
- PostgreSQL (or Neon connection)
- Supabase project with Storage enabled

### Environment Variables

Set these before running:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `SUPABASE_URL`
- `SUPABASE_KEY`

### Run Locally

```bash
git clone https://github.com/asadbekaytjanov/photosapp.git
cd photosapp
mvn clean package -DskipTests
java -jar target/*.jar
```

Open: `http://localhost:8080`

---

## Docker

```bash
docker build -t dropnest .
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://... \
  -e DATABASE_USERNAME=... \
  -e DATABASE_PASSWORD=... \
  -e JWT_SECRET=... \
  -e SUPABASE_URL=... \
  -e SUPABASE_KEY=... \
  dropnest
```

---

## Security Notes

- File endpoints are protected with JWT authentication.
- File access is ownership-validated on the backend.
- Supabase bucket is private; files are fetched through authorized backend calls.

---

## Roadmap

- Shareable file links with token/expiration
- Revoke link support
- Improved audit logging
- Automated tests (unit + integration)
- Optional antivirus scanning pipeline

---

## Author

**Asadbek Aytjanov**
- LinkedIn: https://www.linkedin.com/in/aytjanov/
- GitHub: https://github.com/asadbekaytjanov

---

## License

Licensed under the MIT License. See `LICENSE`.