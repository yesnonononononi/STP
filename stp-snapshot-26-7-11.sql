-- MySQL dump 10.13  Distrib 8.4.6, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: stp
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ 'e30124de-1d27-11f1-a819-0e28aaa25cc5:1-6249';

--
-- Table structure for table `comment_image`
--

DROP TABLE IF EXISTS `comment_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL COMMENT '所属评论ID',
  `image_url` varchar(512) NOT NULL,
  `width` smallint DEFAULT NULL,
  `height` smallint DEFAULT NULL,
  `size` int DEFAULT NULL,
  `sort_order` tinyint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `name` varchar(50) DEFAULT NULL COMMENT '图片名称',
  PRIMARY KEY (`id`),
  KEY `idx_comment_id` (`comment_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论图片关联表（逻辑外键）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_image`
--

LOCK TABLES `comment_image` WRITE;
/*!40000 ALTER TABLE `comment_image` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_like`
--

DROP TABLE IF EXISTS `comment_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论点赞关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_like`
--

LOCK TABLES `comment_like` WRITE;
/*!40000 ALTER TABLE `comment_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comments` (
  `id` bigint NOT NULL COMMENT '评论ID',
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论',
  `root_id` bigint DEFAULT NULL COMMENT '根评论ID',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `is_audit` tinyint(1) DEFAULT '0' COMMENT '是否审核: 0否, 1是',
  `type` int NOT NULL DEFAULT '1' COMMENT '评论类型: 1文字, 2图片, 3视频, 4音频',
  `is_top` int NOT NULL DEFAULT '0' COMMENT '是否置顶: 0否, 1是',
  `status` int NOT NULL DEFAULT '1' COMMENT '评论状态: 0已删除, 1正常',
  `extra` text COLLATE utf8mb4_unicode_ci COMMENT '额外扩展信息 (JSON 格式，包含媒体类型、图片元数据、媒体URL等)',
  `reply_count` int NOT NULL DEFAULT '0' COMMENT '回复数/子评论数',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `ip_location` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP归属地',
  `client_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端类型: ios, android, web等',
  `hot_score` bigint NOT NULL DEFAULT '0',
  `like_count` bigint NOT NULL DEFAULT '0' COMMENT '点赞数',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_root_id` (`root_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_updatetime_id` (`status`,`update_time`,`id`),
  KEY `idx_status_parent_root_updatetime` (`status`,`parent_id`,`root_id`,`update_time`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表（支持回复）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--

LOCK TABLES `comments` WRITE;
/*!40000 ALTER TABLE `comments` DISABLE KEYS */;
INSERT INTO `comments` VALUES (2075485885268365312,2072335787806044160,1,NULL,NULL,'[可达鸭]','2026-07-10 15:42:50',1,3,0,1,'{\n}',0,'2026-07-10 15:42:50','127.0.0.1','web',0,0),(2078734760670384128,2077682878208159744,2,NULL,NULL,'[柯基][柯基][柯基]','2026-07-19 14:52:43',1,3,0,1,'{\n}',1,'2026-07-19 09:56:00','127.0.0.1','web',1,0),(2078735772390928384,2077682878208159744,2,NULL,NULL,'[仓鼠][仓鼠]','2026-07-19 14:56:44',1,3,0,1,'{\n}',1,'2026-07-19 09:48:00','127.0.0.1','web',1,0),(2078735852623769600,2077682878208159744,1,NULL,NULL,'[可达鸭]','2026-07-19 14:57:03',1,3,0,1,'{\n}',0,'2026-07-19 14:57:03','127.0.0.1','web',0,0),(2078778270878416896,2077682878208159744,1,2078735772390928384,2078735772390928384,'thank you my friend','2026-07-19 17:45:37',1,3,0,1,NULL,0,'2026-07-19 17:45:37','127.0.0.1','web',0,0),(2078779223463575552,2077682878208159744,1,2078734760670384128,2078734760670384128,'thank you my friend[哈士奇]','2026-07-19 17:49:23',1,3,0,1,NULL,0,'2026-07-19 17:49:23','127.0.0.1','web',0,0);
/*!40000 ALTER TABLE `comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon`
--

DROP TABLE IF EXISTS `coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` varchar(128) NOT NULL COMMENT '优惠券名称',
  `discount` decimal(3,2) DEFAULT NULL COMMENT '折扣比例 (如 0.85)',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '优惠金额',
  `type` int DEFAULT '0' COMMENT '优惠券类型: 0折扣, 1金额',
  `status` int DEFAULT '1' COMMENT '状态: 1可用, 0禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `scope_type` int NOT NULL DEFAULT '1' COMMENT '1 全场通用  2指定商品分类 3指定商品',
  `time_type` tinyint DEFAULT '2' COMMENT '有效时间类型 1-固定时间段 2-领取后生效(按天) 3-领取后生效(按小时)',
  `valid_days` int DEFAULT NULL COMMENT '领取后有效天数',
  `valid_hours` int DEFAULT NULL COMMENT '领取后有效小时',
  `image` varchar(500) DEFAULT NULL,
  `description` varchar(400) NOT NULL DEFAULT '计算规则: 商品价格 * 优惠券折扣 - 优惠卷金额' COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon`
--

LOCK TABLES `coupon` WRITE;
/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` VALUES (1,'年中大促全场通用满减券',NULL,50.00,1,1,'2026-06-18 15:11:31','2026-06-18 15:11:31',1,1,NULL,NULL,NULL,'全场商品通用满减券，活动期间内有效'),(2,'全场通用大额折扣券(7天)',0.85,NULL,0,1,'2026-06-18 15:11:31','2026-06-21 09:36:43',2,2,7,NULL,NULL,'自领券之日起7天内全场通用，享受85折优惠'),(3,'数码家电限时特惠神券',NULL,100.00,1,1,'2026-06-18 15:11:31','2026-06-18 15:11:31',1,3,NULL,12,NULL,'自领券之日起12小时内有效，数码品类专享');
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon_activity`
--

DROP TABLE IF EXISTS `coupon_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_activity` (
  `id` bigint NOT NULL,
  `coupon_id` bigint NOT NULL,
  `name` varchar(128) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `activity_start_time` datetime NOT NULL,
  `activity_end_time` datetime NOT NULL,
  `status` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `limit_quantity` int NOT NULL DEFAULT '1' COMMENT '本活动单优惠券单人可领取上限',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1 普通活动 2 秒杀活动',
  PRIMARY KEY (`id`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_activity`
--

LOCK TABLES `coupon_activity` WRITE;
/*!40000 ALTER TABLE `coupon_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `coupon_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon_use_scope`
--

DROP TABLE IF EXISTS `coupon_use_scope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_use_scope` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券模板ID',
  `relation_id` bigint NOT NULL COMMENT '关联的实体ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_relation_id` (`relation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券可用范围关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_use_scope`
--

LOCK TABLES `coupon_use_scope` WRITE;
/*!40000 ALTER TABLE `coupon_use_scope` DISABLE KEYS */;
INSERT INTO `coupon_use_scope` VALUES (1,1,1,'2026-06-16 09:58:36'),(2,2,1,'2026-06-21 09:36:55');
/*!40000 ALTER TABLE `coupon_use_scope` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `creator_rank`
--

DROP TABLE IF EXISTS `creator_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `creator_rank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `score` decimal(10,2) NOT NULL COMMENT '鍒嗘暟',
  `rank` int NOT NULL COMMENT '鎺掑悕',
  `period_date` date NOT NULL COMMENT '鍛ㄦ湡鏃堕棿锛堝懆涓?棩鏈燂級',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_period` (`user_id`,`period_date`),
  KEY `idx_period` (`period_date`)
) ENGINE=InnoDB AUTO_INCREMENT=2416 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鍒涗綔鑰呮?鍗';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `creator_rank`
--

LOCK TABLES `creator_rank` WRITE;
/*!40000 ALTER TABLE `creator_rank` DISABLE KEYS */;
INSERT INTO `creator_rank` VALUES (109,1,1.00,1,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(110,2,1.00,2,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(111,3,0.00,3,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(375,2,1.00,1,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(376,3,0.00,2,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(2413,1,2.00,1,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(2414,2,1.00,2,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(2415,3,0.00,3,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00');
/*!40000 ALTER TABLE `creator_rank` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emoji`
--

DROP TABLE IF EXISTS `emoji`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emoji` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `url` varchar(512) NOT NULL,
  `tiny` varchar(512) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `type` tinyint NOT NULL DEFAULT '1' COMMENT 'image 1 gif 2 ',
  `package_id` bigint DEFAULT NULL COMMENT '表情包id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emoji`
--

LOCK TABLES `emoji` WRITE;
/*!40000 ALTER TABLE `emoji` DISABLE KEYS */;
INSERT INTO `emoji` VALUES (121,'[三花猫]','<img src=\'http://localhost:9000/stp-summit-files/emoji/三花猫.png\' alt=\'三花猫\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(122,'[仓鼠]','<img src=\'http://localhost:9000/stp-summit-files/emoji/仓鼠.png\' alt=\'仓鼠\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(123,'[可达鸭]','<img src=\'http://localhost:9000/stp-summit-files/emoji/可达鸭.png\' alt=\'可达鸭\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(124,'[哈士奇]','<img src=\'http://localhost:9000/stp-summit-files/emoji/哈士奇.png\' alt=\'哈士奇\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(125,'[奶牛猫]','<img src=\'http://localhost:9000/stp-summit-files/emoji/奶牛猫.png\' alt=\'奶牛猫\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(126,'[布偶猫]','<img src=\'http://localhost:9000/stp-summit-files/emoji/布偶猫.png\' alt=\'布偶猫\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(127,'[无毛猫]','<img src=\'http://localhost:9000/stp-summit-files/emoji/无毛猫.png\' alt=\'无毛猫\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(128,'[暹罗猫]','<img src=\'http://localhost:9000/stp-summit-files/emoji/暹罗猫.png\' alt=\'暹罗猫\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(129,'[柯基]','<img src=\'http://localhost:9000/stp-summit-files/emoji/柯基.png\' alt=\'柯基\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(130,'[柴犬]','<img src=\'http://localhost:9000/stp-summit-files/emoji/柴犬.png\' alt=\'柴犬\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(131,'[橘猫]','<img src=\'http://localhost:9000/stp-summit-files/emoji/橘猫.png\' alt=\'橘猫\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(132,'[法斗]','<img src=\'http://localhost:9000/stp-summit-files/emoji/法斗.png\' alt=\'法斗\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(133,'[田园犬]','<img src=\'http://localhost:9000/stp-summit-files/emoji/田园犬.png\' alt=\'田园犬\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(134,'[白猫]','<img src=\'http://localhost:9000/stp-summit-files/emoji/白猫.png\' alt=\'白猫\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(135,'[腊肠犬]','<img src=\'http://localhost:9000/stp-summit-files/emoji/腊肠犬.png\' alt=\'腊肠犬\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(136,'[荷兰猪]','<img src=\'http://localhost:9000/stp-summit-files/emoji/荷兰猪.png\' alt=\'荷兰猪\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(137,'[蓝猫]','<img src=\'http://localhost:9000/stp-summit-files/emoji/蓝猫.png\' alt=\'蓝猫\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(138,'[藏獒]','<img src=\'http://localhost:9000/stp-summit-files/emoji/藏獒.png\' alt=\'藏獒\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(139,'[边牧]','<img src=\'http://localhost:9000/stp-summit-files/emoji/边牧.png\' alt=\'边牧\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1),(140,'[黑猫]','<img src=\'http://localhost:9000/stp-summit-files/emoji/黑猫.png\' alt=\'黑猫\' class=\'w-6 h-6 inline-block align-bottom\' />',NULL,'2026-06-03 08:37:52',NULL,1,1);
/*!40000 ALTER TABLE `emoji` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emoji_package`
--

DROP TABLE IF EXISTS `emoji_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emoji_package` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '表情包名称',
  `description` varchar(1024) DEFAULT NULL COMMENT '表情包描述',
  `cover_image` varchar(512) DEFAULT NULL COMMENT '表情包封面图片',
  `status` tinyint DEFAULT '1' COMMENT '1-正常，2-禁用',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='表情包表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emoji_package`
--

LOCK TABLES `emoji_package` WRITE;
/*!40000 ALTER TABLE `emoji_package` DISABLE KEYS */;
INSERT INTO `emoji_package` VALUES (1,'快乐小宠物','小宠物快乐',NULL,1,'2026-06-03 07:26:33','2026-06-03 07:26:33');
/*!40000 ALTER TABLE `emoji_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member_level_config`
--

DROP TABLE IF EXISTS `member_level_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_level_config` (
  `level` int NOT NULL COMMENT 'VIP等级（1,2,3...）',
  `level_name` varchar(50) NOT NULL COMMENT '等级名称（如：黄金VIP）',
  `min_recharge` decimal(12,2) NOT NULL COMMENT '达到该等级所需的最小累计充值金额',
  `privileges_json` json NOT NULL COMMENT '特权配置（JSON格式）',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '等级图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`level`),
  UNIQUE KEY `uk_min_recharge` (`min_recharge`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='VIP等级配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_level_config`
--

LOCK TABLES `member_level_config` WRITE;
/*!40000 ALTER TABLE `member_level_config` DISABLE KEYS */;
INSERT INTO `member_level_config` VALUES (1,'初级客户',0.01,'{}','../../../../public/v1.png',0),(2,'中级牛马',0.05,'{}','../../../../public/v1.png',1);
/*!40000 ALTER TABLE `member_level_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member_package`
--

DROP TABLE IF EXISTS `member_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_package` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(128) NOT NULL COMMENT '商品名称',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `duration` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `description` varchar(200) DEFAULT NULL COMMENT '商品描述',
  `type_id` bigint DEFAULT NULL COMMENT '商品类型id',
  `stock` int NOT NULL DEFAULT '0',
  `discount` double DEFAULT '1' COMMENT '折扣',
  `daily_rate` decimal(10,4) NOT NULL COMMENT '日折算单价',
  `priority` int NOT NULL DEFAULT '1' COMMENT '会员优先级，越大等级越高',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员套餐表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_package`
--

LOCK TABLES `member_package` WRITE;
/*!40000 ALTER TABLE `member_package` DISABLE KEYS */;
INSERT INTO `member_package` VALUES (1,'单月会员',29.90,30,'2026-05-20 07:56:40','2026-06-16 09:22:49',NULL,1,99,0.88,0.0001,1),(2,'年卡会员',315.90,365,'2026-05-21 02:46:03','2026-06-04 03:52:18',NULL,1,0,1,0.0002,1),(3,'季卡会员',99.90,60,'2026-05-21 02:46:23','2026-06-04 03:52:18',NULL,1,0,1,0.0003,1),(4,'会员月卡',39.90,30,'2026-05-21 02:47:10','2026-06-04 03:52:18',NULL,2,0,1,0.0004,1),(5,'会员季卡',129.90,60,'2026-05-21 02:47:27','2026-06-04 03:52:18',NULL,2,0,1,0.0005,1),(6,'会员年卡',399.90,365,'2026-05-21 02:47:56','2026-06-04 03:52:18',NULL,2,0,1,0.0006,1);
/*!40000 ALTER TABLE `member_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_order`
--

DROP TABLE IF EXISTS `payment_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_order` (
  `id` bigint NOT NULL COMMENT '订单ID',
  `amount` decimal(10,2) NOT NULL COMMENT '实际支付总金额',
  `pay_type` int DEFAULT NULL COMMENT '支付类型: 1支付宝, 2微信',
  `creator_id` bigint NOT NULL COMMENT '下单人id',
  `status` int DEFAULT '0' COMMENT '订单状态: 0待支付, 1已支付, 2已完成 3已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `package_id` bigint NOT NULL COMMENT '会员套餐商品ID',
  `quantity` int DEFAULT NULL,
  `coupon_id` bigint DEFAULT NULL,
  `unit_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '购买时的单价快照',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '优惠券优惠金额快照',
  `pay_time` datetime DEFAULT NULL COMMENT '支付完成时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_order`
--

LOCK TABLES `payment_order` WRITE;
/*!40000 ALTER TABLE `payment_order` DISABLE KEYS */;
INSERT INTO `payment_order` VALUES (2058030400407601152,0.01,1,1,3,'2026-05-23 11:40:58','2026-05-23 11:42:00',1,1,NULL,0.01,0.00,NULL),(2058037522163826688,0.01,1,1,3,'2026-05-23 12:09:16','2026-05-23 12:10:20',1,1,NULL,0.01,0.00,NULL),(2058037580166856704,0.01,1,1,1,'2026-05-23 12:09:30','2026-05-23 12:09:44',1,1,NULL,0.01,0.00,'2026-05-23 12:09:44'),(2058037892835442688,0.01,1,1,3,'2026-05-23 12:10:45','2026-05-23 12:11:45',1,1,NULL,0.01,0.00,NULL),(2058038256880058368,0.01,1,1,3,'2026-05-23 12:12:12','2026-05-23 12:13:15',1,1,NULL,0.01,0.00,NULL),(2058038862860517376,0.01,1,1,3,'2026-05-23 12:14:36','2026-05-23 12:15:40',1,1,NULL,0.01,0.00,NULL),(2058085039295430656,0.01,1,1,1,'2026-05-23 15:18:05','2026-05-23 17:35:02',1,1,NULL,0.01,0.00,'2026-05-23 17:35:02'),(2058088256259489792,0.01,1,1,1,'2026-05-23 15:30:52','2026-05-23 15:31:15',1,1,NULL,0.01,0.00,'2026-05-23 15:31:15'),(2058091530014425088,0.01,1,1,1,'2026-05-23 15:43:53','2026-05-23 17:35:03',1,1,NULL,0.01,0.00,'2026-05-23 17:35:03'),(2058117241982943232,0.01,1,1,1,'2026-05-23 17:26:03','2026-05-23 17:35:03',1,1,NULL,0.01,0.00,'2026-05-23 17:35:03'),(2058119205512806400,0.01,1,1,1,'2026-05-23 17:33:51','2026-05-23 17:34:03',1,1,NULL,0.01,0.00,'2026-05-23 17:34:03'),(2058131330578776064,0.05,1,1,1,'2026-05-23 18:22:02','2026-05-23 18:22:30',4,1,NULL,0.05,0.00,'2026-05-23 18:22:30'),(2058131987553583104,0.01,1,1,3,'2026-05-23 18:24:39','2026-05-23 18:25:40',1,1,NULL,0.01,0.00,NULL),(2058132225894907904,0.05,1,1,1,'2026-05-23 18:25:36','2026-05-23 18:26:19',4,1,NULL,0.05,0.00,'2026-05-23 18:26:19'),(2058136152900108288,0.05,2,1,3,'2026-05-23 18:41:12','2026-05-23 18:42:15',4,1,NULL,0.05,0.00,NULL),(2058177259650416640,0.01,1,1,3,'2026-05-23 21:24:32','2026-05-23 21:25:35',1,1,NULL,0.01,0.00,NULL),(2058203119812083712,0.01,1,1,1,'2026-05-23 23:07:18','2026-05-23 23:32:49',1,1,NULL,0.01,0.00,'2026-05-23 23:32:49'),(2058204884087341056,0.01,1,1,1,'2026-05-23 23:14:19','2026-05-23 23:32:49',1,1,NULL,0.01,0.00,'2026-05-23 23:32:49'),(2058205527292248064,0.01,1,1,1,'2026-05-23 23:16:52','2026-05-23 23:32:49',1,1,NULL,0.01,0.00,'2026-05-23 23:32:49'),(2058206418829312000,0.01,1,1,1,'2026-05-23 23:20:25','2026-05-23 23:32:49',1,1,NULL,0.01,0.00,'2026-05-23 23:32:49'),(2058207344315072512,0.01,1,1,1,'2026-05-23 23:24:05','2026-05-23 23:32:49',1,1,NULL,0.01,0.00,'2026-05-23 23:32:49'),(2058208390793920512,0.01,1,1,1,'2026-05-23 23:28:15','2026-05-23 23:32:49',1,1,NULL,0.01,0.00,'2026-05-23 23:32:49'),(2058209940505362432,0.01,1,1,1,'2026-05-23 23:34:24','2026-05-23 23:34:48',1,1,NULL,0.01,0.00,'2026-05-23 23:34:48'),(2058210193811963904,0.05,1,1,1,'2026-05-23 23:35:25','2026-05-23 23:35:36',4,1,NULL,0.05,0.00,'2026-05-23 23:35:36'),(2058211744320323584,0.05,1,1,1,'2026-05-23 23:41:34','2026-05-23 23:41:55',4,1,NULL,0.05,0.00,'2026-05-23 23:41:55'),(2058212566756229120,0.02,1,1,3,'2026-05-23 23:44:50','2026-05-23 23:45:55',2,1,NULL,0.02,0.00,NULL),(2058212604433661952,0.03,1,1,1,'2026-05-23 23:44:59','2026-05-23 23:45:10',5,1,NULL,0.03,0.00,'2026-05-23 23:45:10'),(2058361948226977792,0.01,1,1,1,'2026-05-24 09:38:26','2026-05-24 09:39:08',1,1,NULL,0.01,0.00,'2026-05-24 09:39:08'),(2058367272484864000,0.03,1,1,1,'2026-05-24 09:59:35','2026-05-24 10:00:04',5,1,NULL,0.03,0.00,'2026-05-24 10:00:04'),(2058374879635308544,0.05,1,1,1,'2026-05-24 10:29:49','2026-05-24 10:35:03',4,1,NULL,0.05,0.00,'2026-05-24 10:35:03'),(2058448420510629888,0.01,1,1,1,'2026-05-24 15:22:02','2026-05-24 15:22:31',1,1,NULL,0.01,0.00,'2026-05-24 15:22:31'),(2058448681220177920,0.01,1,1,1,'2026-05-24 15:23:04','2026-05-24 15:23:40',1,1,NULL,0.01,0.00,'2026-05-24 15:23:40'),(2059242350042939392,0.01,1,1,1,'2026-05-26 19:56:50','2026-05-26 19:57:27',1,1,NULL,0.01,0.00,'2026-05-26 19:57:27'),(2059897979422113792,0.01,1,1,1,'2026-05-28 15:22:04','2026-05-28 15:22:28',1,1,NULL,0.01,0.00,'2026-05-28 15:22:28'),(2059900597825437696,0.01,1,1,1,'2026-05-28 15:32:28','2026-05-28 15:32:52',1,1,NULL,0.01,0.00,'2026-05-28 15:32:52'),(2059903638087663616,0.01,1,1,1,'2026-05-28 15:44:33','2026-05-28 15:45:36',1,1,NULL,0.01,0.00,'2026-05-28 15:45:36'),(2059907149894516736,0.01,1,1,1,'2026-05-28 15:58:30','2026-05-28 15:59:36',1,1,NULL,0.01,0.00,'2026-05-28 15:59:36'),(2059913171346391040,0.01,1,1,1,'2026-05-28 16:22:26','2026-05-28 16:23:31',1,1,NULL,0.01,0.00,'2026-05-28 16:23:31'),(2062094398027464704,0.01,1,1,3,'2026-06-03 16:49:51','2026-06-03 16:50:59',1,1,NULL,0.01,0.00,NULL),(2062858301778952192,29.90,1,1,3,'2026-06-05 19:25:20','2026-06-05 19:26:29',1,1,NULL,29.90,0.00,NULL),(2065984193489272832,29.90,1,1,3,'2026-06-14 10:26:31','2026-06-14 10:27:37',1,1,NULL,29.90,0.00,NULL),(2066364081752768512,29.90,1,1,3,'2026-06-15 11:36:03','2026-06-15 11:37:13',1,1,NULL,29.90,0.00,NULL),(2066833768609480704,0.00,1,1,3,'2026-06-16 18:42:25','2026-06-16 18:43:35',1,1,1,29.90,29.90,NULL),(2066834746377240576,0.00,1,1,1,'2026-06-16 18:46:18','2026-06-16 18:46:50',1,1,1,29.90,29.90,'2026-06-16 18:46:50'),(2066838053795659776,29.90,1,1,3,'2026-06-16 18:59:27','2026-06-16 19:00:33',1,1,NULL,29.90,0.00,NULL),(2067155476872691712,26.31,1,1,3,'2026-06-17 16:00:46','2026-06-17 16:01:51',1,1,NULL,29.90,3.59,NULL),(2070056869577097216,26.31,1,2,3,'2026-06-25 16:09:52','2026-06-25 16:10:56',1,1,NULL,29.90,3.59,NULL),(2070056870139133952,26.31,1,2,3,'2026-06-25 16:09:52','2026-06-25 16:10:56',1,1,NULL,29.90,3.59,NULL),(2075466284044124160,26.31,2,1,3,'2026-07-10 14:24:57','2026-07-10 14:26:03',1,1,NULL,29.90,3.59,NULL),(2075466406295502848,26.31,1,1,3,'2026-07-10 14:25:26','2026-07-10 14:26:31',1,1,NULL,29.90,3.59,NULL),(2075466543298248704,26.31,1,1,3,'2026-07-10 14:25:59','2026-07-10 14:27:01',1,1,NULL,29.90,3.59,NULL),(2075467435317657600,26.31,1,1,3,'2026-07-10 14:29:32','2026-07-10 14:31:16',1,1,NULL,29.90,3.59,NULL),(2075467830559506432,26.31,1,1,3,'2026-07-10 14:31:06','2026-07-10 14:32:11',1,1,NULL,29.90,3.59,NULL),(2075468401454612480,26.31,1,1,3,'2026-07-10 14:33:22','2026-07-10 14:34:26',1,1,NULL,29.90,3.59,NULL),(2075468500280803328,26.31,1,1,3,'2026-07-10 14:33:45','2026-07-10 14:34:51',1,1,NULL,29.90,3.59,NULL),(2075469361027481600,26.31,1,1,3,'2026-07-10 14:37:11','2026-07-10 14:38:14',1,1,NULL,29.90,3.59,NULL),(2075469419814846464,26.31,1,1,3,'2026-07-10 14:37:25','2026-07-10 14:38:27',1,1,NULL,29.90,3.59,NULL),(2075469539880992768,26.31,1,1,3,'2026-07-10 14:37:53','2026-07-10 14:39:01',1,1,NULL,29.90,3.59,NULL),(2075469686589358080,26.31,1,1,3,'2026-07-10 14:38:28','2026-07-10 14:39:41',1,1,NULL,29.90,3.59,NULL),(2075469840230907904,26.31,1,1,3,'2026-07-10 14:39:05','2026-07-10 14:40:07',1,1,NULL,29.90,3.59,NULL),(2075470002244288512,26.31,1,1,3,'2026-07-10 14:39:44','2026-07-10 14:41:01',1,1,NULL,29.90,3.59,NULL),(2075470162282151936,26.31,1,1,3,'2026-07-10 14:40:22','2026-07-10 14:41:48',1,1,NULL,29.90,3.59,NULL),(2075470372504862720,26.31,1,1,3,'2026-07-10 14:41:12','2026-07-10 14:42:46',1,1,NULL,29.90,3.59,NULL),(2075470816878788608,26.31,1,1,3,'2026-07-10 14:42:58','2026-07-10 14:44:27',1,1,NULL,29.90,3.59,NULL),(2075470908205563904,26.31,1,1,3,'2026-07-10 14:43:20','2026-07-10 14:45:12',1,1,NULL,29.90,3.59,NULL),(2075471028192018432,26.31,1,1,3,'2026-07-10 14:43:48','2026-07-10 14:45:12',1,1,NULL,29.90,3.59,NULL),(2075471083414224896,26.31,1,1,3,'2026-07-10 14:44:01','2026-07-10 14:45:26',1,1,NULL,29.90,3.59,NULL),(2075471242239934464,26.31,1,1,3,'2026-07-10 14:44:39','2026-07-10 14:46:11',1,1,NULL,29.90,3.59,NULL),(2075471492589551616,26.31,1,1,3,'2026-07-10 14:45:39','2026-07-10 14:46:43',1,1,NULL,29.90,3.59,NULL),(2075471627503534080,26.31,1,1,3,'2026-07-10 14:46:11','2026-07-10 14:47:31',1,1,NULL,29.90,3.59,NULL),(2075478462142873600,26.31,1,1,3,'2026-07-10 15:13:21','2026-07-10 15:14:27',1,1,NULL,29.90,3.59,NULL),(2075478963618054144,26.31,1,1,3,'2026-07-10 15:15:20','2026-07-10 15:16:26',1,1,NULL,29.90,3.59,NULL),(2075479035911077888,26.31,1,1,3,'2026-07-10 15:15:37','2026-07-10 15:16:42',1,1,NULL,29.90,3.59,NULL),(2075479236050681856,26.31,1,1,3,'2026-07-10 15:16:25','2026-07-10 15:17:31',1,1,NULL,29.90,3.59,NULL),(2075479677945774080,26.31,1,1,3,'2026-07-10 15:18:10','2026-07-10 15:19:16',1,1,NULL,29.90,3.59,NULL),(2075479770631503872,26.31,2,1,3,'2026-07-10 15:18:33','2026-07-10 15:19:36',1,1,NULL,29.90,3.59,NULL),(2078394687718162432,26.31,1,2,3,'2026-07-18 16:21:23','2026-07-18 16:22:26',1,1,NULL,29.90,3.59,NULL);
/*!40000 ALTER TABLE `payment_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_collect`
--

DROP TABLE IF EXISTS `post_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子收藏关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_collect`
--

LOCK TABLES `post_collect` WRITE;
/*!40000 ALTER TABLE `post_collect` DISABLE KEYS */;
INSERT INTO `post_collect` VALUES (10,2066448390446788608,3,'2026-07-01 04:24:00'),(11,2072331621482246144,2,'2026-07-01 15:21:00'),(12,2072333233068060672,2,'2026-07-01 15:21:00'),(13,2072333946049409024,2,'2026-07-01 15:21:00'),(14,2072335787806044160,2,'2026-07-01 15:21:00'),(15,2072335787806044160,1,'2026-07-16 13:09:00');
/*!40000 ALTER TABLE `post_collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_image`
--

DROP TABLE IF EXISTS `post_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `image_url` varchar(512) NOT NULL,
  `width` smallint DEFAULT NULL,
  `height` smallint DEFAULT NULL,
  `size` int DEFAULT NULL,
  `sort_order` tinyint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1-正常，2-违规',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子图片关联表（逻辑外键）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_image`
--

LOCK TABLES `post_image` WRITE;
/*!40000 ALTER TABLE `post_image` DISABLE KEYS */;
INSERT INTO `post_image` VALUES (12,2072330002199883776,'http://localhost:9000/stp-summit-files/post-media/ce0708bf-9ae0-4759-870e-31b28cbceafc',1080,1620,NULL,0,1,'2026-07-01 14:42:29'),(13,2072330002199883776,'http://localhost:9000/stp-summit-files/post-media/1ea59ba2-1415-4c8b-b785-33c499ae3b03',1080,1620,NULL,0,1,'2026-07-01 14:42:29'),(14,2072330002199883776,'http://localhost:9000/stp-summit-files/post-media/3bd673c7-b9bd-4951-97a3-6945e88ddbd6',1080,1620,NULL,0,1,'2026-07-01 14:42:29'),(15,2072333233068060672,'http://localhost:9000/stp-summit-files/post-media/c609e96b-2ee3-483e-911f-2976152fad15',1080,1440,NULL,0,1,'2026-07-01 14:55:19'),(16,2072333233068060672,'http://localhost:9000/stp-summit-files/post-media/dd9a4802-7886-451f-99e3-0e3bc3072bc2',1080,1440,NULL,0,1,'2026-07-01 14:55:19'),(17,2072335787806044160,'http://localhost:9000/stp-summit-files/post-media/5a89f263-ff30-4d2b-9112-2a1c44090613',3840,5120,NULL,0,1,'2026-07-01 15:05:28'),(18,2072335787806044160,'http://localhost:9000/stp-summit-files/post-media/88f07793-bf07-41e4-8ff0-0d0f7b5999b9',3840,5120,NULL,0,1,'2026-07-01 15:05:28'),(19,2072335787806044160,'http://localhost:9000/stp-summit-files/post-media/6cb5fb61-4b18-45d4-894e-090920c6085f',2460,3280,NULL,0,1,'2026-07-01 15:05:28'),(20,2072335787806044160,'http://localhost:9000/stp-summit-files/post-media/1aff6d85-21f6-41d7-900a-7404d5cb85e1',2464,3288,NULL,0,1,'2026-07-01 15:05:28'),(21,2077679057318850560,'http://localhost:9000/stp-summit-files/post-media/984f6383-4d5d-469b-bac2-99ad83f46848',1080,1440,NULL,0,1,'2026-07-16 08:57:43'),(22,2077679057318850560,'http://localhost:9000/stp-summit-files/post-media/8ff3d90d-2884-4f1d-b616-82e5e7c11920',1080,1440,NULL,0,1,'2026-07-16 08:57:43'),(23,2077679057318850560,'http://localhost:9000/stp-summit-files/post-media/c9defd8d-a46c-4e54-99a5-64647bdc4254',1080,1080,NULL,0,1,'2026-07-16 08:57:43'),(24,2077679136310177792,'http://localhost:9000/stp-summit-files/post-media/5a11ea4f-0bd9-43a8-b86e-90ad6d3af148',1080,1440,NULL,0,1,'2026-07-16 08:58:02'),(25,2077679136310177792,'http://localhost:9000/stp-summit-files/post-media/f0dc4afb-3d71-44c4-84ec-cac2d2c90ac3',1080,1440,NULL,0,1,'2026-07-16 08:58:02'),(26,2077679136310177792,'http://localhost:9000/stp-summit-files/post-media/f60e0be3-ea00-4ac2-926b-09b4fba0fef3',1080,1080,NULL,0,1,'2026-07-16 08:58:02'),(27,2077681603135549440,'http://localhost:9000/stp-summit-files/post-media/36c423a1-9bca-4b9c-b57e-d996400dc329',1080,1440,NULL,0,1,'2026-07-16 09:07:50'),(28,2077681603135549440,'http://localhost:9000/stp-summit-files/post-media/bcd6fa8f-8961-4309-a9f0-8ae1ec3c2b1a',1080,1440,NULL,0,1,'2026-07-16 09:07:50'),(29,2077681603135549440,'http://localhost:9000/stp-summit-files/post-media/cdec534e-762f-42b8-8613-b04d8969dac2',1440,1080,NULL,0,1,'2026-07-16 09:07:50'),(30,2077681603135549440,'http://localhost:9000/stp-summit-files/post-media/d76b16e6-8be5-448b-ba5b-08808e063759',1280,960,NULL,0,1,'2026-07-16 09:07:50'),(31,2077682217177460736,'http://localhost:9000/stp-summit-files/post-media/012cfa3d-6b9f-47b9-9f68-63fc5569150f',1080,1440,NULL,0,1,'2026-07-16 09:10:16'),(32,2077682217177460736,'http://localhost:9000/stp-summit-files/post-media/3afbbe90-03de-497c-9973-1ad1776b6a13',1080,1440,NULL,0,1,'2026-07-16 09:10:16'),(33,2077682217177460736,'http://localhost:9000/stp-summit-files/post-media/1fbac1b6-9f82-4a2b-bf11-33eec34b36bb',1440,1080,NULL,0,1,'2026-07-16 09:10:16'),(34,2077682217177460736,'http://localhost:9000/stp-summit-files/post-media/dd7508d7-d663-4358-bef8-27b0c0eb70ed',1280,960,NULL,0,1,'2026-07-16 09:10:16'),(35,2077682878208159744,'http://localhost:9000/stp-summit-files/post-media/f012eb00-cc7e-41e3-8a86-827a777525d7',1080,1440,NULL,0,1,'2026-07-16 09:12:54'),(36,2077682878208159744,'http://localhost:9000/stp-summit-files/post-media/68755db9-1fac-4830-a081-201c97164161',1080,1440,NULL,0,1,'2026-07-16 09:12:54'),(37,2077682878208159744,'http://localhost:9000/stp-summit-files/post-media/55d1a557-dc34-44f8-a2ca-ca2928e2c686',1440,1080,NULL,0,1,'2026-07-16 09:12:54'),(38,2077682878208159744,'http://localhost:9000/stp-summit-files/post-media/76d12c90-1f71-4509-9017-6473762a9972',1280,960,NULL,0,1,'2026-07-16 09:12:54');
/*!40000 ALTER TABLE `post_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_like`
--

DROP TABLE IF EXISTS `post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子点赞关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_like`
--

LOCK TABLES `post_like` WRITE;
/*!40000 ALTER TABLE `post_like` DISABLE KEYS */;
INSERT INTO `post_like` VALUES (17,2066448390446788608,3,'2026-07-01 04:24:00'),(18,2066448390446788608,1,'2026-07-01 11:27:00'),(19,2072335787806044160,2,'2026-07-01 15:21:00'),(20,2072335787806044160,1,'2026-07-10 07:48:00'),(21,2077679057318850560,1,'2026-07-16 12:30:00'),(23,2077682878208159744,1,'2026-07-19 09:51:00');
/*!40000 ALTER TABLE `post_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_rank`
--

DROP TABLE IF EXISTS `post_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_rank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '甯栧瓙ID',
  `score` decimal(10,2) NOT NULL COMMENT '鐑?害鍊',
  `rank` int NOT NULL COMMENT '鎺掑悕',
  `period_date` date NOT NULL COMMENT '鍛ㄦ湡鏃堕棿',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_period` (`post_id`,`period_date`),
  KEY `idx_period` (`period_date`)
) ENGINE=InnoDB AUTO_INCREMENT=6693 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐑?偣甯栧瓙姒滃崟';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_rank`
--

LOCK TABLES `post_rank` WRITE;
/*!40000 ALTER TABLE `post_rank` DISABLE KEYS */;
INSERT INTO `post_rank` VALUES (366,2066448390446788608,1.00,1,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(367,2072335787806044160,1.00,2,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(368,2060554677342412800,0.00,3,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(369,2060652056246710272,0.00,4,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(370,2060654998018289664,0.00,5,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(371,2060914578832932864,0.00,6,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(372,2072330002199883776,0.00,7,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(373,2072331621482246144,0.00,8,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(374,2072333233068060672,0.00,9,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(375,2072333946049409024,0.00,10,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(1194,2072335787806044160,1.00,1,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(1195,2060654998018289664,0.00,2,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(1196,2072330002199883776,0.00,3,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(1197,2072331621482246144,0.00,4,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(1198,2072333233068060672,0.00,5,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(1199,2072333946049409024,0.00,6,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(6685,2072335787806044160,1.00,1,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(6686,2077679057318850560,1.00,2,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(6687,2077682878208159744,1.00,3,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(6688,2060654998018289664,0.00,4,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(6689,2072330002199883776,0.00,5,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(6690,2072331621482246144,0.00,6,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(6691,2072333233068060672,0.00,7,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(6692,2072333946049409024,0.00,8,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00');
/*!40000 ALTER TABLE `post_rank` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_tag_rel`
--

DROP TABLE IF EXISTS `post_tag_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_tag_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`,`tag_id`),
  KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_tag_rel`
--

LOCK TABLES `post_tag_rel` WRITE;
/*!40000 ALTER TABLE `post_tag_rel` DISABLE KEYS */;
INSERT INTO `post_tag_rel` VALUES (8,2060554677342412800,1,'2026-05-30 02:51:33'),(9,2060652056246710272,5,'2026-05-30 09:18:29'),(10,2060652056246710272,6,'2026-05-30 09:18:29'),(11,2060652056246710272,1,'2026-05-30 09:18:29'),(12,2060654998018289664,9,'2026-05-30 09:30:11'),(13,2060654998018289664,10,'2026-05-30 09:30:11'),(14,2060654998018289664,11,'2026-05-30 09:30:11'),(15,2060654998018289664,12,'2026-05-30 09:30:11'),(16,2060654998018289664,13,'2026-05-30 09:30:11'),(17,2060914335533940736,1,'2026-05-31 02:40:42'),(18,2060914578832932864,1,'2026-05-31 02:41:40'),(19,2060958926496780288,1,'2026-05-31 05:37:53'),(20,2066448390446788608,14,'2026-06-15 09:11:03'),(21,2066448390446788608,15,'2026-06-15 09:11:03'),(22,2072330002199883776,16,'2026-07-01 14:42:29'),(23,2072330002199883776,17,'2026-07-01 14:42:29'),(24,2072331621482246144,18,'2026-07-01 14:48:55'),(25,2072331621482246144,19,'2026-07-01 14:48:55'),(26,2072331621482246144,20,'2026-07-01 14:48:55'),(27,2072331621482246144,21,'2026-07-01 14:48:55'),(28,2072333946049409024,22,'2026-07-01 14:58:09'),(29,2072333946049409024,23,'2026-07-01 14:58:09'),(30,2072333946049409024,24,'2026-07-01 14:58:09'),(31,2072335787806044160,24,'2026-07-01 15:05:28'),(32,2072335787806044160,25,'2026-07-01 15:05:28'),(33,2077679057318850560,26,'2026-07-16 08:57:43'),(34,2077679136310177792,26,'2026-07-16 08:58:02');
/*!40000 ALTER TABLE `post_tag_rel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `posts` (
  `id` bigint NOT NULL,
  `creator_id` bigint NOT NULL COMMENT '发布者用户ID',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `type` int NOT NULL DEFAULT '1' COMMENT '帖子类型(1 文本 2 图片 3视频 4音频 5连接 6文件 7未知)',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '文本内容',
  `media_urls` text COLLATE utf8mb4_unicode_ci COMMENT '媒体资源URL列表',
  `reply_count` int unsigned NOT NULL DEFAULT '0' COMMENT '回复数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '''状态: 1正常, 2删除, 3封禁, 4举报, 5未知, 6草稿',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_top` tinyint NOT NULL DEFAULT '0',
  `view_count` bigint NOT NULL DEFAULT '0',
  `like_count` bigint NOT NULL DEFAULT '0' COMMENT '点赞数',
  `hot_score` bigint NOT NULL DEFAULT '0',
  `visible_scope` tinyint DEFAULT '1' COMMENT '1-全局可见 2-仅自己可见 3-仅好友可见 ',
  `collect_count` bigint DEFAULT '0' COMMENT '收藏数',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_type` (`type`),
  KEY `idx_created_at` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `posts`
--

LOCK TABLES `posts` WRITE;
/*!40000 ALTER TABLE `posts` DISABLE KEYS */;
INSERT INTO `posts` VALUES (2060554677342412800,1,'王宝强',3,'一脚踩碎质疑。甄子丹的台，被他拆','http://localhost:9000/stp-summit-files/post-media/c2236fa4-5d84-43d6-be50-4b4b9364b8a8.mp4',0,2,'2026-05-30 10:51:33','2026-07-10 15:45:17',0,1,0,0,1,0),(2060652056246710272,1,'沉沦在迷雾森林 精挑细选的路线果然绝美',3,'毛毛球森林有很多条徒步条线，出发前我居然把每一条路线都咨询了一遍。向导都不太愿意把路线说清楚，可能就是怕我们自己独自去爬。\n最后弄清楚了，网上在推的大部分路线在腾冲西北方向，高黎贡山北段，区别是：\n1、腾冲猴桥镇胆扎村：3/6/9km，爬升缓，毛毛球密集（配室内餐饮+村里温泉泡脚），新手亲子友好，名气大人最多，中长线有开阔景观。\n2、腾冲界头镇中坪村：3/7/9km云拓俱乐部开发路线（配森林厨房+野温泉），9km长线可穿越山脊，可远眺山脉。3/5km光民旅行社开发路线，有瀑布景观（配荒野厨房+野温泉），平缓休闲，沿着溪流有多处浅滩和水潭，能玩水。\n3、腾冲芒棒镇窜龙村：13km，原始森林+5级瀑布+高山草甸，难度较大，茶马古道遗址（配山野厨房），但涉及生态保护区的核心区，之前被封了，五一前又重新开放了。\n以上路线，我看了所有买家秀，感觉不够森，我想要《勇敢者的游戏》里的那种丛林。\n又找了高黎贡山南段的毛毛球森林在龙陵县，也就是我们这次去的路线。\n龙陵县雪山村：9km路线（配乡野厨房+锅炉水泡脚），封面图这个景观绝美，还偶遇了大片蓝鸢尾花。爬升3km，精华段平路3km，下坡3km，我们一群小朋友无压力全程跑山。\n最终果然不虚此行。\n不同的路线，俱乐部都是到腾冲市区接送。如果自驾，可以减免费用，龙陵县这段路住潞江坝、龙陵县或芒市更近。\n#腾冲旅游  #云南徒步 \n ','http://localhost:9000/stp-summit-files/post-media/b3393315-5ea3-43c4-bd3c-44d7d9b429e0',0,2,'2026-05-30 17:18:30','2026-07-10 15:45:15',0,1,0,0,1,0),(2060654998018289664,3,'🦞 你真的会吃小龙虾吗？',0,'别再乱嗦头了！\n	\n今天带你硬核解剖一只小龙虾，\n	\n看完这篇，至少帮你省下30%的冤枉钱和医药费！💰\n	\n1️⃣ 【头部：是“精华”还是“毒囊”？】\n	\n⚠️ 真相：头部=胃+肝胰腺+鳃\n	\n那个黄色的“虾黄”，其实是肝胰腺（解毒器官），易富集重金属，建议别吃！🚫\n	\n黑色胃囊（沙包）必须剔除，全是泥沙和未消化物。\n	\n鳃部像毛刷，是过滤脏东西的，绝对不能吃！\n✅ 结论：头部除了肉，其他统统扔掉！别心疼！\n	\n2️⃣ 【虾线：是“肠”还是“筋”？】\n	\n🔍 背部黑线=肠道（屎线）\n	\n里面是未消化的食物残渣和排泄物，口感发苦，建议抽掉！\n🔍 腹部黑线=神经索（虾筋）\n	\n这是控制虾运动的神经，煮熟后发黑是正常的，可以吃，别误杀！\n✅ 结论：抽背线，留腹线！\n	\n3️⃣ 【虾身：唯一的安全区】\n	\n💪 虾尾肌肉：蛋白质高、脂肪低，最干净的部位，放心炫！\n	\n🦐 虾钳：里面也有肉，别浪费，敲开吃！\n	\n✅ 结论：一只虾90%的价值都在这里！\n	\n4️⃣ 【懂吃实验室の避坑指南】\n	\n挑虾：选青壳虾（壳薄肉嫩），避开黑腹、黑鳃的死虾。\n	\n清洗：盐水+白醋浸泡30分钟，刷净腹部。\n	\n烹饪：100℃高温煮10分钟以上，彻底杀菌！\n	\n👉 你平时吃小龙虾会嗦头吗？\n	\n评论区告诉我，下期教你3秒剥虾神技！👇\n','',0,1,'2026-05-30 17:30:11','2026-07-01 04:22:59',0,0,0,0,1,0),(2060914335533940736,1,'一些缓解头油的小tips',2,'1. 洗头一定洗两遍\n第一遍带走表面灰尘，第二遍充分按摩头皮，尤其注意发际线和耳后，彻底清洁到位，油得会慢很多。\n	\n2. 护肤时戴发带\n把刘海和发际线碎发固定好，避免水和护肤品沾到头发。哪怕一点点打湿，都容易加速变油。\n	\n3. 别总用同一款洗发水，交替着用\n部分洗发水（尤其日系）保湿剂多，容易在头皮成膜，选无硅油清爽型的更安心\n	\n4. 二硫化硒洗发水备一瓶，一周1-2次，深层控油清洁，对出油厉害的姐妹很友好\n	\n5. 少梳靠近头皮的发根\n手上和梳子上的油脂会顺着带到发丝。真要梳就重点梳发中发尾。\n	\n6. 洗完后别用毛巾一直包着\n \n\n擦到不滴水立刻吹干，热风吹到八分干，再换冷风定型，冷风吹太久反而容易油\n	\n7. 改掉经常撩头发、摸头发的小习惯\n手上的油脂和灰尘是隐形的“产油加速器”\n	\n8. 额头出油会连累刘海和发际线碎发\n随身带吸油纸，定期摁一摁额头，超级方便\n	\n9. 睡觉时把刘海和发际线区域头发掀起来\n别紧贴额头，干爽一整晚，第二天明显没那么快油\n	\n10. 油头姐妹少用散粉救急\n临时遮一下是可以，但长期用反而让头发更脏更容易腻\n	\n11. 洗头前先用梳子干梳一遍\n把头皮上的灰尘和油脂以及头发松一松，再用温水冲湿，正常洗\n	\n12. 枕套勤换、定期除螨\n尤其油头+长痘体质，这一条很关键\n	\n13. 内调：吃点复合维生素B（两周停一周）和补锌产品，配合晚上用生姜花椒泡泡脚，排湿气\n	\n14. 规律作息，避免熬夜\n	\n15. 如果头油严重到发痒、掉发多，别自己折腾了，去看皮肤科查是不是脂溢性皮炎','',0,2,'2026-05-31 10:40:42','2026-07-01 22:24:57',0,0,0,0,1,0),(2060914578832932864,1,'一些缓解头油的小tips',2,'1. 洗头一定洗两遍\n第一遍带走表面灰尘，第二遍充分按摩头皮，尤其注意发际线和耳后，彻底清洁到位，油得会慢很多。\n	\n2. 护肤时戴发带\n把刘海和发际线碎发固定好，避免水和护肤品沾到头发。哪怕一点点打湿，都容易加速变油。\n	\n3. 别总用同一款洗发水，交替着用\n部分洗发水（尤其日系）保湿剂多，容易在头皮成膜，选无硅油清爽型的更安心\n	\n4. 二硫化硒洗发水备一瓶，一周1-2次，深层控油清洁，对出油厉害的姐妹很友好\n	\n5. 少梳靠近头皮的发根\n手上和梳子上的油脂会顺着带到发丝。真要梳就重点梳发中发尾。\n	\n6. 洗完后别用毛巾一直包着\n \n\n擦到不滴水立刻吹干，热风吹到八分干，再换冷风定型，冷风吹太久反而容易油\n	\n7. 改掉经常撩头发、摸头发的小习惯\n手上的油脂和灰尘是隐形的“产油加速器”\n	\n8. 额头出油会连累刘海和发际线碎发\n随身带吸油纸，定期摁一摁额头，超级方便\n	\n9. 睡觉时把刘海和发际线区域头发掀起来\n别紧贴额头，干爽一整晚，第二天明显没那么快油\n	\n10. 油头姐妹少用散粉救急\n临时遮一下是可以，但长期用反而让头发更脏更容易腻\n	\n11. 洗头前先用梳子干梳一遍\n把头皮上的灰尘和油脂以及头发松一松，再用温水冲湿，正常洗\n	\n12. 枕套勤换、定期除螨\n尤其油头+长痘体质，这一条很关键\n	\n13. 内调：吃点复合维生素B（两周停一周）和补锌产品，配合晚上用生姜花椒泡泡脚，排湿气\n	\n14. 规律作息，避免熬夜\n	\n15. 如果头油严重到发痒、掉发多，别自己折腾了，去看皮肤科查是不是脂溢性皮炎','',0,2,'2026-05-31 10:41:40','2026-07-10 15:45:04',0,1,0,0,1,0),(2060958926496780288,1,'一些缓解头油的小tips',2,'1. 洗头一定洗两遍\n第一遍带走表面灰尘，第二遍充分按摩头皮，尤其注意发际线和耳后，彻底清洁到位，油得会慢很多。\n	\n2. 护肤时戴发带\n把刘海和发际线碎发固定好，避免水和护肤品沾到头发。哪怕一点点打湿，都容易加速变油。\n	\n3. 别总用同一款洗发水，交替着用\n部分洗发水（尤其日系）保湿剂多，容易在头皮成膜，选无硅油清爽型的更安心\n	\n4. 二硫化硒洗发水备一瓶，一周1-2次，深层控油清洁，对出油厉害的姐妹很友好\n	\n5. 少梳靠近头皮的发根\n手上和梳子上的油脂会顺着带到发丝。真要梳就重点梳发中发尾。\n	\n6. 洗完后别用毛巾一直包着\n \n\n擦到不滴水立刻吹干，热风吹到八分干，再换冷风定型，冷风吹太久反而容易油\n	\n7. 改掉经常撩头发、摸头发的小习惯\n手上的油脂和灰尘是隐形的“产油加速器”\n	\n8. 额头出油会连累刘海和发际线碎发\n随身带吸油纸，定期摁一摁额头，超级方便\n	\n9. 睡觉时把刘海和发际线区域头发掀起来\n别紧贴额头，干爽一整晚，第二天明显没那么快油\n	\n10. 油头姐妹少用散粉救急\n临时遮一下是可以，但长期用反而让头发更脏更容易腻\n	\n11. 洗头前先用梳子干梳一遍\n把头皮上的灰尘和油脂以及头发松一松，再用温水冲湿，正常洗\n	\n12. 枕套勤换、定期除螨\n尤其油头+长痘体质，这一条很关键\n	\n13. 内调：吃点复合维生素B（两周停一周）和补锌产品，配合晚上用生姜花椒泡泡脚，排湿气\n	\n14. 规律作息，避免熬夜\n	\n15. 如果头油严重到发痒、掉发多，别自己折腾了，去看皮肤科查是不是脂溢性皮炎','',0,2,'2026-05-31 13:37:53','2026-07-01 22:25:33',0,1,0,0,1,0),(2066448390446788608,1,'英语六级听力第一套花卷答案',2,'根据网上整理，应该没什么问题，想知道这套赋分情况怎么样啊','',0,2,'2026-06-15 17:11:04','2026-07-10 18:05:37',0,1,2,1,1,1),(2072330002199883776,3,'好粉嫩🎀',2,'<a>#pink#</a> <a>#今日穿粉色#</a>','',0,1,'2026-07-01 22:42:29','2026-07-01 14:45:00',0,1,0,0,1,0),(2072331621482246144,3,'所有人，上岸！',3,'<a>#李荣浩#</a> <a>#薛之谦#</a> <a>#陶喆#</a> <a>#我的毕业#</a>','http://localhost:9000/stp-summit-files/post-media/5dfdf3a6-5bd5-411c-8612-32d2314f6e0f.mp4',0,1,'2026-07-01 22:48:55','2026-07-01 15:21:00',0,1,0,0,1,1),(2072333233068060672,3,'With me 🤍',2,'喜欢白色','',0,1,'2026-07-01 22:55:20','2026-07-16 08:03:00',0,2,0,0,1,1),(2072333946049409024,3,'封神蕾丝吊带黑丝！轻松穿出高挑御姐感',3,'谁懂这条蕾丝吊带黑丝的氛围感！ 花边蕾丝腰头自带精致花纹，油亮丝滑面料巨贴肤，分段吊带设计悄悄拉长腿部线条，显瘦一绝。 搭配挂脖短裙直接变身温柔御姐，约会、居家穿搭都合适，质感细腻不容易勾丝，精致女生的氛围感小心机单品，穿上气质直接拉满✨ <a>#丝袜推荐#</a> <a>#黑丝穿搭#</a> <a>#纯欲风穿搭#</a>','http://localhost:9000/stp-summit-files/post-media/5d63fcad-1139-4041-8791-70e94916039a',0,1,'2026-07-01 22:58:09','2026-07-16 08:03:00',1,3,0,0,1,1),(2072335787806044160,2,'这套好显身材！',2,'<a>#吊带穿搭#</a> <a>#纯欲风穿搭#</a>','',1,1,'2026-07-01 23:05:29','2026-07-17 20:38:33',1,5,2,1,1,2),(2077679057318850560,1,'💤',2,'💤 <a>#包包的日常分享图鉴#</a>','',0,1,'2026-07-16 16:57:43','2026-07-16 12:30:00',0,0,1,1,1,0),(2077679136310177792,1,'💤',2,'💤 <a>#包包的日常分享图鉴#</a>','',0,2,'2026-07-16 16:58:02','2026-07-16 16:58:15',0,0,0,0,1,0),(2077681603135549440,1,'右滑有世界杯小狗૮・ᴥ - ა',2,'右滑有世界杯小狗૮・ᴥ - ა','',0,2,'2026-07-16 17:07:50','2026-07-16 17:09:24',0,0,0,0,1,0),(2077682217177460736,1,'右滑有世界杯小狗૮・ᴥ - ა',2,'右滑有世界杯小狗૮・ᴥ - ა','',0,2,'2026-07-16 17:10:17','2026-07-16 17:12:36',0,0,0,0,1,0),(2077682878208159744,1,'右滑有世界杯小狗૮・ᴥ - ა',2,'右滑有世界杯小狗૮・ᴥ - ა','',5,1,'2026-07-16 17:12:54','2026-07-19 09:51:00',0,6,1,1,1,0);
/*!40000 ALTER TABLE `posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `private_message`
--

DROP TABLE IF EXISTS `private_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `private_message` (
  `id` bigint NOT NULL COMMENT '消息id',
  `user_id` bigint NOT NULL COMMENT '发送者id',
  `receiver_id` bigint NOT NULL COMMENT '接收者id',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `content` varchar(1000) DEFAULT NULL COMMENT '消息内容',
  `image` varchar(255) DEFAULT NULL COMMENT '图片',
  `audio` varchar(255) DEFAULT NULL COMMENT '音频',
  `video` varchar(255) DEFAULT NULL COMMENT '视频',
  `type` int NOT NULL COMMENT '消息类型',
  `status` int NOT NULL COMMENT '消息状态',
  `session_id` bigint NOT NULL COMMENT '会话id',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_receiver` (`user_id`,`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='私信消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `private_message`
--

LOCK TABLES `private_message` WRITE;
/*!40000 ALTER TABLE `private_message` DISABLE KEYS */;
INSERT INTO `private_message` VALUES (328846202030264320,3,1,'2026-06-26 18:37:24','你好','','','',2,2,1055),(328847484635844608,3,1,'2026-06-26 18:42:30','沙雕','','','',2,2,1055),(328847628198481920,2,3,'2026-06-26 18:43:04','不是哥们你谁啊','','','',2,2,1056),(328854175586717696,3,2,'2026-06-26 19:09:05','我跟你很熟吗?沙雕','','','',2,2,1056),(328854410740371456,2,3,'2026-06-26 19:10:01','你叫你目','','','',2,2,1056),(328854422840938496,2,3,'2026-06-26 19:10:04','沙雕','','','',2,2,1056),(328854449223110656,2,3,'2026-06-26 19:10:11','蠢猪','','','',2,2,1056),(328854480466481152,3,2,'2026-06-26 19:10:18','骂谁?','','','',2,2,1056),(328854498929807360,3,2,'2026-06-26 19:10:22','你再骂一句试试','','','',2,3,1056),(328854536309444608,2,3,'2026-06-26 19:10:31','我就骂你怎么了','','','',2,2,1056),(328855906739556352,2,3,'2026-06-26 19:15:58','111','','','',2,2,1056),(328856182271774720,2,3,'2026-06-26 19:17:04','111','','','',2,2,1056),(328856391450103808,2,3,'2026-06-26 19:17:54','1212','','','',2,2,1056),(328859023476527104,2,3,'2026-06-26 19:28:21','你好','','','',2,2,1056),(328859994650841088,2,3,'2026-06-26 19:32:13','沙雕','','','',2,2,1056),(328860726334590976,2,3,'2026-06-26 19:35:07','你好','','','',2,2,1056),(328862120005341184,2,3,'2026-06-26 19:40:39','111','','','',2,2,1056),(328862171649806336,2,3,'2026-06-26 19:40:52','11','','','',2,2,1056),(330560835682308096,1,3,'2026-07-01 12:10:45','你好','','','',2,2,1055),(330560898353598464,3,1,'2026-07-01 12:11:00','111','','','',2,2,1055),(330561427427299328,1,3,'2026-07-01 12:13:06','11','','','',2,2,1055),(330561447308300288,3,1,'2026-07-01 12:13:11','傻逼','','','',2,2,1055),(336773113486249984,2,3,'2026-07-18 15:36:07','111','','','',2,1,1056),(336773162396028928,2,1,'2026-07-18 15:36:19','1212','','','',2,3,1024),(336781130525052928,2,1,'2026-07-18 16:07:59','1','','','',2,2,1024),(336782084850847744,2,1,'2026-07-18 16:11:46','[荷兰猪][荷兰猪]','','','',2,2,1024);
/*!40000 ALTER TABLE `private_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session`
--

DROP TABLE IF EXISTS `session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `session` (
  `id` bigint NOT NULL COMMENT '对话id，由 hash(max(user_id, target_id), min(user_id, target_id)) 生成',
  `type` int DEFAULT '1' COMMENT '会话类型',
  `last_message_id` bigint DEFAULT NULL COMMENT '最后一条消息id',
  `last_message_content` varchar(500) DEFAULT NULL COMMENT '最后一条消息内容',
  `last_sender_id` bigint DEFAULT NULL COMMENT '最后发送者id',
  `last_time` datetime DEFAULT NULL COMMENT '最后发送时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session`
--

LOCK TABLES `session` WRITE;
/*!40000 ALTER TABLE `session` DISABLE KEYS */;
INSERT INTO `session` VALUES (1024,1,336782084850847744,'[荷兰猪][荷兰猪]',2,'2026-07-18 16:11:47','2026-07-18 15:36:19','2026-07-18 16:11:47'),(1055,1,330561447308300288,'傻逼',3,'2026-07-01 12:13:11','2026-06-26 18:37:24','2026-07-01 12:13:11'),(1056,1,336773113486249984,'111',2,'2026-07-18 15:36:08','2026-06-26 18:43:04','2026-07-18 15:36:08');
/*!40000 ALTER TABLE `session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_message`
--

DROP TABLE IF EXISTS `system_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_message` (
  `id` bigint NOT NULL COMMENT '主键',
  `from_user_id` bigint NOT NULL COMMENT '系统消息发送者',
  `content` varchar(2000) DEFAULT NULL COMMENT '内容(html)',
  `status` int DEFAULT '1' COMMENT '状态',
  `public_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `associate_user` bigint DEFAULT NULL COMMENT '系统消息关联的用户ID',
  `type` int DEFAULT '1' COMMENT '消息类型：1广播，2单独用户',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_message`
--

LOCK TABLES `system_message` WRITE;
/*!40000 ALTER TABLE `system_message` DISABLE KEYS */;
INSERT INTO `system_message` VALUES (1,4,'系统维护公告：本系统将于今晚24点进行例行停机升级，预计时长1小时，给您带来不便敬请谅解。',1,'2026-06-24 08:00:00','2026-06-24 14:29:27','2026-06-24 14:29:27',NULL,1),(2,4,'新功能上线：本平台全新支持了 WebSocket 实时一对一聊天及多媒体消息收发，赶快去和你的小伙伴们体验吧！',1,'2026-06-24 10:00:00','2026-06-24 14:29:27','2026-06-24 14:29:27',NULL,1),(2072174256707469312,3,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"http://localhost:9000/stp-summit-files/avatar/c0c3b93a-f6c0-40d2-a3e5-911ba194dfc1\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">The follower of summit</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">点赞了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-01 12:23:36</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        英语六级听力第一套花卷答案    </div></div>',1,'2026-07-01 12:23:37','2026-07-01 12:23:37','2026-07-01 12:23:37',1,2),(2072174313917775872,3,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"http://localhost:9000/stp-summit-files/avatar/c0c3b93a-f6c0-40d2-a3e5-911ba194dfc1\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">The follower of summit</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">收藏了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-01 12:23:50</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        英语六级听力第一套花卷答案    </div></div>',1,'2026-07-01 12:23:50','2026-07-01 12:23:50','2026-07-01 12:23:50',1,2),(2072339077117669376,2,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"http://localhost:9000/stp-summit-files/avatar/847a0e5e-9bd1-4bf5-a035-32a18e3b117c\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">硬伤</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">收藏了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-01 23:18:32</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        封神蕾丝吊带黑丝！轻松穿出高挑...    </div></div>',1,'2026-07-01 23:18:33','2026-07-01 23:18:33','2026-07-01 23:18:33',3,2),(2072339082939363328,2,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"http://localhost:9000/stp-summit-files/avatar/847a0e5e-9bd1-4bf5-a035-32a18e3b117c\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">硬伤</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">收藏了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-01 23:18:34</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        With me 🤍    </div></div>',1,'2026-07-01 23:18:34','2026-07-01 23:18:34','2026-07-01 23:18:34',3,2),(2072339091709652992,2,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"http://localhost:9000/stp-summit-files/avatar/847a0e5e-9bd1-4bf5-a035-32a18e3b117c\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">硬伤</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">收藏了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-01 23:18:36</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        所有人，上岸！    </div></div>',1,'2026-07-01 23:18:36','2026-07-01 23:18:36','2026-07-01 23:18:36',3,2),(2075487035161972736,1,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"https://uploadfiles.nowcoder.com/images/20260413/480237176_1776080790933/FECD76F09C4EFFA7102ECDBC1795FB3B?x-oss-process=image%2Fresize%2Cw_72%2Ch_72%2Cm_mfit\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">summit</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">点赞了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-10 15:47:24</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        这套好显身材！    </div></div>',1,'2026-07-10 15:47:25','2026-07-10 15:47:25','2026-07-10 15:47:25',2,2),(2077741620165603328,1,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"https://uploadfiles.nowcoder.com/images/20260413/480237176_1776080790933/FECD76F09C4EFFA7102ECDBC1795FB3B?x-oss-process=image%2Fresize%2Cw_72%2Ch_72%2Cm_mfit\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">summit</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">收藏了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-16 21:06:19</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        这套好显身材！    </div></div>',1,'2026-07-16 21:06:19','2026-07-16 21:06:19','2026-07-16 21:06:19',2,2),(2078734763723837440,2,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"http://localhost:9000/stp-summit-files/avatar/847a0e5e-9bd1-4bf5-a035-32a18e3b117c\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">硬伤</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">评论了你的帖子了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-19 14:52:43</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        右滑有世界杯小狗૮・ᴥ - ა    </div></div>',1,'2026-07-19 14:52:43','2026-07-19 14:52:43','2026-07-19 14:52:43',1,2),(2078735773829574656,2,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"http://localhost:9000/stp-summit-files/avatar/847a0e5e-9bd1-4bf5-a035-32a18e3b117c\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">硬伤</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">评论了你的帖子: \"[仓鼠][仓鼠]\" </div><div style=\"display:none;\">了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-19 14:56:44</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        右滑有世界杯小狗૮・ᴥ - ა    </div></div>',1,'2026-07-19 14:56:44','2026-07-19 14:56:44','2026-07-19 14:56:44',1,2),(2078778274565210112,1,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"https://uploadfiles.nowcoder.com/images/20260413/480237176_1776080790933/FECD76F09C4EFFA7102ECDBC1795FB3B?x-oss-process=image%2Fresize%2Cw_72%2Ch_72%2Cm_mfit\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">summit</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">回复了你的评论: \"[仓鼠][仓鼠]\" </div><div style=\"display:none;\">了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-19 17:45:36</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        帖子: 右滑有世界杯小狗૮・ᴥ - ა    </div></div>',1,'2026-07-19 17:45:37','2026-07-19 17:45:37','2026-07-19 17:45:37',2,2),(2078779223803314176,1,'<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 12px;\">    <div style=\"display: flex; align-items: center; gap: 12px;\">        <img src=\"https://uploadfiles.nowcoder.com/images/20260413/480237176_1776080790933/FECD76F09C4EFFA7102ECDBC1795FB3B?x-oss-process=image%2Fresize%2Cw_72%2Ch_72%2Cm_mfit\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1.5px solid #3b82f6;\" />        <div>            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">summit</div>            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">回复了你的评论: \"[柯基][柯基][柯基]\" </div><div style=\"display:none;\">了你的帖子</div>            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">2026-07-19 17:49:23</div>        </div>    </div>    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">        帖子: 右滑有世界杯小狗૮・ᴥ - ა    </div></div>',1,'2026-07-19 17:49:23','2026-07-19 17:49:23','2026-07-19 17:49:23',2,2);
/*!40000 ALTER TABLE `system_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_message_image`
--

DROP TABLE IF EXISTS `system_message_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_message_image` (
  `id` bigint NOT NULL COMMENT 'id',
  `message_id` bigint NOT NULL COMMENT '消息id',
  `image` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `status` varchar(50) DEFAULT NULL COMMENT '状态',
  `create_time` varchar(100) DEFAULT NULL COMMENT '创建时间',
  `update_time` varchar(100) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统消息图片关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_message_image`
--

LOCK TABLES `system_message_image` WRITE;
/*!40000 ALTER TABLE `system_message_image` DISABLE KEYS */;
INSERT INTO `system_message_image` VALUES (1,2,'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe','1','2026-06-24 10:00:00','2026-06-24 10:00:00');
/*!40000 ALTER TABLE `system_message_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名',
  `sort` int DEFAULT '0' COMMENT '权重排序',
  `use_count` int DEFAULT '0' COMMENT '使用次数',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0禁用, 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `uuid` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`tag_name`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
INSERT INTO `tag` VALUES (1,'生活',0,6,1,'2026-05-26 06:23:19','0'),(2,'美妆',0,0,1,'2026-05-26 06:23:29','0'),(3,'命苦',0,1,1,'2026-05-26 06:23:38','0'),(4,'你好',0,0,0,'2026-05-30 17:06:03','0'),(5,'腾冲旅游',0,1,0,'2026-05-30 17:15:22','0'),(6,'云南徒步',0,1,0,'2026-05-30 17:15:47','0'),(7,'毛毛球森林\n',0,0,0,'2026-05-30 17:16:00','0'),(9,'小龙虾解剖',0,1,0,'2026-05-30 17:28:56','0'),(10,'小龙虾避坑',0,1,0,'2026-05-30 17:29:08','0'),(11,'食品安全',0,1,0,'2026-05-30 17:29:17','0'),(12,'生活技巧',0,1,0,'2026-05-30 17:29:23','0'),(13,'长知识',0,1,0,'2026-05-30 17:29:29','0'),(14,'听力',0,1,0,'2026-06-15 17:10:42','0'),(15,'四六级',0,1,0,'2026-06-15 17:10:55','0'),(16,'pink',0,1,0,'2026-07-01 22:37:39','8cc528dd604349f18fb51cab5433b9ac'),(17,'今日穿粉色',0,1,0,'2026-07-01 22:38:11','f4bd022d01b349308df4e5d7ce218b2b'),(18,'李荣浩',0,1,0,'2026-07-01 22:47:19','d98905a1e02945a4ab1935be6dc24962'),(19,'薛之谦',0,1,0,'2026-07-01 22:47:23','dad4fa783e98429d8804e55cf37fdd5f'),(20,'陶喆',0,1,0,'2026-07-01 22:47:30','61bf5b61714a404e9104d39dbe2f472c'),(21,'我的毕业',0,1,0,'2026-07-01 22:47:42','653867a18ded48a2945584419a35a4ba'),(22,'丝袜推荐',0,1,0,'2026-07-01 22:57:34','e491ce2e437d4d75aeda205463ed92ff'),(23,'黑丝穿搭',0,1,0,'2026-07-01 22:57:41','e2562de5539e4ad193e62dde897a401a'),(24,'纯欲风穿搭',0,2,0,'2026-07-01 22:58:01','d74a078d729e4734926604c4faeffe82'),(25,'吊带穿搭',0,1,0,'2026-07-01 23:05:06','6b54a8aa662f4c7d92ecab2742f57c08'),(26,'包包的日常分享图鉴',0,1,0,'2026-07-16 16:52:14','68f59d5eba944f6484ed9971e1d9f574');
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic_rank`
--

DROP TABLE IF EXISTS `topic_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic_rank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tag_id` bigint NOT NULL COMMENT '璇濋?鏍囩?ID',
  `score` decimal(10,2) NOT NULL COMMENT '鐑?害/浣跨敤娆℃暟',
  `rank` int NOT NULL COMMENT '鎺掑悕',
  `period_date` date NOT NULL COMMENT '鍛ㄦ湡鏃堕棿',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tag_period` (`tag_id`,`period_date`),
  KEY `idx_period` (`period_date`)
) ENGINE=InnoDB AUTO_INCREMENT=2635 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璇濋?姒滃崟';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic_rank`
--

LOCK TABLES `topic_rank` WRITE;
/*!40000 ALTER TABLE `topic_rank` DISABLE KEYS */;
INSERT INTO `topic_rank` VALUES (151,1,6.00,1,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(152,3,1.00,2,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(153,2,0.00,3,'2026-06-29','2026-07-01 23:30:00','2026-07-01 23:30:00'),(505,1,6.00,1,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(506,3,1.00,2,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(507,2,0.00,3,'2026-07-06','2026-07-10 21:48:00','2026-07-10 21:48:00'),(2632,1,6.00,1,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(2633,3,1.00,2,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00'),(2634,2,0.00,3,'2026-07-13','2026-07-19 20:36:00','2026-07-19 20:36:00');
/*!40000 ALTER TABLE `topic_rank` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '用户ID',
  `uname` varchar(100) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '加密密码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status_code` int DEFAULT '1' COMMENT '用户状态: 1激活, 0封禁',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `avatar` varchar(200) NOT NULL DEFAULT 'https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c' COMMENT '头像',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `ip` varchar(50) DEFAULT NULL COMMENT 'ip地址 示例 湖南长沙',
  `introduction` varchar(300) DEFAULT NULL COMMENT '简介',
  `nick` varchar(100) NOT NULL DEFAULT (`uname`) COMMENT '昵称',
  `gender` tinyint NOT NULL DEFAULT '1' COMMENT '性别 1男 0女',
  `age` int DEFAULT NULL,
  `admin` int DEFAULT '0',
  `bg_image` varchar(500) DEFAULT NULL COMMENT '用户主页背景图',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uname` (`uname`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'U_18573757527','$2a$10$bWBIssd76Bmk4FER8q1bmO5VkcM4tm5n5VEbW8l0xFNkQAPsK4eD2','18573757527',1,'2026-05-19 05:00:52','2026-07-17 02:36:55','https://uploadfiles.nowcoder.com/images/20260413/480237176_1776080790933/FECD76F09C4EFFA7102ECDBC1795FB3B?x-oss-process=image%2Fresize%2Cw_72%2Ch_72%2Cm_mfit','2152938389@qq.com','未知','大家好','summit',1,12,0,NULL),(2,'U_17658585858','$2a$10$mVKdPUD63W3l3i9nQ0Kgmee1CdAPvwA70FNZRuXVs2IMI/yvbrhge','17658585858',1,'2026-05-19 05:37:34','2026-06-25 10:19:40','http://localhost:9000/stp-summit-files/avatar/847a0e5e-9bd1-4bf5-a035-32a18e3b117c',NULL,'未知','打架','硬伤',1,0,0,'http://localhost:9000/stp-summit-files/bgImage/e7b712e6-a86c-4401-ba38-d41aa77fdd78'),(3,'U_17375755757','$2a$10$XxjrWNifVtFtRwwRN93lFeYuhjRVjV6K4uHUEJP0sasQ1ndzXS18C','17375755757',1,'2026-05-28 13:30:43','2026-05-29 05:52:43','http://localhost:9000/stp-summit-files/avatar/c0c3b93a-f6c0-40d2-a3e5-911ba194dfc1',NULL,'未知','ikun','The follower of summit',0,18,0,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_coupon`
--

DROP TABLE IF EXISTS `user_coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '持有的用户ID',
  `coupon_id` bigint NOT NULL COMMENT '关联的优惠券模板ID',
  `status` int NOT NULL DEFAULT '1' COMMENT '使用状态: 0-已使用, 1-未使用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `used_time` timestamp NULL DEFAULT NULL COMMENT '核销使用时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `order_id` bigint DEFAULT NULL COMMENT '订单id',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=216 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券实例映射表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_coupon`
--

LOCK TABLES `user_coupon` WRITE;
/*!40000 ALTER TABLE `user_coupon` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_follow`
--

DROP TABLE IF EXISTS `user_follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follower_id` bigint NOT NULL COMMENT '关注者用户ID',
  `followee_id` bigint NOT NULL COMMENT '被关注者用户ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-正常关注，2-已取消，3-互相关注（可选）',
  `source` varchar(16) DEFAULT NULL COMMENT '关注来源（例如：推荐、搜索、扫码）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_followee` (`follower_id`,`followee_id`),
  KEY `idx_followee_id` (`followee_id`),
  KEY `idx_follower_status` (`follower_id`,`status`),
  KEY `idx_followee_status` (`followee_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_follow`
--

LOCK TABLES `user_follow` WRITE;
/*!40000 ALTER TABLE `user_follow` DISABLE KEYS */;
INSERT INTO `user_follow` VALUES (1,1,3,1,'profile','2026-05-28 15:19:00','2026-05-28 15:19:00'),(2,1,2,1,'fans-dialog','2026-06-05 09:54:17','2026-06-05 09:54:17'),(3,2,1,1,'fans-dialog','2026-06-05 13:31:54','2026-06-05 13:31:54'),(4,3,1,2,'profile','2026-06-22 03:42:42','2026-06-22 03:42:42');
/*!40000 ALTER TABLE `user_follow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_member`
--

DROP TABLE IF EXISTS `user_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_member` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_recharge` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计充值金额',
  `vip_level` int NOT NULL DEFAULT '0' COMMENT '当前VIP等级（0=非VIP）',
  `level_upgrade_time` datetime DEFAULT NULL COMMENT '最近一次升级时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `package_type_id` bigint NOT NULL COMMENT '会员类型id',
  `expire_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
  `daily_rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '当前会员日折算单价',
  PRIMARY KEY (`user_id`),
  KEY `idx_level` (`vip_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户VIP汇总表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_member`
--

LOCK TABLES `user_member` WRITE;
/*!40000 ALTER TABLE `user_member` DISABLE KEYS */;
INSERT INTO `user_member` VALUES (1,30.44,1,'2026-06-16 18:46:50','2026-06-16 18:46:50',2,'2026-09-04 09:58:40',0.0004);
/*!40000 ALTER TABLE `user_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_session`
--

DROP TABLE IF EXISTS `user_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint NOT NULL COMMENT '当前用户id',
  `session_id` bigint NOT NULL COMMENT '对话id，关联 session.id',
  `is_top` int DEFAULT '0' COMMENT '是否置顶（仅对该用户）',
  `is_mute` int DEFAULT '0' COMMENT '是否免打扰（仅对该用户）',
  `draft` varchar(500) DEFAULT NULL COMMENT '草稿（仅该用户可见）',
  `unread_count` int DEFAULT '0' COMMENT '该用户在此对话的未读消息数',
  `is_hidden` int DEFAULT '0' COMMENT '是否隐藏（仅对该用户）',
  `target_id` bigint NOT NULL COMMENT '会话对象id',
  `target_nick_name` varchar(100) DEFAULT NULL COMMENT '对方昵称（冗余，方便列表展示）',
  `target_avatar` varchar(255) DEFAULT NULL COMMENT '对方头像（冗余）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_session` (`user_id`,`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户会话设置表（每人一份）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_session`
--

LOCK TABLES `user_session` WRITE;
/*!40000 ALTER TABLE `user_session` DISABLE KEYS */;
INSERT INTO `user_session` VALUES (1,3,1055,0,0,'',0,0,1,'summit','https://uploadfiles.nowcoder.com/images/20260413/480237176_1776080790933/FECD76F09C4EFFA7102ECDBC1795FB3B?x-oss-process=image%2Fresize%2Cw_72%2Ch_72%2Cm_mfit','2026-06-26 18:37:24','2026-07-01 12:14:52'),(2,1,1055,0,0,NULL,0,0,3,'The follower of summit','http://localhost:9000/stp-summit-files/avatar/c0c3b93a-f6c0-40d2-a3e5-911ba194dfc1','2026-06-26 18:37:24','2026-07-18 17:30:03'),(3,2,1056,0,0,NULL,0,0,3,'The follower of summit','http://localhost:9000/stp-summit-files/avatar/c0c3b93a-f6c0-40d2-a3e5-911ba194dfc1','2026-06-26 18:43:04','2026-07-18 16:11:52'),(4,3,1056,0,0,NULL,0,0,2,'硬伤','http://localhost:9000/stp-summit-files/avatar/847a0e5e-9bd1-4bf5-a035-32a18e3b117c','2026-06-26 18:43:04','2026-07-18 15:36:08'),(5,2,1024,0,0,NULL,0,0,1,'summit','https://uploadfiles.nowcoder.com/images/20260413/480237176_1776080790933/FECD76F09C4EFFA7102ECDBC1795FB3B?x-oss-process=image%2Fresize%2Cw_72%2Ch_72%2Cm_mfit','2026-07-18 15:36:19','2026-07-18 16:21:38'),(6,1,1024,0,0,NULL,0,0,2,'硬伤','http://localhost:9000/stp-summit-files/avatar/847a0e5e-9bd1-4bf5-a035-32a18e3b117c','2026-07-18 15:36:19','2026-07-19 14:51:53');
/*!40000 ALTER TABLE `user_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_setting`
--

DROP TABLE IF EXISTS `user_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_setting` (
  `id` bigint NOT NULL COMMENT 'primary key',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `show_delPost` tinyint NOT NULL DEFAULT '1' COMMENT '是否显示删除的帖子',
  `Customization_recommend` tinyint NOT NULL DEFAULT '1' COMMENT '是否开启个性化推荐',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_setting`
--

LOCK TABLES `user_setting` WRITE;
/*!40000 ALTER TABLE `user_setting` DISABLE KEYS */;
INSERT INTO `user_setting` VALUES (2077699307716390912,1,0,1,'2026-07-16 10:18:11','2026-07-16 10:18:11'),(2077944550336425984,2,0,1,'2026-07-17 02:32:41','2026-07-17 02:32:41');
/*!40000 ALTER TABLE `user_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_sign_log`
--

DROP TABLE IF EXISTS `user_sign_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_sign_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `sign_date` date NOT NULL COMMENT '绛惧埌鏃ユ湡锛堟牸寮忥細2026-07-17锛',
  `sign_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绛惧埌鍏蜂綋鏃堕棿鎴',
  `sign_source` tinyint DEFAULT '1' COMMENT '绛惧埌鏉ユ簮锛?-APP 2-H5 3-灏忕▼搴',
  `reward_points` int DEFAULT '0' COMMENT '鏈??绛惧埌鑾峰緱鐨勫熀纭?Н鍒',
  `continuous_days_snapshot` int DEFAULT '0' COMMENT '绛惧埌鏃剁殑杩炵画澶╂暟蹇?収锛堢敤浜庡巻鍙插?璐︼級',
  `extra` json DEFAULT NULL COMMENT '鎵╁睍瀛楁?锛堝瓨鏀捐ˉ绛惧崱ID銆佹椿鍔↖D绛夛級',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`sign_date`) COMMENT '鍞?竴绱㈠紩锛氱‘淇濅竴澶╁彧鑳界?涓??锛屽ぉ鐒堕槻閲',
  KEY `idx_sign_date` (`sign_date`) COMMENT '鏅??绱㈠紩锛氱敤浜庤繍钀ョ粺璁℃煇澶╂湁澶氬皯浜虹?鍒'
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛绛惧埌娴佹按琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_sign_log`
--

LOCK TABLES `user_sign_log` WRITE;
/*!40000 ALTER TABLE `user_sign_log` DISABLE KEYS */;
INSERT INTO `user_sign_log` VALUES (7,1,'2026-07-17','2026-07-17 12:22:12',1,0,0,NULL),(8,2,'2026-07-17','2026-07-17 12:38:08',1,0,0,NULL),(9,1,'2026-07-18','2026-07-18 07:20:12',1,0,0,NULL),(11,2,'2026-07-18','2026-07-18 07:33:16',1,0,2,NULL),(12,1,'2026-07-19','2026-07-19 02:56:21',1,0,3,NULL);
/*!40000 ALTER TABLE `user_sign_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_sign_stats`
--

DROP TABLE IF EXISTS `user_sign_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_sign_stats` (
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `total_days` int NOT NULL DEFAULT '0' COMMENT '鍘嗗彶绱??绛惧埌鎬诲ぉ鏁',
  `current_continuous_days` int NOT NULL DEFAULT '0' COMMENT '褰撳墠杩炵画绛惧埌澶╂暟锛堟埅鑷充粖澶╋級',
  `max_continuous_days` int NOT NULL DEFAULT '0' COMMENT '鍘嗗彶鏈?珮杩炵画绛惧埌澶╂暟锛堝媼绔犵敤锛',
  `last_sign_date` date DEFAULT NULL COMMENT '鏈?悗涓??绛惧埌鏃ユ湡锛堢敤浜庡揩閫熷垽鏂?槰澶╂槸鍚︾?鍒帮級',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛绛惧埌缁熻?琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_sign_stats`
--

LOCK TABLES `user_sign_stats` WRITE;
/*!40000 ALTER TABLE `user_sign_stats` DISABLE KEYS */;
INSERT INTO `user_sign_stats` VALUES (1,3,3,3,'2026-07-19','2026-07-19 10:56:22'),(2,2,2,2,'2026-07-18','2026-07-18 15:33:16');
/*!40000 ALTER TABLE `user_sign_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_stat`
--

DROP TABLE IF EXISTS `user_stat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_stat` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `fans` bigint NOT NULL DEFAULT '0' COMMENT '粉丝数',
  `topic` bigint NOT NULL DEFAULT '0' COMMENT '话题/帖子数',
  `liked` bigint NOT NULL DEFAULT '0' COMMENT '获赞/喜欢的作品数',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_stat`
--

LOCK TABLES `user_stat` WRITE;
/*!40000 ALTER TABLE `user_stat` DISABLE KEYS */;
INSERT INTO `user_stat` VALUES (1,10532,1,11),(2,1,0,2),(3,1,0,1);
/*!40000 ALTER TABLE `user_stat` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-21 13:22:14
