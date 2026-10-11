package com.example.userService.service;

import com.example.userService.feign.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.userService.dto.*;
import com.example.userService.entity.user;
import com.example.userService.repo.userRepo;
import com.example.userService.utility.utility;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.aspectj.weaver.ast.Call;
import org.hibernate.engine.jdbc.mutation.internal.MutationExecutorPostInsertSingleTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class userService {


    @Autowired
    utility utility;
    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
    contestClient contestClient;
    
    @Autowired
    problemClient problemClient;
    
    @Autowired
    normalSubClient normalSubClient;
    
    @Autowired
    compilerClient compilerClient;

    @Autowired
    authClient authClient;


    @Autowired
    adminService adminService;






    String finalUrl="https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyB7auZe3xctUgw1TkzyAge7hhhZGK6XrP4";




    @Autowired
    userRepo userRepo;


    WebClient webClient;


    userService(WebClient.Builder webclient){
        this.webClient=webclient.build();
    }



    //failling Methods  here to
    ResponseEntity<?> faillingMethod(String userId,String contestId,Exception ex){

        return new ResponseEntity<>(new faillingDto("service is down"),HttpStatus.NOT_ACCEPTABLE);
    }

    ResponseEntity<?> faillingMethodForThreadingSub(contestSubmissionDto dto,Exception e){
        return new ResponseEntity<>(new faillingDto("one of the service down which is used to compile the code"),HttpStatus.NOT_FOUND);
    }
//    here

    public ResponseEntity<?> normalSub(userNormalSubmissonDto dto, String tc, String sc) {
        codeDto codeData = dto.getCode();
        int problemId = dto.getProblemId();
        String userName = dto.getUsername();

        // Fetch problem with test cases
        problems problem = utility.convertResponse(
                problemClient.get(problemId), new TypeReference<problems>() {}
        );
        List<testCases> testCases = problem.getTestCases();

        int threadCount = 3;
        int chunkSize = (int) Math.ceil((double) testCases.size() / threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Callable<Map<String, Object>>> tasks = new ArrayList<>();

        for (int i = 0; i < testCases.size(); i += chunkSize) {
            List<testCases> subList = testCases.subList(i, Math.min(i + chunkSize, testCases.size()));

            tasks.add(() -> {
                List<String> results = new ArrayList<>();
                int passedCount = 0;

                for (testCases testCase : subList) {
                    codeDto singleInput = new codeDto(
                            codeData.getCode(),
                            codeData.getLangauge(),
                            testCase.getTc_input()
                    );

                    try {
                        Map<String, String> result = callingCompilerService(singleInput);
                        System.out.println("compiler output"+result.get("stdout") +"  "+ " expected"+testCase.getTc_ouput());
                        String actualOutput = Optional.ofNullable(result.get("stdout"))
                                .orElse("")
                                .trim()
                                .replaceAll("\\s+", " ");
                        String expectedOutput = Optional.ofNullable(testCase.getTc_ouput()).orElse("").trim();

                        if (actualOutput.equals(expectedOutput)) {
                            passedCount++;
                            results.add(testCase.getTc_input() + " -> " + actualOutput + " [PASSED]");
                        } else {
                            results.add(testCase.getTc_input() + " -> " + actualOutput +
                                    " | Expected: " + expectedOutput + " [FAILED]");
                        }

                    } catch (Exception e) {
                        results.add("Error for input: " + testCase.getTc_input() + " -> " + e.getMessage());
                    }
                }

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("totalTestCasePassed", passedCount);
                resultMap.put("result", results);
                return resultMap;
            });
        }

        try {
            List<Future<Map<String, Object>>> futures = executor.invokeAll(tasks);
            executor.shutdown();

            int totalPassed = 0;
            List<String> finalResults = new ArrayList<>();

            for (Future<Map<String, Object>> future : futures) {
                Map<String, Object> result = future.get();
                totalPassed += (int) result.get("totalTestCasePassed");
                finalResults.addAll((List<String>) result.get("result"));
            }

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("status", totalPassed == testCases.size() ? "success" : "unsuccess");
            finalResponse.put("totalTestCasePassed", totalPassed);
            finalResponse.put("totalTestCase", testCases.size());
            finalResponse.put("result", finalResults);
            finalResponse.put("time Complexity",tc);
            finalResponse.put("space Complexity",sc);

            // Save submission
            normalSubmission submission = new normalSubmission(
                    userName, problemId,
                    (String) finalResponse.get("status"),
                    totalPassed, "0", tc, sc, LocalDate.now()
            );

            normalSubClient.addNormalSub(submission);

            if(totalPassed==testCases.size()){
                userRepo.findById(userName).ifPresent(user -> {
                    if (!user.getProblemSolvedId().contains(problemId)) {
                        user.getProblemSolvedId().add(problemId);
                        user.setTotalProblemSolved(user.getTotalProblemSolved() + 1);
                        userRepo.save(user);
                    }
                });
            }
            // Update user profile


            // Save user's code to DB
            codeBaseNormalSubmissionDto codeSave = new codeBaseNormalSubmissionDto(
                    codeData.getCode(),
                    codeData.getLangauge(),
                    problemId,
                    userName,
                    LocalDate.now(),
                    1,1
            );
            utility.convertResponse(normalSubClient.addCodeBase(codeSave), new TypeReference<>() {});

            return new ResponseEntity<>(finalResponse, HttpStatus.OK);

        } catch (Exception e) {
            executor.shutdownNow();
            throw new RuntimeException("Error during parallel test execution: " + e.getMessage(), e);
        }
    }


    public ResponseEntity<?> singUp(user user) {

        //setting the userData in data base of Auth Server to authenticate

        userAuthDto userDto=new userAuthDto();
        userDto.setUsername(user.getUserName());
        userDto.setPassword(user.getPassword());
        userDto.setRole(user.getRole());

        String responseFromAuthService=utility.convertResponse(
                authClient.signUp(userDto),
                new TypeReference<String>() {}
        );
        System.out.println(responseFromAuthService+" 🖕🏻");


        userRepo.save(user);
        return new ResponseEntity<>("singUp",HttpStatus.OK);
    }
    public ResponseEntity<?> userAllSubmission(String userName){

//        String urlForNormalSubService="http://localhost:8081/normalSubmissoinService/getAllSubmissionOfUser?userName="+userName;
//
//        List<normalSubmission> listOfUserSub=restTemplate.exchange(
//                urlForNormalSubService,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<normalSubmission>>() {}
//        ).getBody();


        List<normalSubmission> listOfUserSub = utility.convertResponse(
                normalSubClient.getAllSubOfUser(userName),
                new TypeReference<List<normalSubmission>>() {}
        );





        return new ResponseEntity<>(listOfUserSub,HttpStatus.OK);


    }


    public  ResponseEntity<?> submistOneByOneProblemOfContest(contestSubmissionDto dto){


        contest contest=getContestFromContestService(dto.getContestId());
        int problemId=contest.getProblemId().get(dto.getProblemNo());
        String userId=dto.getUserId();
        LocalDate date=LocalDate.now();
        String status="";


        problems problem=getProblemFromProblemService(problemId);


        codeDto code=dto.getCode();
        int testCasePassed=0;
//        for(testCases t:problem.getTestCases()){
//            code.setInput(t.getTc_input());
//            codeDto copyCode=new codeDto();
//            copyCode.setLangauge(code.getLangauge());
//            copyCode.setCode(code.getCode());
//            copyCode.setInput(code.getInput());
//
//
//            Map<String,String> responseFromCompiler=callingCompilerService(copyCode);
//
//            String compilerOutput=responseFromCompiler.get("stdout");
//            String expectedOutput=t.getTc_ouput();
//
//            if(compilerOutput.equals(expectedOutput)){
//                System.out.println("output  "+compilerOutput +" expected "+expectedOutput);
//                testCasePassed++;
//            }else{
//                System.out.println("wrongnoutput"+compilerOutput +" expected "+expectedOutput);
//
//
//            }
//        }

        optimzeSubDto d=new optimzeSubDto(problem.getTestCases(),code);



        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<optimzeSubDto> request = new HttpEntity<>(d, headers);

        String api="http://localhost:8083/compilerService/testing";

        // 6. Send POST request
        ResponseEntity<Map> response = restTemplate.exchange(
                api,
                HttpMethod.POST,
                request,
                Map.class
        );

        testCasePassed=(int) response.getBody().get("totalTestCasePassed");

        System.out.println(testCasePassed);


//post compilation


        System.out.println("reached post compilation line");
        if(testCasePassed==problem.getTestCases().size()){
            status="success";
//            String urlOfContestServiceToIncreaseProblemSolvedCount = UriComponentsBuilder.fromHttpUrl("http://localhost:8082/contestService/increaseCountOfProblemSolved")
//                    .queryParam("userId", userId)
//                    .queryParam("date", contest.getStartingDate())
//                    .queryParam("contestId", contest.getContestName())
//                    .queryParam("problemId",problemId)
//                    .toUriString();
//
//            //increament the problemsolved count
//            restTemplate.postForEntity(
//                    urlOfContestServiceToIncreaseProblemSolvedCount,
//                    null,
//                    String.class
//            );

            System.out.println(contest.getStartingDate());
            String Response=utility.convertResponse(
                    contestClient.increase(userId, contest.getStartingDate(), contest.getContestName(), problemId),
                    new TypeReference<String>() {}
            );
            System.out.println(response.getBody());

            return  new ResponseEntity<>(response.getBody(),HttpStatus.OK);
        }else{
            status="not success";

            return  new ResponseEntity<>("unsucceess ",HttpStatus.NOT_ACCEPTABLE);

        }
    }

    int counter=1;



//    @Retry(name = "retryTemplate",fallbackMethod = "faillingMethodForThreadingSub")
    public ResponseEntity<?> threadingSumOneByOne(contestSubmissionDto dto,String tc,String sc){
        System.out.println(counter+" attempt");
        contest contest=getContestFromContestService(dto.getContestId());
        int problemId=contest.getProblemId().get(dto.getProblemNo());
        String userId=dto.getUserId();
        String status="";

        codeDto code=dto.getCode();

        problems problem=getProblemFromProblemService(problemId);

        List<testCases> testCases=problem.getTestCases();

        int numOfThreads=2;
        int howManyCaseEachThreadHandle=(int) Math.ceil((double) testCases.size() / numOfThreads);

        ExecutorService excecutor= Executors.newFixedThreadPool(numOfThreads);

        List<Callable<Map<String ,Object>>> taskList= new ArrayList<>();

        for(int i=0;i<testCases.size();i+=howManyCaseEachThreadHandle) {
            int start = i;
            int end = Math.min(i + howManyCaseEachThreadHandle, testCases.size());

            List<testCases> subList = testCases.subList(start, end);

            taskList.add(
                    new Callable<Map<String, Object>>() {
                        @Override
                        public Map<String, Object> call() throws Exception {
                            List<String> output = new ArrayList<>();
                            int totolPassed = 0;


                            for (testCases t : subList) {
                                codeDto copyOfCode=new codeDto();
                                copyOfCode.setInput(t.getTc_input());
                                copyOfCode.setCode(code.getCode());
                                copyOfCode.setLangauge(code.getLangauge());
                                try {
                                Map<String, String> oneCase = callingCompilerService(copyOfCode);

                                    if (oneCase != null && oneCase.get("stdout") != null &&
                                            oneCase.get("stdout").trim().equals(t.getTc_ouput().trim())) {
                                        output.add(oneCase.get("stdout").trim() + " expected " + t.getTc_ouput().trim());
                                        System.out.println(oneCase.get("stdout").trim() + " expected " + t.getTc_ouput().trim());
                                        totolPassed++;
                                    } else {
                                        output.add(oneCase.get("stdout").trim() + " fail expected " + t.getTc_ouput().trim()+"hardik"+t.getTc_input());
                                        System.out.println(oneCase.get("stdout").trim() + " fail expected " + t.getTc_ouput().trim()+"hardik"+t.getTc_input());
                                        System.out.println(t.getTc_input()+"  ");
                                    }
                                } catch (Exception ex) {
                                    output.add(t.getTc_input() + " error: " + ex.getMessage());
                                }
                            }

                            Map<String, Object> groupResult = new HashMap<>();

                            groupResult.put("totalTestCasePassed", totolPassed);
                            groupResult.put("result", output);
                            return groupResult;
                        }
                    }
            );
        }

            try {

                List<Future<Map<String,Object>>>  futures=excecutor.invokeAll(taskList);

                excecutor.shutdown();
                int totalTestCasePassed=0;
                List<String> combineResultOfAllThread=new ArrayList<>();

                for(Future<Map<String,Object>> f:futures){
                    Map<String,Object> response=f.get();

                    totalTestCasePassed+=(int) response.get("totalTestCasePassed");
                    combineResultOfAllThread.addAll((Collection<? extends String>) response.get("result"));
                }
                Map<String, Object> finalResult = new HashMap<>();


                if (totalTestCasePassed==testCases.size()){
                    finalResult.put("status","success");
                    //increament the problem count;
//                    String increment=utility.convertResponse(contestClient.increase(userId, contest.getStartingDate(), contest.getContestName(), problemId),
//                            new TypeReference<String>() {}
//                    );
                    String increment=utility.convertResponse(
                            contestClient.increase(userId, contest.getStartingDate(), contest.getContestName(), problemId),
                            new TypeReference<String>() {
                            }
                    );
                }else{
                    finalResult.put("status","unsuccess");
                }

                finalResult.put("totalTestCasePassed",totalTestCasePassed);
                finalResult.put("totalTestCase",testCases.size());
                finalResult.put("time Complexity",tc);
                finalResult.put("space Complexity",sc);
                finalResult.put("result", combineResultOfAllThread);

                return new ResponseEntity<>(finalResult,HttpStatus.OK);

            }catch (Exception e){
                excecutor.shutdown();
                throw new RuntimeException("Error running test cases in parallel: " + e.getMessage());
            }



    }


    public Map<String ,String> callingCompilerService(codeDto codeDto){


//        url is of docker container
        String  url= "http://localhost:8083/compilerService/runCode";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<codeDto> request = new HttpEntity<>(codeDto, headers);

        Map<String,String> response=restTemplate.postForEntity(
                url,
                request,
                Map.class
        ).getBody();


        //using feign client

//        Map<String ,String > response=utility.convertResponse(
//                compilerClient.runcode(codeDto),
//                new TypeReference<Map<String, String>>() {}
//        );
        return response;

    }

    public  problems getProblemFromProblemService(int problemId){
//        String url="http://localhost:8080/problemService/getProblem?id="+problemId;

//        problems problems=restTemplate.getForObject(
//                url,
//                com.example.userService.dto.problems.class
//        );

        problems problems=utility.convertResponse(
                problemClient.get(problemId),
                new TypeReference<problems>() {}
        );

        return problems;


    }


    public contest getContestFromContestService(String contestId){

//        String url="http://localhost:8082/contestService/getContest?contestId="+contestId;
//        contest contest=restTemplate.getForObject(
//                url,
//                contest.class
//        );

        contest contest=utility.convertResponse(
                contestClient.getContest(contestId),
                new TypeReference<contest>(){}
        );

        return contest;


    }


    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;


    public ResponseEntity<?> endContest(String userId, String contestId, LocalDate date) {

//        String urlForContestService= "http://localhost:8082/contestService/endContest?userId="+userId+"&contestId="+contestId+"&date="+date;

//        String response=restTemplate.postForEntity(
//                urlForContestService,
//                null,
//                String.class
//        ).getBody();

        String response= utility.convertResponse(
                contestClient.endContest(userId, contestId, date),
                new TypeReference<String>() {}
        );



        List<userContestSubmissionRecord> listOfUser = (List<userContestSubmissionRecord>) adminService.sortUserOfContest(contestId, date).getBody();
        simpMessagingTemplate.convertAndSend("/topic/getWinner", listOfUser);


        return new ResponseEntity<>(response,HttpStatus.OK);

    }




    @CircuitBreaker(name = "circuitBreakerTemplate",fallbackMethod = "faillingMethod")
    public   ResponseEntity<?> registerToContest(String userId,String contestId){

    String response=    utility.convertResponse(
            contestClient.register(userId, contestId),
            new TypeReference<String>() {}
    );


    return new ResponseEntity<>(response,HttpStatus.OK);

    }

    public ResponseEntity<?> startTheContest(String userId,String contestId)
    {
        String response=utility.convertResponse(
                contestClient.startContest(userId, contestId),
                new TypeReference<String>() {}
        );

        return new ResponseEntity<>(response,HttpStatus.OK);
    }


    public  ResponseEntity<?> getUserById(String userName){

        Optional<user> user=userRepo.findById(userName);
        if(user.isEmpty()){
            return new ResponseEntity<>("user not found",HttpStatus.NOT_FOUND);
        }else{
            user.get().getUserName();//lazy load handle karava mate
            return new ResponseEntity<>(user.get(),HttpStatus.OK);

        }
    }


    public ResponseEntity<?> getProblemsSovedByUser(String userName) {


        Optional<user> u=userRepo.findById(userName);
        List<problems> listOfProblemsSolvedByUser=new ArrayList<>();


        if(u.isEmpty()){
            return  new ResponseEntity<>("user Not Found",HttpStatus.NOT_FOUND);
        }else{

            user user=u.get();

            Set<Integer> problemIds=user.getProblemSolvedId();


            for(Integer id:problemIds){
                int pronlemid=id;

                problems p=utility.convertResponse(
                        problemClient.get(pronlemid),
                        new TypeReference<problems>() {
                        }
                );
                listOfProblemsSolvedByUser.add(p);
            }
        }
        return  new ResponseEntity<>(listOfProblemsSolvedByUser,HttpStatus.OK);
    }

    public ResponseEntity<?> loadProfile(String userId) {



       ResponseEntity<?> p= getProblemsSovedByUser(userId);


       if(p.getBody() instanceof String){
           return p;
       }


       List<problems> listOfProblems= (List<problems>) p.getBody();

       return new ResponseEntity<>(listOfProblems,HttpStatus.OK);

    }

    public ResponseEntity<?> isAdmin(String userName) {
        Optional<user> u=userRepo.findById(userName);
        if(u.isPresent()){
            if(u.get().getRole().equals("admin")){
                return new ResponseEntity<>("True",HttpStatus.OK);
            }else
            {
                return new ResponseEntity<>("False",HttpStatus.OK);
            }
        }else {
            return  new ResponseEntity<>("userNotFound",HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> optimzeSub(userNormalSubmissonDto userNormalSubmissonDto, String tc, String sc) {

        problems problems=utility.convertResponse(
                problemClient.get(userNormalSubmissonDto.getProblemId()),
                new TypeReference<problems>() {}
        );
        List<testCases> testCases=problems.getTestCases();
        codeDto code=userNormalSubmissonDto.getCode();

        optimzeSubDto d=new optimzeSubDto(
                testCases,code
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<optimzeSubDto> request = new HttpEntity<>(d, headers);

        String api="http://localhost:8083/compilerService/testing";

        // 6. Send POST request
        ResponseEntity<Map> response = restTemplate.exchange(
                api,
                HttpMethod.POST,
                request,
                Map.class
        );


        System.out.println(response.getBody().get("totalTestCasePassed"));

        String userName=userNormalSubmissonDto.getUsername();
        int problemId=userNormalSubmissonDto.getProblemId();

        int totalPassed=(int) response.getBody().get("totalTestCasePassed");




//        work in progress



        normalSubmission submission = new normalSubmission(
                userName, problemId,
                (String) response.getBody().get("status"),
                totalPassed, "0", tc, sc, LocalDate.now()
        );

        normalSubClient.addNormalSub(submission);

        if(totalPassed==testCases.size()){
            userRepo.findById(userName).ifPresent(user -> {
                if (!user.getProblemSolvedId().contains(problemId)) {
                    user.getProblemSolvedId().add(problemId);
                    user.setTotalProblemSolved(user.getTotalProblemSolved() + 1);
                    userRepo.save(user);
                }
            });
        }
        // Update user profile
//    public codeBaseNormalSubmissionDto(int id, String code, String language, int problemId, String username, LocalDate submissionTime, int totalTestCases, int passedTestCases) {




        // Save user's code to DB
        codeBaseNormalSubmissionDto codeSave = new codeBaseNormalSubmissionDto(
                userNormalSubmissonDto.getCode().getCode(),
                userNormalSubmissonDto.getCode().getLangauge(),
                problemId,
                userName,
                LocalDate.now(),
                testCases.size(),
                totalPassed
        );
        utility.convertResponse(normalSubClient.addCodeBase(codeSave), new TypeReference<>() {});

//        return new ResponseEntity<>(response, HttpStatus.OK);




        return  response;

    }

}



//workign code
//{
//        "username": "hardik",
//        "problemId": 55,
//        "code": {
//        "code": "import java.util.*;\n\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    String s1 = sc.next();\n    String s2 = sc.next();\n\n    int m = s1.length();\n    int n = s2.length();\n\n    int[][] dp = new int[m + 1][n + 1];\n\n    for (int i = 1; i <= m; i++) {\n      for (int j = 1; j <= n; j++) {\n        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {\n          dp[i][j] = dp[i - 1][j - 1] + 1;\n        } else {\n          dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);\n        }\n      }\n    }\n\n    System.out.println(dp[m][n]);\n  }\n}",
//        "langauge": "java",
//        "input": "abcabcaa acbacba"
//        }
//        }