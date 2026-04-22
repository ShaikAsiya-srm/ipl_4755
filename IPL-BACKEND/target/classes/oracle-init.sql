-- Oracle SQL Developer Script for IPL Prediction Application
-- Create user (run as SYSTEM user)
-- CREATE USER ipl_user IDENTIFIED BY password;
-- GRANT CREATE SESSION TO ipl_user;
-- GRANT CREATE TABLE TO ipl_user;
-- GRANT UNLIMITED TABLESPACE TO ipl_user;

-- Connect as ipl_user before executing the below statements

-- Drop existing tables if they exist
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE predictions';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE matches';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE users';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

-- Drop sequences
BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE user_seq';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE match_seq';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE prediction_seq';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

-- Create Sequences for Auto-Increment
CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE match_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE prediction_seq START WITH 1 INCREMENT BY 1;

-- Create Users Table
CREATE TABLE users (
    id NUMBER PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(255) NOT NULL,
    score NUMBER(5) DEFAULT 0,
    created_at TIMESTAMP DEFAULT SYSDATE
);

-- Create Matches Table
CREATE TABLE matches (
    id NUMBER PRIMARY KEY,
    team1 VARCHAR2(50) NOT NULL,
    team2 VARCHAR2(50) NOT NULL,
    logo1 VARCHAR2(255),
    logo2 VARCHAR2(255),
    stadium VARCHAR2(100),
    match_date VARCHAR2(30),
    time VARCHAR2(20),
    is_today NUMBER(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT SYSDATE
);

-- Create Predictions Table
CREATE TABLE predictions (
    id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    match_id NUMBER NOT NULL,
    toss_winner VARCHAR2(50),
    bat_first VARCHAR2(50),
    player VARCHAR2(100),
    created_at TIMESTAMP DEFAULT SYSDATE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (match_id) REFERENCES matches(id)
);

-- Insert Sample Data into Users
INSERT INTO users VALUES (user_seq.NEXTVAL, 'user1', 'pass1', 80, SYSDATE);
INSERT INTO users VALUES (user_seq.NEXTVAL, 'user2', 'pass2', 60, SYSDATE);
INSERT INTO users VALUES (user_seq.NEXTVAL, 'user3', 'pass3', 40, SYSDATE);

-- Insert Sample Data into Matches
INSERT INTO matches VALUES (
    match_seq.NEXTVAL,
    'CSK', 'RCB',
    'https://upload.wikimedia.org/wikipedia/en/thumb/2/2e/Chennai_Super_Kings_Logo.svg/512px-Chennai_Super_Kings_Logo.svg.png',
    'https://upload.wikimedia.org/wikipedia/en/thumb/4/4b/Royal_Challengers_Bangalore_Logo.svg/512px-Royal_Challengers_Bangalore_Logo.svg.png',
    'Wankhede', '2026-04-20', '7:30 PM', 1, SYSDATE
);

INSERT INTO matches VALUES (
    match_seq.NEXTVAL,
    'MI', 'KKR',
    'https://upload.wikimedia.org/wikipedia/en/thumb/c/cd/Mumbai_Indians_Logo.svg/512px-Mumbai_Indians_Logo.svg.png',
    'https://upload.wikimedia.org/wikipedia/en/thumb/2/2c/Kolkata_Knight_Riders_Logo.svg/512px-Kolkata_Knight_Riders_Logo.svg.png',
    'Eden Gardens', '2026-04-21', '8:00 PM', 0, SYSDATE
);

COMMIT;