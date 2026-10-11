package com.example.userService.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

public class sampleIO {



    int sampleIo_Id;

    String testcaseInput;
    String  expectedOutput;

    problems problems;

    public String getTestcaseInput() {
        return testcaseInput;
    }

    public void setTestcaseInput(String testcaseInput) {
        this.testcaseInput = testcaseInput;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public int getSampleIo_Id() {
        return sampleIo_Id;
    }

    public void setSampleIo_Id(int sampleIo_Id) {
        this.sampleIo_Id = sampleIo_Id;
    }

    public com.example.userService.dto.problems getProblems() {
        return problems;
    }

    public void setProblems(com.example.userService.dto.problems problems) {
        this.problems = problems;
    }
}
