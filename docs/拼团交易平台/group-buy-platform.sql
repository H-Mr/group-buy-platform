-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: 111.228.51.8    Database: group_buy_market
-- ------------------------------------------------------
-- Server version	8.0.32

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

CREATE DATABASE IF NOT EXISTS `group_buy_market` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `group_buy_market`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


--
-- Table structure for table `crowd_tags`
--

DROP TABLE IF EXISTS `crowd_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crowd_tags` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `tag_id` varchar(32) NOT NULL COMMENT '人群ID',
  `tag_name` varchar(64) NOT NULL COMMENT '人群名称',
  `tag_desc` varchar(256) NOT NULL COMMENT '人群描述',
  `statistics` int NOT NULL COMMENT '人群标签统计量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tag_id` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人群标签';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crowd_tags`
--

LOCK TABLES `crowd_tags` WRITE;
/*!40000 ALTER TABLE `crowd_tags` DISABLE KEYS */;
INSERT INTO `crowd_tags` VALUES (1,'RQ_KJHKL98UU78H66554GFDV','潜在消费用户','潜在消费用户',11,'2024-12-28 12:53:28','2025-01-25 15:44:55');
/*!40000 ALTER TABLE `crowd_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crowd_tags_detail`
--

DROP TABLE IF EXISTS `crowd_tags_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crowd_tags_detail` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `tag_id` varchar(64) NOT NULL COMMENT '人群ID',
  `user_id` varchar(16) NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tag_user` (`tag_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人群标签明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crowd_tags_detail`
--

LOCK TABLES `crowd_tags_detail` WRITE;
/*!40000 ALTER TABLE `crowd_tags_detail` DISABLE KEYS */;
INSERT INTO `crowd_tags_detail` VALUES (4,'RQ_KJHKL98UU78H66554GFDV','xiaofuge','2024-12-28 14:42:30','2024-12-28 14:42:30'),(5,'RQ_KJHKL98UU78H66554GFDV','liergou','2024-12-28 14:42:30','2024-12-28 14:42:30'),(9,'RQ_KJHKL98UU78H66554GFDV','xfg01','2025-01-25 15:44:55','2025-01-25 15:44:55'),(10,'RQ_KJHKL98UU78H66554GFDV','xfg02','2025-01-25 15:44:55','2025-01-25 15:44:55'),(11,'RQ_KJHKL98UU78H66554GFDV','xfg03','2025-01-25 15:44:55','2025-01-25 15:44:55');
/*!40000 ALTER TABLE `crowd_tags_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crowd_tags_job`
--

DROP TABLE IF EXISTS `crowd_tags_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crowd_tags_job` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `tag_id` varchar(32) NOT NULL COMMENT '标签ID',
  `batch_id` varchar(8) NOT NULL COMMENT '批次ID',
  `tag_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '标签类型（参与量、消费金额）',
  `tag_rule` varchar(8) NOT NULL COMMENT '标签规则（限定类型 N次）',
  `stat_start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '统计数据，开始时间',
  `stat_end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '统计数据，结束时间',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态；0初始、1计划（进入执行阶段）、2重置、3完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_batch_id` (`batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人群标签任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crowd_tags_job`
--

LOCK TABLES `crowd_tags_job` WRITE;
/*!40000 ALTER TABLE `crowd_tags_job` DISABLE KEYS */;
INSERT INTO `crowd_tags_job` VALUES (1,'RQ_KJHKL98UU78H66554GFDV','10001',0,'100','2024-12-28 12:55:05','2024-12-28 12:55:05',0,'2024-12-28 12:55:05','2024-12-28 12:55:05');
/*!40000 ALTER TABLE `crowd_tags_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_buy_activity`
--

DROP TABLE IF EXISTS `group_buy_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_buy_activity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `activity_name` varchar(128) NOT NULL COMMENT '活动名称',
  `discount_id` varchar(8) NOT NULL COMMENT '折扣ID',
  `group_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '拼团方式（0自动成团、1达成目标拼团）',
  `take_limit_count` int NOT NULL DEFAULT '1' COMMENT '拼团次数限制',
  `target` int NOT NULL DEFAULT '1' COMMENT '拼团目标',
  `valid_time` int NOT NULL DEFAULT '15' COMMENT '拼团时长（分钟）',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '活动状态（0创建、1生效、2过期、3废弃）',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '活动开始时间',
  `end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '活动结束时间',
  `tag_id` varchar(32) DEFAULT NULL COMMENT '人群标签规则标识',
  `tag_scope` varchar(4) DEFAULT NULL COMMENT '人群标签规则范围（多选；1可见限制、2参与限制）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_activity_id` (`activity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团活动';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_buy_activity`
--

LOCK TABLES `group_buy_activity` WRITE;
/*!40000 ALTER TABLE `group_buy_activity` DISABLE KEYS */;
INSERT INTO `group_buy_activity` VALUES (1,100123,'测试活动','25120208',0,1,1,60,1,'2025-11-07 10:19:40','2026-12-07 10:19:40','RQ_KJHKL98UU78H66554GFDV','1,2','2024-12-07 10:19:40','2025-12-16 14:15:51');
/*!40000 ALTER TABLE `group_buy_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_buy_discount`
--

DROP TABLE IF EXISTS `group_buy_discount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_buy_discount` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `discount_id` varchar(8) NOT NULL COMMENT '折扣ID',
  `discount_name` varchar(64) NOT NULL COMMENT '折扣标题',
  `discount_desc` varchar(256) NOT NULL COMMENT '折扣描述',
  `discount_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '折扣类型（0:base、1:tag）',
  `market_plan` varchar(4) NOT NULL DEFAULT 'ZJ' COMMENT '营销优惠计划（ZJ:直减、MJ:满减、ZK:折扣、N元购）',
  `market_expr` varchar(32) NOT NULL COMMENT '营销优惠表达式',
  `tag_id` varchar(32) DEFAULT NULL COMMENT '人群标签，特定优惠限定',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_discount_id` (`discount_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_buy_discount`
--

LOCK TABLES `group_buy_discount` WRITE;
/*!40000 ALTER TABLE `group_buy_discount` DISABLE KEYS */;
INSERT INTO `group_buy_discount` VALUES (1,'25120207','直减优惠20元','直减优惠20元',1,'ZJ','20','RQ_KJHKL98UU78H66554GFDV','2024-12-07 10:20:15','2025-11-29 17:32:14'),(2,'25120208','满减优惠100-10元','满减优惠100-10元',1,'MJ','100,10','RQ_KJHKL98UU78H66554GFDV','2024-12-07 10:20:15','2025-11-29 17:32:14'),(4,'25120209','折扣优惠8折','折扣优惠8折',1,'ZK','0.8','RQ_KJHKL98UU78H66554GFDV','2024-12-07 10:20:15','2025-11-29 17:32:14'),(5,'25120210','N元购买优惠','N元购买优惠',1,'N','1.99','RQ_KJHKL98UU78H66554GFDV','2024-12-07 10:20:15','2025-11-29 17:32:14');
/*!40000 ALTER TABLE `group_buy_discount` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_buy_order`
--

DROP TABLE IF EXISTS `group_buy_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_buy_order` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `team_id` varchar(8) NOT NULL COMMENT '拼单组队ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `source` varchar(8) NOT NULL COMMENT '渠道',
  `channel` varchar(8) NOT NULL COMMENT '来源',
  `original_price` decimal(8,2) NOT NULL COMMENT '原始价格',
  `deduction_price` decimal(8,2) NOT NULL COMMENT '折扣金额',
  `pay_price` decimal(8,2) NOT NULL COMMENT '支付价格',
  `target_count` int NOT NULL COMMENT '目标数量',
  `complete_count` int NOT NULL COMMENT '完成数量',
  `lock_count` int NOT NULL COMMENT '锁单数量',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态（0-拼单中、1-完成、2-失败）',
  `valid_start_time` datetime NOT NULL COMMENT '拼团开始时间',
  `valid_end_time` datetime NOT NULL COMMENT '拼团结束时间',
  `notify_url` varchar(512) NOT NULL COMMENT '回调地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_team_id` (`team_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_buy_order`
--

LOCK TABLES `group_buy_order` WRITE;
/*!40000 ALTER TABLE `group_buy_order` DISABLE KEYS */;
INSERT INTO `group_buy_order` VALUES (17,'44945994',100123,'s01','c01',100.00,0.00,100.00,1,0,1,0,'2025-12-16 16:12:52','2025-12-16 17:12:52','http://127.0.0.1:8080/api/v1/alipay/group_buy_notify','2025-12-16 16:12:51','2025-12-16 16:12:51'),(18,'14945384',100123,'s01','c01',100.00,0.00,100.00,1,0,1,0,'2025-12-16 16:34:42','2025-12-16 17:34:42','http://127.0.0.1:8080/api/v1/alipay/group_buy_notify','2025-12-16 16:34:41','2025-12-16 16:34:41'),(19,'43071702',100123,'s01','c01',100.00,0.00,100.00,1,0,1,0,'2025-12-16 16:38:30','2025-12-16 17:38:30','http://127.0.0.1:8080/api/v1/alipay/group_buy_notify','2025-12-16 16:38:29','2025-12-16 16:38:29'),(20,'44186934',100123,'s01','c01',100.00,0.00,100.00,1,1,1,1,'2025-12-16 16:46:30','2025-12-16 17:46:30','http://127.0.0.1:8080/api/v1/alipay/group_buy_notify','2025-12-16 16:46:29','2025-12-16 16:52:43'),(21,'07242897',100123,'s01','c01',100.00,0.00,100.00,1,1,1,1,'2025-12-16 17:01:21','2025-12-16 18:01:21','http://127.0.0.1:8080/api/v1/alipay/group_buy_notify','2025-12-16 17:01:20','2025-12-16 17:02:25');
/*!40000 ALTER TABLE `group_buy_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_buy_order_list`
--

DROP TABLE IF EXISTS `group_buy_order_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_buy_order_list` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `team_id` varchar(8) NOT NULL COMMENT '拼单组队ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `goods_id` varchar(16) NOT NULL COMMENT '商品ID',
  `source` varchar(8) NOT NULL COMMENT '渠道',
  `channel` varchar(8) NOT NULL COMMENT '来源',
  `original_price` decimal(8,2) NOT NULL COMMENT '原始价格',
  `deduction_price` decimal(8,2) NOT NULL COMMENT '折扣金额',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态；0初始锁定、1消费完成、2用户退单',
  `out_trade_no` varchar(16) NOT NULL COMMENT '外部交易单号-确保外部调用唯一幂等',
  `out_trade_time` datetime DEFAULT NULL COMMENT '外部交易时间',
  `biz_id` varchar(64) NOT NULL COMMENT '业务唯一ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_buy_order_list`
--

LOCK TABLES `group_buy_order_list` WRITE;
/*!40000 ALTER TABLE `group_buy_order_list` DISABLE KEYS */;
INSERT INTO `group_buy_order_list` VALUES (60,'7736117232733921470425906184967298691211750310624021055982293696','07242897','961425614984',100123,'2025-11-07 10:19:40','2026-12-07 10:19:40','9890001','s01','c01',100.00,0.00,1,'6236838367757155','2025-12-16 17:02:24','800ce42a66f314feeb67e580d21265a57e4e24dcc538fa0e04a144b8968c4ff2','2025-12-16 17:01:20','2025-12-16 17:02:25');
/*!40000 ALTER TABLE `group_buy_order_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notify_task`
--

DROP TABLE IF EXISTS `notify_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notify_task` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `team_id` varchar(8) NOT NULL COMMENT '拼单组队ID',
  `notify_url` varchar(128) NOT NULL COMMENT '回调接口',
  `notify_count` int NOT NULL COMMENT '回调次数',
  `notify_status` tinyint(1) NOT NULL COMMENT '回调状态【0初始、1完成、2重试、3失败】',
  `parameter_json` varchar(256) NOT NULL COMMENT '参数对象',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notify_task`
--

LOCK TABLES `notify_task` WRITE;
/*!40000 ALTER TABLE `notify_task` DISABLE KEYS */;
INSERT INTO `notify_task` VALUES (5,100123,'44186934','http://127.0.0.1:8080/api/v1/alipay/group_buy_notify',1,1,'{\"userIdList\":[\"7736117232733921470425906184967298691211750310624021055982293695\"],\"teamId\":\"44186934\",\"7736117232733921470425906184967298691211750310624021055982293695\":[\"9890001\"]}','2025-12-16 16:52:43','2025-12-16 16:56:22'),(6,100123,'07242897','http://127.0.0.1:8080/api/v1/alipay/group_buy_notify',1,1,'{\"userIdList\":[\"7736117232733921470425906184967298691211750310624021055982293696\"],\"teamId\":\"07242897\",\"7736117232733921470425906184967298691211750310624021055982293696\":[\"9890001\"]}','2025-12-16 17:02:26','2025-12-16 17:02:26');
/*!40000 ALTER TABLE `notify_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pay_order`
--

DROP TABLE IF EXISTS `pay_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pay_order` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `product_id` varchar(16) NOT NULL COMMENT '商品ID',
  `product_name` varchar(64) NOT NULL COMMENT '商品名称',
  `order_id` varchar(16) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `total_amount` decimal(8,2) unsigned DEFAULT NULL COMMENT '订单金额',
  `status` varchar(32) NOT NULL COMMENT '订单状态；create-创建完成、pay_wait-等待支付、pay_success-支付成功、deal_done-交易完成、close-订单关单',
  `pay_url` varchar(2014) DEFAULT NULL COMMENT '支付信息',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `market_type` tinyint(1) DEFAULT NULL COMMENT '营销类型；0无营销、1拼团营销',
  `market_deduction_amount` decimal(8,2) DEFAULT NULL COMMENT '营销金额；优惠金额',
  `pay_amount` decimal(8,2) NOT NULL COMMENT '支付金额',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id_product_id` (`user_id`,`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pay_order`
--

LOCK TABLES `pay_order` WRITE;
/*!40000 ALTER TABLE `pay_order` DISABLE KEYS */;
INSERT INTO `pay_order` VALUES (17,'7736117232733921470425906184967298691211750310624021055982293696','9890001','《手写MyBatis：渐进式源码实践》','6236838367757155','2025-12-16 17:01:21',100.00,'PAY_SUCCESS','<form name=\"punchout_form\" method=\"post\" action=\"https://openapi-sandbox.dl.alipaydev.com/gateway.do?charset=utf-8&method=alipay.trade.page.pay&sign=UjXAT%2BUt3%2BrzJtNj0JdcwgMurP%2F7zrMUPp113M5onZm14jBcpkncNHwFoFeicTXiQMrfCfDOi0OrLwz2%2FHII3UO2rgz7XGP4PvFLdSB3UyMDCaWzDTs1eC2KFckA6qGKJLGwiF%2FdPBPu%2BQnNOZUs6ROwZhPGUkr5VDtFCCY8u2TkICQ5zCOqq%2FJUUTq7lN3lYL62hzkIYTZpprGykmXod%2F1YIHzEBeN5PKAK5XwHNGWu%2B8JGdYu7UCHMJ1zsGOSPpVr6gcc7pE1VNJvW7VejixYFqADK74NXgfWKTEgV%2FSnzitDIrsISS8gaBaP3AUm6TjqF412L227N%2BBhlHK7%2FjA%3D%3D&return_url=www.baidu.com&notify_url=http%3A%2F%2Fp7aecbf5.natappfree.cc%2Fapi%2Fv1%2Falipay%2Falipay_notify_url&version=1.0&app_id=9021000157621491&sign_type=RSA2&timestamp=2025-12-16+17%3A01%3A21&alipay_sdk=alipay-sdk-java-4.38.157.ALL&format=json\">\n<input type=\"hidden\" name=\"biz_content\" value=\"{&quot;out_trade_no&quot;:&quot;6236838367757155&quot;,&quot;total_amount&quot;:&quot;100.00&quot;,&quot;subject&quot;:&quot;《手写MyBatis：渐进式源码实践》&quot;,&quot;timeout_express&quot;:&quot;2h&quot;,&quot;product_code&quot;:&quot;FAST_INSTANT_TRADE_PAY&quot;}\">\n<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >\n</form>\n<script>document.forms[0].submit();</script>','2025-12-16 17:02:24',1,0.00,100.00,'2025-12-16 17:01:20','2025-12-16 17:02:25'),(18,'7736117232733921470425906184967298691211750310624021055982293696','9890001','《手写MyBatis：渐进式源码实践》','3341705777550754','2025-12-16 17:04:21',100.00,'CREATE',NULL,NULL,1,0.00,100.00,'2025-12-16 17:04:20','2025-12-16 17:04:20');
/*!40000 ALTER TABLE `pay_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sc_sku_activity`
--

DROP TABLE IF EXISTS `sc_sku_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sc_sku_activity` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `source` varchar(8) NOT NULL COMMENT '渠道',
  `channel` varchar(8) NOT NULL COMMENT '来源',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `goods_id` varchar(16) NOT NULL COMMENT '商品ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_sc_goodsid` (`source`,`channel`,`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='渠道商品活动配置关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sc_sku_activity`
--

LOCK TABLES `sc_sku_activity` WRITE;
/*!40000 ALTER TABLE `sc_sku_activity` DISABLE KEYS */;
INSERT INTO `sc_sku_activity` VALUES (1,'s01','c01',100123,'9890001','2025-01-01 13:15:54','2025-01-01 13:15:54');
/*!40000 ALTER TABLE `sc_sku_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sku`
--

DROP TABLE IF EXISTS `sku`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sku` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `source` varchar(8) NOT NULL COMMENT '渠道',
  `channel` varchar(8) NOT NULL COMMENT '来源',
  `goods_id` varchar(16) NOT NULL COMMENT '商品ID',
  `goods_name` varchar(128) NOT NULL COMMENT '商品名称',
  `original_price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sku`
--

LOCK TABLES `sku` WRITE;
/*!40000 ALTER TABLE `sku` DISABLE KEYS */;
INSERT INTO `sku` VALUES (1,'s01','c01','9890001','《手写MyBatis：渐进式源码实践》',100.00,'2024-12-21 11:10:06','2024-12-21 11:10:06');
/*!40000 ALTER TABLE `sku` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(64) DEFAULT NULL COMMENT '系统内部用户ID (随机生成)',
  `openid` varchar(64) NOT NULL COMMENT '微信OpenID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_openid` (`openid`),
  UNIQUE KEY `uq_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'7736117232733921470425906184967298691211750310624021055982293696','oL2TZ2AwQ_YZpqnP8rNTMK_ao96A','2025-12-16 14:13:40','2025-12-16 14:13:40');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'group_buy_market'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-16 20:09:34
