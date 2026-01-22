# --- GIAI ĐOẠN 1: Build file JAR (Sử dụng Maven) ---
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Chỉ copy file cấu hình trước để tận dụng cache của Docker cho các thư viện (dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy toàn bộ mã nguồn và thực hiện build
COPY src ./src
RUN mvn clean package -DskipTests

# --- GIAI ĐOẠN 2: Chạy ứng dụng ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Tải OpenTelemetry Agent (Máy ảnh)
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar .

# Copy file .jar ĐÃ BUILD từ Giai đoạn 1 sang Giai đoạn 2
COPY --from=build /app/target/*.jar app.jar

# Biến môi trường cấu hình sẵn
ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc

ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent.jar", "-Dotel.resource.attributes=service.name=java-app", "-jar", "app.jar"]