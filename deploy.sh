#!/bin/bash

# 배포 스크립트
# EC2에서 실행할 배포 자동화 스크립트

set -e

echo "🚀 배포를 시작합니다..."

# 환경 변수 로드
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
else
    echo "❌ .env 파일이 없습니다. .env.example을 참고하여 생성해주세요."
    exit 1
fi

# Git pull (옵션)
if [ "$1" == "--pull" ]; then
    echo "📥 최신 코드를 가져옵니다..."
    git pull origin main
fi

# 이전 컨테이너 정리
echo "🧹 이전 컨테이너를 정리합니다..."
docker-compose down

# Docker 이미지 빌드
echo "🔨 Docker 이미지를 빌드합니다..."
docker-compose build --no-cache

# 컨테이너 실행
echo "▶️  컨테이너를 실행합니다..."
docker-compose up -d

# 헬스 체크
echo "🏥 서비스 상태를 확인합니다..."
sleep 10

if curl -f http://localhost:8080/api/extensions/fixed > /dev/null 2>&1; then
    echo "✅ 백엔드 서비스가 정상적으로 실행 중입니다."
else
    echo "⚠️  백엔드 서비스가 응답하지 않습니다. 로그를 확인하세요:"
    echo "docker-compose logs backend"
fi

if curl -f http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ 프론트엔드 서비스가 정상적으로 실행 중입니다."
else
    echo "⚠️  프론트엔드 서비스가 응답하지 않습니다. 로그를 확인하세요:"
    echo "docker-compose logs frontend"
fi

echo "🎉 배포가 완료되었습니다!"
echo ""
echo "📊 서비스 상태 확인: docker-compose ps"
echo "📝 로그 확인: docker-compose logs -f"
echo "🛑 서비스 중지: docker-compose down"
