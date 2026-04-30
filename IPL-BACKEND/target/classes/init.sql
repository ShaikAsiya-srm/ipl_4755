-- ============================================================
-- IPL Prediction App - H2 Init Script (clean schema)
-- All columns standardized to "points" (no "score" anywhere)
-- ============================================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(100),
    points     INT DEFAULT 0
);

-- Matches table
CREATE TABLE IF NOT EXISTS matches (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    team1               VARCHAR(50)  NOT NULL,
    team2               VARCHAR(50)  NOT NULL,
    logo1               VARCHAR(255),
    logo2               VARCHAR(255),
    stadium             VARCHAR(100),
    match_date          VARCHAR(30),
    time                VARCHAR(20),
    score1              VARCHAR(50),
    score2              VARCHAR(50),
    result              VARCHAR(255),
    is_today            BOOLEAN DEFAULT FALSE,
    actual_winner       VARCHAR(50),
    top_scorer          VARCHAR(100),
    top_bowler          VARCHAR(100),
    actual_total_sixes  VARCHAR(20),
    actual_total_runs   VARCHAR(20),
    toss_winner         VARCHAR(50),
    bat_first           VARCHAR(50)
);

-- Predictions table
CREATE TABLE IF NOT EXISTS predictions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    match_id       BIGINT NOT NULL,
    toss_winner    VARCHAR(50),
    bat_first      VARCHAR(50),
    player         VARCHAR(100),
    winner         VARCHAR(50),
    top_scorer     VARCHAR(100),
    top_bowler     VARCHAR(100),
    total_sixes    VARCHAR(20),
    total_runs     VARCHAR(20),
    predicted_team VARCHAR(50),
    points         INT DEFAULT 0,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pred_user  FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_pred_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_match UNIQUE (user_id, match_id)
);

-- Insert sample users (H2-compatible, no FROM dual)
INSERT INTO users (username, password, points)
    SELECT 'user1', 'pass1', 80
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user1');

INSERT INTO users (username, password, points)
    SELECT 'user2', 'pass2', 60
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user2');

INSERT INTO users (username, password, points)
    SELECT 'user3', 'pass3', 40
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user3');

INSERT INTO users (username, password, points)
    SELECT 'admin', 'admin123', 100
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, points)
    SELECT 'IPLFan', 'password123', 120
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'IPLFan');