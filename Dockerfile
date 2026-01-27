# 1. 빌드 단계 (Java 21)
FROM amazoncorretto:21 AS builder
WORKDIR /app
COPY . .

# Maven이 압축을 풀 수 있게 tar와 gzip을 설치
RUN yum install -y tar gzip

# 실행 권한 부여 및 빌드
RUN chmod +x ./mvnw || true
RUN ./mvnw clean package -DskipTests

# 2. 실행 단계 (Java 21)
FROM amazoncorretto:21
WORKDIR /app

# WAR 파일 복사
COPY --from=builder /app/target/*.war app.war

# 실행
ENTRYPOINT ["java", "-jar", "app.war"]
