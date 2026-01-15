# 멀티 스테이지 빌드
FROM node:18-alpine AS builder

WORKDIR /app

# package.json 복사 및 의존성 설치
COPY package*.json ./
RUN npm ci

# 소스 코드 복사
COPY . .

# 환경 변수 설정 (빌드 시간)
ARG REACT_APP_API_URL
ARG REACT_APP_BACKEND_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL
ENV REACT_APP_BACKEND_URL=$REACT_APP_BACKEND_URL

# 프로덕션 빌드
RUN npm run build

# Nginx 스테이지
FROM nginx:alpine

# 빌드된 파일 복사
COPY --from=builder /app/build /usr/share/nginx/html

# Nginx 설정 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]
