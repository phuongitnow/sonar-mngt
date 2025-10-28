# Hướng dẫn sử dụng SonarQube Admin Application

## 📱 Truy cập ứng dụng

### 1. Mở trình duyệt

Truy cập URL: **http://localhost:4200**

### 2. Giao diện Dashboard

Bạn sẽ thấy giao diện với các thành phần sau:

#### Header
- **Title**: SonarQube Administration
- **Subtitle**: Manage projects and keep LOC under 1,000,000

#### Các nút chức năng
- 🔄 **Refresh**: Làm mới dữ liệu statistics
- 📥 **Sync Projects**: Đồng bộ projects từ SonarQube
- 🧹 **Execute Cleanup**: Chạy cleanup thủ công

#### Statistics Cards (4 cards)
1. **Total Projects**: Số lượng project hiện tại
2. **Lines of Code**: Tổng số dòng code
3. **License Limit (LOC)**: 1,000,000 (giới hạn license)
4. **Projects to Delete**: Số project sẽ bị xóa

#### License Usage
- Thanh progress bar hiển thị % sử dụng license
- Thông tin text hiển thị tỉ lệ phần trăm

#### Last Cleanup Result (hiển thị sau khi cleanup)
- Status: Trạng thái cleanup
- Projects Deleted: Số project đã xóa
- LOC Deleted: Số dòng code đã xóa
- LOC Remaining: Số dòng code còn lại
- Message: Thông báo kết quả

---

## 🧪 Test các chức năng

### Test 1: Kiểm tra Health của Backend

Trước khi test UI, kiểm tra backend:

```bash
curl http://localhost:6996/actuator/health
```

**Kết quả mong đợi:**
```json
{"status":"UP"}
```

### Test 2: Xem Statistics (Get Statistics API)

```bash
curl http://localhost:6996/api/statistics
```

**Kết quả mong đợi:**
```json
{
  "totalProjects": 0,
  "totalLinesOfCode": 0,
  "maxLinesOfCode": 1000000,
  "percentageUsage": 0.0,
  "projectsToDelete": 0
}
```

### Test 3: Refresh Statistics trên UI

1. Click nút **🔄 Refresh** trên UI
2. Kiểm tra các số liệu hiển thị
3. Xem License Usage progress bar

**Kết quả:** Thông tin được cập nhật từ database

### Test 4: Sync Projects từ SonarQube

⚠️ **Lưu ý:** Cần cấu hình SonarQube URL và credentials trong file `.env`

#### Cách 1: Qua UI
1. Click nút **📥 Sync Projects**
2. Đợi quá trình sync hoàn tất
3. Click **🔄 Refresh** để xem kết quả

#### Cách 2: Qua API
```bash
curl -X POST http://localhost:6996/api/projects/sync
```

**Kết quả:** Projects từ SonarQube được sync vào database

### Test 5: Xem danh sách Projects

```bash
curl http://localhost:6996/api/projects
```

**Kết quả mong đợi:**
```json
[
  {
    "projectKey": "my-project",
    "projectName": "My Project",
    "linesOfCode": 15000,
    "lastScanDate": "2024-01-15T10:30:00",
    "createdAt": "2024-01-10T08:00:00"
  }
]
```

### Test 6: Execute Cleanup thủ công

⚠️ **CẢNH BÁO:** Cleanup sẽ xóa các project quá 14 ngày chưa scan để giữ LOC dưới 1,000,000

#### Cách 1: Qua UI
1. Click nút **🧹 Execute Cleanup**
2. Confirm dialog "Are you sure..."
3. Đợi quá trình cleanup hoàn tất
4. Xem kết quả trong "Last Cleanup Result"

#### Cách 2: Qua API
```bash
curl -X POST http://localhost:6996/api/cleanup/execute
```

**Kết quả mong đợi:**
```json
{
  "projectsDeleted": 5,
  "linesOfCodeDeleted": 75000,
  "linesOfCodeRemaining": 925000,
  "status": "COMPLETED",
  "message": "Successfully deleted 5 projects. Remaining LOC: 925000"
}
```

### Test 7: Kiểm tra Swagger UI

Truy cập: **http://localhost:6996/swagger-ui.html**

Bạn sẽ thấy:
- Danh sách tất cả API endpoints
- Có thể test API trực tiếp trên Swagger UI
- Thông tin chi tiết về request/response

**Các APIs có sẵn:**
- `GET /api/statistics` - Lấy statistics
- `POST /api/cleanup/execute` - Chạy cleanup
- `GET /api/projects` - Lấy danh sách projects
- `POST /api/projects/sync` - Sync projects

---

## 🔍 Debug và Troubleshooting

### Xem logs của các services

```bash
# Xem logs của tất cả services
docker compose logs -f

# Xem logs của backend
docker compose logs -f backend

# Xem logs của frontend
docker compose logs -f frontend

# Xem logs của database
docker compose logs -f postgres
```

### Kiểm tra database

```bash
# Kết nối vào database
docker exec -it sonarqube-admin-db psql -U admin -d sonarqube_admin

# Xem bảng project_snapshots
SELECT * FROM project_snapshots;

# Xem cleanup history
SELECT * FROM cleanup_history ORDER BY executed_at DESC LIMIT 10;

# Thoát
\q
```

### Kiểm tra network

```bash
# Kiểm tra containers đang chạy
docker compose ps

# Kiểm tra network
docker network inspect sonarqube_app-network
```

### Test API với curl

```bash
# Health check
curl http://localhost:6996/actuator/health

# Get statistics
curl -X GET http://localhost:6996/api/statistics | jq

# Get projects
curl -X GET http://localhost:6996/api/projects | jq

# Execute cleanup
curl -X POST http://localhost:6996/api/cleanup/execute | jq

# Sync projects
curl -X POST http://localhost:6996/api/projects/sync
```

### Lỗi thường gặp

#### Lỗi 1: "Failed to load statistics"
**Nguyên nhân:** Backend chưa start hoặc không kết nối được

**Giải pháp:**
```bash
# Kiểm tra backend
docker compose logs backend

# Restart backend
docker compose restart backend
```

#### Lỗi 2: "Connection refused"
**Nguyên nhân:** Service chưa chạy hoặc port bị conflict

**Giải pháp:**
```bash
# Kiểm tra services
docker compose ps

# Xem port đang sử dụng
netstat -tuln | grep -E '4200|6996|2345'
```

#### Lỗi 3: Frontend không hiển thị data
**Nguyên nhân:** CORS hoặc API không response

**Giải pháp:**
1. Mở Developer Tools (F12)
2. Xem tab Network
3. Kiểm tra các request API
4. Xem lỗi trong Console

---

## 📊 Kiểm tra trạng thái hệ thống

### 1. Kiểm tra tất cả services

```bash
docker compose ps
```

**Output mong đợi:**
```
NAME                   STATUS
sonarqube-admin-db     Up (healthy)
sonarqube-admin-api    Up (healthy)
sonarqube-admin-ui     Up
```

### 2. Kiểm tra port

```bash
# Database
nc -zv localhost 2345

# API
nc -zv localhost 6996

# Frontend
nc -zv localhost 4200
```

### 3. Kiểm tra URL

Mở trình duyệt và truy cập:
- ✅ **Frontend**: http://localhost:4200
- ✅ **API Health**: http://localhost:6996/actuator/health
- ✅ **Swagger**: http://localhost:6996/swagger-ui.html

---

## 🎯 Checklist test hoàn chỉnh

- [ ] Truy cập được UI tại http://localhost:4200
- [ ] Click Refresh button và xem được statistics
- [ ] Click Sync Projects (nếu có SonarQube config)
- [ ] Kiểm tra Swagger UI tại http://localhost:6996/swagger-ui.html
- [ ] Test API qua curl hoặc Swagger
- [ ] Xem cleanup history (nếu đã chạy cleanup)
- [ ] Xem logs của các services
- [ ] Kiểm tra database có data

---

## 💡 Tips

1. **Sử dụng jq để format JSON:**
   ```bash
   curl http://localhost:6996/api/statistics | jq
   ```

2. **Theo dõi logs real-time:**
   ```bash
   docker compose logs -f backend
   ```

3. **Rebuild containers sau khi sửa code:**
   ```bash
   docker compose up -d --build
   ```

4. **Xóa tất cả và start lại:**
   ```bash
   docker compose down -v
   docker compose up -d
   ```

---

## 🎉 Hoàn thành!

Bây giờ bạn đã biết cách:
- ✅ Truy cập UI và xem dashboard
- ✅ Test các chức năng của ứng dụng
- ✅ Debug và troubleshoot
- ✅ Kiểm tra trạng thái hệ thống

Chúc bạn test thành công! 🚀

