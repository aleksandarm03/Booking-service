## PDS – Booking Microservices (Spring Boot/Cloud)

Mali mikroservisni sistem za domen "rezervacije" koji demonstrira principe distribuiranih sistema:
service discovery (Eureka), API gateway (Spring Cloud Gateway), komunikacija između servisa (OpenFeign), otpornost (Resilience4j), H2 baze, i agregacioni endpoint.

### Arhitektura (pregled)
- Klijent → `api-gateway` → rutira ka servisima po putanjama i nazivima servisa iz Eureke
- Servisi (`user-service`, `bookings-service`) se registruju u `service-discovery` (Eureka)
- `bookings-service` poziva `user-service` preko Feign klijenta i štiti poziv Resilience4j-om (Retry + Circuit Breaker)

### Moduli
- `service-discovery`: Eureka Server (8761)
- `api-gateway`: Spring Cloud Gateway + jednostavan API key filter (8080)
- `user-service`: CRUD nad korisnikom + H2 baza + validacija (8081)
- `bookings-service`: CRUD nad rezervacijom + Feign → `user-service` + Resilience4j + agregacija (8082)

### Tehnologije
- Spring Boot 3, Spring Data JPA, Spring Web
- Spring Cloud Netflix (Eureka), Spring Cloud Gateway
- OpenFeign (servis–servis pozivi)
- Resilience4j (Circuit Breaker + Retry)
- H2 in-memory DB
- Maven multi-module

---

## Podešavanje i build

Zahtevi: JDK 17+, Maven 3.9+

1) Build (iz root-a):
```bash
./mvnw clean install
# Windows PowerShell: .\mvnw clean install
```

2) Pokretanje servisa (četiri terminala, ovim redom):
- Eureka (service-discovery):
```bash
cd service-discovery
./mvnw spring-boot:run
```
- User service:
```bash
cd user-service
./mvnw spring-boot:run
```
- Bookings service:
```bash
cd bookings-service
./mvnw spring-boot:run
```
- API Gateway:
```bash
cd api-gateway
./mvnw spring-boot:run
```

3) Provere
- Eureka dashboard: `http://localhost:8761` (treba da vidiš `api-gateway`, `user-service`, `bookings-service`)
- Gateway: `http://localhost:8080`

Napomena: Gateway koristi API ključ – svaki poziv mora imati header `X-API-KEY: dev-key` (možeš promeniti u properties).

---

## Konfiguracije (ključne vrednosti)

### api-gateway/src/main/resources/application.properties
```properties
spring.application.name=api-gateway
server.port=8080

spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Eksplicitne rute (pored discovery locator-a)
spring.cloud.gateway.routes[0].id=user-service
spring.cloud.gateway.routes[0].uri=lb://user-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**

spring.cloud.gateway.routes[1].id=bookings-service
spring.cloud.gateway.routes[1].uri=lb://bookings-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/bookings/**

# Jednostavan API key (opciono). Header: X-API-KEY
gateway.api-key=dev-key
```

### service-discovery/src/main/resources/application.properties
```properties
spring.application.name=service-discovery
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

### user-service/src/main/resources/application.properties
```properties
spring.application.name=user-service
server.port=8081

spring.datasource.url=jdbc:h2:mem:usersdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

### bookings-service/src/main/resources/application.properties
```properties
spring.application.name=bookings-service
server.port=8082

spring.datasource.url=jdbc:h2:mem:bookingsdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Feign timeouts
feign.client.config.default.connectTimeout=1000
feign.client.config.default.readTimeout=2000

# Resilience4j (isti name kao u @CircuitBreaker/@Retry: userService)
resilience4j.circuitbreaker.instances.userService.slidingWindowSize=10
resilience4j.circuitbreaker.instances.userService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.userService.waitDurationInOpenState=5s
resilience4j.retry.instances.userService.maxAttempts=3
resilience4j.retry.instances.userService.waitDuration=300ms
```

---

## Endpoints i testiranje

Svi zahtevi ka gateway-u moraju imati header: `X-API-KEY: dev-key`.

### Users (preko gateway-a)
- `POST /api/users` – kreiraj korisnika
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: dev-key" \
  -d '{ "name": "Ana", "email": "ana@example.com" }'
```
- `GET /api/users` – listaj korisnike
```bash
curl http://localhost:8080/api/users -H "X-API-KEY: dev-key"
```
- `GET /api/users/{id}` – dohvati korisnika
- `PUT /api/users/{id}` – izmeni korisnika
- `DELETE /api/users/{id}` – obriši korisnika

### Bookings (preko gateway-a)
- `POST /api/bookings` – kreiraj rezervaciju (validira `userId` preko Feign-a)
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: dev-key" \
  -d '{ "userId": 1, "startTime": "2026-01-01T10:00:00", "endTime": "2026-01-01T11:00:00" }'
```
- `GET /api/bookings` – listaj rezervacije
- `GET /api/bookings/{id}` – dohvati rezervaciju
- `PUT /api/bookings/{id}` – izmeni rezervaciju
- `DELETE /api/bookings/{id}` – obriši rezervaciju
- `GET /api/bookings/{id}/details` – agregacija (Booking + User)

### H2 konzole
- user-service: `http://localhost:8081/h2-console` (JDBC: `jdbc:h2:mem:usersdb`)
- bookings-service: `http://localhost:8082/h2-console` (JDBC: `jdbc:h2:mem:bookingsdb`)
  - Username: `sa`, Password: prazno

---

## Kako radi otpornost (Resilience4j)
- `bookings-service` pre kreiranja rezervacije poziva `user-service` da proveri da korisnik postoji
- Ako je `user-service` spor/nedostupan → aktivira se Retry (npr. 3 pokušaja)
- Ako i dalje ne uspe → Circuit Breaker se otvara i poziv se kratko-cirkuitira (fallback vraća jasnu grešku)

---

## Najčešće greške i rešenja
- 401 Unauthorized → dodaj `X-API-KEY: dev-key` header
- 503 ili greška pri POST /api/bookings → `user-service` ne radi ili je preopterećen; pokušaj ponovo kada se `user-service` oporavi
- Port zauzet → promeni `server.port` u odgovarajućem `application.properties`
- IntelliJ pokreće pogrešnu klasu → koristi main klase po modulima:
  - `service-discovery/.../ServiceDiscoveryApplication.java`
  - `api-gateway/.../ApiGatewayApplication.java`
  - `user-service/.../UserServiceApplication.java`
  - `bookings-service/.../BookingsServiceApplication.java`

---

## Sledeći koraci (opciono)
- Spring Cloud Config Server (centralizovana konfiguracija)
- RabbitMQ/Kafka (emitovanje događaja, npr. "BookingCreated")
- Docker Compose (podizanje svih servisa + Eureka + Gateway + broker)
- Naprednija autentikacija (JWT/OAuth2) na gateway-u
- WebSocket notifikacije statusa rezervacije


