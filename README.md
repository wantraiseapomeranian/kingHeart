# kingHeart Shopping Mall

Spring Boot를 기반으로 구축된 현대적인 온라인 쇼핑몰 플랫폼입니다. 사용자에게는 편리한 쇼핑 경험을, 관리자에게는 유연한 상품 및 주문 관리 환경을 제공합니다.

## 📑 프로젝트 개요

본 프로젝트는 자바 백엔드 기술력을 바탕으로 쇼핑몰의 핵심인 주문, 결제, 재고 관리 시스템을 구현하는 데 초점을 맞추었습니다. 특히 프로젝트 종료 후에도 실무 수준의 결제 환경을 구축하기 위해 **카카오페이 API**를 자기주도적으로 연동하여 시스템을 완성했습니다.

## 👨‍💻 본인 구현 및 기여 상세

### 1. 프로젝트 기간 내 구현
프로젝트 진행 중 담당하여 완료한 핵심 비즈니스 로직 및 UI/UX 기능입니다.

* **주문 및 결제 기초 로직 (Order & Payment Baseline)**
    * 주문서 작성 및 결제 데이터 프로세싱 로직 구축
    * 주문 정보(주소, 수령인, 결제 수단 등) 유효성 검증 및 DB 트랜잭션 처리
    * 결제 결과에 따른 주문 상태(결제완료/대기 등) 업데이트 로직 구현
* **장바구니 시스템 (Cart Management)**
    * 장바구니 상품 추가, 수량 변경 및 개별/선택 삭제 로직 구현
    * 비즈니스 로직 연동을 통한 실시간 총합계 금액 산출 및 재고 연동 기초 설계
* **카테고리 시스템 (Category System)**
    * Recursive(재귀) 구조를 활용한 계층형 카테고리 데이터 조회 및 트리 구조 시각화
    * 상위/하위 카테고리 관계를 고려한 상품 필터링 및 네비게이션 로직 설계
* **관리자 전용 기능 (Admin Features)**
    * **상품 옵션 및 SKU 관리**: N개의 옵션 항목을 동적으로 추가 가능한 UI 구현 및 **데카르트 곱(Cartesian Product)** 알고리즘을 통한 재고 관리 단위(SKU) 자동 생성 로직 구축
    * **카테고리 관리**: 관리자 전용 계층형 카테고리 추가, 수정, 삭제(CRUD) 기능 및 순서 변경 로직 구현

---

### 2. 프로젝트 종료 후 독자적 고도화
공식 프로젝트 종료 이후, 기술적 완성도와 실무 능력을 높이기 위해 **자기주도적으로 추가 구현**한 핵심 시스템입니다.

* **카카오페이(KakaoPay) 실결제 연동**
    * `Spring WebFlux`의 `WebClient`를 이용한 결제 준비(Ready), 승인(Approve) API 비동기 연동
    * 결제 성공 시 **트랜잭션(Transaction)** 기반의 주문 정보 생성 및 장바구니 일괄 비우기 프로세스 완성
    * 결제 승인 시점에 맞춘 상품 옵션별 재고 실시간 차감 로직 구현으로 데이터 정합성 확보
* **고도화된 부분 취소 시스템**
    * 카카오페이 부분 취소 API 연동을 통한 상세 항목별 개별 환불 로직 구현
    * 주문 상세 상태(`detail_status`)와 환불 가능 잔액(`orders_remain_price`)의 유기적 동기화 시스템 구축
    * 취소 완료 시 해당 상품의 옵션 재고를 자동으로 원복하는 **재고 자동 복구 시스템** 설계

---

### 💡 기술적 성과
* **복잡한 데이터 구조 처리**: 재귀 쿼리와 데카르트 곱 알고리즘을 실제 서비스 로직에 적용하여 확장성 있는 데이터 구조 설계 능력을 증명함
* **외부 API 통합 능력**: 카카오페이 API의 전체 생명주기(준비-승인-취소)를 이해하고 서버측 비즈니스 로직(주문, 재고)과 성공적으로 결합함
* **트랜잭션 관리**: 결제와 주문 데이터 생성 간의 원자성을 보장하기 위해 트랜잭션 관리를 철저히 수행함

## 🛠️ 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.5.6**
- **Spring WebFlux WebClient** (카카오페이 API 연동)
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
  - 상품 주문 및 결제 (카카오페이 승인/취소 포함)
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

2. **카카오페이 설정:**
   - `.env` 또는 `application.properties`에 가맹점 정보를 추가합니다.
     ```properties
     # 카카오페이 가맹점 정보
     custom.kakaopay.cid=TC0ONETIME          # 테스트 CID 또는 발급받은 CID
     custom.kakaopay.secret-key=your_secret   # 관리자 페이지에서 발급받은 Secret Key
     ```
   - 카카오페이 관리자센터에서 **허용 도메인**에 서비스 도메인/포트(`http://localhost:8080` 등)를 등록하세요.
   - 리다이렉트 URL은 현재 요청 경로에 `/success/{주문번호}`, `/cancel/{주문번호}`, `/fail/{주문번호}`가 자동으로 붙으므로, 등록 도메인에 포함되도록 합니다.

3. **데이터베이스 스키마 생성:**
   - `TABLE_DETAILS.md` 파일의 SQL 스크립트를 실행하여 테이블을 생성합니다.

4. **프로젝트 실행:**
   - IDE(Eclipse, IntelliJ 등)에서 프로젝트를 가져와 실행합니다.
   - 또는, 프로젝트 루트 디렉터리에서 아래 명령어를 실행합니다.
     ```shell
     # Windows
     mvnw.cmd spring-boot:run
     
     # Linux/Mac
     ./mvnw spring-boot:run
     ```

5. **접속:**
   - 웹 브라우저에서 `http://localhost:8080`으로 접속합니다.

## 📝 주요 특징

- **계층형 카테고리 구조**: 상품 카테고리를 계층적으로 관리하여 유연한 분류 체계 제공
- **상품 옵션 관리**: 사이즈, 색상 등 다양한 옵션 조합을 지원하는 유연한 옵션 시스템
- **권한 기반 접근 제어**: Interceptor를 활용한 세밀한 권한 관리
- **RESTful API**: 프론트엔드와 백엔드 분리를 위한 REST API 제공
- **이메일 인증**: 회원가입 및 비밀번호 찾기 시 이메일 인증 기능
- **파일 업로드**: 상품 이미지, 리뷰 이미지 등 멀티파트 파일 업로드 지원
- **예외 처리**: 전역 예외 처리 및 커스텀 예외를 통한 안정적인 에러 핸들링
- **카카오페이 실결제 연동**: WebClient 기반 결제 준비/승인/조회/취소 API 구현 및 주문/재고/환불 로직과 연결

## 💳 카카오페이 결제 흐름

- **준비(ready)**: `/orders/payment` POST에서 장바구니 금액/상품명을 기반으로 카카오페이 `ready` 호출 → `tid`와 결제 페이지 URL 수신 후 리다이렉트.
- **승인(approve)**: 결제 성공 리다이렉트(`/orders/payment/success/{orderId}`) 시 `pg_token`으로 승인 API 호출. 응답 `tid`, 결제 금액, 상품명을 주문 엔터티에 저장하고 상태를 `결제완료`로 설정.
- **주문/재고 처리**: 승인 성공 시
  - 주문/주문상세 insert 및 옵션 재고 차감
  - 장바구니 비우기
  - 사용자가 선택한 경우 배송지 정보를 회원 기본 배송지로 업데이트
- **취소(cancel)**: `ordersNo` 기준 전체 주문 취소 시 카카오페이 취소 API 호출 → 성공 시 재고 복구, 주문 상태를 `주문취소`로 변경.
- **DB 필드**: `orders.orders_tid`(카카오페이 거래번호), `orders_total_price`, `orders_remain_price` 등을 저장해 결제/환불 기준으로 사용.