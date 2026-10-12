package com.example.userService.battle.repo;

import com.example.userService.battle.model.Battle;
import com.example.userService.battle.model.BattleEndReason;
import com.example.userService.battle.model.BattleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {

    @Query("SELECT b FROM Battle b WHERE (b.player1 = :username OR b.player2 = :username) AND b.status = :status")
    Optional<Battle> findByPlayerAndStatus(@Param("username") String username, @Param("status") BattleStatus status);

    @Query("SELECT b FROM Battle b WHERE (b.player1 = :username OR b.player2 = :username) AND b.status IN (com.example.userService.battle.model.BattleStatus.WAITING, com.example.userService.battle.model.BattleStatus.ACTIVE)")
    Optional<Battle> findActiveOrWaitingByPlayer(@Param("username") String username);

    List<Battle> findByStatus(BattleStatus status);

    /**
     * CONCURRENCY DECISION: Atomic update to finish a battle.
     * Prevents race conditions between simultaneous submissions or timer expiration.
     * Only returns 1 if status was ACTIVE and was transitioned to FINISHED.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Battle b SET b.status = com.example.userService.battle.model.BattleStatus.FINISHED, b.winner = :winner, b.endReason = :endReason WHERE b.id = :id AND b.status = com.example.userService.battle.model.BattleStatus.ACTIVE")
    int finishBattleAtomic(@Param("id") Long id, @Param("winner") String winner, @Param("endReason") BattleEndReason endReason);
}
