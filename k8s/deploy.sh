#!/bin/bash

# Script để deploy SonarQube Admin App lên Kubernetes

set -e

echo "=========================================="
echo "  SonarQube Admin App - K8s Deployment"
echo "=========================================="
echo ""

# Kiểm tra kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl không được tìm thấy. Vui lòng cài đặt kubectl."
    exit 1
fi

# Kiểm tra kết nối cluster
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Không thể kết nối tới Kubernetes cluster."
    exit 1
fi

echo "✅ Đã kết nối tới cluster"
echo ""

# Deploy theo thứ tự
echo "1. Tạo namespace..."
kubectl apply -f k8s/namespace.yaml

echo "2. Deploy PersistentVolume..."
kubectl apply -f k8s/postgres-pv.yaml

echo "3. Deploy Database..."
kubectl apply -f k8s/postgres-deployment.yaml

echo "4. Deploy ConfigMap và Secrets..."
kubectl apply -f k8s/configmap-secret.yaml

echo "5. Deploy Nginx ConfigMap..."
kubectl apply -f k8s/nginx-configmap.yaml

echo "6. Deploy Backend..."
kubectl apply -f k8s/backend-deployment.yaml

echo "7. Deploy Frontend..."
kubectl apply -f k8s/frontend-deployment.yaml

echo ""
echo "8. Đợi pods khởi động..."
sleep 10

echo ""
echo "=== Trạng thái Pods ==="
kubectl get pods -n sonarqube-admin

echo ""
echo "=== Trạng thái Services ==="
kubectl get services -n sonarqube-admin

echo ""
echo "✅ Deployment hoàn tất!"
echo ""
echo "📝 Các lệnh hữu ích:"
echo "  - Xem logs backend: kubectl logs -f deployment/backend -n sonarqube-admin"
echo "  - Xem logs frontend: kubectl logs -f deployment/frontend -n sonarqube-admin"
echo "  - Port forward frontend: kubectl port-forward service/frontend-service 4200:80 -n sonarqube-admin"
echo "  - Port forward backend: kubectl port-forward service/backend-service 6996:6996 -n sonarqube-admin"
echo ""

