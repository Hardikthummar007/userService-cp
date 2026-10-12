package com.example.userService.battle.dto;

import com.example.userService.dto.hint;
import com.example.userService.dto.problems;
import com.example.userService.dto.sampleIO;
import com.example.userService.dto.topics;

import java.util.ArrayList;
import java.util.List;

/**
 * Sanitized problem payload sent to battle participants.
 * CRITICAL RULE: Hidden evaluation test cases are completely excluded.
 */
public class BattleProblemDto {

    private int problemId;
    private String title;
    private String description;
    private int difficulty;
    private int inputLimit;
    private int outputLimit;
    private List<sampleIO> sampleIOS = new ArrayList<>();
    private List<hint> hints = new ArrayList<>();
    private List<topics> topics = new ArrayList<>();

    public BattleProblemDto() {
    }

    public static BattleProblemDto fromProblem(problems p) {
        BattleProblemDto dto = new BattleProblemDto();
        if (p != null) {
            dto.setProblemId(p.getProblem_id());
            dto.setTitle(p.getTittle());
            dto.setDescription(p.getDescription());
            dto.setDifficulty(p.getDifficulty());
            dto.setInputLimit(p.getInputLimit());
            dto.setOutputLimit(p.getOutputLimit());
            dto.setSampleIOS(p.getSampleIOS() != null ? p.getSampleIOS() : new ArrayList<>());
            dto.setHints(p.getHints() != null ? p.getHints() : new ArrayList<>());
            dto.setTopics(p.getTopics() != null ? p.getTopics() : new ArrayList<>());
        }
        return dto;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<topics> getTopics() {
        return topics;
    }

    public void setTopics(List<topics> topics) {
        this.topics = topics;
    }
}
