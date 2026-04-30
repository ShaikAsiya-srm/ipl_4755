-- Oracle SQL Developer Script for IPL Prediction Application
-- Run as ipl_user (or grant appropriate privileges)
-- CREATE USER ipl_user IDENTIFIED BY password;
-- GRANT CREATE SESSION, CREATE TABLE, UNLIMITED TABLESPACE TO ipl_user;

-- ============================================================
-- DROP TABLES (predictions first, then matches, then users)
-- CASCADE CONSTRAINTS removes FK references before drop
-- ============================================================
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE predictions CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE matches CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE users CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

-- Drop sequences
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE user_seq';       EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE match_seq';      EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE prediction_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- ============================================================
-- CREATE SEQUENCES
-- ============================================================
CREATE SEQUENCE user_seq       START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE match_seq      START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE prediction_seq START WITH 1 INCREMENT BY 1;

-- ============================================================
-- CREATE TABLES  (all use "points", NOT "score")
-- ============================================================

CREATE TABLE users (
    id         NUMBER PRIMARY KEY,
    username   VARCHAR2(50)  UNIQUE NOT NULL,
    password   VARCHAR2(255) NOT NULL,
    email      VARCHAR2(100),
    points     NUMBER(5) DEFAULT 0,
    created_at TIMESTAMP DEFAULT SYSDATE
);

CREATE TABLE matches (
    id                  NUMBER PRIMARY KEY,
    team1               VARCHAR2(50)  NOT NULL,
    team2               VARCHAR2(50)  NOT NULL,
    logo1               VARCHAR2(255),
    logo2               VARCHAR2(255),
    stadium             VARCHAR2(100),
    match_date          VARCHAR2(30),
    time                VARCHAR2(20),
    score1              VARCHAR2(50),
    score2              VARCHAR2(50),
    result              VARCHAR2(255),
    is_today            NUMBER(1) DEFAULT 0,
    actual_winner       VARCHAR2(50),
    top_scorer          VARCHAR2(100),
    top_bowler          VARCHAR2(100),
    actual_total_sixes  VARCHAR2(20),
    actual_total_runs   VARCHAR2(20),
    toss_winner         VARCHAR2(50),
    bat_first           VARCHAR2(50),
    created_at          TIMESTAMP DEFAULT SYSDATE
);

CREATE TABLE predictions (
    id             NUMBER PRIMARY KEY,
    user_id        NUMBER NOT NULL,
    match_id       NUMBER NOT NULL,
    toss_winner    VARCHAR2(50),
    bat_first      VARCHAR2(50),
    player         VARCHAR2(100),
    winner         VARCHAR2(50),
    top_scorer     VARCHAR2(100),
    top_bowler     VARCHAR2(100),
    total_sixes    VARCHAR2(20),
    total_runs     VARCHAR2(20),
    predicted_team VARCHAR2(50),
    points         NUMBER(5) DEFAULT 0,
    created_at     TIMESTAMP DEFAULT SYSDATE,
    CONSTRAINT fk_pred_user  FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_pred_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_match UNIQUE (user_id, match_id)
);

-- ============================================================
-- INSERT SAMPLE DATA  (explicit column list — uses "points")
-- ============================================================
INSERT INTO users (id, username, password, points) VALUES (user_seq.NEXTVAL, 'user1',   'pass1',       80);
INSERT INTO users (id, username, password, points) VALUES (user_seq.NEXTVAL, 'user2',   'pass2',       60);
INSERT INTO users (id, username, password, points) VALUES (user_seq.NEXTVAL, 'user3',   'pass3',       40);
INSERT INTO users (id, username, password, points) VALUES (user_seq.NEXTVAL, 'admin',   'admin123',   100);
INSERT INTO users (id, username, password, points) VALUES (user_seq.NEXTVAL, 'IPLFan',  'password123',120);

INSERT INTO matches (id, team1, team2, logo1, logo2, stadium, match_date, time, is_today)
VALUES (
    match_seq.NEXTVAL,
    'Chennai Super Kings', 'Royal Challengers Bangalore',
    'https://upload.wikimedia.org/wikipedia/en/thumb/2/2e/Chennai_Super_Kings_Logo.svg/512px-Chennai_Super_Kings_Logo.svg.png',
    'https://upload.wikimedia.org/wikipedia/en/thumb/4/4b/Royal_Challengers_Bangalore_Logo.svg/512px-Royal_Challengers_Bangalore_Logo.svg.png',
    'Wankhede Stadium', '2026-04-20', '7:30 PM', 0
);

INSERT INTO matches (id, team1, team2, stadium, match_date, time, is_today)
VALUES (match_seq.NEXTVAL, 'Mumbai Indians', 'Kolkata Knight Riders', 'Eden Gardens', '2026-04-21', '8:00 PM', 0);

COMMIT;