package com.example.userService.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class sampleIO {

    private int sampleIo_Id;

    @JsonProperty("input")
    @JsonAlias({"input", "testcaseInput", "testcase_input"})
    private String input;

    @JsonProperty("output")
    @JsonAlias({"output", "expectedOutput", "expected_output"})
    private String output;

    @JsonIgnore
    private problems problems;

    public sampleIO() {
    }

    public int getSampleIo_Id() {
        return sampleIo_Id;
    }

    public void setSampleIo_Id(int sampleIo_Id) {
        this.sampleIo_Id = sampleIo_Id;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @JsonIgnore
    public String getTestcaseInput() {
        return input;
    }

    @JsonIgnore
    public void setTestcaseInput(String testcaseInput) {
        this.input = testcaseInput;
    }

    @JsonIgnore
    public String getExpectedOutput() {
        return output;
    }

    @JsonIgnore
    public void setExpectedOutput(String expectedOutput) {
        this.output = expectedOutput;
    }

    public com.example.userService.dto.problems getProblems() {
        return problems;
    }

    public void setProblems(com.example.userService.dto.problems problems) {
        this.problems = problems;
    }
}
