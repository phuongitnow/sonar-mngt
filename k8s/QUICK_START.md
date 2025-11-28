# Quick Start - K8s Deployment

## 🚀 Triển khai nhanh

### Bước 1: Build và Push Images

```bash
# Thay your-registry bằng registry của bạn (hoặc dùng local registry)
REGISTRY="your-registry"

# Build
docker build -t $REGISTRY/sonarqube-backend:latest ./backend
docker build -t $REGISTRY/sonarqube-frontend:latest ./frontend

# Push
docker push $REGISTRY/sonarqube-backend:latest
docker push $REGISTRY/sonarqube-frontend:latest
```

**Lưu ý**: Nếu dùng local images, cập nhật image name trong deployment files:
- `backend-deployment.yaml`: `image: sonarqube-backend:latest`
- `frontend-deployment.yaml`: `image: sonarqube-frontend:latest`

### Bước 2: Cấu hình Secrets

Chỉnh sửa `configmap-secret.yaml` và encode secrets:

```bash
# Encode base64
echo -n "admin123" | base64                    # DB_PASSWORD
echo -n "your_sonarqube_token" | base64       # SONARQUBE_TOKEN
```

### Bước 3: Deploy

```bash
# Cách 1: Dùng script
./k8s/deploy.sh

# Cách 2: Deploy thủ công
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres-pv.yaml
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/configmap-secret.yaml
kubectl apply -f k8s/nginx-configmap.yaml
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
```

### Bước 4: Kiểm tra

```bash
# Xem pods
kubectl get pods -n sonarqube-admin

# Xem services
kubectl get services -n sonarqube-admin

# Port forward để test
kubectl port-forward service/frontend-service 4200:80 -n sonarqube-admin
# Truy cập: http://localhost:4200
```

## 📋 Cấu trúc Pods

```
┌─────────────────────────────────────────┐
│         sonarqube-admin namespace       │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────┐  ┌──────────────┐   │
│  │   Frontend   │  │   Backend    │   │
│  │     Pod      │  │     Pod      │   │
│  │  (Nginx)     │  │ (Spring Boot)│   │
│  └──────┬───────┘  └──────┬───────┘   │
│         │                 │            │
│         └────────┬─────────┘            │
│                  │                      │
│         ┌────────▼────────┐            │
│         │  Postgres Pod   │            │
│         │   (Database)    │            │
│         └─────────────────┘            │
│                                         │
└─────────────────────────────────────────┘
```

## 🔧 Cấu hình quan trọng

### Database Connection

Backend kết nối database qua service name: `postgres-service:5432`

### Frontend → Backend

Frontend proxy API requests tới: `backend-service:6996`

### External Database

Nếu dùng database bên ngoài, cập nhật trong `configmap-secret.yaml`:

```yaml
SPRING_DATASOURCE_URL: "jdbc:postgresql://10.6.145.113:5434/dso"
```

## 🌐 Truy cập

### LoadBalancer (mặc định)

```bash
# Lấy external IP
kubectl get service frontend-service -n sonarqube-admin

# Truy cập
http://<EXTERNAL_IP>
```

### Ingress (optional)

```bash
# Deploy Ingress
kubectl apply -f k8s/ingress.yaml

# Cập nhật host trong ingress.yaml
# Truy cập qua domain đã cấu hình
```

### Port Forward (development)

```bash
# Frontend
kubectl port-forward service/frontend-service 4200:80 -n sonarqube-admin

# Backend
kubectl port-forward service/backend-service 6996:6996 -n sonarqube-admin
```

## 📊 Scaling

```bash
# Scale backend
kubectl scale deployment backend --replicas=3 -n sonarqube-admin

# Scale frontend
kubectl scale deployment frontend --replicas=2 -n sonarqube-admin
```

## 🗑️ Cleanup

```bash
# Xóa tất cả
kubectl delete namespace sonarqube-admin
```

## 📚 Xem thêm

Chi tiết đầy đủ: [README.md](./README.md)

