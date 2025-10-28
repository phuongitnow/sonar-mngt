# SonarQube Admin Application

A containerized application for managing SonarQube projects and automatically cleaning up old projects to stay within license limits.

## Features

- **Automatic Cleanup**: Scheduled task to delete projects older than 14 days
- **License Management**: Ensures total lines of code (LOC) stays under 1,000,000
- **Email Notifications**: Sends cleanup results via email
- **Statistics Dashboard**: View current LOC usage and project statistics
- **Manual Trigger**: Execute cleanup process manually from the UI
- **Real-time Sync**: Synchronize projects from SonarQube

## Architecture

The application follows Clean Architecture principles with three main layers:

- **Presentation Layer**: Angular UI
- **Application Layer**: Business logic and services
- **Infrastructure Layer**: Database, external integrations, configuration
- **Domain Layer**: Entities and repositories

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- PostgreSQL
- Liquibase
- Docker

### Frontend
- Angular 17
- TypeScript 5.2
- Nginx

## Getting Started

> 📖 **Xem hướng dẫn chi tiết**: [GETTING_STARTED.md](GETTING_STARTED.md)

### Quick Start

1. **Tạo file cấu hình**:
   ```bash
   cp env.example .env
   # Chỉnh sửa .env với thông tin của bạn
   ```

2. **Khởi chạy ứng dụng**:
   ```bash
   docker-compose up -d
   ```

3. **Truy cập ứng dụng**:
   - Frontend: http://localhost:4200
   - API: http://localhost:6996
   - Swagger: http://localhost:6996/swagger-ui.html

### Prerequisites

- Docker 20.10+ và Docker Compose 2.0+
- Truy cập SonarQube server

### Configuration

Tạo file `.env` từ template:

```env
# SonarQube Configuration
SONARQUBE_URL=http://sonarqube:9000
SONARQUBE_TOKEN=your_sonarqube_token
SONARQUBE_USERNAME=admin
SONARQUBE_PASSWORD=admin

# Email Configuration (for notifications)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
NOTIFICATION_EMAIL=admin@example.com

# Cleanup Configuration
DAYS_THRESHOLD=14
CLEANUP_CRON=0 0 2 * * *
```

### Running the Application

1. Clone the repository
2. Update environment variables in `.env` file
3. Run with Docker Compose:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 2345
- Spring Boot API on port 6996
- Angular frontend on port 4200

### Access the Application

- **Frontend**: http://localhost:4200
- **API**: http://localhost:6996
- **Swagger UI**: http://localhost:6996/swagger-ui.html
- **Actuator**: http://localhost:6996/actuator/health

### API Endpoints

#### Statistics
- `GET /api/statistics` - Get current statistics

#### Cleanup
- `POST /api/cleanup/execute` - Execute cleanup manually

#### Projects
- `GET /api/projects` - Get all projects
- `POST /api/projects/sync` - Sync projects from SonarQube

## Database Schema

### project_snapshots
Stores information about SonarQube projects.

### cleanup_history
Tracks cleanup operations and results.

## Scheduled Tasks

The cleanup task runs daily at 2 AM by default. Configure via `CLEANUP_CRON` environment variable.

## Security

- OWASP guidelines followed
- Input validation
- Global exception handling
- CORS configuration

## Development

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
```

## Testing

Run unit tests:

```bash
cd backend
mvn test
```

## License

Internal use only.

