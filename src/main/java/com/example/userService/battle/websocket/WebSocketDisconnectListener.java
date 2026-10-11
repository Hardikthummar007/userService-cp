package com.example.userService.battle.websocket;

import com.example.userService.battle.service.MatchmakingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Listens for WebSocket disconnection events to clean up queue state
 * and start forfeit grace period (in Phase 5).
 */
@Component
public class WebSocketDisconnectListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketDisconnectListener.class);

    @Autowired
    private MatchmakingService matchmakingService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            String username = user.getName();
            logger.info("WebSocket disconnected for user: '{}'. Cleaning up matchmaking queue.", username);
            matchmakingService.onDisconnect(username);
        }
    }
}
