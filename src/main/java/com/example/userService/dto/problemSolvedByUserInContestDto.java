package com.example.userService.dto;

import jakarta.persistence.*;

import java.time.LocalDate;

public class problemSolvedByUserInContestDto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;
    String userId;
    int problemId;
    String contestId;
    LocalDate date;

    userContestSubmissionRecord contestSubmission;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getContestId() {
        return contestId;
    }

    public void setContestId(String contestId) {
        this.contestId = contestId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public userContestSubmissionRecord getContestSubmission() {
        return contestSubmission;
    }

    public void setContestSubmission(userContestSubmissionRecord contestSubmission) {
        this.contestSubmission = contestSubmission;
    }
}
