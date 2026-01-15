# 멀티 스테이지 빌드
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Gradle 설정 파일 복사
COPY build.gradle settings.gradle ./

# 소스 코드 복사
COPY src ./src

# 빌드 실행 (테스트 제외) - Gradle 이미지에 포함된 gradle 명령 사용
RUN gradle bootJar -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
