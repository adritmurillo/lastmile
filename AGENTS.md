# AGENTS.md - Lastmile Delivery System

Guidelines for AI agents working on this codebase.

## Project Structure

- **lastmile-system**: Java 21 / Spring Boot 3.5 backend (Hexagonal Architecture)
- **lastmile-dashboard**: TypeScript / React 19 / Vite admin dashboard
- **lastmile-mobile**: TypeScript / React Native / Expo courier app

## Build, Lint, and Test Commands

### Backend (lastmile-system)

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run single test class
./mvnw test -Dtest=OrderServiceTests

# Run single test method
./mvnw test -Dtest=OrderServiceTests#shouldCreateOrder

# Run tests matching pattern
./mvnw test -Dtest="*Controller*"

# Start dev server (requires docker-compose up -d first)
./mvnw spring-boot:run

# Start infrastructure
docker-compose up -d
# PostgreSQL: localhost:5432 (lastmile_db/lastmile_user/lastmile_pass)
# Kafka: localhost:9092
```

### Dashboard (lastmile-dashboard)

```bash
npm install        # Install dependencies
npm run dev        # Start dev server
npm run build      # Build for production
npm run lint       # Run ESLint
```

### Mobile (lastmile-mobile)

```bash
npm install        # Install dependencies
npm start          # Start Expo dev server
npm run android    # Run on Android
npm run ios        # Run on iOS
```

## Code Style - Java Backend

### Hexagonal Architecture

```
domain/
  model/           # Immutable entities (@Getter, @Builder, @With)
  port/in/         # Input ports (use case interfaces)
  port/out/        # Output ports (repository interfaces)
  service/         # Domain services
  exception/       # Domain exceptions extending DomainException
application/
  usecase/         # Use case implementations
  usecase/dto/     # Application DTOs
infrastructure/
  adapter/in/rest/ # REST controllers
  adapter/out/persistence/ # JPA repositories
  config/          # Spring configuration
```

### Naming Conventions

- Use cases: `ManageOrdersUseCase` (interface), `ManageOrdersUseCaseImpl` (impl)
- Repositories: `OrderRepository` (port), `OrderRepositoryImpl` (adapter)
- Controllers: `OrderController` with `/api/v1/` prefix
- Mappers: `OrderDomainMapper`, `OrderRestMapper`, `OrderPersistenceMapper`
- Exceptions: `OrderNotFoundException`, `CourierNotAvailableException`
- Tests: `OrderServiceTests` (class), `shouldCreateOrder` (method)

### Patterns

```java
// Domain entity - immutable with @With for state changes
@Getter @Builder @With
public class Order {
    private final UUID id;
    private final OrderStatus status;
    
    public Order markAsDelivered() {
        return this.withStatus(OrderStatus.DELIVERED);
    }
}

// Controller - constructor injection, validation, security
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final ManageOrdersUseCase manageOrdersUseCase;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders() { ... }
}
```

### Exception Handling

- Domain exceptions extend `DomainException`
- GlobalExceptionHandler maps to HTTP status:
  - `*NotFoundException` -> 404
  - `Duplicate*Exception` -> 409
  - Business violations (`*NotAvailableException`, `*NotModifiableException`) -> 422
  - Validation errors -> 400

## Code Style - TypeScript Frontend

### Import Order

1. React/framework imports
2. Third-party libraries
3. Local components/hooks/api
4. Types

### Types (src/types/index.ts)

```typescript
export interface Order {
  id: string
  trackingCode: string
  status: 'PENDING' | 'ASSIGNED' | 'IN_TRANSIT' | 'DELIVERED' | 'FAILED'
  // ...
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  error?: string
}
```

### API Modules

```typescript
// src/api/ordersApi.ts
export const ordersApi = {
  getOrders: async (params?: { status?: string }) => {
    const response = await api.get<ApiResponse<Order[]>>('/orders', { params })
    return response.data
  },
}
```

### Hooks (Mobile)

```typescript
// src/hooks/useRoute.ts
export function useRoute(onSelectStop: (stop: Stop) => void) {
  const [route, setRoute] = useState<Route | null>(null)
  const [loading, setLoading] = useState(true)
  // ...
  return { route, loading, ... }
}
```

### Naming

- Dashboard pages: `OrdersPage.tsx`, `DashboardPage.tsx`
- Mobile screens: `RouteScreen.tsx`, `LoginScreen.tsx`
- Hooks: `useRoute.ts`, `useLogin.ts`
- API modules: `ordersApi.ts`, `routesApi.ts`
- Stores (Zustand): `src/store/`
- Context (Mobile): `src/context/`

### Error Handling

```typescript
try {
  const data = await ordersApi.getOrders()
  // handle success
} catch {
  message.error('Failed to load orders')  // Ant Design (dashboard)
  Alert.alert('Error', 'Failed to load')  // React Native (mobile)
}
```

## Key Dependencies

**Backend**: Spring Boot 3.5, Spring Security, Spring Data JPA, PostgreSQL, Flyway, Kafka, Lombok, MapStruct, JWT (jjwt), OpenAPI/Swagger

**Dashboard**: React 19, Vite 7, TypeScript 5.9, Ant Design 6, Zustand, Axios, React Router 7

**Mobile**: Expo 54, React Native 0.81, React Navigation 7, Expo Camera/Image Picker

## Roles

- `ADMIN`: Full access
- `DISPATCHER`: Order and route management
- `COURIER`: Mobile app, route execution
