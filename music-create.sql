CREATE SCHEMA IF NOT EXISTS study;

CREATE TABLE IF NOT EXISTS study.music (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO study.music (name) VALUES ('Moonlight Sonata');
INSERT INTO study.music (name) VALUES ('Symphony No 5');
INSERT INTO study.music (name) VALUES ('Happy Tree Friends');