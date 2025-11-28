#!/bin/bash

# Script giúp tạo và thiết lập file .env

echo "=========================================="
echo "   SonarQube Admin App - Env Setup"
echo "=========================================="
echo ""

# Kiểm tra xem file .env đã tồn tại chưa
if [ -f .env ]; then
    echo "⚠️  File .env đã tồn tại!"
    read -p "Bạn có muốn tạo backup và tạo file mới? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        BACKUP_FILE=".env.backup.$(date +%Y%m%d_%H%M%S)"
        cp .env "$BACKUP_FILE"
        echo "✅ Đã backup file .env thành $BACKUP_FILE"
        cp env.example .env
        echo "✅ Đã tạo file .env mới từ env.example"
    else
        echo "❌ Đã hủy. File .env giữ nguyên."
        exit 0
    fi
else
    # Copy từ env.example
    if [ -f env.example ]; then
        cp env.example .env
        echo "✅ Đã tạo file .env từ env.example"
    else
        echo "❌ Không tìm thấy file env.example!"
        exit 1
    fi
fi

echo ""
echo "=========================================="
echo "   Hướng dẫn điền thông tin"
echo "=========================================="
echo ""
echo "📝 Mở file .env và điền các thông tin sau:"
echo ""
echo "1. SONARQUBE_URL"
echo "   - URL của SonarQube server"
echo "   - Ví dụ: http://sonarqube:9000 hoặc http://localhost:9000"
echo ""
echo "2. SONARQUBE_TOKEN (QUAN TRỌNG)"
echo "   - Tạo token tại: SonarQube UI → My Account → Security"
echo "   - Click Generate và copy token"
echo ""
echo "3. Email Configuration (nếu muốn nhận notification)"
echo "   - MAIL_HOST: smtp.gmail.com (hoặc SMTP server của bạn)"
echo "   - MAIL_PORT: 587 (hoặc 465)"
echo "   - MAIL_USERNAME: email của bạn"
echo "   - MAIL_PASSWORD: App Password (KHÔNG phải password thông thường!)"
echo "     Với Gmail: Google Account → Security → App passwords → Generate"
echo "   - NOTIFICATION_EMAIL: email nhận thông báo"
echo ""
echo "4. Cleanup Configuration (tùy chọn)"
echo "   - DAYS_THRESHOLD: Số ngày không scan (mặc định: 14)"
echo "   - CLEANUP_CRON: Lịch chạy cleanup (mặc định: 0 0 2 * * *)"
echo ""
echo "5. Database Configuration (có thể giữ mặc định)"
echo "   - DB_USERNAME: admin (mặc định)"
echo "   - DB_PASSWORD: admin123 (nên đổi trong production!)"
echo ""
echo "=========================================="
echo ""
read -p "Bạn có muốn mở file .env để chỉnh sửa ngay bây giờ? (y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if command -v nano &> /dev/null; then
        nano .env
    elif command -v vi &> /dev/null; then
        vi .env
    elif command -v vim &> /dev/null; then
        vim .env
    else
        echo "⚠️  Không tìm thấy editor. Hãy mở file .env bằng editor yêu thích của bạn."
        echo "   File location: $(pwd)/.env"
    fi
fi

echo ""
echo "✅ Hoàn tất! Kiểm tra file .env đã điền đầy đủ thông tin trước khi chạy:"
echo "   docker compose up -d"
echo ""
echo "📚 Xem hướng dẫn chi tiết tại: ENV_CONFIGURATION_GUIDE.md"



