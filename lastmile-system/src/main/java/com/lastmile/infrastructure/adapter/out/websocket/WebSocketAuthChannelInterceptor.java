package com.lastmile.infrastructure.adapter.out.websocket;

import com.lastmile.infrastructure.adapter.out.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Interceptor para validar JWT en conexiones WebSocket.
 * Extrae el token del header 'Authorization' o del query param 'token'.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    
    private final JwtService jwtService;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }
        
        // Solo validamos en CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            
            if (token != null && jwtService.isTokenValid(token)) {
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);
                String userId = jwtService.extractUserId(token);
                
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                
                // Guardar userId en los atributos de la sesión para uso posterior
                accessor.getSessionAttributes().put("userId", userId);
                accessor.getSessionAttributes().put("role", role);
                accessor.setUser(auth);
                
                log.trace("WebSocket authenticated: user={}, role={}", username, role);
            } else {
                log.warn("WebSocket connection rejected: invalid or missing token");
                // No lanzamos excepción para permitir conexiones anónimas a topics públicos
                // El frontend debe manejar el caso de no estar autenticado
            }
        }
        
        return message;
    }
    
    /**
     * Extrae el token JWT del header Authorization o del query param.
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // Intentar obtener del header Authorization
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Intentar obtener del query param 'token' (útil para SockJS)
        String tokenParam = accessor.getFirstNativeHeader("token");
        if (tokenParam != null) {
            return tokenParam;
        }
        
        return null;
    }
}
