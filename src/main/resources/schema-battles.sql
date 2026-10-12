-- DDL script for 1v1 Battles feature
-- Table name: battles
-- Database: userService
-- Foreign keys reference user(user_name) matching existing user entity

CREATE TABLE IF NOT EXISTS battles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player1 VARCHAR(255) NOT NULL,
    player2 VARCHAR(255) NOT NULL,
    problem_id INT NOT NULL,
    status ENUM('WAITING', 'ACTIVE', 'FINISHED') NOT NULL DEFAULT 'WAITING',
    start_time DATETIME(3) NULL,
    end_time DATETIME(3) NULL,
    winner VARCHAR(255) NULL,
    end_reason ENUM('SOLVED', 'TIMEOUT', 'FORFEIT') NULL,
    total_tests INT NULL,
    p1_best_passed INT DEFAULT 0,
    p2_best_passed INT DEFAULT 0,
    p1_best_at DATETIME(3) NULL,
    p2_best_at DATETIME(3) NULL,
    p1_rating_change INT NULL,
    p2_rating_change INT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT chk_battles_distinct_players CHECK (player1 <> player2),
    CONSTRAINT fk_battles_player1 FOREIGN KEY (player1) REFERENCES user(user_name) ON DELETE CASCADE,
    CONSTRAINT fk_battles_player2 FOREIGN KEY (player2) REFERENCES user(user_name) ON DELETE CASCADE,
    CONSTRAINT fk_battles_winner FOREIGN KEY (winner) REFERENCES user(user_name) ON DELETE SET NULL,
    INDEX idx_battles_status (status),
    INDEX idx_battles_p1_status (player1, status),
    INDEX idx_battles_p2_status (player2, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
