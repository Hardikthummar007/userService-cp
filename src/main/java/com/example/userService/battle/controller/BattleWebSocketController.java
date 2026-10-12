package com.example.userService.battle.controller;

import com.example.userService.battle.service.MatchmakingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP WebSocket Controller handling client messages under /app/battle/**
 */
@Controller
public class BattleWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(BattleWebSocketController.class);

    @Autowired
    private MatchmakingService matchmakingService;

    /**
     * User clicks "Find match" -> client sends STOMP frame to /app/battle/join
     */
    @MessageMapping("/battle/join")
    public void joinBattle(Principal principal) {
        if (principal == null) {
            logger.warn("Received /app/battle/join with unauthenticated Principal");
            return;
        }
        String username = principal.getName();
        logger.info("Received /app/battle/join from user: '{}'", username);
        matchmakingService.joinQueue(username);
    }

    @Autowired
    private com.example.userService.battle.service.BattleService battleService;

    /**
     * User cancels search -> client sends STOMP frame to /app/battle/leave
     */
    @MessageMapping("/battle/leave")
    public void leaveBattle(Principal principal) {
        if (principal == null) {
            return;
        }
        String username = principal.getName();
        logger.info("Received /app/battle/leave from user: '{}'", username);
        matchmakingService.leaveQueue(username);
    }

    /**
     * User surrenders / forfeits active battle -> sends STOMP frame to /app/battle/forfeit
     * Body: { "battleId": 18 }
     */
    @MessageMapping("/battle/forfeit")
    public void forfeitBattle(Principal principal, java.util.Map<String, Object> payload) {
        if (principal == null || payload == null || payload.get("battleId") == null) {
            return;
        }
        try {
            Long battleId = Long.valueOf(payload.get("battleId").toString());
            String username = principal.getName();
            logger.info("Received /app/battle/forfeit from user: '{}' for battle #{}", username, battleId);
            battleService.forfeitBattle(battleId, username);
        } catch (Exception e) {
            logger.error("Error processing STOMP forfeit: {}", e.getMessage());
        }
    }
}
