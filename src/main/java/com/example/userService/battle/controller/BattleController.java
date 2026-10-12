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

    /**
     * Retrieves active battle details (problem, opponent, timer, score) for page reload / sync.
     */
    @GetMapping("/{battleId}")
    public ResponseEntity<?> getBattleDetails(
            @PathVariable Long battleId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

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
            java.util.Optional<com.example.userService.battle.model.Battle> battleOpt = battleService.getBattleById(battleId);
            if (battleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Battle not found"));
            }
            com.example.userService.battle.model.Battle battle = battleOpt.get();
            if (!username.equals(battle.getPlayer1()) && !username.equals(battle.getPlayer2())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("status", "error", "message", "You are not a participant in this battle"));
            }

            String opponent = username.equals(battle.getPlayer1()) ? battle.getPlayer2() : battle.getPlayer1();
            com.example.userService.dto.problems problem = battleService.fetchProblem(battle.getProblemId());
            com.example.userService.battle.dto.BattleProblemDto problemDto = 
                    com.example.userService.battle.dto.BattleProblemDto.fromProblem(problem);

            long startMs = battle.getStartTime() != null ? battle.getStartTime().toEpochMilli() : System.currentTimeMillis();
            long endMs = battle.getEndTime() != null ? battle.getEndTime().toEpochMilli() : (startMs + 20 * 60 * 1000);

            int myPassed = username.equals(battle.getPlayer1()) ? battle.getP1BestPassed() : battle.getP2BestPassed();
            int oppPassed = username.equals(battle.getPlayer1()) ? battle.getP2BestPassed() : battle.getP1BestPassed();

            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("battleId", battle.getId());
            resp.put("status", battle.getStatus() != null ? battle.getStatus().name() : "ACTIVE");
            resp.put("opponent", opponent);
            resp.put("problem", problemDto);
            resp.put("serverNow", System.currentTimeMillis());
            resp.put("startTime", startMs);
            resp.put("endTime", endMs);
            resp.put("totalTests", battle.getTotalTests() != null ? battle.getTotalTests() : 0);
            resp.put("myPassed", myPassed);
            resp.put("opponentPassed", oppPassed);
            resp.put("winner", battle.getWinner() != null ? battle.getWinner() : "");
            resp.put("endReason", battle.getEndReason() != null ? battle.getEndReason().name() : "");

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("Error retrieving battle #{}: {}", battleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * Endpoint for user surrender / forfeit / quit during active battle:
     * POST /userService/battle/{battleId}/forfeit
     */
    @PostMapping("/{battleId}/forfeit")
    public ResponseEntity<?> forfeitBattle(
            @PathVariable Long battleId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

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
            boolean success = battleService.forfeitBattle(battleId, username);
            if (success) {
                return ResponseEntity.ok(Map.of("status", "success", "message", "Battle forfeited successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "error", "message", "Battle is not active or already finished"));
            }
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing forfeit for battle #{}: {}", battleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

}
