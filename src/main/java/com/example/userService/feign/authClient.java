package com.example.userService.feign;

import com.example.userService.dto.userAuthDto;
import com.example.userService.entity.user;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "authService",path = "/auth")
public interface authClient {


    @PostMapping("signUp")
    ResponseEntity<String> signUp(@RequestBody userAuthDto u);

    @GetMapping("login")
    String login(@RequestBody userAuthDto u);
}
