CREATE TABLE IF NOT EXISTS `buffer_profiles`
(
    `object_id`   INT UNSIGNED NOT NULL DEFAULT '0',
    `scheme_name` VARCHAR(16)  NOT NULL DEFAULT 'default',
    `skills`      VARCHAR(200) NOT NULL,
    `last_used`   TINYINT(1)   NOT NULL DEFAULT 0
) ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

INSERT INTO buffer_profiles (object_id, scheme_name, skills, last_used)
VALUES (0, 'Mage', '1355,1391,1045,1040,1036,1062,1204,1035,1085,1078,1059,1303,273,365,276,264,267,268,304,349', 0),
       (0, 'Fighter', '1363,1390,1045,1040,1036,1035,1204,1062,1068,1077,1087,1240,1242,1268,1086,271,272,274,275,310',
        0);