package com.example.userService.dto;

import com.example.userService.dto.problemSolvedByUserInContestDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class userContestSubmissionRecord {


    int contestSubmissionId;

    String  userId;
    String contestId;
    LocalTime startingTime;
    LocalTime endingTime;
    int howManyTestCasePassed;
    LocalDate date;

    int howManyProblemSolved;

    List<Integer> problemsSolved= new ArrayList<>();

    public userContestSubmissionRecord(String userId, String contestId, LocalDate date) {
        this.userId = userId;
        this.contestId = contestId;
        this.date=date;
    }

    public int getHowManyProblemSolved() {
        return howManyProblemSolved;
    }

    public void setHowManyProblemSolved(int howManyProblemSolved) {
        this.howManyProblemSolved = howManyProblemSolved;
    }

    public userContestSubmissionRecord() {
    }



    public int getContestSubmissionId() {
        return contestSubmissionId;
    }

    public void setContestSubmissionId(int contestSubmissionId) {
        this.contestSubmissionId = contestSubmissionId;
    }

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

    public LocalTime getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(LocalTime startingTime) {
        this.startingTime = startingTime;
    }

    public LocalTime getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(LocalTime endingTime) {
        this.endingTime = endingTime;
    }

    public int getHowManyTestCasePassed() {
        return howManyTestCasePassed;
    }

    public void setHowManyTestCasePassed(int howManyTestCasePassed) {
        this.howManyTestCasePassed = howManyTestCasePassed;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<Integer> getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(List<Integer> problemsSolved) {
        this.problemsSolved = problemsSolved;
    }
}
