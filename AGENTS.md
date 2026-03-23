# AGENTS.md - Lastmile Delivery System

This document provides guidelines for AI agents working on the Lastmile codebase.

## Project Overview

Lastmile is a delivery management system with three modules:
- **lastmile-system**: Java 21 / Spring Boot 3.5 backend (Hexagonal Architecture)
- **lastmile-dashboard**: TypeScript / React 19 / Vite admin dashboard
- **lastmile-mobile**: TypeScript / React Native / Expo courier mobile app

---

## Build, Lint, and Test Commands

### Backend (lastmile-system)

```bash
# Navigate to backend
cd lastmile-system

# Build (skip tests)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=LastmileSystemApplicationTests

# Run a single test method
./mvnw test -Dtest=LastmileSystemApplicationTests#contextLoads

# Run with pattern matching
./mvnw test -Dtest="*OrderTest"

# Start development server
./mvnw spring-boot:run

# Start infrastructure (PostgreSQL + Kafka)
docker-compose up -d
```

### Dashboard (lastmile-dashboard)

```bash
cd lastmile-dashboard

# Install dependencies
npm install

# Start dev server
npm run dev

# Build for production
npm run build

# Lint
npm run lint
```

### Mobile (lastmile-mobile)

```bash
cd lastmile-mobile

# Install dependencies
npm install

# Start Expo dev server
npm start

# Run on Android
npm run android

# Run on iOS
npm run ios
```

---

## Code Style Guidelines

### Java (Backend)

**Architecture**: Hexagonal (Ports & Adapters)
- `domain/model/` - Immutable domain entities with Lombok @Getter, @Builder, @With
- `domain/port/in/` - Input ports (use case interfaces)
- `domain/port/out/` - Output ports (repository interfaces)
- `domain/service/` - Domain services with business logic
- `domain/exception/` - Domain exceptions extending DomainException
- `application/usecase/` - Use case implementations
- `application/usecase/dto/` - DTOs for use case layer
- `infrastructure/adapter/in/rest/` - REST controllers
- `infrastructure/adapter/out/persistence/` - JPA repositories
- `infrastructure/config/` - Spring configuration

**Naming**:
- Use cases: `ManageOrdersUseCase` (interface), `ManageOrdersUseCaseImpl` (impl)
- Repositories: `OrderRepository` (port), `OrderRepositoryImpl` (adapter)
- Controllers: `OrderController` with `/api/v1/` prefix
- Mappers: `OrderDomainMapper`, `OrderRestMapper` (use MapStruct)
- Exceptions: `OrderNotFoundException`, `CourierNotAvailableException`

**Patterns**:
- Immutable domain models with Lombok @With for state changes
- Constructor injection via @RequiredArgsConstructor
- Logging with @Slf4j
- Validation with Jakarta @Valid
- Security with @PreAuthorize
- API documentation with OpenAPI/Swagger annotations

**Exception Handling**:
- Domain exceptions extend `DomainException`
- Use GlobalExceptionHandler for consistent API responses
- Return ApiResponse<T> wrapper for all endpoints

**Example Domain Entity**:
```java
@Getter
@Builder
@With
public class Order {
    private final UUID id;
    private final String trackingCode;
    private final OrderStatus status;
    
    public Order markAsDelivered() {
        return this.withStatus(OrderStatus.DELIVERED);
    }
}
```

### TypeScript (Dashboard & Mobile)

**Imports**: Group in order:
1. React/framework imports
2. Third-party libraries
3. Local components/hooks
4. Types

**Types**:
- Define interfaces in `src/types/index.ts`
- Use explicit return types for API functions
- Prefer `type` for unions, `interface` for objects

**Components**:
- Default export for pages/screens: `export default function OrdersPage()`
- Named exports for utilities and hooks
- Use functional components exclusively

**State Management**:
- Dashboard: Zustand stores in `src/store/`
- Mobile: React Context in `src/context/`
- Custom hooks in `src/hooks/` for complex logic

**API Layer**:
- Centralized API client (`axiosConfig.ts` / `apiClient.ts`)
- API modules per domain: `ordersApi.ts`, `routesApi.ts`
- Always wrap responses in `ApiResponse<T>`

**Naming**:
- Pages: `OrdersPage.tsx`, `DashboardPage.tsx`
- Screens (mobile): `RouteScreen.tsx`, `LoginScreen.tsx`
- Hooks: `useRoute.ts`, `useLogin.ts`
- API: `ordersApi`, `routesApi`

**Example API Module**:
```typescript
export const ordersApi = {
  getOrders: async (params?: { status?: string }) => {
    const response = await api.get<ApiResponse<Order[]>>('/orders', { params })
    return response.data
  },
}
```

**Example Hook**:
```typescript
export function useRoute(onSelectStop: (stop: Stop) => void) {
  const [route, setRoute] = useState<Route | null>(null)
  const [loading, setLoading] = useState(true)
  // ...
  return { route, loading, ... }
}
```

---

## Error Handling

### Backend
- Throw domain-specific exceptions: `throw new OrderNotFoundException(id)`
- GlobalExceptionHandler maps exceptions to HTTP status codes:
  - `*NotFoundException` -> 404
  - `Duplicate*Exception` -> 409
  - Business rule violations -> 422
  - Validation errors -> 400

### Frontend
- Wrap async calls in try/catch
- Use `message.error()` (Ant Design) for user feedback
- Silent catch for non-critical failures

---

## Testing

### Backend Tests
- Use JUnit 5 with Spring Boot Test
- Test naming: `ClassName` + `Tests` suffix
- Integration tests with @SpringBootTest
- Mock external services in tests

### Running Single Tests
```bash
# Single class
./mvnw test -Dtest=OrderServiceTests

# Single method
./mvnw test -Dtest=OrderServiceTests#shouldCreateOrder

# Pattern
./mvnw test -Dtest="*Controller*"
```

---

## Key Dependencies

**Backend**:
- Spring Boot 3.5, Spring Security, Spring Data JPA
- PostgreSQL, Flyway migrations
- Kafka for async messaging
- Lombok, MapStruct for boilerplate reduction
- JWT (jjwt) for authentication

**Dashboard**:
- React 19, Vite 7, TypeScript 5.9
- Ant Design 6 (UI components)
- Zustand (state), Axios (HTTP), dayjs (dates)
- React Router DOM 7

**Mobile**:
- Expo 54, React Native 0.81
- React Navigation 7
- Expo Camera, Image Picker

---

## Infrastructure

```bash
# Start local services
cd lastmile-system
docker-compose up -d

# Services:
# - PostgreSQL: localhost:5432 (lastmile_db/lastmile_user/lastmile_pass)
# - Kafka: localhost:9092
```

---

## Common Patterns

1. **Domain Events**: Use Kafka for async order status notifications
2. **File Upload**: Multipart for Excel/CSV bulk order import
3. **Proof of Delivery**: Photo upload via mobile app
4. **Role-Based Access**: ADMIN, DISPATCHER, COURIER roles
5. **Geocoding**: External port for address-to-coordinates conversion
