# 🚀 Fitview 프로젝트 개발 가이드라인

이 문서는 Fitview 프로젝트에 처음 참여하는 개발자를 위한 기술 스택, 구조, 컨벤션 및 개발 규칙을 정의합니다.

## 🛠 1. 기술 스택 (Tech Stack)

- **Language**: Kotlin 1.9.25 (Java 17)
- **Framework**: Spring Boot 3.5.6
- **Database**: 
  - Main: MySQL (Runtime)
  - Test: H2 (In-memory)
- **Persistence**: Spring Data JPA, QueryDSL 5.0.0
- **Security**: Spring Security, OAuth2 (Google, Apple 등), JWT
- **Communication**: WebSocket (STOMP), WebClient (WebFlux)
- **Infrastructure**: AWS (S3), Redis, Firebase (FCM)
- **API Docs**: Spring RestDocs (Asciidoctor)
- **Test**: JUnit 5, Mockito Kotlin, MockMvc

---

## 📂 2. 프로젝트 구조 (Project Structure)

프로젝트는 도메인 기반의 레이어드 아키텍처(Domain-Driven Layered Architecture)를 따릅니다.

### 패키지 구조
- `kr.co.fitview.api.app.domain`: 비즈니스 로직의 핵심인 도메인별 패키지
  - `controller`: API 엔드포인트 정의
  - `service`: 비즈니스 로직 (CUD 작업은 `Service`, Read 작업은 `QueryService`로 분리하는 경향이 있음)
  - `repository`: 데이터 액세스 계층 (JPA, QueryDSL)
  - `entity`: JPA 엔티티
  - `dto`: 요청/응답 데이터 객체
  - `condition`: 검색 조건 객체 (QueryDSL 사용 시)
- `kr.co.fitview.api.app.global`: 전역적으로 사용되는 공통 모듈
  - `config`, `exception`, `jwt`, `security`, `util` 등

---

## 💻 3. 코딩 컨벤션 (Coding Conventions)

### naming
- **Class/Interface**: `PascalCase`
- **Function/Variable**: `camelCase`
- **Package**: `lowercase` (언더바 지양)

### Service 계층 분리
- 복잡한 도메인의 경우 비즈니스 로직(Command)과 조회 로직(Query)을 분리하여 관리합니다.
  - 예: `MemberService` (회원 가입, 수정, 탈퇴), `MemberQueryService` (회원 조회, 목록 검색)

### 예외 처리 (Exception Handling)
- 모든 예외는 `GlobalExceptionHandler`에서 전역적으로 처리됩니다.
- 비즈니스 예외 발생 시 `GlobalException` 또는 이를 상속받은 커스텀 예외를 정의하여 사용하세요.

---

## 🧪 4. 테스트 및 API 문서화

### 테스트 작성
- **Controller Test**: `ControllerTestSupport` 클래스를 상속받아 작성합니다. `@WebMvcTest`를 기반으로 하며 필요한 서비스는 Mocking 처리합니다.
- **Integration Test**: `IntegrationTestSupport` 클래스를 상속받아 전체 컨텍스트를 로드하여 테스트합니다.

### API 문서 (RestDocs)
- 테스트 코드 실행 성공 시 `build/generated-snippets`에 문서 조각이 생성됩니다.
- 생성된 조각은 `src/docs/asciidoc` 내의 `.adoc` 파일에서 참조하여 최종 HTML 문서를 구성합니다.

---

## 🚀 5. 실행 및 빌드

### 로컬 실행
```bash
./gradlew bootRun
```

### 빌드 및 테스트
```bash
./gradlew build
```
- 빌드 성공 시 `src/main/resources/static/docs` 경로에 API 문서가 포함된 JAR 파일이 생성됩니다.

---

## 🤝 6. 협업 규칙

- **Git Branch Strategy**: (프로젝트 상황에 맞춰 수정 필요) 보통 `feature/{issue-number}-{description}` 형식을 사용합니다.
- **Commit Message**: `[feat]`, `[fix]`, `[docs]`, `[refactor]`, `[test]` 등의 prefix를 사용합니다.
- **Code Review**: PR 생성 후 동료의 리뷰를 거쳐 `develop` 브랜치에 머지합니다.

## 테스트 코드 작성 규칙
- 각 엔티티 생성 시, 각 테스트 함수 내에서 엔티티를 생성해야한다.
  - (즉, 별도의 private 함수를 쓰지 않아야 한다.)


## 명령 수용 규칙
- 명령 외, 명령과 관련없는 함수를 수정하면 안된다