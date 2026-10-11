package com.example.userService.controller;


import com.example.userService.dto.codeDto;
import com.example.userService.dto.contestSubmissionDto;
import com.example.userService.dto.userNormalSubmissonDto;
import com.example.userService.entity.user;
import com.example.userService.feign.normalSubClient;
import com.example.userService.service.userService;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("userService")
//@CrossOrigin(origins = "*")
public class userController {

    @Autowired
    userService userServise;
    private final WebClient webClient;




    userController(WebClient.Builder webClient){
        this.webClient=webClient.build();
    }






//    @GetMapping("getSubOfUserAsPerQuestion")
//    ResponseEntity<?>  getSubOfUserAsPerQuestion(@RequestParam String userName,@RequestParam int questionId){
//
//        return normalSubClient.gspp(userName,questionId);
//
//    }


    @PostMapping("/optimizeCompileNormalSolution")
    ResponseEntity<?> optimizeCompileNormalSolution(@RequestBody userNormalSubmissonDto userNormalSubmissonDto) {

        String tc="";
        String sc="";
      return   userServise.optimzeSub(userNormalSubmissonDto,tc,sc);

    }



        @GetMapping("isHeSheAdmin")
            ResponseEntity<?> isAdmin(@RequestParam String userName){
            return userServise.isAdmin(userName);

    }



    @GetMapping("profileLoading")
    ResponseEntity<?> loadProfile(
            @RequestParam String userId
    )
    {
        return userServise.loadProfile(userId);
    }








    @GetMapping("getProblemsSolveByUser")
    ResponseEntity<?> getProblemsSolvedByUser(String userName){

        return userServise.getProblemsSovedByUser(userName);

    }

    @PostMapping("/optimizeSubmission")
    ResponseEntity<?> optimize(@RequestBody userNormalSubmissonDto userNormalSubmissonDto){

        String tc="";
        String sc="";
        return userServise.optimzeSub(userNormalSubmissonDto,tc,sc);

    }




    @PostMapping("/normalSubmission")
    ResponseEntity<?> normalSub(@RequestBody userNormalSubmissonDto userNormalSubmissonDto){


        String  code=userNormalSubmissonDto.getCode().getCode();
////        String tcQuestion=code+"find time compilexity of this code int 0() form in ans in one word";
////        String scQuestion=code+"find space compilexity of this code int 0() form in ans in one word";
//
        String timeComplexity="";
        String spaceComplexity="";
//
////        String timeComplexity=(String) geminis(tcQuestion).getBody();
////        String spaceComplexity=(String) geminis(scQuestion).getBody();
//
//
        return userServise.normalSub(userNormalSubmissonDto,timeComplexity,spaceComplexity);

    }


    public   ResponseEntity<?> geminis(String question){
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[] {
                                Map.of("text", question)
                        } )
                }
        );

        Map<String ,Object> response = webClient.post()
                .uri(finalUrl)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");

        String answer = parts.get(0).get("text");
        System.out.println(response);

        return new ResponseEntity<>(answer, HttpStatus.OK);

    }


    @PostMapping("/singUp")
    ResponseEntity<?> singUp(@RequestBody user user){
        return userServise.singUp(user);
    }

    @GetMapping("/getUserSubmissions")
    ResponseEntity<?> getuserSubs(@RequestParam String userName){
        return userServise.userAllSubmission(userName);
    }

    @PostMapping("submistOneByOneProblemOfContest")
    ResponseEntity<?> submistOneByOneProblemOfContest(
            @RequestBody contestSubmissionDto contestSubmissionDto
    ){
        String tcQuestion=contestSubmissionDto.getCode().getCode()+"find time compilexity of this code int 0() form in ans in one word";
        String scQuestion=contestSubmissionDto.getCode().getCode()+"find space compilexity of this code int 0() form in ans in one word";
//        String timeComplexity=(String) geminis(tcQuestion).getBody();
//        String spaceComplexity=(String) geminis(scQuestion).getBody();

        System.out.println("reached submistOneByOneProblemOfContest ");

        return userServise.submistOneByOneProblemOfContest(contestSubmissionDto);
    }





    @PostMapping("threadTesting")
    ResponseEntity<?> threadingTest(
            @RequestBody contestSubmissionDto contestSubmissionDto
    ){
        return userServise.threadingSumOneByOne(contestSubmissionDto,"","");
    }



    @PostMapping("registerToContest")
    ResponseEntity<?> registerToContest(
            @RequestParam String userId,
            @RequestParam String contestId
    ){

        return userServise.registerToContest(userId,contestId);

    }

    @PostMapping("/startContest")
    ResponseEntity<?> startContest(
            @RequestParam String userId,
            @RequestParam String contestId
    ){
        return userServise.startTheContest(userId,contestId);
    }

    @PostMapping("endContest")
    ResponseEntity<?> endContest(
            @RequestParam String userId,
            @RequestParam String contestId,
            @RequestParam LocalDate date
    ){
        return userServise.endContest(userId,contestId,date);
    }


    @GetMapping("/getUserById")
    ResponseEntity<?> getUser(
            @RequestParam String  username
    ){

       return userServise.getUserById(username);

    }


    String finalUrl="https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyBj2DS9PPPMH6l9ObJZsNX1ynxKqn6Vzog";



}
