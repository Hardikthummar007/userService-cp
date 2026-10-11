package com.example.userService.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class codeBaseNormalSubmissionDto {


    int id;


    private String code;
    private String language;
    private int problemId;
    private String username;
    private LocalDate submissionTime;

    private int totalTestCases;
    private int passedTestCases;

    public int getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(int totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public int getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(int passedTestCases) {
        this.passedTestCases = passedTestCases;
    }



    public codeBaseNormalSubmissionDto(String code, String language, int problemId, String username, LocalDate submissionTime, int totalTestCases, int passedTestCases) {
        this.code = code;
        this.language = language;
        this.problemId = problemId;
        this.username = username;
        this.submissionTime = submissionTime;
        this.totalTestCases = totalTestCases;
        this.passedTestCases = passedTestCases;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public LocalDate getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(LocalDate submissionTime) {
        this.submissionTime = submissionTime;
    }
}