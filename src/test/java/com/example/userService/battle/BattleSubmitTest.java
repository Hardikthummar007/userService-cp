package com.example.userService.battle;

import com.example.userService.battle.dto.BattleSubmissionRequest;
import com.example.userService.battle.model.Battle;
import com.example.userService.battle.model.BattleEndReason;
import com.example.userService.battle.model.BattleStatus;
import com.example.userService.battle.repo.BattleRepository;
import com.example.userService.battle.service.BattleJudgeService;
import com.example.userService.battle.service.BattleService;
import com.example.userService.dto.codeDto;
import com.example.userService.entity.user;
import com.example.userService.repo.userRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest
public class BattleSubmitTest {

    @Autowired
    private BattleService battleService;

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private userRepo userRepo;

    @MockBean
    private BattleJudgeService battleJudgeService;

    private void ensureUser(String username) {
        if (!userRepo.existsById(username)) {
            user u = new user();
            u.setUserName(username);
            u.setRole("user");
            userRepo.save(u);
        }
    }

    @BeforeEach
    void setup() {
        ensureUser("submitP1");
        ensureUser("submitP2");
    }

    private Battle createActiveTestBattle(String p1, String p2) {
        Battle battle = new Battle(p1, p2, 18);
        battle.setTotalTests(10);
        battle.setStatus(BattleStatus.ACTIVE);
        battle.setStartTime(Instant.now());
        battle.setEndTime(Instant.now().plus(Duration.ofMinutes(20)));
        return battleRepository.save(battle);
    }

    @Test
    void testPartialSubmissionUpdatesScore() {
        Battle battle = createActiveTestBattle("submitP1", "submitP2");

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("status", "success");
        mockResult.put("totalTestCase", 10);
        mockResult.put("totalTestCasePassed", 6);
        mockResult.put("passedOrNot", false);

        when(battleJudgeService.judgeCodeWithoutSideEffects(anyInt(), any())).thenReturn(mockResult);

        BattleSubmissionRequest request = new BattleSubmissionRequest(new codeDto("int a = 1;", "java", ""));
        Map<String, Object> result = battleService.submitCode(battle.getId(), "submitP1", request);

        assertEquals(6, result.get("totalTestCasePassed"));
        assertFalse((Boolean) result.get("passedOrNot"));

        Battle updated = battleRepository.findById(battle.getId()).orElseThrow();
        assertEquals(BattleStatus.ACTIVE, updated.getStatus());
        assertEquals(6, updated.getP1BestPassed());
        assertNotNull(updated.getP1BestAt());
    }

    @Test
    void testRateLimitingEnforced() {
        Battle battle = createActiveTestBattle("submitP1", "submitP2");

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("status", "success");
        mockResult.put("totalTestCase", 10);
        mockResult.put("totalTestCasePassed", 3);
        mockResult.put("passedOrNot", false);

        when(battleJudgeService.judgeCodeWithoutSideEffects(anyInt(), any())).thenReturn(mockResult);

        BattleSubmissionRequest request = new BattleSubmissionRequest(new codeDto("int a = 1;", "java", ""));

        // First submit should succeed
        assertDoesNotThrow(() -> battleService.submitCode(battle.getId(), "submitP1", request));

        // Immediate second submit must fail with Rate limit exception
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                battleService.submitCode(battle.getId(), "submitP1", request)
        );
        assertTrue(ex.getMessage().contains("Rate limit"));
    }

    @Test
    void testFullSolutionWinsBattleAtomically() {
        Battle battle = createActiveTestBattle("submitP1", "submitP2");

        Map<String, Object> winResult = new HashMap<>();
        winResult.put("status", "success");
        winResult.put("totalTestCase", 10);
        winResult.put("totalTestCasePassed", 10);
        winResult.put("passedOrNot", true);

        when(battleJudgeService.judgeCodeWithoutSideEffects(anyInt(), any())).thenReturn(winResult);

        BattleSubmissionRequest request = new BattleSubmissionRequest(new codeDto("winning code", "java", ""));
        Map<String, Object> result = battleService.submitCode(battle.getId(), "submitP2", request);

        assertTrue((Boolean) result.get("passedOrNot"));

        Battle finished = battleRepository.findById(battle.getId()).orElseThrow();
        assertEquals(BattleStatus.FINISHED, finished.getStatus());
        assertEquals("submitP2", finished.getWinner());
        assertEquals(BattleEndReason.SOLVED, finished.getEndReason());
    }

    @Test
    void testTimeoutWinnerDetermination() {
        Battle battle = createActiveTestBattle("submitP1", "submitP2");
        battle.setP1BestPassed(8);
        battle.setP1BestAt(Instant.now().minusSeconds(100));
        battle.setP2BestPassed(5);
        battle.setP2BestAt(Instant.now().minusSeconds(50));
        battleRepository.save(battle);

        battleService.onBattleTimeout(battle.getId());

        Battle timedOut = battleRepository.findById(battle.getId()).orElseThrow();
        assertEquals(BattleStatus.FINISHED, timedOut.getStatus());
        assertEquals("submitP1", timedOut.getWinner()); // p1 has 8 > 5
        assertEquals(BattleEndReason.TIMEOUT, timedOut.getEndReason());
    }
}
