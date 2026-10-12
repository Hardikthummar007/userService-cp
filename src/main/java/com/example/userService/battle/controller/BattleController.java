package com.example.userService.battle.controller;

import com.example.userService.battle.dto.BattleSubmissionRequest;
import com.example.userService.battle.service.BattleService;
import com.example.userService.battle.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for battle actions (code submission).
 * Endpoint: POST /userService/battle/{battleId}/submit
 */
@RestController
@RequestMapping("userService/battle")
public class BattleController {

    private static final Logger logger = LoggerFactory.getLogger(BattleController.class);

    @Autowired
    private BattleService battleService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Submits code for judging in an active battle.
     * Authenticated via JWT Bearer token in Authorization header.
     */
    @PostMapping("/{battleId}/submit")
    public ResponseEntity<?> submitCode(
            @PathVariable Long battleId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody BattleSubmissionRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Invalid or expired JWT token"));
        }

        String username = jwtUtil.extractUsername(token);

        try {
            Map<String, Object> judgeResult = battleService.submitCode(battleId, username, request);
            return ResponseEntity.ok(judgeResult);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", e.getMessage()));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", e.getMessage()));

        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Rate limit")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("status", "error", "message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error judging battle submission #{}: {}", battleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Internal server error: " + e.getMessage()));
        }
    }
}
