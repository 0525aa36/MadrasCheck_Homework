# 🚀 AWS Console을 통한 배포 가이드

## 📋 Step 1: AWS Console 로그인

1. 브라우저에서 접속: https://940482431364.signin.aws.amazon.com/console
2. 사용자 이름: `madras`
3. 암호: `cEK0I9-|`
4. 로그인 클릭

---

## 🖥️ Step 2: EC2 인스턴스 생성

### 1. EC2 대시보드로 이동
- 상단 검색창에 "EC2" 입력 → EC2 클릭
- 왼쪽 메뉴에서 "인스턴스" 클릭
- **"인스턴스 시작" 버튼 클릭**

### 2. 인스턴스 설정

#### 이름 및 태그
```
이름: MadrasCheck-FileExtension
```

#### AMI 선택 (운영체제)
```
선택: Amazon Linux 2023 AMI (무료 티어 사용 가능)
```

#### 인스턴스 유형
```
선택: t2.micro (무료 티어) ✅

※ 면접용/테스트용으로 충분
※ 메모리 1GB이므로 최적화 필요 (아래 설명)
```

#### 키 페어 (로그인)
```
1. "새 키 페어 생성" 클릭
2. 키 페어 이름: madrascheck-key
3. 키 페어 유형: RSA
4. 프라이빗 키 파일 형식: .pem
5. "키 페어 생성" 클릭
6. ⚠️ madrascheck-key.pem 파일 다운로드됨 (안전한 곳에 보관!)
```

#### 네트워크 설정
```
✅ 퍼블릭 IP 자동 할당: 활성화
✅ 보안 그룹 생성 (다음 규칙 추가):
   - SSH (22) - 내 IP
   - HTTP (80) - 0.0.0.0/0
   - HTTPS (443) - 0.0.0.0/0
   - 사용자 지정 TCP (8080) - 0.0.0.0/0  # 백엔드
   - 사용자 지정 TCP (3000) - 0.0.0.0/0  # 프론트엔드
```

#### 스토리지 구성
```
크기: 20 GiB (기본값)
볼륨 유형: gp3 (범용 SSD)
```

### 3. 인스턴스 시작
- "인스턴스 시작" 버튼 클릭
- 성공 메시지 확인
- "인스턴스 보기" 클릭

### 4. 퍼블릭 IP 확인
- 인스턴스 선택
- 하단 "세부 정보" 탭에서 **퍼블릭 IPv4 주소** 복사
- 예: `13.125.123.45`

---

## 🔑 Step 3: SSH 키 파일 준비 (Windows)

### 다운로드한 키 파일 위치 확인
```
다운로드 폴더: C:\Users\seok0\Downloads\madrascheck-key.pem
```

### 키 파일 권한 설정 (Windows)
```powershell
# PowerShell 관리자 권한으로 실행
icacls "C:\Users\seok0\Downloads\madrascheck-key.pem" /inheritance:r
icacls "C:\Users\seok0\Downloads\madrascheck-key.pem" /grant:r "%USERNAME%:R"
```

또는 Git Bash 사용:
```bash
chmod 400 ~/Downloads/madrascheck-key.pem
```

---

## 🌐 Step 4: SSH 접속

### Git Bash 또는 WSL에서 접속
```bash
# EC2 퍼블릭 IP를 실제 IP로 변경
ssh -i ~/Downloads/madrascheck-key.pem ec2-user@EC2퍼블릭IP

# 예시:
ssh -i ~/Downloads/madrascheck-key.pem ec2-user@13.125.123.45

# 처음 접속 시 "yes" 입력
```

---

## 🐳 Step 5: EC2 서버에서 Docker 설치 및 메모리 최적화

SSH 접속 후 다음 명령어 실행:

```bash
# 시스템 업데이트
sudo yum update -y

# Docker 설치
sudo yum install -y docker

# Docker 시작
sudo systemctl start docker
sudo systemctl enable docker

# Docker 권한 추가
sudo usermod -aG docker ec2-user

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Git 설치
sudo yum install -y git

# ⚡ t2.micro 메모리 최적화: Swap 메모리 추가 (2GB)
sudo dd if=/dev/zero of=/swapfile bs=128M count=16
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
# 재부팅 후에도 유지
echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab

# 메모리 확인
free -h

# 재로그인 (권한 적용)
exit
```

다시 SSH 접속:
```bash
ssh -i ~/Downloads/madrascheck-key.pem ec2-user@EC2퍼블릭IP
```

---

## 📦 Step 6: 프로젝트 배포

### 1. 프로젝트 클론
```bash
cd ~
git clone https://github.com/0525aa36/MadrasCheck_Homework.git
cd MadrasCheck_Homework
```

### 2. 환경 변수 설정
```bash
cp .env.example .env
nano .env
```

**`.env` 파일 내용 수정:**
```env
# Database Configuration
DB_USERNAME=root
DB_PASSWORD=SecurePassword123!

# Google OAuth2 (중요!)
GOOGLE_CLIENT_ID=여기에_구글_클라이언트_ID
GOOGLE_CLIENT_SECRET=여기에_구글_시크릿

# URLs (EC2 퍼블릭 IP로 변경)
FRONTEND_URL=http://13.125.123.45:3000
BACKEND_URL=http://13.125.123.45:8080

# Cookie Settings
COOKIE_DOMAIN=
COOKIE_SECURE=false

# Server Port
SERVER_PORT=8080

# React App
REACT_APP_API_URL=http://13.125.123.45:8080/api
REACT_APP_BACKEND_URL=http://13.125.123.45:8080
```

저장: `Ctrl + X` → `Y` → `Enter`

### 3. Google OAuth2 설정

**중요!** Google Cloud Console에서 승인된 리디렉션 URI 추가:

1. https://console.cloud.google.com/apis/credentials 접속
2. OAuth 2.0 클라이언트 ID 선택
3. "승인된 리디렉션 URI"에 추가:
   ```
   http://13.125.123.45:8080/login/oauth2/code/google
   ```
4. 저장

### 4. 배포 실행
```bash
# 실행 권한 부여
chmod +x deploy.sh

# 배포 시작 (5~10분 소요)
./deploy.sh
```

---

## 🎯 Step 7: 배포 확인

### 컨테이너 상태 확인
```bash
docker-compose ps
```

**정상 출력:**
```
NAME                      STATUS    PORTS
fileextension-mysql       Up        0.0.0.0:3306->3306/tcp
fileextension-backend     Up        0.0.0.0:8080->8080/tcp
fileextension-frontend    Up        0.0.0.0:3000->80/tcp
```

### 로그 확인
```bash
# 전체 로그
docker-compose logs -f

# Ctrl + C로 종료
```

### 브라우저 접속 테스트
```
프론트엔드: http://13.125.123.45:3000
백엔드 API: http://13.125.123.45:8080/api/extensions/fixed
```

---

## 🔧 주요 명령어

### 서비스 재시작
```bash
docker-compose restart
```

### 로그 확인
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
```

### 서비스 중지
```bash
docker-compose down
```

### 재배포
```bash
git pull origin main
./deploy.sh
```

---

## 🐛 문제 해결

### 백엔드가 시작 안 될 때
```bash
docker-compose logs backend
# 환경 변수 확인
cat .env
```

### 프론트엔드 접속 안 될 때
```bash
docker-compose logs frontend
# 포트 확인
curl http://localhost:3000
```

### MySQL 연결 오류
```bash
docker exec -it fileextension-mysql mysql -u root -p
# 비밀번호: .env의 DB_PASSWORD
```

---

## ✅ 완료!

축하합니다! 배포가 완료되었습니다.

**접속 URL:**
- 프론트엔드: http://EC2퍼블릭IP:3000
- 백엔드 API: http://EC2퍼블릭IP:8080/api

**다음 단계:**
1. Google OAuth2로 로그인 테스트
2. 파일 확장자 관리 기능 테스트
3. (선택) 도메인 연결 및 HTTPS 설정
