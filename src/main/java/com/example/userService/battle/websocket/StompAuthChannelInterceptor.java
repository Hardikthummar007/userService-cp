package com.example.userService.battle.websocket;

import com.example.userService.battle.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Intercepts STOMP messages on the inbound channel to:
 * 1. Authenticate users on STOMP CONNECT using "Authorization: Bearer <token>"
 * 2. Set the custom StompPrincipal (principal.getName() == userName)
 * 3. Enforce authentication on battle actions (/app/battle/**)
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // 1. Authenticate on CONNECT frame
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null) {
                authHeader = accessor.getFirstNativeHeader("authorization");
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    if (username != null && !username.isBlank()) {
                        accessor.setUser(new StompPrincipal(username));
                        logger.info("STOMP CONNECT: User '{}' successfully authenticated via JWT", username);
                    } else {
                        logger.warn("STOMP CONNECT: JWT token valid but username subject is missing");
                        throw new IllegalArgumentException("Invalid JWT: missing username subject");
                    }
                } else {
                    logger.warn("STOMP CONNECT: Rejected due to invalid or expired JWT token");
                    throw new IllegalArgumentException("Invalid or expired JWT token");
                }
            } else {
                logger.info("STOMP CONNECT: Connected without Bearer token (anonymous session)");
            }
        }

        // 2. Protect battle destinations on SEND (/app/battle/**)
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/app/battle")) {
                if (accessor.getUser() == null) {
                    logger.warn("STOMP SEND rejected: Unauthorized attempt to send to '{}'", destination);
                    throw new IllegalArgumentException("Authentication required for battle commands");
                }
            }
        }

        return message;
    }
}
