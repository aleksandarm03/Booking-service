## PDS Booking Microservices

### Moduli
- `service-discovery` (Eureka Server)
- `api-gateway` (Spring Cloud Gateway + API key)
- `user-service` (CRUD + H2)
- `bookings-service` (CRUD + Feign + Resilience4j + agregacija)

### Pokretanje (JDK 17, Maven 3.9+)
1) `service-discovery` na 8761
2) `user-service` na 8081
3) `bookings-service` na 8082
4) `api-gateway` na 8080

Eureka: http://localhost:8761

### Gateway
- API key header: `X-API-KEY: dev-key`
- Rute:
  - `/api/users/**` → user-service
  - `/api/bookings/**` → bookings-service

### H2
- `user-service`: JDBC `jdbc:h2:mem:usersdb`
- `bookings-service`: JDBC `jdbc:h2:mem:bookingsdb`
- Konzola: `/h2-console`

### Endpoints
- Users: `GET/POST/PUT/DELETE /api/users` (+ `/{id}`)
- Bookings: `GET/POST/PUT/DELETE /api/bookings` (+ `/{id}`)
- Agregacija: `GET /api/bookings/{id}/details`

### Resilience4j/Feign (bookings-service)
Podešeno: timeouts, retry (3x), circuit breaker nad Feign pozivom ka user-service.

