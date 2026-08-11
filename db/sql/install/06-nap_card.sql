-- nap_card : webapp nap the (Spring Boot + Liquibase)
-- schema 26 bang + data cau hinh. databasechangelog PHAI co, neu khong Liquibase se chay lai migration.
-- KHONG kem tai khoan nguoi dung (users/token/ip_locked).

-- MySQL dump 10.13  Distrib 5.6.49, for Win64 (x86_64)
--
-- Host: localhost    Database: nap_card
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
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `activity_name` varchar(50) DEFAULT NULL,
  `activity_type` int(11) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_item`
--

DROP TABLE IF EXISTS `activity_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `activity_id` varchar(50) DEFAULT NULL,
  `target` int(11) DEFAULT NULL,
  `reward` varchar(1000) DEFAULT NULL,
  `reward_details` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_progress`
--

DROP TABLE IF EXISTS `activity_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_progress` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `activity_id` int(11) DEFAULT NULL,
  `item_id` int(11) DEFAULT NULL,
  `player_id` varchar(50) DEFAULT NULL,
  `target` int(11) DEFAULT NULL,
  `reward` varchar(1000) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `is_deleted` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `app_properties`
--

DROP TABLE IF EXISTS `app_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_properties` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key_app` varchar(100) NOT NULL,
  `value_app` varchar(100) NOT NULL,
  `description` varchar(100) NOT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `databasechangelog`
--

DROP TABLE IF EXISTS `databasechangelog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `databasechangeloglock`
--

DROP TABLE IF EXISTS `databasechangeloglock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `databasechangeloglock` (
  `ID` int(11) NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gift_code`
--

DROP TABLE IF EXISTS `gift_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gift_code` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `expression` varchar(1000) NOT NULL,
  `status` int(11) NOT NULL,
  `begin_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `code_type` int(11) DEFAULT NULL,
  `quantity` int(11) DEFAULT '10000',
  `user_ids` varchar(1000) DEFAULT NULL,
  `distributed` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gift_code_log`
--

DROP TABLE IF EXISTS `gift_code_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gift_code_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gift_id` int(11) NOT NULL,
  `member_id` varchar(50) DEFAULT NULL,
  `server_id` int(11) NOT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  `code_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gift_daily_log`
--

DROP TABLE IF EXISTS `gift_daily_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gift_daily_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gift_id` int(11) NOT NULL,
  `member_id` varchar(50) DEFAULT NULL,
  `server_id` int(11) NOT NULL,
  `last_receiver` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ip_block`
--

DROP TABLE IF EXISTS `ip_block`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ip_block` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip_address` varchar(1000) NOT NULL,
  `reason` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ip_locked`
--

DROP TABLE IF EXISTS `ip_locked`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ip_locked` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip_address` varchar(1000) NOT NULL,
  `path_request` varchar(100) NOT NULL,
  `date_execute` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=66 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lucky_draw_his`
--

DROP TABLE IF EXISTS `lucky_draw_his`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lucky_draw_his` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(50) DEFAULT NULL,
  `turn_round` int(11) DEFAULT '0',
  `prize_id` int(11) DEFAULT '99999',
  `prize_content` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `content` varchar(3000) NOT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `package_charge`
--

DROP TABLE IF EXISTS `package_charge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `package_charge` (
  `id` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `sycee` int(11) NOT NULL DEFAULT '0',
  `extra_sycee` int(11) NOT NULL DEFAULT '0',
  `display_id` int(11) NOT NULL DEFAULT '0',
  `title` varchar(1000) DEFAULT NULL,
  `is_show` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `package_charge_history`
--

DROP TABLE IF EXISTS `package_charge_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `package_charge_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `package_charge_id` int(11) NOT NULL,
  `package_charge_name` varchar(255) NOT NULL DEFAULT '0',
  `user_id` int(11) NOT NULL DEFAULT '0',
  `price` int(11) NOT NULL DEFAULT '0',
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  `player_id` varchar(255) DEFAULT NULL,
  `player_name` varchar(255) DEFAULT NULL,
  `server_name` varchar(255) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  `first_charge` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `point_history`
--

DROP TABLE IF EXISTS `point_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `point_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `transaction_type` varchar(100) NOT NULL,
  `wallet_user_id` int(11) NOT NULL,
  `old_point` int(11) DEFAULT NULL,
  `new_point` int(11) NOT NULL,
  `point` int(11) NOT NULL DEFAULT '0',
  `notes` varchar(1000) DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  `rate_charge` double DEFAULT '1',
  `package_id` int(11) DEFAULT '0',
  `package_name` varchar(100) DEFAULT NULL,
  `package_price` int(11) DEFAULT '0',
  `package_cost` int(11) DEFAULT '0',
  `server_id` int(11) DEFAULT '0',
  `account_id` varchar(50) DEFAULT NULL,
  `account_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prize`
--

DROP TABLE IF EXISTS `prize`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prize` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text` varchar(50) NOT NULL,
  `img` varchar(100) NOT NULL,
  `number` int(11) DEFAULT '99999',
  `percent_page` float DEFAULT '0',
  `point_lock` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roles` (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `server`
--

DROP TABLE IF EXISTS `server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `server` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name_server` varchar(100) NOT NULL,
  `server_id` varchar(100) NOT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  `active_charge` int(11) DEFAULT '0',
  `db_host` varchar(1000) DEFAULT NULL,
  `db_user` varchar(1000) DEFAULT NULL,
  `db_password` varchar(1000) DEFAULT NULL,
  `url_charge` varchar(1000) DEFAULT NULL,
  `merge_server` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `token`
--

DROP TABLE IF EXISTS `token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `token` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `token` varchar(200) NOT NULL,
  `exp_date` datetime NOT NULL,
  `active` int(11) NOT NULL DEFAULT '1',
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transaction_log`
--

DROP TABLE IF EXISTS `transaction_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transaction_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `server_id` int(11) NOT NULL,
  `total_point` int(11) NOT NULL,
  `total_gold_receive` int(11) NOT NULL,
  `response_message` varchar(1000) NOT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `email` varchar(200) DEFAULT NULL,
  `role_id` int(11) NOT NULL,
  `point` int(11) NOT NULL DEFAULT '0',
  `last_transfer` datetime DEFAULT NULL,
  `password_game` varchar(1000) DEFAULT NULL,
  `appkey` varchar(1000) DEFAULT NULL,
  `token` varchar(1000) DEFAULT NULL,
  `qltoken` varchar(1000) DEFAULT NULL,
  `imei` varchar(1000) DEFAULT NULL,
  `mobile` varchar(100) DEFAULT NULL,
  `point_lock` int(11) DEFAULT '0',
  `turn_round` int(11) DEFAULT '0',
  `last_draw` datetime DEFAULT '2022-01-01 12:00:00',
  `token_ios` varchar(100) DEFAULT NULL,
  `openlogin` int(11) DEFAULT '0',
  PRIMARY KEY (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=67 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `version`
--

DROP TABLE IF EXISTS `version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `version` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `maxversion` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wallet_user`
--

DROP TABLE IF EXISTS `wallet_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wallet_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `type_network` varchar(100) NOT NULL,
  `card_value` int(11) DEFAULT NULL,
  `seri_num` varchar(100) NOT NULL,
  `secret_num` varchar(100) NOT NULL,
  `status` int(11) NOT NULL DEFAULT '0',
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  `response_first_face` varchar(5000) DEFAULT NULL,
  `response_call_back` varchar(5000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `webshop`
--

DROP TABLE IF EXISTS `webshop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `webshop` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(500) NOT NULL,
  `img_url` varchar(500) NOT NULL,
  `quantity` int(11) DEFAULT '10',
  `price` int(11) DEFAULT '10000',
  `old_price` int(11) DEFAULT '50000',
  `sold` int(11) DEFAULT '0',
  `expression` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `webshop_log`
--

DROP TABLE IF EXISTS `webshop_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `webshop_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `player_id` varchar(50) NOT NULL,
  `name_item` varchar(500) NOT NULL,
  `quantity` int(11) DEFAULT '10',
  `price` int(11) DEFAULT '10000',
  `created_by` varchar(50) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_on` datetime DEFAULT NULL,
  `expression` varchar(256) DEFAULT NULL,
  `webshop_id` int(11) DEFAULT NULL,
  `server_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
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

-- Dump completed on 2026-08-10 21:13:37

-- ---------- DATA: cau hinh + so migration ----------
-- MySQL dump 10.13  Distrib 5.6.49, for Win64 (x86_64)
--
-- Host: localhost    Database: nap_card
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
-- Dumping data for table `databasechangelog`
--

LOCK TABLES `databasechangelog` WRITE;
/*!40000 ALTER TABLE `databasechangelog` DISABLE KEYS */;
INSERT INTO `databasechangelog` VALUES ('20190323134000-1','dokd','db/changelog/20190323134000_add_entity_user.xml','2022-10-08 21:56:51',1,'EXECUTED','8:70b1a05e9aaa3a62b5b1368f48aae38a','createTable tableName=users','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20191114090000-1','DoKD','db/changelog/20190323134000_add_entity_user.xml','2022-10-08 21:56:51',2,'EXECUTED','8:a22f395533162b526c301137ba6624ae','addColumn tableName=users','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20200806153000-1','DoKD','db/changelog/20190323134000_add_entity_user.xml','2022-10-08 21:56:51',3,'EXECUTED','8:1cb083e759bf1b211633481aa5e131c6','addColumn tableName=users','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20210407223000-1','DoKD','db/changelog/20190323134000_add_entity_user.xml','2022-10-08 21:56:51',4,'EXECUTED','8:442694fdb15ae2b596b354526482c706','addColumn tableName=users','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20210407223000-2','DoKD','db/changelog/20190323134000_add_entity_user.xml','2022-10-08 21:56:51',5,'EXECUTED','8:8eca441f95c054b1c9f258cf6c739d41','addColumn tableName=users','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220108225500-1','DoKD','db/changelog/20190323134000_add_entity_user.xml','2022-10-08 21:56:51',6,'EXECUTED','8:14d8cf3f8840cdd436d9f5956b140cdf','addColumn tableName=users','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220212131900-1','DoKD','db/changelog/20190323134000_add_entity_user.xml','2022-10-08 21:56:51',7,'EXECUTED','8:03c71bb9bfde3aa693d57be202de4fe1','addDefaultValue columnName=last_draw, tableName=users','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220108225500-2','DoKD','db/changelog/20190323134000_add_entity_user.xml','2022-10-08 21:56:51',8,'EXECUTED','8:4bb918566fac1bbe08ca76655a6bda72','addColumn tableName=users','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190323135000-1','dokd','db/changelog/20190323135000_add_entity_role.xml','2022-10-08 21:56:51',9,'EXECUTED','8:409d7652dcb56a9a62dcb2b9b6908d21','createTable tableName=roles','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190323140000-1','dokd','db/changelog/20190323140000_add_entity_token.xml','2022-10-08 21:56:51',10,'EXECUTED','8:29019f4f365b67c74c660fda5c2b8ede','createTable tableName=token','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190323134000-1','dokd','db/changelog/20190524115500_add_entity_wallet_user.xml','2022-10-08 21:56:51',11,'EXECUTED','8:1460b5a748b23f2f7a81728358ae2671','createTable tableName=wallet_user','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190525000000-1','dokd','db/changelog/20190525000000_add_entity_point_history.xml','2022-10-08 21:56:51',12,'EXECUTED','8:643fc785139d80f73c3403cd7fffe37e','createTable tableName=point_history','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20201018222000-1','DoKD','db/changelog/20190525000000_add_entity_point_history.xml','2022-10-08 21:56:51',13,'EXECUTED','8:4bf3b2ada0f27f22e8e82611b4a50eb7','addColumn tableName=point_history','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220302093000-1','DoKD','db/changelog/20190525000000_add_entity_point_history.xml','2022-10-08 21:56:51',14,'EXECUTED','8:cd958efbf87976268bb6c1895abcedd6','modifyDataType columnName=account_id, tableName=point_history','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190526000000-1','dokd','db/changelog/20190526000000_add_entity_app_properties.xml','2022-10-08 21:56:51',15,'EXECUTED','8:39e1fedfac13309fae95da5cd07de131','createTable tableName=app_properties','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190526113000-1','dokd','db/changelog/20190526113000_add_entity_server.xml','2022-10-08 21:56:51',16,'EXECUTED','8:c1c480a4ab7e290023c7ad4e15717930','createTable tableName=server','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20200806010700-1','dokd','db/changelog/20190526113000_add_entity_server.xml','2022-10-08 21:56:51',17,'EXECUTED','8:0990ffc7dee636c739278bbfd7933f9f','addColumn tableName=server','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20191109112000-1','dokd','db/changelog/20191109112000_add_entity_gift_code.xml','2022-10-08 21:56:51',18,'EXECUTED','8:6e6595d61c6651c4332f3af5786e9e94','createTable tableName=gift_code','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220122214000-1','DoKD','db/changelog/20191109112000_add_entity_gift_code.xml','2022-10-08 21:56:51',19,'EXECUTED','8:d326c35c6f11a8349516879dbc570768','addColumn tableName=gift_code','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20191116122000-1','dokd','db/changelog/20191109112000_add_entity_gift_code_log.xml','2022-10-08 21:56:51',20,'EXECUTED','8:9a0beaf8e05192f1934885987ade69a7','createTable tableName=gift_code_log','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220303205300-1','DoKD','db/changelog/20191109112000_add_entity_gift_code_log.xml','2022-10-08 21:56:51',21,'EXECUTED','8:910fe966aa48adc700e88a5dd731384c','modifyDataType columnName=member_id, tableName=gift_code_log','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20191109112000-1','dokd','db/changelog/20200806161500_add_entity_package_charge.xml','2022-10-08 21:56:51',22,'EXECUTED','8:5d98ead98d98e32c5268481674534382','createTable tableName=package_charge','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20191109112000-1','dokd','db/changelog/20200806161500_add_entity_package_charge_history.xml','2022-10-08 21:56:51',23,'EXECUTED','8:f87ad2ab8005cac64ac6f90007614fed','createTable tableName=package_charge_history','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20201016003000-1','DoKD','db/changelog/20200806161500_add_entity_package_charge_history.xml','2022-10-08 21:56:51',24,'EXECUTED','8:71d4282e6f510c0991a429881cbce942','addColumn tableName=package_charge_history','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20201016003000-2','DoKD','db/changelog/20200806161500_add_entity_package_charge_history.xml','2022-10-08 21:56:51',25,'EXECUTED','8:6ccabed1843f23a8a1887cb7132ae2ce','addColumn tableName=package_charge_history','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20201015223500-1','dokd','db/changelog/20201015223500_add_entity_notification.xml','2022-10-08 21:56:51',26,'EXECUTED','8:ea41d5301e5edf74b1eaff3089396a09','createTable tableName=notification','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190526113500-1','dokd','db/changelog/20190526113500_add_entity_transaction_log.xml','2022-10-08 21:56:52',27,'EXECUTED','8:5790a327b32b55692c2841ba232e7047','createTable tableName=transaction_log','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20200924233000-1','dokd','db/changelog/20190323134000_add_entity_version.xml','2022-10-08 21:56:52',28,'EXECUTED','8:f96d205739e9f68707275e8b93610d73','createTable tableName=version','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20200924233000-2','dokd','db/changelog/20190323134000_add_entity_version.xml','2022-10-08 21:56:52',29,'EXECUTED','8:fc4af2dc9edeeed84e5670f76fd1e31d','insert tableName=version','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20200916195900-1','dokd','db/changelog/20190323134000_add_ip_locked.xml','2022-10-08 21:56:52',30,'EXECUTED','8:b86280302bc2ff4c51d2724a001750b9','createTable tableName=ip_locked','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20200916200000-1','dokd','db/changelog/20190323134000_add_ip_locked.xml','2022-10-08 21:56:52',31,'EXECUTED','8:bbead90f2558a673a681f8beb2a28dc9','createTable tableName=ip_block','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20210407224500-1','dokd','db/changelog/20210407224000_add_entity_prize.xml','2022-10-08 21:56:52',32,'EXECUTED','8:92c9afb22907dedfadd03cd4db5180f6','createTable tableName=prize','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20210407224500-2','DoKD','db/changelog/20210407224000_add_entity_prize.xml','2022-10-08 21:56:52',33,'EXECUTED','8:c92e68e1214418749475adc57f6f5fa0','addColumn tableName=prize','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20210407224500-2','dokd','db/changelog/20210407224000_add_entity_lucky_draw_his.xml','2022-10-08 21:56:52',34,'EXECUTED','8:1297cfe547ebc2a98dbe3ef2646a1dfe','createTable tableName=lucky_draw_his','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220306131500-1','dokd','db/changelog/20220306131500_add_entity_webshop.xml','2022-10-08 21:56:52',35,'EXECUTED','8:e91969bc7bdab32c4026bf3001131312','createTable tableName=webshop','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220306131500-2','dokd','db/changelog/20220306131500_add_entity_webshop.xml','2022-10-08 21:56:52',36,'EXECUTED','8:7822d88302d102e6dca8716813fd5fcf','addColumn tableName=webshop','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220306131500-3','dokd','db/changelog/20220306131500_add_entity_webshop.xml','2022-10-08 21:56:52',37,'EXECUTED','8:23b3182b54b1137ad46142d9aa74fb49','addColumn tableName=webshop','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220306131500-4','dokd','db/changelog/20220306131500_add_entity_webshop.xml','2022-10-08 21:56:52',38,'EXECUTED','8:62a3f75c001bfe184b215f800fdfbff2','dropColumn columnName=item_id, tableName=webshop','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220306131600-1','dokd','db/changelog/20220306131500_add_entity_webshop_log.xml','2022-10-08 21:56:52',39,'EXECUTED','8:8517c80526dab0455d09e28d2552db15','createTable tableName=webshop_log','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220306131600-2','dokd','db/changelog/20220306131500_add_entity_webshop_log.xml','2022-10-08 21:56:52',40,'EXECUTED','8:967604e424ddafd2bfd4ad6c43fc5860','addColumn tableName=webshop_log','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220306131600-3','dokd','db/changelog/20220306131500_add_entity_webshop_log.xml','2022-10-08 21:56:52',41,'EXECUTED','8:b16562a80976c4b1d322735c546d5f1e','dropColumn columnName=id_item, tableName=webshop_log','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220306131600-4','dokd','db/changelog/20220306131500_add_entity_webshop_log.xml','2022-10-08 21:56:52',42,'EXECUTED','8:efcd7971d8b0f94f71ff341a8d1b9e80','addColumn tableName=webshop_log','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220309194500-1','dokd','db/changelog/20191109112000_add_entity_gift_daily_log.xml','2022-10-08 21:56:52',43,'EXECUTED','8:068653327571250ed73b02ac205587a9','createTable tableName=gift_daily_log','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20220309194500-2','dokd','db/changelog/20191109112000_add_entity_gift_daily_log.xml','2022-10-08 21:56:52',44,'EXECUTED','8:e8fea26943345b4406b91ef64ce85d3c','modifyDataType columnName=member_id, tableName=gift_daily_log','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190323134000-1','dokd','db/changelog/20220322225000_add_entity_activity.xml','2022-10-08 21:56:52',45,'EXECUTED','8:192c99e81874f1c30df2121304bd5a0a','createTable tableName=activity','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190323134001-1','dokd','db/changelog/20220322225000_add_entity_activity_item.xml','2022-10-08 21:56:52',46,'EXECUTED','8:ab83db8e1ed36b5c60dea5ca5830e5b8','createTable tableName=activity_item','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190323134001-2','dokd','db/changelog/20220322225000_add_entity_activity_item.xml','2022-10-08 21:56:52',47,'EXECUTED','8:0e969b05dd7dc4eb15b5ba31551f4ada','addColumn tableName=activity_item','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190323134002-1','dokd','db/changelog/20220322225000_add_entity_activity_progress.xml','2022-10-08 21:56:52',48,'EXECUTED','8:c771295980eae7145d109abb2a6369d9','createTable tableName=activity_progress','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190323134002-2','dokd','db/changelog/20220322225000_add_entity_activity_progress.xml','2022-10-08 21:56:52',49,'EXECUTED','8:0d935ec8510e445f36308cab3564e0f4','modifyDataType columnName=player_id, tableName=activity_progress','',NULL,'3.8.9',NULL,NULL,'5241010984'),('20190323134002-3','dokd','db/changelog/20220322225000_add_entity_activity_progress.xml','2022-10-08 21:56:52',50,'EXECUTED','8:b0cf60a3a31b4c2fd4c4531e5cc4c52b','addColumn tableName=activity_progress','',NULL,'3.8.9',NULL,NULL,'5241010984');
/*!40000 ALTER TABLE `databasechangelog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `databasechangeloglock`
--

LOCK TABLES `databasechangeloglock` WRITE;
/*!40000 ALTER TABLE `databasechangeloglock` DISABLE KEYS */;
INSERT INTO `databasechangeloglock` VALUES (1,'\0',NULL,NULL);
/*!40000 ALTER TABLE `databasechangeloglock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `app_properties`
--

LOCK TABLES `app_properties` WRITE;
/*!40000 ALTER TABLE `app_properties` DISABLE KEYS */;
INSERT INTO `app_properties` VALUES (1,'CHARGE_CARD','1','Rate charge card',NULL,'2022-10-08 21:57:12',NULL,'2022-10-08 21:57:12'),(2,'CHARGE_MOMO','1.5','Rate charge momo',NULL,'2022-10-08 21:57:12',NULL,'2022-10-08 21:57:12'),(3,'GM_CODE','123456799','GM Code.',NULL,'2022-10-08 21:57:12',NULL,'2022-10-08 21:57:12'),(4,'LINK_IOS','http://link-ios.com','Link tải ios.',NULL,'2022-10-08 21:57:12',NULL,'2022-10-08 21:57:12'),(5,'RATE_DRAW','100000','Tỉ lệ đổi qua vòng quay.',NULL,'2022-10-08 21:57:12',NULL,'2022-10-08 21:57:12');
/*!40000 ALTER TABLE `app_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `prize`
--

LOCK TABLES `prize` WRITE;
/*!40000 ALTER TABLE `prize` DISABLE KEYS */;
INSERT INTO `prize` VALUES (1,'100.000 Xu','100.png',99999,0.02,100000),(2,'50.000 Xu','10.png',99999,0.03,50000),(3,'20.000 Xu','20.png',99999,0.05,20000),(4,'10.000 Xu','50.png',99999,0.1,10000),(5,'Chúc bạn may mắn lần sau','miss.png',99999,0.8,0);
/*!40000 ALTER TABLE `prize` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'USER',NULL,'2022-10-08 21:57:11',NULL,'2022-10-08 21:57:11'),(2,'ADMIN',NULL,'2022-10-08 21:57:12',NULL,'2022-10-08 21:57:12');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `version`
--

LOCK TABLES `version` WRITE;
/*!40000 ALTER TABLE `version` DISABLE KEYS */;
INSERT INTO `version` VALUES (1,'9.9.9');
/*!40000 ALTER TABLE `version` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-10 21:13:37
