# Hướng dẫn điền thông tin vào file .env

## 📋 Tạo file .env

Đầu tiên, copy file `env.example` thành `.env`:

```bash
cp env.example .env
```

## 🔧 Thông tin cần điền vào .env

### 1. SonarQube Configuration (BẮT BUỘC)

#### SONARQUBE_URL
- **Mô tả**: URL của SonarQube server
- **Cách lấy**:
  - Nếu SonarQube chạy local: `http://localhost:9000`
  - Nếu SonarQube chạy trên server khác: `http://your-sonarqube-server:9000`
  - Nếu SonarQube chạy trong Docker cùng network: `http://sonarqube:9000`
- **Ví dụ**: 
  ```env
  SONARQUBE_URL=http://sonarqube:9000
  ```
  hoặc
  ```env
  SONARQUBE_URL=http://192.168.1.100:9000
  ```

#### SONARQUBE_TOKEN (QUAN TRỌNG - Nên dùng Token thay vì Username/Password)
- **Mô tả**: Personal Access Token để authenticate với SonarQube API
- **Cách lấy**:
  1. Đăng nhập vào SonarQube Web UI
  2. Click vào avatar (góc trên bên phải) → **My Account**
  3. Chọn tab **Security**
  4. Nhập **Token Name** (ví dụ: `admin-app`)
  5. Click **Generate**
  6. Copy token (chỉ hiển thị một lần!)
  7. Paste vào file .env
- **Ví dụ**:
  ```env
  SONARQUBE_TOKEN=squ_abc123xyz456def789ghi012jkl345mno678pqr901stu234vwx567yza890
  ```

#### SONARQUBE_USERNAME & SONARQUBE_PASSWORD (Tùy chọn - chỉ dùng nếu không có Token)
- **Mô tả**: Username và password của SonarQube (chỉ dùng nếu không có token)
- **Mặc định**: `admin` / `admin`
- **Lưu ý**: Nên dùng Token thay vì username/password vì an toàn hơn
- **Ví dụ**:
  ```env
  SONARQUBE_USERNAME=admin
  SONARQUBE_PASSWORD=admin
  ```

---

### 2. Email Configuration (BẮT BUỘC nếu muốn nhận email notification)

#### MAIL_HOST
- **Mô tả**: SMTP server host
- **Các giá trị phổ biến**:
  - Gmail: `smtp.gmail.com`
  - Outlook: `smtp-mail.outlook.com`
  - Yahoo: `smtp.mail.yahoo.com`
  - Custom SMTP: Nhập host của bạn
- **Ví dụ**:
  ```env
  MAIL_HOST=smtp.gmail.com
  ```

#### MAIL_PORT
- **Mô tả**: SMTP server port
- **Các giá trị phổ biến**:
  - Gmail/Outlook: `587` (TLS) hoặc `465` (SSL)
  - Yahoo: `587` hoặc `465`
- **Ví dụ**:
  ```env
  MAIL_PORT=587
  ```

#### MAIL_USERNAME
- **Mô tả**: Email address để gửi email
- **Ví dụ**:
  ```env
  MAIL_USERNAME=your-email@gmail.com
  ```

#### MAIL_PASSWORD
- **Mô tả**: Password hoặc App Password của email
- **Cách lấy cho Gmail**:
  1. Vào Google Account → **Security**
  2. Bật **2-Step Verification** (nếu chưa bật)
  3. Tìm **App passwords** (Mật khẩu ứng dụng)
  4. Chọn **Mail** và **Other (Custom name)**
  5. Nhập tên: `SonarQube Admin App`
  6. Click **Generate**
  7. Copy password 16 ký tự
  8. Paste vào .env
- **Lưu ý**: 
  - Với Gmail, **KHÔNG** dùng password thông thường, **PHẢI** dùng App Password
  - App Password có 16 ký tự, không có khoảng trắng
- **Ví dụ**:
  ```env
  MAIL_PASSWORD=abcd efgh ijkl mnop
  ```
  (viết liền: `abcdefghijklmnop`)

#### NOTIFICATION_EMAIL
- **Mô tả**: Email nhận thông báo kết quả cleanup
- **Ví dụ**:
  ```env
  NOTIFICATION_EMAIL=admin@company.com
  ```
  hoặc
  ```env
  NOTIFICATION_EMAIL=your-email@gmail.com
  ```

---

### 3. Cleanup Configuration (TÙY CHỌN)

#### DAYS_THRESHOLD
- **Mô tả**: Số ngày không scan thì project sẽ bị xóa
- **Mặc định**: `14` (2 tuần)
- **Ví dụ**:
  ```env
  DAYS_THRESHOLD=14
  ```
  hoặc cho 30 ngày:
  ```env
  DAYS_THRESHOLD=30
  ```

#### CLEANUP_CRON
- **Mô tả**: Lịch chạy cleanup tự động (Cron expression)
- **Format**: `second minute hour day month day-of-week`
- **Mặc định**: `0 0 2 * * *` (2 giờ sáng mỗi ngày)
- **Ví dụ**:
  ```env
  # Chạy lúc 2 giờ sáng mỗi ngày
  CLEANUP_CRON=0 0 2 * * *
  
  # Chạy lúc 3 giờ sáng mỗi ngày
  CLEANUP_CRON=0 0 3 * * *
  
  # Chạy lúc 2 giờ sáng mỗi thứ 2 hàng tuần
  CLEANUP_CRON=0 0 2 * * MON
  
  # Chạy mỗi 12 giờ (12h trưa và 12h đêm)
  CLEANUP_CRON=0 0 */12 * * *
  ```

**Giải thích Cron Expression**:
- `0 0 2 * * *` = Giây:0, Phút:0, Giờ:2, Mọi ngày, Mọi tháng, Mọi thứ trong tuần
- Format: `second minute hour day-of-month month day-of-week`

---

### 4. Database Configuration (TÙY CHỌN - có thể giữ mặc định)

#### DB_USERNAME
- **Mô tả**: Username cho PostgreSQL database
- **Mặc định**: `admin`
- **Ví dụ**:
  ```env
  DB_USERNAME=admin
  ```

#### DB_PASSWORD
- **Mô tả**: Password cho PostgreSQL database
- **Mặc định**: `admin123`
- **Lưu ý**: Nên đổi trong production!
- **Ví dụ**:
  ```env
  DB_PASSWORD=admin123
  ```
  hoặc password mạnh hơn:
  ```env
  DB_PASSWORD=MyStr0ng!P@ssw0rd
  ```

---

## 📝 Ví dụ file .env hoàn chỉnh

```env
# SonarQube Configuration
SONARQUBE_URL=http://sonarqube:9000
SONARQUBE_TOKEN=squ_abc123xyz456def789ghi012jkl345mno678pqr901stu234vwx567yza890
SONARQUBE_USERNAME=admin
SONARQUBE_PASSWORD=admin

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
NOTIFICATION_EMAIL=admin@company.com

# Cleanup Configuration
DAYS_THRESHOLD=14
CLEANUP_CRON=0 0 2 * * *

# Database Configuration
DB_USERNAME=admin
DB_PASSWORD=admin123
```

---

## ✅ Checklist trước khi chạy

- [ ] Đã tạo file `.env` từ `env.example`
- [ ] Đã điền `SONARQUBE_URL` (hoặc đúng URL SonarQube của bạn)
- [ ] Đã tạo và điền `SONARQUBE_TOKEN`
- [ ] Đã điền thông tin email (nếu muốn nhận notification)
  - [ ] `MAIL_HOST` và `MAIL_PORT`
  - [ ] `MAIL_USERNAME` và `MAIL_PASSWORD` (App Password cho Gmail)
  - [ ] `NOTIFICATION_EMAIL`
- [ ] Đã kiểm tra `DAYS_THRESHOLD` và `CLEANUP_CRON` (nếu cần thay đổi)
- [ ] Đã kiểm tra `DB_USERNAME` và `DB_PASSWORD` (nên đổi trong production)

---

## 🔒 Bảo mật

1. **KHÔNG** commit file `.env` lên Git (đã có trong `.gitignore`)
2. **KHÔNG** chia sẻ file `.env` công khai
3. Nên dùng **Token** thay vì Username/Password cho SonarQube
4. Nên dùng **App Password** cho Gmail thay vì password thông thường
5. Trong production, nên dùng secrets manager (Kubernetes Secrets, AWS Secrets Manager, etc.)

---

## 🆘 Troubleshooting

### Lỗi kết nối SonarQube
- Kiểm tra `SONARQUBE_URL` có đúng không
- Kiểm tra SonarQube có đang chạy không
- Kiểm tra network/firewall có block không
- Kiểm tra token có còn valid không

### Lỗi gửi email
- Gmail: Phải dùng App Password, không dùng password thông thường
- Kiểm tra `MAIL_HOST` và `MAIL_PORT` có đúng không
- Kiểm tra firewall có block port 587/465 không
- Kiểm tra email có bật "Less secure app access" (nếu không dùng App Password)

### Lỗi kết nối Database
- Kiểm tra container postgres có đang chạy không
- Kiểm tra `DB_USERNAME` và `DB_PASSWORD` có đúng không
- Kiểm tra network connection giữa containers

---

## 📚 Tài liệu tham khảo

- [SonarQube User Token](https://docs.sonarqube.org/latest/user-guide/user-account/generating-and-using-tokens/)
- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [Spring Mail Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- [Cron Expression](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html)



