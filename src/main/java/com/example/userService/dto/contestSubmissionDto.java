package com.example.userService.dto;

import java.time.LocalTime;
//used when i need to run code
//when i need to run code i will pass this dto
public class contestSubmissionDto {
    String userId;
    String contestId;
    int problemNo;

    codeDto code;
    LocalTime endingTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContestId() {
        return contestId;
    }

    public void setContestId(String contestId) {
        this.contestId = contestId;
    }

    public int getProblemNo() {
        return problemNo;
    }

    public void setProblemNo(int problemNo) {
        this.problemNo = problemNo;
    }

    public codeDto getCode() {
        return code;
    }

    public void setCode(codeDto code) {
        this.code = code;
    }

    public LocalTime getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(LocalTime endingTime) {
        this.endingTime = endingTime;
    }
}
