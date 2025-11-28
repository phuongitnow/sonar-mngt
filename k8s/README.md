# Kubernetes Deployment Guide

Hướng dẫn triển khai SonarQube Admin App lên Kubernetes với frontend và backend trên các pod riêng biệt.

## 📋 Cấu trúc

```
k8s/
├── namespace.yaml              # Namespace cho ứng dụng
├── postgres-pv.yaml            # PersistentVolume cho database
├── postgres-deployment.yaml    # PostgreSQL deployment
├── configmap-secret.yaml       # ConfigMap và Secret
├── backend-deployment.yaml     # Backend API deployment
├── frontend-deployment.yaml    # Frontend UI deployment
├── nginx-configmap.yaml        # Nginx config cho frontend
└── ingress.yaml                # Ingress (optional)
```

## 🚀 Các bước triển khai

### 1. Build và Push Docker Images

```bash
# Build images
docker build -t sonarqube-backend:latest ./backend
docker build -t sonarqube-frontend:latest ./frontend

# Tag cho registry (thay your-registry bằng registry của bạn)
docker tag sonarqube-backend:latest your-registry/sonarqube-backend:latest
docker tag sonarqube-frontend:latest your-registry/sonarqube-frontend:latest

# Push to registry
docker push your-registry/sonarqube-backend:latest
docker push your-registry/sonarqube-frontend:latest
```

**Lưu ý**: Cập nhật image name trong các file deployment nếu dùng registry khác.

### 2. Cấu hình Secrets và ConfigMap

Chỉnh sửa `configmap-secret.yaml`:

```bash
# Encode secrets (base64)
echo -n "your_db_password" | base64
echo -n "your_sonarqube_token" | base64
echo -n "your_email@gmail.com" | base64
echo -n "your_app_password" | base64
```

Cập nhật các giá trị trong `configmap-secret.yaml`.

### 3. Deploy lên Kubernetes

```bash
# Tạo namespace
kubectl apply -f k8s/namespace.yaml

# Deploy PersistentVolume
kubectl apply -f k8s/postgres-pv.yaml

# Deploy Database
kubectl apply -f k8s/postgres-deployment.yaml

# Deploy ConfigMap và Secrets
kubectl apply -f k8s/configmap-secret.yaml

# Deploy Nginx ConfigMap
kubectl apply -f k8s/nginx-configmap.yaml

# Deploy Backend
kubectl apply -f k8s/backend-deployment.yaml

# Deploy Frontend
kubectl apply -f k8s/frontend-deployment.yaml

# Deploy Ingress (optional)
kubectl apply -f k8s/ingress.yaml
```

### 4. Kiểm tra Status

```bash
# Kiểm tra pods
kubectl get pods -n sonarqube-admin

# Kiểm tra services
kubectl get services -n sonarqube-admin

# Kiểm tra persistent volumes
kubectl get pv,pvc -n sonarqube-admin

# Xem logs
kubectl logs -f deployment/backend -n sonarqube-admin
kubectl logs -f deployment/frontend -n sonarqube-admin
```

## 🔧 Cấu hình

### Database

- **Service name**: `postgres-service`
- **Port**: `5432`
- **Database**: `sonarqube_admin`
- **Username**: `admin`
- **Password**: Từ Secret `app-secrets`

### Backend

- **Service name**: `backend-service`
- **Port**: `6996`
- **Health check**: `/actuator/health`
- **Replicas**: 1 (có thể scale)

### Frontend

- **Service name**: `frontend-service`
- **Port**: `80`
- **Type**: `LoadBalancer` (có thể đổi thành `ClusterIP` nếu dùng Ingress)
- **Replicas**: 1 (có thể scale)

## 🌐 Truy cập

### Với LoadBalancer

```bash
# Lấy external IP
kubectl get service frontend-service -n sonarqube-admin

# Truy cập
http://<EXTERNAL_IP>
```

### Với Ingress

1. Cài đặt Ingress Controller (nginx):
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```

2. Cập nhật host trong `ingress.yaml`:
```yaml
spec:
  rules:
  - host: your-domain.com  # Thay đổi domain
```

3. Truy cập:
- Frontend: `http://your-domain.com`
- Backend API: `http://your-domain.com/api`

### Port Forward (Development)

```bash
# Frontend
kubectl port-forward service/frontend-service 4200:80 -n sonarqube-admin

# Backend
kubectl port-forward service/backend-service 6996:6996 -n sonarqube-admin
```

Truy cập:
- Frontend: http://localhost:4200
- Backend: http://localhost:6996

## 📊 Scaling

### Scale Backend

```bash
kubectl scale deployment backend --replicas=3 -n sonarqube-admin
```

### Scale Frontend

```bash
kubectl scale deployment frontend --replicas=2 -n sonarqube-admin
```

## 🔄 Updates

### Update Backend Image

```bash
# Build và push image mới
docker build -t your-registry/sonarqube-backend:v2.0 ./backend
docker push your-registry/sonarqube-backend:v2.0

# Update deployment
kubectl set image deployment/backend backend=your-registry/sonarqube-backend:v2.0 -n sonarqube-admin

# Rollout status
kubectl rollout status deployment/backend -n sonarqube-admin
```

### Update Frontend Image

```bash
# Build và push image mới
docker build -t your-registry/sonarqube-frontend:v2.0 ./frontend
docker push your-registry/sonarqube-frontend:v2.0

# Update deployment
kubectl set image deployment/frontend frontend=your-registry/sonarqube-frontend:v2.0 -n sonarqube-admin

# Rollout status
kubectl rollout status deployment/frontend -n sonarqube-admin
```

## 🔒 Security

### Sử dụng External Database

Nếu dùng database bên ngoài, cập nhật `SPRING_DATASOURCE_URL` trong ConfigMap:

```yaml
SPRING_DATASOURCE_URL: "jdbc:postgresql://10.6.145.113:5434/dso"
```

### Secrets Management

Trong production, nên dùng:
- **Sealed Secrets**: https://github.com/bitnami-labs/sealed-secrets
- **External Secrets Operator**: https://external-secrets.io/
- **Vault**: https://www.vaultproject.io/

## 🗑️ Cleanup

```bash
# Xóa tất cả resources
kubectl delete namespace sonarqube-admin

# Hoặc xóa từng resource
kubectl delete -f k8s/
```

## 📝 Notes

1. **PersistentVolume**: Dữ liệu database được lưu trong PV, không bị mất khi pod restart
2. **Health Checks**: Backend và Frontend đều có liveness và readiness probes
3. **Resource Limits**: Đã set limits để tránh resource exhaustion
4. **Network**: Tất cả pods trong cùng namespace có thể giao tiếp qua service names

## 🆘 Troubleshooting

### Pod không start

```bash
# Xem events
kubectl describe pod <pod-name> -n sonarqube-admin

# Xem logs
kubectl logs <pod-name> -n sonarqube-admin
```

### Backend không kết nối database

```bash
# Kiểm tra service
kubectl get service postgres-service -n sonarqube-admin

# Test connection từ backend pod
kubectl exec -it deployment/backend -n sonarqube-admin -- sh
# Trong pod: nc -zv postgres-service 5432
```

### Frontend không proxy được API

```bash
# Kiểm tra nginx config
kubectl get configmap nginx-config -n sonarqube-admin -o yaml

# Kiểm tra backend service
kubectl get service backend-service -n sonarqube-admin
```

