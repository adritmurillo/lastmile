package com.lastmile.infrastructure.config;

import com.lastmile.infrastructure.adapter.out.websocket.WebSocketAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket con STOMP.
 * 
 * Endpoints:
 * - /ws: Conexión WebSocket con SockJS fallback
 * 
 * Topics:
 * - /topic/dispatcher/close-requests: Solicitudes de cierre de ruta
 * - /topic/dispatcher/orders: Nuevas órdenes y actualizaciones
 * - /topic/courier/{courierId}: Notificaciones específicas para un courier
 * 
 * Autenticación:
 * - El cliente debe enviar el JWT en el header 'Authorization' al conectar
 * - Formato: Authorization: Bearer <token>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final WebSocketAuthChannelInterceptor authChannelInterceptor;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Topics a los que los clientes pueden suscribirse
        registry.enableSimpleBroker("/topic");
        // Prefijo para mensajes enviados desde el cliente al servidor
        registry.setApplicationDestinationPrefixes("/app");
    }

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
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Agregar interceptor para autenticación JWT
        registration.interceptors(authChannelInterceptor);
    }
}
