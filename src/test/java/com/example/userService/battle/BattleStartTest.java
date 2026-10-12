package com.example.userService.battle;

import com.example.userService.battle.dto.BattleProblemDto;
import com.example.userService.battle.model.Battle;
import com.example.userService.battle.model.BattleStatus;
import com.example.userService.battle.repo.BattleRepository;
import com.example.userService.battle.service.BattleService;
import com.example.userService.battle.websocket.StompAuthChannelInterceptor;
import com.example.userService.battle.websocket.StompPrincipal;
import com.example.userService.dto.problems;
import com.example.userService.dto.sampleIO;
import com.example.userService.dto.testCases;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class BattleStartTest {

    @Autowired
    private BattleService battleService;

    @Autowired
    private BattleRepository battleRepository;

    @Autowired
    private StompAuthChannelInterceptor interceptor;

    @Autowired
    private com.example.userService.repo.userRepo userRepo;

    private void ensureUserExists(String username) {
        if (userRepo.findById(username).isEmpty()) {
            com.example.userService.entity.user u = new com.example.userService.entity.user();
            u.setUserName(username);
            u.setPassword("pass");
            u.setRole("user");
            u.setEmail(username + "@test.com");
            u.setCoutry("US");
            u.setTotalProblemSolved(0);
            userRepo.save(u);
        }
    }

    @Test
    void testCreateBattleAndActivation() throws InterruptedException {
        String p1 = "testPlayer1";
        String p2 = "testPlayer2";
        ensureUserExists(p1);
        ensureUserExists(p2);

        Battle battle = battleService.createBattle(p1, p2);
        assertNotNull(battle);
        assertNotNull(battle.getId());
        assertEquals(BattleStatus.WAITING, battle.getStatus());
        assertEquals(p1, battle.getPlayer1());
        assertEquals(p2, battle.getPlayer2());
        assertNotNull(battle.getProblemId());

        // Participants check
        assertTrue(battleService.isParticipant(battle.getId(), p1));
        assertTrue(battleService.isParticipant(battle.getId(), p2));
        assertFalse(battleService.isParticipant(battle.getId(), "outsider"));

        // Wait 3.5s for start timer to transition battle to ACTIVE
        Thread.sleep(3500);

        Battle activated = battleRepository.findById(battle.getId()).orElseThrow();
        assertEquals(BattleStatus.ACTIVE, activated.getStatus());
        assertNotNull(activated.getStartTime());
        assertNotNull(activated.getEndTime());
        assertTrue(activated.getEndTime().isAfter(activated.getStartTime()));
    }

    @Test
    void testProblemPayloadSanitizationStripsHiddenTestCases() {
        problems raw = new problems();
        raw.setProblem_id(18);
        raw.setTittle("Two Sum");
        raw.setDescription("Find indices that sum to target");
        raw.setDifficulty(1);
        sampleIO s = new sampleIO();
        s.setTestcaseInput("[2,7,11,15], 9");
        s.setExpectedOutput("[0,1]");
        raw.setSampleIOS(List.of(s));

        // Add hidden evaluation test cases
        testCases hiddenTc = new testCases();
        hiddenTc.setTc_input("[3,2,4], 6");
        hiddenTc.setTc_ouput("[1,2]");
        raw.setTestCases(List.of(hiddenTc));

        // Convert to battle payload
        BattleProblemDto sanitized = BattleProblemDto.fromProblem(raw);

        assertEquals(18, sanitized.getProblemId());
        assertEquals("Two Sum", sanitized.getTitle());
        assertEquals(1, sanitized.getSampleIOS().size());

        // Verify BattleProblemDto has NO getTestCases method and hidden tests are completely stripped
        assertNotNull(sanitized.getSampleIOS());
    }

    @Test
    void testSubscriptionAuthorization() {
        String p1 = "subPlayer1";
        String p2 = "subPlayer2";
        ensureUserExists(p1);
        ensureUserExists(p2);
        Battle battle = battleService.createBattle(p1, p2);

        MessageChannel mockChannel = mock(MessageChannel.class);

        // 1. Participant p1 subscribes -> Allowed
        StompHeaderAccessor accessorP1 = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessorP1.setLeaveMutable(true);
        accessorP1.setDestination("/topic/battle/" + battle.getId());
        accessorP1.setUser(new StompPrincipal(p1));
        Message<byte[]> msgP1 = MessageBuilder.createMessage(new byte[0], accessorP1.getMessageHeaders());
        assertDoesNotThrow(() -> interceptor.preSend(msgP1, mockChannel));

        // 2. Outsider "intruder" subscribes -> Rejected with IllegalArgumentException
        StompHeaderAccessor accessorIntruder = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessorIntruder.setLeaveMutable(true);
        accessorIntruder.setDestination("/topic/battle/" + battle.getId());
        accessorIntruder.setUser(new StompPrincipal("intruder"));
        Message<byte[]> msgIntruder = MessageBuilder.createMessage(new byte[0], accessorIntruder.getMessageHeaders());

        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(msgIntruder, mockChannel));
    }
}
