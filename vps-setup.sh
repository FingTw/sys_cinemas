#!/bin/bash
# Script cài đặt VPS lần đầu tiên
# Chạy với: bash vps-setup.sh

set -e

echo "=== [1/5] Cài Docker ==="
curl -fsSL https://get.docker.com | bash
usermod -aG docker $USER

echo "=== [2/5] Cài Docker Compose plugin ==="
apt-get install -y docker-compose-plugin

echo "=== [3/5] Tạo thư mục project ==="
mkdir -p /home/deploy/sys_cinemas
cd /home/deploy/sys_cinemas

echo "=== [4/5] Hướng dẫn tiếp theo ==="
echo ""
echo "  Bạn cần copy 2 file lên VPS:"
echo "  1. docker-compose.yml"
echo "     scp docker-compose.yml user@VPS_IP:/home/deploy/sys_cinemas/"
echo ""
echo "  2. .env (KHÔNG commit file này lên Git)"  
echo "     scp .env user@VPS_IP:/home/deploy/sys_cinemas/"
echo ""
echo "  Hoặc tạo file .env thủ công trên VPS:"
echo "  nano /home/deploy/sys_cinemas/.env"
echo ""
echo "=== [5/5] Sửa docker-compose.yml để pull image từ Docker Hub ==="
echo "  Thay 'build:' bằng 'image: your-dockerhub-username/service-name:latest'"
echo "  Xem file docker-compose.production.yml mẫu đính kèm."
