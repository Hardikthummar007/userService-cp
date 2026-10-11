package com.example.userService.battle.dto;

/**
 * Message payload sent to /user/queue/match when two players are paired.
 */
public class MatchFoundDto {

    private String type = "MATCH_FOUND";
    private Long battleId;
    private String opponent;

    public MatchFoundDto() {
    }

    public MatchFoundDto(Long battleId, String opponent) {
        this.battleId = battleId;
        this.opponent = opponent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getBattleId() {
        return battleId;
    }

    public void setBattleId(Long battleId) {
        this.battleId = battleId;
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }
}
