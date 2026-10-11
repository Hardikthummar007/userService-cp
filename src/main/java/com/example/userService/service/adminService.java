package com.example.userService.service;

import com.example.userService.dto.contest;
import com.example.userService.dto.contestSubmissionDto;
import com.example.userService.dto.userContestSubmissionRecord;
import com.example.userService.feign.contestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class adminService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    contestClient contestClient;

    public ResponseEntity<?> sortUserOfContest(String contestId,LocalDate date) {


//        String baseUrl = "http://localhost:8082/contestService/getUserOfContest";
//
//        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
//                .queryParam("contestId", contestId)
//                .queryParam("date", date)
//                .build()
//                .toUri();
//
//
//
//        ResponseEntity<List<userContestSubmissionRecord>> response = restTemplate.exchange(
//                uri,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<userContestSubmissionRecord>>() {}
//        );

//        List<userContestSubmissionRecord> allUserSub=response.getBody();
        ResponseEntity<?>  getListOfUserOfContestResponse= contestClient.getListOfUserOfContest(contestId, date);

        ObjectMapper mapperForgetListOfUserOfContestResponse=new ObjectMapper().registerModule(new JavaTimeModule());
        List<userContestSubmissionRecord> allUserSub =
                mapperForgetListOfUserOfContestResponse.convertValue(
                        getListOfUserOfContestResponse.getBody(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<userContestSubmissionRecord>>() {}
                );


        //sort the all user based on ending time
        Collections.sort(allUserSub, new Comparator<userContestSubmissionRecord>() {
            @Override
            public int compare(userContestSubmissionRecord o1, userContestSubmissionRecord o2) {
                LocalTime t1 = o1.getEndingTime() != null ? o1.getEndingTime() : LocalTime.MAX;
                LocalTime t2 = o2.getEndingTime() != null ? o2.getEndingTime() : LocalTime.MAX;
                return t1.compareTo(t2);
            }
        });

//        String urlToGetContest="http://localhost:8082/contestService/getContest?contestId="+contestId;
//
//
//        contest contest=restTemplate.getForObject(
//                urlToGetContest,
//                contest.class
//        );

//        contest contest= (com.example.userService.dto.contest) contestClient.getContest(contestId).getBody();
        ResponseEntity<?> contest = contestClient.getContest(contestId);

        Object bodyContest=contest.getBody();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()); // ✅ add this
        contest contests=mapper.convertValue(bodyContest,contest.class);
        int totalQustionInContest=contests.getProblemId().size();

        List<userContestSubmissionRecord> finalListOfuserWhoCompletedTheContest=new ArrayList<>();


        for( userContestSubmissionRecord user:allUserSub){
                if(user.getHowManyTestCasePassed()==totalQustionInContest){

                    System.out.println(user.getProblemsSolved());
                    finalListOfuserWhoCompletedTheContest.add(user);
                }
        }
        return new ResponseEntity<>(finalListOfuserWhoCompletedTheContest, HttpStatus.OK);

    }


}
