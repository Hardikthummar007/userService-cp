package com.example.userService.feign;


import com.example.userService.dto.problems;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "problemService",path = "problemService")
public interface problemClient {

    @PostMapping("/addProblem")
    ResponseEntity<?> add(@RequestBody problems p);

    @Transactional
    @GetMapping("/getProblem")
    ResponseEntity<?> get(@RequestParam int id);
}
