package com.example.userService.battle.websocket;

import java.security.Principal;

/**
 * Custom Principal representation for authenticated STOMP WebSocket sessions.
 * Allows Spring STOMP messaging to identify users by their String userName
 * so principal.getName() returns the player's username.
 */
public class StompPrincipal implements Principal {

    private final String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "StompPrincipal{" + "name='" + name + '\'' + '}';
    }
}
