-- user_center : schema day du (21 bang + view v_server_info) + data cau hinh server
-- KHONG kem account/nhan vat/log nap the.

-- MySQL dump 10.13  Distrib 5.6.49, for Win64 (x86_64)
--
-- Host: localhost    Database: user_center
-- ------------------------------------------------------
-- Server version	5.6.49

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `server_login_white`
--

DROP TABLE IF EXISTS `server_login_white`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `server_login_white` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `server_id` int(11) NOT NULL COMMENT '服务器id',
  `account_id` int(11) NOT NULL COMMENT '账号ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user_id` int(11) NOT NULL COMMENT '创建人Id',
  `create_user_name` varchar(50) NOT NULL COMMENT '创建人名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `s_a` (`server_id`,`account_id`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=260 DEFAULT CHARSET=utf8 COMMENT='服务器登录白名单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_recharge_log`
--

DROP TABLE IF EXISTS `t_recharge_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_recharge_log` (
  `log_date` datetime NOT NULL COMMENT '日志时间',
  `uid` varchar(45) DEFAULT NULL COMMENT '用户ID',
  `pid` bigint(20) DEFAULT NULL COMMENT '玩家ID',
  `cp_id` varchar(45) DEFAULT NULL COMMENT '商品ID',
  `channel_id` int(11) DEFAULT NULL COMMENT '渠道ID',
  `channel_app_id` int(11) DEFAULT NULL,
  `server_id` varchar(45) DEFAULT NULL COMMENT '服务器ID',
  `order_id` varchar(45) DEFAULT NULL COMMENT '订单号',
  `money` double DEFAULT NULL COMMENT '金额',
  `currency_type` varchar(45) DEFAULT NULL COMMENT '货币类型(如：USD)',
  `currency_unit` varchar(45) DEFAULT NULL COMMENT '货币单位(如:元)',
  `item_count` int(11) DEFAULT NULL COMMENT '发放道具数量(一般为钻石)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='充值日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_role_register_log`
--

DROP TABLE IF EXISTS `t_role_register_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_role_register_log` (
  `log_date` datetime DEFAULT NULL COMMENT '日志时间',
  `uid` varchar(45) DEFAULT NULL COMMENT '用户ID',
  `pid` bigint(20) DEFAULT NULL COMMENT '玩家ID',
  `channel_id` int(11) DEFAULT NULL COMMENT '渠道ID',
  `channel_app_id` int(11) DEFAULT NULL,
  `server_id` varchar(45) DEFAULT NULL COMMENT '服务器ID',
  `imei` varchar(100) DEFAULT NULL COMMENT '设备号',
  `device` varchar(45) DEFAULT NULL COMMENT '设备型号',
  `os_type` varchar(45) DEFAULT NULL COMMENT '系统类型',
  `os_ver` varchar(45) DEFAULT NULL COMMENT '系统版本'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色注册日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_activity_mail`
--

DROP TABLE IF EXISTS `t_s_activity_mail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_activity_mail` (
  `id` int(11) NOT NULL COMMENT '运营活动id  ',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `got_count` int(11) NOT NULL DEFAULT '1' COMMENT '活动领取次数',
  `reward` varchar(255) DEFAULT NULL COMMENT '奖励',
  `content` varchar(255) DEFAULT NULL COMMENT '活动奖励邮件的内容',
  `title` varchar(255) DEFAULT NULL COMMENT '活动奖励标题',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='运营问卷活动的活动与奖励的配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_channel_list`
--

DROP TABLE IF EXISTS `t_s_channel_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_channel_list` (
  `id` int(11) NOT NULL,
  `sign` varchar(45) NOT NULL,
  `remark` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_channel_switch`
--

DROP TABLE IF EXISTS `t_s_channel_switch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_channel_switch` (
  `channel` varchar(255) NOT NULL COMMENT '渠道',
  `switch_name` varchar(50) DEFAULT NULL COMMENT '渠道名',
  `switch_type` smallint(4) NOT NULL COMMENT '开关类型1礼包码2微信关注3邀请码',
  `open` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0关1开',
  `begin_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`channel`,`switch_type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='渠道开关';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_server_config`
--

DROP TABLE IF EXISTS `t_s_server_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_server_config` (
  `id` int(11) NOT NULL,
  `zoneId` int(11) DEFAULT '1',
  `web_host` varchar(512) DEFAULT NULL,
  `db_host` varchar(512) DEFAULT NULL,
  `db_user` varchar(32) DEFAULT NULL,
  `db_password` varchar(32) DEFAULT NULL,
  `data_host` varchar(512) DEFAULT NULL,
  `recharge_host` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='服务器配置表，配置了服务器的很多扩展管理信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_server_list`
--

DROP TABLE IF EXISTS `t_s_server_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_server_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '服务器ID',
  `name` varchar(64) NOT NULL DEFAULT '',
  `mark` tinyint(4) NOT NULL DEFAULT '0' COMMENT '[按位取]0:普通;1:新服;2:推荐',
  `address` varchar(128) NOT NULL DEFAULT '' COMMENT '服务器连接地址，IP:PORT',
  `recharge_ip` varchar(48) DEFAULT NULL COMMENT '充值服地址',
  `system` int(11) NOT NULL DEFAULT '0' COMMENT '系统类型：0：任意；1、IOS；2、Android',
  `max_login_count` mediumint(6) NOT NULL COMMENT '在线人数上限',
  `max_reg_count` mediumint(6) NOT NULL DEFAULT '20000' COMMENT '注册人数上限',
  `hot_threshold` smallint(4) NOT NULL DEFAULT '70' COMMENT '火爆阀值（注册上限的百分比）',
  `rec_threshold` smallint(4) NOT NULL DEFAULT '80' COMMENT '推荐阀值（在线上限的百分比）',
  `no_rec_threshold` smallint(4) NOT NULL DEFAULT '90' COMMENT '不可推荐阀值（注册上限的百分比）',
  `open_time` datetime DEFAULT NULL COMMENT '开服时间',
  `zone_id` int(1) NOT NULL DEFAULT '0' COMMENT '分区id',
  `upkeep_message` varchar(200) NOT NULL DEFAULT '' COMMENT '维护公告',
  `open_server_time` datetime DEFAULT NULL COMMENT '服务器维护时预计开启服务时间（维护结束时间）',
  `min_version` varchar(20) DEFAULT NULL COMMENT '最低版本号',
  `max_version` varchar(20) DEFAULT NULL COMMENT '最高版本号',
  `stride_server` varchar(200) DEFAULT NULL COMMENT '跨服地址',
  `stride_server_group` varchar(64) DEFAULT NULL COMMENT '跨服分组',
  `server_chat_group` varchar(64) DEFAULT NULL COMMENT '跨服聊天分组',
  `activity_server_group` varchar(64) DEFAULT NULL COMMENT '活动跨服分组',
  `cross_server` varchar(200) DEFAULT NULL COMMENT '跨服服务器的地址',
  `join_cross_guild_battle` int(11) NOT NULL DEFAULT '2' COMMENT '跨服帮派战参加数量',
  `join_cross_champions_battle` int(11) NOT NULL DEFAULT '8' COMMENT '跨服个人战参加数量',
  `gift_code` varchar(64) DEFAULT NULL,
  `game_port` int(11) DEFAULT NULL,
  `http_port` int(11) DEFAULT NULL,
  `recharge_port` int(11) DEFAULT NULL,
  `area_id` int(11) DEFAULT NULL,
  `db_game` varchar(256) DEFAULT NULL,
  `db_game_data` varchar(256) DEFAULT NULL,
  `db_game_log` varchar(256) DEFAULT NULL,
  `db_game_global_log` varchar(256) DEFAULT NULL,
  `max_connect` int(11) DEFAULT NULL,
  `server_country` int(11) DEFAULT NULL,
  `server_timezone` varchar(20) DEFAULT NULL,
  `is_test` tinyint(2) DEFAULT NULL,
  `is_rebate` tinyint(1) unsigned zerofill DEFAULT '0' COMMENT '是否是充值返利服',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1012 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_server_path`
--

DROP TABLE IF EXISTS `t_s_server_path`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_server_path` (
  `server_id` varchar(255) DEFAULT NULL,
  `server_path` varchar(255) DEFAULT NULL,
  `server_log_path` varchar(255) DEFAULT NULL,
  `log_db_ip` varchar(255) DEFAULT NULL,
  `log_db_name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_server_status`
--

DROP TABLE IF EXISTS `t_s_server_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_server_status` (
  `server_id` int(11) NOT NULL COMMENT '服务器Id',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `login_threshold` smallint(4) NOT NULL DEFAULT '0' COMMENT '登录阀值变化（百分比）',
  `reg_threshold` smallint(4) NOT NULL DEFAULT '0' COMMENT '注册阀值变化（百分比）',
  PRIMARY KEY (`server_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_server_switch`
--

DROP TABLE IF EXISTS `t_s_server_switch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_server_switch` (
  `server_id` int(11) NOT NULL COMMENT '服务器id',
  `switch_name` varchar(50) DEFAULT NULL COMMENT '开关名',
  `switch_type` smallint(4) NOT NULL COMMENT '开关类型1开关服2登录3充值4注册5礼包码6微信关注',
  `open` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0关1开',
  `begin_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`server_id`,`switch_type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_server_white_ips`
--

DROP TABLE IF EXISTS `t_s_server_white_ips`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_server_white_ips` (
  `serverId` int(11) NOT NULL DEFAULT '0',
  `is_test` tinyint(1) DEFAULT '0' COMMENT '1只看test服务器 0都可以看到',
  `white_ips` text,
  PRIMARY KEY (`serverId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_s_server_zone`
--

DROP TABLE IF EXISTS `t_s_server_zone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_s_server_zone` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分区id',
  `zone_name` varchar(100) NOT NULL COMMENT '分区名',
  `channel` varchar(1024) DEFAULT '' COMMENT '渠道',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='服务器分区表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_u_account`
--

DROP TABLE IF EXISTS `t_u_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_u_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '不同渠道userId可能重复，由我们系统自动分配一个唯一id',
  `user_id` varchar(64) NOT NULL DEFAULT '' COMMENT 'SDK方用户ID',
  `channel` varchar(64) NOT NULL COMMENT '渠道号',
  `name` varchar(64) DEFAULT NULL COMMENT '渠道方注册的帐号名称，不一定会有',
  `server_id` int(11) DEFAULT '0' COMMENT '最后一次登录的服务器ID',
  `sdk_name` varchar(32) NOT NULL COMMENT 'sdk名称，如：360，UC等',
  `internal` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否是内部帐号，1：是，0：否',
  `create_time` datetime NOT NULL COMMENT '帐号创建的日期',
  `login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `logout_time` datetime DEFAULT NULL COMMENT '最后登出时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id_sdk` (`user_id`,`channel`) USING BTREE,
  KEY `idx_createdate` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=299 DEFAULT CHARSET=utf8 COMMENT='玩家帐号表，记录了玩家的帐号信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_u_ban`
--

DROP TABLE IF EXISTS `t_u_ban`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_u_ban` (
  `account_id` int(11) NOT NULL COMMENT '账号id，与t_u_account表的id对应',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `reason` varchar(1024) NOT NULL COMMENT '原因，由gm录入',
  `operator_name` varchar(32) NOT NULL COMMENT '操作人员',
  `server_id` int(11) NOT NULL DEFAULT '0' COMMENT '服务器id（如果为0则是全服封号）',
  PRIMARY KEY (`account_id`,`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_u_equipment_code`
--

DROP TABLE IF EXISTS `t_u_equipment_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_u_equipment_code` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `equipment_code` varchar(64) DEFAULT NULL COMMENT '设备码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_u_gag`
--

DROP TABLE IF EXISTS `t_u_gag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_u_gag` (
  `account_id` int(11) NOT NULL COMMENT '账号id，与t_u_account表的id对应',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `reason` varchar(1024) NOT NULL COMMENT '原因，由gm录入',
  `operator_name` varchar(32) NOT NULL COMMENT '操作人员',
  PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_u_role`
--

DROP TABLE IF EXISTS `t_u_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_u_role` (
  `id` bigint(20) NOT NULL COMMENT '角色id',
  `account_id` int(11) NOT NULL COMMENT '我方帐号ID，对应t_u_account表格的id',
  `server_id` int(11) NOT NULL COMMENT '角色所在服务器ID，对应t_s_server_list表格的ID',
  `name` varchar(128) NOT NULL,
  `level` int(11) NOT NULL COMMENT '玩家等级',
  `vip_level` int(11) NOT NULL COMMENT 'vip等级',
  `head_id` int(11) NOT NULL COMMENT '头像id',
  `headframe_id` int(11) NOT NULL COMMENT '头像框id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户服务器表格，用户在某个服务器创建角色后会创建一条记录，并且分配一个唯一的角色ID';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_u_send_mail`
--

DROP TABLE IF EXISTS `t_u_send_mail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_u_send_mail` (
  `player_id` int(11) NOT NULL COMMENT '玩家id',
  `create_time` datetime DEFAULT NULL COMMENT '发送时间',
  `activity_id` int(6) DEFAULT NULL COMMENT '活动id',
  `send_count` int(6) DEFAULT NULL COMMENT '发送次数'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='发送邮件历史记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_u_sendmail_order`
--

DROP TABLE IF EXISTS `t_u_sendmail_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_u_sendmail_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` varchar(225) NOT NULL,
  `player_id` int(11) NOT NULL COMMENT '玩家id',
  `activity_id` int(6) DEFAULT NULL COMMENT '活动id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_u_top_recharge`
--

DROP TABLE IF EXISTS `t_u_top_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_u_top_recharge` (
  `recharge` int(11) NOT NULL COMMENT '總充值',
  `server_id` int(11) NOT NULL COMMENT '服務器id',
  `user_id` int(11) NOT NULL COMMENT '渠道方提供的id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `v_server_info`
--

DROP TABLE IF EXISTS `v_server_info`;
/*!50001 DROP VIEW IF EXISTS `v_server_info`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `v_server_info` AS SELECT 
 1 AS `server_id`,
 1 AS `name`,
 1 AS `out_ip`,
 1 AS `out_port`,
 1 AS `in_ip`,
 1 AS `in_port`,
 1 AS `db_ip`,
 1 AS `db_name`,
 1 AS `db_user`,
 1 AS `db_password`,
 1 AS `static_ip`,
 1 AS `static_name`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_server_info`
--

/*!50001 DROP VIEW IF EXISTS `v_server_info`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 SQL SECURITY INVOKER */
/*!50001 VIEW `v_server_info` AS select `l`.`id` AS `server_id`,`l`.`name` AS `name`,substring_index(`l`.`address`,':',1) AS `out_ip`,substring_index(`l`.`address`,':',-(1)) AS `out_port`,substring_index(substring_index(`c`.`web_host`,'/',-(1)),':',1) AS `in_ip`,substring_index(substring_index(`c`.`web_host`,'/',-(1)),':',-(1)) AS `in_port`,substring_index(substring_index(`c`.`db_host`,'/',-(2)),':',1) AS `db_ip`,substring_index(`c`.`db_host`,'/',-(1)) AS `db_name`,`c`.`db_user` AS `db_user`,`c`.`db_password` AS `db_password`,substring_index(substring_index(`c`.`data_host`,'/',-(2)),':',1) AS `static_ip`,substring_index(`c`.`data_host`,'/',-(1)) AS `static_name` from (`t_s_server_config` `c` left join `t_s_server_list` `l` on((`c`.`id` = `l`.`id`))) where ((`l`.`mark` & 16) <> 16) order by `l`.`id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-10 21:12:36

-- ---------- DATA: bang cau hinh ----------
-- MySQL dump 10.13  Distrib 5.6.49, for Win64 (x86_64)
--
-- Host: localhost    Database: user_center
-- ------------------------------------------------------
-- Server version	5.6.49

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `t_s_server_switch`
--

LOCK TABLES `t_s_server_switch` WRITE;
/*!40000 ALTER TABLE `t_s_server_switch` DISABLE KEYS */;
INSERT INTO `t_s_server_switch` VALUES (1011,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1011,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1011,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1011,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1011,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1012,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1012,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1012,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1012,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1012,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1013,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1013,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1013,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1013,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1013,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1014,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1014,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1014,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1014,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1014,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1015,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1015,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1015,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1015,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1015,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1016,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1016,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1016,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1016,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1016,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1017,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1017,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1017,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1017,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1017,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1018,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1018,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1018,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1018,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1018,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1019,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1019,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1019,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1019,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1019,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1020,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1020,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1020,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1020,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1020,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1001,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1001,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1001,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1001,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1001,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1002,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1002,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1002,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1002,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1002,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1003,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1003,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1003,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1003,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1003,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1004,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1004,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1004,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1004,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1004,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1005,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1005,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1005,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1005,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1005,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1006,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1006,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1006,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1006,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1006,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1007,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1007,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1007,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1007,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1007,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1008,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1008,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1008,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1008,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1008,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1009,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1009,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1009,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1009,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1009,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1010,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1010,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1010,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1010,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1010,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1021,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1021,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1021,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1021,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1021,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1022,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1022,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1022,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1022,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1022,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1023,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1023,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1023,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1023,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1023,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1024,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1024,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1024,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1024,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1024,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1025,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1025,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1025,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1025,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1025,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1026,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1026,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1026,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1026,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1026,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00'),(1027,'登录',2,1,NULL,NULL,'2017-11-29 19:06:33'),(1027,'充值',3,1,NULL,NULL,'2017-11-29 19:06:44'),(1027,'注册',4,1,NULL,NULL,'2017-11-29 19:06:40'),(1027,'礼包码',5,1,NULL,NULL,'2017-07-31 14:17:02'),(1027,'服务器',1,1,NULL,NULL,'2018-10-11 01:36:00');
/*!40000 ALTER TABLE `t_s_server_switch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_s_server_status`
--

LOCK TABLES `t_s_server_status` WRITE;
/*!40000 ALTER TABLE `t_s_server_status` DISABLE KEYS */;
INSERT INTO `t_s_server_status` VALUES (1001,'2018-06-12 18:21:59',0,0),(1021,'2019-01-29 09:56:09',0,0),(1015,'2019-02-27 14:34:55',0,0),(1020,'2017-07-31 13:37:58',0,0),(1031,'2017-10-16 15:40:07',0,0),(1030,'2017-10-09 16:15:54',0,0),(1004,'2018-10-10 18:16:16',0,0),(1003,'2018-06-12 18:21:04',0,0),(1002,'2018-06-12 18:46:11',0,0),(999,'2017-12-01 12:22:42',0,0),(998,'2017-12-12 14:49:56',0,0),(1005,'2018-01-15 11:38:24',0,0),(1006,'2018-01-26 14:13:38',0,0),(1007,'2018-06-12 16:57:34',0,0),(1009,'2018-04-24 16:05:34',0,0),(1008,'2018-06-12 16:21:33',0,0),(1010,'2018-05-02 12:24:34',0,0),(1011,'2018-05-02 12:24:34',0,0),(1012,'2018-06-12 18:45:32',0,0),(1013,'2018-06-25 15:38:53',0,0),(1014,'2018-07-03 15:59:58',0,0),(1022,'2019-03-04 15:39:57',0,0),(1023,'2018-07-26 11:52:14',0,0),(1016,'2018-07-26 11:52:14',0,0),(1024,'2019-08-07 16:50:13',0,0),(1025,'2019-08-07 16:50:13',0,0),(1027,'2019-08-10 14:39:31',0,0),(1026,'2019-08-10 14:39:31',0,0);
/*!40000 ALTER TABLE `t_s_server_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_s_channel_list`
--

LOCK TABLES `t_s_channel_list` WRITE;
/*!40000 ALTER TABLE `t_s_channel_list` DISABLE KEYS */;
INSERT INTO `t_s_channel_list` VALUES (70,'tc','{\"appid\":\"10038\",\"appkey\":\"bb82c8bb9886a52536b88345e5eed0ba\",\"login_url\":\"http://test.tiancigame.cn/api/server/check_user\",\"url\":\"http://server10038.service.tiancigame.cn/api/server/check_user\"}'),(600,'ym_android','{\"appkey\":\"5b73ef46f29d987d25000021\",\"appMasterSecret\":\"rvas0hllewnlfejbxek1aocvvjvmp0a5\",\"testMode\":\"true\"}'),(700,'rdhd_android','{\"appkey\":\"33791688174089380817785295382595\",\"appMasterSecret\":\"rvas0hllewnlfejbxek1aocvvjvmp0a5\",\"testMode\":\"true\"}'),(701,'rdhd_ios','{\"appkey\":\"16214991584405190914725142724705\",\"appMasterSecret\":\"rvas0hllewnlfejbxek1aocvvjvmp0a5\",\"testMode\":\"true\"}'),(1000000,'BANSHU','{\"desc\":\"版署专用\"}'),(800,'51sfsy_android','{\"appkey\":\"561f5caa62d594f3ade8a4b9b64b288b\"}'),(801,'51sfsy_ios','{\"appkey\":\"561f5caa62d594f3ade8a4b9b64b288b\"}'),(702,'rdhd_ios','{\"appkey\":\"16214991584405190914725142724705\",\"appMasterSecret\":\"rvas0hllewnlfejbxek1aocvvjvmp0a5\",\"testMode\":\"true\"}'),(900,'yn_android','{\"appkey\":\"886ff87be63afaec3b6c31cd3ea65fc4\"}'),(901,'yn_ios','{\"appkey\":\"886ff87be63afaec3b6c31cd3ea65fc4\"}'),(902,'yn_ios_aud','{\"appkey\":\"886ff87be63afaec3b6c31cd3ea65fc4\"}'),(820,'hl51sfsy_android','{\"appkey\":\"598848ce3fdae0186905249c364ea0ed\"}'),(821,'hl51sfsy_ios','{\"appkey\":\"598848ce3fdae0186905249c364ea0ed\"}'),(810,'mv51sfsy_android','{\"appkey\":\"23b9e9286dfbb21c871b2fa0e9c1ccfa\"}'),(811,'mv51sfsy_ios','{\"appkey\":\"23b9e9286dfbb21c871b2fa0e9c1ccfa\"}'),(1001,'hn_yyb_qq','{\"appid\":\"1106806233\",\"appkey\":\"1qSLx1rtAMFVs7CS\"}'),(1002,'hn_yyb_wx','{\"appid\":\"wxba2904daf1f68ba4\",\"appkey\":\"0e60761b3f7469ea695b1b1953584dff\"}'),(1003,'hn_uc','{\"appid\":\"1092177\",\"appkey\":\"0a200dbc08de5771cf1b4bbef9a3e938\"}'),(999,'hn','{\"appid\":\"17\",\"appkey\":\"4479a940133b8aa412e3e9911b6e8452\"}'),(1004,'hn_huawei','{\"appid\":\"100684577\",\"appkey\":\"9408eb2204e9f0df5cc3ded9205893d2\",\"cpid\":\"900086000000100789\",\"privatekey\":\"MIIBVgIBADANBgkqhkiG9w0BAQEFAASCAUAwggE8AgEAAkEA4BnisVRtWZ96gu/LsW/qtB+jwyqDuYRmcXm/SOrww05DVgWohJQWgxAzpn7NslglQVYEa/xB3RH+OzbjCsTE4QIDAQABAkEAvI5LJPVqhGwhAqaM9qC6FBBDE+Vjq+Zw462SeuHi7hAKQtkMlAwuSJmxS9kJxL0xRmK1xaCjyy2RqL8DBTCt+QIhAPmoZFaPUbBttdWvgqFTCuwyywB6EWut8IcNC95fO6sTAiEA5ctKPzmRA6q0SNyh4Jjy0EkV9QpyWwuzoMRVSQHxursCIFFR01UKm94u7jKrV456wS0MomkGWdRMNPOYgUwukv33AiEAjCs81uQeeMYfwnISrBWfxz1Nj3MX3kF9CIu6GhZ9hRECIQCGuTCbDxdIzmGhNK3F7Dznc9OJIQwCr+3Pyvij878uJg==\",\"publickey\":\"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAOAZ4rFUbVmfeoLvy7Fv6rQfo8Mqg7mEZnF5v0jq8MNOQ1YFqISUFoMQM6Z+zbJYJUFWBGv8Qd0R/js24wrExOECAwEAAQ==\"}'),(1005,'hn_mi','{\"appid\":\"2882303761517978540\",\"appkey\":\"5611797856540\",\"appSecret\":\"fGffiyfxgzBdcQhVS4Ykvg==\"}'),(1006,'hn_vivo','{\"appid\":\"100425935\",\"appkey\":\"cad36775e04c71773871771d6d97d1e4\",\"cpid\":\"f5c8a8a14c686ec6e0dd\"}'),(1007,'hn_oppo','{\"appid\":\"3589674\",\"appkey\":\"3r5brH4cotWkcW840Wc4ss480\",\"appSecret\":\"F5A5Aa95D234463c3552FCb50b76ccF5\"}'),(1102,'xiao7_sdk_ios','{\"appkey\":\"80207719d0ebf5af10eb2dcf03850b42\",\"rsakey\":\"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDSNpFpRmtWb+5uj4oNI6ZczYzHXqQfPDeRFJKDfCKTtqxE9MIeLtyu58N8pA8gOT6hMYF2OkCEWDlbyDILgBoEi70H2PWgE1krSlPrQV0qU8ojmyZPlheKOGaT9UmRijLCTrv8lInFJfn9U8Q07O6OBpFLNRZbYVorZ0yvhhCakwIDAQAB\"}'),(1101,'xiao7_sdk','{\"appkey\":\"d86d80b725161ccefeaaafc4ed4121d8\",\"rsakey\":\"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDru0piUU76HdQscjVty7PJ0q5zpsRchTwpZJW/EQv/LI2BovevZ/ntjLwT1KwxtfE++fMNPUuZQbGeiUcmXyzMaYO/q/ToLWs327MY/zTq0j3Jlupm7WdubCvbGDNxRXOLovysOjQXS7BehQRemZ69ocpw7eio1Ej6QUKPrEL9OwIDAQAB\"}');
/*!40000 ALTER TABLE `t_s_channel_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_s_server_config`
--

LOCK TABLES `t_s_server_config` WRITE;
/*!40000 ALTER TABLE `t_s_server_config` DISABLE KEYS */;
INSERT INTO `t_s_server_config` VALUES (1001,6,'http://127.0.0.1:8889','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8880'),(1002,1,'http://127.0.0.1:8891','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8879'),(1007,1,'http://127.0.0.1:8889','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8879'),(1004,1,'http://127.0.0.1:8893','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8883'),(1011,1,'http://127.0.0.1:8889','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8879'),(1005,1,'http://127.0.0.1:8894','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8884'),(1023,1,'http://127.0.0.1:8891','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8879'),(1014,1,'http://127.0.0.1:8889','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8879'),(1016,1,'http://127.0.0.1:8890','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8880'),(1008,1,'http://127.0.0.1:8889','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8879'),(1003,1,'http://127.0.0.1:8890','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8881'),(1021,6,'http://127.0.0.1:8895','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8885'),(1022,1,'http://127.0.0.1:8892','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:8883'),(1024,7,'http://127.0.0.1:21024','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:31024'),(1025,7,'http://127.0.0.1:21025','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:31025'),(1027,8,'http://127.0.0.1:21027','jdbc:mysql://127.0.0.1:3306/h_game_log','root','xpymw.com','jdbc:mysql://127.0.0.1:3306/h_game_log','http://127.0.0.1:31027');
/*!40000 ALTER TABLE `t_s_server_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_s_channel_switch`
--

LOCK TABLES `t_s_channel_switch` WRITE;
/*!40000 ALTER TABLE `t_s_channel_switch` DISABLE KEYS */;
INSERT INTO `t_s_channel_switch` VALUES ('NAN','礼包码',5,0,NULL,NULL,'2017-09-12 17:56:13'),('NAN','微信关注',6,0,NULL,NULL,'2017-09-12 17:56:13'),('NAN','邀请码',8,0,NULL,NULL,'2017-09-12 17:56:13'),('NAN','天猫',16,0,NULL,NULL,'2017-09-12 17:56:13'),('NAN','贵宾',24,0,NULL,NULL,'2017-09-12 17:56:13'),('NAN','是否显示退出',26,0,NULL,NULL,'2017-09-12 17:56:13');
/*!40000 ALTER TABLE `t_s_channel_switch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_s_server_zone`
--

LOCK TABLES `t_s_server_zone` WRITE;
/*!40000 ALTER TABLE `t_s_server_zone` DISABLE KEYS */;
INSERT INTO `t_s_server_zone` VALUES (1,'测试','NAN|70|700|701|702|800|801|1001|1002|1003|1004|1005|1102|1101'),(2,'版署','BANSHU|NAN'),(6,'满V','NAN|70|700|701|702|800|801|1001|1002|1003|1004|1005|1102|1101'),(7,'越南','NAN|70|700|701|702|800|801|1001|1002|1003|1004|1005|1102|1101'),(8,'商城','NAN|70|700|701|702|800|801|1001|1002|1003|1004|1005|1102|1101');
/*!40000 ALTER TABLE `t_s_server_zone` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `server_login_white`
--

LOCK TABLES `server_login_white` WRITE;
/*!40000 ALTER TABLE `server_login_white` DISABLE KEYS */;
INSERT INTO `server_login_white` VALUES (246,0,4,'2017-05-25 20:13:23',1,'admin'),(247,0,576,'2017-10-27 15:38:21',1,'admin'),(248,0,1,'2018-06-05 10:40:51',2,'12'),(1,0,4276,'2018-12-12 21:39:50',0,'add');
/*!40000 ALTER TABLE `server_login_white` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-10 21:12:36
