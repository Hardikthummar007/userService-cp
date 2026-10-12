package com.example.userService.battle.service;

import com.example.userService.battle.dto.BattleProblemDto;
import com.example.userService.battle.dto.BattleStartDto;
import com.example.userService.battle.dto.MatchFoundDto;
import com.example.userService.battle.model.Battle;
import com.example.userService.battle.model.BattleEndReason;
import com.example.userService.battle.model.BattleStatus;
import com.example.userService.battle.repo.BattleRepository;
import com.example.userService.dto.problems;
import com.example.userService.entity.user;
import com.example.userService.feign.problemClient;
import com.example.userService.repo.userRepo;
import com.example.userService.utility.utility;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.example.userService.battle.dto.BattleEndDto;
import com.example.userService.battle.dto.BattleSubmissionRequest;
import com.example.userService.battle.dto.OpponentProgressDto;
import org.springframework.context.annotation.Lazy;

/**
 * Core business service managing 1v1 battle lifecycle:
 * - Match creation and unsolved problem selection
 * - 3-second countdown to BATTLE_START
 * - Sanitized problem payload broadcast
 * - Timeout scheduling
 */
@Service
public class BattleService {

    private static final Logger logger = LoggerFactory.getLogger(BattleService.class);

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private userRepo userRepo;

    @Autowired
    private problemClient problemClient;

    @Autowired
    private utility utility;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ThreadPoolTaskScheduler battleTaskScheduler;

    @Autowired
    private BattleJudgeService battleJudgeService;

    @Autowired
    @Lazy
    private MatchmakingService matchmakingService;

    @Value("${battle.duration-minutes:20}")
    private int durationMinutes;

    @Value("${battle.start-delay-seconds:3}")
    private int startDelaySeconds;

    @Value("${battle.allowed-difficulties:EASY,MEDIUM}")
    private String allowedDifficultiesStr;

    // Cache to quickly look up participants for subscription authorization
    private final Map<Long, Set<String>> battleParticipantsCache = new ConcurrentHashMap<>();

    // 5-second rate limiting map per player per battle: key = "battleId:username"
    private final Map<String, Instant> playerLastSubmitTime = new ConcurrentHashMap<>();

    /**
     * Creates a new Battle in WAITING status, notifies both players,
     * and schedules the 3-second start timer.
     */
    @org.springframework.transaction.annotation.Transactional
    public Battle createBattle(String player1, String player2) {
        logger.info("Creating battle between '{}' and '{}'", player1, player2);

        // 0. Ensure players exist in user table to satisfy foreign key constraints
        ensureUserExists(player1);
        ensureUserExists(player2);

        // 1. Select a problem matching allowed difficulties that neither player has solved
        problems chosenProblem = selectProblemForPlayers(player1, player2);
        int problemId = chosenProblem != null ? chosenProblem.getProblem_id() : 18; // Fallback problem
        int totalTests = (chosenProblem != null && chosenProblem.getTestCases() != null)
                ? chosenProblem.getTestCases().size() : 0;

        // 2. Persist Battle in WAITING status
        Battle battle = new Battle(player1, player2, problemId);
        battle.setTotalTests(totalTests);
        battle = battleRepository.save(battle);

        Long battleId = battle.getId();
        battleParticipantsCache.put(battleId, Set.of(player1, player2));

        // 3. Notify both players with MATCH_FOUND privately
        notifyMatchFound(player1, battleId, player2);
        notifyMatchFound(player2, battleId, player1);

        // 4. Schedule transition to ACTIVE after startDelaySeconds (3s)
        final Long scheduledBattleId = battleId;
        battleTaskScheduler.schedule(
                () -> activateBattle(scheduledBattleId),
                Instant.now().plusSeconds(startDelaySeconds)
        );

        logger.info("Battle #{} created in WAITING state. Start countdown scheduled in {}s.", battleId, startDelaySeconds);
        return battle;
    }

    /**
     * Transitions battle from WAITING to ACTIVE, sets endTime, and broadcasts BATTLE_START.
     */
    public void activateBattle(Long battleId) {
        Optional<Battle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isEmpty()) {
            logger.warn("Cannot activate battle #{}: not found in database", battleId);
            return;
        }

        Battle battle = battleOpt.get();
        if (battle.getStatus() != BattleStatus.WAITING) {
            logger.info("Battle #{} is already in state {}, skipping activation", battleId, battle.getStatus());
            return;
        }

        Instant now = Instant.now();
        Instant endTime = now.plus(Duration.ofMinutes(durationMinutes));

        battle.setStatus(BattleStatus.ACTIVE);
        battle.setStartTime(now);
        battle.setEndTime(endTime);
        battleRepository.save(battle);

        logger.info("Battle #{} is now ACTIVE! End time: {}", battleId, endTime);

        // Fetch problem details and strip hidden test cases
        problems problem = fetchProblem(battle.getProblemId());
        BattleProblemDto safeProblemDto = BattleProblemDto.fromProblem(problem);

        // Broadcast BATTLE_START to /topic/battle/{battleId}
        BattleStartDto startPayload = new BattleStartDto(
                battleId,
                safeProblemDto,
                endTime.toEpochMilli(),
                now.toEpochMilli()
        );

        messagingTemplate.convertAndSend("/topic/battle/" + battleId, startPayload);
        logger.info("Broadcasted BATTLE_START to /topic/battle/{}", battleId);

        // Schedule timeout at endTime
        scheduleTimeout(battleId, endTime);
    }

    /**
     * Schedules timeout timer at endTime.
     */
    public void scheduleTimeout(Long battleId, Instant endTime) {
        battleTaskScheduler.schedule(() -> onBattleTimeout(battleId), endTime);
    }

    /**
     * Submits code for a battle:
     * - Validates battle is ACTIVE and not expired
     * - Validates user is a participant
     * - Checks 5-second rate limit
     * - Runs isolated judging without side effects
     * - Updates player's best passed count in DB
     * - Notifies opponent privately of test case progress
     * - Atomically declares winner if all test cases passed
     */
    public Map<String, Object> submitCode(Long battleId, String username, BattleSubmissionRequest request) {
        Optional<Battle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isEmpty()) {
            throw new NoSuchElementException("Battle #" + battleId + " not found");
        }

        Battle battle = battleOpt.get();

        if (battle.getStatus() != BattleStatus.ACTIVE) {
            throw new IllegalStateException("Battle #" + battleId + " is not ACTIVE (status: " + battle.getStatus() + ")");
        }

        Instant now = Instant.now();
        if (battle.getEndTime() != null && now.isAfter(battle.getEndTime())) {
            throw new IllegalStateException("Battle #" + battleId + " duration has expired");
        }

        if (!username.equals(battle.getPlayer1()) && !username.equals(battle.getPlayer2())) {
            throw new SecurityException("User '" + username + "' is not a participant of battle #" + battleId);
        }

        // 5-second rate limit check per player per battle
        String rateLimitKey = battleId + ":" + username;
        Instant lastSubmit = playerLastSubmitTime.get(rateLimitKey);
        if (lastSubmit != null && Duration.between(lastSubmit, now).toSeconds() < 5) {
            long waitSec = 5 - Duration.between(lastSubmit, now).toSeconds();
            throw new IllegalStateException("Rate limit: please wait " + waitSec + "s before submitting again");
        }
        playerLastSubmitTime.put(rateLimitKey, now);

        if (request == null || request.getCode() == null) {
            throw new IllegalArgumentException("Submission code cannot be empty");
        }

        // 1. Run isolated judge (never updates user profile or normal submissions)
        Map<String, Object> judgeResult = battleJudgeService.judgeCodeWithoutSideEffects(
                battle.getProblemId(),
                request.getCode()
        );

        int totalPassed = 0;
        if (judgeResult.get("totalTestCasePassed") instanceof Number) {
            totalPassed = ((Number) judgeResult.get("totalTestCasePassed")).intValue();
        }

        int totalTests = battle.getTotalTests() != null ? battle.getTotalTests() : 0;
        if (totalTests == 0 && judgeResult.get("totalTestCase") instanceof Number) {
            totalTests = ((Number) judgeResult.get("totalTestCase")).intValue();
            battle.setTotalTests(totalTests);
        }

        // 2. Update player's best score in DB if improved
        boolean isP1 = username.equals(battle.getPlayer1());
        if (isP1) {
            if (totalPassed > battle.getP1BestPassed()) {
                battle.setP1BestPassed(totalPassed);
                battle.setP1BestAt(now);
            }
        } else {
            if (totalPassed > battle.getP2BestPassed()) {
                battle.setP2BestPassed(totalPassed);
                battle.setP2BestAt(now);
            }
        }
        battleRepository.save(battle);

        // 3. Notify opponent privately with test progress (NO code details)
        String opponent = isP1 ? battle.getPlayer2() : battle.getPlayer1();
        try {
            OpponentProgressDto progressDto = new OpponentProgressDto(totalPassed, totalTests);
            messagingTemplate.convertAndSendToUser(opponent, "/queue/opponent", progressDto);
            logger.info("Sent OPPONENT_SUBMITTED (passed={}/{}) to '{}' for battle #{}",
                    totalPassed, totalTests, opponent, battleId);
        } catch (Exception e) {
            logger.error("Failed to send opponent progress to '{}': {}", opponent, e.getMessage());
        }

        // 4. Check Win Condition: if all test cases passed
        boolean passedAll = Boolean.TRUE.equals(judgeResult.get("passedOrNot"));
        if (passedAll && totalTests > 0) {
            int updated = battleRepository.finishBattleAtomic(battleId, username, BattleEndReason.SOLVED);
            if (updated == 1) {
                logger.info("🏆 Battle #{} solved by '{}'! Declaring official winner.", battleId, username);
                broadcastBattleEnd(battleId, username, BattleEndReason.SOLVED);
                cleanupBattlePlayers(battle.getPlayer1(), battle.getPlayer2());
            }
        }

        return judgeResult;
    }

    /**
     * Called when the 20-minute battle timer expires.
     */
    public void onBattleTimeout(Long battleId) {
        logger.info("Battle timer expired for battle #{}", battleId);

        Optional<Battle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isEmpty()) {
            return;
        }

        Battle battle = battleOpt.get();
        if (battle.getStatus() != BattleStatus.ACTIVE) {
            logger.info("Battle #{} is already finished (status: {}). Ignoring timeout.", battleId, battle.getStatus());
            return;
        }

        // Determine winner based on best passed count, then earlier submission time
        int p1Passed = battle.getP1BestPassed();
        int p2Passed = battle.getP2BestPassed();
        String winner = null;

        if (p1Passed > p2Passed) {
            winner = battle.getPlayer1();
        } else if (p2Passed > p1Passed) {
            winner = battle.getPlayer2();
        } else if (p1Passed > 0) {
            // Tie breaker: whoever achieved their best passed score first
            if (battle.getP1BestAt() != null && battle.getP2BestAt() != null) {
                if (battle.getP1BestAt().isBefore(battle.getP2BestAt())) {
                    winner = battle.getPlayer1();
                } else if (battle.getP2BestAt().isBefore(battle.getP1BestAt())) {
                    winner = battle.getPlayer2();
                }
            } else if (battle.getP1BestAt() != null) {
                winner = battle.getPlayer1();
            } else if (battle.getP2BestAt() != null) {
                winner = battle.getPlayer2();
            }
        }

        int updated = battleRepository.finishBattleAtomic(battleId, winner, BattleEndReason.TIMEOUT);
        if (updated == 1) {
            logger.info("⏰ Battle #{} ended by TIMEOUT. Winner: '{}'", battleId, winner);
            broadcastBattleEnd(battleId, winner, BattleEndReason.TIMEOUT);
            cleanupBattlePlayers(battle.getPlayer1(), battle.getPlayer2());
        }
    }

    private void broadcastBattleEnd(Long battleId, String winner, BattleEndReason reason) {
        BattleEndDto payload = new BattleEndDto(battleId, winner, reason.name());
        messagingTemplate.convertAndSend("/topic/battle/" + battleId, payload);
        logger.info("Broadcasted BATTLE_END for battle #{} (winner: {}, reason: {})", battleId, winner, reason);
    }

    private void cleanupBattlePlayers(String player1, String player2) {
        matchmakingService.removeActiveBattlePlayer(player1);
        matchmakingService.removeActiveBattlePlayer(player2);
    }

    /**
     * Handles user voluntary surrender / forfeit.
     * Declares the opponent the winner with reason FORFEIT.
     */
    public boolean forfeitBattle(Long battleId, String username) {
        Optional<Battle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isEmpty()) {
            throw new NoSuchElementException("Battle #" + battleId + " not found");
        }

        Battle battle = battleOpt.get();
        if (battle.getStatus() != BattleStatus.ACTIVE) {
            logger.info("Battle #{} is in status {}, cannot forfeit", battleId, battle.getStatus());
            return false;
        }

        if (!username.equals(battle.getPlayer1()) && !username.equals(battle.getPlayer2())) {
            throw new SecurityException("User '" + username + "' is not a participant of battle #" + battleId);
        }

        String winner = username.equals(battle.getPlayer1()) ? battle.getPlayer2() : battle.getPlayer1();
        int updated = battleRepository.finishBattleAtomic(battleId, winner, BattleEndReason.FORFEIT);
        if (updated == 1) {
            logger.info("🏳️ Battle #{} forfeited by '{}'. Winner: '{}'", battleId, username, winner);
            broadcastBattleEnd(battleId, winner, BattleEndReason.FORFEIT);
            cleanupBattlePlayers(battle.getPlayer1(), battle.getPlayer2());
            return true;
        }
        return false;
    }

    /**
     * Picks an unsolved problem matching difficulty configuration.
     */
    public problems selectProblemForPlayers(String player1, String player2) {
        Set<Integer> allowedDifficulties = parseAllowedDifficulties(allowedDifficultiesStr);

        // 1. Gather all problems from problemService
        List<problems> allProblems = fetchAllProblems();
        if (allProblems.isEmpty()) {
            logger.warn("No problems returned from problemService. Using fallback problem.");
            return null;
        }

        // 2. Filter by allowed difficulties
        List<problems> difficultyFiltered = allProblems.stream()
                .filter(p -> allowedDifficulties.contains(p.getDifficulty()))
                .collect(Collectors.toList());

        if (difficultyFiltered.isEmpty()) {
            difficultyFiltered = allProblems;
        }

        // 3. Find problem solved IDs of both players
        Set<Integer> solvedIds = new HashSet<>();
        userRepo.findById(player1).ifPresent(u -> {
            if (u.getProblemSolvedId() != null) solvedIds.addAll(u.getProblemSolvedId());
        });
        userRepo.findById(player2).ifPresent(u -> {
            if (u.getProblemSolvedId() != null) solvedIds.addAll(u.getProblemSolvedId());
        });

        // 4. Prefer problems that NEITHER player has solved
        List<problems> neitherSolved = difficultyFiltered.stream()
                .filter(p -> !solvedIds.contains(p.getProblem_id()))
                .collect(Collectors.toList());

        List<problems> candidates = !neitherSolved.isEmpty() ? neitherSolved : difficultyFiltered;

        // Pick a random problem
        Random random = new Random();
        return candidates.get(random.nextInt(candidates.size()));
    }

    private List<problems> fetchAllProblems() {
        try {
            return utility.convertResponse(
                    problemClient.getAllProblem(),
                    new TypeReference<List<problems>>() {}
            );
        } catch (Exception e) {
            logger.warn("Could not fetch problems from problemService: {}. Using fallback problem 18.", e.getMessage());
            return Collections.emptyList();
        }
    }

    public problems fetchProblem(int problemId) {
        try {
            return utility.convertResponse(
                    problemClient.get(problemId),
                    new TypeReference<problems>() {}
            );
        } catch (Exception e) {
            logger.error("Failed to fetch problem {}: {}", problemId, e.getMessage());
            problems fallback = new problems();
            fallback.setProblem_id(problemId);
            fallback.setTittle("Two Sum");
            fallback.setDescription("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.");
            fallback.setDifficulty(1);
            return fallback;
        }
    }

    private Set<Integer> parseAllowedDifficulties(String diffConfig) {
        Set<Integer> set = new HashSet<>();
        if (diffConfig == null || diffConfig.isBlank()) {
            set.add(1);
            set.add(2);
            return set;
        }
        for (String part : diffConfig.split(",")) {
            String trimmed = part.trim().toUpperCase();
            if (trimmed.equals("EASY") || trimmed.equals("1")) {
                set.add(1);
            } else if (trimmed.equals("MEDIUM") || trimmed.equals("2")) {
                set.add(2);
            } else if (trimmed.equals("HARD") || trimmed.equals("3")) {
                set.add(3);
            }
        }
        return set.isEmpty() ? Set.of(1, 2) : set;
    }

    private void notifyMatchFound(String player, Long battleId, String opponent) {
        MatchFoundDto payload = new MatchFoundDto(battleId, opponent);
        try {
            messagingTemplate.convertAndSendToUser(player, "/queue/match", payload);
            logger.info("Sent MATCH_FOUND to '{}' for battle #{}", player, battleId);
        } catch (Exception e) {
            logger.error("Failed to send MATCH_FOUND to '{}': {}", player, e.getMessage());
        }
    }

    /**
     * Checks if a user is one of the two players in a battle.
     * Used to authorize SUBSCRIBE to /topic/battle/{battleId}.
     */
    public boolean isParticipant(Long battleId, String username) {
        Set<String> cached = battleParticipantsCache.get(battleId);
        if (cached != null) {
            return cached.contains(username);
        }

        Optional<Battle> battle = battleRepository.findById(battleId);
        if (battle.isPresent()) {
            Set<String> participants = Set.of(battle.get().getPlayer1(), battle.get().getPlayer2());
            battleParticipantsCache.put(battleId, participants);
            return participants.contains(username);
        }
        return false;
    }

    public Optional<Battle> getBattleById(Long battleId) {
        return battleRepository.findById(battleId);
    }

    private void ensureUserExists(String username) {
        if (username != null && !userRepo.existsById(username)) {
            com.example.userService.entity.user u = new com.example.userService.entity.user();
            u.setUserName(username);
            u.setRole("user");
            userRepo.save(u);
            logger.info("Auto-provisioned user '{}' in database for battle", username);
        }
    }
}
