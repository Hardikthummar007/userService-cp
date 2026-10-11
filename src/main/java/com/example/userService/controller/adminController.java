package com.example.userService.controller;


import com.example.userService.dto.userContestSubmissionRecord;
import com.example.userService.service.adminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("")
public class adminController {

    @Autowired
    adminService adminService;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;


    @GetMapping("sortUserOfContestWhoCompletedTheContest")
    ResponseEntity<?> sortUserOfContest(
            @RequestParam String contestId,
            @RequestParam LocalDate date
    ){
        return adminService.sortUserOfContest(contestId,date);
    }

    @GetMapping("/topersOnDashBoard")
    ResponseEntity<?> webSocketUserAscendingOrder(@RequestParam String contestId,@RequestParam LocalDate date){

        List<userContestSubmissionRecord> listOfUser = (List<userContestSubmissionRecord>) adminService.sortUserOfContest(contestId, date).getBody();

        simpMessagingTemplate.convertAndSend("/topic/getWinner", listOfUser);
        return ResponseEntity.ok("Leaderboard pushed");

    }


}
