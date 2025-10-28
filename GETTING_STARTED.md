# Hướng dẫn Getting Started - SonarQube Admin Application

## 📋 Yêu cầu hệ thống

- Docker 20.10 trở lên
- Docker Compose 2.0 trở lên
- Quyền truy cập SonarQube server (hoặc chạy SonarQube local)

## 🚀 Các bước thiết lập

### Bước 1: Kiểm tra Docker

```bash
# Kiểm tra Docker đã cài đặt
docker --version

# Kiểm tra Docker Compose
docker-compose --version

# Đảm bảo Docker daemon đang chạy
docker ps
```

### Bước 2: Cấu hình môi trường

Tạo file `.env` từ template:

```bash
cp env.example .env
```

Chỉnh sửa file `.env` với thông tin của bạn:

```env
# SonarQube Configuration
SONARQUBE_URL=http://sonarqube:9000
SONARQUBE_TOKEN=your_sonarqube_token_here
SONARQUBE_USERNAME=admin
SONARQUBE_PASSWORD=admin

# Email Configuration (Optional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
NOTIFICATION_EMAIL=admin@example.com

# Cleanup Configuration
DAYS_THRESHOLD=14
CLEANUP_CRON=0 0 2 * * *
```

**Lưu ý:**
- Nếu SonarQube chạy trên host khác, thay đổi `SONARQUBE_URL`
- Để tạo SonarQube token: Login vào SonarQube → My Account → Security → Generate Token
- Nếu không cần email, có thể để trống các thông tin MAIL_*

### Bước 3: Build và khởi chạy ứng dụng

```bash
# Build images và start containers
docker-compose up -d

# Xem logs để theo dõi quá trình khởi động
docker-compose logs -f
```

**Thời gian khởi động:**
- PostgreSQL: ~5-10 giây
- Backend API: ~30-60 giây (tùy theo CPU)
- Frontend: ~10-15 giây

### Bước 4: Kiểm tra services đang chạy

```bash
# Kiểm tra status của tất cả containers
docker-compose ps

# Kết quả mong đợi:
# NAME                     COMMAND                  STATUS
# sonarqube-admin-db       "docker-entrypoint.s…"   Up
# sonarqube-admin-api      "java -jar app.jar"      Up
# sonarqube-admin-ui       "/docker-entrypoint.…"   Up
```

### Bước 5: Kiểm tra health của API

```bash
# Kiểm tra API health endpoint
curl http://localhost:6996/actuator/health

# Kết quả mong đợi:
# {"status":"UP"}
```

### Bước 6: Truy cập ứng dụng

Mở trình duyệt và truy cập:

- **🎨 Frontend Dashboard**: http://localhost:4200
- **🔧 API Base URL**: http://localhost:6996
- **📚 Swagger UI**: http://localhost:6996/swagger-ui.html
- **❤️ Health Check**: http://localhost:6996/actuator/health

## 📊 Sử dụng ứng dụng

### Dashboard Overview

Khi mở Frontend (http://localhost:4200), bạn sẽ thấy:

1. **Total Projects**: Số lượng project hiện tại trong SonarQube
2. **Lines of Code**: Tổng số dòng code hiện tại
3. **License Limit**: Giới hạn 1,000,000 LOC của license
4. **Projects to Delete**: Số project sẽ bị xóa (quá 14 ngày chưa scan)
5. **License Usage**: Thanh progress bar hiển thị % đã sử dụng

### Các chức năng chính

#### 1. Sync Projects từ SonarQube
```bash
# Click nút "📥 Sync Projects" trên UI
# Hoặc qua API:
curl -X POST http://localhost:6996/api/projects/sync
```

#### 2. Xem Statistics
```bash
# Click nút "🔄 Refresh" trên UI
# Hoặc qua API:
curl http://localhost:6996/api/statistics
```

#### 3. Chạy Cleanup thủ công
```bash
# Click nút "🧹 Execute Cleanup" trên UI
# Hoặc qua API:
curl -X POST http://localhost:6996/api/cleanup/execute
```

**⚠️ Cảnh báo:** Cleanup sẽ xóa các project quá 14 ngày chưa scan để giữ LOC dưới 1,000,000.

#### 4. Xem tất cả Projects
```bash
curl http://localhost:6996/api/projects
```

## 🔧 Troubleshooting

### Vấn đề 1: Container không start

```bash
# Xem logs chi tiết
docker-compose logs backend

# Restart container
docker-compose restart backend
```

### Vấn đề 2: Database connection failed

```bash
# Kiểm tra PostgreSQL đang chạy
docker-compose ps postgres

# Xem logs của database
docker-compose logs postgres

# Nếu database chưa sẵn sàng, đợi thêm
docker-compose up -d postgres
```

### Vấn đề 3: API trả về 404

```bash
# Kiểm tra API đã start chưa
curl http://localhost:6996/actuator/health

# Nếu không response, check logs
docker-compose logs backend

# Rebuild backend
docker-compose up -d --build backend
```

### Vấn đề 4: Frontend không kết nối được API

```bash
# Kiểm tra backend đang chạy
curl http://localhost:6996/actuator/health

# Rebuild frontend
docker-compose up -d --build frontend
```

### Vấn đề 5: SonarQube connection failed

```bash
# Kiểm tra SonarQube URL
docker-compose logs backend | grep sonarqube

# Sửa SONARQUBE_URL trong .env nếu cần
# Sau đó restart
docker-compose restart backend
```

## 🛠️ Các lệnh hữu ích

```bash
# Xem logs real-time
docker-compose logs -f

# Stop tất cả services
docker-compose down

# Stop và xóa volumes (⚠️ Xóa dữ liệu database)
docker-compose down -v

# Rebuild specific service
docker-compose up -d --build backend

# Xem resource usage
docker stats

# Vào trong container để debug
docker exec -it sonarqube-admin-api bash
docker exec -it sonarqube-admin-db psql -U admin -d sonarqube_admin
```

## 📅 Scheduled Cleanup

Cleanup tự động chạy theo cron job. Mặc định là **2:00 AM mỗi ngày**.

Để thay đổi lịch, sửa trong `.env`:
```env
# Cleanup mỗi 6 giờ
CLEANUP_CRON=0 0 */6 * * *

# Cleanup mỗi 12 giờ
CLEANUP_CRON=0 0 */12 * * *

# Cleanup thứ 2 mỗi tuần lúc 3 AM
CLEANUP_CRON=0 3 * * 1
```

Format: `giây phút giờ ngày tháng thứ-trong-tuần`

## 🔒 Security Notes

1. **Production**: Thêm authentication cho API (hiện tại permitAll cho development)
2. **Database**: Đổi password mặc định trong `.env`
3. **SonarQube Token**: Sử dụng token thay vì username/password khi có thể
4. **Firewall**: Chỉ expose ports cần thiết

## 📧 Email Notifications

Để nhận email thông báo sau mỗi lần cleanup:

1. Cấu hình SMTP trong `.env`
2. Đối với Gmail, tạo [App Password](https://myaccount.google.com/apppasswords)
3. Lưu ý: Email chỉ gửi khi cleanup thực sự xóa projects

## 🆘 Cần hỗ trợ?

Nếu gặp vấn đề, check logs:
```bash
# Backend logs
docker-compose logs backend

# Frontend logs  
docker-compose logs frontend

# Database logs
docker-compose logs postgres
```

## 🎉 Hoàn thành!

Bây giờ bạn đã sẵn sàng sử dụng SonarQube Admin Application!

**Next Steps:**
- Sync projects từ SonarQube
- Theo dõi LOC usage
- Setup cleanup schedule theo nhu cầu
- Cấu hình email notifications

