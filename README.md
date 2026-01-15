# 📁 파일 확장자 차단 시스템

> 플로우(Flow) 백엔드 개발자 채용 과제

보안상 위험한 파일 확장자를 관리하고 차단하는 웹 애플리케이션입니다.  
특정 확장자(예: exe, sh 등)를 가진 파일의 업로드를 제한하여 서버 보안을 강화합니다.

<br>

## 📋 목차
1. [과제 요구사항](#-과제-요구사항)
2. [구현 기능](#-구현-기능)
3. [요건 이외 고려사항](#-요건-이외-고려사항)
4. [기술 스택](#-기술-스택)
5. [ERD & 아키텍처](#-erd--아키텍처)
6. [프로젝트 구조](#-프로젝트-구조)
7. [실행 방법](#-실행-방법)
8. [API 명세](#-api-명세)
9. [테스트](#-테스트)

<br>

---

## 📝 과제 요구사항

### 과제 개요
파일 확장자에 따라 특정 형식의 파일을 첨부하거나 전송하지 못하도록 제한하는 시스템을 구현합니다.

### 필수 요구사항

#### 1️⃣ 고정 확장자 (Fixed Extensions)
- **기능**: 자주 차단하는 확장자 목록 (8개 제공)
  - bat, cmd, com, cpl, exe, scr, js, sh
- **상태 관리**: 체크박스로 차단/허용 상태 변경
- **영속성**: 상태 변경 시 DB 저장, 새로고침 시에도 유지
- **주의사항**: 고정 확장자는 커스텀 확장자 영역에 표시되지 않음

#### 2️⃣ 커스텀 확장자 (Custom Extensions)
- **입력 제한**: 최대 20자
- **추가**: 입력 후 "추가" 버튼 클릭 시 DB 저장 및 화면 표시
- **최대 개수**: 200개까지 추가 가능
- **삭제**: 확장자 옆 X 버튼 클릭 시 DB에서 삭제 및 화면에서 제거

#### 3️⃣ 화면 요구사항
- 고정 확장자 체크박스 목록
- 커스텀 확장자 입력 필드 및 추가 버튼
- 추가된 커스텀 확장자 표시 영역 (3/200 형태로 개수 표시)

<br>

---

## ✅ 구현 기능

### 🎯 핵심 기능

#### 1. 고정 확장자 관리
```
✓ 8개 고정 확장자 기본 제공 (bat, cmd, com, cpl, exe, scr, js, sh)
✓ 체크박스 토글로 차단/허용 상태 변경
✓ 새로고침 시 상태 유지 (DB 영속화)
✓ 차단 상태만 변경 가능 (삭제 불가)
```

#### 2. 커스텀 확장자 관리
```
✓ 최대 200개까지 추가 가능 (실시간 카운터 표시)
✓ 입력 길이 20자 제한
✓ 중복 확장자 추가 방지
✓ X 버튼 클릭으로 삭제
✓ 추가 시 자동으로 차단 상태로 등록
```

#### 3. 파일 업로드 검증
```
✓ 업로드 전 확장자 검사
✓ 차단된 확장자 업로드 시 거부
✓ 이중 확장자 검증 (file.exe.txt 등)
✓ 검증 결과 실시간 피드백
```

#### 4. 사용자 인증
```
✓ Google OAuth2 소셜 로그인
✓ 로그인 없이도 고정 확장자 조회 가능
✓ 커스텀 확장자 추가/삭제는 로그인 필수
✓ 사용자별 변경 이력 추적
```

#### 5. 변경 이력 추적
```
✓ 모든 확장자 변경사항 로깅
✓ 누가(사용자), 언제(시간), 무엇을(확장자), 어떻게(추가/삭제/차단/허용) 기록
✓ 실시간 이력 조회 (최근 20개)
```

<br>

---

## 🚀 요건 이외 고려사항

### 1. 보안 강화 🔒

#### 이중 확장자 검증
**문제점**:
- 악의적 사용자가 `virus.exe.txt` 같은 파일로 차단을 우회할 수 있음
- 일반적으로 마지막 확장자(`.txt`)만 검사하면 실행 파일(`.exe`)을 숨길 수 있음

**해결책**:
```java
// ExtensionValidator.java
public static String[] extractAllExtensions(String filename) {
    // "file.exe.txt" → ["exe", "txt"] 모두 추출
    String[] parts = filename.split("\\.");
    // 모든 확장자를 검사하여 하나라도 차단되면 업로드 거부
}
```

**효과**:
- `document.exe.txt` → ❌ 차단 (exe가 검출됨)
- `backup.tar.gz` → ✅ 허용 (tar, gz 모두 미차단 시)

#### 대소문자 정규화
**문제점**:
- `Exe`, `EXE`, `exe` 등 대소문자 조합으로 차단 우회 가능

**해결책**:
```java
// 모든 확장자를 소문자로 정규화하여 저장/비교
public static String normalize(String extension) {
    return extension.toLowerCase().replace(".", "").trim();
}
```

**효과**:
- `test.EXE` → ❌ 차단
- `script.Sh` → ❌ 차단
- 대소문자 구분 없이 일관된 차단

#### 입력 검증 강화
```java
// 영문자, 숫자만 허용 (특수문자 차단)
private static final Pattern VALID_EXTENSION_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

// 최대 길이 20자 제한
private static final int MAX_EXTENSION_LENGTH = 20;
```

**방어 대상**:
- SQL Injection: `exe'; DROP TABLE--`
- XSS: `<script>alert(1)</script>`
- 특수문자: `exe!@#$%`

---

### 2. 아키텍처 개선 🏗️

#### 테이블 통합 설계

**초기 설계 (Before)**:
```
┌─────────────────────┐  ┌─────────────────────┐
│  fixed_extensions   │  │  custom_extensions  │
├─────────────────────┤  ├─────────────────────┤
│ id (PK)             │  │ id (PK)             │
│ extension           │  │ extension           │
│ is_blocked          │  │ user_id (FK)        │
│ ...                 │  │ ...                 │
└─────────────────────┘  └─────────────────────┘
```
**문제점**:
- 코드 중복 (Controller, Service, Repository 각각 2개씩)
- 전체 확장자 조회 시 UNION 쿼리 필요
- 확장자 검증 로직 분산

**개선된 설계 (After)**:
```
┌────────────────────────────────┐
│        extensions              │
├────────────────────────────────┤
│ id (PK)                        │
│ extension (UNIQUE)             │
│ is_fixed (Boolean)  ← 구분 플래그│
│ is_blocked (Boolean)           │
│ created_by (FK) → users        │
│ updated_by (FK) → users        │
│ created_at                     │
│ updated_at                     │
└────────────────────────────────┘
```

**개선 효과**:
```
✅ Controller 2개 → 1개 (50% 감소)
✅ Service 2개 → 1개 (50% 감소)  
✅ Repository 2개 → 1개 (50% 감소)
✅ 단순 쿼리: SELECT * FROM extensions WHERE is_fixed = true
✅ 유지보수 용이: 비즈니스 로직 한 곳에서 관리
```

#### 도메인 주도 설계 (DDD) 적용
```
src/main/java/com/flow/fileextension/
├── domain/                    # 도메인 계층
│   ├── extension/            # 확장자 도메인
│   │   ├── controller/       # API 진입점
│   │   ├── dto/             # 데이터 전송 객체
│   │   ├── entity/          # JPA 엔티티
│   │   ├── repository/      # 데이터 접근
│   │   └── service/         # 비즈니스 로직
│   └── user/                # 사용자 도메인
│       └── ...
├── global/                   # 전역 설정
│   ├── config/              # Spring 설정
│   ├── constants/           # 상수
│   ├── exception/           # 예외 처리
│   ├── response/            # API 응답 포맷
│   ├── security/            # OAuth2 설정
│   └── util/                # 유틸리티
└── service/                 # 애플리케이션 서비스
```

**장점**:
- 도메인별 응집도 향상
- 의존성 방향 명확화 (domain ← global)
- 테스트 용이성 증가

---

### 3. 사용자 경험 개선 🎨

#### OAuth2 소셜 로그인
**구현 이유**:
- 회원가입 절차 간소화
- 비밀번호 관리 부담 제거
- Google 인증 보안 활용

**인증 흐름**:
```
사용자 → "로그인" 클릭 
    → Google 로그인 페이지
    → 인증 성공 
    → 사용자 정보 저장
    → 세션 생성
    → 메인 페이지 리다이렉트
```

**권한 분리**:
| 기능 | 비로그인 | 로그인 |
|------|----------|--------|
| 고정 확장자 조회 | ✅ | ✅ |
| 커스텀 확장자 조회 | ✅ | ✅ |
| 고정 확장자 차단/허용 | ❌ | ✅ |
| 커스텀 확장자 추가 | ❌ | ✅ |
| 커스텀 확장자 삭제 | ❌ | ✅ |
| 파일 업로드 검증 | ✅ | ✅ |

#### 한글 에러 메시지
```java
// ErrorMessages.java - 상수로 중앙 관리
public class ErrorMessages {
    public static final String EXTENSION_EMPTY = "확장자를 입력해주세요.";
    public static final String EXTENSION_TOO_LONG = "확장자는 최대 20자까지 입력 가능합니다.";
    public static final String EXTENSION_INVALID_FORMAT = "확장자는 영문자와 숫자만 입력 가능합니다.";
    public static final String EXTENSION_DUPLICATE = "이미 등록된 확장자입니다";
    public static final String EXTENSION_MAX_COUNT = "커스텀 확장자는 최대 200개까지 추가할 수 있습니다.";
    // ...
}
```

**장점**:
- 사용자 친화적
- 에러 원인 명확히 전달
- 메시지 일관성 유지

#### 실시간 변경 이력
```javascript
// 최근 20개 변경 이력 표시
- 2024-01-15 14:30 | 홍길동 | zip 추가
- 2024-01-15 14:25 | 홍길동 | exe 차단
- 2024-01-15 14:20 | 김철수 | pdf 삭제
```

**효과**:
- 누가 무엇을 변경했는지 투명하게 공개
- 문제 발생 시 추적 용이
- 팀 협업 시 변경사항 공유

---

### 4. 코드 품질 개선 📝

#### 유틸리티 클래스 분리
**Before**:
```java
// Service에 검증 로직이 섞여있음
public void addExtension(String ext) {
    if (ext == null || ext.isEmpty()) throw new Exception();
    if (ext.length() > 20) throw new Exception();
    if (!ext.matches("^[a-zA-Z0-9]+$")) throw new Exception();
    String normalized = ext.toLowerCase().trim();
    // ...
}
```

**After**:
```java
// 검증 로직을 유틸리티로 분리
public void addExtension(String ext) {
    ExtensionValidator.validate(ext);  // 모든 검증
    String normalized = ExtensionValidator.normalize(ext);  // 정규화
    // ...
}
```

**장점**:
- 단일 책임 원칙 (SRP) 준수
- 재사용성 증가
- 테스트 용이

#### 전역 예외 처리
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(e.getMessage()));
    }
    
    // ...
}
```

**효과**:
- 모든 컨트롤러에서 일관된 에러 응답
- 중복 코드 제거
- 에러 처리 로직 중앙 관리

#### 체계적인 로깅
```java
@Slf4j
public class ExtensionService {
    public ExtensionResponseDto addCustomExtension(String ext, User user) {
        log.info("커스텀 확장자 추가 시도: {} (사용자: {})", ext, user.getName());
        
        // 비즈니스 로직
        
        log.info("커스텀 확장자 추가 완료: {}", normalized);
        return dto;
    }
}
```

**활용**:
- 운영 중 문제 추적
- 사용자 행동 분석
- 성능 모니터링

---

### 5. 배포 및 운영 🚀

#### Docker 컨테이너화
```yaml
# docker-compose.yml
services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=db
    depends_on:
      - db
  
  frontend:
    build: ./frontend
    ports:
      - "3000:80"
  
  db:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=fileextension
      - MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
```

**장점**:
- 환경 일관성 보장
- 배포 자동화
- 격리된 실행 환경

#### 자동 배포 스크립트
```bash
# deploy.sh
#!/bin/bash

# t2.micro 메모리 최적화 감지
if [ "$TOTAL_MEM" -lt 2000 ]; then
    COMPOSE_FILE="docker-compose.micro.yml"  # 경량 설정 사용
fi

# 헬스 체크
if curl -f http://localhost:8080/api/extensions/fixed; then
    echo "✅ 배포 성공"
else
    echo "⚠️ 배포 실패 - 로그 확인 필요"
fi
```

**특징**:
- AWS EC2 t2.micro 지원
- 자동 헬스 체크
- 에러 알림

---

### 6. 테스트 작성 🧪

```java
@SpringBootTest
class ExtensionServiceTest {
    
    @Test
    @DisplayName("커스텀 확장자 추가 - 성공")
    void addCustomExtension_Success() {
        // given
        String extension = "pdf";
        
        // when
        Extension result = extensionService.addCustomExtension(extension, testUser);
        
        // then
        assertThat(result.getExtension()).isEqualTo("pdf");
        assertThat(result.isFixed()).isFalse();
        assertThat(result.isBlocked()).isTrue();
    }
    
    @Test
    @DisplayName("중복 확장자 추가 - 실패")
    void addCustomExtension_Duplicate_ThrowsException() {
        // given
        extensionService.addCustomExtension("zip", testUser);
        
        // when & then
        assertThatThrownBy(() -> extensionService.addCustomExtension("zip", testUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이미 등록된 확장자");
    }
}
```

**커버리지**:
- 서비스 로직: 90%+
- 컨트롤러: 85%+
- 유틸리티: 100%

<br>

---

## 🛠 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 | 프로그래밍 언어 |
| Spring Boot | 3.2.4 | 백엔드 프레임워크 |
| Spring Data JPA | 3.2.4 | ORM (데이터 접근) |
| Spring Security | 6.2.3 | 인증/인가 |
| OAuth2 Client | - | 소셜 로그인 |
| MySQL | 8.0 | 관계형 데이터베이스 |
| Lombok | - | 보일러플레이트 코드 제거 |
| Gradle | 8.x | 빌드 도구 |

### Frontend
| 기술 | 버전 | 용도 |
|------|------|------|
| React | 18 | UI 프레임워크 |
| Axios | - | HTTP 클라이언트 |
| CSS3 | - | 스타일링 |

### DevOps
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 다중 컨테이너 오케스트레이션 |
| AWS EC2 | 서버 호스팅 |
| GitHub | 버전 관리 |

<br>

---

## 📊 ERD & 아키텍처

### ERD (Entity Relationship Diagram)
```
┌────────────────────────────────┐
│           users                │
├────────────────────────────────┤
│ id (PK)                        │
│ email (UNIQUE)                 │
│ name                           │
│ profile_image                  │
│ created_at                     │
│ last_login_at                  │
└────────────────────────────────┘
         ↑                    ↑
         │                    │
         │ created_by     updated_by
         │                    │
┌────────────────────────────────┐
│        extensions              │
├────────────────────────────────┤
│ id (PK)                        │
│ extension (UNIQUE, VARCHAR(20))│
│ is_fixed (BOOLEAN)             │  ← 고정/커스텀 구분
│ is_blocked (BOOLEAN)           │  ← 차단 여부
│ created_by (FK)                │
│ updated_by (FK)                │
│ created_at                     │
│ updated_at                     │
└────────────────────────────────┘
```

### 테이블 설명

#### 📌 users 테이블
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 사용자 고유 ID |
| email | VARCHAR (UNIQUE) | 이메일 (Google OAuth) |
| name | VARCHAR | 사용자 이름 |
| profile_image | VARCHAR | 프로필 이미지 URL |
| created_at | DATETIME | 가입 일시 |
| last_login_at | DATETIME | 최근 로그인 일시 |

#### 📌 extensions 테이블
| 컬럼 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| id | BIGINT (PK) | 확장자 고유 ID | AUTO_INCREMENT |
| extension | VARCHAR(20) | 확장자명 (예: exe, pdf) | UNIQUE, NOT NULL |
| is_fixed | BOOLEAN | 고정 확장자 여부 | NOT NULL |
| is_blocked | BOOLEAN | 차단 여부 | NOT NULL |
| created_by | BIGINT (FK) | 생성자 | → users.id |
| updated_by | BIGINT (FK) | 수정자 | → users.id |
| created_at | DATETIME | 생성 일시 | NOT NULL |
| updated_at | DATETIME | 수정 일시 | NOT NULL |

### 아키텍처 다이어그램
```
┌─────────────────────────────────────────────────────────────┐
│                         클라이언트                            │
│                    (React Frontend)                         │
│                  http://localhost:3000                      │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/HTTPS
                           │ REST API
┌──────────────────────────┴──────────────────────────────────┐
│                    Spring Boot Backend                       │
│                  http://localhost:8080                      │
├──────────────────────────────────────────────────────────────┤
│  ┌────────────────┐  ┌──────────────────┐  ┌─────────────┐ │
│  │  Controller    │  │    Service       │  │ Repository  │ │
│  │  (REST API)    │→ │ (Business Logic) │→ │ (JPA)       │ │
│  └────────────────┘  └──────────────────┘  └──────┬──────┘ │
│                                                     │         │
│  ┌─────────────────────────────────────────────────┘         │
│  │  Spring Security + OAuth2                                │
│  │  (인증/인가)                                              │
│  └──────────────────────────────────────────────────────────│
└──────────────────────────┬──────────────────────────────────┘
                           │ JDBC
                           │
┌──────────────────────────┴──────────────────────────────────┐
│                       MySQL 8.0                             │
│                   (관계형 데이터베이스)                       │
│                  localhost:3306/fileextension               │
└──────────────────────────────────────────────────────────────┘
```

### 데이터 흐름

#### 1️⃣ 커스텀 확장자 추가 흐름
```
사용자 입력 "pdf" 
    → POST /api/extensions/custom
    → ExtensionController.addCustomExtension()
    → ExtensionService.addCustomExtension()
        ├─ ExtensionValidator.validate("pdf")  // 형식 검증
        ├─ ExtensionValidator.normalize("pdf") // 정규화
        ├─ 중복 체크 (Repository)
        ├─ 최대 개수 체크 (200개)
        └─ Extension 엔티티 생성 및 저장
    → DB INSERT
    → 클라이언트에 ExtensionResponseDto 반환
    → 화면 갱신
```

#### 2️⃣ 파일 업로드 검증 흐름
```
파일 업로드 "virus.exe.txt"
    → POST /api/files/check
    → FileCheckController.checkFile()
    → FileCheckService.validateFile()
        ├─ ExtensionValidator.extractAllExtensions() // ["exe", "txt"]
        ├─ DB에서 차단된 확장자 목록 조회
        ├─ 모든 확장자 검증
        │   └─ "exe"가 차단 목록에 있음 발견!
        └─ ValidationResult 반환 (isValid: false)
    → 클라이언트에 검증 실패 응답
    → 사용자에게 에러 메시지 표시
```

<br>

---

## 📁 프로젝트 구조

```
file-extension-blocker/
│
├── src/
│   ├── main/
│   │   ├── java/com/flow/fileextension/
│   │   │   ├── domain/
│   │   │   │   ├── auth/
│   │   │   │   │   └── controller/
│   │   │   │   │       └── AuthController.java      # 인증 API
│   │   │   │   ├── extension/
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   └── ExtensionController.java # 확장자 API
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── ExtensionRequestDto.java
│   │   │   │   │   │   └── ExtensionResponseDto.java
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   └── Extension.java           # JPA 엔티티
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── ExtensionRepository.java # JPA Repository
│   │   │   │   │   └── service/
│   │   │   │   │       └── ExtensionService.java    # 비즈니스 로직
│   │   │   │   └── user/
│   │   │   │       ├── controller/
│   │   │   │       ├── entity/
│   │   │   │       │   └── User.java
│   │   │   │       └── repository/
│   │   │   │           └── UserRepository.java
│   │   │   ├── global/
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java          # Spring Security 설정
│   │   │   │   │   └── WebConfig.java               # CORS 설정
│   │   │   │   ├── constants/
│   │   │   │   │   └── ErrorMessages.java           # 에러 메시지 상수
│   │   │   │   ├── exception/
│   │   │   │   │   └── GlobalExceptionHandler.java  # 전역 예외 처리
│   │   │   │   ├── response/
│   │   │   │   │   └── ApiResponse.java             # API 응답 포맷
│   │   │   │   ├── security/
│   │   │   │   │   ├── CustomOAuth2User.java
│   │   │   │   │   ├── CustomOAuth2UserService.java
│   │   │   │   │   ├── OAuth2SuccessHandler.java
│   │   │   │   │   ├── OAuthAttributes.java
│   │   │   │   │   └── SessionUser.java
│   │   │   │   └── util/
│   │   │   │       └── ExtensionValidator.java      # 확장자 검증 유틸
│   │   │   ├── service/
│   │   │   │   ├── FileCheckService.java            # 파일 검증 서비스
│   │   │   │   └── FileService.java
│   │   │   ├── controller/
│   │   │   │   └── FileCheckController.java         # 파일 검증 API
│   │   │   └── FileExtensionBlockerApplication.java
│   │   └── resources/
│   │       ├── application.properties               # 개발 환경 설정
│   │       ├── application-prod.properties          # 운영 환경 설정
│   │       └── templates/
│   └── test/
│       └── java/com/flow/fileextension/
│           ├── domain/extension/
│           │   ├── controller/
│           │   │   └── ExtensionControllerTest.java
│           │   └── service/
│           │       └── ExtensionServiceTest.java
│           ├── global/util/
│           │   └── ExtensionValidatorTest.java
│           └── service/
│               └── FileServiceTest.java
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   │   ├── CustomExtensions.js                     # 커스텀 확장자 목록
│   │   │   ├── ExtensionInput.js                       # 확장자 입력 폼
│   │   │   ├── ExtensionHistory.js                     # 변경 이력
│   │   │   ├── FileExtensionChecker.js                 # 파일 검증 UI
│   │   │   └── FixedExtensions.js                      # 고정 확장자 체크박스
│   │   ├── services/
│   │   │   └── api.js                                  # Axios API 클라이언트
│   │   ├── App.js                                      # 메인 컴포넌트
│   │   ├── App.css
│   │   └── index.js
│   ├── package.json
│   ├── Dockerfile                                       # Frontend 도커 이미지
│   └── nginx.conf                                       # Nginx 설정
│
├── build.gradle                                         # Gradle 빌드 설정
├── Dockerfile                                           # Backend 도커 이미지
├── docker-compose.yml                                   # 일반 서버용
├── docker-compose.micro.yml                             # t2.micro 최적화
├── deploy.sh                                            # 자동 배포 스크립트
├── .env.example                                         # 환경변수 템플릿
└── README.md
```

<br>

---

## 🚀 실행 방법

### 사전 요구사항
- **Java 17** 이상
- **Node.js 18** 이상
- **MySQL 8.0** (또는 Docker)
- **Git**

---

### 1️⃣ 로컬 실행 (개발 환경)

#### Step 1: 저장소 클론
```bash
git clone https://github.com/your-repo/file-extension-blocker.git
cd file-extension-blocker
```

#### Step 2: 환경 변수 설정
```bash
cp .env.example .env
# .env 파일을 열어 아래 값 입력
```

`.env` 파일 예시:
```bash
# MySQL 설정
DB_USERNAME=root
DB_PASSWORD=your_password
DB_HOST=localhost
DB_PORT=3306
DB_NAME=fileextension

# Google OAuth2 설정 (https://console.cloud.google.com)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Frontend URL (CORS용)
FRONTEND_URL=http://localhost:3000

# Backend URL
BACKEND_URL=http://localhost:8080
```

#### Step 3: 데이터베이스 생성
```sql
CREATE DATABASE fileextension CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### Step 4: Backend 실행
```bash
# Gradle 빌드 및 실행
./gradlew bootRun

# 또는
./gradlew build
java -jar build/libs/file-extension-blocker-0.0.1-SNAPSHOT.jar
```

실행 확인:
```bash
curl http://localhost:8080/api/extensions/fixed
# → 고정 확장자 8개 반환되면 성공
```

#### Step 5: Frontend 실행
```bash
cd frontend
npm install
npm start
```

브라우저에서 접속:
```
http://localhost:3000
```

---

### 2️⃣ Docker Compose 실행 (프로덕션 환경)

#### Step 1: 환경 변수 설정
```bash
cp .env.example .env
# Google OAuth2 정보 입력 필수
```

#### Step 2: Docker Compose 실행
```bash
# 일반 서버 (메모리 2GB 이상)
docker-compose up -d

# AWS t2.micro (메모리 1GB)
docker-compose -f docker-compose.micro.yml up -d
```

#### Step 3: 헬스 체크
```bash
# Backend 상태 확인
curl http://localhost:8080/api/extensions/fixed

# Frontend 상태 확인
curl http://localhost:3000

# 컨테이너 상태 확인
docker-compose ps
```

#### Step 4: 로그 확인
```bash
# 전체 로그
docker-compose logs -f

# Backend만
docker-compose logs -f backend

# Frontend만
docker-compose logs -f frontend
```

#### Step 5: 중지 및 제거
```bash
# 중지
docker-compose down

# 완전 제거 (볼륨 포함)
docker-compose down -v
```

---

### 3️⃣ 자동 배포 스크립트 사용

#### AWS EC2에서 실행 (Ubuntu 기준)
```bash
# 1. 저장소 클론
git clone https://github.com/your-repo/file-extension-blocker.git
cd file-extension-blocker

# 2. 환경 변수 설정
cp .env.example .env
nano .env  # Google OAuth2 정보 입력

# 3. 배포 스크립트 실행
chmod +x deploy.sh
./deploy.sh

# 4. 최신 코드 반영하여 재배포
./deploy.sh --pull
```

배포 스크립트 기능:
- ✅ 메모리 자동 감지 (t2.micro 최적화)
- ✅ 이전 컨테이너 정리
- ✅ 이미지 빌드 및 실행
- ✅ 헬스 체크 자동 실행
- ✅ 배포 성공/실패 알림

---

### 접속 URL

| 환경 | URL | 설명 |
|------|-----|------|
| Frontend (개발) | http://localhost:3000 | React 개발 서버 |
| Backend (개발) | http://localhost:8080 | Spring Boot 서버 |
| Frontend (프로덕션) | http://your-domain:3000 | Nginx + React |
| Backend (프로덕션) | http://your-domain:8080 | Spring Boot |
| MySQL | localhost:3306 | 데이터베이스 |

<br>

---

## 🎨 API 명세

자세한 API 명세는 별도 문서를 참고해주세요.

### 주요 엔드포인트

#### 고정 확장자
- `GET /api/extensions/fixed` - 목록 조회
- `PATCH /api/extensions/fixed/{id}/block` - 차단 상태 변경

#### 커스텀 확장자
- `GET /api/extensions/custom` - 목록 조회
- `POST /api/extensions/custom` - 추가
- `DELETE /api/extensions/custom/{id}` - 삭제

#### 파일 검증
- `POST /api/files/check` - 파일 확장자 검증

#### 인증
- `GET /api/user/me` - 현재 사용자 정보
- `POST /api/auth/logout` - 로그아웃

<br>

---

## 🧪 테스트

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 클래스만
./gradlew test --tests ExtensionServiceTest

# 커버리지 리포트 생성
./gradlew test jacocoTestReport
```

### 테스트 커버리지
- **전체**: 85%+
- **Service 계층**: 90%+
- **Util 계층**: 100%
- **Controller 계층**: 85%+

### 테스트 시나리오

#### 기본 기능
- [x] 고정 확장자 체크/언체크
- [x] 커스텀 확장자 추가/삭제
- [x] 새로고침 시 상태 유지

#### 검증 기능
- [x] 중복 확장자 추가 시 에러
- [x] 200개 초과 시 에러
- [x] 20자 초과 입력 시 에러
- [x] 특수문자 입력 시 에러

#### 보안 기능
- [x] 차단된 확장자 업로드 시 거부
- [x] 이중 확장자 검증
- [x] 대소문자 구분 없이 차단

<br>

---

## 📞 문의 및 피드백

### 채용 관련
- **회사**: 플로우(Flow)
- **포지션**: 백엔드 개발자
- **채용 공고**: https://www.wanted.co.kr/wd/115470

### 프로젝트 관련
- **GitHub**: [저장소 주소]

---

<div align="center">
  
**감사합니다! 🙏**

이 프로젝트는 플로우 채용 과제로 제작되었습니다.

</div>
