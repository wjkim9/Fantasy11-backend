# ---- Build stage (Gradle 포함) ----
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# 캐시 효율을 위해 설정/래퍼 먼저 복사
COPY settings.gradle build.gradle gradlew ./
COPY gradle ./gradle

# 소스
COPY src ./src

# 권한 & 빌드
RUN chmod +x ./gradlew \
 && ./gradlew clean build -x test --no-daemon

# ---- Runtime stage (JRE 최소) ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 필요 시 JVM 메모리 한도 (t3.medium 고려)
ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC -Xms256m -Xmx512m"

COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 8080

# 헬스체크(선택: Spring Actuator 사용 시)
# HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]
