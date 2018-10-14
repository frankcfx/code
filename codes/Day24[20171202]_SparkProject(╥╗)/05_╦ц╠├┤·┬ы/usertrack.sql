# Host: localhost  (Version: 5.5.40)
# Date: 2017-03-16 21:56:50
# Generator: MySQL-Front 5.3  (Build 4.120)

/*!40101 SET NAMES utf8 */;
CREATE DATABASE `usertrack` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `usertrack`;

#
# Structure for table "city_info"
#

DROP TABLE IF EXISTS `city_info`;
CREATE TABLE `city_info` (
  `city_id` int(11) NOT NULL DEFAULT '0',
  `city_name` varchar(255) DEFAULT NULL COMMENT '城市名称',
  `province_name` varchar(255) DEFAULT NULL COMMENT '省份名称',
  `area` varchar(255) DEFAULT NULL COMMENT '地域名称',
  PRIMARY KEY (`city_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Data for table "city_info"
#

INSERT INTO `city_info` VALUES (0,'上海','上海市','华东'),(1,'北京','北京市','华北'),(2,'深圳','广东省','华南'),(3,'广州','广东省','华南'),(4,'南京','江苏省','华东'),(5,'杭州','浙江省','华东'),(6,'长沙','湖南省','华中'),(7,'南昌','江西省','华中'),(8,'张家界','湖南省','华中'),(9,'香港','香港','华南'),(10,'澳门','澳门','华南');

#
# Structure for table "tb_area_top10_product"
#

DROP TABLE IF EXISTS `tb_area_top10_product`;
CREATE TABLE `tb_area_top10_product` (
  `task_id` bigint(18) NOT NULL DEFAULT '0' COMMENT '任务id',
  `area` varchar(50) NOT NULL DEFAULT '' COMMENT '地域名称',
  `product_id` bigint(18) NOT NULL DEFAULT '0' COMMENT '商品ID',
  `area_level` varchar(10) DEFAULT NULL COMMENT '区域级别',
  `count` int(11) NOT NULL DEFAULT '0' COMMENT '被触发的次数',
  `city_infos` varchar(255) DEFAULT NULL COMMENT '城市地域信息',
  `product_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `product_type` varchar(255) DEFAULT NULL COMMENT '商品类型',
  PRIMARY KEY (`task_id`,`area`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Data for table "tb_area_top10_product"
#


#
# Structure for table "tb_black_users"
#

DROP TABLE IF EXISTS `tb_black_users`;
CREATE TABLE `tb_black_users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Data for table "tb_black_users"
#


#
# Structure for table "tb_task"
#

DROP TABLE IF EXISTS `tb_task`;
CREATE TABLE `tb_task` (
  `task_id` int(11) NOT NULL AUTO_INCREMENT,
  `task_name` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `finish_time` datetime DEFAULT NULL,
  `task_type` varchar(20) DEFAULT NULL,
  `task_status` varchar(20) DEFAULT NULL,
  `task_param` text,
  KEY `pk` (`task_id`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

#
# Data for table "tb_task"
#

/*!40000 ALTER TABLE `tb_task` DISABLE KEYS */;
INSERT INTO `tb_task` VALUES (1,'test01','2017-03-07 11:45:50','1899-12-29 00:00:00',NULL,'session','normal','{\"sex\":\"male\",\"startDate\":\"2017-03-01\",\"endDate\":\"2017-03-20\",\"professionals\":\"程序员\"}'),(2,'test02',NULL,NULL,NULL,'session','normal','{\"sex\":\"male\",\"startDate\":\"2017-03-01\",\"endDate\":\"2017-03-20\"}');
/*!40000 ALTER TABLE `tb_task` ENABLE KEYS */;

#
# Structure for table "tb_task_result_sample_session"
#

DROP TABLE IF EXISTS `tb_task_result_sample_session`;
CREATE TABLE `tb_task_result_sample_session` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(1) NOT NULL DEFAULT '0' COMMENT '任务id',
  `session_id` varchar(255) NOT NULL DEFAULT '' COMMENT '会话id',
  `day` varchar(20) NOT NULL DEFAULT '' COMMENT '日期',
  `hour` int(11) NOT NULL DEFAULT '0' COMMENT '小时数(24小时制)',
  `record` text COMMENT '会话记录',
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Data for table "tb_task_result_sample_session"
#


#
# Structure for table "tb_task_top10_category"
#

DROP TABLE IF EXISTS `tb_task_top10_category`;
CREATE TABLE `tb_task_top10_category` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(1) NOT NULL DEFAULT '0' COMMENT '任务id',
  `category_id` varchar(20) NOT NULL DEFAULT '' COMMENT '品类ID',
  `click_count` int(11) unsigned DEFAULT '0' COMMENT '点击次数',
  `order_count` int(11) unsigned DEFAULT '0' COMMENT '下单数量',
  `pay_count` int(11) unsigned DEFAULT '0' COMMENT '支付数量',
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Data for table "tb_task_top10_category"
#


#
# Structure for table "tb_task_top10_category_session"
#

DROP TABLE IF EXISTS `tb_task_top10_category_session`;
CREATE TABLE `tb_task_top10_category_session` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(1) unsigned NOT NULL DEFAULT '0' COMMENT '任务id',
  `category_id` varchar(20) NOT NULL DEFAULT '' COMMENT '品类id',
  `click_sessions` text COMMENT '点击操作对应的Top10会话信息',
  `order_sessions` text COMMENT '下单操作对应的Top10会话信息',
  `pay_sessions` text COMMENT '支付操作对应的Top10会话信息',
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Data for table "tb_task_top10_category_session"
#

