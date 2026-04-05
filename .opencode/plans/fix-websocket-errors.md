# Plan: Fix WebSocket Errors in Dashboard and Mobile

## Problem Summary

1. **Dashboard**: Blank screen with `Uncaught ReferenceError: global is not defined` in `sockjs-client.js`
2. **Mobile**: WebSocket connection failing with infinite reconnection loop (logs incrementing every 5 seconds)

## Root Cause Analysis

### Dashboard Issue
- `sockjs-client` library is designed for Node.js and uses the `global` variable
- Vite doesn't polyfill Node.js globals like Webpack does
- Solution: Add `global: 'window'` define in Vite config

### Mobile Issue
- Backend WebSocket is configured with SockJS fallback: `.withSockJS()`
- Mobile is using native WebSocket to connect to `ws://192.168.18.6:8080/ws`
- SockJS wraps the endpoint and expects a different protocol - it doesn't accept direct WebSocket connections
- Solution: Add a second endpoint `/ws-native` without SockJS for native clients

## Changes Required

### 1. Dashboard: `lastmile-dashboard/vite.config.ts`

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // Polyfill for sockjs-client which expects Node.js 'global' variable
    global: 'window',
  },
})
```

### 2. Backend: `lastmile-system/src/main/java/com/lastmile/infrastructure/config/WebSocketConfig.java`

Update `registerStompEndpoints` method:

```java
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Endpoint with SockJS for web browsers (dashboard)
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    
    // Endpoint without SockJS for native clients (mobile app)
    registry.addEndpoint("/ws-native")
            .setAllowedOriginPatterns("*");
}
```

### 3. Mobile: `lastmile-mobile/src/context/NotificationContext.tsx`

Update WebSocket URL (line 10):

```typescript
// Old:
const WS_URL = `ws://${API_HOST}/ws`

// New:
const WS_URL = `ws://${API_HOST}/ws-native`
```

## Testing Steps

1. **Dashboard**:
   - Stop `npm run dev`
   - Run `npm run dev` again
   - Navigate to http://localhost:5173
   - Verify no blank screen, dashboard loads normally
   - Check console for WebSocket connection message

2. **Mobile**:
   - Reload the app in Expo Go
   - Check logs - should see `[WebSocket] Connected` instead of errors
   - Verify green connection indicator appears in RouteScreen

3. **Backend**:
   - Restart Spring Boot
   - Should see WebSocket stats in logs without errors

## Notes

- The "Route not found" warning in backend logs is expected behavior when a courier doesn't have a route assigned for today
- Mobile logs about `expo-notifications` deprecation are informational warnings from Expo SDK 53, not errors
- `SafeAreaView` deprecation warning can be addressed later by migrating to `react-native-safe-area-context`
