# 설스터디 (Study Coaching Platform) — Backend

멘토가 멘티에게 과제를 부여하고, 플래너를 검토하며 피드백을 제공하는 학습 코칭 플랫폼의 백엔드입니다.

- **Stack**: Spring Boot 3.5, Java 21, MySQL 8.4, JPA(Hibernate), Spring Security
- **Infra**: AWS S3(호환 스토리지), Docker Compose, Railway 배포

현재 백엔드 서버는 Railway에 배포되어 있으며, Swagger UI에서 API 명세를 확인할 수 있습니다.

| 항목 | URL |
|---|---|
| Production Server | https://back-end-production-4beb.up.railway.app |
| Swagger UI | https://back-end-production-4beb.up.railway.app/swagger-ui/index.html |

<img width="710" alt="Railway" src="https://github.com/user-attachments/assets/edf95ceb-2f07-45ac-9710-1e9b249e12ed" />

---

## 목차
- [아키텍처 개요](#아키텍처-개요)
- [기술적 의사결정 (Highlights)](#기술적-의사결정-highlights)
  - [1. 파일 저장소 추상화 — 전략 + 어댑터 패턴](#1-파일-저장소-추상화--전략--어댑터-패턴)
  - [2. 프로필 기반 환경 분리 (local / railway)](#2-프로필-기반-환경-분리-local--railway)
  - [3. JWT 인증 + Refresh Token 회전](#3-jwt-인증--refresh-token-회전)
  - [4. 메서드 단위 역할 기반 접근제어 (RBAC)](#4-메서드-단위-역할-기반-접근제어-rbac)
  - [5. 통일된 API 응답 & 중앙 예외 처리](#5-통일된-api-응답--중앙-예외-처리)
- [기타 설계 포인트](#기타-설계-포인트)
- [실행 방법](#실행-방법)

---

## 아키텍처 개요

도메인 주도 패키지 구조로, 각 도메인이 `controller / dto / entity / repository / service`를 자체적으로 보유합니다.

```
com.seolstudy.backend
├── domain
│   ├── auth        # JWT 인증 (회원가입/로그인/토큰 재발급/로그아웃)
│   ├── user        # 사용자 + 역할(MENTOR/MENTEE)
│   ├── mentoring   # 멘토-멘티 매칭 관리
│   ├── task        # 과제 부여(멘토) / 제출(멘티) + 완료 인증
│   ├── planner     # 일자별 플래너 + 멘토 코멘트
│   ├── feedback    # 일자별 멘토 피드백
│   └── subject     # 과목 (시작 시 시드)
└── global
    ├── config      # Security, S3, Swagger, Web, Cors
    ├── security    # JwtTokenProvider, JwtAuthenticationFilter, UserDetails
    ├── payload     # CommonResponse + Error/Success Status enum
    ├── exception   # GeneralException + GlobalExceptionHandler
    └── storage     # FileStorage 추상화 (S3 / Local)
```

---

## 기술적 의사결정 (Highlights)

### 1. 파일 저장소 추상화 — 전략 + 어댑터 패턴

업로드 로직을 `FileStorage` 인터페이스로 추상화하고, Spring `@Profile`로 구현체를 자동 선택합니다.
비즈니스 서비스는 구현체(S3/로컬)를 전혀 모른 채 인터페이스에만 의존합니다.

```
        [PlannerService / TaskService]   ← 인터페이스에만 의존
                     │
                     ▼
            ┌──────────────────┐
            │   FileStorage    │  (interface)
            └──────────────────┘
                  ▲        ▲
        @Profile("!local") │ @Profile("local")
        ┌─────────────┐  ┌──────────────────────┐
        │S3FileStorage│  │LocalFileStorageAdapter│──위임──▶ LocalFileStorage
        └─────────────┘  └──────────────────────┘           (실제 파일 I/O)
```

**핵심 설계**

| 항목 | 내용 | 근거 |
|---|---|---|
| 전략 패턴 + DIP | 프로필이 주입 빈을 결정 → 서비스 코드 변경 0줄로 저장소 교체 | `global/storage/FileStorage.java` |
| 어댑터 패턴 | 시그니처가 다른 기존 `LocalFileStorage`를 인터페이스에 맞춰 재사용 | `LocalFileStorageAdapter.java` |
| 2단계 저장/조회 | 업로드 시 **key만 DB 저장** → 조회 시점에 접근 URL 생성 | `createPresignedGetUrl()` |
| Presigned URL | 비공개 버킷 + **만료시간 있는 임시 URL**만 노출 | `S3FileStorage.java:129-147` |
| 로컬 외부의존성 제거 | local 프로필은 S3 빈 자체를 띄우지 않음 | `S3Config.java` `@Profile("!local")` |
| 안전한 부가작업 | 객체 삭제 실패 시 throw 대신 `log.warn` → 메인 트랜잭션 보호 | `S3FileStorage.java:124-126` |

> **설계 의도**: 외부 스토리지(S3)를 인터페이스 뒤로 숨겨, 로컬에서는 디스크·운영에서는 S3를 **동일한 코드로** 사용한다. S3는 버킷을 비공개로 두고 Presigned URL로만 접근을 허용해 노출 범위를 제한했다.

---

### 2. 프로필 기반 환경 분리 (local / railway)

"공통 + 프로필별 오버라이드" 3단 구성으로, 로컬은 진입장벽을 낮추고 운영은 설정 누락을 차단합니다.

```
application.yml          # 공통값 + 기본 프로필(default: local)
application-local.yml    # 로컬: DB 하드코딩 + JWT secret 기본값 제공
application-railway.yml  # 운영: DB/secret 전부 환경변수 주입
```

| 환경 | DB 설정 | JWT Secret |
|---|---|---|
| **local** | `localhost:3306` 하드코딩 (docker-compose와 짝) | 기본값 제공 → 무설정 즉시 실행 |
| **railway** | `${MYSQLHOST}` 등 환경변수 주입 | 환경변수 강제 → 미설정 시 부팅 실패 |

> **설계 의도**: 로컬은 기본값으로 클론 후 바로 실행 가능하게, 운영은 비밀키를 강제해 **설정 누락 시 부팅이 실패**하도록 분리했다. DB·JWT·S3 자격증명은 코드에 하드코딩하지 않고 환경변수로 분리해 주입한다 (운영: Railway 환경변수 / 로컬: 실행 구성의 환경변수).

---

### 3. JWT 인증 + Refresh Token 회전

Access(1시간) + Refresh(7일) 토큰 페어 구조이며, Refresh Token을 DB에 저장해 재발급 회전과 로그아웃 무효화를 구현했습니다.

- `JwtTokenProvider` — HMAC-SHA256(`Keys.hmacShaKeyFor`, 256bit), userId/username/role을 claim으로 발급·추출
- `RefreshToken` 엔티티 — User와 1:1, 재발급 시 `updateToken()`으로 **회전**, 로그아웃 시 DB에서 **삭제**해 재사용 차단
- `JwtAuthenticationFilter`(`OncePerRequestFilter`) — Authorization 헤더의 Bearer 토큰 검증 후 `SecurityContext` 설정
- `SessionCreationPolicy.STATELESS` — 완전 무상태
- `BCryptPasswordEncoder` — 비밀번호 단방향 해싱

---

### 4. 메서드 단위 역할 기반 접근제어 (RBAC)

`@EnableMethodSecurity`를 켜고 컨트롤러 메서드에 권한을 선언해, URL 패턴이 아닌 **비즈니스 메서드 단위**로 인가를 처리합니다.

```java
@PreAuthorize("hasRole('MENTOR')")             // 과제 부여, 피드백 작성
@PreAuthorize("hasRole('MENTEE')")             // 플래너/과제 제출
@PreAuthorize("hasAnyRole('MENTOR','MENTEE')") // 공통 조회
```

역할은 `UserRole` enum(`MENTOR`/`MENTEE`)으로 모델링하고 `EnumType.STRING`으로 저장합니다.

---

### 5. 통일된 API 응답 & 중앙 예외 처리

모든 응답을 `CommonResponse<T>` 한 가지 포맷으로 통일하고, 예외를 단일 핸들러에서 처리합니다.

```json
{ "isSuccess": true, "code": "COMMON_200", "message": "성공", "result": { } }
```

- `CommonResponse<T>` — 제네릭 래퍼, `@JsonInclude(NON_NULL)`로 에러 시 result 생략
- `ErrorStatus` enum — 도메인별 40여 개 에러 코드 (`AUTH_4011`, `TASK_4001` …), HTTP 상태/코드/메시지 일괄 정의
- `GlobalExceptionHandler`(`@RestControllerAdvice`) — 비즈니스 예외 / **검증 실패(필드별 상세)** / 파일 업로드 초과 / 인증·인가 예외 / 폴백을 한 곳에서 처리

> **가치**: 프론트엔드가 항상 같은 구조로 응답을 파싱할 수 있고, 에러 케이스가 코드로 표준화되어 협업·디버깅 효율이 높다.

---

## 기타 설계 포인트

| 포인트 | 근거 |
|---|---|
| **N+1 방지** | 월별 과제 조회에서 `JOIN FETCH t.subject` (`TaskRepository`) |
| **트랜잭션 전략** | 기본 `@Transactional(readOnly = true)`, 쓰기 메서드만 재정의 |
| **지연 로딩** | 모든 연관관계 `FetchType.LAZY` |
| **생성자 주입 일관** | `@RequiredArgsConstructor` + `final` 필드 전용 (필드 주입 미사용) |
| **DTO 매핑 컨벤션** | 정적 팩토리 `from()` / `of()`로 매핑 로직 중앙화 |
| **입력 검증** | Jakarta Validation(`@NotBlank`, `@Size`) + 비밀번호 확인 커스텀 검증 |
| **초기 데이터 시딩** | `SubjectInitializer`(`ApplicationRunner`)가 중복 체크 후 과목 시드 |
| **요청 로깅** | `RequestLoggingFilter`로 method/URI/status/소요시간(ms) 기록 |
| **테스트** | Mockito BDD(`given/when/then`), 한글 `@DisplayName`, H2 인메모리 |
| **API 문서화** | Swagger(OpenAPI 3.0) + JWT Bearer SecurityScheme |

---

## 실행 방법

```bash
# 1) 로컬 MySQL 기동 (port 3306, db=app / user=app / pw=app1234)
docker compose up -d

# 2) 애플리케이션 실행 (기본 프로필: local)
./gradlew bootRun

# 3) 테스트
./gradlew test
```
