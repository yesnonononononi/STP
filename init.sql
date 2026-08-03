-- MySQL dump 10.13  Distrib 8.4.6, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: stp-project
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
-- Table structure for table `comment_image`
--

DROP TABLE IF EXISTS `comment_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_comment_id` (`comment_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论图片关联表（逻辑外键）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comment_like`
--

DROP TABLE IF EXISTS `comment_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论点赞关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL COMMENT '唯一id',
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论',
  `root_id` bigint DEFAULT NULL COMMENT '根评论ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `is_audit` tinyint(1) DEFAULT '0' COMMENT '是否审核: 0否, 1是',
  `type` int NOT NULL DEFAULT '1' COMMENT '评论类型: 1文字, 2图片, 3视频, 4音频',
  `is_top` int NOT NULL DEFAULT '0' COMMENT '是否置顶: 0否, 1是',
  `status` int NOT NULL DEFAULT '1' COMMENT '评论状态: 0已删除, 1正常',
  `extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '额外扩展信息 (JSON 格式，包含媒体类型、图片元数据、媒体URL等)',
  `reply_count` int NOT NULL DEFAULT '0' COMMENT '回复数/子评论数',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `ip_location` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP归属地',
  `client_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端类型: ios, android, web等',
  `hot_score` bigint NOT NULL DEFAULT '0',
  `like_count` bigint NOT NULL DEFAULT '0' COMMENT '点赞数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_root_id` (`root_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_updatetime_id` (`status`,`update_time`,`id`),
  KEY `idx_status_parent_root_updatetime` (`status`,`parent_id`,`root_id`,`update_time`,`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2080923482518106121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表（支持回复）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `coupon`
--

DROP TABLE IF EXISTS `coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon` (
  `id` bigint NOT NULL COMMENT '优惠券ID',
  `public_id` bigint NOT NULL,
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `coupon_activity`
--

DROP TABLE IF EXISTS `coupon_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `coupon_use_scope`
--

DROP TABLE IF EXISTS `coupon_use_scope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_use_scope` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `public_id` bigint NOT NULL,
  `coupon_id` bigint NOT NULL COMMENT '优惠券模板ID',
  `relation_id` bigint NOT NULL COMMENT '关联的实体ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_relation_id` (`relation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券可用范围关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `creator_rank`
--

DROP TABLE IF EXISTS `creator_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `creator_rank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `score` decimal(10,2) NOT NULL COMMENT '鍒嗘暟',
  `rank` int NOT NULL COMMENT '鎺掑悕',
  `period_date` date NOT NULL COMMENT '鍛ㄦ湡鏃堕棿锛堝懆涓?棩鏈燂級',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_user_period` (`user_id`,`period_date`),
  KEY `idx_period` (`period_date`)
) ENGINE=InnoDB AUTO_INCREMENT=3865 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鍒涗綔鑰呮?鍗';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `emoji`
--

DROP TABLE IF EXISTS `emoji`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emoji` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
  `name` varchar(128) NOT NULL,
  `url` varchar(512) NOT NULL,
  `tiny` varchar(512) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `type` tinyint NOT NULL DEFAULT '1' COMMENT 'image 1 gif 2 ',
  `package_id` bigint DEFAULT NULL COMMENT '表情包id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `emoji_package`
--

DROP TABLE IF EXISTS `emoji_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emoji_package` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
  `name` varchar(128) NOT NULL COMMENT '表情包名称',
  `description` varchar(1024) DEFAULT NULL COMMENT '表情包描述',
  `cover_image` varchar(512) DEFAULT NULL COMMENT '表情包封面图片',
  `status` tinyint DEFAULT '1' COMMENT '1-正常，2-禁用',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='表情包表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `interaction_message`
--

DROP TABLE IF EXISTS `interaction_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interaction_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL COMMENT '涓氬姟鍞?竴ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `sender_avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送者头像',
  `sender_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送者名称',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `message_type` tinyint NOT NULL COMMENT '消息类型：1-点赞 2-评论 3-关注 4-提及 5-回复 6-收藏',
  `content` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息内容',
  `associate_content` bigint DEFAULT NULL COMMENT '关联内容ID',
  `post_id` bigint DEFAULT NULL COMMENT '关联帖子ID',
  `associate_title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联内容标题/快照',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '接收者是否删除：0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uuid` (`public_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_isdel_public_id` (`receiver_id`,`is_del`,`public_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互动消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

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
-- Table structure for table `member_package`
--

DROP TABLE IF EXISTS `member_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_package` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `public_id` bigint NOT NULL,
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员套餐表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payment_order`
--

DROP TABLE IF EXISTS `payment_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_payment_order_status_timeout_time_id` (`status`,`timeout_time`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_collect`
--

DROP TABLE IF EXISTS `post_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子收藏关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_image`
--

DROP TABLE IF EXISTS `post_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `image_url` varchar(512) NOT NULL,
  `width` smallint DEFAULT NULL,
  `height` smallint DEFAULT NULL,
  `size` int DEFAULT NULL,
  `sort_order` tinyint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1-正常，2-违规',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_post_id` (`post_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子图片关联表（逻辑外键）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_like`
--

DROP TABLE IF EXISTS `post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子点赞关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_rank`
--

DROP TABLE IF EXISTS `post_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_rank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
  `post_id` bigint NOT NULL COMMENT '甯栧瓙ID',
  `score` decimal(10,2) NOT NULL COMMENT '鐑?害鍊',
  `rank` int NOT NULL COMMENT '鎺掑悕',
  `period_date` date NOT NULL COMMENT '鍛ㄦ湡鏃堕棿',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_post_period` (`post_id`,`period_date`),
  KEY `idx_period` (`period_date`)
) ENGINE=InnoDB AUTO_INCREMENT=12006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐑?偣甯栧瓙姒滃崟';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_tag_rel`
--

DROP TABLE IF EXISTS `post_tag_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_tag_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `public_id` bigint NOT NULL,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`,`tag_id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `posts` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
  `creator_id` bigint NOT NULL COMMENT '发布者用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `type` int NOT NULL DEFAULT '1' COMMENT '帖子类型(1 文本 2 图片 3视频 4音频 5连接 6文件 7未知)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '文本内容',
  `media_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '媒体资源URL列表',
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
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_type` (`type`),
  KEY `idx_created_at` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `private_message`
--

DROP TABLE IF EXISTS `private_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `private_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_receiver` (`user_id`,`receiver_id`)
) ENGINE=InnoDB AUTO_INCREMENT=339412780337598465 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='私信消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `session`
--

DROP TABLE IF EXISTS `session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
  `type` int DEFAULT '1' COMMENT '会话类型',
  `last_message_id` bigint DEFAULT NULL COMMENT '最后一条消息id',
  `last_message_content` varchar(500) DEFAULT NULL COMMENT '最后一条消息内容',
  `last_sender_id` bigint DEFAULT NULL COMMENT '最后发送者id',
  `last_time` datetime DEFAULT NULL COMMENT '最后发送时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_message`
--

DROP TABLE IF EXISTS `system_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
  `from_user_id` bigint NOT NULL COMMENT '系统消息发送者',
  `content` varchar(2000) DEFAULT NULL COMMENT '内容(html)',
  `status` int DEFAULT '1' COMMENT '状态',
  `public_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `associate_user` bigint DEFAULT NULL COMMENT '系统消息关联的用户ID',
  `type` int DEFAULT '1' COMMENT '消息类型：1广播，2单独用户',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_message_image`
--

DROP TABLE IF EXISTS `system_message_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_message_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
  `message_id` bigint NOT NULL COMMENT '消息id',
  `image` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `status` varchar(50) DEFAULT NULL COMMENT '状态',
  `create_time` varchar(100) DEFAULT NULL COMMENT '创建时间',
  `update_time` varchar(100) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统消息图片关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `public_id` bigint NOT NULL,
  `tag_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名',
  `sort` int DEFAULT '0' COMMENT '权重排序',
  `use_count` int DEFAULT '0' COMMENT '使用次数',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0禁用, 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `uuid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`tag_name`),
  UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `topic_rank`
--

DROP TABLE IF EXISTS `topic_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic_rank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `public_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL COMMENT '璇濋?鏍囩?ID',
  `score` decimal(10,2) NOT NULL COMMENT '鐑?害/浣跨敤娆℃暟',
  `rank` int NOT NULL COMMENT '鎺掑悕',
  `period_date` date NOT NULL COMMENT '鍛ㄦ湡鏃堕棿',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_tag_period` (`tag_id`,`period_date`),
  KEY `idx_period` (`period_date`)
) ENGINE=InnoDB AUTO_INCREMENT=4084 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璇濋?姒滃崟';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_coupon`
--

DROP TABLE IF EXISTS `user_coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `public_id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '持有的用户ID',
  `coupon_id` bigint NOT NULL COMMENT '关联的优惠券模板ID',
  `status` int NOT NULL DEFAULT '1' COMMENT '使用状态: 0-已使用, 1-未使用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `used_time` timestamp NULL DEFAULT NULL COMMENT '核销使用时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `order_id` bigint DEFAULT NULL COMMENT '订单id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=220 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券实例映射表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_follow`
--

DROP TABLE IF EXISTS `user_follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `public_id` bigint NOT NULL,
  `follower_id` bigint NOT NULL COMMENT '关注者用户ID',
  `followee_id` bigint NOT NULL COMMENT '被关注者用户ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-正常关注，2-已取消，3-互相关注（可选）',
  `source` varchar(16) DEFAULT NULL COMMENT '关注来源（例如：推荐、搜索、扫码）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_followee` (`follower_id`,`followee_id`),
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_followee_id` (`followee_id`),
  KEY `idx_follower_status` (`follower_id`,`status`),
  KEY `idx_followee_status` (`followee_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

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
-- Table structure for table `user_session`
--

DROP TABLE IF EXISTS `user_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `public_id` bigint NOT NULL,
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
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户会话设置表（每人一份）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_setting`
--

DROP TABLE IF EXISTS `user_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `show_delPost` tinyint NOT NULL DEFAULT '1' COMMENT '是否显示删除的帖子',
  `Customization_recommend` tinyint NOT NULL DEFAULT '1' COMMENT '是否开启个性化推荐',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_public_id` (`public_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_sign_log`
--

DROP TABLE IF EXISTS `user_sign_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_sign_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑷??涓婚敭',
  `public_id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `sign_date` date NOT NULL COMMENT '绛惧埌鏃ユ湡锛堟牸寮忥細2026-07-17锛',
  `sign_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绛惧埌鍏蜂綋鏃堕棿鎴',
  `sign_source` tinyint DEFAULT '1' COMMENT '绛惧埌鏉ユ簮锛?-APP 2-H5 3-灏忕▼搴',
  `reward_points` int DEFAULT '0' COMMENT '鏈??绛惧埌鑾峰緱鐨勫熀纭?Н鍒',
  `continuous_days_snapshot` int DEFAULT '0' COMMENT '绛惧埌鏃剁殑杩炵画澶╂暟蹇?収锛堢敤浜庡巻鍙插?璐︼級',
  `extra` json DEFAULT NULL COMMENT '鎵╁睍瀛楁?锛堝瓨鏀捐ˉ绛惧崱ID銆佹椿鍔↖D绛夛級',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`sign_date`) COMMENT '鍞?竴绱㈠紩锛氱‘淇濅竴澶╁彧鑳界?涓??锛屽ぉ鐒堕槻閲',
  UNIQUE KEY `uk_public_id` (`public_id`),
  KEY `idx_sign_date` (`sign_date`) COMMENT '鏅??绱㈠紩锛氱敤浜庤繍钀ョ粺璁℃煇澶╂湁澶氬皯浜虹?鍒'
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛绛惧埌娴佹按琛';
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-29 15:39:23
