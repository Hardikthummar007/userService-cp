package com.example.userService.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import java.util.ArrayList;
import java.util.List;

public class topics {

    String topic;


    List<problems> problems=new ArrayList<>();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<com.example.userService.dto.problems> getProblems() {
        return problems;
    }

    public void setProblems(List<com.example.userService.dto.problems> problems) {
        this.problems = problems;
    }
}
