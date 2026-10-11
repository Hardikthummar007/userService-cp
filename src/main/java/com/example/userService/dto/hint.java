package com.example.userService.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

public class hint {


    int hint_id;

    String hint;
    problems problems;

    public int getHint_id() {
        return hint_id;
    }

    public void setHint_id(int hint_id) {
        this.hint_id = hint_id;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public com.example.userService.dto.problems getProblems() {
        return problems;
    }

    public void setProblems(com.example.userService.dto.problems problems) {
        this.problems = problems;
    }
}
