package com.example.userService.battle.service;

import com.example.userService.dto.codeDto;
import com.example.userService.dto.optimzeSubDto;
import com.example.userService.dto.problems;
import com.example.userService.dto.testCases;
import com.example.userService.feign.problemClient;
import com.example.userService.utility.utility;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Isolated code judge service for 1v1 coding battles.
 *
 * CRITICAL RULE:
 * This judge MUST NEVER call normalSubClient.addNormalSub,
 * MUST NEVER call addCodeBase, and MUST NEVER modify user.problemSolvedId.
 * It is completely side-effect free.
 */
@Service
public class BattleJudgeService {

    private static final Logger logger = LoggerFactory.getLogger(BattleJudgeService.class);

    @Autowired
    private problemClient problemClient;

    @Autowired
    private utility utility;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${compiler.service.url:http://localhost:8083/compilerService/testing}")
    private String compilerServiceUrl;

    /**
     * Executes the submitted code against all test cases of the problem
     * without persisting normal submissions or modifying user statistics.
     */
    public Map<String, Object> judgeCodeWithoutSideEffects(int problemId, codeDto code) {
        // 1. Fetch problem and its full test cases
        problems problem = fetchProblemWithTestCases(problemId);
        List<testCases> testCases = problem != null ? problem.getTestCases() : List.of();

        if (testCases == null || testCases.isEmpty()) {
            logger.warn("No test cases found for problem ID {}", problemId);
            Map<String, Object> fallbackRes = new HashMap<>();
            fallbackRes.put("status", "error");
            fallbackRes.put("errorMessage", "No test cases configured for this problem");
            fallbackRes.put("totalTestCase", 0);
            fallbackRes.put("totalTestCasePassed", 0);
            fallbackRes.put("passedOrNot", false);
            return fallbackRes;
        }

        // 2. Prepare request for compilerService
        optimzeSubDto payload = new optimzeSubDto(testCases, code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<optimzeSubDto> entity = new HttpEntity<>(payload, headers);

        try {
            // 3. Call compilerService
            ResponseEntity<Map> response = restTemplate.exchange(
                    compilerServiceUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                body = new HashMap<>();
            } else {
                body = new HashMap<>(body); // Make mutable
            }

            int totalPassed = 0;
            if (body.get("totalTestCasePassed") instanceof Number) {
                totalPassed = ((Number) body.get("totalTestCasePassed")).intValue();
            }

            int totalTests = testCases.size();
            boolean passedAll = (totalPassed == totalTests && totalTests > 0);

            body.put("totalTestCase", totalTests);
            body.put("totalTestCasePassed", totalPassed);
            body.put("passedOrNot", passedAll);

            logger.info("Judged code for problem #{}: passed {}/{} (passedOrNot={})",
                    problemId, totalPassed, totalTests, passedAll);

            return body;

        } catch (Exception e) {
            logger.error("Error communicating with compilerService: {}", e.getMessage());
            Map<String, Object> errorRes = new HashMap<>();
            errorRes.put("status", "error");
            errorRes.put("errorMessage", "Compilation service unavailable: " + e.getMessage());
            errorRes.put("totalTestCase", testCases.size());
            errorRes.put("totalTestCasePassed", 0);
            errorRes.put("passedOrNot", false);
            return errorRes;
        }
    }

    private problems fetchProblemWithTestCases(int problemId) {
        try {
            return utility.convertResponse(
                    problemClient.get(problemId),
                    new TypeReference<problems>() {}
            );
        } catch (Exception e) {
            logger.error("Failed to fetch problem #{} from problemClient: {}", problemId, e.getMessage());
            return null;
        }
    }
}
