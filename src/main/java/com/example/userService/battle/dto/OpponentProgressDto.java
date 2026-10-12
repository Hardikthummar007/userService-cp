package com.example.userService.battle.dto;

/**
 * Sent to /user/{opponent}/queue/opponent to inform the opponent of test progress.
 * Intentionally contains ZERO code or test case details.
 */
public class OpponentProgressDto {

    private String type = "OPPONENT_SUBMITTED";
    private int passed;
    private int total;

    public OpponentProgressDto() {
    }

    public OpponentProgressDto(int passed, int total) {
        this.type = "OPPONENT_SUBMITTED";
        this.passed = passed;
        this.total = total;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPassed() {
        return passed;
    }

    public void setPassed(int passed) {
        this.passed = passed;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
