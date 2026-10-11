package com.example.userService.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

public class normalSubmission {


    int id;

    String userId;
    int problemId;
    String status;
    int howmanyTestCasePassed;
    String totaltime;
    String timeComplexity;
    String spaceComplexity;
    LocalDate submissionDate;

    public normalSubmission() {
    }

    public normalSubmission(String userName, int problemId, String status, int howmanyTestCasePassed, String totaltime, String timeComplexity, String spaceComplexity, LocalDate submissionDate) {
        this.userId=userName;
        this.problemId = problemId;
        this.status = status;
        this.howmanyTestCasePassed = howmanyTestCasePassed;
        this.totaltime = totaltime;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
        this.submissionDate=submissionDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHowmanyTestCasePassed() {
        return howmanyTestCasePassed;
    }

    public void setHowmanyTestCasePassed(int howmanyTestCasePassed) {
        this.howmanyTestCasePassed = howmanyTestCasePassed;
    }

    public String getTotaltime() {
        return totaltime;
    }

    public void setTotaltime(String totaltime) {
        this.totaltime = totaltime;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(String timeComplexity) {
        this.timeComplexity = timeComplexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public void setSpaceComplexity(String spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
    }


    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDate submissionDate) {
        this.submissionDate = submissionDate;
    }
}
