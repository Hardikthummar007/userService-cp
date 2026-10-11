package com.example.userService.dto;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

public class problems {
    int problem_id;

    String  tittle;
    String  description;
    int difficulty;

    int inputLimit;
    int outputLimit;

    List<sampleIO> sampleIOS=new ArrayList<>();


    List<hint> hints=new ArrayList<>();



    List<topics> topics=new ArrayList<>();


    List<testCases> testCases=new ArrayList<>();

    public int getProblem_id() {
        return problem_id;
    }

    public void setProblem_id(int problem_id) {
        this.problem_id = problem_id;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getInputLimit() {
        return inputLimit;
    }

    public void setInputLimit(int inputLimit) {
        this.inputLimit = inputLimit;
    }

    public int getOutputLimit() {
        return outputLimit;
    }

    public void setOutputLimit(int outputLimit) {
        this.outputLimit = outputLimit;
    }

    public List<sampleIO> getSampleIOS() {
        return sampleIOS;
    }

    public void setSampleIOS(List<sampleIO> sampleIOS) {
        this.sampleIOS = sampleIOS;
    }

    public List<hint> getHints() {
        return hints;
    }

    public void setHints(List<hint> hints) {
        this.hints = hints;
    }

    public List<com.example.userService.dto.topics> getTopics() {
        return topics;
    }

    public void setTopics(List<com.example.userService.dto.topics> topics) {
        this.topics = topics;
    }

    public List<com.example.userService.dto.testCases> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<com.example.userService.dto.testCases> testCases) {
        this.testCases = testCases;
    }
}


//problem add req for post men
//{
//        "tittle": "Fibonacci Series",
//        "description": "Print nth Fibonacci number.",
//        "difficulty": 2,
//        "inputLimit": 100,
//        "outputLimit": 100,
//        "sampleIOS": [
//        { "input": "5", "output": "5" },
//        { "input": "8", "output": "21" }
//        ],
//        "hints": [
//        { "hint": "Use recursion or DP." },
//        { "hint": "Memoization helps." }
//        ],
//        "topics": [
//        { "topic": "recursion" },
//        { "topic": "dp" }
//        ],
//        "testCases": [
//        { "tc_input": "3", "tc_ouput": "2" },
//        { "tc_input": "4", "tc_ouput": "3" }
//        ]
//        }