# kingHeart Shopping Mall

KH 세미프로젝트로 진행한 쇼핑몰 웹 애플리케이션입니다.

## 📑 프로젝트 개요

Spring Boot를 기반으로 구축된 온라인 쇼핑몰 플랫폼입니다. 사용자는 상품을 검색하고, 장바구니에 담아 주문할 수 있으며, 관리자는 상품, 회원, 주문 등을 관리할 수 있는 시스템을 갖추고 있습니다.

## 🛠️ 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.5.6**
- **Oracle Database** (ojdbc11)
- **Spring Boot Mail Sender** (이메일 인증 및 알림)
- **JSoup** (HTML 파싱 및 처리)
- **Lombok** (보일러플레이트 코드 감소)

### Frontend
- **JSP** (View 템플릿)
- **JavaScript** (클라이언트 사이드 로직)
- **CSS** (스타일링)
- **Summernote** (상품 상세 정보 편집용 WYSIWYG 에디터)

### Server
- **Apache Tomcat** (내장 서버)

### 아키텍처 패턴
- **MVC 패턴** (Model-View-Controller)
- **DAO 패턴** (Data Access Object)
- **Service 레이어** (비즈니스 로직 분리)
- **Interceptor** (인증/인가 처리)
- **AOP** (예외 처리 및 전역 설정)

## ✨ 주요 기능

### 🙍‍♂️ 사용자 기능
- **회원 관리:**
  - 회원가입 (아이디, 비밀번호, 닉네임, 이메일 등 유효성 검증)
  - 로그인/로그아웃
  - 아이디/비밀번호 찾기 (이메일 인증)
  - 회원 정보 수정
  - 비밀번호 변경
  - 회원 탈퇴
  - 프로필 이미지 관리
- **상품:**
  - 상품 목록 보기 (카테고리별, 페이지네이션)
  - 상품 상세 정보 보기
  - 카테고리별 상품 조회 (계층형 카테고리 지원)
  - 상품 검색
- **주문:**
  - 장바구니 추가/삭제/수정
  - 위시리스트 추가/삭제
  - 상품 주문 및 결제
  - 주문 내역 조회
  - 주문 상세 정보 확인
- **리뷰:**
  - 상품 리뷰 작성 (평점, 내용, 이미지)
  - 리뷰 수정/삭제 (작성자만 가능)
  - 리뷰 목록 조회
- **고객센터:**
  - 공지사항 및 문의 게시판
  - 게시글 작성/수정/삭제
  - 게시글 목록 조회

### 👨‍💼 관리자 기능
- **회원 관리:**
  - 전체 회원 목록 조회 및 검색
  - 회원 상세 정보 확인
  - 회원 등급 관리 (일반회원, 우수회원, 관리자)
- **상품 관리:**
  - 상품 등록 (썸네일, 상세 이미지, 옵션)
  - 상품 수정/삭제
  - 재고 관리
  - 상품 카테고리 연결
- **주문 관리:**
  - 전체 주문 내역 조회
  - 주문 상태 변경 (결제완료, 배송준비중, 배송중, 배송완료 등)
  - 주문 상세 정보 확인
- **카테고리 관리:**
  - 상품 카테고리 추가/수정/삭제
  - 계층형 카테고리 구조 지원
- **배너 관리:**
  - 메인 페이지 배너 등록 및 관리
  - 배너 이미지 업로드
- **통계:**
  - 매출 통계 확인
  - 주문 관련 통계
  - REST API를 통한 통계 데이터 제공
- **상품 옵션 관리:**
  - 사이즈, 색상 등 상품 옵션 관리
  - 옵션별 재고 관리
  - 옵션 조합 관리

### 🔐 보안 및 권한 관리
- **Interceptor 기반 인증/인가:**
  - `MemberLoginInterceptor`: 로그인 필수 페이지 접근 제어
  - `AdminInterceptor`: 관리자 권한 검증
  - `PreventAdminInterceptor`: 관리자 계정 보호
  - `ReviewAuthorInterceptor`: 리뷰 작성자만 수정/삭제 가능
  - `AdvancedMemberInterceptor`: 우수회원 전용 기능 제어
- **예외 처리:**
  - `GlobalControllerAdvice`: 전역 카테고리 트리 제공
  - `ExceptionControllerAdvice`: 전역 예외 처리
  - 커스텀 예외 클래스 (`NeedPermissionException`, `TargetNotfoundException`, `UnauthorizationException`)

### 📡 REST API
- 회원 관련 REST API (`MemberRestController`)
- 상품 관련 REST API (`ProductRestController`)
- 장바구니 REST API (`CartRestController`)
- 위시리스트 REST API (`WishlistRestController`)
- 리뷰 REST API (`ReviewRestController`)
- 고객센터 게시판 REST API (`CsBoardRestController`)
- 관리자 통계 REST API (`AdminStatRestController`)
- 관리자 상품 옵션 REST API (`AdminProductOptionRestController`)

## 🗄️ 데이터베이스 스키마

프로젝트에 사용된 테이블 구조는 `TABLE_DETAILS.md` 파일에서 확인할 수 있습니다.

주요 테이블:
- `member`: 회원 정보
- `product`: 상품 정보
- `product_option`: 상품 옵션 (사이즈, 색상 등)
- `category`: 카테고리 (계층형 구조)
- `product_category_map`: 상품-카테고리 매핑 (M:N)
- `orders`: 주문 정보
- `order_detail`: 주문 상세 정보
- `cart`: 장바구니
- `wishlist`: 위시리스트
- `review`: 리뷰
- `attachment`: 첨부파일 (이미지 등)
- `banner`: 배너 정보

## 📁 프로젝트 구조

```
src/main/java/com/kh/shoppingmall/
├── advice/              # 전역 컨트롤러 어드바이스
├── aop/                 # 인터셉터 및 예외 처리
├── configuration/       # 설정 클래스 (이메일 등)
├── controller/          # MVC 컨트롤러
├── restcontroller/      # REST API 컨트롤러
├── dao/                 # 데이터 접근 객체
├── mapper/              # MyBatis Mapper 인터페이스
├── service/             # 비즈니스 로직 서비스
├── dto/                 # 데이터 전송 객체
├── vo/                  # 값 객체
├── error/               # 커스텀 예외 클래스
└── interceptor/         # 인터셉터 클래스
```

## 🚀 실행 방법

### 사전 요구사항
- Java 21 이상
- Oracle Database
- Maven 3.6 이상

### 설정 및 실행

1. **데이터베이스 설정:**
   - Oracle 데이터베이스를 준비합니다.
   - `src/main/resources/application.properties` 파일에 자신의 데이터베이스 환경에 맞게 아래 정보를 수정합니다.
     ```properties
     spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```
   - 이메일 설정 (선택사항):
     ```properties
     spring.mail.host=smtp.gmail.com
     spring.mail.port=587
     spring.mail.username=your_email@gmail.com
     spring.mail.password=your_password
     spring.mail.properties.mail.smtp.auth=true
     spring.mail.properties.mail.smtp.starttls.enable=true
     ```

2. **데이터베이스 스키마 생성:**
   - `TABLE_DETAILS.md` 파일의 SQL 스크립트를 실행하여 테이블을 생성합니다.

3. **프로젝트 실행:**
   - IDE(Eclipse, IntelliJ 등)에서 프로젝트를 가져와 실행합니다.
   - 또는, 프로젝트 루트 디렉터리에서 아래 명령어를 실행합니다.
     ```shell
     # Windows
     mvnw.cmd spring-boot:run
     
     # Linux/Mac
     ./mvnw spring-boot:run
     ```

4. **접속:**
   - 웹 브라우저에서 `http://localhost:8080`으로 접속합니다.

## 📝 주요 특징

- **계층형 카테고리 구조**: 상품 카테고리를 계층적으로 관리하여 유연한 분류 체계 제공
- **상품 옵션 관리**: 사이즈, 색상 등 다양한 옵션 조합을 지원하는 유연한 옵션 시스템
- **권한 기반 접근 제어**: Interceptor를 활용한 세밀한 권한 관리
- **RESTful API**: 프론트엔드와 백엔드 분리를 위한 REST API 제공
- **이메일 인증**: 회원가입 및 비밀번호 찾기 시 이메일 인증 기능
- **파일 업로드**: 상품 이미지, 리뷰 이미지 등 멀티파트 파일 업로드 지원
- **예외 처리**: 전역 예외 처리 및 커스텀 예외를 통한 안정적인 에러 핸들링
