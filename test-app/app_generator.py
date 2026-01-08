import logging
import time
import requests
import random
from opentelemetry import trace
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.instrumentation.logging import LoggingInstrumentor

# Cấu hình OpenTelemetry Resource (Service Name)
RESOURCE = Resource.create({"service.name": "full-stack-test-generator", "environment": "testing"})

# Địa chỉ OTLP Collector của Alloy (tên service trong Docker network)
OTEL_EXPORTER_OTLP_ENDPOINT = "http://alloy:4317"

# -----------------
# Cấu hình Tracing
# -----------------
tracer_provider = TracerProvider(resource=RESOURCE)
trace_exporter = OTLPSpanExporter(endpoint=OTEL_EXPORTER_OTLP_ENDPOINT, insecure=True)
tracer_provider.add_span_processor(BatchSpanProcessor(trace_exporter))
trace.set_tracer_provider(tracer_provider)
tracer = trace.get_tracer(__name__)

# -----------------
# Cấu hình Logging
# -----------------
LoggingInstrumentor().instrument(set_logging_format=True)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# -----------------
# Cấu hình Metrics (sử dụng Prometheus registry và gửi qua OTLP)
# Do sử dụng Prometheus Client nên Alloy sẽ scrape metrics này.
# -----------------
from prometheus_client import start_http_server, Counter, Gauge

REQUESTS_TOTAL = Counter('http_requests_total', 'Tổng số request được tạo')
REQUEST_LATENCY = Gauge('http_request_duration_seconds', 'Thời gian xử lý request')

# Khởi động Prometheus HTTP server cục bộ cho Alloy scrape
start_http_server(8000)

def generate_trace_and_log():
    # 1. TRACE (Dấu vết)
    with tracer.start_as_current_span("parent-request"):
        REQUESTS_TOTAL.inc()
        latency = random.uniform(0.1, 0.5)

        with tracer.start_as_current_span("database-query"):
            time.sleep(latency / 2)
            logger.info("Executing database query successfully.")

        with tracer.start_as_current_span("external-service-call"):
            time.sleep(latency / 2)
            # 2. LOG (Nhật ký)
            if random.random() < 0.1:
                logger.error("Failed to connect to external service!", extra={'user_id': random.randint(100, 999)})
            else:
                logger.info(f"External service call took {latency/2:.2f}s.")
        
        # 3. METRIC (Chỉ số)
        REQUEST_LATENCY.set(latency)

        logger.info(f"Trace completed for latency {latency:.2f}s.")

if __name__ == "__main__":
    logger.info("Starting Full Stack Test Generator...")
    while True:
        generate_trace_and_log()
        time.sleep(2) # Tạo dữ liệu mỗi 2 giây