package com.example.userService.battle;

import com.example.userService.battle.service.MatchmakingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MatchmakingTest {

    @Autowired
    private MatchmakingService matchmakingService;

    @BeforeEach
    void setup() {
        // Clear any players left over from other tests
        matchmakingService.leaveQueue("userA");
        matchmakingService.leaveQueue("userB");
        matchmakingService.leaveQueue("userC");
        matchmakingService.removeActiveBattlePlayer("userA");
        matchmakingService.removeActiveBattlePlayer("userB");
        matchmakingService.removeActiveBattlePlayer("userC");
    }

    @Test
    void testJoinAndLeaveQueue() {
        assertTrue(matchmakingService.joinQueue("userA"));
        assertTrue(matchmakingService.isUserInQueue("userA"));

        // Duplicate join must be ignored
        assertFalse(matchmakingService.joinQueue("userA"));

        // Leave queue
        assertTrue(matchmakingService.leaveQueue("userA"));
        assertFalse(matchmakingService.isUserInQueue("userA"));
    }

    @Test
    void testPairingTwoUsers() {
        assertTrue(matchmakingService.joinQueue("userA"));
        assertTrue(matchmakingService.isUserInQueue("userA"));

        // When second user joins, they are paired immediately
        assertTrue(matchmakingService.joinQueue("userB"));

        // Neither should remain in waiting queue
        assertFalse(matchmakingService.isUserInQueue("userA"));
        assertFalse(matchmakingService.isUserInQueue("userB"));

        // Both are now marked active in battle
        assertTrue(matchmakingService.isUserInActiveBattle("userA"));
        assertTrue(matchmakingService.isUserInActiveBattle("userB"));
    }

    @Test
    void testCannotJoinWhileInActiveBattle() {
        matchmakingService.joinQueue("userA");
        matchmakingService.joinQueue("userB");

        // userA is in active battle, join must be rejected
        assertFalse(matchmakingService.joinQueue("userA"));
    }

    @Test
    void testConcurrentJoinRaceSafety() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final String username = "concurrent_user_" + i;
            executor.submit(() -> {
                try {
                    startGate.await();
                    boolean res = matchmakingService.joinQueue(username);
                    results.add(res);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Fire all threads simultaneously
        startGate.countDown();
        endGate.await();
        executor.shutdown();

        assertEquals(threadCount, results.size());
        // All joins must succeed
        for (Boolean res : results) {
            assertTrue(res);
        }

        // Clean up
        for (int i = 0; i < threadCount; i++) {
            matchmakingService.removeActiveBattlePlayer("concurrent_user_" + i);
            matchmakingService.leaveQueue("concurrent_user_" + i);
        }
    }
}
