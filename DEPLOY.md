# 🚀 EC2 배포 가이드

Docker + EC2를 사용하여 파일 확장자 차단 시스템을 배포하는 방법을 설명합니다.

## 📋 사전 준비사항

### 1. AWS EC2 인스턴스 생성

- **인스턴스 타입**: t2.medium 이상 권장 (메모리 4GB+)
- **AMI**: Amazon Linux 2023 또는 Ubuntu 22.04
- **보안 그룹 설정**:
  - SSH (22) - 본인 IP만 허용
  - HTTP (80)
  - HTTPS (443)
  - Custom TCP (8080) - 백엔드 API
  - Custom TCP (3000) - 프론트엔드 (개발용)

### 2. Google OAuth2 설정

Google Cloud Console에서 승인된 리디렉션 URI 추가:
```
http://your-ec2-public-ip:8080/login/oauth2/code/google
또는
https://your-domain.com/login/oauth2/code/google
```

---

## 🔧 EC2 서버 설정

### 1. SSH 접속

```bash
ssh -i your-key.pem ec2-user@your-ec2-public-ip
# 또는 Ubuntu의 경우
ssh -i your-key.pem ubuntu@your-ec2-public-ip
```

### 2. Docker 설치

**Amazon Linux 2023:**
```bash
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
```

**Ubuntu 22.04:**
```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
```

**재로그인 필요** (그룹 권한 적용)
```bash
exit
# 다시 SSH 접속
```

### 3. Docker Compose 설치 (Amazon Linux의 경우)

```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

---

## 📦 프로젝트 배포

### 1. Git Clone

```bash
cd ~
git clone https://github.com/your-username/MadrasCheck_Homework.git
cd MadrasCheck_Homework
```

### 2. 환경 변수 설정

`.env` 파일 생성:
```bash
cp .env.example .env
nano .env
```

`.env` 파일 내용:
```env
# Database Configuration
DB_USERNAME=root
DB_PASSWORD=your_secure_password_here
DB_URL=jdbc:mysql://mysql:3306/file_extension_blocker?useSSL=false&serverTimezone=UTC

# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# URLs (EC2 Public IP 또는 도메인)
FRONTEND_URL=http://your-ec2-public-ip:3000
BACKEND_URL=http://your-ec2-public-ip:8080

# Cookie Settings
COOKIE_DOMAIN=
COOKIE_SECURE=false

# Server Port
SERVER_PORT=8080

# React App
REACT_APP_API_URL=http://your-ec2-public-ip:8080/api
REACT_APP_BACKEND_URL=http://your-ec2-public-ip:8080
```

### 3. 배포 스크립트 실행 권한 부여

```bash
chmod +x deploy.sh
```

### 4. 배포 실행

```bash
./deploy.sh
```

또는 최신 코드를 pull 후 배포:
```bash
./deploy.sh --pull
```

---

## 🔍 배포 확인

### 1. 컨테이너 상태 확인

```bash
docker-compose ps
```

정상적인 출력:
```
NAME                      STATUS    PORTS
fileextension-backend     Up        0.0.0.0:8080->8080/tcp
fileextension-frontend    Up        0.0.0.0:3000->80/tcp
fileextension-mysql       Up        0.0.0.0:3306->3306/tcp
```

### 2. 로그 확인

```bash
# 전체 로그
docker-compose logs -f

# 백엔드 로그만
docker-compose logs -f backend

# 프론트엔드 로그만
docker-compose logs -f frontend
```

### 3. 서비스 접속 테스트

```bash
# 백엔드 API 테스트
curl http://localhost:8080/api/extensions/fixed

# 프론트엔드 테스트
curl http://localhost:3000
```

### 4. 브라우저 접속

- 프론트엔드: `http://your-ec2-public-ip:3000`
- 백엔드 API: `http://your-ec2-public-ip:8080/api`

---

## 🛠️ 관리 명령어

### 컨테이너 재시작

```bash
docker-compose restart
```

### 특정 서비스만 재시작

```bash
docker-compose restart backend
docker-compose restart frontend
```

### 컨테이너 중지

```bash
docker-compose stop
```

### 컨테이너 삭제 (데이터는 유지)

```bash
docker-compose down
```

### 컨테이너 및 볼륨 삭제 (데이터도 삭제)

```bash
docker-compose down -v
```

### 재배포 (이미지 재빌드)

```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

---

## 🔐 보안 강화 (프로덕션)

### 1. 방화벽 설정 (UFW - Ubuntu)

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

### 2. HTTPS 설정 (Let's Encrypt)

```bash
# Certbot 설치
sudo apt-get install -y certbot python3-certbot-nginx

# SSL 인증서 발급
sudo certbot --nginx -d your-domain.com
```

### 3. 환경 변수 업데이트 (HTTPS)

```env
FRONTEND_URL=https://your-domain.com
BACKEND_URL=https://your-domain.com
COOKIE_SECURE=true
COOKIE_DOMAIN=your-domain.com
```

---

## 📊 모니터링

### Docker 리소스 사용량

```bash
docker stats
```

### 디스크 사용량

```bash
df -h
docker system df
```

### 로그 파일 크기 제한

`docker-compose.yml`에 추가:
```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

---

## 🐛 문제 해결

### 백엔드가 시작되지 않을 때

```bash
# MySQL이 준비될 때까지 대기
docker-compose logs mysql

# 백엔드 로그 확인
docker-compose logs backend

# 컨테이너 재시작
docker-compose restart backend
```

### 프론트엔드 빌드 실패

```bash
# 로컬에서 빌드 테스트
cd frontend
npm install
npm run build

# Docker 이미지 재빌드
docker-compose build --no-cache frontend
```

### MySQL 연결 오류

```bash
# MySQL 컨테이너 접속
docker exec -it fileextension-mysql mysql -u root -p

# 데이터베이스 확인
SHOW DATABASES;
USE file_extension_blocker;
SHOW TABLES;
```

### 포트 충돌

```bash
# 포트 사용 확인
sudo netstat -tuln | grep 8080
sudo netstat -tuln | grep 3000

# 포트 사용 중인 프로세스 종료
sudo kill -9 $(sudo lsof -t -i:8080)
```

---

## 📝 백업 및 복구

### 데이터베이스 백업

```bash
# 백업
docker exec fileextension-mysql mysqldump -u root -p file_extension_blocker > backup.sql

# 복구
docker exec -i fileextension-mysql mysql -u root -p file_extension_blocker < backup.sql
```

### 자동 백업 (cron)

```bash
# crontab 편집
crontab -e

# 매일 새벽 2시에 백업
0 2 * * * docker exec fileextension-mysql mysqldump -u root -pYOUR_PASSWORD file_extension_blocker > ~/backups/db_$(date +\%Y\%m\%d).sql
```

---

## 🎯 성능 최적화

### JVM 메모리 설정

`docker-compose.yml`에 추가:
```yaml
services:
  backend:
    environment:
      - JAVA_OPTS=-Xms512m -Xmx1024m
```

### MySQL 설정 최적화

```yaml
services:
  mysql:
    command: --default-authentication-plugin=mysql_native_password --max_connections=200
```

---

## 📞 지원

문제가 발생하면 로그를 확인하고 다음 정보를 포함하여 이슈를 등록하세요:
- EC2 인스턴스 타입
- 운영체제 버전
- Docker 버전: `docker --version`
- Docker Compose 버전: `docker-compose --version`
- 에러 로그: `docker-compose logs`
