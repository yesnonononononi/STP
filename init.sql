-- MySQL dump 10.13  Distrib 8.4.6, for Win64 (x86_64)
--
-- Host: localhost    Database: stp_cloud
-- ------------------------------------------------------
-- Server version	8.0.30

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

--
-- Table structure for table `admin_user`
--

DROP TABLE IF EXISTS `admin_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `username` varchar(30) NOT NULL COMMENT '用户名',
  `root_pw` varchar(30) NOT NULL COMMENT '管理员密码',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
  `order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_user`
--

LOCK TABLES `admin_user` WRITE;
/*!40000 ALTER TABLE `admin_user` DISABLE KEYS */;
INSERT INTO `admin_user` VALUES (1,2084276653212233728,'U_18573757527','root',1,1,'2026-08-14 11:33:27','2026-08-15 13:01:53');
/*!40000 ALTER TABLE `admin_user` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论图片关联表（逻辑外键）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_image`
--

LOCK TABLES `comment_image` WRITE;
/*!40000 ALTER TABLE `comment_image` DISABLE KEYS */;
INSERT INTO `comment_image` VALUES (9,2087548918358511616,'http://localhost:9000/stp-summit-files/comment-media/b626c281-7256-4456-a96c-67c354ba7453',282,282,4470390,0,1,'2026-08-12 22:37:02','eca99807216447bbbd03df4ca419e7d9.gif');
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
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论',
  `root_id` bigint DEFAULT NULL COMMENT '根评论ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `type` int NOT NULL DEFAULT '1' COMMENT '评论类型: 1文字, 2图片, 3视频, 4音频',
  `is_top` int NOT NULL DEFAULT '0' COMMENT '是否置顶: 0否, 1是',
  `status` int NOT NULL DEFAULT '1' COMMENT '评论状态: 0已删除, 1正常',
  `report_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '举报原因',
  `extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '额外扩展信息 (JSON 格式，包含媒体类型、图片元数据、媒体URL等)',
  `reply_count` int NOT NULL DEFAULT '0' COMMENT '回复数/子评论数',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `ip_location` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP归属地',
  `client_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端类型: ios, android, web等',
  `hot_score` bigint NOT NULL DEFAULT '0',
  `like_count` bigint NOT NULL DEFAULT '0' COMMENT '点赞数',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_root_id` (`root_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_updatetime_id` (`status`,`update_time`,`id`),
  KEY `idx_status_parent_root_updatetime` (`status`,`parent_id`,`root_id`,`update_time`,`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2080923482518106125 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表（支持回复）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--

LOCK TABLES `comments` WRITE;
/*!40000 ALTER TABLE `comments` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon`
--

LOCK TABLES `coupon` WRITE;
/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` VALUES (1,'优惠券',0.80,NULL,0,1,'2026-08-07 08:43:54','2026-08-07 08:43:54',1,2,7,NULL,NULL,'计算规则: 商品价格 * 优惠券折扣 - 优惠卷金额'),(5,'新人优惠券',NULL,10.00,1,1,'2026-08-18 12:01:55','2026-08-18 12:01:55',1,2,7,NULL,NULL,'');
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon_activity`
--

DROP TABLE IF EXISTS `coupon_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `coupon_id` bigint NOT NULL,
  `name` varchar(128) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `activity_start_time` datetime NOT NULL,
  `activity_end_time` datetime NOT NULL,
  `status` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1 普通活动 2 秒杀活动',
  PRIMARY KEY (`id`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_activity`
--

LOCK TABLES `coupon_activity` WRITE;
/*!40000 ALTER TABLE `coupon_activity` DISABLE KEYS */;
INSERT INTO `coupon_activity` VALUES (2,1,'新人活动',100,'2026-08-18 13:35:51','2026-09-01 13:35:59',0,'2026-08-18 05:05:29','2026-08-18 05:39:45',1),(3,1,'秒杀活动',100,'2026-08-18 13:47:00','2026-08-18 14:00:00',1,'2026-08-18 05:45:33','2026-08-18 05:47:41',2);
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券可用范围关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon_use_scope`
--

LOCK TABLES `coupon_use_scope` WRITE;
/*!40000 ALTER TABLE `coupon_use_scope` DISABLE KEYS */;
INSERT INTO `coupon_use_scope` VALUES (3,2084276653212233726,2084276653212233728,'2026-08-07 16:42:27');
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
) ENGINE=InnoDB AUTO_INCREMENT=5578 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鍒涗綔鑰呮?鍗';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `creator_rank`
--

LOCK TABLES `creator_rank` WRITE;
/*!40000 ALTER TABLE `creator_rank` DISABLE KEYS */;
INSERT INTO `creator_rank` VALUES (4175,1,1.00,1,'2026-08-03','2026-08-08 15:27:00','2026-08-08 15:27:00'),(4176,2084276653212233728,1.00,2,'2026-08-03','2026-08-08 15:27:00','2026-08-08 15:27:00'),(5388,2084276653212233728,3.00,1,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(5389,1,1.00,2,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(5576,2084276653212233728,3.00,1,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(5577,1,1.00,2,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00');
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
/*!40000 ALTER TABLE `emoji_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interaction_message`
--

DROP TABLE IF EXISTS `interaction_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interaction_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `sender_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送者头像',
  `sender_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送者名称',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `message_type` tinyint NOT NULL COMMENT '消息类型：1-点赞 2-评论 3-关注 4-提及 5-回复 6-收藏',
  `content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息内容',
  `associate_content` bigint DEFAULT NULL COMMENT '关联内容ID',
  `post_id` bigint DEFAULT NULL COMMENT '关联帖子ID',
  `associate_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联内容标题/快照',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '接收者是否删除：0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互动消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_message`
--

LOCK TABLES `interaction_message` WRITE;
/*!40000 ALTER TABLE `interaction_message` DISABLE KEYS */;
INSERT INTO `interaction_message` VALUES (11,2084276653212233728,'http://localhost:9000/stp-summit-files/avatar/d70be715-98f4-43ea-b70e-9863d1959086','summit',1,1,NULL,2085641774138826752,2085641774138826752,'第一篇帖子',0,'2026-08-12 23:22:02','2026-08-12 23:22:02');
/*!40000 ALTER TABLE `interaction_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member_level_config`
--

DROP TABLE IF EXISTS `member_level_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_level_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `level` int NOT NULL COMMENT 'VIP等级（1,2,3...）',
  `level_name` varchar(50) NOT NULL COMMENT '等级名称（如：黄金VIP）',
  `min_recharge` decimal(12,2) NOT NULL COMMENT '达到该等级所需的最小累计充值金额',
  `privileges_json` json NOT NULL COMMENT '特权配置（JSON格式）',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '等级图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level` (`level`),
  UNIQUE KEY `uk_min_recharge` (`min_recharge`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='VIP等级配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_level_config`
--

LOCK TABLES `member_level_config` WRITE;
/*!40000 ALTER TABLE `member_level_config` DISABLE KEYS */;
INSERT INTO `member_level_config` VALUES (1,1,'黄金VIP',100.00,'[{\"name\": \"专属头衔挂件\", \"enabled\": true, \"description\": \"\"}]','',0),(2,2,'白银VIP',200.00,'[]','',0);
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
  `status` int NOT NULL DEFAULT '0' COMMENT '0 未使用 1使用中',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员套餐表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_package`
--

LOCK TABLES `member_package` WRITE;
/*!40000 ALTER TABLE `member_package` DISABLE KEYS */;
INSERT INTO `member_package` VALUES (7,'白银会员',30.00,30,'2026-08-07 08:38:20','2026-08-08 07:09:53',NULL,1,0,0.7,0.0100,1,0),(8,'VIP连续包月套餐',13.00,30,'2026-08-18 11:38:02','2026-08-18 03:38:01','',1,999,1,0.0000,2,0);
/*!40000 ALTER TABLE `member_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `entity_id` bigint DEFAULT NULL COMMENT '操作实体ID',
  `type` varchar(100) NOT NULL DEFAULT 'OTHER' COMMENT '操作类型',
  `username` varchar(30) NOT NULL COMMENT '用户名',
  `operation` varchar(100) NOT NULL COMMENT '操作内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_log`
--

LOCK TABLES `operation_log` WRITE;
/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
INSERT INTO `operation_log` VALUES (1,2084276653212233728,NULL,'OTHER','U_18573757527','获取管理员列表','2026-08-16 14:28:19'),(2,2084276653212233728,NULL,'UPDATE','U_18573757527','禁用用户','2026-08-16 14:28:52'),(3,2084276653212233728,NULL,'UPDATE','U_18573757527','启用用户','2026-08-16 14:31:31'),(4,2084276653212233728,NULL,'UPDATE','U_18573757527','启用用户','2026-08-16 14:32:20'),(5,2084276653212233728,2084276653212233758,'UPDATE','U_18573757527','启用用户','2026-08-16 14:33:25'),(6,2084276653212233728,1,'UPDATE','U_18573757527','提升管理员等级','2026-08-16 14:35:10'),(7,2084276653212233728,2084276653212235221,'UPDATE','U_18573757527','禁用用户','2026-08-16 21:15:38'),(8,2084276653212233728,2084276653212235221,'UPDATE','U_18573757527','启用用户','2026-08-16 21:16:27'),(9,2084276653212233728,2084276653212235241,'UPDATE','U_18573757527','禁用用户','2026-08-16 21:25:42'),(10,2084276653212233728,2084276653212235241,'UPDATE','U_18573757527','启用用户','2026-08-16 21:26:04'),(11,2084276653212233728,2084276653212235241,'UPDATE','U_18573757527','禁用用户','2026-08-16 21:30:30'),(12,2084276653212233728,2084276653212235241,'UPDATE','U_18573757527','启用用户','2026-08-16 21:30:52'),(13,2084276653212233728,1,'UPDATE','U_18573757527','提升管理员等级','2026-08-16 21:34:27');
/*!40000 ALTER TABLE `operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_order`
--

DROP TABLE IF EXISTS `payment_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
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
  `timeout_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '超时时间',
  PRIMARY KEY (`id`),
  KEY `idx_payment_order_status_timeout_time_id` (`status`,`timeout_time`,`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_order`
--

LOCK TABLES `payment_order` WRITE;
/*!40000 ALTER TABLE `payment_order` DISABLE KEYS */;
INSERT INTO `payment_order` VALUES (1,0.01,1,0,3,'2026-08-07 20:21:41','2026-08-07 20:22:52',2084276653212233728,1,NULL,0.01,0.00,NULL,'2026-08-07 20:22:41'),(2,0.01,1,2084276653212233728,1,'2026-08-08 14:45:16','2026-08-08 14:46:14',2084276653212233728,1,NULL,0.01,0.00,'2026-08-08 14:46:14','2026-08-08 14:46:16');
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
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子收藏关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_collect`
--

LOCK TABLES `post_collect` WRITE;
/*!40000 ALTER TABLE `post_collect` DISABLE KEYS */;
INSERT INTO `post_collect` VALUES (17,2087546769910145024,2084276653212233728,'2026-08-12 14:30:00'),(18,2087547733119475712,2084276653212233728,'2026-08-12 14:39:00'),(19,2087508779536097280,2084276653212233728,'2026-08-12 14:48:00'),(20,2087547551690661888,2084276653212233728,'2026-08-12 14:48:00'),(21,2087534115955040256,2084276653212233728,'2026-08-12 14:48:00');
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
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子图片关联表（逻辑外键）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_image`
--

LOCK TABLES `post_image` WRITE;
/*!40000 ALTER TABLE `post_image` DISABLE KEYS */;
INSERT INTO `post_image` VALUES (41,2087547551690661888,'http://localhost:9000/stp-summit-files/post-media/b2069a74-e6f8-40bf-a6d2-10c8fc6aa941',1280,1921,NULL,0,1,'2026-08-12 14:31:35'),(42,2087547733119475712,'http://localhost:9000/stp-summit-files/post-media/6cc35780-2d31-4126-9eb2-8e841e957f4f',1280,1921,NULL,0,1,'2026-08-12 14:32:19');
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
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子点赞关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_like`
--

LOCK TABLES `post_like` WRITE;
/*!40000 ALTER TABLE `post_like` DISABLE KEYS */;
INSERT INTO `post_like` VALUES (27,2085641773287383040,1,'2026-08-07 08:21:00'),(28,2085915521936769024,2084276653212233728,'2026-08-08 03:27:00'),(30,2087508779536097280,2084276653212233728,'2026-08-12 14:09:00'),(31,2087546769910145024,2084276653212233728,'2026-08-12 14:30:00'),(32,2087547733119475712,2084276653212233728,'2026-08-12 14:39:00'),(33,2087547551690661888,2084276653212233728,'2026-08-12 14:39:00'),(34,2087534115955040256,2084276653212233728,'2026-08-12 14:48:00'),(35,2087553951007420416,2084276653212233728,'2026-08-12 15:00:00'),(36,2087547323180785664,2084276653212233728,'2026-08-12 15:03:00');
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
) ENGINE=InnoDB AUTO_INCREMENT=18853 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐑?偣甯栧瓙姒滃崟';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_rank`
--

LOCK TABLES `post_rank` WRITE;
/*!40000 ALTER TABLE `post_rank` DISABLE KEYS */;
INSERT INTO `post_rank` VALUES (12316,2085641774138826752,1.00,1,'2026-08-03','2026-08-08 15:27:00','2026-08-08 15:27:00'),(12317,2085915521936769024,1.00,2,'2026-08-03','2026-08-08 15:27:00','2026-08-08 15:27:00'),(17808,2085641773287383040,1.00,1,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17809,2085641773287383156,1.00,2,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17810,2085641773287383157,1.00,3,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17811,2085641773287383158,1.00,4,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17812,2085641773287383049,0.00,5,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17813,2085641773287383150,0.00,6,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17814,2085641773287383151,0.00,7,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17815,2085641773287383152,0.00,8,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17816,2085641773287383153,0.00,9,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17817,2085641773287383154,0.00,10,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(17818,2085641773287383155,0.00,11,'2026-08-10','2026-08-16 23:09:00','2026-08-16 23:09:00'),(18842,2085641773287383040,1.00,1,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18843,2085641773287383156,1.00,2,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18844,2085641773287383157,1.00,3,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18845,2085641773287383158,1.00,4,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18846,2085641773287383049,0.00,5,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18847,2085641773287383150,0.00,6,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18848,2085641773287383151,0.00,7,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18849,2085641773287383152,0.00,8,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18850,2085641773287383153,0.00,9,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18851,2085641773287383154,0.00,10,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00'),(18852,2085641773287383155,0.00,11,'2026-08-17','2026-08-18 15:36:00','2026-08-18 15:36:00');
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
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_tag_rel`
--

LOCK TABLES `post_tag_rel` WRITE;
/*!40000 ALTER TABLE `post_tag_rel` DISABLE KEYS */;
INSERT INTO `post_tag_rel` VALUES (66,2085641773287383040,32,'2026-08-07 08:18:43'),(67,2087475170972401664,2087475162185334784,'2026-08-12 09:43:59'),(68,2087480590583595008,2085641766723297280,'2026-08-12 10:05:31'),(69,2087485615145893888,2087475162185334784,'2026-08-12 10:25:29'),(70,2087486798405173248,2087486792726085632,'2026-08-12 10:30:11'),(71,2087487232482082816,2087475162185334784,'2026-08-12 10:31:54'),(72,2087487330750431232,2085641766723297280,'2026-08-12 10:32:17'),(73,2087488582599299072,2087488575909384192,'2026-08-12 10:37:16'),(74,2087508779536097280,2087508127846113280,'2026-08-12 11:57:31'),(75,2087508779536097280,2087508155394301952,'2026-08-12 11:57:31'),(76,2087534115955040256,2087532928170090496,'2026-08-12 13:38:12'),(77,2087541929721131008,2087541887018921984,'2026-08-12 14:09:15'),(78,2087541929721131008,2087486792726085632,'2026-08-12 14:09:15'),(79,2087546769910145024,2087486792726085632,'2026-08-12 14:28:29'),(80,2087546769910145024,2087508155394301952,'2026-08-12 14:28:29'),(81,2087546769910145024,2087541887018921984,'2026-08-12 14:28:29'),(82,2087546955956887552,2085641766723297280,'2026-08-12 14:29:13'),(83,2087546955956887552,2087508155394301952,'2026-08-12 14:29:13'),(84,2087547101348241408,2087475162185334784,'2026-08-12 14:29:48'),(85,2087547101348241408,2087488575909384192,'2026-08-12 14:29:48'),(86,2087547101348241408,2087508127846113280,'2026-08-12 14:29:48'),(87,2087547101348241408,2087508155394301952,'2026-08-12 14:29:48'),(88,2087547101348241408,2087532928170090496,'2026-08-12 14:29:48'),(89,2087547323180785664,2087486792726085632,'2026-08-12 14:30:41'),(90,2087547323180785664,2087488575909384192,'2026-08-12 14:30:41'),(91,2087547323180785664,2087508155394301952,'2026-08-12 14:30:41'),(92,2087547551690661888,2087488575909384192,'2026-08-12 14:31:35'),(93,2087547733119475712,2087547669416386560,'2026-08-12 14:32:19'),(94,2087547733119475712,2087547682250956800,'2026-08-12 14:32:19'),(95,2087547733119475712,2087547701825773568,'2026-08-12 14:32:19'),(96,2087553951007420416,2087532928170090496,'2026-08-12 14:57:01'),(97,2087553951007420416,2087553892501073920,'2026-08-12 14:57:01');
/*!40000 ALTER TABLE `post_tag_rel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `posts` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `creator_id` bigint NOT NULL COMMENT '发布者用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `type` int NOT NULL DEFAULT '1' COMMENT '帖子类型(1 文本 2 图片 3视频 4音频 5连接 6文件 7未知)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '文本内容',
  `media_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '媒体资源URL列表',
  `reply_count` int unsigned NOT NULL DEFAULT '0' COMMENT '回复数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '''状态: 1正常, 2删除, 3封禁, 4举报, 5未知, 6草稿',
  `unpass_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核未通过原因',
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
) ENGINE=InnoDB AUTO_INCREMENT=2085641773287383159 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `posts`
--

LOCK TABLES `posts` WRITE;
/*!40000 ALTER TABLE `posts` DISABLE KEYS */;
INSERT INTO `posts` VALUES (2085641773287383040,1,'第一篇帖子',1,'start <a>#第一篇#</a>','',0,1,'2026-08-07 16:18:43','2026-08-07 08:21:00',0,1,1,1,1,0),(2085641773287383041,2084276653212233728,'publish',1,'Once','',0,2,'2026-08-08 10:26:30','2026-08-15 13:00:05',0,1,1,1,1,0),(2085641773287383042,2084276653212233728,'今天学习ElasticSearch',1,'好开心 <a>#ElasticSearch#</a>','',0,2,'2026-08-12 17:43:59','2026-08-12 19:45:49',0,0,0,0,1,0),(2085641773287383043,2084276653212233728,'开心?防丢失',1,'<a>#第一篇#</a>','',0,2,'2026-08-12 18:05:31','2026-08-12 18:31:39',0,0,0,0,1,0),(2085641773287383044,2084276653212233728,'测试分词器',1,'<a>#ElasticSearch#</a>','',0,2,'2026-08-12 18:25:29','2026-08-12 18:31:37',0,0,0,0,1,0),(2085641773287383045,2084276653212233728,'羡慕有钱人',1,'<a>#财富#</a>','',0,2,'2026-08-12 18:30:11','2026-08-12 18:31:35',0,0,0,0,1,0),(2085641773287383046,2084276653212233728,'今天学习Es',1,'<a>#ElasticSearch#</a>','',0,2,'2026-08-12 18:31:55','2026-08-12 19:45:47',0,0,0,0,1,0),(2085641773287383047,2084276653212233728,'好的,我知道了',1,'<a>#第一篇#</a>','',0,2,'2026-08-12 18:32:18','2026-08-12 19:45:45',0,0,0,0,1,0),(2085641773287383048,2084276653212233728,'你好帅,蔡徐坤',1,'<a>#蔡徐坤#</a>','',0,2,'2026-08-12 18:37:16','2026-08-12 19:45:43',0,0,0,0,1,0),(2085641773287383049,2084276653212233728,'听说狗熊岭来了一只正太松鼠~',3,'<a>#蹦蹦#</a> <a>#正太扭腰#</a>','http://localhost:9000/stp-summit-files/post-media/81d21017-9a50-4646-b9e0-acb0d20c5c8e.mp4',0,1,'2026-08-12 19:57:32','2026-08-12 19:57:32',0,0,0,0,1,0),(2085641773287383150,2084276653212233728,'《当全球吃苦能力降低10万倍而我不变》',3,'<a>#搞笑#</a>','http://localhost:9000/stp-summit-files/post-media/a95cc3df-1587-4292-966f-99b92a9babcb.mp4',0,1,'2026-08-12 21:38:12','2026-08-12 21:38:12',0,0,0,0,1,0),(2085641773287383151,2084276653212233728,'标签测试',1,'<a>#测试#</a> <a>#财富#</a>','',0,1,'2026-08-12 22:09:15','2026-08-12 22:09:15',0,0,0,0,1,0),(2085641773287383152,2084276653212233728,'标签测试2',1,'<a>#测试#</a> <a>#财富#</a> <a>#正太扭腰#</a>','',0,1,'2026-08-12 22:28:29','2026-08-12 22:28:29',0,0,0,0,1,0),(2085641773287383153,2084276653212233728,'标签测试3',1,'<a>#第一篇#</a> <a>#正太扭腰#</a>','',0,1,'2026-08-12 22:29:14','2026-08-12 22:29:14',0,0,0,0,1,0),(2085641773287383154,2084276653212233728,'标签测试4',1,'<a>#正太扭腰#</a> <a>#搞笑#</a> <a>#ElasticSearch#</a> <a>#蹦蹦#</a> <a>#蔡徐坤#</a>','',0,1,'2026-08-12 22:29:48','2026-08-12 22:29:48',0,0,0,0,1,0),(2085641773287383155,2084276653212233728,'标签测试5',1,'<a>#正太扭腰#</a> <a>#财富#</a> <a>#蔡徐坤#</a>','',0,1,'2026-08-12 22:30:41','2026-08-12 22:30:41',0,0,0,0,1,0),(2085641773287383156,2084276653212233728,'标签测试6',2,'<a>#蔡徐坤#</a>','',0,1,'2026-08-12 22:31:36','2026-08-13 15:00:01',0,0,1,1,1,1),(2085641773287383157,2084276653212233728,'李一桐大美女',2,'<a>#李一桐#</a> <a>#美女#</a> <a>#甜美#</a>','',0,1,'2026-08-12 22:32:19','2026-08-13 15:00:01',0,0,1,1,1,1),(2085641773287383158,2084276653212233728,'同父同母姐弟差距离谱，姐姐自律不敌锤锤',3,'<a>#开心锤锤#</a> <a>#搞笑#</a>','http://localhost:9000/stp-summit-files/post-media/09016552-7d97-4169-9219-0542817be36b.mp4',1,1,'2026-08-12 22:57:01','2026-08-15 20:57:00',0,2,1,1,1,0);
/*!40000 ALTER TABLE `posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `private_message`
--

DROP TABLE IF EXISTS `private_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `private_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
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
) ENGINE=InnoDB AUTO_INCREMENT=339412780337598465 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='私信消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `private_message`
--

LOCK TABLES `private_message` WRITE;
/*!40000 ALTER TABLE `private_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `private_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session`
--

DROP TABLE IF EXISTS `session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
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
/*!40000 ALTER TABLE `session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_message`
--

DROP TABLE IF EXISTS `system_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
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
/*!40000 ALTER TABLE `system_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_message_image`
--

DROP TABLE IF EXISTS `system_message_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_message_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
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
  `tag_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名',
  `sort` int DEFAULT '0' COMMENT '权重排序',
  `use_count` int DEFAULT '0' COMMENT '使用次数',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0禁用, 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`tag_name`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
INSERT INTO `tag` VALUES (32,'第一篇',0,2,0,'2026-08-07 16:18:41'),(33,'ElasticSearch',0,1,0,'2026-08-12 17:43:57'),(34,'财富',0,2,0,'2026-08-12 18:30:10'),(35,'蔡徐坤',0,3,0,'2026-08-12 18:37:15'),(36,'蹦蹦',0,1,0,'2026-08-12 19:54:56'),(37,'正太扭腰',0,4,0,'2026-08-12 19:55:03'),(38,'搞笑',0,2,0,'2026-08-12 21:33:29'),(39,'测试',0,1,0,'2026-08-12 22:09:05'),(40,'李一桐',0,1,0,'2026-08-12 22:32:04'),(41,'美女',0,1,0,'2026-08-12 22:32:07'),(42,'甜美',0,1,0,'2026-08-12 22:32:12'),(43,'开心锤锤',0,1,0,'2026-08-12 22:56:48');
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
) ENGINE=InnoDB AUTO_INCREMENT=4084 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璇濋?姒滃崟';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic_rank`
--

LOCK TABLES `topic_rank` WRITE;
/*!40000 ALTER TABLE `topic_rank` DISABLE KEYS */;
/*!40000 ALTER TABLE `topic_rank` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `undo_log`
--

DROP TABLE IF EXISTS `undo_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `undo_log` (
  `branch_id` bigint NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`),
  KEY `ix_log_created` (`log_created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AT transaction mode undo table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `undo_log`
--

LOCK TABLES `undo_log` WRITE;
/*!40000 ALTER TABLE `undo_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `undo_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
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
) ENGINE=InnoDB AUTO_INCREMENT=2084276653212235281 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (2084276653212233728,'U_18573757527','$2a$10$80IHjPR8SMk7y7aScXyQxuVn3qFaS68M36i2LHLlR65JqJ94iACr.','18573757527',1,'2026-08-03 13:54:12','2026-08-18 13:31:53','http://localhost:9000/stp-summit-files/avatar/d70be715-98f4-43ea-b70e-9863d1959086',NULL,'未知','','summit',1,19,0,NULL),(2084276653212233758,'U_13999999393','$2a$10$HoyUM0o3wxha3IXZAYDovO0dAfdfBzYHFw0R4bHiisHYm.pfQ7EBK','13999999393',1,'2026-08-16 11:16:17','2026-08-16 15:03:22','http://localhost:9000/stp-summit-files/avatar/5e20a2c4-c3e1-460b-b636-34b64553ad80',NULL,'未知','大家好','liu',1,20,0,NULL),(2084276653212235231,'U_19966772684','$2a$10$EztB6JKdjjbGDyBWcvuxR.rM/spmJD8A9jCkMc0AeYAHpMeqHyPdy','19966772684',1,'2026-08-16 21:23:15','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772684',1,NULL,0,NULL),(2084276653212235232,'U_18866772682','$2a$10$GmrRWwdi3NU6LZ8LB.OKoeCsEqFiyU.fybl3XIjVwWmkn8UNV4vBy','18866772682',1,'2026-08-16 21:23:15','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772682',1,NULL,0,NULL),(2084276653212235233,'U_13966772685','$2a$10$QaVKow5XIQM.BYauSnbb1OS7iHwdi9I4OR4JiBdDC/m1HjkKdK7RO','13966772685',1,'2026-08-16 21:23:15','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772685',1,NULL,0,NULL),(2084276653212235234,'U_18866772683','$2a$10$iQmc/BuIaSzprjPN6dLR6OlKfiqzSVgCOLMCEbzz34FlqW04G30G6','18866772683',1,'2026-08-16 21:23:15','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772683',1,NULL,0,NULL),(2084276653212235235,'U_18866772686','$2a$10$xjtoxFh0q4sa8JJQPNO2E.6nK2cOyJv7u5gOpil7peq9Nzg98Nzz2','18866772686',1,'2026-08-16 21:23:15','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772686',1,NULL,0,NULL),(2084276653212235236,'U_15866772687','$2a$10$nYSfs8jlX.BNKPZu9Z2bxewZRORDfIFAlWYd5aUAuxcpu7VN/hbW.','15866772687',1,'2026-08-16 21:23:16','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_15866772687',1,NULL,0,NULL),(2084276653212235237,'U_18866772689','$2a$10$M08AAw96TC6U7yxbqEa25ebzwJD8aZ04ZsQ8mR1DUoUcxzlruVMdu','18866772689',1,'2026-08-16 21:23:16','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772689',1,NULL,0,NULL),(2084276653212235238,'U_19966772691','$2a$10$7UW56cd7lMlZ24aLa4ddLuhnYaj8hQ66rqBqqYVarMW7OWJKWCtIi','19966772691',1,'2026-08-16 21:23:16','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772691',1,NULL,0,NULL),(2084276653212235239,'U_18866772690','$2a$10$HdEwPLN5hpwfsj8346tZbuaqyzOuJaN1th/cug3ov6rhUvxG6zqBq','18866772690',1,'2026-08-16 21:23:16','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772690',1,NULL,0,NULL),(2084276653212235240,'U_18866772688','$2a$10$YQ7iwq1TZyKi6lkRHzOExepBo5Ma4.SlB535WAAxFHbbUCho5.cbq','18866772688',1,'2026-08-16 21:23:16','2026-08-16 13:23:15','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772688',1,NULL,0,NULL),(2084276653212235241,'U_19966772692','$2a$10$SMS4fxvzwPtyN9vCRcg1Nu8R9e9nJsH9I/oKAimb.3UZbjRhp/G46','19966772692',1,'2026-08-16 21:23:18','2026-08-16 21:30:52','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772692',0,77,0,NULL),(2084276653212235242,'U_13966772694','$2a$10$Ry2kzOgP7aQOF.uaiZqCnuqj4uNij5aMIM4bkwKRFUMy.AcdTpp1q','13966772694',1,'2026-08-16 21:23:18','2026-08-16 13:23:17','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772694',1,NULL,0,NULL),(2084276653212235243,'U_18866772696','$2a$10$923v2WEAB62MvhJquEYK6uSqb0cxU3FewSC6MXPu/psN08P4U1Po6','18866772696',1,'2026-08-16 21:23:18','2026-08-16 13:25:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772696',0,NULL,0,NULL),(2084276653212235244,'U_18866772695','$2a$10$0P5GGGwZTED1L5oLf2WH1ONRtsunQLbzjSVGJ0sU5HRjzyYwn4COu','18866772695',1,'2026-08-16 21:23:18','2026-08-16 13:23:17','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772695',1,NULL,0,NULL),(2084276653212235245,'U_19966772693','$2a$10$Jhd3YmwTAh2V0mVBLyc0ZuNCR0P.ff5E0cqWjG71OWy64gZRseu9m','19966772693',1,'2026-08-16 21:23:18','2026-08-16 13:23:17','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772693',1,NULL,0,NULL),(2084276653212235246,'U_15866772699','$2a$10$L2Dr4l/K3nCfiJBPNHt.1eLc4KC0ej.1XnL8xo0XgtcWmCSkDzrU.','15866772699',1,'2026-08-16 21:23:18','2026-08-16 13:23:18','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_15866772699',1,NULL,0,NULL),(2084276653212235247,'U_13966772697','$2a$10$IytJw6TC2mPTFydc6u7iMOelY.ki.5O/oyZDgY.30bWvT7Uip3gWe','13966772697',1,'2026-08-16 21:23:18','2026-08-16 13:23:18','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772697',1,NULL,0,NULL),(2084276653212235248,'U_13966772698','$2a$10$wYrj3S5stv/mWwtUChYC2u3HWJoXSbaMT5C3lUEdHX6JrqGzhGMVq','13966772698',1,'2026-08-16 21:23:18','2026-08-16 13:23:18','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772698',1,NULL,0,NULL),(2084276653212235249,'U_13966772701','$2a$10$7owVFe5LhzPAzZu2q5yJuOuZrr3ovZbU/UfsCsgiA5qQd81Or506K','13966772701',1,'2026-08-16 21:23:18','2026-08-16 13:23:18','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772701',1,NULL,0,NULL),(2084276653212235250,'U_13966772700','$2a$10$Trticn9nayOxYVc/FNA0K.VvSszi.nVsAJjIP6D90f47gPdiA34xa','13966772700',1,'2026-08-16 21:23:18','2026-08-16 13:23:18','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772700',1,NULL,0,NULL),(2084276653212235251,'U_18866772706','$2a$10$eBtivCaC0ZudyENXo7Jpgu/1SVwNrv9Ipt7walb6NP/pZ809mr0jS','18866772706',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772706',1,NULL,0,NULL),(2084276653212235252,'U_18866772703','$2a$10$amv1yUmxH6W8dxyq2ozX4uu.UvF1i3XnFiu0RCHqxMUGh6NHTxCna','18866772703',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772703',1,NULL,0,NULL),(2084276653212235253,'U_18866772704','$2a$10$1zHHYh/KFHTYVN6tf20WYuet2SpUfHv0bXxV3.VMK18wVMBjzgFda','18866772704',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772704',1,NULL,0,NULL),(2084276653212235254,'U_13966772705','$2a$10$IhDp5XCZbhXJYcLOLEu0ju5qxJ4J/znlw.Dbnq79osAn85cOKHRve','13966772705',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772705',1,NULL,0,NULL),(2084276653212235255,'U_18866772702','$2a$10$7z5Mo309CLBz4npfKY7JQutwKA78Lym8wK4MIZC40SwMtwy06tLsW','18866772702',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772702',1,NULL,0,NULL),(2084276653212235256,'U_13966772707','$2a$10$JCM1xjMCEHz4A9E7hq51z.tbBmhGm6PX1V.VMLeLzA3f3vGZEUeZO','13966772707',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772707',1,NULL,0,NULL),(2084276653212235257,'U_13966772709','$2a$10$y1z3YZV0I5FT5GhluwkAu.xz9FYb8GuCePo3URgbQp9L0mXQt5Sn.','13966772709',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772709',1,NULL,0,NULL),(2084276653212235258,'U_13966772710','$2a$10$KMR22nnpc3r55zhp.lXxhehjNzoEaNmvJAxPMg4c8Zy9i8ANnydri','13966772710',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772710',1,NULL,0,NULL),(2084276653212235259,'U_19966772711','$2a$10$OXZa6DQ7th4TAeeEAQGgY.cMzuT39Nh0U2g71SSDlXk0vtA8dnh6e','19966772711',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772711',1,NULL,0,NULL),(2084276653212235260,'U_19966772708','$2a$10$2XEUppIlsHy8J3Xub/2HY.HZyYu7oWJEJag1a6dz0.qAN5JDvNJmm','19966772708',1,'2026-08-16 21:23:20','2026-08-16 13:23:20','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772708',1,NULL,0,NULL),(2084276653212235261,'U_19966772715','$2a$10$6LGBM8yKJ2oEtj61SK9joO9.jv4y6jDUVH3hQooRzcDYFjPhBKTYW','19966772715',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772715',1,NULL,0,NULL),(2084276653212235262,'U_13966772716','$2a$10$pYqicZWshQnDu.x2Q497.ucLDYIwxvX.wl/QKUTMa5ZvEGozuFrQu','13966772716',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772716',1,NULL,0,NULL),(2084276653212235263,'U_13966772714','$2a$10$y9fkzmfOsESGutjfcHdUcuKTpzMXz4IbYJ0E4FzioKoAXeKQfx7Ry','13966772714',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772714',1,NULL,0,NULL),(2084276653212235264,'U_19966772712','$2a$10$0lKmwARJJyaTQNsWmHj/h.YrD60Y0Y.hUYmDHM6kBFaJKXG/pf3ym','19966772712',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772712',1,NULL,0,NULL),(2084276653212235265,'U_15866772713','$2a$10$/Y3ixdKgSKLvSkYacKrrQ.O/Hl1G/VmMCV/G/RZjUZoH/gBDuzdQ2','15866772713',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_15866772713',1,NULL,0,NULL),(2084276653212235266,'U_19966772720','$2a$10$Ifo3Yr6ibKJnhYFQWTVPTeYYCo8m1rlpzvug8Zf8zv6M4OCxCoOYe','19966772720',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772720',1,NULL,0,NULL),(2084276653212235267,'U_18866772719','$2a$10$tHmOkzjaibGqVTdeNJJmq.Np9wkeid2czgIcSD0L0ssYItUgdpyQG','18866772719',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772719',1,NULL,0,NULL),(2084276653212235268,'U_15866772717','$2a$10$FN19wd3DdFYFy.qZxTCIeuR79BTqKTvVZx9i8Avlp.oLw0gzIO8Hi','15866772717',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_15866772717',1,NULL,0,NULL),(2084276653212235269,'U_15866772718','$2a$10$utF50aK5WzfEtCNXWxp3n.PWLDHlWqbAYb6iJMCmMjAtK5fNXCl4m','15866772718',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_15866772718',1,NULL,0,NULL),(2084276653212235270,'U_18866772721','$2a$10$x7R3fNOJmoQJk/Em3w6J..mwjJNNl5HuKiHmzMnc5Lv5CrnkAFUZ.','18866772721',1,'2026-08-16 21:23:22','2026-08-16 13:23:22','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772721',1,NULL,0,NULL),(2084276653212235271,'U_15866772723','$2a$10$82FwY6Io3BCELSJSqye39O56FE8H4GQ3XlvPYW7/PDFgCNQ5NOBfm','15866772723',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_15866772723',1,NULL,0,NULL),(2084276653212235272,'U_19966772724','$2a$10$qpIl/VxBn0XgGe9/G7wk7uPwI/1yyVf7qTVxisE7DS2lwVBqOfI/6','19966772724',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772724',1,NULL,0,NULL),(2084276653212235273,'U_18866772725','$2a$10$uuTZSp//..LHOxVPdqrNeegra4LEqLLwg6hoa6LPiv7RpSnX9tfwy','18866772725',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772725',1,NULL,0,NULL),(2084276653212235274,'U_18866772726','$2a$10$crp.z.4yez9WskyH08QsjeVf1Ja8puaVCEdgduMT1VKH4iaCyg3nm','18866772726',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772726',1,NULL,0,NULL),(2084276653212235275,'U_15866772722','$2a$10$H37U25UMvsm/mDqGiNn2u.AdctNEn3WZ7yd93iNxamVFhuH4lQssi','15866772722',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_15866772722',1,NULL,0,NULL),(2084276653212235276,'U_15866772727','$2a$10$koSPkGMjUePaG2jht6UZUehaks9SU.vz/L7R.yIdUKuhIIU9JNEM.','15866772727',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_15866772727',1,NULL,0,NULL),(2084276653212235277,'U_13966772729','$2a$10$EutpAOdyD0Ng9bPjah0rzOJXKWEBj2Fw14iQFw0it/zyYQiLEegcK','13966772729',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_13966772729',1,NULL,0,NULL),(2084276653212235278,'U_18866772731','$2a$10$zwOemTpcd.EDfYllMTCLg.2oMrEP3zCVAbtlI529IuAoLImJVMsi.','18866772731',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_18866772731',1,NULL,0,NULL),(2084276653212235279,'U_19966772730','$2a$10$KOw8ftEMyFO27pypmmzASuCQ9OQH5TtMbmXkvFXJ.KVKQHKuv/5Oi','19966772730',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772730',1,NULL,0,NULL),(2084276653212235280,'U_19966772728','$2a$10$j4rCv6oSu2y2A8g31ZE3.OrhSS/shMgZUDmBj3vL/Rm/4hIga/w/S','19966772728',1,'2026-08-16 21:23:25','2026-08-16 13:23:24','https://pic4.zhimg.com/50/v2-6afa72220d29f045c15217aa6b275808_hd.jpg?source=1940ef5c',NULL,NULL,NULL,'U_19966772728',1,NULL,0,NULL);
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
  `status` int NOT NULL DEFAULT '1' COMMENT '使用状态: 0-未使用, 1-已使用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `used_time` timestamp NULL DEFAULT NULL COMMENT '核销使用时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `order_id` bigint DEFAULT NULL COMMENT '订单id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_coupon_status` (`user_id`,`coupon_id`,`status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=223 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券实例映射表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_coupon`
--

LOCK TABLES `user_coupon` WRITE;
/*!40000 ALTER TABLE `user_coupon` DISABLE KEYS */;
INSERT INTO `user_coupon` VALUES (220,2084276653212233728,2084276653212233726,1,'2026-08-07 20:08:36','2026-08-07 12:08:36',NULL,'2026-08-14 20:08:36',NULL),(221,2084276653212233728,2084276653212233726,0,'2026-08-08 14:39:28','2026-08-08 06:39:28',NULL,'2026-08-15 14:39:28',NULL),(222,2084276653212233728,1,0,'2026-08-18 13:47:55','2026-08-18 05:47:55',NULL,'2026-08-25 13:47:55',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_follow`
--

LOCK TABLES `user_follow` WRITE;
/*!40000 ALTER TABLE `user_follow` DISABLE KEYS */;
INSERT INTO `user_follow` VALUES (5,2084276653212233700,1,2,'hover_card','2026-08-07 12:26:59','2026-08-07 12:26:59');
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
INSERT INTO `user_member` VALUES (2084276653212233728,0.01,1,'2026-08-08 14:46:14','2026-08-08 14:46:14',1,'2026-08-09 14:46:14',0.0100);
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户会话设置表（每人一份）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_session`
--

LOCK TABLES `user_session` WRITE;
/*!40000 ALTER TABLE `user_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_setting`
--

DROP TABLE IF EXISTS `user_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `show_delPost` tinyint NOT NULL DEFAULT '1' COMMENT '是否显示删除的帖子',
  `Customization_recommend` tinyint NOT NULL DEFAULT '1' COMMENT '是否开启个性化推荐',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2085641569041625741 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_setting`
--

LOCK TABLES `user_setting` WRITE;
/*!40000 ALTER TABLE `user_setting` DISABLE KEYS */;
INSERT INTO `user_setting` VALUES (2085641569041625089,2084276653212233728,0,1,'2026-08-07 13:12:08','2026-08-07 13:12:08'),(2085641569041625090,2084276653212233758,1,1,'2026-08-16 03:16:17','2026-08-16 03:16:17'),(2085641569041625691,2084276653212235231,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625692,2084276653212235233,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625693,2084276653212235234,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625694,2084276653212235232,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625695,2084276653212235235,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625696,2084276653212235238,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625697,2084276653212235239,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625698,2084276653212235236,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625699,2084276653212235237,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625700,2084276653212235240,1,1,'2026-08-16 13:23:15','2026-08-16 13:23:15'),(2085641569041625701,2084276653212235241,1,1,'2026-08-16 13:23:17','2026-08-16 13:23:17'),(2085641569041625702,2084276653212235243,1,1,'2026-08-16 13:23:17','2026-08-16 13:23:17'),(2085641569041625703,2084276653212235242,1,1,'2026-08-16 13:23:17','2026-08-16 13:23:17'),(2085641569041625704,2084276653212235244,1,1,'2026-08-16 13:23:17','2026-08-16 13:23:17'),(2085641569041625705,2084276653212235245,1,1,'2026-08-16 13:23:17','2026-08-16 13:23:17'),(2085641569041625706,2084276653212235246,1,1,'2026-08-16 13:23:18','2026-08-16 13:23:18'),(2085641569041625707,2084276653212235247,1,1,'2026-08-16 13:23:18','2026-08-16 13:23:18'),(2085641569041625708,2084276653212235248,1,1,'2026-08-16 13:23:18','2026-08-16 13:23:18'),(2085641569041625709,2084276653212235249,1,1,'2026-08-16 13:23:18','2026-08-16 13:23:18'),(2085641569041625710,2084276653212235250,1,1,'2026-08-16 13:23:18','2026-08-16 13:23:18'),(2085641569041625711,2084276653212235252,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625712,2084276653212235251,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625713,2084276653212235253,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625714,2084276653212235254,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625715,2084276653212235255,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625716,2084276653212235256,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625717,2084276653212235260,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625718,2084276653212235257,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625719,2084276653212235258,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625720,2084276653212235259,1,1,'2026-08-16 13:23:20','2026-08-16 13:23:20'),(2085641569041625721,2084276653212235261,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625722,2084276653212235262,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625723,2084276653212235263,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625724,2084276653212235265,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625725,2084276653212235264,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625726,2084276653212235267,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625727,2084276653212235266,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625728,2084276653212235268,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625729,2084276653212235269,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625730,2084276653212235270,1,1,'2026-08-16 13:23:22','2026-08-16 13:23:22'),(2085641569041625731,2084276653212235271,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24'),(2085641569041625732,2084276653212235273,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24'),(2085641569041625733,2084276653212235272,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24'),(2085641569041625734,2084276653212235274,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24'),(2085641569041625735,2084276653212235275,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24'),(2085641569041625736,2084276653212235276,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24'),(2085641569041625737,2084276653212235277,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24'),(2085641569041625738,2084276653212235279,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24'),(2085641569041625739,2084276653212235278,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24'),(2085641569041625740,2084276653212235280,1,1,'2026-08-16 13:23:24','2026-08-16 13:23:24');
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
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛绛惧埌娴佹按琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_sign_log`
--

LOCK TABLES `user_sign_log` WRITE;
/*!40000 ALTER TABLE `user_sign_log` DISABLE KEYS */;
INSERT INTO `user_sign_log` VALUES (19,2084276653212233728,'2026-08-06','2026-08-06 15:10:42',1,0,1,NULL),(20,2084276653212233728,'2026-08-07','2026-08-07 08:17:47',1,0,2,NULL),(21,2084276653212233728,'2026-08-08','2026-08-08 03:25:58',1,0,1,NULL),(22,2084276653212233728,'2026-08-12','2026-08-12 06:48:20',1,0,1,NULL),(23,2084276653212233728,'2026-08-13','2026-08-13 07:02:42',1,0,2,NULL);
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
INSERT INTO `user_sign_stats` VALUES (1,2,2,2,'2026-08-07','2026-08-07 16:17:48'),(2084276653212233728,3,2,2,'2026-08-13','2026-08-13 15:02:42'),(2088652920940806145,0,0,0,NULL,'2026-08-15 23:43:56');
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
INSERT INTO `user_stat` VALUES (2084276653212233728,0,0,0),(2084276653212233758,0,0,0),(2084276653212235231,0,0,0),(2084276653212235232,0,0,0),(2084276653212235233,0,0,0),(2084276653212235234,0,0,0),(2084276653212235235,0,0,0),(2084276653212235236,0,0,0),(2084276653212235237,0,0,0),(2084276653212235238,0,0,0),(2084276653212235239,0,0,0),(2084276653212235240,0,0,0),(2084276653212235241,0,0,0),(2084276653212235242,0,0,0),(2084276653212235243,0,0,0),(2084276653212235244,0,0,0),(2084276653212235245,0,0,0),(2084276653212235246,0,0,0),(2084276653212235247,0,0,0),(2084276653212235248,0,0,0),(2084276653212235249,0,0,0),(2084276653212235250,0,0,0),(2084276653212235251,0,0,0),(2084276653212235252,0,0,0),(2084276653212235253,0,0,0),(2084276653212235254,0,0,0),(2084276653212235255,0,0,0),(2084276653212235256,0,0,0),(2084276653212235257,0,0,0),(2084276653212235258,0,0,0),(2084276653212235259,0,0,0),(2084276653212235260,0,0,0),(2084276653212235261,0,0,0),(2084276653212235262,0,0,0),(2084276653212235263,0,0,0),(2084276653212235264,0,0,0),(2084276653212235265,0,0,0),(2084276653212235266,0,0,0),(2084276653212235267,0,0,0),(2084276653212235268,0,0,0),(2084276653212235269,0,0,0),(2084276653212235270,0,0,0),(2084276653212235271,0,0,0),(2084276653212235272,0,0,0),(2084276653212235273,0,0,0),(2084276653212235274,0,0,0),(2084276653212235275,0,0,0),(2084276653212235276,0,0,0),(2084276653212235277,0,0,0),(2084276653212235278,0,0,0),(2084276653212235279,0,0,0),(2084276653212235280,0,0,0);
/*!40000 ALTER TABLE `user_stat` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-18 15:36:26

-- Table structure for user_report
CREATE TABLE IF NOT EXISTS `user_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reporter_id` bigint NOT NULL COMMENT '举报用户ID',
  `reported_id` bigint NOT NULL COMMENT '被举报用户ID',
  `reason` varchar(500) NOT NULL COMMENT '举报原因',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待处理, 1-已忽略, 2-已处置',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_reported_id` (`reported_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户举报记录表';

-- Table structure for system_activity
CREATE TABLE IF NOT EXISTS `system_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(32) NOT NULL COMMENT '活动类型: WARN-预警, INFO-日志, ORDER-交易, AUDIT-审核',
  `module` varchar(64) DEFAULT NULL COMMENT '业务模块: USER, POST, ORDER, SYSTEM',
  `title` varchar(128) NOT NULL COMMENT '活动标题',
  `content` varchar(500) NOT NULL COMMENT '活动详细说明',
  `target_url` varchar(256) DEFAULT NULL COMMENT '目标路由地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统活动与预警日志表';

INSERT INTO `system_activity` (`id`, `type`, `module`, `title`, `content`, `target_url`, `create_time`) VALUES
(101, 'WARN', 'USER', '检测到高频发帖违规行为', '用户 ID: 10092 在 1 分钟内连续发布 15 条重复推广广告', '/user/report', '2026-08-20 13:20:15'),
(102, 'ORDER', 'ORDER', '大额会员充值成功', '用户 ID: 10854 购买了 [VIP 尊享年卡]，金额 ￥198.00', '/order', '2026-08-20 13:15:02'),
(103, 'AUDIT', 'POST', '新发布帖子触发敏感词人工审核', '帖子 ID: 40921 包含高风险关键词需人工二次核查', '/post', '2026-08-20 12:45:10'),
(104, 'INFO', 'SYSTEM', '微服务高可用节点自动扩容成功', 'stp-user-service 节点增加至 3 台副本', '/dashboard', '2026-08-20 11:30:00');


