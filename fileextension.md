당신은 시니어 백엔드 개발자입니다. 아래 요구사항에 따라 파일 확장자 차단 시스템을 단계별로 구현해주세요.

## 프로젝트 정보
- 기술 스택: Spring Boot 3.x + React 18 + MySQL 8.0 + Google OAuth 2.0
- 빌드 도구: Gradle
- Java 버전: 17
- 아키텍처: 도메인 주도 설계(DDD)

## 핵심 요구사항
1. 고정 확장자 관리
   - 기본 제공: bat, cmd, com, cpl, exe, scr, js, sh
   - 초기 상태: 모두 uncheck (isBlocked = false)
   - 체크/언체크 시 DB 저장
   - 새로고침 시 상태 유지

2. 커스텀 확장자 관리
   - 최대 입력 길이: 20자
   - 최대 개수: 200개
   - 추가 시 DB 저장 및 화면 표시
   - X 버튼으로 삭제

3. 추가 고려사항
   - 중복 체크: 커스텀끼리, 고정과 커스텀 간
   - 대소문자 구분 없이 처리 (sh = SH = Sh)
   - 입력 정규화: 점(.) 제거, 공백 trim
   - 영문, 숫자만 허용

4. Google OAuth 로그인
   - 로그인한 사용자만 접근 가능
   - 관리자 권한 구분 없음 (모든 로그인 사용자 동일 권한)

## 개발 원칙
1. **단일 책임 원칙**: 각 함수는 하나의 기능만 수행
2. **작은 단위 커밋**: 기능별로 커밋 분리
3. **명확한 커밋 메시지**: "feat:", "refactor:", "test:" 등 컨벤션 사용
4. **에러 처리**: 모든 예외 상황 처리

## 패키지 구조
src/main/java/com/flow/fileextension/
├── domain/
│   ├── fixed/
│   │   ├── controller/    # FixedExtensionController
│   │   ├── dto/           # Request/Response DTO
│   │   ├── entity/        # FixedExtension
│   │   ├── repository/    # FixedExtensionRepository
│   │   └── service/       # FixedExtensionService
│   ├── custom/
│   │   └── (동일 구조)
│   └── user/
│       └── (동일 구조)
└── global/
├── config/            # SecurityConfig, WebConfig
├── response/          # ApiResponse<T>
└── security/          # OAuth 관련 클래스

## 데이터베이스 스키마
```sql
-- 고정 확장자
CREATE TABLE fixed_extensions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    extension VARCHAR(20) NOT NULL UNIQUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE
);

-- 커스텀 확장자
CREATE TABLE custom_extensions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    extension VARCHAR(20) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL
);

-- 사용자
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    profile_image VARCHAR(500),
    created_at DATETIME NOT NULL,
    last_login_at DATETIME
);
```

## API 명세

### 고정 확장자
- GET /api/extensions/fixed - 전체 조회
- PATCH /api/extensions/fixed/{id}?isBlocked=true - 상태 업데이트

### 커스텀 확장자
- GET /api/extensions/custom - 전체 조회
- POST /api/extensions/custom?extension=pdf - 추가
- DELETE /api/extensions/custom/{id} - 삭제

### 공통 응답 형식
```json
{
  "success": true,
  "message": "성공 메시지",
  "data": {}
}
```

## 단계별 구현 가이드

### Step 1: Entity 구현
먼저 다음 Entity를 구현해주세요:

1. FixedExtension
   - 필드: id, extension, isBlocked
   - 메서드: updateBlockStatus(Boolean isBlocked) - 단일 책임

2. CustomExtension
   - 필드: id, extension, createdAt
   - @PrePersist로 createdAt 자동 설정

3. User
   - 필드: id, email, name, profileImage, createdAt, lastLoginAt
   - 메서드: updateLastLogin() - 단일 책임

각 Entity 구현 후 커밋해주세요.

### Step 2: Repository 구현
다음 Repository 인터페이스를 구현해주세요:

1. FixedExtensionRepository
```java
Optional<FixedExtension> findByExtension(String extension);
boolean existsByExtension(String extension);
```

2. CustomExtensionRepository
```java
boolean existsByExtension(String extension);
long count();
```

3. UserRepository
```java
Optional<User> findByEmail(String email);
```

커밋 메시지: "feat: Repository 인터페이스 구현"

### Step 3: DTO 구현
다음 DTO를 구현해주세요:

1. ApiResponse<T> (global/response)
```java
public static <T> ApiResponse<T> success(T data)
public static <T> ApiResponse<T> error(String message)
```

2. FixedExtensionResponseDto
```java
public static FixedExtensionResponseDto from(FixedExtension entity)
```

3. CustomExtensionRequestDto
```java
@NotBlank(message = "확장자는 필수입니다")
@Size(max = 20, message = "확장자는 최대 20자까지 가능합니다")
@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "확장자는 영문과 숫자만 가능합니다")
private String extension;
```

4. CustomExtensionResponseDto
```java
public static CustomExtensionResponseDto from(CustomExtension entity)
```

### Step 4: Service 구현 (중요!)
다음 Service를 **함수별로 분리하여** 구현해주세요:

#### FixedExtensionService

1. 초기화 메서드
```java
@PostConstruct
public void initializeDefaultExtensions() {
    List<String> defaults = Arrays.asList("bat", "cmd", "com", "cpl", "exe", "scr", "js", "sh");
    // 각 확장자가 없으면 생성 (isBlocked = false)
}
```
커밋: "feat: 고정 확장자 초기 데이터 생성"

2. 조회 메서드
```java
public List<FixedExtensionResponseDto> getAllFixedExtensions() {
    // 전체 조회 후 DTO 변환
}
```
커밋: "feat: 고정 확장자 전체 조회 기능"

3. 업데이트 메서드 (함수 분리!)
```java
public FixedExtensionResponseDto updateBlockStatus(Long id, Boolean isBlocked) {
    FixedExtension extension = findFixedExtensionById(id);
    extension.updateBlockStatus(isBlocked);
    return FixedExtensionResponseDto.from(repository.save(extension));
}

private FixedExtension findFixedExtensionById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("확장자를 찾을 수 없습니다: " + id));
}
```
커밋: "feat: 고정 확장자 상태 업데이트 기능"

#### CustomExtensionService

1. 조회 메서드
```java
public List<CustomExtensionResponseDto> getAllCustomExtensions() {
    // 전체 조회 후 DTO 변환
}
```
커밋: "feat: 커스텀 확장자 전체 조회 기능"

2. 추가 메서드 (함수 분리 필수!)
```java
public CustomExtensionResponseDto addCustomExtension(String extension) {
    String normalized = normalizeExtension(extension);
    validateMaxCount();
    validateDuplicateInCustom(normalized);
    validateDuplicateInFixed(normalized);
    
    CustomExtension saved = repository.save(
        CustomExtension.builder()
            .extension(normalized)
            .build()
    );
    return CustomExtensionResponseDto.from(saved);
}

// 각 검증 로직을 별도 함수로!
private String normalizeExtension(String extension) {
    return extension.toLowerCase().replace(".", "").trim();
}

private void validateMaxCount() {
    if (repository.count() >= 200) {
        throw new IllegalStateException("커스텀 확장자는 최대 200개까지만 추가 가능합니다");
    }
}

private void validateDuplicateInCustom(String extension) {
    if (customRepository.existsByExtension(extension)) {
        throw new IllegalArgumentException("이미 존재하는 확장자입니다: " + extension);
    }
}

private void validateDuplicateInFixed(String extension) {
    if (fixedRepository.existsByExtension(extension)) {
        throw new IllegalArgumentException("고정 확장자에 이미 존재합니다: " + extension);
    }
}
```
커밋: "feat: 커스텀 확장자 추가 및 검증 로직 구현"

3. 삭제 메서드
```java
public void deleteCustomExtension(Long id) {
    validateExtensionExists(id);
    repository.deleteById(id);
}

private void validateExtensionExists(Long id) {
    if (!repository.existsById(id)) {
        throw new IllegalArgumentException("확장자를 찾을 수 없습니다: " + id);
    }
}
```
커밋: "feat: 커스텀 확장자 삭제 기능 구현"

### Step 5: Controller 구현
각 엔드포인트별로 메서드를 구현하고 커밋해주세요:

#### FixedExtensionController
```java
@RestController
@RequestMapping("/api/extensions/fixed")
@RequiredArgsConstructor
public class FixedExtensionController {
    
    private final FixedExtensionService service;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<FixedExtensionResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllFixedExtensions()));
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FixedExtensionResponseDto>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isBlocked) {
        try {
            return ResponseEntity.ok(
                ApiResponse.success(service.updateBlockStatus(id, isBlocked))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
```

#### CustomExtensionController
유사하게 GET, POST, DELETE 구현

커밋: "feat: 확장자 관리 REST API 구현"

### Step 6: OAuth 구현

1. SecurityConfig
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/error", "/actuator/health").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .defaultSuccessUrl("/", true)
            );
        return http.build();
    }
}
```
커밋: "feat: Spring Security OAuth2 설정"

2. CustomOAuth2UserService
```java
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        
        User user = findOrCreateUser(email, name, picture);
        
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
    
    private User findOrCreateUser(String email, String name, String picture) {
        return userRepository.findByEmail(email)
            .map(this::updateLastLogin)
            .orElseGet(() -> createNewUser(email, name, picture));
    }
    
    private User updateLastLogin(User user) {
        user.updateLastLogin();
        return userRepository.save(user);
    }
    
    private User createNewUser(String email, String name, String picture) {
        return userRepository.save(User.builder()
            .email(email)
            .name(name)
            .profileImage(picture)
            .build());
    }
}
```
커밋: "feat: OAuth 사용자 정보 처리 서비스 구현"

3. CustomOAuth2User
```java
public class CustomOAuth2User implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
    }
    
    // getName(), getAttributes() 구현
}
```
커밋: "feat: CustomOAuth2User 구현"

### Step 7: 전역 예외 처리
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        log.error("IllegalStateException: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse("입력값이 올바르지 않습니다");
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(message));
    }
}
```
커밋: "feat: 전역 예외 처리 핸들러 구현"

### Step 8: CORS 설정
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PATCH", "DELETE")
            .allowCredentials(true);
    }
}
```
커밋: "feat: CORS 설정 추가"

## Frontend 구현 가이드 (React)

### Step 9: API 서비스
```javascript
// src/services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  withCredentials: true,
});

export const extensionApi = {
  getFixedExtensions: () => api.get('/extensions/fixed'),
  updateFixedExtension: (id, isBlocked) => 
    api.patch(`/extensions/fixed/${id}?isBlocked=${isBlocked}`),
  getCustomExtensions: () => api.get('/extensions/custom'),
  addCustomExtension: (extension) => 
    api.post(`/extensions/custom?extension=${extension}`),
  deleteCustomExtension: (id) => api.delete(`/extensions/custom/${id}`),
};
```
커밋: "feat: API 통신 서비스 구현"

### Step 10: 컴포넌트

#### FixedExtensions.js
```javascript
const FixedExtensions = () => {
  const [extensions, setExtensions] = useState([]);
  const [loading, setLoading] = useState(true);

  // 단일 책임: 데이터 가져오기
  const fetchExtensions = async () => {
    try {
      setLoading(true);
      const response = await extensionApi.getFixedExtensions();
      setExtensions(response.data.data);
    } catch (error) {
      console.error('조회 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  // 단일 책임: 상태 토글
  const handleToggle = async (id, currentStatus) => {
    try {
      await extensionApi.updateFixedExtension(id, !currentStatus);
      setExtensions(extensions.map(ext => 
        ext.id === id ? { ...ext, isBlocked: !currentStatus } : ext
      ));
    } catch (error) {
      alert('업데이트 실패: ' + error.message);
    }
  };

  useEffect(() => {
    fetchExtensions();
  }, []);

  if (loading) return <div>로딩중...</div>;

  return (
    <div>
      <h2>고정 확장자</h2>
      {extensions.map(ext => (
        <label key={ext.id}>
          <input
            type="checkbox"
            checked={ext.isBlocked}
            onChange={() => handleToggle(ext.id, ext.isBlocked)}
          />
          {ext.extension}
        </label>
      ))}
    </div>
  );
};
```
커밋: "feat: 고정 확장자 컴포넌트 구현"

#### ExtensionInput.js
```javascript
const ExtensionInput = ({ onAdd }) => {
  const [extension, setExtension] = useState('');
  const [error, setError] = useState('');

  // 단일 책임: 입력 검증
  const validateExtension = (value) => {
    if (!value.trim()) return '확장자를 입력해주세요';
    if (value.length > 20) return '최대 20자까지 입력 가능합니다';
    if (!/^[a-zA-Z0-9]+$/.test(value.replace('.', ''))) {
      return '영문과 숫자만 가능합니다';
    }
    return '';
  };

  // 단일 책임: 제출 처리
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const validationError = validateExtension(extension);
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      const response = await extensionApi.addCustomExtension(extension);
      setExtension('');
      setError('');
      onAdd(response.data.data);
      alert('추가되었습니다');
    } catch (error) {
      setError(error.response?.data?.message || '추가 실패');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        value={extension}
        onChange={(e) => setExtension(e.target.value)}
        placeholder="확장자 입력 (예: pdf)"
        maxLength={20}
      />
      <button type="submit">추가</button>
      {error && <div className="error">{error}</div>}
    </form>
  );
};
```
커밋: "feat: 커스텀 확장자 입력 컴포넌트 구현"

#### CustomExtensions.js
```javascript
const CustomExtensions = ({ refreshTrigger }) => {
  const [extensions, setExtensions] = useState([]);

  const fetchExtensions = async () => {
    const response = await extensionApi.getCustomExtensions();
    setExtensions(response.data.data);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('삭제하시겠습니까?')) return;
    
    try {
      await extensionApi.deleteCustomExtension(id);
      setExtensions(extensions.filter(ext => ext.id !== id));
      alert('삭제되었습니다');
    } catch (error) {
      alert('삭제 실패');
    }
  };

  useEffect(() => {
    fetchExtensions();
  }, [refreshTrigger]);

  return (
    <div>
      <h2>커스텀 확장자 ({extensions.length} / 200)</h2>
      {extensions.map(ext => (
        <span key={ext.id}>
          {ext.extension}
          <button onClick={() => handleDelete(ext.id)}>×</button>
        </span>
      ))}
    </div>
  );
};
```
커밋: "feat: 커스텀 확장자 목록 컴포넌트 구현"

## 추가 요청사항

### 코드 작성 시 지켜야 할 규칙
1. 모든 함수는 10줄 이내로 작성 (복잡하면 분리)
2. 함수명은 동사로 시작 (get, find, validate, create, update, delete)
3. 매직 넘버/문자열은 상수로 선언
4. 주석은 "왜"를 설명 (코드가 "무엇"을 설명)
5. try-catch로 모든 에러 처리

### 커밋 메시지 컨벤션
- feat: 새로운 기능
- fix: 버그 수정
- refactor: 리팩토링
- test: 테스트 추가
- docs: 문서 수정
- chore: 빌드/설정 변경

### 테스트 작성 (선택사항)
```java
@Test
void 커스텀_확장자_중복_추가_시_예외발생() {
    // given
    customExtensionRepository.save(CustomExtension.builder()
        .extension("pdf")
        .build());
    
    // when & then
    assertThrows(IllegalArgumentException.class, 
        () -> service.addCustomExtension("pdf"));
}

@Test
void 대소문자_구분없이_중복_체크() {
    // given
    customExtensionRepository.save(CustomExtension.builder()
        .extension("pdf")
        .build());
    
    // when & then
    assertThrows(IllegalArgumentException.class, 
        () -> service.addCustomExtension("PDF"));
}
```

## 최종 체크리스트
- [ ] 고정 확장자 초기화 (8개, isBlocked=false)
- [ ] 체크/언체크 DB 저장
- [ ] 새로고침 상태 유지
- [ ] 커스텀 추가 (20자 제한)
- [ ] 커스텀 200개 제한
- [ ] 중복 체크 (커스텀끼리, 고정과)
- [ ] 대소문자 무시
- [ ] X 버튼 삭제
- [ ] OAuth 로그인
- [ ] 모든 함수 단일 책임
- [ ] 작은 단위 커밋

이제 Step 1부터 시작해주세요. 각 단계를 완료할 때마다 코드와 함께 커밋 메시지를 알려주세요.