# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# JVM 메모리 옵션 (EC2 인스턴스에 맞게 조절)
ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC -Xms256m -Xmx512m"

# CI/CD에서 복사해 올 JAR
COPY app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
