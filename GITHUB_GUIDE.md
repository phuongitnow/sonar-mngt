# Hướng dẫn đồng bộ project lên GitHub

## 📋 Các bước thực hiện

### Bước 1: Khởi tạo Git repository

```bash
# Khởi tạo git repository
git init

# Kiểm tra trạng thái
git status
```

### Bước 2: Thêm các file vào Git

```bash
# Thêm tất cả các file (theo .gitignore)
git add .

# Commit các file
git commit -m "Initial commit: SonarQube Admin Application"
```

### Bước 3: Tạo repository trên GitHub

1. Truy cập https://github.com
2. Click **"New repository"** hoặc **"+" → "New repository"**
3. Điền thông tin:
   - **Repository name**: `sonarqube-admin-app` (hoặc tên bạn muốn)
   - **Description**: SonarQube Project Management Application
   - **Visibility**: Public hoặc Private (tùy chọn)
   - **⚠️ KHÔNG tick** "Initialize with README", "Add .gitignore", hoặc "Choose a license"
4. Click **"Create repository"**

### Bước 4: Kết nối local repository với GitHub

```bash
# Thêm remote origin (thay YOUR_USERNAME bằng username GitHub của bạn)
git remote add origin https://github.com/YOUR_USERNAME/sonarqube-admin-app.git

# Hoặc nếu dùng SSH:
# git remote add origin git@github.com:YOUR_USERNAME/sonarqube-admin-app.git

# Xác nhận remote đã được thêm
git remote -v
```

### Bước 5: Push code lên GitHub

```bash
# Push code lên GitHub (branch main)
git branch -M main
git push -u origin main

# Nếu là lần đầu, GitHub sẽ yêu cầu authentication
# Sử dụng Personal Access Token thay vì password
```

## 🔐 Authentication với GitHub

### Tạo Personal Access Token

1. Truy cập: https://github.com/settings/tokens
2. Click **"Generate new token"** → **"Generate new token (classic)"**
3. Đặt tên token (ví dụ: "sonarqube-admin-app")
4. Chọn scopes: **repo** (full control)
5. Click **"Generate token"**
6. **Lưu lại token** (sẽ không hiển thị lại)

### Sử dụng token

Khi push, sử dụng token làm password:
- Username: `YOUR_USERNAME`
- Password: `YOUR_TOKEN`

## 📦 Cấu trúc repository

```
sonarqube-admin-app/
├── backend/              # Spring Boot API
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/             # Angular UI
│   ├── src/
│   ├── package.json
│   ├── angular.json
│   └── Dockerfile
├── docker-compose.yml    # Container orchestration
├── .gitignore           # Git ignore rules
├── README.md            # Main documentation
├── GETTING_STARTED.md   # Installation guide
├── USER_GUIDE.md        # User manual
├── env.example          # Environment template
└── LICENSE              # License file (optional)
```

## ⚠️ Files KHÔNG được commit

Các file sau đã được loại trừ trong `.gitignore`:

- `.env` - Chứa thông tin nhạy cảm (SonarQube tokens, database passwords)
- `backend/target/` - Maven build output
- `frontend/node_modules/` - Node dependencies
- `frontend/dist/` - Angular build output
- `*.log` - Log files

## 🔄 Các lệnh Git hữu ích

### Xem trạng thái
```bash
git status
```

### Xem thay đổi
```bash
git diff
```

### Xem commit history
```bash
git log --oneline
```

### Thêm thay đổi và commit
```bash
git add .
git commit -m "Mô tả thay đổi"
git push origin main
```

### Tạo branch mới
```bash
git checkout -b feature/new-feature
git push -u origin feature/new-feature
```

### Merge branch vào main
```bash
git checkout main
git merge feature/new-feature
git push origin main
```

## 📝 Nội dung commit messages

Sử dụng format rõ ràng cho commit messages:

```
type: short description

Longer description if needed
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Ví dụ:**
```bash
git commit -m "feat: add cleanup scheduler for old projects"

git commit -m "fix: update database port mapping to 2345"

git commit -m "docs: update README with new port configuration"
```

## 🚀 GitHub Actions (CI/CD) - Optional

Bạn có thể thêm CI/CD workflow để:
- Build và test tự động
- Build Docker images
- Deploy tự động

Ví dụ file `.github/workflows/build.yml`:

```yaml
name: Build and Test

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Maven
        run: |
          cd backend
          mvn clean package
  
  build-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Install dependencies
        run: |
          cd frontend
          npm install
      - name: Build Angular app
        run: |
          cd frontend
          npm run build
```

## 🔒 Bảo mật

### Các thông tin KHÔNG được commit:

- ✅ SonarQube token
- ✅ Database passwords
- ✅ Email credentials
- ✅ API keys
- ✅ SSH private keys

### File cấu hình mẫu:

- ✅ `env.example` - Template cho .env
- ✅ `README.md` - Hướng dẫn setup

## 📚 Badges cho README.md

Thêm vào đầu README.md:

```markdown
![License](https://img.shields.io/badge/license-Internal-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)
![Angular](https://img.shields.io/badge/Angular-17-red)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)
```

## ✅ Checklist trước khi push

- [ ] Kiểm tra `.env` không có trong Git
- [ ] Kiểm tra không có credentials hard-coded trong code
- [ ] README.md đầy đủ và rõ ràng
- [ ] .gitignore đã bao gồm đầy đủ files/folders
- [ ] Commit messages rõ ràng
- [ ] Code đã được test và hoạt động

## 🎉 Hoàn thành!

Sau khi push thành công, bạn có thể:
- Xem code trên GitHub
- Share repository với team
- Cài đặt CI/CD
- Tạo Issues và Pull Requests
- Deploy từ GitHub

**Link repository**: https://github.com/YOUR_USERNAME/sonarqube-admin-app

