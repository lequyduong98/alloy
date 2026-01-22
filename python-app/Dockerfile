# Sử dụng base image Python
FROM python:3.11-slim

WORKDIR /app

# Cài đặt thư viện OpenTelemetry cần thiết
RUN pip install \
    opentelemetry-api \
    opentelemetry-sdk \
    opentelemetry-exporter-otlp \
    opentelemetry-instrumentation-logging \
    prometheus-client \
    requests

COPY app_generator.py .

CMD ["python", "app_generator.py"]