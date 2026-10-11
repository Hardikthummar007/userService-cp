package com.example.userService.dto;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class contest {

    String contestName;

    LocalDate startingDate;
    LocalTime startingTime;
    LocalTime endingTime;


    Set<String> userid=new HashSet<>();


    List<Integer> problemId=new ArrayList<>();

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }

    public LocalDate getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(LocalDate startingDate) {
        this.startingDate = startingDate;
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

    public Set<String> getUserid() {
        return userid;
    }

    public void setUserid(Set<String> userid) {
        this.userid = userid;
    }

    public List<Integer> getProblemId() {
        return problemId;
    }

    public void setProblemId(List<Integer> problemId) {
        this.problemId = problemId;
    }
}
