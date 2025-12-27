# kingHeart Shopping Mall

KH 세미프로젝트로 진행한 쇼핑몰 웹 애플리케이션입니다.

## 📑 프로젝트 개요

Spring Boot와 MyBatis를 기반으로 구축된 온라인 쇼핑몰 플랫폼입니다. 사용자는 상품을 검색하고, 장바구니에 담아 주문할 수 있으며, 관리자는 상품, 회원, 주문 등을 관리할 수 있는 시스템을 갖추고 있습니다.

## 🛠️ 기술 스택

### Backend
- Java 11
- Spring Boot 2.7.17
- MyBatis
- Oracle Database (ojdbc11)
- Spring Boot Mail Sender

### Frontend
- JSP
- JavaScript
- CSS
- Summernote (상품 상세 정보 편집)

### Server
- Apache Tomcat

## ✨ 주요 기능

### 🙍‍♂️ 사용자 기능
- **회원 관리:** 회원가입, 로그인/로그아웃, 아이디/비밀번호 찾기, 회원 정보 수정, 회원 탈퇴
- **상품:** 상품 목록 보기, 상품 상세 정보 보기, 카테고리별 상품 조회
- **주문:** 장바구니, 위시리스트, 상품 주문 및 결제, 주문 내역 조회
- **고객센터:** 공지사항 및 문의 게시판

### 👨‍💼 관리자 기능
- **회원 관리:** 전체 회원 목록 조회 및 관리
- **상품 관리:** 상품 등록, 수정, 삭제, 재고 관리
- **주문 관리:** 전체 주문 내역 조회 및 주문 상태 변경
- **카테고리 관리:** 상품 카테고리 추가, 수정, 삭제
- **배너 관리:** 메인 페이지 배너 등록 및 관리
- **통계:** 매출 및 주문 관련 통계 확인
- **상품 옵션 관리:** 사이즈, 색상 등 상품 옵션 관리

## 🗄️ 데이터베이스 스키마

프로젝트에 사용된 테이블 구조에 대한 ERD는 `테이블` 디렉터리에서 확인할 수 있습니다.

## 🚀 실행 방법

1.  **데이터베이스 설정:**
    - Oracle 데이터베이스를 준비합니다.
    - `src/main/resources/application.properties` 파일에 자신의 데이터베이스 환경에 맞게 아래 정보를 수정합니다.
      ```properties
      spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
      spring.datasource.username=your_username
      spring.datasource.password=your_password
      ```
2.  **프로젝트 실행:**
    - IDE(Eclipse, IntelliJ 등)에서 프로젝트를 가져와 실행합니다.
    - 또는, 프로젝트 루트 디렉터리에서 아래 명령어를 실행합니다.
      ```shell
      ./mvnw spring-boot:run
      ```
3.  **접속:**
    - 웹 브라우저에서 `http://localhost:8080`으로 접속합니다.
