-- Created by privetdruk for L2jSpace
DROP TABLE IF EXISTS ctf_team_setting;
CREATE TABLE ctf_team_setting
(
    event_id                 SMALLINT     NOT NULL COMMENT 'Event ID',
    id                       INT(4)       NOT NULL DEFAULT 0 COMMENT 'Team ID',
    name                     VARCHAR(255) NOT NULL DEFAULT '' COMMENT 'Team name',
    name_color               INT(11)      NOT NULL DEFAULT 0 COMMENT 'Name color',
    offset                   INT(11)      NOT NULL DEFAULT 300 COMMENT 'Displacement of the player after teleporting to the event.',
    position_x               INT(11)      NOT NULL DEFAULT 0 COMMENT 'X-axis team spawn coordinates',
    position_y               INT(11)      NOT NULL DEFAULT 0 COMMENT 'Y-axis team spawn coordinates',
    position_z               INT(11)      NOT NULL DEFAULT 0 COMMENT 'Z-axis team spawn coordinates',
    flag_position_x          INT(11)      NOT NULL DEFAULT 0 COMMENT 'X-axis flag spawn coordinates',
    flag_position_y          INT(11)      NOT NULL DEFAULT 0 COMMENT 'Y-axis flag spawn coordinates',
    flag_position_z          INT(11)      NOT NULL DEFAULT 0 COMMENT 'Z-axis flag spawn coordinates',
    flag_item_id             INT          NOT NULL DEFAULT 6718 COMMENT 'Flag item ID',
    flag_npc_id              INT          NOT NULL DEFAULT 35062 COMMENT 'Flag npc ID',
    throne_npc_id            INT          NOT NULL DEFAULT 32027 COMMENT 'Throne npc ID',
    offset_throne_position_z INT(11)      NOT NULL DEFAULT 10 COMMENT 'Shifting the position of the appearance of the throne along the Z axis',
    primary key (event_id, id)
) ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

ALTER TABLE ctf_team_setting COMMENT = 'CTF teams settings';
ALTER TABLE ctf_team_setting MODIFY event_id                 SMALLINT      COMMENT 'Event ID';
ALTER TABLE ctf_team_setting MODIFY id                       INT(4) COMMENT 'Team ID';
ALTER TABLE ctf_team_setting MODIFY name                     VARCHAR(255) COMMENT 'Team name';
ALTER TABLE ctf_team_setting MODIFY name_color               INT(11) COMMENT 'Team color';
ALTER TABLE ctf_team_setting MODIFY position_x               INT(11) COMMENT 'Team spawn position X';
ALTER TABLE ctf_team_setting MODIFY position_y               INT(11) COMMENT 'Team spawn position Y';
ALTER TABLE ctf_team_setting MODIFY position_z               INT(11) COMMENT 'Team spawn position Z';
ALTER TABLE ctf_team_setting MODIFY flag_position_x          INT(11) COMMENT 'Flag spawn position X';
ALTER TABLE ctf_team_setting MODIFY flag_position_y          INT(11) COMMENT 'Flag spawn position Y';
ALTER TABLE ctf_team_setting MODIFY flag_position_z          INT(11) COMMENT 'Flag spawn position Z';
ALTER TABLE ctf_team_setting MODIFY flag_item_id             INT COMMENT 'Flag item ID';
ALTER TABLE ctf_team_setting MODIFY flag_npc_id              INT COMMENT 'Flag npc ID';
ALTER TABLE ctf_team_setting MODIFY throne_npc_id            INT COMMENT 'Throne npc ID';
ALTER TABLE ctf_team_setting MODIFY offset_throne_position_z INT(11) COMMENT 'Offset throne position on Z-axis';

INSERT INTO ctf_team_setting (event_id, id, name, name_color,
                              offset, position_x, position_y, position_z,
                              flag_position_x, flag_position_y, flag_position_z,
                              flag_item_id, flag_npc_id, throne_npc_id, offset_throne_position_z)
VALUES (1, 1, 'Blue', 255, 300, 87357, -145722, -1288, 87358, -145979, -1291, 6718, 35062, 32027, 10),
       (1, 2, 'Red', 16711680, 300, 87351, -139984, -1536,  87359, -139584, -1536, 6718, 35062, 32027, 10),
       (2, 1, 'Blue', 255, 300, 87357, -145722, -1288, 87358, -145979, -1291, 6718, 35062, 32027, 10),
       (2, 2, 'Red', 16711680, 300, 87351, -139984, -1536,  87359, -139584, -1536, 6718, 35062, 32027, 10);