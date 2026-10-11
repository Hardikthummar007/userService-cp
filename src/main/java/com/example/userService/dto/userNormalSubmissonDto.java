package com.example.userService.dto;

public class userNormalSubmissonDto {

    String  username;
    int problemId;
    codeDto code;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public codeDto getCode() {
        return code;
    }

    public void setCode(codeDto code) {
        this.code = code;
    }
}
