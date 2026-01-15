# 파일 확장자 차단 시스템

배포 URL: http://15.164.100.213.nip.io:3000/ <br>
GitHub: https://github.com/0525aa36/MadrasCheck_Homework

---

## 목차

1. [과제 요구사항](#1-과제-요구사항)
2. [요구사항 이외 구현사항](#2-요구사항-이외-구현사항)
3. [ERD](#3-erd)
4. [아키텍처](#4-아키텍처)
5. [구현 시 고민과 의사결정](#5-구현-시-고민과-의사결정)
6. [화면](#6-화면)
7. [실행 방법](#7-실행-방법)

---

## 1. 과제 요구사항

### 필수 구현 사항

#### 고정 확장자
- 8개 고정 확장자 제공: bat, cmd, com, cpl, exe, scr, js, sh
- 기본값은 체크 해제(차단되지 않음)
- 체크/언체크 시 DB에 저장
- 새로고침 시에도 상태 유지
- 커스텀 확장자 영역에 표시되지 않음

#### 커스텀 확장자
- 확장자 입력 (최대 20자)
- 추가 버튼 클릭 시 DB 저장 및 화면 표시
- 최대 200개까지 추가 가능
- 추가된 개수 표시 (예: 3/200)
- X 버튼으로 삭제

---

## 2. 요구사항 이외 구현사항

### OAuth2 소셜 로그인 및 권한 관리

**구현 배경**
실제 SaaS 서비스에서는 회원가입 절차가 진입 장벽이 될 수 있습니다. Google 소셜 로그인을 통해 사용자 편의성을 높이고자 했습니다.

또한 파일 확장자 차단 시스템은 보안과 직결된 민감한 설정이므로, 아무나 차단 설정을 변경할 수 있다면 악의적인 사용자가 차단을 해제하여 위험한 파일을 업로드할 수 있습니다. 이에 인증된 사용자만 수정할 수 있도록 권한을 분리했습니다.

**구현 내용**
- Spring Security OAuth2 Client 사용
- Google OAuth2 인증
- 최초 로그인 시 자동 회원가입
- 세션 기반 인증 상태 관리

**권한 설계**

```java
// SecurityConfig.java
http.authorizeHttpRequests(authorize -> authorize
    // 조회 API (비로그인 허용)
    .requestMatchers("/api/extensions/fixed").permitAll()
    .requestMatchers("/api/extensions/custom").permitAll()
    .requestMatchers("/api/file/check").permitAll()
    
    // 수정 API (로그인 필수)
    .requestMatchers("/api/extensions/**").authenticated()
)
```

**권한 구분**

| 기능 | 비인증 사용자 | 인증 사용자 |
|------|--------------|------------|
| 고정 확장자 조회 | O | O |
| 커스텀 확장자 조회 | O | O |
| 차단 목록 조회 | O | O |
| 파일 검증 | O | O |
| 고정 확장자 차단/허용 | X | O |
| 커스텀 확장자 추가 | X | O |
| 커스텀 확장자 삭제 | X | O |

**설계 의도**

조회를 허용한 이유:
- 확장자 목록 자체는 민감한 정보가 아님
- 사용자가 서비스를 미리 체험해볼 수 있음
- 가입 전에도 파일 검증 기능을 사용할 수 있어 편의성 증대

수정을 제한한 이유:
- 차단 설정은 전체 시스템 보안에 영향
- 무분별한 차단 해제 방지
- 변경 이력 추적 필요 (누가 변경했는지)
- 책임 소재 명확화

### 이중 확장자 검증

**구현 배경**
과제 명세에는 명시되지 않았지만, 실제 서비스에서는 `virus.exe.txt`와 같이 이중 확장자를 사용하여 차단을 우회하려는 시도가 있을 수 있다고 판단했습니다.

**구현 방법**
파일명에서 모든 확장자를 추출하여 각각 검증합니다.

```java
// ExtensionValidator.java
public static String[] extractAllExtensions(String filename) {
    String[] parts = filename.split("\\.");
    // 첫 번째 요소는 파일명이므로 제외
    return Arrays.copyOfRange(parts, 1, parts.length);
}
```

**검증 로직**
```java
// FileCheckService.java
public boolean isFileExtensionBlocked(MultipartFile file) {
    String[] allExtensions = ExtensionValidator.extractAllExtensions(filename);
    
    for (String ext : allExtensions) {
        if (extensionRepository.findByExtension(ext).isBlocked()) {
            return true; // 하나라도 차단되면 전체 차단
        }
    }
    return false;
}
```

**효과**
- `document.exe.txt` → exe가 차단되어 있으면 업로드 거부
- `backup.tar.gz` → tar와 gz 모두 허용되어야 업로드 가능

### 대소문자 정규화

**구현 배경**
사용자가 `exe`, `EXE`, `Exe` 등 다양한 형태로 입력할 수 있고, 이를 모두 동일하게 처리해야 한다고 판단했습니다.

**구현 방법**
저장 전 모든 확장자를 소문자로 변환합니다.

```java
public static String normalize(String extension) {
    return extension.toLowerCase().trim();
}
```

### 사용자별 변경 이력 추적

**구현 배경**
실무에서는 누가 언제 어떤 설정을 변경했는지 추적할 수 있어야 합니다. 특히 보안 관련 설정의 경우 감사 로그가 필수적이라고 생각했습니다.

**구현 방법**
Extension 엔티티에 생성자, 수정자, 생성일시, 수정일시를 기록합니다.

```java
@Entity
public class Extension {
    @ManyToOne
    private User createdBy;
    
    @ManyToOne
    private User updatedBy;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 입력 검증

**구현 배경**
악의적인 입력으로부터 시스템을 보호해야 합니다.

**구현 내용**
- 길이 제한: 최대 20자
- 허용 문자: 영문자, 숫자만
- 정규식 검증: `^[a-zA-Z0-9]+$`
- SQL Injection, XSS 방어

### 한글 에러 메시지

**구현 배경**
사용자 친화적인 서비스를 위해 에러 메시지를 한글로 제공하고, 상수 클래스로 중앙 관리하여 일관성을 유지했습니다.

```java
public class ErrorMessages {
    public static final String EXTENSION_EMPTY = "확장자를 입력해주세요.";
    public static final String EXTENSION_TOO_LONG = "확장자는 최대 20자까지 입력 가능합니다.";
    public static final String EXTENSION_DUPLICATE = "이미 등록된 확장자입니다";
    // ...
}
```

---

## 3. ERD

```
┌─────────────────────────┐
│         users           │
├─────────────────────────┤
│ id (PK)                 │
│ email (UNIQUE)          │
│ name                    │
│ profile_image           │
│ created_at              │
│ last_login_at           │
└─────────────────────────┘
         ↑          ↑
         │          │
   created_by   updated_by
         │          │
┌─────────────────────────┐
│      extensions         │
├─────────────────────────┤
│ id (PK)                 │
│ extension (UNIQUE)      │
│ is_fixed (BOOLEAN)      │
│ is_blocked (BOOLEAN)    │
│ created_by (FK)         │
│ updated_by (FK)         │
│ created_at              │
│ updated_at              │
└─────────────────────────┘
```

### 테이블 설계 의도

**users 테이블**
- OAuth2 로그인 정보 저장
- 확장자 변경 이력 추적을 위한 사용자 정보

**extensions 테이블**
- 고정 확장자와 커스텀 확장자를 하나의 테이블로 통합
- `is_fixed` 플래그로 구분
- 코드 중복 제거 및 쿼리 단순화

### 인덱스 전략

```sql
CREATE INDEX idx_extension ON extensions(extension);
CREATE INDEX idx_is_blocked ON extensions(is_blocked);
CREATE INDEX idx_is_fixed ON extensions(is_fixed);
```

- `extension`: 중복 검사 및 검색 성능 향상
- `is_blocked`: 차단 목록 조회 성능
- `is_fixed`: 고정/커스텀 확장자 분리 조회 성능

---

## 4. 아키텍처

### 전체 시스템 구조

<img width="4728" height="3004" alt="image" src="https://github.com/user-attachments/assets/e0f76442-6b1b-4c71-8d48-47f9ccd9fb08" />

**배포 구성 요소**

**EC2 Instance**
- 인스턴스 타입: t2.micro (프리티어)
- OS: Ubuntu 22.04 LTS
- 메모리 최적화: docker-compose.micro.yml 사용
- Security Group: HTTP(80), HTTPS(443), SSH(22)

**Docker Compose**
- 컨테이너 오케스트레이션
- 서비스 간 네트워크 구성
- 볼륨 마운트를 통한 데이터 영속성

**네트워크 구성**
- app-network: Bridge 네트워크로 컨테이너 간 통신
- Nginx → Backend: 리버스 프록시
- Backend → MySQL: JDBC 연결

### 기술 스택

**Backend**
- Java 17
- Spring Boot 3.2.4
- Spring Data JPA
- Spring Security + OAuth2
- MySQL 8.0
- Lombok

**Frontend**
- React 18
- Axios
- CSS3

**DevOps**
- Docker
- Docker Compose
- Nginx
- AWS EC2

---
## 5. 구현 시 고민과 의사결정

### 백엔드 구현

#### 고정 확장자를 DB에 저장할 것인가?

**고민 과정**
과제 명세에서 "고정 확장자를 check or uncheck를 할 경우 db에 저장됩니다"라는 요구사항이 있었습니다. 이를 두 가지 방식으로 해석할 수 있었습니다.

1. 고정 확장자 자체는 코드에 하드코딩하고, 차단 상태만 DB에 저장
2. 고정 확장자도 DB에 저장하되, `is_fixed` 플래그로 구분

**선택한 방법**
2번 방법을 선택했습니다. 애플리케이션 시작 시 초기화 로직에서 8개 고정 확장자를 DB에 저장합니다.

```java
@PostConstruct
public void initializeDefaultExtensions() {
    for (String ext : DEFAULT_FIXED_EXTENSIONS) {
        extensionRepository.findByExtension(ext)
            .ifPresentOrElse(
                existing -> log.info("확장자 존재: {}", ext),
                () -> {
                    Extension newExt = Extension.builder()
                        .extension(ext)
                        .isFixed(true)
                        .isBlocked(false)
                        .build();
                    extensionRepository.save(newExt);
                }
            );
    }
}
```

**선택 이유**
- 고정 확장자와 커스텀 확장자의 조회 로직을 통합할 수 있음
- 전체 차단 목록을 가져올 때 단일 쿼리로 처리 가능
- `is_fixed` 플래그로 삭제 방지 및 권한 제어 가능
- 향후 고정 확장자를 관리자 페이지에서 추가할 수 있도록 확장 가능

#### 테이블 통합 vs 분리

**고민 과정**
고정 확장자와 커스텀 확장자를 별도 테이블로 관리할지, 하나의 테이블로 통합할지 고민했습니다.

**분리할 경우**
```
fixed_extensions
custom_extensions
```
- 명확한 분리
- 각각 독립적인 관리

**통합할 경우**
```
extensions (is_fixed 플래그 사용)
```
- 코드 중복 제거
- 쿼리 단순화

**선택한 방법**
통합 테이블을 선택했습니다.

**선택 이유**
- Controller, Service, Repository가 각각 2개씩 필요 없음
- 전체 확장자 조회 시 UNION 불필요
- 비즈니스 로직 한 곳에서 관리 가능
- 확장자 개수 제한(200개)을 커스텀만 적용하기 쉬움

#### 사용자별 확장자 관리 vs 전역 관리

**고민 과정**
커스텀 확장자를 사용자별로 관리할지, 전역으로 관리할지 고민했습니다.

**사용자별 관리**
- 각 사용자가 자신만의 차단 목록 관리
- `user_id` 컬럼 추가 필요
- 복잡도 증가

**전역 관리**
- 모든 사용자가 동일한 차단 목록 공유
- 구현 단순
- 조직 단위 서비스에 적합

**선택한 방법**
전역 관리를 선택했습니다.

**선택 이유**
- 과제 명세에 사용자별 관리 요구사항 없음
- 플로우는 협업 툴이므로 조직 단위 관리가 더 적합하다고 판단
- 추후 `organization_id` 추가로 쉽게 확장 가능

#### 인증된 사용자만 차단 설정 수정 가능

**구현 배경**
파일 확장자 차단 시스템은 보안과 직결된 민감한 설정입니다. 아무나 차단 설정을 변경할 수 있다면 악의적인 사용자가 차단을 해제하여 위험한 파일을 업로드할 수 있습니다.

**고민 과정**
1. 모든 기능을 로그인 필수로 할 것인가?
2. 일부 기능만 로그인 필수로 할 것인가?

**선택한 방법**
조회와 검증은 비로그인 사용자도 가능하지만, 수정은 인증된 사용자만 가능하도록 구현했습니다.

```java
// SecurityConfig.java
http.authorizeHttpRequests(authorize -> authorize
    // 조회 API (비로그인 허용)
    .requestMatchers("/api/extensions/fixed").permitAll()
    .requestMatchers("/api/extensions/custom").permitAll()
    .requestMatchers("/api/file/check").permitAll()
    
    // 수정 API (로그인 필수)
    .requestMatchers("/api/extensions/**").authenticated()
)
```

**권한 구분**

| 기능 | 비인증 사용자 | 인증 사용자 |
|------|--------------|------------|
| 고정 확장자 조회 | O | O |
| 커스텀 확장자 조회 | O | O |
| 차단 목록 조회 | O | O |
| 파일 검증 | O | O |
| 고정 확장자 차단/허용 | X | O |
| 커스텀 확장자 추가 | X | O |
| 커스텀 확장자 삭제 | X | O |

**선택 이유**

조회를 허용한 이유:
- 확장자 목록 자체는 민감한 정보가 아님
- 사용자가 서비스를 미리 체험해볼 수 있음
- 가입 전에도 파일 검증 기능을 사용할 수 있어 편의성 증대

수정을 제한한 이유:
- 차단 설정은 전체 시스템 보안에 영향
- 무분별한 차단 해제 방지
- 변경 이력 추적 필요 (누가 변경했는지)
- 책임 소재 명확화

**프론트엔드에서의 처리**
```javascript
// FixedExtensions.js
const handleToggle = async (id, currentStatus) => {
    if (!isAuthenticated) {
        showNotification('확장자를 수정하려면 로그인이 필요합니다.', 'warning');
        setIsLoginDialogOpen(true);
        return;
    }
    // 수정 로직...
}
```

비로그인 사용자가 수정 시도 시:
1. 경고 메시지 표시
2. 로그인 다이얼로그 팝업
3. 로그인 페이지로 이동 제안

#### 세션 vs JWT

**고민 과정**
OAuth2 인증 후 인증 상태를 유지하는 방식으로 세션과 JWT를 고려했습니다.

**세션**
- Spring Security 기본 제공
- 서버 메모리 사용
- 구현 간단

**JWT**
- Stateless
- 확장성 우수
- 구현 복잡

**선택한 방법**
세션 기반 인증을 선택했습니다.

**선택 이유**
- 단일 서버 환경이므로 확장성 이슈 없음
- Spring Security OAuth2의 기본 방식
- 구현 시간 단축
- 로그아웃 처리 간단

#### Extension 엔티티 설계

**고민 과정**
Extension 엔티티에 어떤 필드를 포함할지 고민했습니다.

```java
@Entity
public class Extension {
    private Long id;
    private String extension;
    private boolean isFixed;
    private boolean isBlocked;
    
    // 추가 고민 대상
    private User createdBy;      // 필요한가?
    private User updatedBy;      // 필요한가?
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**선택한 방법**
생성자, 수정자, 생성일시, 수정일시를 모두 포함했습니다.

**선택 이유**
- 감사 로그(Audit Log) 확보
- 누가 언제 무엇을 변경했는지 추적 가능
- 실제 서비스에서 필수적인 기능
- OAuth2로 사용자 정보를 쉽게 가져올 수 있음
- 향후 변경 이력 조회 기능 추가 가능

### 데이터베이스 구현

#### UNIQUE 제약조건 위치

**고민 과정**
확장자 중복을 어디서 검사할지 고민했습니다.

**애플리케이션 레벨만**
```java
if (extensionRepository.existsByExtension(ext)) {
    throw new IllegalArgumentException("중복");
}
```

**DB UNIQUE 제약조건만**
```sql
CREATE TABLE extensions (
    extension VARCHAR(20) UNIQUE
);
```

**둘 다 적용**

**선택한 방법**
둘 다 적용했습니다.

**선택 이유**
- 애플리케이션 레벨: 사용자 친화적 에러 메시지 제공
- DB 레벨: 동시성 문제 완벽 방어 (Race Condition)
- 이중 안전장치

#### 인덱스 전략

**고민 과정**
어떤 컬럼에 인덱스를 생성할지 고민했습니다.

**후보 컬럼**
- extension: 검색 빈번
- is_blocked: 차단 목록 조회
- is_fixed: 고정/커스텀 분리 조회

**선택한 방법**
세 컬럼 모두 인덱스를 생성했습니다.

```sql
CREATE INDEX idx_extension ON extensions(extension);
CREATE INDEX idx_is_blocked ON extensions(is_blocked);
CREATE INDEX idx_is_fixed ON extensions(is_fixed);
```

**선택 이유**
- extension: 중복 체크 및 파일 검증 시 빈번한 조회
- is_blocked: 파일 업로드 시 차단 목록 전체 조회
- is_fixed: API별로 고정/커스텀 분리 조회
- 데이터 규모가 크지 않아 인덱스 유지 비용 낮음

#### 소프트 삭제 vs 하드 삭제

**고민 과정**
커스텀 확장자 삭제 시 실제로 DB에서 제거할지, 플래그만 변경할지 고민했습니다.

**소프트 삭제 (Soft Delete)**
```java
private boolean deleted;
```
- 복구 가능
- 이력 보존
- 쿼리 복잡도 증가

**하드 삭제 (Hard Delete)**
```java
extensionRepository.deleteById(id);
```
- 완전 제거
- 단순 구현

**선택한 방법**
하드 삭제를 선택했습니다.

**선택 이유**
- 과제 요구사항에 복구 기능 없음
- 삭제 이력은 created_by, updated_by로 충분
- 구현 단순화
- 필요 시 별도 history 테이블 추가 가능

---
## 6. 화면

### 메인 화면
<img width="2080" height="1182" alt="image" src="https://github.com/user-attachments/assets/5db5ccca-14a0-4a6d-997c-3ada23a5b922" />


### 로그인 전 화면
<img width="2078" height="1171" alt="image" src="https://github.com/user-attachments/assets/ab0bd230-9af4-49fc-b315-efcc1a51152c" />


### 로그인 후 화면
![222](https://github.com/user-attachments/assets/f2a3174b-9e5d-4b63-b7f5-5a018e848cbd)


### 파일 검증
<img width="1863" height="1205" alt="image" src="https://github.com/user-attachments/assets/e9861faa-aa48-4848-8c41-b78fab2bac16" />


---



## 7. 실행 방법

### 로컬 환경 실행

#### 1. 환경 변수 설정

`.env` 파일 생성:
```bash
DB_USERNAME=root
DB_PASSWORD=your_password
DB_HOST=localhost
DB_PORT=3306
DB_NAME=fileextension

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

FRONTEND_URL=http://localhost:3000
BACKEND_URL=http://localhost:8080
```

#### 2. MySQL 실행

```bash
docker run -d -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -e MYSQL_DATABASE=fileextension \
  mysql:8.0
```

#### 3. Backend 실행

```bash
./gradlew bootRun
```

접속: http://localhost:8080

#### 4. Frontend 실행

```bash
cd frontend
npm install
npm start
```

접속: http://localhost:3000

---

## 연락처

GitHub: https://github.com/0525aa36
Email: 0525aa36@gmail.com

