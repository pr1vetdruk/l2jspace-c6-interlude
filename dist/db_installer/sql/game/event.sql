-- Created by privetdruk for L2jSpace
DROP TABLE IF EXISTS event;
CREATE TABLE event
(
    id                    SMALLINT     NOT NULL PRIMARY KEY,
    type                  VARCHAR(32)  NOT NULL DEFAULT '',
    loading_order         INT(2)       NOT NULL DEFAULT 0,
    name                  VARCHAR(255) NOT NULL DEFAULT '',
    description           VARCHAR(255) NOT NULL DEFAULT '',
    registration_location VARCHAR(255) NOT NULL DEFAULT '',
    min_level             INT(4)       NOT NULL DEFAULT 0,
    max_level             INT(4)       NOT NULL DEFAULT 0,
    npc_id                INT(8)       NOT NULL DEFAULT 0,
    npc_x                 INT(11)      NOT NULL DEFAULT 0,
    npc_y                 INT(11)      NOT NULL DEFAULT 0,
    npc_z                 INT(11)      NOT NULL DEFAULT 0,
    npc_heading           INT(11)      NOT NULL DEFAULT 0,
    reward_id             INT(11)      NOT NULL DEFAULT 0,
    reward_amount         INT(11)      NOT NULL DEFAULT 0,
    teams_count           INT(4)       NOT NULL DEFAULT 0,
    time_registration     INT(11)      NOT NULL DEFAULT 0,
    duration_event        INT(11)      NOT NULL DEFAULT 0,
    min_players           INT(4)       NOT NULL DEFAULT 0,
    max_players           INT(4)       NOT NULL DEFAULT 0,
    delay_next_event      BIGINT       NOT NULL DEFAULT 0
) ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

ALTER TABLE event COMMENT = 'Events general settings';
ALTER TABLE event MODIFY id                    SMALLINT     COMMENT 'Event ID';
ALTER TABLE event MODIFY type                  VARCHAR(32)  COMMENT 'Event type (see enum Event)';
ALTER TABLE event MODIFY loading_order         VARCHAR(32)  COMMENT 'Loading order';
ALTER TABLE event MODIFY name                  VARCHAR(255) COMMENT 'Event name';
ALTER TABLE event MODIFY description           VARCHAR(255) COMMENT 'Event description';
ALTER TABLE event MODIFY registration_location VARCHAR(255) COMMENT 'Registration locations name';
ALTER TABLE event MODIFY min_level             INT(4)       COMMENT 'Minimum level for registration';
ALTER TABLE event MODIFY max_level             INT(4)       COMMENT 'Maximum level for registration';
ALTER TABLE event MODIFY npc_id                INT(8)       COMMENT 'Npc ID';
ALTER TABLE event MODIFY npc_x                 INT(11)      COMMENT 'Npc spawn position X';
ALTER TABLE event MODIFY npc_y                 INT(11)      COMMENT 'Npc spawn position Y';
ALTER TABLE event MODIFY npc_z                 INT(11)      COMMENT 'Npc spawn position Z';
ALTER TABLE event MODIFY npc_heading           INT(11)      COMMENT 'Npc heading';
ALTER TABLE event MODIFY reward_id             INT(11)      COMMENT 'Reward ID';
ALTER TABLE event MODIFY reward_amount         INT(11)      COMMENT 'Reward amount';
ALTER TABLE event MODIFY teams_count           INT(4)       COMMENT 'Teams count';
ALTER TABLE event MODIFY time_registration     INT(11)      COMMENT 'Time for registration';
ALTER TABLE event MODIFY duration_event        INT(11)      COMMENT 'Duration of the event';
ALTER TABLE event MODIFY min_players           INT(4)       COMMENT 'Min players for registration';
ALTER TABLE event MODIFY max_players           INT(4)       COMMENT 'Max players for registration';
ALTER TABLE event MODIFY delay_next_event      BIGINT       COMMENT 'Delay for next event';

INSERT INTO event (id, type, loading_order, name, description, registration_location, min_level, max_level,
                   npc_id, npc_x, npc_y, npc_z, npc_heading, reward_id, reward_amount,
                   teams_count, time_registration, duration_event, min_players, max_players, delay_next_event)
VALUES (1, 'CTF', 1, 'Capture the flag 1', 'Battle for Shutgartt', 'Giran', 1, 80, 70011, 82580, 148552, -3468, 16972, 8752, 1, 2, 5, 5, 2, 50, 300000),
       (2, 'CTF', 2, 'Capture the flag 2', 'Battle for Giran', 'Giran', 1, 80, 70011, 82580, 148552, -3468, 16972, 8752, 1, 2, 5, 5, 2, 50, 300000);