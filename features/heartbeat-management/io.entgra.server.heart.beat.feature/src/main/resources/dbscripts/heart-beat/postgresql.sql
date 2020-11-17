-- -----------------------------------------------------
-- Table SERVER_HEART_BEAT_EVENTS
-- -----------------------------------------------------
 CREATE TABLE IF NOT EXISTS SERVER_HEART_BEAT_EVENTS (
    ID INTEGER AUTO_INCREMENT NOT NULL,
    HOST_NAME VARCHAR(100)  NOT NULL,
    MAC VARCHAR(100)  NOT NULL,
    UUID VARCHAR(100)  NOT NULL,
	SERVER_PORT INTEGER  NOT NULL,
    LAST_UPDATED_TIMESTAMP TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ID)
);
