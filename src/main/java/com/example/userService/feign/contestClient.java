package com.example.userService.feign;

import com.example.userService.dto.contest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "contestService",path = "/contestService")
public interface contestClient {


    @PostMapping("/addContest")
    ResponseEntity<?> addContest(@RequestBody contest contest);

    @PostMapping("/registerToContest")
    ResponseEntity<String> register(
            @RequestParam String userId,
            @RequestParam String  contestId
    );

    @PostMapping("/startContest")
    ResponseEntity<String> startContest(
            @RequestParam String userId,
            @RequestParam String  contestId
    );

    @PostMapping("endContest")
    ResponseEntity<String> endContest(
            @RequestParam String userId,
            @RequestParam String contestId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    );

    @GetMapping("/getContest")
    ResponseEntity<?> getContest(@RequestParam String contestId);


    @GetMapping("/getContestSubmissionOfUser")
    ResponseEntity<?> getContestSubmissionOfUser(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String contestId
    );

    @PostMapping("increaseCountOfProblemSolved")
    ResponseEntity<String> increase(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String contestId,
            @RequestParam int problemId
    );

    @GetMapping("getUserOfContest")
    ResponseEntity<?> getListOfUserOfContest(
            @RequestParam String  contestId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date

            );
}
