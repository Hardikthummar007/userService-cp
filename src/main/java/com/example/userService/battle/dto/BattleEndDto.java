package com.example.userService.battle.dto;

/**
 * Broadcasted to /topic/battle/{battleId} when the battle ends
 * (either by SOLVED, TIMEOUT, or FORFEIT).
 */
public class BattleEndDto {

    private String type = "BATTLE_END";
    private Long battleId;
    private String winner;
    private String reason;

    public BattleEndDto() {
    }

    public BattleEndDto(Long battleId, String winner, String reason) {
        this.type = "BATTLE_END";
        this.battleId = battleId;
        this.winner = winner;
        this.reason = reason;
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

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
