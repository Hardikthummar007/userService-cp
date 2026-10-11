package com.example.userService.dto;

import java.util.List;

public class optimzeSubDto {

    List<testCases> testCases;
    codeDto codeData;

    public optimzeSubDto() {
    }

    public optimzeSubDto(List<testCases> testCases, codeDto codeData) {
        this.testCases = testCases;
        this.codeData = codeData;
    }

    public List<testCases> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<testCases> testCases) {
        this.testCases = testCases;
    }

    public codeDto getCodeData() {
        return codeData;
    }

    public void setCodeData(codeDto codeData) {
        this.codeData = codeData;
    }
}
