-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    score INT DEFAULT 0,
    email VARCHAR(100)
);

-- Matches table
CREATE TABLE IF NOT EXISTS matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team1 VARCHAR(50) NOT NULL,
    team2 VARCHAR(50) NOT NULL,
    logo1 VARCHAR(255),
    logo2 VARCHAR(255),
    stadium VARCHAR(100),
    match_date VARCHAR(30),
    time VARCHAR(20),
    is_today BOOLEAN DEFAULT FALSE
);

-- Predictions table
CREATE TABLE IF NOT EXISTS predictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    match_id BIGINT NOT NULL,
    toss_winner VARCHAR(50),
    bat_first VARCHAR(50),
    player VARCHAR(100),
    winner VARCHAR(50),
    top_scorer VARCHAR(100),
    top_bowler VARCHAR(100),
    total_sixes VARCHAR(20),
    total_runs VARCHAR(20),
    predicted_team VARCHAR(50),
    points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_match FOREIGN KEY (match_id) REFERENCES matches(id),
    CONSTRAINT unique_user_match UNIQUE (user_id, match_id)
);

-- Insert sample users (only if not exists)
INSERT INTO users (username, password, score) SELECT 'user1', 'pass1', 80 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user1');
INSERT INTO users (username, password, score) SELECT 'user2', 'pass2', 60 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user2');
INSERT INTO users (username, password, score) SELECT 'user3', 'pass3', 40 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user3');
INSERT INTO users (username, password, score) SELECT 'admin', 'admin123', 100 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
INSERT INTO users (username, password, score) SELECT 'IPLFan', 'password123', 120 WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'IPLFan');