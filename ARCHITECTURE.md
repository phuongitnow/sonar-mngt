# Tài liệu Kiến trúc Ứng dụng - SonarQube Admin Application

## 📋 Mục lục

1. [Tổng quan](#tổng-quan)
2. [Kiến trúc tổng thể](#kiến-trúc-tổng-thể)
3. [Kiến trúc Backend](#kiến-trúc-backend)
4. [Kiến trúc Frontend](#kiến-trúc-frontend)
5. [Kiến trúc Deployment](#kiến-trúc-deployment)
6. [Luồng xử lý dữ liệu](#luồng-xử-lý-dữ-liệu)
7. [Công nghệ sử dụng](#công-nghệ-sử-dụng)
8. [Bảo mật](#bảo-mật)

---

## Tổng quan

SonarQube Admin Application là một ứng dụng quản lý dự án SonarQube, tự động dọn dẹp các dự án cũ để duy trì giới hạn license. Ứng dụng được xây dựng theo nguyên tắc **Clean Architecture** với kiến trúc phân lớp rõ ràng.

### Mục đích

- Quản lý và theo dõi các dự án SonarQube
- Tự động dọn dẹp các dự án cũ (quá 14 ngày chưa scan)
- Đảm bảo tổng số dòng code (LOC) dưới 1,000,000
- Gửi thông báo email sau mỗi lần cleanup
- Cung cấp dashboard thống kê real-time

---

## Kiến trúc tổng thể

### Sơ đồ kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend Layer                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Angular 17 Application (TypeScript)                │  │
│  │  - Components, Services, Routing                     │  │
│  │  - HTTP Client (REST API calls)                     │  │
│  └──────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTP/REST
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                     Backend Layer                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Presentation Layer (Controllers)                    │  │
│  │  - StatisticsController                             │  │
│  │  - CleanupController                                │  │
│  │  - ProjectController                                │  │
│  └───────────────────────┬──────────────────────────────┘  │
│                          │                                  │
│  ┌───────────────────────▼──────────────────────────────┐  │
│  │  Application Layer (Services)                        │  │
│  │  - CleanupService                                    │  │
│  │  - SonarQubeService (Interface)                     │  │
│  └───────────────────────┬──────────────────────────────┘  │
│                          │                                  │
│  ┌───────────────────────▼──────────────────────────────┐  │
│  │  Domain Layer                                         │  │
│  │  - Entities (ProjectSnapshot, CleanupHistory)        │  │
│  │  - Repositories (Interfaces)                         │  │
│  └───────────────────────┬──────────────────────────────┘  │
│                          │                                  │
│  ┌───────────────────────▼──────────────────────────────┐  │
│  │  Infrastructure Layer                                │  │
│  │  - SonarQubeServiceImpl                              │  │
│  │  - EmailService, EmailServiceImpl                    │  │
│  │  - CleanupScheduler                                  │  │
│  │  - Config (Security, Web, Swagger)                   │  │
│  │  - Exception Handling                                │  │
│  └───────────────────────┬──────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼──────┐  ┌────────▼────────┐  ┌─────▼──────┐
│  PostgreSQL  │  │   SonarQube     │  │   SMTP    │
│  Database    │  │   Server        │  │   Server  │
└──────────────┘  └─────────────────┘  └───────────┘
```

---

## Kiến trúc Backend

### Clean Architecture - 4 Layers

Ứng dụng backend được tổ chức theo **Clean Architecture** với 4 lớp chính:

#### 1. Presentation Layer (Lớp trình bày)

**Vị trí**: `com.sonarqube.adminapp.presentation`

**Trách nhiệm**:
- Xử lý HTTP requests/responses
- Validation đầu vào
- Mapping DTOs
- API documentation (Swagger)

**Components**:
- `StatisticsController`: API thống kê
  - `GET /api/statistics` - Lấy thống kê hiện tại
- `CleanupController`: API cleanup
  - `POST /api/cleanup/execute` - Thực thi cleanup thủ công
- `ProjectController`: API quản lý dự án
  - `GET /api/projects` - Lấy tất cả dự án
  - `POST /api/projects/sync` - Đồng bộ dự án từ SonarQube

**Dependencies**: Chỉ phụ thuộc vào Application Layer

#### 2. Application Layer (Lớp ứng dụng)

**Vị trí**: `com.sonarqube.adminapp.application`

**Trách nhiệm**:
- Business logic chính
- Orchestration các use cases
- DTOs (Data Transfer Objects)
- Transaction management

**Components**:
- `CleanupService`: 
  - `performCleanup()`: Thực thi quá trình cleanup
  - `getStatistics()`: Tính toán thống kê
- `SonarQubeService` (Interface): Định nghĩa contract cho SonarQube operations
- DTOs:
  - `StatisticsDTO`: Thống kê tổng quan
  - `CleanupResultDTO`: Kết quả cleanup
  - `ProjectDTO`: Thông tin dự án

**Dependencies**: Domain Layer, Infrastructure Layer (qua interfaces)

#### 3. Domain Layer (Lớp domain)

**Vị trí**: `com.sonarqube.adminapp.domain`

**Trách nhiệm**:
- Domain entities (business objects)
- Repository interfaces
- Business rules cốt lõi

**Components**:
- **Entities**:
  - `ProjectSnapshot`: Snapshot của dự án SonarQube
    - `id`, `projectKey`, `projectName`
    - `linesOfCode`, `lastScanDate`
    - `createdAt`, `updatedAt`
  - `CleanupHistory`: Lịch sử cleanup operations
    - `id`, `executedAt`
    - `projectsDeleted`, `linesOfCodeDeleted`
    - `linesOfCodeRemaining`, `status`, `errorMessage`

- **Repositories** (Interfaces):
  - `ProjectSnapshotRepository`: CRUD operations cho ProjectSnapshot
    - `findByProjectKey()`
    - `findProjectsOlderThan()`
    - `getTotalLinesOfCode()`
  - `CleanupHistoryRepository`: CRUD operations cho CleanupHistory
    - `findTop10ByOrderByExecutedAtDesc()`

**Dependencies**: Không phụ thuộc vào layer khác (pure business logic)

#### 4. Infrastructure Layer (Lớp hạ tầng)

**Vị trí**: `com.sonarqube.adminapp.infrastructure`

**Trách nhiệm**:
- Implementations của external services
- Database access (JPA implementations)
- External API integrations
- Configuration
- Cross-cutting concerns

**Components**:
- **SonarQube Integration**:
  - `SonarQubeServiceImpl`: Implementation của SonarQubeService
    - Giao tiếp với SonarQube REST API
    - Authentication (Token hoặc Basic Auth)
    - Fetch projects, LOC, last scan date
    - Delete projects

- **Email Service**:
  - `EmailService` (Interface)
  - `EmailServiceImpl`: Gửi email thông báo cleanup

- **Scheduler**:
  - `CleanupScheduler`: Scheduled task chạy cleanup theo cron
    - Mặc định: `0 0 2 * * *` (2:00 AM mỗi ngày)

- **Configuration**:
  - `SecurityConfig`: Spring Security configuration
  - `WebConfig`: CORS configuration
  - `SwaggerConfig`: API documentation

- **Exception Handling**:
  - `GlobalExceptionHandler`: Xử lý exceptions toàn cục

---

## Kiến trúc Frontend

### Angular 17 Architecture

**Vị trí**: `frontend/src/app`

**Cấu trúc**:
```
frontend/
├── src/
│   ├── app/
│   │   ├── app.component.ts      # Root component
│   │   ├── app.component.html    # Template
│   │   ├── app.component.css     # Styles
│   │   ├── app.service.ts        # HTTP service
│   │   ├── app.routes.ts         # Routing configuration
│   │   └── main.ts               # Bootstrap
│   ├── index.html
│   └── styles.css
```

**Components**:
- `AppComponent`: Component chính
  - Hiển thị statistics dashboard
  - Buttons: Sync Projects, Execute Cleanup, Refresh
  - Real-time updates

**Services**:
- `AppService`: HTTP client service
  - `getStatistics()`: GET /api/statistics
  - `executeCleanup()`: POST /api/cleanup/execute
  - `getProjects()`: GET /api/projects
  - `syncProjects()`: POST /api/projects/sync

**Features**:
- Standalone components (Angular 17)
- Reactive programming với RxJS
- HTTP interceptors (có thể mở rộng)
- Error handling

---

## Kiến trúc Deployment

### Docker Compose Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Docker Compose Network                     │
│              (app-network)                              │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │  Frontend    │  │   Backend    │  │  PostgreSQL  │ │
│  │  Container   │  │   Container  │  │  Container   │ │
│  │              │  │              │  │              │ │
│  │  Nginx       │  │  Spring Boot │  │  PostgreSQL  │ │
│  │  Port: 4200  │  │  Port: 6996 │  │  Port: 2345  │ │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘ │
│         │                  │                  │         │
│         └──────────────────┼──────────────────┘         │
│                            │                            │
└────────────────────────────┼────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │  SonarQube      │
                    │  (External)     │
                    └─────────────────┘
```

### Container Details

1. **Frontend Container** (`sonarqube-admin-ui`)
   - Base: `nginx:alpine`
   - Build: Angular production build → Nginx static files
   - Port: `4200:80`
   - Depends on: Backend

2. **Backend Container** (`sonarqube-admin-api`)
   - Base: `openjdk:17-jdk-slim`
   - Build: Maven build → JAR file
   - Port: `6996:6996`
   - Depends on: PostgreSQL (health check)
   - Environment variables: Database, SonarQube, Email config

3. **PostgreSQL Container** (`sonarqube-admin-db`)
   - Base: `postgres:15-alpine`
   - Port: `2345:5432`
   - Volume: `postgres_data` (persistent storage)
   - Health check: `pg_isready`

### Network Configuration

- **Network**: `app-network` (bridge driver)
- **Service Discovery**: Container names as hostnames
  - Backend → Database: `postgres:5432`
  - Frontend → Backend: `http://localhost:6996` (hoặc backend service name)

---

## Luồng xử lý dữ liệu

### 1. Luồng Sync Projects

```
Frontend                    Backend                    SonarQube
   │                           │                           │
   │  POST /api/projects/sync  │                           │
   ├──────────────────────────>│                           │
   │                           │  GET /api/projects/search │
   │                           ├──────────────────────────>│
   │                           │<──────────────────────────┤
   │                           │  GET /api/measures/...    │
   │                           ├──────────────────────────>│
   │                           │<──────────────────────────┤
   │                           │  GET /api/project_analyses │
   │                           ├──────────────────────────>│
   │                           │<──────────────────────────┤
   │                           │  Save to Database         │
   │                           │  (ProjectSnapshot)        │
   │<──────────────────────────┤                           │
   │  200 OK                   │                           │
```

### 2. Luồng Cleanup Process

```
Scheduler/API                CleanupService              SonarQube
   │                           │                           │
   │  performCleanup()         │                           │
   ├──────────────────────────>│                           │
   │                           │  syncProjects()           │
   │                           ├──────────────────────────>│
   │                           │<──────────────────────────┤
   │                           │  getStatistics()          │
   │                           │  (Check LOC limit)        │
   │                           │                           │
   │                           │  findProjectsOlderThan()  │
   │                           │  (Database query)         │
   │                           │                           │
   │                           │  For each project:        │
   │                           │  DELETE /api/projects/... │
   │                           ├──────────────────────────>│
   │                           │<──────────────────────────┤
   │                           │  Delete from DB           │
   │                           │                           │
   │                           │  syncProjects()           │
   │                           │  (Update statistics)      │
   │                           │                           │
   │                           │  Save CleanupHistory      │
   │                           │                           │
   │                           │  sendEmail()              │
   │                           │  (Notification)           │
   │<──────────────────────────┤                           │
   │  CleanupResultDTO         │                           │
```

### 3. Luồng Get Statistics

```
Frontend                    Backend                    Database
   │                           │                           │
   │  GET /api/statistics      │                           │
   ├──────────────────────────>│                           │
   │                           │  count()                  │
   │                           ├──────────────────────────>│
   │                           │<──────────────────────────┤
   │                           │  getTotalLinesOfCode()    │
   │                           ├──────────────────────────>│
   │                           │<──────────────────────────┤
   │                           │  findProjectsOlderThan()  │
   │                           ├──────────────────────────>│
   │                           │<──────────────────────────┤
   │                           │  Calculate percentage     │
   │                           │  Build StatisticsDTO      │
   │<──────────────────────────┤                           │
   │  StatisticsDTO            │                           │
```

---

## Công nghệ sử dụng

### Backend Stack

| Technology | Version | Mục đích |
|------------|---------|----------|
| Java | 17 | Programming language |
| Spring Boot | 3.2.0 | Framework |
| Spring Data JPA | 3.2.0 | Database access |
| PostgreSQL | 15 | Database |
| Liquibase | 4.25.1 | Database migration |
| Spring Security | 3.2.0 | Security |
| Spring Mail | 3.2.0 | Email |
| OkHttp | 4.12.0 | HTTP client |
| Swagger/OpenAPI | 2.2.0 | API documentation |
| Lombok | - | Code generation |
| Maven | - | Build tool |

### Frontend Stack

| Technology | Version | Mục đích |
|------------|---------|----------|
| Angular | 17.0.0 | Framework |
| TypeScript | 5.2.0 | Programming language |
| RxJS | 7.8.0 | Reactive programming |
| Angular CLI | 17.0.0 | Build tool |
| Nginx | Alpine | Web server |

### DevOps Stack

| Technology | Mục đích |
|------------|----------|
| Docker | Containerization |
| Docker Compose | Orchestration |
| Maven | Backend build |
| npm | Frontend build |

---

## Bảo mật

### Security Measures

1. **Spring Security**
   - Hiện tại: `permitAll()` (development mode)
   - Production: Nên thêm authentication (JWT, OAuth2)

2. **CORS Configuration**
   - Cho phép tất cả origins (development)
   - Production: Chỉ định specific origins

3. **Input Validation**
   - Spring Validation annotations
   - Global exception handling

4. **Database Security**
   - Connection pooling
   - Prepared statements (JPA)
   - SQL injection protection

5. **External API Security**
   - SonarQube: Token-based hoặc Basic Auth
   - HTTPS cho production

### Recommendations

- [ ] Thêm authentication/authorization
- [ ] Restrict CORS origins
- [ ] Enable HTTPS
- [ ] Database encryption
- [ ] Audit logging
- [ ] Rate limiting

---

## Sơ đồ lớp (Class Diagram)

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
├─────────────────────────────────────────────────────────┤
│  StatisticsController                                    │
│  CleanupController                                       │
│  ProjectController                                       │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ uses
                        ▼
┌─────────────────────────────────────────────────────────┐
│                   Application Layer                      │
├─────────────────────────────────────────────────────────┤
│  CleanupService ────────┐                                │
│  SonarQubeService       │                                │
│  (Interface)            │                                │
│                         │                                │
│  StatisticsDTO          │                                │
│  CleanupResultDTO      │                                │
│  ProjectDTO            │                                │
└────────────────────────┼────────────────────────────────┘
                         │
                         │ depends on
                         ▼
┌─────────────────────────────────────────────────────────┐
│                      Domain Layer                        │
├─────────────────────────────────────────────────────────┤
│  ProjectSnapshot                                        │
│  CleanupHistory                                         │
│                                                          │
│  ProjectSnapshotRepository (Interface)                  │
│  CleanupHistoryRepository (Interface)                   │
└────────────────────────┬────────────────────────────────┘
                         │
                         │ implemented by
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                    │
├─────────────────────────────────────────────────────────┤
│  SonarQubeServiceImpl ────► implements SonarQubeService│
│  EmailServiceImpl ────────► implements EmailService    │
│  CleanupScheduler                                        │
│  SecurityConfig                                          │
│  WebConfig                                               │
│  GlobalExceptionHandler                                  │
└─────────────────────────────────────────────────────────┘
```

---

## Kết luận

Ứng dụng SonarQube Admin được thiết kế theo **Clean Architecture** với:

- ✅ **Separation of Concerns**: Mỗi layer có trách nhiệm rõ ràng
- ✅ **Dependency Inversion**: Domain layer không phụ thuộc vào infrastructure
- ✅ **Testability**: Dễ dàng test từng layer độc lập
- ✅ **Maintainability**: Code dễ đọc, dễ bảo trì
- ✅ **Scalability**: Có thể mở rộng dễ dàng

Kiến trúc này đảm bảo ứng dụng có thể phát triển và mở rộng một cách bền vững.


