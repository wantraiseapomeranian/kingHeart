# kingHeart Shopping Mall

Spring Boot를 기반으로 구축된 현대적인 온라인 쇼핑몰 플랫폼입니다. 사용자에게는 편리한 쇼핑 경험을, 관리자에게는 유연한 상품 및 주문 관리 환경을 제공합니다.

## 📑 프로젝트 개요

본 프로젝트는 자바 백엔드 기술력을 바탕으로 쇼핑몰의 핵심인 주문, 결제, 재고 관리 시스템을 구현하는 데 초점을 맞추었습니다. 특히 프로젝트 종료 후에도 실무 수준의 결제 환경을 구축하기 위해 **카카오페이 API**를 자기주도적으로 연동하여 시스템을 완성했습니다.

---

## 👥 팀원 소개

| 이름 | 담당 영역 | GitHub |
| :---: | :---: | :--- |
| **김민준** | **PM, 장바구니 기능, 결제 로직, 카테고리 시스템, 상품 옵션 기능, 구매내역 페이지 구현** | https://github.com/wantraiseapomeranian | 
| 김동규 | 관리자 상품 등록, 상품 옵션, 카테고리 구현 | https://github.com/kim9709 | 
| 김영도 | 상품 위시리스트 구현, 상품 관련 리뷰 구현 | https://github.com/kimyeong123 | 
| 박창현 |  회원 기능, 고객센터 게시판 구현 | https://github.com/charlie098 | 

---

## 🔧 Troubleshooting & 핵심 기술 경험

### 1. 동적 상품 옵션과 SKU 관리: 데카르트 곱(Cartesian Product) 알고리즘

의류 쇼핑몰 특성상 색상, 사이즈 등 N개의 옵션이 조합되어야 하며, 각 조합(SKU)별로 독립적인 재고 관리가 필수적이었습니다.

#### 문제 상황
- **확장성 문제:** 상품별 옵션(색상, 사이즈 등)을 각각 별도 컬럼으로 관리할 경우, 새로운 옵션 속성이 추가될 때마다 DB 스키마를 변경해야 하는 비효율이 발생했습니다.
- **재고 추적의 어려움:** 'Red' 색상 재고와 'L' 사이즈 재고를 따로 관리하면, 정작 'Red 색상의 L 사이즈'가 몇 개 남았는지 특정하기 어려운 데이터 구조적 한계가 있었습니다.
- **옵션 등록의 번거로움:** 관리자가 수십 개의 옵션 조합을 일일이 수동으로 등록해야 하여 휴먼 에러 발생 가능성이 높았습니다.

#### 해결 방안
1. **SKU(Stock Keeping Unit) 방식 도입**
   - '상품-SKU' 간의 1:N 관계를 정의했습니다. 관리자가 색상(Red, Blue)과 사이즈(S, M)를 입력하면, 백엔드에서 이를 조합하여 독립적인 레코드(SKU)로 생성하도록 구현했습니다.

2. **데카르트 곱(Cartesian Product) 알고리즘 적용**
   - 사용자가 입력한 옵션 그룹 배열을 바탕으로 **모든 가능한 경우의 수**를 계산하는 알고리즘을 Java로 구현하여, 클릭 한 번으로 수십 개의 옵션 조합이 자동 생성되도록 했습니다.

3. **옵션별 재고 격리**
   - `Product` 테이블이 아닌 `ProductOption` 테이블에 `stock` 컬럼을 배치하여, 옵션 조합별로 실시간 재고 차감 및 품절 처리가 가능하도록 정규화했습니다.

**결과**: 옵션 조합별로 실시간 재고 차감 및 품절 처리가 가능해졌으며, 복잡한 다중 조인 없이도 데이터의 원자성과 무결성을 확보했습니다.

---

### 2. 결제 데이터 정합성 보장: 카카오페이 생명주기(Lifecycle) 관리

실제 PG사(카카오페이)의 결제 승인과 내부 DB의 주문 생성 간의 시차로 인해 발생할 수 있는 데이터 불일치 문제를 해결해야 했습니다.

#### 문제 상황
- **Ghost Order 발생 위험:** 카카오페이 결제 승인 API(`approve`)는 성공했으나, 내부 DB에 주문 정보를 `INSERT`하는 도중 서버 에러가 발생하면 "돈은 빠져나갔는데 주문 내역은 없는" 치명적인 상황이 발생할 수 있었습니다.
- **재고 차감 시점의 딜레마:** 결제 페이지 진입 시점에 재고를 차감하면 구매를 포기한 유저 때문에 재고가 묶이고, 결제 완료 후 차감하면 동시성 이슈로 '마이너스 재고'가 발생할 위험이 있었습니다.
- **부분 환불 검증:** 여러 상품을 한 번에 결제한 후 일부만 취소할 때, 남은 금액과 취소 가능 금액의 정합성을 검증하는 로직이 복잡했습니다.

#### 해결 방안
1. **Atomic Transaction 설계**
   - **결제 승인 요청 -> 주문/상세 데이터 Insert -> 장바구니 삭제 -> 재고 차감**의 모든 과정을 하나의 `@Transactional`로 묶었습니다.
   - 만약 내부 로직 중 하나라도 실패(Exception)하면, `catch` 블록에서 즉시 **카카오페이 결제 취소 API**를 코드로 자동 호출하여 원자성(All or Nothing)을 보장했습니다.

2. **WebClient 비동기 통신 도입**
   - 기존의 Blocking 방식인 `RestTemplate` 대신 `Spring WebFlux`의 `WebClient`를 도입하여, 외부 PG사와의 통신 지연이 내부 서버의 스레드 고갈로 이어지지 않도록 성능을 최적화했습니다.

3. **재고 자동 복구 트리거**
   - 주문 취소(전체/부분)가 확정되는 순간, 해당 주문 건에 연결된 SKU 정보를 조회하여 차감되었던 수량만큼 재고를 즉시 `UPDATE`하는 로직을 서비스 계층에 구현하여 CS 소요를 최소화했습니다.

**결과**: 네트워크 불안정이나 DB 오류 상황에서도 결제 금액과 주문 내역이 100% 일치하는 멱등성을 확보했으며, 부분 취소 및 재고 관리가 자동화된 안정적인 결제 시스템을 구축했습니다.

---

### 3. 무제한 확장이 가능한 재귀적 카테고리 구조

카테고리의 깊이(Depth)가 고정되어 있어 확장이 불가능했던 기존 구조를 개선하여, 유연한 카테고리 시스템을 구축했습니다.

#### 문제 상황
- '대분류 > 중분류 > 소분류'와 같이 컬럼을 고정(`cate_1`, `cate_2`)하여 설계할 경우, 4단계 이상의 깊이가 필요해지면 테이블 구조 자체를 뜯어고쳐야 했습니다.
- 하위 카테고리 조회 시 불필요한 `JOIN` 연산이 반복되어 쿼리 성능이 저하되었습니다.

#### 해결 방안
1. **Self-Referencing 테이블 설계**
   - `CATEGORY` 테이블에 `PARENT_NO` 컬럼을 추가하여, 자기 자신의 PK를 참조하는 방식으로 무제한 계층 구조를 설계했습니다.
   
2. **재귀적 조회 로직 구현**
   - **Oracle:** `START WITH ... CONNECT BY PRIOR` 구문을 활용하여 단 한 번의 쿼리로 전체 트리 구조를 조회했습니다.
   - **Java:** 조회된 데이터를 재귀 함수를 통해 JSON 트리 구조로 변환하여 프론트엔드(UI)에서 계층형 메뉴를 쉽게 렌더링할 수 있도록 가공했습니다.

**결과**: 카테고리 깊이에 제한이 없는 유연한 구조를 확보하여, 추후 상품군이 확장되더라도 코드 수정 없이 데이터 추가만으로 메뉴를 관리할 수 있게 되었습니다.


## 👨‍💻 프로젝트 종료 후 독자적 고도화
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

## ⭐️ 본인 구현 및 기여 상세

**Role: Backend Core Developer & Payment System Architect**

<br>

쇼핑몰의 핵심인 **주문/결제 시스템**과 **재고 관리 로직**을 전담하여 구현했으며, 프로젝트 종료 후에도 **카카오페이 실결제 연동**을 자기주도적으로 완성하여 실무 수준의 시스템을 구축했습니다.

### 💳 1. 카카오페이 실결제 연동 시스템 (Full-Stack)

- **구현 내용:** 실제 PG사(카카오페이) API를 연동하여 결제 준비, 승인, 취소, 조회의 전 생명주기를 관리하는 안정적인 결제 시스템 구축.

- **기술적 해결:**

    - **Backend (Payment Lifecycle Management):**

        - **WebClient 비동기 통신:** `Spring WebFlux`의 `WebClient`를 활용하여 카카오페이 API와의 통신을 비동기로 처리했습니다. `RestTemplate` 대신 `WebClient`를 선택한 이유는 외부 API 호출 지연이 내부 서버의 스레드 풀 고갈로 이어지지 않도록 하기 위함입니다. `@Configuration`으로 `KakaoPayProperties`를 주입받아 Secret Key를 안전하게 관리했습니다.

        - **Atomic Transaction 설계:** 결제 승인(`approve`) API 호출 → 주문/주문상세 INSERT → 장바구니 삭제 → 재고 차감의 모든 과정을 하나의 `@Transactional`로 묶었습니다. 만약 내부 로직 중 하나라도 실패하면, `catch` 블록에서 즉시 카카오페이 취소 API를 자동 호출하여 **"돈은 빠져나갔는데 주문 내역은 없는"** Ghost Order 상황을 원천 차단했습니다.

        - **세션 기반 상태 관리:** 결제 준비(`ready`) 단계에서 생성된 `tid`와 주문 정보를 세션에 임시 저장하고, 결제 성공 리다이렉트(`/payment/success/{partnerOrderId}`) 시 `pg_token`과 함께 승인 요청을 보내는 방식으로, 중간에 사용자가 브라우저를 닫아도 세션 만료로 안전하게 처리되도록 설계했습니다.

        - **재고 자동 복구 시스템:** 주문 취소 시 `KakaoPayCancelRequestVO`를 통해 카카오페이 취소 API를 호출하고, 성공 시 해당 주문의 모든 `order_detail`을 조회하여 차감되었던 수량만큼 옵션별 재고를 자동으로 `UPDATE`하는 로직을 구현했습니다. 이를 통해 CS 소요를 최소화했습니다.

    - **Frontend (Payment Flow UX):**

        - **결제 페이지 리다이렉트:** 카카오페이 `ready` API 응답으로 받은 `next_redirect_pc_url`을 그대로 리다이렉트하여 사용자가 카카오페이 결제 페이지로 자연스럽게 이동하도록 구현했습니다.

        - **결제 결과 페이지:** 결제 성공/취소/실패 각각에 대한 전용 JSP 페이지를 구현하여 사용자에게 명확한 피드백을 제공했습니다.

### 🛒 2. 장바구니 및 주문 프로세싱 시스템 (Backend)

- **구현 내용:** 장바구니 상품 관리부터 주문 생성, 재고 차감까지의 전체 주문 프로세스를 안정적으로 처리하는 비즈니스 로직 구현.

- **기술적 해결:**

    - **장바구니 실시간 금액 계산:** `CartService`에서 장바구니 상품 목록을 조회할 때, 각 상품의 현재 가격(`product_price`)과 수량(`cart_amount`)을 곱하여 실시간 총합계를 계산했습니다. 상품 가격이 변경되어도 장바구니 조회 시점의 최신 가격을 반영하도록 설계했습니다.

    - **주문 상세 일괄 처리 (Batch Insert):** 여러 상품을 한 번에 주문할 때, `OrderDetailDao.batchInsert()`를 통해 N개의 `order_detail` 레코드를 단일 쿼리로 일괄 삽입하여 DB 부하를 최소화했습니다.

    - **옵션별 재고 차감 검증:** 주문 생성 시 각 상품 옵션(`option_no`)별로 재고를 차감하는 과정에서, `ProductOptionDao.updateStock()`이 실패(재고 부족)하면 `RuntimeException`을 던져 트랜잭션 전체를 롤백시켜 데이터 정합성을 보장했습니다.

    - **배송지 정보 자동 저장:** 사용자가 결제 페이지에서 "기본 배송지로 저장" 체크박스를 선택하면, 주문 완료 후 `MemberService.updateMemberAddress()`를 호출하여 회원 정보의 주소 필드를 자동 업데이트하는 기능을 구현했습니다.

### 🌳 3. 재귀적 계층형 카테고리 시스템 (Full-Stack)

- **구현 내용:** Self-Referencing 테이블 구조를 활용하여 무제한 깊이의 카테고리를 지원하고, 프론트엔드에서 트리 구조로 시각화하는 시스템 구축.

- **기술적 해결:**

    - **Backend (Oracle Hierarchical Query):**

        - **START WITH ... CONNECT BY PRIOR:** Oracle의 계층형 쿼리 구문을 활용하여 단 한 번의 SQL로 전체 카테고리 트리를 조회했습니다. `PARENT_NO`가 NULL인 최상위 노드를 `START WITH`로 지정하고, `CONNECT BY PRIOR category_no = parent_no`로 하위 노드를 재귀적으로 연결했습니다.

        - **Java 재귀 변환 로직:** DB에서 조회한 평면 데이터를 Java의 재귀 함수를 통해 JSON 트리 구조로 변환하여, 프론트엔드에서 `children` 배열을 활용한 중첩 렌더링이 가능하도록 가공했습니다.

    - **Frontend (Tree Visualization):**

        - **동적 메뉴 렌더링:** 카테고리 데이터를 받아와서 `<ul>`/`<li>` 태그를 재귀적으로 생성하여 계층형 메뉴를 시각화했습니다. 상위 카테고리를 클릭하면 하위 카테고리로 필터링되는 네비게이션 로직을 구현했습니다.

### ⚙️ 4. 관리자 상품 옵션 및 SKU 자동 생성 시스템 (Full-Stack)

- **구현 내용:** 관리자가 색상, 사이즈 등 N개의 옵션을 입력하면, 모든 조합(SKU)을 자동으로 생성하여 옵션별 재고 관리가 가능하도록 하는 시스템 구현.

- **기술적 해결:**

    - **Backend (Cartesian Product Algorithm):**

        - **데카르트 곱 알고리즘:** 사용자가 입력한 옵션 그룹 배열(예: `[["Red", "Blue"], ["S", "M", "L"]]`)을 바탕으로, Java의 중첩 반복문과 재귀를 활용하여 **모든 가능한 경우의 수**를 계산하는 알고리즘을 구현했습니다. 예를 들어 2색상 × 3사이즈 = 6개의 SKU가 자동 생성됩니다.

        - **옵션별 재고 격리:** `ProductOption` 테이블에 `stock` 컬럼을 배치하여, 각 옵션 조합별로 독립적인 재고를 관리하도록 정규화했습니다. 주문 시 `option_no`를 기준으로 재고를 차감하므로, "Red 색상의 L 사이즈"가 품절되어도 "Blue 색상의 L 사이즈"는 별도로 판매 가능합니다.

    - **Frontend (Dynamic Option UI):**

        - **동적 옵션 입력 필드:** 관리자가 "옵션 그룹 추가" 버튼을 클릭하면 JavaScript로 새로운 옵션 입력 필드가 동적으로 추가되며, 각 그룹별로 여러 옵션 값을 입력할 수 있는 유연한 UI를 구현했습니다.

### 📊 5. 주문 내역 조회 및 상태 관리 시스템 (Backend)

- **구현 내용:** 사용자별 주문 내역을 효율적으로 조회하고, 주문 상태 변경에 따른 비즈니스 로직을 처리하는 시스템 구현.

- **기술적 해결:**

    - **복합 조인 쿼리 최적화:** `OrderListDao`에서 주문 정보, 상품 정보, 옵션 정보를 한 번의 쿼리로 조회하기 위해 여러 테이블을 `JOIN`하여, 애플리케이션 레벨에서의 추가 조회를 최소화했습니다.

    - **주문 상태 검증 로직:** 주문 취소 시 현재 주문 상태가 "결제완료" 또는 "배송준비중"인지 검증하여, 이미 배송 중이거나 완료된 주문은 취소할 수 없도록 비즈니스 규칙을 적용했습니다.

---


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