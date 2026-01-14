# 파일 확장자 차단 시스템

플로우 채용 과제로 제작한 파일 확장자 차단 웹 애플리케이션입니다.

## 📋 과제 요구사항

### 기본 요구사항
- ✅ 고정 확장자 8개 (bat, cmd, com, cpl, exe, scr, js, sh)
- ✅ 고정 확장자 체크/언체크 시 DB 저장 및 새로고침 유지
- ✅ 확장자 최대 20자 제한
- ✅ 커스텀 확장자 최대 200개 제한
- ✅ 커스텀 확장자 추가/삭제 기능
- ✅ 중복 확장자 검증

## 🎯 추가 구현 기능

### 1. 보안 강화
- **이중 확장자 검증**: `file.exe.txt` 같은 우회 시도 방어
- **대소문자 정규화**: `Exe`, `EXE`, `exe` 모두 동일하게 처리
- **입력 검증 강화**: 특수문자, 공백 등 차단

### 2. 아키텍처 개선
- **도메인 통합**: `custom_extensions`, `fixed_extensions` 테이블을 `extensions` 하나로 통합
  - `is_fixed` 필드로 고정/커스텀 구분
  - 코드 중복 제거 및 유지보수성 향상
- **사용자 추적**: 생성자/수정자 정보 저장 (OAuth2 연동)
- **감사 로그**: 생성/수정 시간 자동 기록

### 3. 사용자 경험
- **OAuth2 소셜 로그인**: Google 계정으로 간편 로그인
- **한글 에러 메시지**: 사용자 친화적인 에러 처리
- **실시간 피드백**: 입력 검증 및 에러 메시지 즉시 표시

### 4. 코드 품질
- **유틸리티 클래스**: 재사용 가능한 검증 로직 분리
- **상수 관리**: ErrorMessages 클래스로 메시지 중앙 관리
- **로깅**: SLF4J를 통한 체계적인 로그 관리
- **작은 단위 커밋**: 기능별 명확한 커밋 메시지

## 🛠 기술 스택

### Backend
- Java 17
- Spring Boot 3.2.4
- Spring Data JPA
- Spring Security + OAuth2
- MySQL 8.0
- Gradle

### Frontend
- React 18
- Axios
- CSS3

## 📁 프로젝트 구조

```
src/main/java/com/flow/fileextension/
├── domain/
│   ├── extension/          # 통합 확장자 도메인
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   └── user/              # 사용자 도메인
├── global/
│   ├── config/            # 설정 (Security, CORS 등)
│   ├── constants/         # 상수 (ErrorMessages)
│   ├── exception/         # 전역 예외 처리
│   ├── response/          # API 응답 포맷
│   ├── security/          # OAuth2 설정
│   └── util/              # 유틸리티 (ExtensionValidator)
└── service/               # 파일 검증 서비스
```

## 🚀 실행 방법

### 환경 변수 설정
```bash
# MySQL 설정
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Google OAuth2 설정
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret
```

### 백엔드 실행
```bash
./gradlew bootRun
```

### 프론트엔드 실행
```bash
cd frontend
npm install
npm start
```

### 접속
- Frontend: http://localhost:3000
- Backend: http://localhost:8080

## 💡 주요 설계 결정

### 1. 테이블 통합
**이전**: `fixed_extensions`, `custom_extensions` 2개 테이블  
**현재**: `extensions` 1개 테이블 + `is_fixed` 플래그

**장점**:
- 코드 중복 제거 (Controller, Service, Repository 통합)
- 확장자 조회 쿼리 단순화
- 유지보수성 향상

### 2. 이중 확장자 검증
`file.exe.txt` 같은 파일 업로드 시:
- `.txt`만 검사 ❌
- `.exe`, `.txt` 모두 검사 ✅

**이유**: 악의적 사용자가 차단된 확장자를 숨기는 시도 방어

### 3. 사용자 추적
모든 확장자 추가/수정 시 누가 언제 작업했는지 기록

**활용**:
- 감사 로그
- 문제 발생 시 추적
- 사용자별 통계

## 🔒 보안 고려사항

1. **입력 검증**
   - 길이 제한 (20자)
   - 형식 검증 (영문, 숫자만)
   - SQL Injection 방지 (Prepared Statement 사용)

2. **이중 확장자 방어**
   - 모든 확장자 검증
   - 정규화 처리

3. **인증/인가**
   - OAuth2 소셜 로그인
   - 세션 기반 인증

## 📊 데이터베이스 스키마

```sql
CREATE TABLE extensions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    extension VARCHAR(20) NOT NULL UNIQUE,
    is_fixed BOOLEAN NOT NULL,
    is_blocked BOOLEAN NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);
```

## 🎨 API 엔드포인트

### 고정 확장자
- `GET /api/extensions/fixed` - 목록 조회
- `PATCH /api/extensions/fixed/{id}/block` - 차단 상태 변경

### 커스텀 확장자
- `GET /api/extensions/custom` - 목록 조회
- `POST /api/extensions/custom` - 추가
- `DELETE /api/extensions/custom/{id}` - 삭제

### 파일 검증
- `POST /api/files/check` - 파일 확장자 검증

### 인증
- `GET /api/user/me` - 현재 사용자 정보

## 📝 테스트 시나리오

1. **기본 기능**
   - [ ] 고정 확장자 체크/언체크
   - [ ] 커스텀 확장자 추가/삭제
   - [ ] 새로고침 시 상태 유지

2. **검증 기능**
   - [ ] 중복 확장자 추가 시 에러
   - [ ] 200개 초과 시 에러
   - [ ] 20자 초과 입력 시 에러
   - [ ] 특수문자 입력 시 에러

3. **보안 기능**
   - [ ] 차단된 확장자 업로드 시 거부
   - [ ] 이중 확장자 검증 (file.exe.txt)
   - [ ] 대소문자 구분 없이 차단

## 🐛 알려진 제한사항

1. **MIME Type 검증 미구현**
   - 현재는 파일명만 검증
   - 향후 파일 내용 기반 검증 추가 필요

2. **파일 크기 제한 없음**
   - 향후 추가 필요

3. **배치 작업 미구현**
   - 여러 확장자 동시 추가/삭제 기능

## 📈 향후 개선 계획

1. **보안 강화**
   - MIME Type 검증
   - Magic Number 검증
   - 파일 내용 스캔

2. **기능 추가**
   - 확장자 그룹 관리
   - 통계 대시보드
   - 검색 기능

3. **성능 최적화**
   - Redis 캐싱
   - 배치 처리

## 👨‍💻 개발자

**이름**: [본인 이름]  
**이메일**: [본인 이메일]  
**GitHub**: [본인 GitHub]

## 📄 라이선스

이 프로젝트는 플로우 채용 과제용으로 제작되었습니다.
