package com.example.userService.battle;

import com.example.userService.battle.util.JwtUtil;
import com.example.userService.battle.websocket.StompAuthChannelInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class JwtAndStompAuthTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StompAuthChannelInterceptor interceptor;

    @Test
    void testJwtGenerationAndValidation() {
        String username = "alice";
        String token = jwtUtil.generateToken(username, "user");

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("alice", jwtUtil.extractUsername(token));

        // Tampered token must fail
        assertFalse(jwtUtil.validateToken(token + "tampered"));
    }

    @Test
    void testStompConnectAuthenticationSuccess() {
        String token = jwtUtil.generateToken("bob", "user");

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("Authorization", "Bearer " + token);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageChannel mockChannel = mock(MessageChannel.class);
        Message<?> result = interceptor.preSend(message, mockChannel);

        assertNotNull(result);
        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertNotNull(resultAccessor);
        assertNotNull(resultAccessor.getUser(), "Principal should be set on successful CONNECT");
        assertEquals("bob", resultAccessor.getUser().getName());
    }

    @Test
    void testStompConnectWithInvalidTokenFails() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer invalid.jwt.token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageChannel mockChannel = mock(MessageChannel.class);
        assertThrows(IllegalArgumentException.class, () -> {
            interceptor.preSend(message, mockChannel);
        });
    }
}
