-- h_game_global_log : 2 bang log toan cuc - RONG

-- MySQL dump 10.13  Distrib 5.6.49, for Win64 (x86_64)
--
-- Host: localhost    Database: h_game_global_log
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
  `channel_id` varchar(45) DEFAULT NULL COMMENT '渠道ID',
  `server_id` varchar(45) DEFAULT NULL COMMENT '服务器ID',
  `order_id` varchar(45) DEFAULT NULL COMMENT '订单号',
  `money` double DEFAULT NULL COMMENT '金额',
  `currency_type` varchar(45) DEFAULT NULL COMMENT '货币类型(如：USD)',
  `currency_unit` varchar(45) DEFAULT NULL COMMENT '货币单位(如:元)',
  `item_count` int(11) DEFAULT NULL COMMENT '发放道具数量(一般为钻石)',
  PRIMARY KEY (`log_date`)
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
  `channel_id` varchar(45) DEFAULT NULL COMMENT '渠道ID',
  `server_id` varchar(45) DEFAULT NULL COMMENT '服务器ID',
  `imei` varchar(100) DEFAULT NULL COMMENT '设备号',
  `os_type` varchar(45) DEFAULT NULL COMMENT '系统类型',
  `os_ver` varchar(45) DEFAULT NULL COMMENT '系统版本'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色注册日志';
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
