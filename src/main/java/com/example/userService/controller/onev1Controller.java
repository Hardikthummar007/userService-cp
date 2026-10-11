package com.example.userService.controller;

import com.example.userService.battle.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("onev1Service")
public class onev1Controller {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Helper endpoint for Phase 1 testing:
     * Generates a valid JWT token signed with jwt.secret to test STOMP CONNECT.
     */
    @GetMapping("generateTestToken")
    public ResponseEntity<?> generateTestToken(
            @RequestParam(defaultValue = "player1") String username,
            @RequestParam(defaultValue = "user") String role) {
        String token = jwtUtil.generateToken(username, role);
        return ResponseEntity.ok(Map.of(
                "username", username,
                "role", role,
                "token", token,
                "bearer", "Bearer " + token
        ));
    }
}

