package com.example.userService.feign;


import com.example.userService.dto.codeBaseNormalSubmissionDto;
import com.example.userService.dto.normalSubmission;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "normalSubmissionService",path = "/normalSubmissionService")
public interface normalSubClient {

    @PostMapping("/normalSubmission")
    String  addNormalSub(@RequestBody normalSubmission normalSubmission);

    @GetMapping("/getAllSubmissionOfUser")
    ResponseEntity<?> getAllSubOfUser(@RequestParam String userName);

    @PostMapping("/addCodeBase")
    ResponseEntity<codeBaseNormalSubmissionDto> addCodeBase(@RequestBody codeBaseNormalSubmissionDto codeBase);


//    @GetMapping("/getSubmissionPerProblem")
//    ResponseEntity<?> gspp(@RequestParam String  userName,@RequestParam int problemId );
}
