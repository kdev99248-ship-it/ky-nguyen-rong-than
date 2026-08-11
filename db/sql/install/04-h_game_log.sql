-- h_game_log : 11 bang log trong game (login/logout/item/recharge...) - RONG

-- MySQL dump 10.13  Distrib 5.6.49, for Win64 (x86_64)
--
-- Host: localhost    Database: h_game_log
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
-- Table structure for table `log_account_create`
--

DROP TABLE IF EXISTS `log_account_create`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log_account_create` (
  `time` datetime DEFAULT NULL,
  `areaId` int(11) DEFAULT NULL,
  `channel_id` varchar(255) DEFAULT NULL,
  `account` varchar(255) DEFAULT NULL,
  `accountUid` bigint(20) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `deviceName` varchar(255) DEFAULT NULL,
  `deviceId` varchar(255) DEFAULT NULL,
  `osName` varchar(255) DEFAULT NULL,
  `osVersion` varchar(255) DEFAULT NULL,
  `sdk` varchar(255) DEFAULT NULL,
  `sdkVersion` varchar(255) DEFAULT NULL,
  `mcc` varchar(255) DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  KEY `time_index` (`time`),
  KEY `account_index` (`accountUid`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `log_player_current`
--

DROP TABLE IF EXISTS `log_player_current`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log_player_current` (
  `role_id` bigint(20) NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `platform_id` int(11) DEFAULT NULL,
  `platform_account` varchar(255) DEFAULT NULL,
  `account` bigint(20) DEFAULT NULL,
  `role_name` varchar(255) DEFAULT NULL,
  `create_server` smallint(6) unsigned DEFAULT NULL,
  `current_server` smallint(6) unsigned DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `last_offline_time` datetime DEFAULT NULL,
  `online_long_total` int(11) unsigned DEFAULT NULL,
  `isForbid` tinyint(4) DEFAULT NULL,
  `level` int(11) unsigned DEFAULT NULL,
  `vip_level` int(11) unsigned DEFAULT NULL,
  `profession` int(11) DEFAULT NULL,
  `fight_power` bigint(20) unsigned DEFAULT NULL,
  `exp` bigint(20) unsigned DEFAULT NULL,
  `mission_star_total` int(11) unsigned DEFAULT NULL,
  `recharge_total` int(11) unsigned DEFAULT NULL,
  `gold` bigint(20) unsigned DEFAULT NULL,
  `diamond_recharge` bigint(20) unsigned DEFAULT NULL,
  `diamond_present` bigint(20) unsigned DEFAULT NULL,
  `diamond_total_recharge` bigint(20) DEFAULT NULL,
  `diamond_total_consume` bigint(20) DEFAULT NULL,
  `prestige` int(11) DEFAULT NULL,
  `honour` int(11) DEFAULT NULL,
  `brave` int(11) DEFAULT NULL,
  `qiling` int(11) DEFAULT NULL,
  `spar` int(11) DEFAULT NULL,
  `last_newbie_finish_id` int(11) DEFAULT '0',
  `last_newbie_finish_time` datetime DEFAULT NULL,
  `server_ip` varchar(255) DEFAULT NULL,
  `server_port` int(11) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  PRIMARY KEY (`role_id`),
  KEY `account_index` (`account`) USING BTREE,
  KEY `rolename_index` (`role_name`) USING BTREE,
  KEY `createtime_index` (`create_time`) USING BTREE,
  KEY `last_newbie_finish_id_index` (`last_newbie_finish_id`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_item_log`
--

DROP TABLE IF EXISTS `t_item_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_item_log` (
  `areaId` int(20) DEFAULT NULL,
  `channel_id` varchar(500) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `accountId` varchar(500) DEFAULT NULL,
  `accountUid` bigint(20) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `pname` varchar(255) DEFAULT NULL,
  `plvl` int(11) DEFAULT NULL,
  `pvip` int(11) unsigned DEFAULT NULL,
  `log_date` datetime DEFAULT NULL,
  `item_id` int(11) DEFAULT NULL,
  `change_num` int(11) DEFAULT NULL,
  `current_num` bigint(20) DEFAULT NULL,
  `log_type` int(11) DEFAULT NULL,
  `log_ext` varchar(255) DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  KEY `userid_index` (`pid`) USING BTREE,
  KEY `roleid_index` (`areaId`) USING BTREE,
  KEY `rolename_index` (`plvl`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_player_action_log`
--

DROP TABLE IF EXISTS `t_player_action_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_player_action_log` (
  `areaId` int(20) DEFAULT NULL,
  `channel_id` varchar(500) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `accountId` varchar(500) DEFAULT NULL,
  `accountUid` bigint(20) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `pname` varchar(255) DEFAULT NULL,
  `plvl` int(11) unsigned DEFAULT NULL,
  `pvip` int(11) DEFAULT NULL,
  `log_date` datetime DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `log_type` int(11) DEFAULT NULL,
  `log_ext` varchar(255) DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  KEY `userid_index` (`pid`) USING BTREE,
  KEY `roleid_index` (`areaId`) USING BTREE,
  KEY `rolename_index` (`pname`) USING BTREE,
  KEY `time_index` (`log_date`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_player_guide_log`
--

DROP TABLE IF EXISTS `t_player_guide_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_player_guide_log` (
  `areaId` int(20) DEFAULT NULL,
  `channel_id` varchar(500) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `accountId` varchar(500) DEFAULT NULL,
  `accountUid` bigint(20) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `pname` varchar(255) DEFAULT NULL,
  `plvl` int(11) unsigned DEFAULT NULL,
  `pvip` int(11) DEFAULT NULL,
  `log_date` datetime DEFAULT NULL,
  `content` varchar(500) DEFAULT NULL,
  `log_type` int(11) DEFAULT NULL,
  `log_ext` varchar(500) DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  KEY `userid_index` (`pid`) USING BTREE,
  KEY `roleid_index` (`areaId`) USING BTREE,
  KEY `rolename_index` (`pname`) USING BTREE,
  KEY `time_index` (`log_date`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_player_linenum_log`
--

DROP TABLE IF EXISTS `t_player_linenum_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_player_linenum_log` (
  `date` datetime DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `areaId` int(20) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  `channel_id` varchar(255) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_player_login_log`
--

DROP TABLE IF EXISTS `t_player_login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_player_login_log` (
  `areaId` int(20) DEFAULT NULL,
  `channel_id` varchar(500) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `accountId` varchar(500) DEFAULT NULL,
  `accountUid` bigint(20) DEFAULT NULL,
  `pid` bigint(20) NOT NULL,
  `pname` varchar(255) DEFAULT NULL,
  `plvl` int(11) unsigned DEFAULT NULL,
  `pvip` int(11) DEFAULT NULL,
  `log_date` datetime DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `log_type` int(11) DEFAULT NULL,
  `log_ext` varchar(255) DEFAULT NULL,
  `imei` varchar(255) DEFAULT NULL,
  `device_name` varchar(255) DEFAULT NULL,
  `os_version` varchar(255) DEFAULT NULL,
  `client_version` varchar(255) DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  KEY `userid_index` (`pid`) USING BTREE,
  KEY `roleid_index` (`areaId`) USING BTREE,
  KEY `rolename_index` (`pname`) USING BTREE,
  KEY `time_index` (`log_date`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_player_logout_log`
--

DROP TABLE IF EXISTS `t_player_logout_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_player_logout_log` (
  `areaId` int(20) DEFAULT NULL,
  `channel_id` varchar(500) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `accountId` varchar(500) DEFAULT NULL,
  `accountUid` bigint(20) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `pname` varchar(255) DEFAULT NULL,
  `plvl` int(11) unsigned DEFAULT NULL,
  `pvip` int(11) DEFAULT NULL,
  `log_date` datetime DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `log_type` int(11) DEFAULT NULL,
  `log_ext` varchar(255) DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  `onlineTime` int(11) DEFAULT NULL,
  `exp` bigint(20) DEFAULT NULL,
  `diamond` int(11) DEFAULT NULL,
  KEY `userid_index` (`pid`) USING BTREE,
  KEY `roleid_index` (`areaId`) USING BTREE,
  KEY `rolename_index` (`pname`) USING BTREE,
  KEY `time_index` (`log_date`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_player_lvl_log`
--

DROP TABLE IF EXISTS `t_player_lvl_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_player_lvl_log` (
  `areaId` int(20) DEFAULT NULL,
  `channel_id` varchar(500) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `accountId` varchar(500) DEFAULT NULL,
  `accountUid` bigint(20) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `pname` varchar(255) DEFAULT NULL,
  `plvl` int(11) unsigned DEFAULT NULL,
  `pvip` int(11) DEFAULT NULL,
  `log_date` datetime DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `log_type` int(11) DEFAULT NULL,
  `log_ext` varchar(255) DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  KEY `userid_index` (`pid`) USING BTREE,
  KEY `roleid_index` (`areaId`) USING BTREE,
  KEY `rolename_index` (`pname`) USING BTREE,
  KEY `time_index` (`log_date`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_player_recharge_log`
--

DROP TABLE IF EXISTS `t_player_recharge_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_player_recharge_log` (
  `log_date` datetime NOT NULL COMMENT '日志时间',
  `uid` varchar(45) DEFAULT NULL COMMENT '用户ID',
  `player_level` int(11) DEFAULT NULL COMMENT '玩家等级',
  `pid` bigint(20) DEFAULT NULL COMMENT '玩家ID',
  `cp_id` varchar(45) DEFAULT NULL COMMENT '商品ID',
  `channel_id` varchar(45) DEFAULT NULL COMMENT '渠道ID',
  `server_id` varchar(45) DEFAULT NULL COMMENT '服务器ID',
  `order_id` varchar(45) NOT NULL DEFAULT '' COMMENT '订单号',
  `money` double DEFAULT NULL COMMENT '金额',
  `type` int(11) DEFAULT NULL COMMENT '充值类型',
  `currency_type` varchar(45) DEFAULT NULL COMMENT '货币类型(如：USD)',
  `currency_unit` varchar(45) DEFAULT NULL COMMENT '货币单位(如:元)',
  `item_count` int(11) DEFAULT NULL COMMENT '发放道具数量(一般为钻石)',
  `item_count_before` bigint(20) DEFAULT NULL COMMENT '发放钻石则记录发放前',
  `item_count_after` bigint(20) DEFAULT NULL COMMENT '发放钻石则记录发放后',
  `channel_app_id` int(11) DEFAULT '0',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='充值日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_player_register_log`
--

DROP TABLE IF EXISTS `t_player_register_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_player_register_log` (
  `areaId` int(11) DEFAULT NULL,
  `channel_id` varchar(255) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `accountId` varchar(255) DEFAULT NULL,
  `accountUid` bigint(20) DEFAULT NULL,
  `pid` varchar(255) DEFAULT NULL,
  `pname` varchar(255) DEFAULT NULL,
  `log_date` datetime DEFAULT NULL,
  `channel_app_id` int(11) DEFAULT '0',
  `imei` varchar(255) DEFAULT NULL,
  KEY `account_index` (`accountUid`) USING BTREE,
  KEY `rolename_index` (`pname`) USING BTREE,
  KEY `createtime_index` (`log_date`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-10 21:13:36
