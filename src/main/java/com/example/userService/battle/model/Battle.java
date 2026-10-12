package com.example.userService.battle.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing a 1v1 coding battle between two players.
 */
@Entity
@Table(name = "battles", indexes = {
        @Index(name = "idx_battles_status", columnList = "status"),
        @Index(name = "idx_battles_p1_status", columnList = "player1, status"),
        @Index(name = "idx_battles_p2_status", columnList = "player2, status")
})
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player1", nullable = false)
    private String player1;

    @Column(name = "player2", nullable = false)
    private String player2;

    @Column(name = "problem_id", nullable = false)
    private Integer problemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BattleStatus status = BattleStatus.WAITING;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "winner")
    private String winner;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_reason")
    private BattleEndReason endReason;

    @Column(name = "total_tests")
    private Integer totalTests;

    @Column(name = "p1_best_passed")
    private int p1BestPassed = 0;

    @Column(name = "p2_best_passed")
    private int p2BestPassed = 0;

    @Column(name = "p1_best_at")
    private Instant p1BestAt;

    @Column(name = "p2_best_at")
    private Instant p2BestAt;

    @Column(name = "p1_rating_change")
    private Integer p1RatingChange;

    @Column(name = "p2_rating_change")
    private Integer p2RatingChange;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Battle() {
    }

    public Battle(String player1, String player2, Integer problemId) {
        this.player1 = player1;
        this.player2 = player2;
        this.problemId = problemId;
        this.status = BattleStatus.WAITING;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public Integer getProblemId() {
        return problemId;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }

    public BattleStatus getStatus() {
        return status;
    }

    public void setStatus(BattleStatus status) {
        this.status = status;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public BattleEndReason getEndReason() {
        return endReason;
    }

    public void setEndReason(BattleEndReason endReason) {
        this.endReason = endReason;
    }

    public Integer getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(Integer totalTests) {
        this.totalTests = totalTests;
    }

    public int getP1BestPassed() {
        return p1BestPassed;
    }

    public void setP1BestPassed(int p1BestPassed) {
        this.p1BestPassed = p1BestPassed;
    }

    public int getP2BestPassed() {
        return p2BestPassed;
    }

    public void setP2BestPassed(int p2BestPassed) {
        this.p2BestPassed = p2BestPassed;
    }

    public Instant getP1BestAt() {
        return p1BestAt;
    }

    public void setP1BestAt(Instant p1BestAt) {
        this.p1BestAt = p1BestAt;
    }

    public Instant getP2BestAt() {
        return p2BestAt;
    }

    public void setP2BestAt(Instant p2BestAt) {
        this.p2BestAt = p2BestAt;
    }

    public Integer getP1RatingChange() {
        return p1RatingChange;
    }

    public void setP1RatingChange(Integer p1RatingChange) {
        this.p1RatingChange = p1RatingChange;
    }

    public Integer getP2RatingChange() {
        return p2RatingChange;
    }

    public void setP2RatingChange(Integer p2RatingChange) {
        this.p2RatingChange = p2RatingChange;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
