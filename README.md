# WikiPedia 검색 앱(멀티모듈 및 클린아키텍처 공부용)

Wikipedia 검색 및 미디어 조회 Android 앱

## 프로젝트 개요

Wikipedia API를 활용하여 검색과 미디어 콘텐츠 조회 기능을 제공하는 Android 애플리케이션입니다. 
Clean Architecture와 모듈화 설계를 통해 확장성과 유지보수성을 고려하여 개발되었습니다.

## 주요 기능

- **검색 기능**: Wikipedia 페이지 검색 및 요약 정보 제공
- **미디어 조회**: 검색 결과와 연관된 이미지 및 미디어 콘텐츠 표시
- **상세 페이지**: WebView를 통한 Wikipedia 페이지 직접 조회
- **이미지 캐싱**: 메모리 기반 이미지 캐싱으로 성능 최적화
- **Pull-to-Refresh**: 콘텐츠 새로고침 지원

## 기술 스택

### 개발 환경
- **언어**: Kotlin
- **최소 SDK**: 23 (Android 6.0)
- **타겟 SDK**: 36
- **빌드 도구**: Gradle with Kotlin DSL

### 핵심 라이브러리
- **UI**: Jetpack Compose
- **네비게이션**: Navigation Compose
- **상태 관리**: ViewModel + StateFlow
- **의존성 주입**: Manual DI
- **네트워킹**: 커스텀 HttpURLConnection 기반 HTTP 클라이언트

## 프로젝트 구조

```
├── app/                          # 메인 애플리케이션 모듈
│   └── src/main/java/com/grensil/nhn_gmail/
│       ├── MainActivity.kt       # 진입점 Activity
│       ├── MainNavGraph.kt       # 네비게이션 그래프
│       └── NhnApplication.kt     # Application 클래스
│
├── core/                         # 핵심 모듈들
│   ├── data/                     # 데이터 레이어
│   │   └── src/main/java/com/grensil/data/
│   │       ├── datasource/       # 원격 데이터 소스
│   │       ├── entity/           # 데이터 엔티티
│   │       ├── mapper/           # 데이터 매퍼
│   │       └── repository/       # Repository 구현
│   │
│   ├── domain/                   # 도메인 레이어
│   │   └── src/main/java/com/grensil/domain/
│   │       ├── dto/              # 도메인 모델
│   │       ├── repository/       # Repository 인터페이스
│   │       └── usecase/          # 비즈니스 로직
│   │
│   ├── navigation/               # 네비게이션 설정
│   │   └── src/main/java/com/grensil/navigation/
│   │       └── Routes.kt         # 라우트 정의
│   │
│   ├── network/                  # 네트워크 모듈
│   │   └── src/main/java/com/grensil/network/
│   │       ├── HttpClient.kt     # HTTP 클라이언트
│   │       ├── HttpMethod.kt     # HTTP 메서드 정의
│   │       ├── HttpRequest.kt    # 요청 모델
│   │       ├── HttpResponse.kt   # 응답 모델
│   │       └── NhnNetworkException.kt # 네트워크 예외
│   │
│   └── ui/                       # 공통 UI 컴포넌트
│       └── src/main/java/com/grensil/ui/
│           ├── component/        # 재사용 가능한 컴포넌트
│           └── image/            # 이미지 처리
│
└── feature/                      # 기능 모듈들
    ├── detail/                   # 상세 페이지 기능
    │   └── src/main/java/com/grensil/detail/
    │       ├── DetailScreen.kt   # 상세 화면
    │       └── DetailViewModel.kt # 상세 화면 ViewModel
    │
    └── search/                   # 검색 기능
        └── src/main/java/com/grensil/search/
            ├── SearchScreen.kt   # 검색 화면
            ├── SearchViewModel.kt # 검색 ViewModel
            └── component/        # 검색 관련 컴포넌트
```

## 아키텍처

### Clean Architecture

프로젝트는 Clean Architecture 원칙을 따라 다음과 같이 레이어를 분리합니다:

1. **Presentation Layer** (`feature/`)
   - UI 컴포넌트 (Compose)
   - ViewModel (상태 관리)
   - Screen 구성

2. **Domain Layer** (`core/domain/`)
   - Use Cases (비즈니스 로직)
   - Domain Models (DTO)
   - Repository Interfaces

3. **Data Layer** (`core/data/`)
   - Repository 구현체
   - Data Sources (Remote)
   - Data Mappers
   - Network 통신

### 모듈화 설계

- **기능별 모듈 분리**: 각 기능을 독립적인 모듈로 구성
- **핵심 모듈 공유**: 공통 기능을 core 모듈로 분리
- **의존성 역전**: Domain이 Data를 의존하지 않도록 설계

## 네트워크 아키텍처

### 커스텀 HTTP 클라이언트

HttpURLConnection/HttpsURLConnection 기반의 커스텀 HTTP 클라이언트를 구현하여 다음 기능을 제공합니다:

- **HTTP 메서드 지원**: GET, POST, PUT, DELETE
- **요청/응답 헤더 처리**: 커스텀 헤더 설정 및 응답 헤더 파싱
- **타임아웃 설정**: 연결 및 읽기 타임아웃 설정
- **예외 처리**: 네트워크 에러에 대한 상세한 예외 분류
- **응답 형식**: String, ByteArray, JSON 등 다양한 형식 지원

### API 통합

Wikipedia REST API를 활용하여:
- 페이지 검색 및 요약 정보 조회
- 미디어 파일 목록 조회
- 썸네일 이미지 다운로드

## 빌드 및 실행

### 사전 요구사항

- Android Studio Arctic Fox 이상
- JDK 11 이상
- Android SDK 23 이상

### 빌드 방법

```bash
# 프로젝트 클론
git clone https://github.com/grensil/nhn_gmail.git

# 프로젝트 디렉토리로 이동
cd nhn_gmail

# Gradle 빌드
./gradlew build

# APK 생성
./gradlew assembleDebug
```

### 실행

1. Android Studio에서 프로젝트 열기
2. 디바이스/에뮬레이터 연결
3. Run 버튼 클릭 또는 `Shift + F10`

## 테스트

프로젝트는 단위 테스트와 통합 테스트를 포함합니다:

```bash
# 단위 테스트 실행
./gradlew test

# Android 통합 테스트 실행
./gradlew connectedAndroidTest
```

### 테스트 구성

- **Unit Tests**: Domain layer 로직 테스트
- **Integration Tests**: Data layer 및 네트워크 통신 테스트
- **UI Tests**: Compose UI 컴포넌트 테스트

## 성능 최적화

- **이미지 캐싱**: 메모리 기반 LRU 캐시로 이미지 로딩 성능 향상
- **LazyColumn**: 대용량 리스트에 대한 효율적인 렌더링
- **StateFlow**: 효율적인 상태 관리 및 UI 업데이트
- **Compose**: 선언형 UI로 리컴포지션 최적화

## 보안 고려사항

- **HTTPS 통신**: 모든 API 호출은 HTTPS로 암호화
- **입력 검증**: 사용자 입력에 대한 유효성 검사
- **예외 처리**: 안전한 에러 핸들링
