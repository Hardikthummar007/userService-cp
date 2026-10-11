package com.example.userService.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

public class testCases {

    int testcase_id;

    String tc_input;
    String tc_ouput;


    problems problems;

    public int getTestcase_id() {
        return testcase_id;
    }

    public void setTestcase_id(int testcase_id) {
        this.testcase_id = testcase_id;
    }

    public String getTc_input() {
        return tc_input;
    }

    public void setTc_input(String tc_input) {
        this.tc_input = tc_input;
    }

    public String getTc_ouput() {
        return tc_ouput;
    }

    public void setTc_ouput(String tc_ouput) {
        this.tc_ouput = tc_ouput;
    }

    public com.example.userService.dto.problems getProblems() {
        return problems;
    }

    public void setProblems(com.example.userService.dto.problems problems) {
        this.problems = problems;
    }
}
