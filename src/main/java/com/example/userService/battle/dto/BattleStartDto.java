package com.example.userService.battle.dto;

/**
 * Payload broadcast to /topic/battle/{battleId} when the battle begins.
 */
public class BattleStartDto {

    private String type = "BATTLE_START";
    private Long battleId;
    private BattleProblemDto problem;
    private long endTime;
    private long serverNow;

    public BattleStartDto() {
    }

    public BattleStartDto(Long battleId, BattleProblemDto problem, long endTime, long serverNow) {
        this.battleId = battleId;
        this.problem = problem;
        this.endTime = endTime;
        this.serverNow = serverNow;
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

    public BattleProblemDto getProblem() {
        return problem;
    }

    public void setProblem(BattleProblemDto problem) {
        this.problem = problem;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getServerNow() {
        return serverNow;
    }

    public void setServerNow(long serverNow) {
        this.serverNow = serverNow;
    }
}
