# Backend: Monitor de microservicios

API REST en Spring Boot 3 (Java 17) que simula tres microservicios detrás de un Proxy
genérico de auditoría. Los logs y los datos de ejemplo viven en memoria: no hay base de datos.

## Requisitos
- Java 17 o superior
- Maven 3.9 o superior

## Ejecutar en local
```bash
mvn spring-boot:run
```
El servidor queda en `http://localhost:8080`. Comprobación rápida: `GET /actuator/health`.

Para generar el JAR:
```bash
mvn -DskipTests clean package
java -jar target/app.jar
```

## Variables de entorno
| Variable | Descripción | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP (Railway lo asigna solo) | `8080` |
| `CORS_ORIGINS` | Orígenes permitidos, separados por coma | `*` |
| `APP_TIMEZONE` | Zona horaria de las marcas de tiempo | `America/Bogota` |

## Estructura
```
src/main/java/com/example/monitor/
├── controller/   ServiceController, MetricsController, ApiExceptionHandler
├── service/      InventoryService, OrderService, PaymentService,
│                 MetricsService, LoadSimulationService, BaseService
├── proxy/        MicroserviceProxy<T>, MicroserviceProxyImpl<T>, ServiceOperation<T>
├── model/        LogEntry, LogStatus, MetricsSummary, ServiceMetrics, PageResult, ServiceException
├── repository/   LogRepository (almacenamiento en memoria, seguro para hilos)
└── config/       ProxyConfig (envuelve cada servicio con el Proxy), CorsConfig
```

## Patrón Proxy
`MicroserviceProxyImpl<T>.execute(operation, params)`:
1. Genera un UUID por solicitud.
2. Marca el inicio y ejecuta la operación real del servicio.
3. Calcula la duración en milisegundos.
4. Guarda un `LogEntry` con estado `SUCCESS`, o `ERROR` con el mensaje si hubo excepción.
5. Devuelve el resultado original o lanza una `ServiceException` con el `requestId`.

Los controladores solo reciben los beans `inventoryProxy`, `orderProxy` y `paymentProxy`,
así que ninguna llamada llega a un servicio sin pasar por el Proxy.

## Endpoints
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/services/inventory/{operation}` | `getStock`, `addStock`, `removeStock` |
| POST | `/api/services/orders/{operation}` | `createOrder`, `getOrder` |
| POST | `/api/services/payments/{operation}` | `processPayment`, `refundPayment` |
| GET | `/api/metrics/summary` | Totales, tasa de errores, promedio y métricas por servicio |
| GET | `/api/metrics/logs` | Parámetros opcionales: `service`, `status`, `from`, `to`, `page`, `size` |
| POST | `/api/metrics/simulate-load` | Genera 50 llamadas aleatorias a través de los proxies |
| GET | `/actuator/health` | Estado del servicio |

### Cuerpos de ejemplo
```
getStock        {"productId":"LAPTOP-001"}
addStock        {"productId":"LAPTOP-001","quantity":10}
removeStock     {"productId":"LAPTOP-001","quantity":5}
createOrder     {"productId":"MOUSE-002","quantity":2}
getOrder        {"orderId":"ORD-1001"}
processPayment  {"orderId":"ORD-1001","amount":150}
processPayment  {"orderId":"ORD-1001","amount":150,"forceError":true}
refundPayment   {"paymentId":"PAY-0001","amount":50}
```
Productos iniciales: `LAPTOP-001`, `MOUSE-002`, `KEYBOARD-003`, `MONITOR-004`.

### Errores
- `processPayment` falla de forma aleatoria el 10% de las veces, o siempre con `"forceError": true`.
- Una operación fallida responde `422` con `{"status":"ERROR","requestId":"...","message":"..."}`
  y queda registrada con estado `ERROR`.
- Una operación inexistente, un parámetro faltante, un producto desconocido o existencias
  insuficientes también se registran como `ERROR`.

### Filtros de `/api/metrics/logs`
- `service`: `InventoryService`, `OrderService` o `PaymentService`.
- `status`: `SUCCESS` o `ERROR`.
- `from` y `to`: fecha y hora ISO, por ejemplo `2026-10-30T14:00:00`.
- `page` (desde 0) y `size` (por defecto 20, máximo 200).
- Los resultados salen del más reciente al más antiguo.

### Estructura de un log
```json
{
  "requestId": "260894f1-ef98-4d7d-a753-216e7aabd4b7",
  "service": "PaymentService",
  "operation": "processPayment",
  "duration": 198,
  "status": "SUCCESS",
  "timestamp": "2026-10-30T14:00:00.123",
  "message": "Operación ejecutada correctamente"
}
```

## Comprobar el Proxy con curl
```bash
curl -X POST localhost:8080/api/services/inventory/getStock \
  -H 'Content-Type: application/json' -d '{"productId":"LAPTOP-001"}'

curl -X POST localhost:8080/api/services/payments/processPayment \
  -H 'Content-Type: application/json' -d '{"orderId":"ORD-1","amount":50,"forceError":true}'

curl 'localhost:8080/api/metrics/logs?size=2'
curl localhost:8080/api/metrics/summary
curl -X POST localhost:8080/api/metrics/simulate-load
```

## Pruebas
```bash
mvn test
```
`MicroserviceProxyTest` comprueba que el Proxy registra tanto las llamadas exitosas como las fallidas.

## Despliegue en Railway
1. Sube el repositorio a GitHub.
2. En Railway: **New Project → Deploy from GitHub repo**.
3. En *Settings → Root Directory* escribe `backend`. El archivo `railway.json` ya define la compilación, el arranque y el healthcheck.
4. En *Variables* agrega `NIXPACKS_JDK_VERSION=17` y `CORS_ORIGINS=https://TU-FRONTEND.vercel.app`.
5. En *Settings → Networking* pulsa **Generate Domain**.
6. Verifica `https://TU-BACKEND.up.railway.app/actuator/health`.

## Notas
- Los logs se pierden al reiniciar y se conservan como máximo 5000.
- La simulación de carga tarda unos segundos porque cada servicio simula latencia de 20 a 250 ms.
