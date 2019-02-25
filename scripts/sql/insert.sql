
DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sys_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dept_code` varchar(20) DEFAULT NULL,
  `dept_name` varchar(100) DEFAULT NULL,
  `dept_description` varchar(200) DEFAULT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `dept_level` int(11) DEFAULT NULL,
  `dept_path` varchar(200) DEFAULT NULL,
  `dept_status` int(11) NOT NULL,
  `dept_sequence` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

INSERT INTO `sys_dept` VALUES (1,'1001','部门1',NULL,NULL,1,'部门1',1,NULL),(3,'1003','部门3',NULL,1,2,'部门1/部门3',1,NULL),(4,'1004','部门4',NULL,1,2,'部门1/部门4',0,NULL),(5,'1005','部门5',NULL,1,2,'部门1/部门5',0,NULL),(15,'123','123','123',1,2,'部门1/123',0,NULL),(16,'1100','部门6','叮叮',1,2,'部门1/部门6',1,NULL);


DROP TABLE IF EXISTS `sys_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sys_option` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `option_parent_id` int(11) DEFAULT NULL,
  `option_code` varchar(250) DEFAULT NULL,
  `option_name` varchar(250) DEFAULT NULL,
  `option_comment` varchar(250) DEFAULT NULL,
  `option_sequence` int(11) DEFAULT NULL,
  `option_icon` varchar(32) DEFAULT NULL,
  `option_url` varchar(250) DEFAULT NULL,
  `option_type` int(11) DEFAULT NULL,
  `option_flag` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;


INSERT INTO `sys_option` VALUES (1,NULL,'dept_level','一级部门A',NULL,1,NULL,NULL,1,0),(2,NULL,'dept_level','一级部门B',NULL,2,NULL,NULL,1,0),(3,NULL,'dept_level','一级部门C',NULL,3,NULL,NULL,1,0),(4,NULL,'dept_level','一级部门D',NULL,4,NULL,NULL,1,0),(5,1,'dept_level','二级部门E',NULL,1,NULL,NULL,2,0),(6,1,'dept_level','二级部门F',NULL,2,NULL,NULL,2,0),(7,5,'dept_level','三级部门G',NULL,1,NULL,NULL,3,0);

DROP TABLE IF EXISTS `sys_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sys_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `permission_parent_id` varchar(32) DEFAULT NULL,
  `permission_url` varchar(200) DEFAULT NULL,
  `permission_class` varchar(100) DEFAULT NULL,
  `permission_code` varchar(100) DEFAULT NULL,
  `permission_name` varchar(50) DEFAULT NULL,
  `permission_sequence` int(11) DEFAULT NULL,
  `permission_type` varchar(2) NOT NULL,
  `permission_status` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_key` varchar(30) DEFAULT NULL,
  `role_name` varchar(250) DEFAULT NULL,
  `role_description` varchar(200) DEFAULT NULL,
  `role_status` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
INSERT INTO `sys_role` VALUES (1,'admin','超级用户','显示所有菜单（包括未实现功能）',1),(2,'user','普通用户','现在84、85版本正常显示的版本（2018.06.08）',1),(3,'programer','开发者','备用（显示系统管理）',1),(4,'test','测试用户','供给Crrc人员查看的版本（屏蔽菜单、主数据）',1),(5,'crrc','crrc','crrc',1),(7,'test1','专属用户','提供给测试用户的超级用户',1);


DROP TABLE IF EXISTS `sys_role_permissions`;
CREATE TABLE `sys_role_permissions` (
  `permissions_id` bigint(20) NOT NULL,
  `sys_roles_id` bigint(20) NOT NULL,
  PRIMARY KEY (`sys_roles_id`,`permissions_id`) USING BTREE,
  KEY `fk_sys_role_permissions_permissions_id` (`permissions_id`) USING BTREE,
  CONSTRAINT `fk_sys_role_permissions_permissions_id` FOREIGN KEY (`permissions_id`) REFERENCES `sys_permission` (`id`),
  CONSTRAINT `fk_sys_role_permissions_sys_roles_id` FOREIGN KEY (`sys_roles_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
INSERT INTO `sys_role_permissions` VALUES (1,1),(1,3),(1,4),(1,6),(2,1),(2,3),(2,4),(2,6),(3,1),(3,3),(3,4),(3,6),(4,1),(4,3),(4,4),(4,6),(5,1),(5,3),(5,4),(5,6),(6,1),(6,3),(6,4),(6,6),(7,1),(7,3),(7,4),(7,6),(8,1),(8,3),(8,4),(8,6),(9,3),(9,4),(9,6),(10,1),(10,3),(10,4),(10,6),(12,1),(12,3),(12,4),(12,6),(13,1),(13,3),(13,4),(13,6),(14,1),(14,3),(14,4),(14,6),(15,1),(15,3),(15,4),(15,6),(17,3),(17,4),(17,6),(18,3),(18,4),(18,6),(19,3),(19,4),(19,6),(20,3),(20,4),(20,6),(21,3),(21,4),(21,6),(28,1),(28,3),(28,6),(28,7),(31,1),(31,3),(31,6),(31,7),(32,1),(32,3),(32,6),(32,7),(33,1),(33,3),(33,6),(33,7),(34,1),(34,3),(34,6),(34,7),(35,1),(35,3),(35,6),(35,7),(38,1),(38,3),(38,6),(38,7),(39,1),(39,3),(39,6),(39,7),(41,1),(41,3),(41,6),(41,7),(47,3),(47,6),(48,1),(48,3),(48,6),(49,1),(49,3),(49,6),(50,1),(50,3),(50,4),(50,6),(51,1),(51,3),(51,6),(51,7),(52,1),(52,3),(52,6),(52,7),(53,1),(53,3),(53,6),(53,7),(54,1),(54,3),(54,6),(54,7),(55,1),(55,3),(55,4),(55,6),(56,1),(56,3),(56,4),(56,6),(57,1),(57,3),(57,4),(57,6),(68,1),(68,3),(68,4),(68,6),(72,1),(72,3),(72,4),(72,6),(74,3),(74,4),(74,7),(75,3),(75,4),(75,7),(78,3),(78,4),(78,7),(79,3),(79,4),(80,3),(80,4),(80,7),(81,3),(81,4),(81,7),(82,5),(83,3),(83,4),(83,7),(84,3),(84,4),(85,3),(85,4),(86,1),(86,3),(86,4),(86,7),(86,8),(87,1),(87,3),(87,4),(87,7),(87,8),(88,1),(88,3),(88,4),(88,7),(88,8),(89,3),(89,4),(90,3),(90,4),(91,3),(91,4),(92,3),(92,4),(93,3),(93,4),(94,3),(94,4),(95,3),(95,4),(96,3),(96,4),(97,3),(97,4),(97,7),(99,1),(100,1);


DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(45) DEFAULT NULL,
  `user_realname` varchar(45) DEFAULT NULL,
  `user_password` varchar(128) DEFAULT NULL,
  `user_workno` varchar(45) DEFAULT NULL,
  `user_mobile` varchar(32) DEFAULT NULL,
  `user_email` varchar(250) DEFAULT NULL,
  `user_status` int(11) DEFAULT NULL,
  `user_description` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8;
INSERT INTO `sys_user` VALUES (1,'admin','管理员','$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC','NO0001','123123123123','12sssssssssssssssssssssssssssssssssssss',1,'crrc'),(2,'test1','测试1','$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC','NO10002',NULL,NULL,1,NULL),(3,'test2','测试2','$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC','NO10003',NULL,NULL,1,NULL),(4,'test3','测试3','$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC','NO10004',NULL,NULL,1,NULL),(5,'test4','测试4','$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC','NO10005',NULL,NULL,1,NULL),(6,'JJJJack','zhang','$2a$10$qdbHL2PDBghgQ6EMqyeqXeIPeCt34xLhnpSVfUHE1IzAxR/ldjUE2',NULL,NULL,NULL,1,'本人'),(7,'haobo','郝波','$2a$10$xolJVdBXgat9dFIlI6dYu.IxpwK5L7FhoETC/4I7/u44jHY3Hh.wG','No00001',NULL,NULL,1,NULL),(8,'wangduo','王朵','$2a$10$808zHQjZN6YdSHRslopAhOQJdF2FFdLx5XZIpN8TCoSyVJvWdS0Sm','NO00002',NULL,NULL,1,NULL),(9,'huweimin','胡为民','$2a$10$808zHQjZN6YdSHRslopAhOQJdF2FFdLx5XZIpN8TCoSyVJvWdS0Sm','NO00003',NULL,NULL,1,NULL),(10,'liusi','刘思','$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC','NO00004',NULL,NULL,1,NULL),(11,'lishuxing','李书行','$2a$10$808zHQjZN6YdSHRslopAhOQJdF2FFdLx5XZIpN8TCoSyVJvWdS0Sm','NO00005',NULL,NULL,1,NULL),(12,'chenfei','陈斐','$2a$10$808zHQjZN6YdSHRslopAhOQJdF2FFdLx5XZIpN8TCoSyVJvWdS0Sm','NO00006',NULL,NULL,1,NULL),(13,'huangcheng','黄城','$2a$10$808zHQjZN6YdSHRslopAhOQJdF2FFdLx5XZIpN8TCoSyVJvWdS0Sm','NO00007',NULL,NULL,1,NULL),(14,'yuanji','袁吉','$2a$10$808zHQjZN6YdSHRslopAhOQJdF2FFdLx5XZIpN8TCoSyVJvWdS0Sm','NO00008',NULL,NULL,1,NULL),(15,'hufeifei','胡菲菲','$2a$10$808zHQjZN6YdSHRslopAhOQJdF2FFdLx5XZIpN8TCoSyVJvWdS0Sm','NO00009',NULL,NULL,1,NULL),(16,'xiaqingsong','夏青松','$2a$10$808zHQjZN6YdSHRslopAhOQJdF2FFdLx5XZIpN8TCoSyVJvWdS0Sm','NO00010',NULL,NULL,1,NULL),(18,'developer','开发','$2a$10$XetM39Gc9MmS3/EhyStPouBcAObbc4JcmKBPyjrSRCMzCghLmbhg6','No001',NULL,NULL,1,NULL),(19,'demo','demo','$2a$10$j9KYuh4iDs1sd1Zv.Fuqku8AwKkK1dyV7n4x6wVe1aMW19DctfZD.',NULL,NULL,NULL,1,NULL),(20,'NO1001',NULL,'$2a$10$LHd0idaL6Ekgw./XyrSMNuj/CP9Q/UftXHP7ProlaiR3rJlpAK8GW',NULL,'13735862584',NULL,1,NULL),(21,'no1001',NULL,'$2a$10$zK4.bKaGtmO7Dz80soZ39uZ0y3qReD9OSzq8wkAgsJyRLb7dD2WbW',NULL,'13735862584',NULL,1,NULL),(22,'no1001',NULL,'$2a$10$hJf72UAmBu2LeE7IiVCNI.TiMhtxvorwAiY43BtJ8op/X5LIy8LvC',NULL,'13735862584',NULL,1,NULL),(23,'no100111',NULL,'$2a$10$463e.A71d91OiI5rTRRm/.0CL9TSw6PIuPT16OGuKwizDgrXPl2GS',NULL,'13735862584',NULL,0,NULL),(25,'xiaowei',NULL,'$2a$10$znzk/pV3AsMpMKQVKvtdy.3UYL3OIM.y/wTR9k7dVdMKqVdgJw5AO',NULL,'13735862584',NULL,1,NULL);



DROP TABLE IF EXISTS `sys_user_depts`;
CREATE TABLE `sys_user_depts` (
  `depts_id` bigint(20) NOT NULL,
  `sys_users_id` bigint(20) NOT NULL,
  PRIMARY KEY (`sys_users_id`,`depts_id`),
  KEY `fk_sys_user_depts_depts_id` (`depts_id`),
  CONSTRAINT `fk_sys_user_depts_depts_id` FOREIGN KEY (`depts_id`) REFERENCES `sys_dept` (`id`),
  CONSTRAINT `fk_sys_user_depts_sys_users_id` FOREIGN KEY (`sys_users_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `sys_user_depts` VALUES (1,2),(3,1),(3,5);


DROP TABLE IF EXISTS `sys_user_roles`;

CREATE TABLE `sys_user_roles` (
  `roles_id` bigint(20) NOT NULL,
  `sys_users_id` bigint(20) NOT NULL,
  PRIMARY KEY (`sys_users_id`,`roles_id`),
  KEY `fk_sys_user_roles_roles_id` (`roles_id`),
  CONSTRAINT `fk_sys_user_roles_roles_id` FOREIGN KEY (`roles_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT `fk_sys_user_roles_sys_users_id` FOREIGN KEY (`sys_users_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `sys_user_roles` VALUES (1,1),(2,20),(5,21),(7,10),(7,22),(7,23),(7,25);


/* jhipster*/
DROP TABLE IF EXISTS `databasechangelog`;
CREATE TABLE `databasechangelog` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL,
  `CONTEXTS` varchar(255) DEFAULT NULL,
  `LABELS` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `databasechangeloglock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `databasechangeloglock` (
  `ID` int(11) NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `jhi_authority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jhi_authority` (
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `jhi_persistent_audit_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jhi_persistent_audit_event` (
  `event_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `principal` varchar(50) NOT NULL,
  `event_date` timestamp NULL DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`event_id`),
  KEY `idx_persistent_audit_event` (`principal`,`event_date`)
) ENGINE=InnoDB AUTO_INCREMENT=2492 DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `jhi_persistent_audit_evt_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jhi_persistent_audit_evt_data` (
  `event_id` bigint(20) NOT NULL,
  `name` varchar(150) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`event_id`,`name`),
  KEY `idx_persistent_audit_evt_data` (`event_id`),
  CONSTRAINT `fk_evt_pers_audit_evt_data` FOREIGN KEY (`event_id`) REFERENCES `jhi_persistent_audit_event` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `jhi_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jhi_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `login` varchar(50) NOT NULL,
  `password_hash` varchar(60) DEFAULT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `image_url` varchar(256) DEFAULT NULL,
  `activated` bit(1) NOT NULL,
  `lang_key` varchar(6) DEFAULT NULL,
  `activation_key` varchar(20) DEFAULT NULL,
  `reset_key` varchar(20) DEFAULT NULL,
  `created_by` varchar(50) NOT NULL,
  `created_date` timestamp NOT NULL,
  `reset_date` timestamp NULL DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `last_modified_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_user_login` (`login`),
  UNIQUE KEY `idx_user_login` (`login`),
  UNIQUE KEY `ux_user_email` (`email`),
  UNIQUE KEY `idx_user_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `jhi_user_authority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jhi_user_authority` (
  `user_id` bigint(20) NOT NULL,
  `authority_name` varchar(50) NOT NULL,
  PRIMARY KEY (`user_id`,`authority_name`),
  KEY `fk_authority_name` (`authority_name`),
  CONSTRAINT `fk_authority_name` FOREIGN KEY (`authority_name`) REFERENCES `jhi_authority` (`name`),
  CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `jhi_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

