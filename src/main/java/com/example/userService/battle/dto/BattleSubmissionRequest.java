package com.example.userService.battle.dto;

import com.example.userService.dto.codeDto;

/**
 * Request body for submitting code during a battle.
 * POST /userService/battle/{battleId}/submit
 */
public class BattleSubmissionRequest {

    private codeDto code;

    public BattleSubmissionRequest() {
    }

    public BattleSubmissionRequest(codeDto code) {
        this.code = code;
    }

    public codeDto getCode() {
        return code;
    }

    public void setCode(codeDto code) {
        this.code = code;
    }
}
