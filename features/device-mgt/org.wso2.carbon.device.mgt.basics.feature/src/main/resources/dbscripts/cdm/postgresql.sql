CREATE SEQUENCE DM_DEVICE_TYPE_seq;

CREATE TABLE IF NOT EXISTS DM_DEVICE_TYPE (
    ID   INTEGER DEFAULT NEXTVAL ('DM_DEVICE_TYPE_seq') NOT NULL,
    NAME VARCHAR(300) DEFAULT NULL,
    DEVICE_TYPE_META VARCHAR(20000) DEFAULT NULL,
    LAST_UPDATED_TIMESTAMP TIMESTAMP(0) NOT NULL,
    PROVIDER_TENANT_ID INTEGER DEFAULT 0,
    SHARED_WITH_ALL_TENANTS BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (ID)
);

CREATE INDEX IDX_DEVICE_TYPE ON DM_DEVICE_TYPE (NAME, PROVIDER_TENANT_ID);
CREATE INDEX IDX_DEVICE_NAME ON DM_DEVICE_TYPE (NAME);
CREATE INDEX IDX_DEVICE_TYPE_DEVICE_NAME ON DM_DEVICE_TYPE(ID, NAME);

CREATE SEQUENCE DM_GROUP_seq;

CREATE TABLE IF NOT EXISTS DM_GROUP (
    ID          INTEGER DEFAULT NEXTVAL ('DM_GROUP_seq') NOT NULL,
    GROUP_NAME  VARCHAR(100) DEFAULT NULL,
    DESCRIPTION TEXT         DEFAULT NULL,
    OWNER       VARCHAR(45)  DEFAULT NULL,
    TENANT_ID   INTEGER      DEFAULT 0,
    PRIMARY KEY (ID)
)
;

CREATE SEQUENCE DM_ROLE_GROUP_MAP_seq;

CREATE TABLE IF NOT EXISTS DM_ROLE_GROUP_MAP (
    ID        INTEGER DEFAULT NEXTVAL ('DM_ROLE_GROUP_MAP_seq') NOT NULL,
    GROUP_ID  INTEGER     DEFAULT NULL,
    ROLE      VARCHAR(45) DEFAULT NULL,
    TENANT_ID INTEGER     DEFAULT 0,
    PRIMARY KEY (ID),
    CONSTRAINT DM_ROLE_GROUP_MAP_DM_GROUP2 FOREIGN KEY (GROUP_ID)
     REFERENCES DM_GROUP (ID)
     ON DELETE CASCADE
     ON UPDATE CASCADE
)
;

CREATE SEQUENCE DM_DEVICE_seq;

CREATE TABLE IF NOT EXISTS DM_DEVICE (
    ID                    INTEGER DEFAULT NEXTVAL ('DM_DEVICE_seq') NOT NULL,
    DESCRIPTION           TEXT DEFAULT NULL,
    NAME                  VARCHAR(100) DEFAULT NULL,
    DEVICE_TYPE_ID        INTEGER DEFAULT NULL,
    DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL,
    LAST_UPDATED_TIMESTAMP TIMESTAMP(0) NOT NULL,
    TENANT_ID INTEGER DEFAULT 0,
    PRIMARY KEY (ID),
    CONSTRAINT fk_DM_DEVICE_DM_DEVICE_TYPE2 FOREIGN KEY (DEVICE_TYPE_ID)
     REFERENCES DM_DEVICE_TYPE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IDX_DM_DEVICE ON DM_DEVICE(TENANT_ID, DEVICE_TYPE_ID);
CREATE INDEX IDX_DM_DEVICE_TYPE_ID_DEVICE_IDENTIFICATION ON DM_DEVICE(TENANT_ID, DEVICE_TYPE_ID,DEVICE_IDENTIFICATION);

CREATE TABLE IF NOT EXISTS DM_DEVICE_PROPERTIES (
    DEVICE_TYPE_NAME VARCHAR(300) NOT NULL,
    DEVICE_IDENTIFICATION VARCHAR(300) NOT NULL,
    PROPERTY_NAME VARCHAR(100) DEFAULT 0,
    PROPERTY_VALUE VARCHAR(100) DEFAULT NULL,
    TENANT_ID VARCHAR(100),
    PRIMARY KEY (DEVICE_TYPE_NAME, DEVICE_IDENTIFICATION, PROPERTY_NAME, TENANT_ID)
);

CREATE SEQUENCE DM_DEVICE_GROUP_MAP_seq;

CREATE TABLE IF NOT EXISTS DM_DEVICE_GROUP_MAP (
    ID        INTEGER DEFAULT NEXTVAL ('DM_DEVICE_GROUP_MAP_seq') NOT NULL,
    DEVICE_ID INTEGER DEFAULT NULL,
    GROUP_ID  INTEGER DEFAULT NULL,
    TENANT_ID INTEGER DEFAULT 0,
    PRIMARY KEY (ID),
    CONSTRAINT fk_DM_DEVICE_GROUP_MAP_DM_DEVICE2 FOREIGN KEY (DEVICE_ID)
       REFERENCES DM_DEVICE (ID)
       ON DELETE CASCADE
       ON UPDATE CASCADE ,
    CONSTRAINT fk_DM_DEVICE_GROUP_MAP_DM_GROUP2 FOREIGN KEY (GROUP_ID)
       REFERENCES DM_GROUP (ID)
       ON DELETE CASCADE
       ON UPDATE CASCADE
)
;

CREATE SEQUENCE DM_OPERATION_seq;

CREATE TABLE IF NOT EXISTS DM_OPERATION (
    ID INTEGER DEFAULT NEXTVAL ('DM_OPERATION_seq') NOT NULL,
    TYPE VARCHAR(20) NOT NULL,
    CREATED_TIMESTAMP TIMESTAMP(0) NOT NULL,
    RECEIVED_TIMESTAMP TIMESTAMP(0) NULL,
    OPERATION_CODE VARCHAR(50) NOT NULL,
    INITIATED_BY VARCHAR(100) NULL,
    OPERATION_DETAILS BYTEA DEFAULT NULL,
    ENABLED BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (ID)
);

CREATE SEQUENCE DM_ENROLMENT_seq;

CREATE TABLE IF NOT EXISTS DM_ENROLMENT (
    ID INTEGER DEFAULT NEXTVAL ('DM_ENROLMENT_seq') NOT NULL,
    DEVICE_ID INTEGER NOT NULL,
    OWNER VARCHAR(50) NOT NULL,
    OWNERSHIP VARCHAR(45) DEFAULT NULL,
    STATUS VARCHAR(50) NULL,
    IS_TRANSFERRED BOOLEAN NOT NULL DEFAULT FALSE,
    DATE_OF_ENROLMENT TIMESTAMP(0) NULL DEFAULT NULL,
    DATE_OF_LAST_UPDATE TIMESTAMP(0) NULL DEFAULT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_ENROLMENT FOREIGN KEY (DEVICE_ID) REFERENCES
        DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IDX_ENROLMENT_FK_DEVICE_ID ON DM_ENROLMENT(DEVICE_ID);
CREATE INDEX IDX_ENROLMENT_DEVICE_ID_TENANT_ID ON DM_ENROLMENT(DEVICE_ID, TENANT_ID);
CREATE INDEX IDX_ENROLMENT_DEVICE_ID_TENANT_ID_STATUS ON DM_ENROLMENT(DEVICE_ID, TENANT_ID, STATUS);

CREATE SEQUENCE DM_ENROLMENT_OP_MAPPING_seq;

CREATE TABLE IF NOT EXISTS DM_ENROLMENT_OP_MAPPING (
    ID INTEGER DEFAULT NEXTVAL ('DM_ENROLMENT_OP_MAPPING_seq') NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    STATUS VARCHAR(50) NULL,
    PUSH_NOTIFICATION_STATUS VARCHAR(50) NULL,
    CREATED_TIMESTAMP INTEGER NOT NULL,
    UPDATED_TIMESTAMP INTEGER NOT NULL,
    PRIMARY KEY (ID)
    ,
    CONSTRAINT fk_dm_device_operation_mapping_device FOREIGN KEY (ENROLMENT_ID) REFERENCES
       DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_device_operation_mapping_operation FOREIGN KEY (OPERATION_ID) REFERENCES
       DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX fk_dm_device_operation_mapping_operation ON DM_ENROLMENT_OP_MAPPING (OPERATION_ID);
CREATE INDEX IDX_DM_ENROLMENT_OP_MAPPING ON DM_ENROLMENT_OP_MAPPING (ENROLMENT_ID,OPERATION_ID);
CREATE INDEX ID_DM_ENROLMENT_OP_MAPPING_UPDATED_TIMESTAMP ON DM_ENROLMENT_OP_MAPPING (UPDATED_TIMESTAMP);

ALTER TABLE DM_ENROLMENT_OP_MAPPING
    ADD OPERATION_CODE VARCHAR(50) NOT NULL,
    ADD    INITIATED_BY VARCHAR(100) NULL,
    ADD TYPE VARCHAR(20) NOT NULL,
    ADD DEVICE_ID INTEGER DEFAULT NULL,
    ADD DEVICE_TYPE VARCHAR(300) NOT NULL,
    ADD DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL,
    ADD TENANT_ID INTEGER DEFAULT 0;

CREATE INDEX IDX_ENROLMENT_OP_MAPPING ON DM_ENROLMENT_OP_MAPPING (UPDATED_TIMESTAMP);
CREATE INDEX IDX_EN_OP_MAPPING_EN_ID ON DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID);
CREATE INDEX IDX_EN_OP_MAPPING_OP_ID ON DM_ENROLMENT_OP_MAPPING(OPERATION_ID);
CREATE INDEX IDX_EN_OP_MAPPING_EN_ID_STATUS ON DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID, STATUS);

CREATE SEQUENCE DM_DEVICE_OPERATION_RESPONSE_seq;

CREATE TABLE IF NOT EXISTS DM_DEVICE_OPERATION_RESPONSE
(
    ID                 INTEGER   NOT NULL DEFAULT NEXTVAL ('DM_DEVICE_OPERATION_RESPONSE_seq'),
    ENROLMENT_ID       INTEGER   NOT NULL,
    OPERATION_ID       INTEGER   NOT NULL,
    EN_OP_MAP_ID       INTEGER   NOT NULL,
    OPERATION_RESPONSE VARCHAR(1024)      DEFAULT NULL,
    IS_LARGE_RESPONSE  BOOLEAN   NOT NULL DEFAULT FALSE,
    RECEIVED_TIMESTAMP TIMESTAMP(0) NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_dm_device_operation_response_enrollment FOREIGN KEY (ENROLMENT_ID) REFERENCES
        DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_device_operation_response_operation FOREIGN KEY (OPERATION_ID) REFERENCES
        DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_en_op_map_response FOREIGN KEY (EN_OP_MAP_ID) REFERENCES
        DM_ENROLMENT_OP_MAPPING (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IDX_DM_RES_RT ON DM_DEVICE_OPERATION_RESPONSE(RECEIVED_TIMESTAMP);
CREATE INDEX IDX_ENID_OP_ID ON DM_DEVICE_OPERATION_RESPONSE(OPERATION_ID, ENROLMENT_ID);
CREATE INDEX IDX_DM_EN_OP_MAP_ID ON DM_DEVICE_OPERATION_RESPONSE(EN_OP_MAP_ID);

CREATE TABLE IF NOT EXISTS DM_DEVICE_OPERATION_RESPONSE_LARGE
(
    ID                    INTEGER   NOT NULL,
    OPERATION_RESPONSE    BYTEA     DEFAULT NULL,
    OPERATION_ID          INTEGER   NOT NULL,
    EN_OP_MAP_ID          INTEGER   NOT NULL,
    RECEIVED_TIMESTAMP    TIMESTAMP(0) NULL,
    DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL,
    CONSTRAINT fk_dm_device_operation_response_large FOREIGN KEY (ID) REFERENCES
        DM_DEVICE_OPERATION_RESPONSE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_en_op_map_response_large FOREIGN KEY (EN_OP_MAP_ID) REFERENCES
        DM_ENROLMENT_OP_MAPPING (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IDX_DM_RES_LRG_RT ON DM_DEVICE_OPERATION_RESPONSE_LARGE(RECEIVED_TIMESTAMP);
CREATE INDEX IDX_OP_RES_LARGE_EN_OP_MAP_ID ON DM_DEVICE_OPERATION_RESPONSE_LARGE(EN_OP_MAP_ID);

-- POLICY RELATED TABLES ---

CREATE SEQUENCE DM_PROFILE_seq;

CREATE  TABLE IF NOT EXISTS DM_PROFILE (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_PROFILE_seq') ,
    PROFILE_NAME VARCHAR(45) NOT NULL ,
    TENANT_ID INTEGER NOT NULL ,
    DEVICE_TYPE VARCHAR(300) NOT NULL ,
    CREATED_TIME TIMESTAMP(0) NOT NULL ,
    UPDATED_TIME TIMESTAMP(0) NOT NULL ,
    PRIMARY KEY (ID)
);


CREATE SEQUENCE DM_POLICY_seq;

CREATE  TABLE IF NOT EXISTS DM_POLICY (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_POLICY_seq') ,
    NAME VARCHAR(45) DEFAULT NULL ,
    DESCRIPTION VARCHAR(1000) NULL,
    TENANT_ID INTEGER NOT NULL ,
    PROFILE_ID INTEGER NOT NULL ,
    OWNERSHIP_TYPE VARCHAR(45) NULL,
    COMPLIANCE VARCHAR(100) NULL,
    PRIORITY INTEGER NOT NULL,
    ACTIVE INTEGER NOT NULL,
    UPDATED INTEGER NULL,
    PRIMARY KEY (ID) ,
    CONSTRAINT FK_DM_PROFILE_DM_POLICY
      FOREIGN KEY (PROFILE_ID )
          REFERENCES DM_PROFILE (ID )
          ON DELETE NO ACTION
          ON UPDATE NO ACTION
);


CREATE SEQUENCE DM_DEVICE_POLICY_seq;

CREATE  TABLE IF NOT EXISTS DM_DEVICE_POLICY (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_DEVICE_POLICY_seq') ,
    DEVICE_ID INTEGER NOT NULL ,
    ENROLMENT_ID INTEGER NOT NULL,
    DEVICE BYTEA NOT NULL,
    POLICY_ID INTEGER NOT NULL ,
    PRIMARY KEY (ID) ,
    CONSTRAINT FK_POLICY_DEVICE_POLICY
     FOREIGN KEY (POLICY_ID )
         REFERENCES DM_POLICY (ID )
         ON DELETE NO ACTION
         ON UPDATE NO ACTION,
    CONSTRAINT FK_DEVICE_DEVICE_POLICY
     FOREIGN KEY (DEVICE_ID )
         REFERENCES DM_DEVICE (ID )
         ON DELETE NO ACTION
         ON UPDATE NO ACTION
);


CREATE  TABLE IF NOT EXISTS DM_DEVICE_TYPE_POLICY (
    ID INTEGER NOT NULL ,
    DEVICE_TYPE VARCHAR(300) NOT NULL ,
    POLICY_ID INTEGER NOT NULL ,
    PRIMARY KEY (ID) ,
    CONSTRAINT FK_DEVICE_TYPE_POLICY
      FOREIGN KEY (POLICY_ID )
          REFERENCES DM_POLICY (ID )
          ON DELETE NO ACTION
          ON UPDATE NO ACTION
);


CREATE SEQUENCE DM_PROFILE_FEATURES_seq;

CREATE  TABLE IF NOT EXISTS DM_PROFILE_FEATURES (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_PROFILE_FEATURES_seq'),
    PROFILE_ID INTEGER NOT NULL,
    FEATURE_CODE VARCHAR(100) NOT NULL,
    DEVICE_TYPE VARCHAR(300) NOT NULL ,
    TENANT_ID INTEGER NOT NULL ,
    CONTENT BYTEA NULL DEFAULT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_PROFILE_DM_POLICY_FEATURES
        FOREIGN KEY (PROFILE_ID)
            REFERENCES DM_PROFILE (ID)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
);


CREATE SEQUENCE DM_ROLE_POLICY_seq;

CREATE  TABLE IF NOT EXISTS DM_ROLE_POLICY (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_ROLE_POLICY_seq') ,
    ROLE_NAME VARCHAR(45) NOT NULL ,
    POLICY_ID INTEGER NOT NULL ,
    PRIMARY KEY (ID) ,
    CONSTRAINT FK_ROLE_POLICY_POLICY
       FOREIGN KEY (POLICY_ID )
           REFERENCES DM_POLICY (ID )
           ON DELETE NO ACTION
           ON UPDATE NO ACTION
);


CREATE SEQUENCE DM_USER_POLICY_seq;

CREATE  TABLE IF NOT EXISTS DM_USER_POLICY (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_USER_POLICY_seq') ,
    POLICY_ID INTEGER NOT NULL ,
    USERNAME VARCHAR(45) NOT NULL ,
    PRIMARY KEY (ID) ,
    CONSTRAINT DM_POLICY_USER_POLICY
       FOREIGN KEY (POLICY_ID )
           REFERENCES DM_POLICY (ID )
           ON DELETE NO ACTION
           ON UPDATE NO ACTION
);


CREATE SEQUENCE DM_DEVICE_POLICY_APPLIED_seq;

CREATE  TABLE IF NOT EXISTS DM_DEVICE_POLICY_APPLIED (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_DEVICE_POLICY_APPLIED_seq') ,
    DEVICE_ID INTEGER NOT NULL ,
    ENROLMENT_ID INTEGER NOT NULL,
    POLICY_ID INTEGER NOT NULL ,
    POLICY_CONTENT BYTEA NULL ,
    TENANT_ID INTEGER NOT NULL,
    APPLIED SMALLINT NULL ,
    CREATED_TIME TIMESTAMP(0) NULL ,
    UPDATED_TIME TIMESTAMP(0) NULL ,
    APPLIED_TIME TIMESTAMP(0) NULL ,
    PRIMARY KEY (ID) ,
    CONSTRAINT FK_DM_POLICY_DEVCIE_APPLIED
     FOREIGN KEY (DEVICE_ID )
         REFERENCES DM_DEVICE (ID )
         ON DELETE NO ACTION
         ON UPDATE NO ACTION
);


CREATE SEQUENCE DM_CRITERIA_seq;

CREATE TABLE IF NOT EXISTS DM_CRITERIA (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_CRITERIA_seq'),
    TENANT_ID INTEGER NOT NULL,
    NAME VARCHAR(50) NULL,
    PRIMARY KEY (ID)
);


CREATE SEQUENCE DM_POLICY_CRITERIA_seq;

CREATE TABLE IF NOT EXISTS DM_POLICY_CRITERIA (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_POLICY_CRITERIA_seq'),
    CRITERIA_ID INTEGER NOT NULL,
    POLICY_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_CRITERIA_POLICY_CRITERIA
      FOREIGN KEY (CRITERIA_ID)
          REFERENCES DM_CRITERIA (ID)
          ON DELETE NO ACTION
          ON UPDATE NO ACTION,
    CONSTRAINT FK_POLICY_POLICY_CRITERIA
      FOREIGN KEY (POLICY_ID)
          REFERENCES DM_POLICY (ID)
          ON DELETE NO ACTION
          ON UPDATE NO ACTION
);


CREATE SEQUENCE DM_POLICY_CRITERIA_PROPERTIES_seq;

CREATE TABLE IF NOT EXISTS DM_POLICY_CRITERIA_PROPERTIES (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_POLICY_CRITERIA_PROPERTIES_seq'),
    POLICY_CRITERION_ID INTEGER NOT NULL,
    PROP_KEY VARCHAR(45) NULL,
    PROP_VALUE VARCHAR(100) NULL,
    CONTENT BYTEA NULL ,
    PRIMARY KEY (ID),
    CONSTRAINT FK_POLICY_CRITERIA_PROPERTIES
     FOREIGN KEY (POLICY_CRITERION_ID)
         REFERENCES DM_POLICY_CRITERIA (ID)
         ON DELETE CASCADE
         ON UPDATE NO ACTION
);


CREATE SEQUENCE DM_POLICY_COMPLIANCE_STATUS_seq;

CREATE TABLE IF NOT EXISTS DM_POLICY_COMPLIANCE_STATUS (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_POLICY_COMPLIANCE_STATUS_seq'),
    DEVICE_ID INTEGER NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    POLICY_ID INTEGER NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    STATUS INTEGER NULL,
    LAST_SUCCESS_TIME TIMESTAMP(0) NULL,
    LAST_REQUESTED_TIME TIMESTAMP(0) NULL,
    LAST_FAILED_TIME TIMESTAMP(0) NULL,
    ATTEMPTS INTEGER NULL,
    PRIMARY KEY (ID)
);


CREATE SEQUENCE DM_POLICY_CHANGE_MGT_seq;

CREATE TABLE IF NOT EXISTS DM_POLICY_CHANGE_MGT (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_POLICY_CHANGE_MGT_seq'),
    POLICY_ID INTEGER NOT NULL,
    DEVICE_TYPE VARCHAR(300) NOT NULL ,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (ID)
);


CREATE SEQUENCE DM_POLICY_COMPLIANCE_FEATURES_seq;

CREATE TABLE IF NOT EXISTS DM_POLICY_COMPLIANCE_FEATURES (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_POLICY_COMPLIANCE_FEATURES_seq'),
    COMPLIANCE_STATUS_ID INTEGER NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    FEATURE_CODE VARCHAR(100) NOT NULL,
    STATUS INTEGER NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_COMPLIANCE_FEATURES_STATUS
     FOREIGN KEY (COMPLIANCE_STATUS_ID)
         REFERENCES DM_POLICY_COMPLIANCE_STATUS (ID)
         ON DELETE NO ACTION
         ON UPDATE NO ACTION
);

CREATE SEQUENCE DM_APPLICATION_seq;

CREATE TABLE IF NOT EXISTS DM_APPLICATION (
    ID INTEGER DEFAULT NEXTVAL ('DM_APPLICATION_seq') NOT NULL,
    NAME VARCHAR(150) NOT NULL,
    APP_IDENTIFIER VARCHAR(150) NOT NULL,
    PLATFORM VARCHAR(50) DEFAULT NULL,
    CATEGORY VARCHAR(50) NULL,
    VERSION VARCHAR(50) NULL,
    TYPE VARCHAR(50) NULL,
    LOCATION_URL VARCHAR(100) DEFAULT NULL,
    IMAGE_URL VARCHAR(100) DEFAULT NULL,
    APP_PROPERTIES BYTEA NULL,
    MEMORY_USAGE INTEGER NULL,
    IS_ACTIVE BOOLEAN NOT NULL DEFAULT FALSE,
    DEVICE_ID INTEGER NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_dm_device FOREIGN KEY (DEVICE_ID) REFERENCES
      DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_APP_MAP_DM_ENROL FOREIGN KEY (ENROLMENT_ID) REFERENCES
      DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- END OF POLICY RELATED TABLES --

-- POLICY AND DEVICE GROUP MAPPING --

CREATE SEQUENCE DM_DEVICE_GROUP_POLICY_seq;

CREATE TABLE IF NOT EXISTS DM_DEVICE_GROUP_POLICY (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_DEVICE_GROUP_POLICY_seq'),
    DEVICE_GROUP_ID INTEGER NOT NULL,
    POLICY_ID INTEGER NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_GROUP_POLICY
      FOREIGN KEY (DEVICE_GROUP_ID)
          REFERENCES DM_GROUP (ID)
          ON DELETE CASCADE
          ON UPDATE CASCADE ,
    CONSTRAINT FK_DM_DEVICE_GROUP_DM_POLICY
      FOREIGN KEY (POLICY_ID)
          REFERENCES DM_POLICY (ID)
          ON DELETE CASCADE
          ON UPDATE CASCADE
);

-- END OF POLICY AND DEVICE GROUP MAPPING --

-- NOTIFICATION TABLES --

CREATE SEQUENCE DM_NOTIFICATION_seq;

CREATE TABLE IF NOT EXISTS DM_NOTIFICATION (
    NOTIFICATION_ID INTEGER DEFAULT NEXTVAL ('DM_NOTIFICATION_seq') NOT NULL,
    DEVICE_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NULL,
    TENANT_ID INTEGER NOT NULL,
    STATUS VARCHAR(10) NULL,
    DESCRIPTION VARCHAR(1000) NULL,
    LAST_UPDATED_TIMESTAMP TIMESTAMP(0) NOT NULL,
    PRIMARY KEY (NOTIFICATION_ID),
    CONSTRAINT fk_dm_device_notification FOREIGN KEY (DEVICE_ID) REFERENCES
       DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IDX_NOTF_UT ON DM_NOTIFICATION(LAST_UPDATED_TIMESTAMP);

-- END NOTIFICATION TABLES --

CREATE SEQUENCE DM_DEVICE_INFO_seq;

CREATE TABLE IF NOT EXISTS DM_DEVICE_INFO (
    ID INTEGER DEFAULT NEXTVAL ('DM_DEVICE_INFO_seq') NOT NULL,
    DEVICE_ID INTEGER NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    KEY_FIELD VARCHAR(45) NULL,
    VALUE_FIELD VARCHAR(100) NULL,
    PRIMARY KEY (ID)
    ,
    CONSTRAINT DM_DEVICE_INFO_DEVICE
      FOREIGN KEY (DEVICE_ID)
          REFERENCES DM_DEVICE (ID)
          ON DELETE NO ACTION
          ON UPDATE NO ACTION,
    CONSTRAINT DM_DEVICE_INFO_DEVICE_ENROLLMENT
      FOREIGN KEY (ENROLMENT_ID)
          REFERENCES DM_ENROLMENT (ID)
          ON DELETE NO ACTION
          ON UPDATE NO ACTION
);

CREATE INDEX IDX_DM_DEVICE_INFO_DID_EID_KFIELD ON DM_DEVICE_INFO(DEVICE_ID, ENROLMENT_ID, KEY_FIELD);

CREATE INDEX DM_DEVICE_INFO_DEVICE_idx ON DM_DEVICE_INFO (DEVICE_ID ASC);
CREATE INDEX DM_DEVICE_INFO_DEVICE_ENROLLMENT_idx ON DM_DEVICE_INFO (ENROLMENT_ID ASC);

CREATE SEQUENCE DM_DEVICE_LOCATION_seq;

CREATE TABLE IF NOT EXISTS DM_DEVICE_LOCATION (
    ID INTEGER DEFAULT NEXTVAL ('DM_DEVICE_LOCATION_seq') NOT NULL,
    DEVICE_ID INT NULL,
    ENROLMENT_ID INT NOT NULL,
    LATITUDE DOUBLE PRECISION NULL,
    LONGITUDE DOUBLE PRECISION NULL,
    STREET1 VARCHAR(255) NULL,
    STREET2 VARCHAR(45) NULL,
    CITY VARCHAR(45) NULL,
    ZIP VARCHAR(10) NULL,
    STATE VARCHAR(45) NULL,
    COUNTRY VARCHAR(45) NULL,
    GEO_HASH VARCHAR(45) NULL,
    UPDATE_TIMESTAMP BIGINT NOT NULL,
    PRIMARY KEY (ID)
    ,
    CONSTRAINT DM_DEVICE_LOCATION_DEVICE
      FOREIGN KEY (DEVICE_ID)
          REFERENCES DM_DEVICE (ID)
          ON DELETE NO ACTION
          ON UPDATE NO ACTION,
    CONSTRAINT DM_DEVICE_LOCATION_DM_ENROLLMENT
      FOREIGN KEY (ENROLMENT_ID)
          REFERENCES DM_ENROLMENT (ID)
          ON DELETE NO ACTION
          ON UPDATE NO ACTION
)
;

CREATE INDEX DM_DEVICE_LOCATION_DEVICE_idx ON DM_DEVICE_LOCATION (DEVICE_ID ASC);
CREATE INDEX DM_DEVICE_LOCATION_GEO_hashx ON DM_DEVICE_LOCATION (GEO_HASH ASC);
CREATE INDEX DM_DEVICE_LOCATION_DM_ENROLLMENT_idx ON DM_DEVICE_LOCATION (ENROLMENT_ID ASC);

CREATE SEQUENCE DM_DEVICE_DETAIL_seq;

CREATE TABLE IF NOT EXISTS DM_DEVICE_DETAIL (
    ID INTEGER NOT NULL DEFAULT NEXTVAL ('DM_DEVICE_DETAIL_seq'),
    DEVICE_ID INTEGER NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    DEVICE_MODEL VARCHAR(45) NULL,
    VENDOR VARCHAR(45) NULL,
    OS_VERSION VARCHAR(45) NULL,
    OS_BUILD_DATE VARCHAR(100) NULL,
    BATTERY_LEVEL DECIMAL(4) NULL,
    INTERNAL_TOTAL_MEMORY DECIMAL(30,3) NULL,
    INTERNAL_AVAILABLE_MEMORY DECIMAL(30,3) NULL,
    EXTERNAL_TOTAL_MEMORY DECIMAL(30,3) NULL,
    EXTERNAL_AVAILABLE_MEMORY DECIMAL(30,3) NULL,
    CONNECTION_TYPE VARCHAR(50) NULL,
    SSID VARCHAR(45) NULL,
    CPU_USAGE DECIMAL(5) NULL,
    TOTAL_RAM_MEMORY DECIMAL(30,3) NULL,
    AVAILABLE_RAM_MEMORY DECIMAL(30,3) NULL,
    PLUGGED_IN Boolean NULL,
    UPDATE_TIMESTAMP BIGINT NOT NULL,
    PRIMARY KEY (ID)
    ,
    CONSTRAINT FK_DM_DEVICE_DETAILS_DEVICE
        FOREIGN KEY (DEVICE_ID)
            REFERENCES DM_DEVICE (ID)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_ENROLMENT_DEVICE_DETAILS
        FOREIGN KEY (ENROLMENT_ID)
            REFERENCES DM_ENROLMENT (ID)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
)
;

CREATE INDEX FK_DM_DEVICE_DETAILS_DEVICE_idx ON DM_DEVICE_DETAIL (DEVICE_ID ASC);
CREATE INDEX FK_DM_ENROLMENT_DEVICE_DETAILS_idx ON DM_DEVICE_DETAIL (ENROLMENT_ID ASC);

-- METADATA TABLE --
CREATE TABLE IF NOT EXISTS DM_METADATA (
    METADATA_ID BIGSERIAL PRIMARY KEY,
    DATA_TYPE VARCHAR(16) NOT NULL,
    METADATA_KEY VARCHAR(128) NOT NULL,
    METADATA_VALUE VARCHAR(512) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    CONSTRAINT METADATA_KEY_TENANT_ID UNIQUE(METADATA_KEY, TENANT_ID)
);
-- END OF METADATA TABLE --

-- DASHBOARD RELATED VIEWS --

CREATE VIEW DEVICE_INFO_VIEW AS
SELECT
    DM_DEVICE.ID AS DEVICE_ID,
    DM_DEVICE.DEVICE_IDENTIFICATION,
    DM_DEVICE_TYPE.NAME AS PLATFORM,
    DM_ENROLMENT.OWNERSHIP,
    DM_ENROLMENT.STATUS AS CONNECTIVITY_STATUS,
    DM_DEVICE.TENANT_ID
FROM DM_DEVICE, DM_DEVICE_TYPE, DM_ENROLMENT
WHERE DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID AND DM_DEVICE.ID = DM_ENROLMENT.DEVICE_ID;

CREATE VIEW DEVICE_WITH_POLICY_INFO_VIEW AS
SELECT
    DEVICE_ID,
    POLICY_ID,
    STATUS AS IS_COMPLIANT
FROM DM_POLICY_COMPLIANCE_STATUS;

CREATE VIEW POLICY_COMPLIANCE_INFO AS
SELECT
    DEVICE_INFO_VIEW.DEVICE_ID,
    DEVICE_INFO_VIEW.DEVICE_IDENTIFICATION,
    DEVICE_INFO_VIEW.PLATFORM,
    DEVICE_INFO_VIEW.OWNERSHIP,
    DEVICE_INFO_VIEW.CONNECTIVITY_STATUS,
    COALESCE(DEVICE_WITH_POLICY_INFO_VIEW.POLICY_ID, -1) AS POLICY_ID,
    COALESCE(DEVICE_WITH_POLICY_INFO_VIEW.IS_COMPLIANT, -1) AS IS_COMPLIANT,
    DEVICE_INFO_VIEW.TENANT_ID
FROM
    DEVICE_INFO_VIEW
        LEFT JOIN
    DEVICE_WITH_POLICY_INFO_VIEW
    ON DEVICE_INFO_VIEW.DEVICE_ID = DEVICE_WITH_POLICY_INFO_VIEW.DEVICE_ID
ORDER BY DEVICE_INFO_VIEW.DEVICE_ID;

CREATE VIEW FEATURE_NON_COMPLIANCE_INFO AS
SELECT
    DM_DEVICE.ID AS DEVICE_ID,
    DM_DEVICE.DEVICE_IDENTIFICATION,
    DM_DEVICE_DETAIL.DEVICE_MODEL,
    DM_DEVICE_DETAIL.VENDOR,
    DM_DEVICE_DETAIL.OS_VERSION,
    DM_ENROLMENT.OWNERSHIP,
    DM_ENROLMENT.OWNER,
    DM_ENROLMENT.STATUS AS CONNECTIVITY_STATUS,
    DM_POLICY_COMPLIANCE_STATUS.POLICY_ID,
    DM_DEVICE_TYPE.NAME AS PLATFORM,
    DM_POLICY_COMPLIANCE_FEATURES.FEATURE_CODE,
    DM_POLICY_COMPLIANCE_FEATURES.STATUS AS IS_COMPLAINT,
    DM_DEVICE.TENANT_ID
FROM
    DM_POLICY_COMPLIANCE_FEATURES, DM_POLICY_COMPLIANCE_STATUS, DM_ENROLMENT, DM_DEVICE, DM_DEVICE_TYPE, DM_DEVICE_DETAIL
WHERE
    DM_POLICY_COMPLIANCE_FEATURES.COMPLIANCE_STATUS_ID = DM_POLICY_COMPLIANCE_STATUS.ID AND
    DM_POLICY_COMPLIANCE_STATUS.ENROLMENT_ID = DM_ENROLMENT.ID AND
    DM_POLICY_COMPLIANCE_STATUS.DEVICE_ID = DM_DEVICE.ID AND
    DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID AND
    DM_DEVICE.ID = DM_DEVICE_DETAIL.DEVICE_ID
ORDER BY TENANT_ID, DEVICE_ID;

-- END OF DASHBOARD RELATED VIEWS --
