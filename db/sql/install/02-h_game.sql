-- h_game : schema 45 bang du lieu dong (nhan vat, mail, guild...) - RONG
-- kem data 8 bang cau hinh hoat dong/goi nap.
-- Da bo bang rac t_mail_copy.

-- MySQL dump 10.13  Distrib 5.6.49, for Win64 (x86_64)
--
-- Host: localhost    Database: h_game
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
-- Table structure for table `t_account`
--

DROP TABLE IF EXISTS `t_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_account` (
  `uid` bigint(20) NOT NULL,
  `accountId` varchar(255) NOT NULL COMMENT '渠道账号',
  `createTime` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `ip` varchar(255) DEFAULT NULL,
  `lastLoginTime` bigint(20) DEFAULT NULL COMMENT '最近登录时间',
  `createServer` int(11) DEFAULT NULL COMMENT '账号创建时所在游戏服id',
  `currentServer` int(11) DEFAULT NULL COMMENT '当前所在游戏服id',
  `areaId` int(11) DEFAULT NULL COMMENT '渠道id',
  `channel` varchar(255) DEFAULT NULL,
  `isforbid` int(11) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `t_useId` (`uid`) USING BTREE,
  KEY `t_username` (`accountId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_activity_data`
--

DROP TABLE IF EXISTS `t_activity_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_activity_data` (
  `id` varchar(255) NOT NULL,
  `type` int(11) NOT NULL,
  `dataString` longtext,
  PRIMARY KEY (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_activity_data2`
--

DROP TABLE IF EXISTS `t_activity_data2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_activity_data2` (
  `id` varchar(255) NOT NULL,
  `type` int(11) DEFAULT NULL,
  `dataString` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_activity_template`
--

DROP TABLE IF EXISTS `t_activity_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_activity_template` (
  `type` int(11) DEFAULT NULL,
  `jsonString` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_arena_ranking`
--

DROP TABLE IF EXISTS `t_arena_ranking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_arena_ranking` (
  `t_ranking_id` int(11) NOT NULL,
  `t_player_id` bigint(20) NOT NULL,
  PRIMARY KEY (`t_ranking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_champion_rank`
--

DROP TABLE IF EXISTS `t_champion_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_champion_rank` (
  `playerId` bigint(20) NOT NULL,
  `maxFloor` int(11) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_cross_indigo_data`
--

DROP TABLE IF EXISTS `t_cross_indigo_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_cross_indigo_data` (
  `id` int(11) NOT NULL,
  `bePoured` longtext,
  `lastfinalRace` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_cross_indigo_day_rank`
--

DROP TABLE IF EXISTS `t_cross_indigo_day_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_cross_indigo_day_rank` (
  `session` int(11) NOT NULL,
  `totalNum` int(11) DEFAULT NULL,
  `guildNum` longtext,
  `data` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_cross_indigo_parti`
--

DROP TABLE IF EXISTS `t_cross_indigo_parti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_cross_indigo_parti` (
  `id` int(11) NOT NULL,
  `data` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_cross_indigo_rank`
--

DROP TABLE IF EXISTS `t_cross_indigo_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_cross_indigo_rank` (
  `rank` int(11) NOT NULL,
  `data` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_cross_pour_info`
--

DROP TABLE IF EXISTS `t_cross_pour_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_cross_pour_info` (
  `playerId` bigint(20) NOT NULL,
  `pouredId` bigint(20) DEFAULT NULL,
  `pourType` int(11) DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_custom_package`
--

DROP TABLE IF EXISTS `t_custom_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_custom_package` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `desc` varchar(1024) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `itemStr` longtext,
  `serverName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_deadline_hero_config`
--

DROP TABLE IF EXISTS `t_deadline_hero_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_deadline_hero_config` (
  `id` varchar(255) NOT NULL,
  `serverName` varchar(255) DEFAULT NULL,
  `startTime` varchar(20) DEFAULT NULL,
  `endTime` varchar(20) DEFAULT NULL,
  `heroId` int(11) DEFAULT NULL,
  `consume` int(11) DEFAULT NULL,
  `consume5` int(11) DEFAULT NULL,
  `freeTime` int(11) DEFAULT NULL,
  `rankRewardStr` longtext,
  `jackpot` longtext,
  `extraJackpot` longtext,
  `rewardShowStr` longtext,
  `rewardBoxStr` longtext,
  `open` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_extra_drop`
--

DROP TABLE IF EXISTS `t_extra_drop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_extra_drop` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `timeType` int(11) DEFAULT NULL,
  `activeTime` longtext,
  `openServerTime` longtext,
  `weight` int(11) DEFAULT NULL,
  `imgUrl` longtext,
  `itemStr` longtext,
  `info` longtext,
  `open` int(11) DEFAULT NULL,
  `jumpId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_fight_report`
--

DROP TABLE IF EXISTS `t_fight_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_fight_report` (
  `uid` bigint(20) NOT NULL,
  `fightType` int(11) DEFAULT NULL,
  `attUid` bigint(20) DEFAULT NULL,
  `defUid` bigint(20) DEFAULT NULL,
  `attName` varchar(100) DEFAULT NULL,
  `defName` varchar(100) DEFAULT NULL,
  `attCamp` longtext,
  `defCamp` longtext,
  `randomNum` varchar(255) DEFAULT NULL,
  `round` int(11) DEFAULT NULL,
  `maxRound` int(11) DEFAULT NULL,
  `result` int(11) DEFAULT NULL,
  `star` int(11) DEFAULT NULL,
  `orders` longtext,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_flash_sale`
--

DROP TABLE IF EXISTS `t_flash_sale`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_flash_sale` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `timeType` int(11) DEFAULT NULL,
  `activeTime` longtext,
  `openServerTime` longtext,
  `weight` int(11) DEFAULT NULL,
  `imgUrl` longtext,
  `itemStr` longtext,
  `open` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_friend`
--

DROP TABLE IF EXISTS `t_friend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_friend` (
  `playerId` bigint(20) NOT NULL,
  `data` longtext,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_game_global`
--

DROP TABLE IF EXISTS `t_game_global`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_game_global` (
  `t_id` int(11) NOT NULL,
  `t_intVal` int(11) NOT NULL,
  `t_strVal` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`t_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_general_activity_config`
--

DROP TABLE IF EXISTS `t_general_activity_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_general_activity_config` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `showType` int(20) DEFAULT NULL,
  `activityType` int(20) DEFAULT NULL,
  `timeType` int(20) DEFAULT NULL,
  `weight` int(20) DEFAULT NULL,
  `status` int(20) DEFAULT NULL,
  `resetType` int(20) DEFAULT NULL,
  `resetExpression` varchar(255) DEFAULT NULL,
  `startTime` varchar(20) DEFAULT NULL,
  `endTime` varchar(20) DEFAULT NULL,
  `openingTime` varchar(20) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `info` varchar(255) DEFAULT NULL,
  `itemList` longtext,
  `serverName` varchar(255) DEFAULT NULL,
  `open` int(20) DEFAULT NULL,
  `params` varchar(1024) DEFAULT NULL,
  `diffType` int(20) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_guild`
--

DROP TABLE IF EXISTS `t_guild`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_guild` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  `notice` varchar(400) DEFAULT NULL,
  `chairmanId` bigint(20) DEFAULT NULL,
  `data` longtext,
  `createTime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_guildwar_data`
--

DROP TABLE IF EXISTS `t_guildwar_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_guildwar_data` (
  `id` varchar(255) NOT NULL DEFAULT '',
  `type` int(11) NOT NULL DEFAULT '0',
  `dataString` longtext,
  PRIMARY KEY (`id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_gym_limit_shops`
--

DROP TABLE IF EXISTS `t_gym_limit_shops`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_gym_limit_shops` (
  `id` bigint(20) NOT NULL DEFAULT '0',
  `type` int(11) DEFAULT NULL,
  `time` varchar(128) DEFAULT NULL,
  `serverIds` text,
  `descStr` varchar(255) DEFAULT NULL,
  `data` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_gym_shops`
--

DROP TABLE IF EXISTS `t_gym_shops`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_gym_shops` (
  `guildId` bigint(20) NOT NULL DEFAULT '0',
  `isBreak` tinyint(4) DEFAULT NULL,
  `data` text,
  PRIMARY KEY (`guildId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_huntteam`
--

DROP TABLE IF EXISTS `t_huntteam`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_huntteam` (
  `id` bigint(20) NOT NULL,
  `data` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_indigo_data`
--

DROP TABLE IF EXISTS `t_indigo_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_indigo_data` (
  `id` int(11) NOT NULL,
  `bePoured` longtext,
  `lastfinalRace` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_indigo_parti`
--

DROP TABLE IF EXISTS `t_indigo_parti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_indigo_parti` (
  `id` int(11) NOT NULL,
  `data` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_indigo_rank`
--

DROP TABLE IF EXISTS `t_indigo_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_indigo_rank` (
  `rank` int(11) NOT NULL,
  `data` longtext,
  PRIMARY KEY (`rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_limittime_exchange`
--

DROP TABLE IF EXISTS `t_limittime_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_limittime_exchange` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `timeType` int(11) DEFAULT NULL,
  `activeTime` longtext,
  `openServerTime` longtext,
  `weight` int(11) DEFAULT NULL,
  `imgUrl` longtext,
  `itemStr` longtext,
  `open` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_luckwheel_config`
--

DROP TABLE IF EXISTS `t_luckwheel_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_luckwheel_config` (
  `id` varchar(255) NOT NULL,
  `serverName` varchar(255) DEFAULT NULL,
  `startTime` varchar(20) DEFAULT NULL,
  `endTime` varchar(20) DEFAULT NULL,
  `joinTimes` int(11) DEFAULT NULL,
  `consumeStr` varchar(255) DEFAULT NULL,
  `rewardStr` longtext,
  `vipStr` varchar(255) DEFAULT NULL,
  `open` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_mail`
--

DROP TABLE IF EXISTS `t_mail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_mail` (
  `id` bigint(20) NOT NULL,
  `type` tinyint(3) DEFAULT NULL,
  `state` tinyint(3) DEFAULT NULL,
  `receiveId` bigint(20) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `isAdjunct` tinyint(3) DEFAULT NULL,
  `data` longtext,
  `sendTime` datetime DEFAULT NULL,
  `delTime` datetime DEFAULT NULL,
  `reason` int(11) DEFAULT NULL,
  `reasonExtra` varchar(255) DEFAULT NULL,
  `param` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_mystic_hero_config`
--

DROP TABLE IF EXISTS `t_mystic_hero_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_mystic_hero_config` (
  `id` varchar(255) NOT NULL,
  `serverName` varchar(255) DEFAULT NULL,
  `startTime` varchar(20) DEFAULT NULL,
  `endTime` varchar(20) DEFAULT NULL,
  `consume` int(11) DEFAULT NULL,
  `vip` int(11) DEFAULT NULL,
  `consume5` int(11) DEFAULT NULL,
  `vip5` int(11) DEFAULT NULL,
  `heroId` int(11) DEFAULT NULL,
  `heroIcon` varchar(255) DEFAULT NULL,
  `extraHeroId` varchar(255) DEFAULT NULL,
  `extraHeroIconStr` varchar(255) DEFAULT NULL,
  `rewardStr` longtext,
  `extraRewardStr` varchar(255) DEFAULT NULL,
  `open` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_player`
--

DROP TABLE IF EXISTS `t_player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_player` (
  `playerId` bigint(20) NOT NULL,
  `accountUid` bigint(20) DEFAULT NULL,
  `createServer` int(20) DEFAULT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `level` int(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `isForbid` int(11) DEFAULT NULL,
  `lastLoginTime` bigint(20) DEFAULT NULL,
  `currentServer` int(11) DEFAULT NULL,
  `onlineTime` bigint(20) DEFAULT NULL,
  `data` longtext,
  `offlineTime` bigint(20) DEFAULT NULL,
  `gold` bigint(20) DEFAULT NULL,
  `diamond` bigint(20) DEFAULT NULL,
  `rechargeDiamond` bigint(20) DEFAULT NULL,
  `vipLevel` int(11) DEFAULT NULL,
  `forbidEndTime` bigint(20) DEFAULT '0',
  `levelUpTime` bigint(20) DEFAULT '0',
  `image` int(20) DEFAULT NULL,
  `imageFrame` int(20) DEFAULT NULL,
  `imgbg` int(20) DEFAULT NULL,
  `playerType` int(20) DEFAULT NULL,
  `maxPower` bigint(20) DEFAULT NULL,
  `power` bigint(20) DEFAULT NULL,
  `powerTime` bigint(20) DEFAULT NULL,
  `allStars` int(20) DEFAULT NULL,
  `allStarsTime` bigint(20) DEFAULT NULL,
  `praisedNum` int(20) DEFAULT NULL,
  `powerestHeroId` varchar(255) DEFAULT NULL,
  `herodexLvl` int(20) DEFAULT NULL,
  `herodexLvlTime` bigint(20) DEFAULT NULL,
  `vsTowerPoint` int(11) DEFAULT NULL,
  `formation` varchar(255) DEFAULT NULL,
  `activeMoonCard` tinyint(1) DEFAULT '0',
  `doubleMoonCard` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`playerId`),
  UNIQUE KEY `playerId` (`playerId`) USING BTREE,
  KEY `accountUid` (`accountUid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_pour_info`
--

DROP TABLE IF EXISTS `t_pour_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_pour_info` (
  `playerId` bigint(20) NOT NULL,
  `pouredId` bigint(20) DEFAULT NULL,
  `pourType` int(11) DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_recharge`
--

DROP TABLE IF EXISTS `t_recharge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_recharge` (
  `orderId` varchar(255) NOT NULL,
  `playerId` bigint(20) DEFAULT NULL,
  `productId` int(11) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `singleMount` float DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `times` bigint(20) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `ipAddress` varchar(255) DEFAULT NULL,
  `platformOrderNo` varchar(255) DEFAULT NULL,
  `shouldMount` float DEFAULT NULL,
  `extInfo` text,
  PRIMARY KEY (`orderId`),
  UNIQUE KEY `orderId` (`orderId`) USING BTREE,
  KEY `playerId` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_slot_machines_config`
--

DROP TABLE IF EXISTS `t_slot_machines_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_slot_machines_config` (
  `id` varchar(255) NOT NULL,
  `serverName` varchar(255) DEFAULT NULL,
  `startTime` varchar(255) DEFAULT NULL,
  `endTime` varchar(255) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `score10` int(11) DEFAULT NULL,
  `consume` int(11) DEFAULT NULL,
  `consume10` int(11) DEFAULT NULL,
  `freeTime` int(11) DEFAULT NULL,
  `banner` varchar(255) DEFAULT NULL,
  `gacha` longtext,
  `boxConfigs` longtext,
  `rankConfigs` longtext,
  `open` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_snatch_territory_rank`
--

DROP TABLE IF EXISTS `t_snatch_territory_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_snatch_territory_rank` (
  `id` bigint(20) NOT NULL DEFAULT '0',
  `isGuild` tinyint(4) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `winNum` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_snatchterritory`
--

DROP TABLE IF EXISTS `t_snatchterritory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_snatchterritory` (
  `id` int(11) NOT NULL DEFAULT '0',
  `data` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_super_supply_config_data`
--

DROP TABLE IF EXISTS `t_super_supply_config_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_super_supply_config_data` (
  `id` varchar(255) NOT NULL,
  `timeType` int(11) DEFAULT NULL,
  `productId` int(11) DEFAULT NULL,
  `startDate` varchar(255) DEFAULT NULL,
  `endDate` varchar(255) DEFAULT NULL,
  `items` longtext,
  `open` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_taskbase`
--

DROP TABLE IF EXISTS `t_taskbase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_taskbase` (
  `taskId` int(11) NOT NULL DEFAULT '0',
  `type` int(11) DEFAULT NULL,
  `taskType` int(11) DEFAULT NULL,
  `progress` int(11) DEFAULT NULL,
  `canAwarded` tinyint(4) DEFAULT NULL,
  `finished` tinyint(4) DEFAULT NULL,
  `opened` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`taskId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_time_limit_activity`
--

DROP TABLE IF EXISTS `t_time_limit_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_time_limit_activity` (
  `activeId` int(11) NOT NULL,
  `startTime` bigint(20) DEFAULT '0',
  `endTime` bigint(20) DEFAULT '0',
  `overStatus` int(11) DEFAULT '0',
  `intervalTime` int(11) DEFAULT '0',
  `randomStatus` int(11) DEFAULT '0',
  `filters` varchar(255) DEFAULT NULL,
  `infoData` text,
  PRIMARY KEY (`activeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_vs_tower_rank`
--

DROP TABLE IF EXISTS `t_vs_tower_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_vs_tower_rank` (
  `playerId` bigint(20) NOT NULL,
  `point` int(11) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_wish_poll`
--

DROP TABLE IF EXISTS `t_wish_poll`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_wish_poll` (
  `id` varchar(255) NOT NULL,
  `serverName` varchar(255) DEFAULT NULL,
  `startTime` varchar(20) DEFAULT NULL,
  `endTime` varchar(20) DEFAULT NULL,
  `rechargeNum` int(11) DEFAULT NULL,
  `rewardStr` varchar(1024) DEFAULT NULL,
  `open` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_world_answer`
--

DROP TABLE IF EXISTS `t_world_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_world_answer` (
  `id` varchar(255) NOT NULL,
  `timeType` int(11) DEFAULT NULL,
  `openServerTime` varchar(255) DEFAULT NULL,
  `openTime` varchar(1024) DEFAULT NULL,
  `showTime` varchar(255) DEFAULT NULL,
  `seconds` int(11) DEFAULT NULL,
  `open` int(11) DEFAULT NULL,
  `reward` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_world_boss_guild_rank`
--

DROP TABLE IF EXISTS `t_world_boss_guild_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_world_boss_guild_rank` (
  `guildId` bigint(20) NOT NULL,
  `guildName` varchar(255) DEFAULT NULL,
  `hurt` bigint(20) DEFAULT NULL,
  `hurtPercent` int(11) DEFAULT NULL,
  `attAdd` text,
  `reward` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`guildId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_world_boss_rank`
--

DROP TABLE IF EXISTS `t_world_boss_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_world_boss_rank` (
  `playerId` bigint(20) NOT NULL,
  `hurt` bigint(20) DEFAULT NULL,
  `hurtPercent` int(11) DEFAULT NULL,
  `fightCount` int(11) DEFAULT NULL,
  `inspireCount` int(11) DEFAULT NULL,
  `reliveCount` int(11) DEFAULT NULL,
  `attAdd` int(11) DEFAULT NULL,
  `reward` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-10 21:12:48

-- ---------- DATA: bang cau hinh ----------
-- MySQL dump 10.13  Distrib 5.6.49, for Win64 (x86_64)
--
-- Host: localhost    Database: h_game
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
-- Dumping data for table `t_custom_package`
--

LOCK TABLES `t_custom_package` WRITE;
/*!40000 ALTER TABLE `t_custom_package` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_custom_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_general_activity_config`
--

LOCK TABLES `t_general_activity_config` WRITE;
/*!40000 ALTER TABLE `t_general_activity_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_general_activity_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_deadline_hero_config`
--

LOCK TABLES `t_deadline_hero_config` WRITE;
/*!40000 ALTER TABLE `t_deadline_hero_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_deadline_hero_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_limittime_exchange`
--

LOCK TABLES `t_limittime_exchange` WRITE;
/*!40000 ALTER TABLE `t_limittime_exchange` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_limittime_exchange` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_luckwheel_config`
--

LOCK TABLES `t_luckwheel_config` WRITE;
/*!40000 ALTER TABLE `t_luckwheel_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_luckwheel_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_mystic_hero_config`
--

LOCK TABLES `t_mystic_hero_config` WRITE;
/*!40000 ALTER TABLE `t_mystic_hero_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_mystic_hero_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_slot_machines_config`
--

LOCK TABLES `t_slot_machines_config` WRITE;
/*!40000 ALTER TABLE `t_slot_machines_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_slot_machines_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `t_super_supply_config_data`
--

LOCK TABLES `t_super_supply_config_data` WRITE;
/*!40000 ALTER TABLE `t_super_supply_config_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_super_supply_config_data` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-10 21:12:48
