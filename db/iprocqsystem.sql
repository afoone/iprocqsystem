-- MySQL dump 10.13  Distrib 5.5.61, for linux-glibc2.12 (x86_64)
--
-- Host: localhost    Database: qsystem
-- ------------------------------------------------------
-- Server version	5.5.61

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
-- Table structure for table `advance`
--

DROP TABLE IF EXISTS `advance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `advance` (
  `id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL COMMENT 'Ð£ÑÐ»ÑƒÐ³Ð° Ð¿Ñ€ÐµÐ´Ð²Ð°Ñ€Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸',
  `advance_time` datetime NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð¿Ñ€ÐµÐ´Ð²Ð°Ñ€Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸',
  `priority` int(11) NOT NULL DEFAULT '2' COMMENT 'ÐŸÑ€Ð¸Ð¾Ñ€Ð¸Ñ‚ÐµÑ‚ Ð·Ð°Ñ€Ð°Ð½ÐµÐµ Ð·Ð°Ð¿Ð¸ÑÐ°Ð²ÑˆÐµÐ³Ð¾ÑÑ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð°.',
  `clients_authorization_id` bigint(20) DEFAULT NULL COMMENT 'ÐžÐ¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¾ ÐµÑÐ»Ð¸ ÐºÐ»Ð¸ÐµÐ½Ñ‚ Ð°Ð²Ñ‚Ð¾Ñ€Ð¸Ð·Ð¾Ð²Ð°Ð»ÑÑ',
  `input_data` varchar(150) DEFAULT NULL COMMENT 'Ð’Ð²ÐµÐ´ÐµÐ½Ñ‹Ðµ Ð¿Ñ€Ð¸ Ð¿Ñ€ÐµÐ´Ð²Ð°Ñ€Ð¸Ð»Ð¾Ð²ÐºÐµ Ð´Ð°Ð½Ð½Ñ‹Ðµ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð° ÐµÑÐ»Ð¸ ÑƒÑÐ»ÑƒÐ³Ð° ÑÑ‚Ð¾Ð³Ð¾ Ñ‚Ñ€ÐµÐ±ÑƒÐµÑ‚',
  `comments` varchar(345) DEFAULT '' COMMENT 'ÐšÐ¾Ð¼ÐµÐ½Ñ‚Ð°Ñ€Ð¸Ð¸ Ð¿Ñ€Ð¸ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ð¿Ñ€ÐµÐ´Ð²Ð°Ñ€Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾ Ð¾Ð¿ÐµÑ€Ð°Ñ‚Ð¾Ñ€Ð¾Ð¼ ÑƒÐ´Ð°Ð»ÐµÐ½Ð½Ð¾',
  PRIMARY KEY (`id`),
  KEY `idx_scenario_services` (`service_id`),
  KEY `idx_advance_clients_authorization` (`clients_authorization_id`),
  CONSTRAINT `fk_advance_clients_authorization` FOREIGN KEY (`clients_authorization_id`) REFERENCES `clients_authorization` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_scenario_services` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð¢Ð°Ð±Ð»Ð¸Ñ†Ð° Ð¿Ñ€ÐµÐ´Ð²Ð°Ñ€Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advance`
--

LOCK TABLES `advance` WRITE;
/*!40000 ALTER TABLE `advance` DISABLE KEYS */;
INSERT INTO `advance` VALUES (11962,2,'2018-09-06 11:00:00',2,NULL,NULL,''),(27393,2,'2018-09-29 13:00:00',2,NULL,NULL,''),(92771,2,'2018-09-29 11:00:00',2,NULL,NULL,''),(274227,1533664063269,'2018-09-06 14:00:00',2,NULL,NULL,''),(726004,2,'2018-08-10 12:00:00',2,NULL,NULL,'');
/*!40000 ALTER TABLE `advance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `break`
--

DROP TABLE IF EXISTS `break`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `break` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `breaks_id` bigint(20) DEFAULT NULL,
  `from_time` time NOT NULL,
  `to_time` time NOT NULL,
  `hint` varchar(1024) DEFAULT NULL COMMENT 'Ð¢ÐµÐºÑÑ‚ Html Ð¿Ð¾Ð´ÑÐºÐ°Ð·ÐºÐ¸ Ð¿Ñ€Ð¸ Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð¸Ð¸ Ñ‚Ð°Ð»Ð¾Ð½Ð° Ð²Ð¾ Ð²Ñ€ÐµÐ¼Ñ Ð¿ÐµÑ€ÐµÑ€Ñ‹Ð²Ð°',
  PRIMARY KEY (`id`),
  KEY `idx_break_breaks1` (`breaks_id`),
  CONSTRAINT `fk_break_breaks1` FOREIGN KEY (`breaks_id`) REFERENCES `breaks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='ÐŸÐµÑ€ÐµÑ€Ñ‹Ð²Ñ‹ Ð² Ñ€Ð°Ð±Ð¾Ñ‚Ðµ Ð´Ð»Ñ Ð¿Ñ€ÐµÐ´Ð²Ð°Ñ€Ð¸Ð»Ð¾Ð²ÐºÐ¸';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `break`
--

LOCK TABLES `break` WRITE;
/*!40000 ALTER TABLE `break` DISABLE KEYS */;
/*!40000 ALTER TABLE `break` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `breaks`
--

DROP TABLE IF EXISTS `breaks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `breaks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(245) NOT NULL DEFAULT 'Unknown',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð¡Ð¿Ð¸ÑÐºÐ¸ Ð½Ð°Ð±Ð¾Ñ€Ð¾Ð² Ð¿ÐµÑ€ÐµÑ€Ñ‹Ð²Ð¾Ð² Ð´Ð»Ñ Ð¿Ñ€Ð¸Ð²ÑÐ·ÐºÐ¸ Ðº Ð´Ð½ÐµÐ²Ð½Ð¾Ð¼Ñƒ Ñ€Ð°ÑÐ¿Ð¸ÑÐ°Ð½Ð¸ÑŽ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `breaks`
--

LOCK TABLES `breaks` WRITE;
/*!40000 ALTER TABLE `breaks` DISABLE KEYS */;
/*!40000 ALTER TABLE `breaks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `calendar`
--

DROP TABLE IF EXISTS `calendar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `calendar` (
  `id` bigint(20) NOT NULL,
  `name` varchar(45) NOT NULL DEFAULT '' COMMENT 'ÐÐ°Ð·Ð²Ð°Ð½Ð¸Ðµ ÐºÐ°Ð»ÐµÐ½Ð´Ð°Ñ€Ñ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='ÐšÐ°Ð»ÐµÐ½Ð´Ð°Ñ€ÑŒ ÑƒÑÐ»ÑƒÐ³ Ð½Ð° Ð³Ð¾Ð´';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `calendar`
--

LOCK TABLES `calendar` WRITE;
/*!40000 ALTER TABLE `calendar` DISABLE KEYS */;
INSERT INTO `calendar` VALUES (1,'Common calendar');
/*!40000 ALTER TABLE `calendar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `calendar_out_days`
--

DROP TABLE IF EXISTS `calendar_out_days`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `calendar_out_days` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `out_day` date NOT NULL COMMENT 'Ð”Ð°Ñ‚Ð° Ð½ÐµÑ€Ð°Ð±Ð¾Ñ‚Ñ‹. Ð’Ð°Ð¶ÐµÐ½ Ð¼ÐµÑÑÑ† Ð¸ Ð´ÐµÐ½ÑŒ',
  `calendar_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_calendar_out_days_calendar` (`calendar_id`),
  CONSTRAINT `fk_calendar_out_days_calendar` FOREIGN KEY (`calendar_id`) REFERENCES `calendar` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COMMENT='Ð”Ð½Ð¸ Ð½ÐµÑ€Ð°Ð±Ð¾Ñ‚Ñ‹ ÑƒÑÐ»ÑƒÐ³';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `calendar_out_days`
--

LOCK TABLES `calendar_out_days` WRITE;
/*!40000 ALTER TABLE `calendar_out_days` DISABLE KEYS */;
INSERT INTO `calendar_out_days` VALUES (1,'2010-01-01',1);
/*!40000 ALTER TABLE `calendar_out_days` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clients`
--

DROP TABLE IF EXISTS `clients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clients` (
  `id` bigint(20) NOT NULL COMMENT 'ÐŸÐµÑ€Ð²Ð¸Ñ‡Ð½Ñ‹Ð¹ ÐºÐ»ÑŽÑ‡.\nÐ’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ! Ð’ÑÑ‚Ð°Ð²Ð»ÑÑ‚ÑŒ Ð¸ Ð°Ð¿Ð´ÐµÐ¹Ñ‚Ð¸Ñ‚ÑŒ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¾ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸ÑŽ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð¾Ð¼.',
  `service_id` bigint(20) NOT NULL COMMENT 'Ð£ÑÐ»ÑƒÐ³Ð°, Ðº ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ð¹  Ð¿Ñ€Ð¸ÑˆÐµÐ» Ð¿ÐµÑ€Ð²Ð¾Ð½Ð°Ñ‡Ð°Ð»ÑŒÐ½Ð¾ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€.  Ð’ÑÐ¿Ð¾Ð¼Ð¾Ð³Ð°Ñ‚ÐµÐ»ÑŒÐ½Ð¾Ðµ Ð¿Ð¾Ð»Ðµ.\nÐ’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ! Ð’ÑÑ‚Ð°Ð²Ð»ÑÑ‚ÑŒ Ð¸ Ð°Ð¿Ð´ÐµÐ¹Ñ‚Ð¸Ñ‚ÑŒ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¾ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸ÑŽ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð¾Ð¼.',
  `user_id` bigint(20) NOT NULL COMMENT ' Ð’ÑÐ¿Ð¾Ð¼Ð¾Ð³Ð°Ñ‚ÐµÐ»ÑŒÐ½Ð¾Ðµ Ð¿Ð¾Ð»Ðµ.\nÐ’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ! Ð’ÑÑ‚Ð°Ð²Ð»ÑÑ‚ÑŒ Ð¸ Ð°Ð¿Ð´ÐµÐ¹Ñ‚Ð¸Ñ‚ÑŒ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¾ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸ÑŽ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð¾Ð¼.',
  `service_prefix` varchar(45) NOT NULL COMMENT 'ÐŸÑ€ÐµÑ„Ð¸ÐºÑ Ð½Ð¾Ð¼ÐµÑ€Ð° ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð°. Ð˜Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¾Ð½Ð½Ð¾Ðµ Ð¿Ð¾Ð»Ðµ.\nÐ’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ! Ð’ÑÑ‚Ð°Ð²Ð»ÑÑ‚ÑŒ Ð¸ Ð°Ð¿Ð´ÐµÐ¹Ñ‚Ð¸Ñ‚ÑŒ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¾ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸ÑŽ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð¾Ð¼.',
  `number` int(11) NOT NULL COMMENT 'ÐÐ¾Ð¼ÐµÑ€ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð° Ð±ÐµÐ· Ð¿Ñ€ÐµÑ„Ð¸ÐºÑÐ°. Ð˜Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¾Ð½Ð½Ð¾Ðµ Ð¿Ð¾Ð»Ðµ.\nÐ’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ! Ð’ÑÑ‚Ð°Ð²Ð»ÑÑ‚ÑŒ Ð¸ Ð°Ð¿Ð´ÐµÐ¹Ñ‚Ð¸Ñ‚ÑŒ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¾ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸ÑŽ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð¾Ð¼.',
  `stand_time` datetime NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð¿Ð¾ÑÑ‚Ð°Ð½Ð¾Ð²ÐºÐ¸ Ð² Ð¾Ñ‡ÐµÑ€ÐµÐ´ÑŒ. Ð˜Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¾Ð½Ð½Ð¾Ðµ Ð¿Ð¾Ð»Ðµ.\nÐ’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ! Ð’ÑÑ‚Ð°Ð²Ð»ÑÑ‚ÑŒ Ð¸ Ð°Ð¿Ð´ÐµÐ¹Ñ‚Ð¸Ñ‚ÑŒ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¾ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸ÑŽ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð¾Ð¼.',
  `start_time` datetime NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð½Ð°Ñ‡Ð°Ð»Ð° Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚ÐºÐ¸ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð° Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»ÐµÐ¼. Ð’ÑÐ¿Ð¾Ð¼Ð¾Ð³Ð°Ñ‚ÐµÐ»ÑŒÐ½Ð¾Ðµ Ð¿Ð¾Ð»Ðµ.\nÐ’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ! Ð’ÑÑ‚Ð°Ð²Ð»ÑÑ‚ÑŒ Ð¸ Ð°Ð¿Ð´ÐµÐ¹Ñ‚Ð¸Ñ‚ÑŒ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¾ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸ÑŽ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð¾Ð¼.',
  `finish_time` datetime NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸Ñ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ñ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð¼ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»ÐµÐ¼. Ð˜Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¾Ð½Ð½Ð¾Ðµ Ð¿Ð¾Ð»Ðµ.\nÐ’Ð½Ð¸Ð¼Ð°Ð½Ð¸Ðµ! Ð’ÑÑ‚Ð°Ð²Ð»ÑÑ‚ÑŒ Ð¸ Ð°Ð¿Ð´ÐµÐ¹Ñ‚Ð¸Ñ‚ÑŒ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ð¿Ð¾ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸ÑŽ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð¾Ð¼.',
  `clients_authorization_id` bigint(20) DEFAULT NULL COMMENT 'ÐžÐ¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¾ ÐµÑÐ»Ð¸ ÐºÐ»Ð¸ÐµÐ½Ñ‚ Ð°Ð²Ñ‚Ð¾Ñ€Ð¸Ð·Ð¾Ð²Ð°Ð»ÑÑ',
  `result_id` bigint(20) DEFAULT NULL COMMENT 'Ð•ÑÐ»Ð¸ Ð²Ñ‹Ð±Ñ€Ð°Ð»Ð¸ Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹',
  `input_data` varchar(150) NOT NULL DEFAULT '' COMMENT 'Ð’Ð²ÐµÐ´ÐµÐ½Ð½Ñ‹Ðµ Ð´Ð°Ð½Ð½Ñ‹Ðµ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»ÐµÐ¼',
  `state_in` int(11) NOT NULL DEFAULT '0' COMMENT 'ÐºÐ»Ð¸ÐµÐ½Ñ‚ Ð¿ÐµÑ€ÐµÑˆÐµÐ» Ð² ÑÑ‚Ð¾ ÑÐ¾ÑÑ‚Ð¾ÑÐ½Ð¸Ðµ.',
  PRIMARY KEY (`id`),
  KEY `idx_Ñlients_service_id_services_id` (`service_id`),
  KEY `idx_Ñlients_user_id_users_id` (`user_id`),
  KEY `idx_clients_clients_authorization` (`clients_authorization_id`),
  KEY `idx_clients_results` (`result_id`),
  CONSTRAINT `fk_Ñlients_service_id_services_id` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_Ñlients_user_id_users_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_clients_clients_authorization` FOREIGN KEY (`clients_authorization_id`) REFERENCES `clients_authorization` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  CONSTRAINT `fk_clients_results` FOREIGN KEY (`result_id`) REFERENCES `results` (`id`) ON DELETE SET NULL ON UPDATE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð¢Ð°Ð±Ð»Ð¸Ñ†Ð° Ñ€ÐµÐ³Ð¸ÑÑ‚Ñ€Ð°Ñ†Ð¸Ð¸ ÑÑ‚Ð°Ñ‚Ð¸ÑÑ‚Ð¸Ñ‡ÐµÑÐºÐ¸Ñ… ÑÐ¾Ð±Ñ‹Ñ‚Ð¸Ð¹ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð².';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clients`
--

LOCK TABLES `clients` WRITE;
/*!40000 ALTER TABLE `clients` DISABLE KEYS */;
INSERT INTO `clients` VALUES (1536142918134,2,2,'A',1,'2018-09-05 12:21:58','2018-09-05 17:13:18','2018-09-05 17:13:29',NULL,NULL,'',10),(1536147978477,2,2,'A',2,'2018-09-05 13:46:18','2018-09-05 17:13:43','2018-09-05 17:13:43',NULL,NULL,'',0),(1536149524342,2,2,'A',3,'2018-09-05 17:15:18','2018-09-05 17:38:22','2018-09-05 17:38:39',NULL,NULL,'',10),(1536159790062,1533664063269,2,'BOL',4,'2018-09-05 17:03:10','2018-09-05 17:15:12','2018-09-05 17:15:14',NULL,NULL,'',10),(1536161263513,1533664063269,2,'BOL',5,'2018-09-05 17:27:43','2018-09-05 17:38:43','2018-09-05 18:53:19',NULL,NULL,'',10),(1536161276145,1533664063269,2,'BOL',6,'2018-09-05 17:27:56','2018-09-05 18:53:28','2018-09-05 18:53:29',NULL,NULL,'',10),(1536166031923,1533664063269,2,'BOL',7,'2018-09-05 18:47:11','2018-09-05 18:53:40','2018-09-05 18:53:42',NULL,NULL,'',10),(1536166311702,1533664063269,2,'BOL',8,'2018-09-05 18:51:51','2018-09-05 18:54:20','2018-09-05 18:54:24',NULL,NULL,'',10),(1536166440266,1533664063269,2,'BOL',9,'2018-09-05 18:54:00','2018-09-05 18:54:30','2018-09-05 18:54:31',NULL,NULL,'',10),(1536736712105,1533664063269,2,'BOL',1,'2018-09-12 09:18:32','2018-09-12 09:56:37','2018-09-12 09:56:39',NULL,NULL,'',10),(1536738318804,1533664063269,2,'BOL',2,'2018-09-12 09:45:18','2018-09-12 10:21:14','2018-09-12 10:21:15',NULL,NULL,'',10),(1536739750099,2,2,'A',1,'2018-09-12 10:09:10','2018-09-12 11:45:05','2018-09-12 11:45:07',NULL,NULL,'',10),(1536745391125,2,2,'A',2,'2018-09-12 11:43:11','2018-09-12 13:06:32','2018-09-12 13:06:34',NULL,NULL,'',10),(1536746593964,2,2,'A',1,'2018-09-12 12:03:13','2018-09-12 13:06:57','2018-09-12 13:06:59',NULL,NULL,'',10),(1536746948347,2,2,'A',2,'2018-09-12 12:09:08','2018-09-12 13:07:27','2018-09-12 13:07:29',NULL,NULL,'',10),(1536749217521,2,2,'A',3,'2018-09-12 12:46:57','2018-09-12 13:09:34','2018-09-12 13:09:36',NULL,NULL,'',10),(1536750494272,2,2,'A',4,'2018-09-12 13:08:14','2018-09-12 13:16:45','2018-09-12 13:16:51',NULL,NULL,'',10),(1536750500365,2,2,'A',5,'2018-09-12 13:08:20','2018-09-12 13:22:30','2018-09-12 13:22:33',NULL,NULL,'',10),(1536750506243,2,2,'A',6,'2018-09-12 13:08:26','2018-09-12 13:22:44','2018-09-12 13:22:45',NULL,NULL,'',10),(1536750528187,2,2,'A',7,'2018-09-12 13:08:48','2018-09-12 13:22:55','2018-09-12 13:22:59',NULL,NULL,'',10),(1536750536146,2,2,'A',8,'2018-09-12 13:08:56','2018-09-12 13:24:11','2018-09-12 13:24:13',NULL,NULL,'',10),(1536750543858,2,2,'A',9,'2018-09-12 13:09:03','2018-09-12 13:24:26','2018-09-12 13:24:28',NULL,NULL,'',10),(1536750551589,2,2,'A',10,'2018-09-12 13:09:11','2018-09-12 13:24:48','2018-09-12 13:24:50',NULL,NULL,'',10),(1538121456917,2,2,'A',1,'2018-09-28 09:57:36','2018-09-28 10:02:35','2018-09-28 10:03:09',NULL,NULL,'',11),(1538121826430,2,2,'A',2,'2018-09-28 10:03:46','2018-09-28 10:04:19','2018-09-28 10:05:27',NULL,NULL,'',11);
/*!40000 ALTER TABLE `clients` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER insert_to_statistic 
    AFTER INSERT ON clients
    FOR EACH ROW
BEGIN
    SET @finish_start= TIMEDIFF(NEW.finish_time, NEW.start_time);
    SET @start_starnd = TIMEDIFF(NEW.start_time, NEW.stand_time);
    INSERT
        INTO statistic(state_in, results_id, user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period) 
    VALUES
        (NEW.state_in, NEW.result_id, NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time, 
        round(
                (HOUR(@finish_start) * 60 * 60 +
                 MINUTE(@finish_start) * 60 +
                 SECOND(@finish_start) + 59)/60),
        round(
                (HOUR(@start_starnd) * 60 * 60 +
                MINUTE(@start_starnd) * 60 +
                SECOND(@start_starnd) + 59)/60)  
        );
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER update_to_statistic
    AFTER UPDATE ON clients
    FOR EACH ROW
BEGIN
    SET @finish_start= TIMEDIFF(NEW.finish_time, NEW.start_time);
    SET @start_starnd = TIMEDIFF(NEW.start_time, NEW.stand_time);
    INSERT
        INTO statistic(state_in, results_id, user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period) 
    VALUES
        (NEW.state_in, NEW.result_id, NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time, 
        round(
                (HOUR(@finish_start) * 60 * 60 +
                 MINUTE(@finish_start) * 60 +
                 SECOND(@finish_start) + 59)/60),
        round(
                (HOUR(@start_starnd) * 60 * 60 +
                MINUTE(@start_starnd) * 60 +
                SECOND(@start_starnd) + 59)/60)  
        );
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `clients_authorization`
--

DROP TABLE IF EXISTS `clients_authorization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clients_authorization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `auth_id` varchar(128) DEFAULT NULL COMMENT 'Ð•ÑÐ»Ð¸ ÐµÑÑ‚ÑŒ ÑÑ‚Ñ€Ð¾ÐºÐ¾Ð²Ñ‹Ð¹ Ð¸Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€, Ñ‚Ð¾ Ð¼Ð¾Ð¶Ð½Ð¾ Ð¸ÑÐ¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÑŒ',
  `name` varchar(45) NOT NULL DEFAULT '' COMMENT 'Ð˜Ð¼Ñ',
  `surname` varchar(45) NOT NULL DEFAULT '' COMMENT 'Ð¤Ð°Ð¼Ð¸Ð»Ð¸Ðµ',
  `otchestvo` varchar(45) NOT NULL DEFAULT '' COMMENT 'ÐžÑ‚Ñ‡ÐµÑÑ‚Ð²Ð¾, Ð¸Ð½Ð¾Ð³Ð´Ð° Ð¼Ð¾Ð¶ÐµÑ‚ Ð¾Ñ‚ÑÑƒÑ‚ÑÑ‚Ð²Ð¾Ð²Ð°Ñ‚ÑŒ.',
  `birthday` date DEFAULT NULL COMMENT 'Ð”Ð°Ñ‚Ð° Ñ€Ð¾Ð¶Ð´ÐµÐ½Ð¸Ñ',
  `streets_id` bigint(20) DEFAULT NULL COMMENT 'Ð¡Ð²ÑÐ·ÑŒ ÑÐ¾ ÑÐ»Ð¾Ð²Ð°Ñ€ÐµÐ¼ ÑƒÐ»Ð¸Ñ†. ÐŸÑ€Ð¾Ð¶Ð¸Ð²Ð°Ð½Ð¸Ðµ.',
  `house` varchar(10) DEFAULT '' COMMENT 'ÐÐ¾Ð¼ÐµÑ€ Ð´Ð¾Ð¼Ð°',
  `korp` varchar(10) DEFAULT '' COMMENT 'ÐšÐ¾Ñ€Ð¿ÑƒÑ Ð´Ð¾Ð¼Ð°',
  `flat` varchar(10) DEFAULT '' COMMENT 'ÐšÐ²Ð°Ñ€Ñ‚Ð¸Ñ€Ð°',
  `validity` int(11) NOT NULL DEFAULT '-1' COMMENT 'Ð¡Ñ‚ÐµÐ¿ÐµÐ½ÑŒ Ð²Ð°Ð»Ð¸Ð´Ð½Ð¾ÑÑ‚Ð¸ Ð°Ð²Ñ‚Ð¾Ñ€Ð¸Ð·Ð¾Ð²Ð°Ð½Ð½Ð¾Ð³Ð¾ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð°',
  `comments` varchar(512) DEFAULT NULL COMMENT 'ÐÐµÐºÐ¸Ð¹ Ð½ÐµÐ¾Ð±ÑÐ·Ð°Ñ‚ÐµÐ»ÑŒÐ½Ñ‹Ð¹ ÐºÐ¾Ð¼Ð¼ÐµÐ½Ñ‚Ð°Ñ€Ð¸Ð¹',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_auth_id_UNIQUE` (`auth_id`),
  KEY `idx_clients_authorization_streets` (`streets_id`),
  CONSTRAINT `fk_clients_authorization_streets` FOREIGN KEY (`streets_id`) REFERENCES `streets` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð¡Ð»Ð¾Ð²Ð°Ñ€ÑŒ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð² Ð´Ð»Ñ Ð°Ð²Ñ‚Ð¾Ñ€Ð¸Ð·Ð°Ñ†Ð¸Ð¸.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clients_authorization`
--

LOCK TABLES `clients_authorization` WRITE;
/*!40000 ALTER TABLE `clients_authorization` DISABLE KEYS */;
/*!40000 ALTER TABLE `clients_authorization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `information`
--

DROP TABLE IF EXISTS `information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `information` (
  `id` bigint(20) NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `name` varchar(100) NOT NULL COMMENT 'ÐÐ°Ð¸Ð¼ÐµÐ½Ð¾Ð²Ð°Ð½Ð¸Ðµ ÑƒÐ·Ð»Ð° ÑÐ¿Ñ€Ð°Ð²ÐºÐ¸',
  `text` text NOT NULL COMMENT 'html-Ñ‚ÐµÐºÑÑ‚ ÑÐ¿Ñ€Ð°Ð²ÐºÐ¸',
  `text_print` text NOT NULL COMMENT 'Ð¢ÐµÐºÑÑ‚ Ð´Ð»Ñ Ð¿ÐµÑ‡Ð°Ñ‚Ð¸ Ð¸Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¾Ð½Ð½Ð¾Ð³Ð¾ ÑƒÐ·Ð»Ð°',
  PRIMARY KEY (`id`),
  KEY `idx_information_information` (`parent_id`),
  CONSTRAINT `fk_information_information` FOREIGN KEY (`parent_id`) REFERENCES `information` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð¢Ð°Ð±Ð»Ð¸Ñ†Ð° ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¾Ð¹ Ð¸Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¸ Ð´Ñ€ÐµÐ²Ð¾Ð²Ð¸Ð´Ð½Ð¾Ð¹ ÑÑ‚Ñ€ÑƒÐºÑ‚ÑƒÑ€Ñ‹';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `information`
--

LOCK TABLES `information` WRITE;
/*!40000 ALTER TABLE `information` DISABLE KEYS */;
/*!40000 ALTER TABLE `information` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `net`
--

DROP TABLE IF EXISTS `net`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `net` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ÐŸÐ¾Ñ€Ñ‚Ð¡ÐµÑ€Ð²ÐµÑ€Ð°="3128" ÐŸÐ¾Ñ€Ñ‚Ð’ÐµÐ±Ð¡ÐµÑ€Ð²ÐµÑ€Ð°="8080" ÐŸÐ¾Ñ€Ñ‚ÐšÐ»Ð¸ÐµÐ½Ñ‚Ð°="3129" ÐÐ´Ñ€ÐµÑÐ¡ÐµÑ€Ð²ÐµÑ€Ð°="localhost"',
  `server_port` int(11) NOT NULL COMMENT 'Ð¡ÐµÑ€Ð²ÐµÑ€Ð½Ñ‹Ð¹ Ð¿Ð¾Ñ€Ñ‚ Ð¿Ñ€Ð¸ÐµÐ¼Ð° Ð·Ð°Ð´Ð°Ð½Ð¸Ð¹ Ð¿Ð¾ ÑÐµÑ‚Ð¸ Ð¾Ñ‚ ÐºÐ»Ð¸ÐµÑ‚ÑÐºÐ¸Ñ… Ð¿Ñ€Ð¸Ð»Ð¾Ð¶ÐµÐ½Ð¸Ð¹.',
  `web_server_port` int(11) NOT NULL COMMENT 'Ð¡ÐµÑ€Ð²ÐµÑ€Ð½Ñ‹Ð¹ Ð¿Ð¾Ñ€Ñ‚ Ð´Ð»Ñ Ð¿Ñ€Ð¸ÐµÐ¼Ð° web Ð·Ð°Ð¿Ñ€Ð¾ÑÐ¾Ð² Ð² ÑÐ¸ÑÑ‚ÐµÐ¼Ðµ Ð¾Ñ‚Ñ‡ÐµÑ‚Ð¾Ð².',
  `client_port` int(11) NOT NULL COMMENT 'UDP ÐŸÐ¾Ñ€Ñ‚ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð°, Ð½Ð° ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ð¹ Ð¸Ð´ÐµÑ‚ Ñ€Ð°ÑÑÑ‹Ð»ÐºÐ° ÑˆÐ¸Ñ€Ð¾ÐºÐ¾Ð²ÐµÑ‰Ð°Ñ‚ÐµÐ»ÑŒÐ½Ñ‹Ñ… Ð¿Ð°ÐºÐµÑ‚Ð¾Ð².',
  `finish_time` time NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð¿Ñ€ÐµÐºÑ€Ð°Ñ‰ÐµÐ½Ð¸Ñ Ð¿Ñ€Ð¸ÐµÐ¼Ð° Ð·Ð°ÑÐ²Ð¾Ðº Ð½Ð° Ð¿Ð¾ÑÑ‚Ð°Ð½Ð¾Ð²ÐºÑƒ Ð² Ð¾Ñ‡ÐµÑ€ÐµÐ´ÑŒ',
  `start_time` time NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð½Ð°Ñ‡Ð°Ð»Ð° Ð¿Ñ€Ð¸ÐµÐ¼Ð° Ð·Ð°ÑÐ²Ð¾Ðº Ð½Ð° Ð¿Ð¾ÑÑ‚Ð°Ð½Ð¾Ð²ÐºÑƒ Ð² Ð¾Ñ‡ÐµÑ€ÐµÐ´ÑŒ',
  `version` varchar(25) NOT NULL DEFAULT 'ÐÐµ Ð¿Ñ€Ð¸ÑÐ²Ð¾ÐµÐ½Ð°' COMMENT 'Ð’ÐµÑ€ÑÐ¸Ñ Ð‘Ð”',
  `first_number` int(11) NOT NULL DEFAULT '1',
  `last_number` int(11) NOT NULL DEFAULT '999',
  `numering` tinyint(1) NOT NULL DEFAULT '1' COMMENT '0 Ð¾Ð±Ñ‰Ð°Ñ Ð½ÑƒÐ¼ÐµÑ€Ð°Ñ†Ð¸Ñ, 1 Ð´Ð»Ñ ÐºÐ°Ð¶Ð´Ð¾Ð¹ ÑƒÑÐ»ÑƒÐ³Ð¸ ÑÐ²Ð¾Ñ Ð½ÑƒÐ¼ÐµÑ€Ð°Ñ†Ð¸Ñ',
  `point` int(11) NOT NULL DEFAULT '0' COMMENT '0 ÐºÐ°Ð±Ð¸Ð½ÐµÑ‚, 1 Ð¾ÐºÐ½Ð¾, 2 ÑÑ‚Ð¾Ð¹ÐºÐ°',
  `sound` int(11) NOT NULL DEFAULT '2' COMMENT '0 Ð½ÐµÑ‚ Ð¾Ð¿Ð¾Ð²ÐµÑ‰ÐµÐ½Ð¸Ñ, 1 Ñ‚Ð¾Ð»ÑŒÐºÐ¾ ÑÐ¸Ð³Ð½Ð°Ð», 2 ÑÐ¸Ð³Ð½Ð°Ð»+Ð³Ð¾Ð»Ð¾Ñ',
  `branch_id` bigint(20) NOT NULL DEFAULT '-1',
  `sky_server_url` varchar(145) NOT NULL DEFAULT '',
  `zone_board_serv_addr` varchar(145) NOT NULL DEFAULT '',
  `zone_board_serv_port` bigint(20) NOT NULL DEFAULT '0',
  `voice` int(11) NOT NULL DEFAULT '0' COMMENT '0 - Ð¿Ð¾ ÑƒÐ¼Ð¾Ð»Ñ‡Ð°Ð½Ð¸ÑŽ, Ð½Ñƒ Ð¸ Ñ‚.Ð´. Ð¿Ð¾ Ð½Ð°Ð±Ð¾Ñ€Ñƒ Ð·Ð²ÑƒÐºÐ¾Ð²',
  `black_time` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð½Ð°Ñ…Ð¾Ð¶Ð´ÐµÐ½Ð¸Ñ Ð² Ð±Ð»ÐµÐºÐ»Ð¸ÑÑ‚Ðµ Ð² Ð¼Ð¸Ð½ÑƒÑ‚Ð°Ñ…. 0 - Ð¿Ð¾Ð¿Ð°Ð²ÑˆÐ¸Ðµ Ð² Ð±Ð»ÐµÐºÑÐ»Ð¸ÑÑ‚ Ð½Ðµ Ð±Ð»Ð¾ÐºÐ¸Ñ€ÑƒÑŽÑ‚ÑÑ',
  `limit_recall` int(11) NOT NULL DEFAULT '0' COMMENT 'ÐšÐ¾Ð»Ð¸Ñ‡ÐµÑÑ‚Ð²Ð¾ Ð¿Ð¾Ð²Ñ‚Ð¾Ñ€Ð½Ñ‹Ñ… Ð²Ñ‹Ð·Ð¾Ð²Ð¾Ð² Ð¿ÐµÑ€ÐµÐ´ Ð¾Ñ‚ÐºÐ»Ð¾Ð½ÐµÐ½Ð¸ÐµÐ¼ Ð½ÐµÑÐ²Ð¸Ð²ÑˆÐµÐ³Ð¾ÑÑ Ð¿Ð¾ÑÐµÑ‚Ð¸Ñ‚ÐµÐ»Ñ, 0-Ð±ÐµÑÐºÐ¾Ð½ÐµÑ‡Ð½Ð¾',
  `button_free_design` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Ð°Ð²Ñ‚Ð¾Ñ€Ð°ÑÑÑ‚Ð°Ð½Ð¾Ð²ÐºÐ° ÐºÐ½Ð¾Ð¿Ð¾Ðº Ð½Ð° ÐºÐ¸Ð¾ÑÐºÐµ',
  `ext_priority` int(11) NOT NULL DEFAULT '0' COMMENT 'ÐšÐ¾Ð»Ð¸Ñ‡ÐµÑÑ‚Ð²Ð¾ Ð´Ð¾Ð¿Ð¾Ð»Ð½Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ñ‹Ñ… Ð¿Ñ€Ð¸Ð¾Ñ€Ð¸Ñ‚ÐµÑ‚Ð¾Ð²',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COMMENT='Ð¡ÐµÑ‚ÐµÐ²Ñ‹Ðµ Ð½Ð°ÑÑ‚Ñ€Ð¾Ð¹ÐºÐ¸ ÑÐµÑ€Ð²ÐµÑ€Ð°.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `net`
--

LOCK TABLES `net` WRITE;
/*!40000 ALTER TABLE `net` DISABLE KEYS */;
INSERT INTO `net` VALUES (1,3128,8088,3129,'18:00:00','08:45:00','7',1,999,0,0,1,113,'http://localhost:8080/qskyapi/customer_events?wsdl','127.0.0.1',27007,0,0,0,0,0);
/*!40000 ALTER TABLE `net` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `properties`
--

DROP TABLE IF EXISTS `properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `properties` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hide` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'ÐŸÐ¾ÐºÐ°Ð·Ñ‹Ð²Ð°Ñ‚ÑŒ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»ÑŽ ÑÑ‚Ð¾Ñ‚ Ð¿Ð°Ñ€Ð°Ð¼ÐµÑ‚Ñ€ Ð¸Ð»Ð¸ Ð½ÐµÑ‚',
  `psection` varchar(128) DEFAULT NULL COMMENT 'Ð Ð°Ð·Ð´ÐµÐ»',
  `pkey` varchar(128) NOT NULL COMMENT 'ÐšÐ»ÑŽÑ‡',
  `pvalue` varchar(10240) DEFAULT NULL COMMENT 'Ð—Ð½Ð°Ñ‡ÐµÐ½Ð¸Ðµ',
  `pcomment` varchar(256) DEFAULT NULL,
  `pdata` text COMMENT 'ÐÐµ Ñ‚Ð¸Ð¿Ð¸Ð·Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ñ‹Ðµ Ñ‚ÐµÐºÑÑ‚Ð¾Ð²Ñ‹Ðµ Ð´Ð°Ð½Ð½Ñ‹Ðµ.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `section_key_idx` (`psection`,`pkey`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `properties`
--

LOCK TABLES `properties` WRITE;
/*!40000 ALTER TABLE `properties` DISABLE KEYS */;
INSERT INTO `properties` VALUES (1,0,'mainboard','type','classic','Type of main tablo',NULL);
/*!40000 ALTER TABLE `properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT 'ÐÐ°Ð·Ð²Ð°Ð½Ð¸Ðµ Ð¾Ñ‚Ñ‡ÐµÑ‚Ð°, Ð²Ñ‹Ð²Ð¾Ð´Ð¸Ð¼Ð¾Ðµ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»ÑŽ.',
  `className` varchar(150) NOT NULL COMMENT 'ÐšÐ»Ð°ÑÑ Ñ„Ð¾Ñ€Ð¼Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð¸Ñ Ð¾Ñ‚Ñ‡ÐµÑ‚Ð°. ÐŸÐ¾Ð»Ð½Ð¾Ðµ Ð½Ð°Ð¸Ð¼ÐµÐ½Ð¾Ð²Ð°Ð½Ð¸Ðµ ÐºÐ»Ð°ÑÑÐ° Ñ Ð¿Ð°ÐºÐµÑ‚Ð°Ð¼Ð¸.',
  `template` varchar(150) NOT NULL COMMENT 'Ð¨Ð°Ð±Ð»Ð¾Ð½ Ð¾Ñ‚Ñ‡ÐµÑ‚Ð°. Ð¥Ñ€Ð°Ð½Ð¸Ñ‚ÑÑ Ð² Ð¾Ñ‚Ð´ÐµÐ»ÑŒÐ½Ð¾Ð¼ Ð¿Ð°ÐºÐµÑ‚Ðµ Ð² jar.',
  `href` varchar(150) NOT NULL COMMENT 'Ð¡ÑÑ‹Ð»ÐºÐ° Ð½Ð° Ð¾Ñ‚Ñ‡ÐµÑ‚ Ð² index.html. Ð‘ÐµÐ· Ñ€Ð°ÑÑˆÐ¸Ñ€ÐµÐ½Ð¸Ñ Ñ‚Ð¸Ð¿Ð° Ñ„Ð°Ð¹Ð»Ð°.',
  PRIMARY KEY (`id`,`href`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1 COMMENT='Ð—Ð°Ñ€ÐµÐ³Ð¸ÑÑ‚Ñ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ñ‹Ðµ Ð°Ð½Ð°Ð»Ð¸Ñ‚Ð¸Ñ‡ÐµÑÐºÐ¸Ðµ Ð¾Ñ‚Ñ‡ÐµÑ‚Ñ‹.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
INSERT INTO `reports` VALUES (1,'Ð¡Ñ‚Ð°Ñ‚Ð¸ÑÑ‚Ð¸Ñ‡ÐµÑÐºÐ¸Ð¹ Ð¾Ñ‚Ñ‡ÐµÑ‚ Ð² Ñ€Ð°Ð·Ñ€ÐµÐ·Ðµ ÑƒÑÐ»ÑƒÐ³ Ð·Ð° Ð¿ÐµÑ€Ð¸Ð¾Ð´','ru.apertum.qsystem.reports.formirovators.StatisticServices','/ru/apertum/qsystem/reports/templates/statisticServicesPeriod.jasper','statistic_period_services'),(2,'Ð¡Ñ‚Ð°Ñ‚Ð¸ÑÑ‚Ð¸Ñ‡ÐµÑÐºÐ¸Ð¹ Ð¾Ñ‚Ñ‡ÐµÑ‚ Ð² Ñ€Ð°Ð·Ñ€ÐµÐ·Ðµ Ð¿ÐµÑ€ÑÐ¾Ð½Ð°Ð»Ð° Ð·Ð° Ð¿ÐµÑ€Ð¸Ð¾Ð´','ru.apertum.qsystem.reports.formirovators.StatisticUsers','/ru/apertum/qsystem/reports/templates/statisticUsersPeriod.jasper','statistic_period_users'),(3,'ÐžÑ‚Ñ‡ÐµÑ‚ Ð¿Ð¾ Ñ€Ð°ÑÐ¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸ÑŽ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð² Ð¿Ð¾ Ð²Ð¸Ð´Ñƒ ÑƒÑÐ»ÑƒÐ³ Ð·Ð° Ð¿ÐµÑ€Ð¸Ð¾Ð´','ru.apertum.qsystem.reports.formirovators.RatioServices','/ru/apertum/qsystem/reports/templates/ratioServicesPeriod.jasper','ratio_period_services'),(4,'Ð Ð°ÑÐ¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸Ðµ Ð½Ð°Ð³Ñ€ÑƒÐ·ÐºÐ¸ Ð²Ð½ÑƒÑ‚Ñ€Ð¸ Ð´Ð½Ñ','ru.apertum.qsystem.reports.formirovators.DistributionJobDay','/ru/apertum/qsystem/reports/templates/DistributionJobDay.jasper','distribution_job_day'),(5,'Ð Ð°ÑÐ¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸Ðµ Ð½Ð°Ð³Ñ€ÑƒÐ·ÐºÐ¸ Ð²Ð½ÑƒÑ‚Ñ€Ð¸ Ð´Ð½Ñ Ð´Ð»Ñ ÑƒÑÐ»ÑƒÐ³Ð¸','ru.apertum.qsystem.reports.formirovators.DistributionJobDayServices','/ru/apertum/qsystem/reports/templates/DistributionJobDayServices.jasper','distribution_job_services'),(6,'Ð Ð°ÑÐ¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸Ðµ Ð½Ð°Ð³Ñ€ÑƒÐ·ÐºÐ¸ Ð²Ð½ÑƒÑ‚Ñ€Ð¸ Ð´Ð½Ñ Ð´Ð»Ñ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ','ru.apertum.qsystem.reports.formirovators.DistributionJobDayUsers','/ru/apertum/qsystem/reports/templates/DistributionJobDayUsers.jasper','distribution_job_users'),(7,'Ð Ð°ÑÐ¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸Ðµ ÑÑ€ÐµÐ´Ð½ÐµÐ³Ð¾ Ð²Ñ€ÐµÐ¼ÐµÐ½Ð¸ Ð¾Ð¶Ð¸Ð´Ð°Ð½Ð¸Ñ Ð²Ð½ÑƒÑ‚Ñ€Ð¸ Ð´Ð½Ñ','ru.apertum.qsystem.reports.formirovators.DistributionWaitDay','/ru/apertum/qsystem/reports/templates/DistributionWaitDay.jasper','distribution_wait_day'),(8,'Ð Ð°ÑÐ¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸Ðµ ÑÑ€ÐµÐ´Ð½ÐµÐ³Ð¾ Ð²Ñ€ÐµÐ¼ÐµÐ½Ð¸ Ð¾Ð¶Ð¸Ð´Ð°Ð½Ð¸Ñ Ð²Ð½ÑƒÑ‚Ñ€Ð¸ Ð´Ð½Ñ Ð´Ð»Ñ ÑƒÑÐ»ÑƒÐ³Ð¸','ru.apertum.qsystem.reports.formirovators.DistributionWaitDayServices','/ru/apertum/qsystem/reports/templates/DistributionWaitDayServices.jasper','distribution_wait_services'),(9,'Ð Ð°ÑÐ¿Ñ€ÐµÐ´ÐµÐ»ÐµÐ½Ð¸Ðµ ÑÑ€ÐµÐ´Ð½ÐµÐ³Ð¾ Ð²Ñ€ÐµÐ¼ÐµÐ½Ð¸ Ð¾Ð¶Ð¸Ð´Ð°Ð½Ð¸Ñ Ð²Ð½ÑƒÑ‚Ñ€Ð¸ Ð´Ð½Ñ Ð´Ð»Ñ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ','ru.apertum.qsystem.reports.formirovators.DistributionWaitDayUsers','/ru/apertum/qsystem/reports/templates/DistributionWaitDayUsers.jasper','distribution_wait_users'),(10,'Ð¡Ñ‚Ð°Ñ‚Ð¸ÑÑ‚Ð¸Ñ‡ÐµÑÐºÐ¸Ð¹ Ð¾Ñ‚Ñ‡ÐµÑ‚ Ð¿Ð¾ Ð¾Ñ‚Ð·Ñ‹Ð²Ð°Ð¼ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð² Ð·Ð° Ð¿ÐµÑ€Ð¸Ð¾Ð´','ru.apertum.qsystem.reports.formirovators.ResponsesReport','/ru/apertum/qsystem/reports/templates/responsesReport.jasper','statistic_period_responses'),(11,'ÐŸÐ¾Ð»Ð½Ñ‹Ð¹ Ð¾Ñ‚Ñ‡ÐµÑ‚ Ð¿Ð¾ Ð¾Ñ‚Ð·Ñ‹Ð²Ð°Ð¼ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð² Ð·Ð° Ð¿ÐµÑ€Ð¸Ð¾Ð´','ru.apertum.qsystem.reports.formirovators.ResponsesDateReport','/ru/apertum/qsystem/reports/templates/responsesDateReport.jasper','statistic_period_date_responses'),(12,'ÐžÑ‚Ñ‡ÐµÑ‚ Ð¿Ñ€ÐµÐ´Ð²Ð°Ñ€Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾ Ð·Ð°Ñ€ÐµÐ³Ð¸ÑÑ‚Ñ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ñ‹Ñ… ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð² Ð¿Ð¾ ÑƒÑÐ»ÑƒÐ³Ðµ Ð½Ð° Ð´Ð°Ñ‚Ñƒ','ru.apertum.qsystem.reports.formirovators.DistributionMedDayServices','/ru/apertum/qsystem/reports/templates/DistributionMedDayServices.jasper','distribution_med_services'),(13,'ÐžÑ‚Ñ‡ÐµÑ‚ Ð¿Ð¾ Ð°Ð²Ñ‚Ð¾Ñ€Ð¸Ð·Ð¾Ð²Ð°Ð½Ð½Ñ‹Ð¼ Ð¿ÐµÑ€ÑÐ¾Ð½Ð°Ð¼ Ð·Ð° Ð¿ÐµÑ€Ð¸Ð¾Ð´ Ð´Ð»Ñ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ','ru.apertum.qsystem.reports.formirovators.AuthorizedClientsPeriodUsers','/ru/apertum/qsystem/reports/templates/AuthorizedClientsPeriodUsers.jasper','authorized_clients_period_users'),(14,'ÐžÑ‚Ñ‡ÐµÑ‚ Ð¿Ð¾ Ð°Ð²Ñ‚Ð¾Ñ€Ð¸Ð·Ð¾Ð²Ð°Ð½Ð½Ñ‹Ð¼ Ð¿ÐµÑ€ÑÐ¾Ð½Ð°Ð¼ Ð·Ð° Ð¿ÐµÑ€Ð¸Ð¾Ð´ Ð´Ð»Ñ ÑƒÑÐ»ÑƒÐ³Ð¸','ru.apertum.qsystem.reports.formirovators.AuthorizedClientsPeriodServices','/ru/apertum/qsystem/reports/templates/AuthorizedClientsPeriodServices.jasper','authorized_clients_period_services'),(15,'ÐžÑ‚Ñ‡ÐµÑ‚ Ð¿Ð¾ Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ð°Ð¼ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð·Ð° Ð¿ÐµÑ€Ð¸Ð¾Ð´ Ð² Ñ€Ð°Ð·Ñ€ÐµÐ·Ðµ ÑƒÑÐ»ÑƒÐ³','ru.apertum.qsystem.reports.formirovators.ResultStateServices','/ru/apertum/qsystem/reports/templates/resultStateServicesPeriod.jasper','result_state_services');
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `response_event`
--

DROP TABLE IF EXISTS `response_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `response_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resp_date` datetime NOT NULL COMMENT 'Ð”Ð°Ñ‚Ð° Ð¾Ñ‚ÐºÐ»Ð¸ÐºÐ°',
  `response_id` bigint(20) NOT NULL,
  `services_id` bigint(20) DEFAULT NULL,
  `users_id` bigint(20) DEFAULT NULL,
  `clients_id` bigint(20) DEFAULT NULL COMMENT 'ÐšÐ»Ð¸ÐµÐ½Ñ‚ Ð¾ÑÑ‚Ð°Ð²Ð¸Ð²ÑˆÐ¸Ð¹ Ð¾Ñ‚Ð·Ñ‹Ð²',
  `client_data` varchar(245) NOT NULL DEFAULT '',
  `comment` varchar(256) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_response_date_responses` (`response_id`),
  KEY `idx_response_event_services` (`services_id`),
  KEY `idx_response_event_users` (`users_id`),
  KEY `idx_response_event_clients` (`clients_id`),
  CONSTRAINT `fk_response_date_responses` FOREIGN KEY (`response_id`) REFERENCES `responses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_response_event_clients` FOREIGN KEY (`clients_id`) REFERENCES `clients` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_response_event_services` FOREIGN KEY (`services_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_response_event_users` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COMMENT='Ð”Ð°Ñ‚Ñ‹ Ð¾ÑÑ‚Ð°Ð²Ð»ÐµÐ½Ð½Ñ‹Ñ… Ð¾Ñ‚Ð·Ñ‹Ð²Ð¾Ð².';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `response_event`
--

LOCK TABLES `response_event` WRITE;
/*!40000 ALTER TABLE `response_event` DISABLE KEYS */;
INSERT INTO `response_event` VALUES (1,'2018-09-05 13:46:44',1,NULL,NULL,NULL,'','');
/*!40000 ALTER TABLE `response_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `responses`
--

DROP TABLE IF EXISTS `responses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `responses` (
  `id` bigint(20) NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `name` varchar(100) NOT NULL DEFAULT '',
  `text` varchar(5000) NOT NULL DEFAULT '',
  `input_caption` varchar(512) NOT NULL DEFAULT '',
  `input_required` tinyint(1) NOT NULL DEFAULT '0',
  `deleted` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_responses_responses` (`parent_id`),
  CONSTRAINT `fk_responses_responses` FOREIGN KEY (`parent_id`) REFERENCES `responses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð¡Ð¿Ð¸ÑÐ¾Ðº Ð¾Ñ‚Ð·Ñ‹Ð²Ð¾Ð² Ð² Ð¾Ñ‚Ñ€Ð°Ñ‚Ð½Ð¾Ð¹ ÑÐ²ÑÐ·Ð¸';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `responses`
--

LOCK TABLES `responses` WRITE;
/*!40000 ALTER TABLE `responses` DISABLE KEYS */;
INSERT INTO `responses` VALUES (0,NULL,'Responses','<html><p  style=\'text-align: center;\'><font size=\'10\' color=\'#ffff4d\'>Help us to improve our work.</font><br><font  size=\'4\' color=\'#ffff4d\'>Each of your feedback is very important to us.</font></p>','',0,NULL),(1,0,'Excellent','<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Excellent</span></b>','',0,NULL),(2,0,'Good','<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Good</span></b>','',0,NULL),(3,0,'So, so...','<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>So, so...</span></b>','',0,NULL),(4,0,'Bad','<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Bad</span></b>','',0,NULL),(5,0,'Disgusting','<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Disgusting</span></b>','',0,NULL);
/*!40000 ALTER TABLE `responses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `results`
--

DROP TABLE IF EXISTS `results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `results` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL DEFAULT '' COMMENT 'Ð¢ÐµÐºÑÑ‚ Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ð°',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COMMENT='Ð¡Ð¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸Ðº Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ð¾Ð² Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ñ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð¼';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `results`
--

LOCK TABLES `results` WRITE;
/*!40000 ALTER TABLE `results` DISABLE KEYS */;
INSERT INTO `results` VALUES (1,'Completando formularios'),(2,'Problemas'),(3,'Otros');
/*!40000 ALTER TABLE `results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule`
--

DROP TABLE IF EXISTS `schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schedule` (
  `id` bigint(20) NOT NULL,
  `name` varchar(150) NOT NULL DEFAULT '' COMMENT 'ÐÐ°Ð¸Ð¼ÐµÐ½Ð¾Ð²Ð°Ð½Ð¸Ðµ Ð¿Ð»Ð°Ð½Ð°',
  `type` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð¢Ð¸Ð¿ Ð¿Ð»Ð°Ð½Ð°\n0 - Ð½ÐµÐ´ÐµÐ»ÑŒÐ½Ñ‹Ð¹\n1 - Ñ‡ÐµÑ‚Ð½Ñ‹Ðµ/Ð½ÐµÑ‡ÐµÑ‚Ð½Ñ‹Ðµ Ð´Ð½Ð¸',
  `time_begin_1` time DEFAULT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð½Ð°Ñ‡Ð°Ð»Ð° Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹',
  `time_end_1` time DEFAULT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸Ñ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹',
  `time_begin_2` time DEFAULT NULL,
  `time_end_2` time DEFAULT NULL,
  `time_begin_3` time DEFAULT NULL,
  `time_end_3` time DEFAULT NULL,
  `time_begin_4` time DEFAULT NULL,
  `time_end_4` time DEFAULT NULL,
  `time_begin_5` time DEFAULT NULL,
  `time_end_5` time DEFAULT NULL,
  `time_begin_6` time DEFAULT NULL,
  `time_end_6` time DEFAULT NULL,
  `time_begin_7` time DEFAULT NULL,
  `time_end_7` time DEFAULT NULL,
  `breaks_id1` bigint(20) DEFAULT NULL,
  `breaks_id2` bigint(20) DEFAULT NULL,
  `breaks_id3` bigint(20) DEFAULT NULL,
  `breaks_id4` bigint(20) DEFAULT NULL,
  `breaks_id5` bigint(20) DEFAULT NULL,
  `breaks_id6` bigint(20) DEFAULT NULL,
  `breaks_id7` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_schedule_breaks1` (`breaks_id1`),
  KEY `idx_schedule_breaks2` (`breaks_id2`),
  KEY `idx_schedule_breaks3` (`breaks_id7`),
  KEY `idx_schedule_breaks4` (`breaks_id3`),
  KEY `idx_schedule_breaks5` (`breaks_id4`),
  KEY `idx_schedule_breaks6` (`breaks_id5`),
  KEY `idx_schedule_breaks7` (`breaks_id6`),
  CONSTRAINT `fk_schedule_breaks1` FOREIGN KEY (`breaks_id1`) REFERENCES `breaks` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks2` FOREIGN KEY (`breaks_id2`) REFERENCES `breaks` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks3` FOREIGN KEY (`breaks_id7`) REFERENCES `breaks` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks4` FOREIGN KEY (`breaks_id3`) REFERENCES `breaks` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks5` FOREIGN KEY (`breaks_id4`) REFERENCES `breaks` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks6` FOREIGN KEY (`breaks_id5`) REFERENCES `breaks` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks7` FOREIGN KEY (`breaks_id6`) REFERENCES `breaks` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð¡Ð¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸Ðº Ñ€Ð°ÑÐ¿Ð¸ÑÐ°Ð½Ð¸Ð¹ Ð´Ð»Ñ ÑƒÑÐ»ÑƒÐ³';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule`
--

LOCK TABLES `schedule` WRITE;
/*!40000 ALTER TABLE `schedule` DISABLE KEYS */;
INSERT INTO `schedule` VALUES (1,'Working plan from 8.00 to 17.00',0,'08:00:00','17:00:00','08:00:00','17:00:00','08:00:00','17:00:00','08:00:00','17:00:00','08:00:00','17:00:00','08:00:00','17:00:00','08:00:00','17:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,'Workplan for the odd / even',1,'08:00:00','13:00:00','12:00:00','17:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(1533663911108,'Plan de Trabajo',1,'08:00:00','23:00:00','08:00:00','23:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `services`
--

DROP TABLE IF EXISTS `services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `services` (
  `id` bigint(20) NOT NULL,
  `name` varchar(2000) NOT NULL COMMENT 'ÐÐ°Ð¸Ð¼ÐµÐ½Ð¾Ð²Ð°Ð½Ð¸Ðµ ÑƒÑÐ»ÑƒÐ³Ð¸',
  `description` varchar(2000) DEFAULT NULL COMMENT 'ÐžÐ¿Ð¸ÑÐ°Ð½Ð¸Ðµ ÑƒÑÐ»ÑƒÐ³Ð¸.',
  `service_prefix` varchar(10) DEFAULT '',
  `button_text` varchar(2500) NOT NULL DEFAULT '' COMMENT 'HTML-Ñ‚ÐµÐºÑÑ‚ Ð´Ð»Ñ Ð²Ñ‹Ð²Ð¾Ð´Ð° Ð½Ð° ÐºÐ½Ð¾Ð¿ÐºÐ¸ Ñ€ÐµÐ³Ð¸ÑÑ‚Ñ€Ð°Ñ†Ð¸Ð¸.',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT 'Ð¡Ð¾ÑÑ‚Ð¾ÑÐ½Ð¸Ðµ ÑƒÑÐ»ÑƒÐ³Ð¸. 1 - Ð´Ð¾ÑÑ‚ÑƒÐ¿Ð½Ð°, 0 - Ð½ÐµÐ´Ð¾ÑÑ‚ÑƒÐ¿Ð½Ð°, -1 - Ð½ÐµÐ²Ð¸Ð´Ð¸Ð¼Ð°.',
  `enable` int(11) NOT NULL DEFAULT '1' COMMENT 'Ð¡Ð¿Ð¾ÑÐ¾Ð± Ð²Ñ‹Ð·Ð¾Ð²Ð° ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð° ÑŽÐ·ÐµÑ€Ð¾Ð¼\n1 - ÑÑ‚Ð°Ð½Ð´Ð°Ñ€Ñ‚Ð½Ð¾\n2 - backoffice, Ñ‚.Ðµ. Ð²Ñ‹Ð·Ð¾Ð² ÑÐ»ÐµÐ´ÑƒÑŽÑ‰ÐµÐ³Ð¾ Ð±ÐµÐ· Ñ‚Ð°Ð±Ð»Ð¾ Ð¸ Ð·Ð²ÑƒÐºÐ°, Ð·Ð°Ð¿ÐµÑ€ÑˆÐµÐ½Ð¸Ðµ Ñ‚Ð¾Ð»ÑŒÐºÐ¾ Ñ€ÐµÐ´Ð¸Ñ€ÐµÐºÑ‚Ð¾Ð¼',
  `prent_id` bigint(20) DEFAULT NULL COMMENT 'Ð“Ñ€ÑƒÐ¿Ð¿Ð¾Ð²Ð¾Ðµ Ð¿Ð¾Ð´Ñ‡Ð¸Ð½ÐµÐ½Ð¸Ðµ.',
  `day_limit` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð¾Ð³Ñ€Ð°Ð½Ð¸Ñ‡ÐµÐ½Ð¸Ðµ Ð²Ñ‹Ð´Ð°Ð½Ð½Ñ‹Ñ… Ð±Ð¸Ð»ÐµÑ‚Ð¾Ð² Ð² Ð´ÐµÐ½ÑŒ. 0-Ð½ÐµÑ‚ Ð¾Ð³Ñ€Ð°Ð½Ð¸Ñ‡ÐµÐ½Ð¸Ñ',
  `person_day_limit` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð¾Ð³Ñ€Ð°Ð½Ð¸Ñ‡ÐµÐ½Ð¸Ðµ Ð²Ñ‹Ð´Ð°Ð½Ð½Ñ‹Ñ… Ð±Ð¸Ð»ÐµÑ‚Ð¾Ð² Ð² Ð´ÐµÐ½ÑŒ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð°Ð¼ Ñ Ð¾Ð´Ð¸Ð½Ð°ÐºÐ¾Ð²Ñ‹Ð¼Ð¸ Ð²Ð²ÐµÐ´ÐµÐ½Ð½Ñ‹Ð¼Ð¸ Ð´Ð°Ð½Ð½Ñ‹Ð¼Ð¸. 0-Ð½ÐµÑ‚ Ð¾Ð³Ñ€Ð°Ð½Ð¸Ñ‡ÐµÐ½Ð¸Ñ',
  `advance_limit` int(11) NOT NULL DEFAULT '1' COMMENT 'ÐžÐ³Ñ€Ð°Ð½Ð¸Ñ‡ÐµÐ½Ð¸Ðµ Ð¿Ð¾ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑÑ‚Ð²Ñƒ Ð¿Ñ€ÐµÐ´Ð²Ð°Ñ€Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾ Ñ€ÐµÐ³Ð¸ÑÑ‚Ñ€Ð¸Ñ€Ð¾Ð²ÑˆÐ¸Ñ…ÑÑ Ð² Ñ‡Ð°Ñ',
  `advance_limit_period` int(11) DEFAULT '14' COMMENT 'Ð¾Ð³Ñ€Ð°Ð½Ð¸Ñ‡ÐµÐ½Ð¸Ðµ Ð² Ð´Ð½ÑÑ…, Ð² Ð¿Ñ€ÐµÐ´ÐµÐ»Ð°Ñ… ÐºÐ¾Ñ‚Ð¾Ñ€Ð¾Ð³Ð¾ Ð¼Ð¾Ð¶Ð½Ð¾ Ð·Ð°Ð¿Ð¸ÑÐ°Ñ‚ÑŒÑÑ Ð²Ð¿ÐµÑ€ÐµÐ´. Ð¼Ð¾Ð¶ÐµÑ‚ Ð±Ñ‹Ñ‚ÑŒ null Ð¸Ð»Ð¸ 0 ÐµÑÐ»Ð¸ Ð½ÐµÑ‚ Ð¾Ð³Ñ€Ð°Ð½Ð¸Ñ‡ÐµÐ½Ð¸Ñ',
  `advance_time_period` int(11) NOT NULL DEFAULT '60' COMMENT 'Ð¿ÐµÑ€Ð¸Ð¾Ð´Ñ‹, Ð½Ð° ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ðµ Ð´ÐµÐ»Ð¸Ñ‚ÑÑ Ð´ÐµÐ½ÑŒ, Ð´Ð»Ñ Ð·Ð°Ð¿Ð¸ÑÐ¸ Ð¿Ñ€ÐµÐ´Ð²Ð°Ñ€Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾',
  `schedule_id` bigint(20) DEFAULT NULL COMMENT 'ÐŸÐ»Ð°Ð½ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ ÑƒÑÐ»ÑƒÐ³Ð¸',
  `input_required` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'ÐžÐ±ÑÐ·Ñ‹Ð²Ð°Ñ‚ÑŒ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð° Ð²Ð²Ð¾Ð´Ð¸Ñ‚ÑŒ Ñ‡Ñ‚Ð¾-Ñ‚Ð¾ Ð¿ÐµÑ€ÐµÐ´ Ð¿Ð¾ÑÑ‚Ð¾Ð½Ð¾Ð²ÐºÐ¾Ð¹ Ð² Ð¾Ñ‡ÐµÑ€ÐµÐ´ÑŒ',
  `input_caption` varchar(2000) NOT NULL DEFAULT 'Ð’Ð²ÐµÐ´Ð¸Ñ‚Ðµ Ð½Ð¾Ð¼ÐµÑ€ Ð´Ð¾ÐºÑƒÐ¼ÐµÐ½Ñ‚Ð°' COMMENT 'Ð¢ÐµÐºÑÑ‚ Ð½Ð°Ð´ Ð¿Ð¾Ð»ÐµÐ¼ Ð²Ð²Ð¾Ð´Ð° Ð¾Ð±ÑÐ·Ð°Ñ‚ÐµÐ»ÑŒÐ½Ð¾Ð³Ð¾ Ð²Ð²Ð¾Ð´Ð°',
  `result_required` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Ð¢Ñ€ÐµÐ±Ð¾Ð²Ð°Ñ‚ÑŒ Ð²Ð²Ð¾Ð´ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»ÐµÐ¼ Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚Ð° Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ñ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð¼',
  `calendar_id` bigint(20) DEFAULT NULL,
  `pre_info_html` text NOT NULL COMMENT 'html Ñ‚ÐµÐºÑÑ‚ Ð¸Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ð¾Ð½Ð½Ð¾Ð³Ð¾ ÑÐ¾Ð¾Ð±Ñ‰ÐµÐ½Ð¸Ñ Ð¿ÐµÑ€ÐµÐ´ Ð¿Ð¾ÑÑ‚Ð°Ð½Ð¾Ð²ÐºÐ¾Ð¹ Ð² Ð¾Ñ‡ÐµÑ€ÐµÐ´ÑŒ',
  `pre_info_print_text` text NOT NULL COMMENT 'Ñ‚ÐµÐºÑÑ‚ Ð´Ð»Ñ Ð¿ÐµÑ‡Ð°Ñ‚Ð¸ Ð¿Ñ€Ð¸ Ð½ÐµÐ¾Ð±Ñ…Ð¾Ð´Ð¸Ð¼Ð¾ÑÑ‚Ð¸ Ð¿ÐµÑ€ÐµÐ´ Ð¿Ð¾ÑÑ‚Ð°Ð½Ð¾Ð²ÐºÐ¾Ð¹ Ð² Ð¾Ñ‡ÐµÑ€ÐµÐ´ÑŒ',
  `point` int(11) NOT NULL DEFAULT '0' COMMENT 'ÑƒÐºÐ°Ð·Ð°Ð½Ð¸Ðµ Ð´Ð»Ñ ÐºÐ°ÐºÐ¾Ð³Ð¾ Ð¿ÑƒÐ½ÐºÑ‚Ð° Ñ€ÐµÐ³Ð¸ÑÑ‚Ñ€Ð°Ñ†Ð¸Ð¸ ÑƒÑÐ»ÑƒÐ³Ð°, 0-Ð´Ð»Ñ Ð²ÑÐµÑ…, Ñ…-Ð´Ð»Ñ ÐºÐ¸Ð¾ÑÐºÐ° Ñ….',
  `ticket_text` varchar(1500) DEFAULT NULL COMMENT 'Ð¢ÐµÐºÑÑ‚ Ð½Ð°Ð¿ÐµÑ‡Ð°Ñ‚Ð°ÐµÑ‚ÑÑ Ð½Ð° Ñ‚Ð°Ð»Ð¾Ð½Ðµ.',
  `tablo_text` varchar(1500) NOT NULL DEFAULT '' COMMENT 'Ð¢ÐµÐºÑÑ‚ Ð´Ð»Ñ Ð²Ñ‹Ð²Ð¾Ð´Ð° Ð½Ð° Ð³Ð»Ð°Ð²Ð½Ð¾Ðµ Ñ‚Ð°Ð±Ð»Ð¾ Ð² ÑˆÐ°Ð±Ð»Ð¾Ð½Ñ‹ Ð¿Ð°Ð½ÐµÐ»Ð¸ Ð²Ñ‹Ð·Ð²Ð°Ð½Ð½Ð¾Ð³Ð¾ Ð¸ Ñ‚Ñ€ÐµÑ‚ÑŒÑŽ ÐºÐ¾Ð»Ð¾Ð½ÐºÑƒ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ',
  `seq_id` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð¿Ð¾Ñ€ÑÐ´Ð¾Ðº ÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð¸Ñ ÐºÐ½Ð¾Ð¿Ð¾Ðº ÑƒÑÐ»ÑƒÐ³ Ð½Ð° Ð¿ÑƒÐ½ÐºÑ‚Ðµ Ñ€ÐµÐ³Ð¸ÑÑ‚Ñ€Ð°Ñ†Ð¸Ð¸',
  `but_x` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð¿Ð¾Ð·Ð¸Ñ†Ð¸Ñ ÐºÐ½Ð¾Ð¿ÐºÐ¸',
  `but_y` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð¿Ð¾Ð·Ð¸Ñ†Ð¸Ñ ÐºÐ½Ð¾Ð¿ÐºÐ¸',
  `but_b` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð¿Ð¾Ð·Ð¸Ñ†Ð¸Ñ ÐºÐ½Ð¾Ð¿ÐºÐ¸',
  `but_h` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð¿Ð¾Ð·Ð¸Ñ†Ð¸Ñ ÐºÐ½Ð¾Ð¿ÐºÐ¸',
  `deleted` date DEFAULT NULL COMMENT 'Ð¿Ñ€Ð¸Ð·Ð½Ð°Ðº ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ Ñ Ð¿Ñ€Ð¾ÑÑ‚Ð°Ð²Ð»ÐµÐ½Ð¸Ð¼ Ð´Ð°Ñ‚Ñ‹',
  `duration` int(11) NOT NULL DEFAULT '1' COMMENT 'ÐÐ¾Ñ€Ð¼Ð°Ñ‚Ð¸Ð². Ð¡Ñ€ÐµÐ´Ð½ÐµÐµ Ð²Ñ€ÐµÐ¼Ñ Ð¾ÐºÐ°Ð·Ð°Ð½Ð¸Ñ ÑÑ‚Ð¾Ð¹ ÑƒÑÐ»ÑƒÐ³Ð¸.  ÐŸÐ¾ÐºÐ° Ð´Ð»Ñ Ð¼Ð°Ñ€ÑˆÑ€ÑƒÑ‚Ð¸Ð·Ð°Ñ†Ð¸Ð¸ Ð¿Ñ€Ð¸ Ð¼ÐµÐ´Ð¾ÑÐ¼Ð¾Ñ‚Ñ€Ðµ',
  `sound_template` varchar(45) DEFAULT NULL COMMENT 'ÑˆÐ°Ð±Ð»Ð¾Ð½ Ð·Ð²ÑƒÐºÐ¾Ð²Ð¾Ð³Ð¾ Ð¿Ñ€Ð¸Ð³Ð»Ð°ÑˆÐµÐ½Ð¸Ñ. null Ð¸Ð»Ð¸ 0... - Ð¸ÑÐ¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÑŒ Ñ€Ð¾Ð´Ð¸Ñ‚ÐµÐ»ÑŒÑÐºÐ¸Ð¹.',
  `expectation` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð¾Ð±ÑÐ·Ð°Ñ‚ÐµÐ»ÑŒÐ½Ð¾Ð³Ð¾ Ð¾Ð¶Ð¸Ð´Ð°Ð½Ð¸Ñ Ð¿Ð¾ÑÐµÑ‚Ð¸Ñ‚ÐµÐ»Ñ',
  `link_service_id` bigint(20) DEFAULT NULL COMMENT 'Ð£ÑÐ»ÑƒÐ³Ð°, Ð² ÐºÐ¾Ñ‚Ð¾Ñ€ÑƒÑŽ Ñ€ÐµÐ°Ð»ÑŒÐ½Ð¾ Ð¿Ð¾Ð¿Ð°Ð´ÐµÑ‚ ÐºÐ»Ð¸ÐµÐ½Ñ‚. Ð ÑÑ‚Ð° ÑÐ°Ð¼Ð° ÑƒÑÐ»ÑƒÐ³Ð° Ñ‡Ð¸ÑÑ‚Ð¾ ÐºÐ½Ð¾Ð¿ÐºÐ°, ÑÑ€Ð»Ñ‹Ðº.',
  `inputed_as_ext` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Ð Ð°Ð·Ñ€ÐµÑˆÐµÐ½Ð¸Ðµ Ð²Ñ‹Ð²Ð¾Ð´Ð¸Ñ‚ÑŒ Ð²Ð²ÐµÐ´ÐµÐ½Ð½Ñ‹Ðµ Ð´Ð°Ð½Ð½Ñ‹Ðµ Ð² Ñ‚Ñ€ÐµÑ‚ÑŒÑŽ ÐºÐ¾Ð»Ð¾Ð½ÐºÑƒ Ð½Ð° Ñ‚Ð°Ð±Ð»Ð¾ Ð¸ Ð² Ð¿Ð°Ð½ÐµÐ»ÑŒ Ð²Ñ‹Ð·Ð¾Ð²Ð°',
  `inputed_as_number` int(11) NOT NULL DEFAULT '0' COMMENT 'Ð•ÑÐ»Ð¸ Ñ‚Ñ€ÐµÐ±ÑƒÐµÑ‚ÑÑ Ð¸ÑÐ¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÑŒ Ð²Ð²ÐµÐ´ÐµÐ½Ð½Ñ‹Ðµ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»ÐµÐ¼ Ð´Ð°Ð½Ð½Ñ‹Ðµ ÐºÐ°Ðº ÐµÐ³Ð¾ Ð½Ð¾Ð¼ÐµÑ€ Ð² Ð¾Ñ‡ÐµÑ€ÐµÐ´Ð¸. 0 - Ð³ÐµÐ½ÐµÑ€Ð¸Ñ€Ð¾Ð²Ð°Ñ‚ÑŒ ÐºÐ°Ðº Ð¾Ð±Ñ‹Ñ‡Ð½Ð¾',
  PRIMARY KEY (`id`),
  KEY `idx_servises_parent_id_servises_id` (`prent_id`),
  KEY `idx_services_shedule` (`schedule_id`),
  KEY `idx_services_calendar` (`calendar_id`),
  CONSTRAINT `fk_services_calendar` FOREIGN KEY (`calendar_id`) REFERENCES `calendar` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_services_shedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_servises_parent_id_servises_id` FOREIGN KEY (`prent_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð”ÐµÑ€ÐµÐ²Ð¾ ÑƒÑÐ»ÑƒÐ³';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `services`
--

LOCK TABLES `services` WRITE;
/*!40000 ALTER TABLE `services` DISABLE KEYS */;
INSERT INTO `services` VALUES (1,'Territorial de Valencia','Raiz de servicios','-','<html><p align=center><span style=\'font-size:55.0;color:#DC143C\'>Dirección Territorial</span><br><span style=\'font-size:45.0;color:#DC143C\'><i>Seleccione un servicio</i>',1,1,NULL,0,0,1,14,60,NULL,0,'',0,NULL,'','',0,'','',0,100,100,200,100,NULL,1,'120050##0',0,NULL,0,0),(2,'Service','Description of service','A','<html><b><p align=center><span style=\'font-size:40.0pt;color:blue\'>Información ',1,1,1,0,0,1,14,60,1,0,'',0,1,'','',0,'','',0,100,100,200,100,NULL,1,'031141##0',0,NULL,0,0),(1533664063269,'Bolsa de trabajo','Bolsa de trabajo','BOL','<html><b><p align=center><span style=\'font-size:40.0pt;color:blue\'>Bolsa de trabajo</span></b>',1,1,1,0,0,1,0,60,1533663911108,0,'',0,1,'','',0,'','',1,100,100,100,100,NULL,4,'031111##0',0,NULL,0,0);
/*!40000 ALTER TABLE `services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `services_langs`
--

DROP TABLE IF EXISTS `services_langs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `services_langs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `services_id` bigint(20) DEFAULT NULL,
  `lang` varchar(45) NOT NULL,
  `name` varchar(2000) NOT NULL DEFAULT '',
  `description` varchar(2000) DEFAULT NULL,
  `button_text` varchar(2500) NOT NULL DEFAULT '',
  `input_caption` varchar(2000) NOT NULL DEFAULT '',
  `ticket_text` varchar(1500) DEFAULT NULL,
  `tablo_text` varchar(1500) NOT NULL DEFAULT '',
  `pre_info_html` text NOT NULL,
  `pre_info_print_text` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_services_langs_services` (`services_id`),
  CONSTRAINT `fk_services_langs_services` FOREIGN KEY (`services_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `services_langs`
--

LOCK TABLES `services_langs` WRITE;
/*!40000 ALTER TABLE `services_langs` DISABLE KEYS */;
/*!40000 ALTER TABLE `services_langs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `services_users`
--

DROP TABLE IF EXISTS `services_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `services_users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `coefficient` int(11) NOT NULL DEFAULT '1' COMMENT 'ÐšÐ¾ÑÑ„Ñ„Ð¸Ñ†Ð¸ÐµÐ½Ñ‚ ÑƒÑ‡Ð°ÑÑ‚Ð¸Ñ. 0/1/2',
  `flexible_coef` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Ð²Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ð¾ÑÑ‚ÑŒ Ð¸Ð·Ð¼ÐµÐ½Ð¸Ñ‚ÑŒ ÐºÐ¾ÑÑ„ ÑƒÑ‡Ð°ÑÑ‚Ð¸Ñ ÑŽÐ·ÐµÑ€Ð°Ð¼Ð¸',
  `flexible_invitation` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Ð²Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ð¾ÑÑ‚ÑŒ Ð²Ñ‹Ð·Ñ‹Ð²Ð°Ñ‚ÑŒ Ð¸Ð· Ð»ÑŽÐ´ÐµÐ¹ Ðº ÑƒÑÐ»ÑƒÐ³Ðµ Ð° Ð½Ðµ Ð°Ð²Ñ‚Ð¾Ð¼Ð°Ñ‚Ð¾Ð¼ Ð¾Ñ‚Ð¾ Ð²ÑÑŽÐ´Ñƒ',
  PRIMARY KEY (`id`),
  KEY `idx_services_id_su_service_id` (`service_id`),
  KEY `idx_userss_id_su_user_id` (`user_id`),
  CONSTRAINT `fk_services_id_su_service_id` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_userss_id_su_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1 COMMENT='Ð¢Ð°Ð±Ð»Ð¸Ñ†Ð° ÑÐ¾Ð¾Ñ‚Ð²ÐµÑ‚ÑÑ‚Ð²Ð¸Ð¹ ÑƒÑÐ»ÑƒÐ³Ð° - Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»ÑŒ.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `services_users`
--

LOCK TABLES `services_users` WRITE;
/*!40000 ALTER TABLE `services_users` DISABLE KEYS */;
INSERT INTO `services_users` VALUES (1,2,2,1,0,0),(2,1533664063269,2,1,0,0);
/*!40000 ALTER TABLE `services_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spec_schedule`
--

DROP TABLE IF EXISTS `spec_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spec_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_from` date NOT NULL COMMENT 'Ð­Ñ‚Ð¾ ÑÐ¿ÐµÑ† Ñ€Ð°ÑÐ¿Ð¸ÑÐ°Ð½Ð¸Ðµ Ð´Ð»Ñ ÑÑ‚Ð¾Ð³Ð¾ ÐºÐ°Ð»ÐµÐ½Ð´Ð°Ñ€Ñ Ð´ÐµÐ¹ÑÑ‚Ð²ÑƒÐµÑ‚ Ñ ÑÑ‚Ð¾Ð¹ Ð´Ð°Ñ‚Ñ‹',
  `date_to` date NOT NULL COMMENT 'Ð­Ñ‚Ð¾ ÑÐ¿ÐµÑ† Ñ€Ð°ÑÐ¿Ð¸ÑÐ°Ð½Ð¸Ðµ Ð´Ð»Ñ ÑÑ‚Ð¾Ð³Ð¾ ÐºÐ°Ð»ÐµÐ½Ð´Ð°Ñ€Ñ Ð´ÐµÐ¹ÑÑ‚Ð²ÑƒÐµÑ‚ Ð´Ð¾ ÑÑ‚Ð¾Ð¹ Ð´Ð°Ñ‚Ñ‹',
  `calendar_id` bigint(20) NOT NULL,
  `schedule_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_spec_chedule_calendar` (`calendar_id`),
  KEY `idx_spec_chedule_schedule` (`schedule_id`),
  CONSTRAINT `fk_spec_schedule_calendar` FOREIGN KEY (`calendar_id`) REFERENCES `calendar` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_spec_schedule_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð¡Ð¿ÐµÑ†Ð¸Ð°Ð»ÑŒÐ½Ñ‹Ðµ Ñ€Ð°ÑÐ¿Ð¸ÑÐ°Ð½Ð¸Ñ Ð´Ð»Ñ Ð¿ÐµÑ€Ð¸Ð¾Ð´Ð¾Ð² Ð´Ð»Ñ ÐºÐ¾Ð½ÐºÑ€ÐµÑ‚Ð½Ñ‹Ñ… ÐºÐ°Ð»ÐµÐ½Ð´Ð°Ñ€ÐµÐ¹. ÐŸÐµÑ€ÐµÐºÑ€Ñ‹Ð²Ð°ÑŽÑ‚ ÑÑ‚Ð°Ñ€Ð´Ð°Ñ€Ñ‚Ð½Ð¾Ðµ Ñ€Ð°ÑÐ¿Ð¸ÑÐ°Ð½Ð¸Ðµ.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spec_schedule`
--

LOCK TABLES `spec_schedule` WRITE;
/*!40000 ALTER TABLE `spec_schedule` DISABLE KEYS */;
/*!40000 ALTER TABLE `spec_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `standards`
--

DROP TABLE IF EXISTS `standards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `standards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `wait_max` int(11) NOT NULL DEFAULT '0' COMMENT 'ÐœÐ°ÐºÑÐ¸Ð¼Ð°Ð»ÑŒÐ½Ð¾Ðµ Ð²Ñ€ÐµÐ¼Ñ Ð¾Ð¶Ð¸Ð´Ð°Ð½Ð¸Ñ, Ð² Ð¼Ð¸Ð½ÑƒÑ‚Ð°Ñ…',
  `work_max` int(11) NOT NULL DEFAULT '0' COMMENT 'ÐœÐ°ÐºÑÐ¸Ð¼Ð°Ð»ÑŒÐ½Ð¾Ðµ Ð²Ñ€ÐµÐ¼Ñ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ñ Ð¾Ð´Ð½Ð¸Ð¼ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð¼, Ð² Ð¼Ð¸Ð½ÑƒÑ‚Ð°Ñ…',
  `downtime_max` int(11) NOT NULL DEFAULT '0' COMMENT 'ÐœÐ°ÐºÑÐ¸Ð¼Ð°Ð»ÑŒÐ½Ð¾Ðµ Ð²Ñ€ÐµÐ¼Ñ Ð¿Ñ€Ð¾ÑÑ‚Ð¾Ñ Ð¿Ñ€Ð¸ Ð½Ð°Ð»Ð¸Ñ‡Ð¸Ð¸ Ð¾Ñ‡ÐµÑ€ÐµÐ´Ð¸, Ð² Ð¼Ð¸Ð½ÑƒÑ‚Ð°Ñ…',
  `line_service_max` int(11) NOT NULL DEFAULT '0' COMMENT 'ÐœÐ°ÐºÑÐ¸Ð¼Ð°Ð»ÑŒÐ½Ð°Ñ Ð´Ð»Ð¸Ð½Ð½Ð° Ð¾Ñ‡ÐµÑ€ÐµÐ´Ð¸ Ðº Ð¾Ð´Ð½Ð¾Ð¹ ÑƒÑÐ»ÑƒÐ³Ðµ',
  `line_total_max` int(11) NOT NULL DEFAULT '0' COMMENT 'ÐœÐ°ÐºÑÐ¸Ð¼Ð°Ð»ÑŒÐ½Ð¾Ðµ ÐºÐ¾Ð»Ð¸Ñ‡ÐµÑÑ‚Ð²Ð¾ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð² ÐºÐ¾ Ð²ÑÐµÐ¼ ÑƒÑÐ»ÑƒÐ³Ð°Ð¼',
  `relocation` int(11) NOT NULL DEFAULT '1' COMMENT 'Ñ‚Ð¸Ð¿Ð° Ð¿Ð°Ñ€Ð°Ð¼ÐµÑ‚Ñ€ ÐµÑÐ»Ð¸ ÐµÑÑ‚ÑŒ Ð¿ÐµÑ€ÐµÐ¼ÐµÑ‰ÐµÐ½Ð¸Ðµ, Ð½Ð°Ð¿Ñ€Ð¸Ð¼ÐµÑ€ Ð¼ÐµÐ¶Ð´Ñƒ ÐºÐ¾Ñ€Ð¿ÑƒÑÐ°Ð¼Ð¸ Ð¸Ð»Ð¸ Ñ…Ð¾Ð´ÑŒÐ±Ð° Ð´Ð¾ Ð¾Ð¿ÐµÑ€Ð°Ñ‚Ð¾Ñ€Ð°',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standards`
--

LOCK TABLES `standards` WRITE;
/*!40000 ALTER TABLE `standards` DISABLE KEYS */;
INSERT INTO `standards` VALUES (1,10,20,10,10,20,1);
/*!40000 ALTER TABLE `standards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `statistic`
--

DROP TABLE IF EXISTS `statistic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `statistic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  `results_id` bigint(20) DEFAULT NULL,
  `user_start_time` datetime NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð½Ð°Ñ‡Ð°Ð»Ð° Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚ÐºÐ¸ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð° ÑŽÐ·ÐµÑ€Ð¾Ð¼.',
  `user_finish_time` datetime NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð·Ð°Ð²ÐµÑ€ÑˆÐµÐ½Ð¸Ñ Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚ÐºÐ¸ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð° ÑŽÐ·ÐµÑ€Ð¾Ð¼.',
  `client_stand_time` datetime NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð¿Ð¾ÑÑ‚Ð°Ð½Ð¾Ð²ÐºÐ¸ ÐºÐ°ÑÑ‚Ð¾Ð¼ÐµÑ€Ð° Ð² Ð¾Ñ‡ÐµÑ€ÐµÐ´ÑŒ',
  `user_work_period` int(11) NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð¼ Ð² Ð¼Ð¸Ð½ÑƒÑ‚Ð°Ñ….',
  `client_wait_period` int(11) NOT NULL COMMENT 'Ð’Ñ€ÐµÐ¼Ñ Ð¾Ð¶Ð¸Ð´Ð°Ð½Ð¸Ñ Ð² Ð¼Ð¸Ð½ÑƒÑ‚Ð°Ñ…. ÐžÐ¿Ñ€ÐµÐ´ÐµÐ»ÑÐµÑ‚ÑÑ Ñ‚Ñ€Ð¸Ð³Ð³ÐµÑ€Ð¾Ð¼.',
  `state_in` int(11) NOT NULL DEFAULT '0' COMMENT 'ÐšÐ»Ð¸ÐµÐ½Ñ‚ Ð¿ÐµÑ€ÐµÑˆÐµÐ» Ð² ÑÑ‚Ð¾ ÑÐ¾ÑÑ‚Ð¾ÑÐ½Ð¸Ðµ',
  PRIMARY KEY (`id`),
  KEY `idx_work_user_id_users_id` (`user_id`),
  KEY `idx_work_Ñlient_id_Ñlients_id` (`client_id`),
  KEY `idx_work_service_id_services_id` (`service_id`),
  KEY `idx_statistic_results` (`results_id`),
  CONSTRAINT `fk_statistic_results` FOREIGN KEY (`results_id`) REFERENCES `results` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  CONSTRAINT `fk_work_Ñlient_id_Ñlients_id` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_work_service_id_services_id` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_work_user_id_users_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=latin1 COMMENT='Ð¡Ð¾Ð±Ñ‹Ñ‚Ð¸Ñ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ñ ÐºÐ»Ð¸ÐµÐ½Ñ‚Ð¾Ð¼.Ð¤Ð¾Ñ€Ð¼Ð¸Ñ€ÑƒÐµÑ‚ÑÑ Ñ‚Ñ€Ð¸Ð³Ð³ÐµÑ€Ð¾Ð¼';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `statistic`
--

LOCK TABLES `statistic` WRITE;
/*!40000 ALTER TABLE `statistic` DISABLE KEYS */;
INSERT INTO `statistic` VALUES (1,2,1536142918134,2,NULL,'2018-09-05 17:13:18','2018-09-05 17:13:29','2018-09-05 12:21:58',1,292,10),(2,2,1536147978477,2,NULL,'2018-09-05 17:13:43','2018-09-05 17:13:43','2018-09-05 13:46:18',1,208,0),(3,2,1536149524342,2,NULL,'2018-09-05 17:14:19','2018-09-05 17:14:43','2018-09-05 14:12:04',1,183,11),(4,2,1536159790062,1533664063269,NULL,'2018-09-05 17:15:12','2018-09-05 17:15:14','2018-09-05 17:03:10',1,13,10),(5,2,1536149524342,2,NULL,'2018-09-05 17:38:22','2018-09-05 17:38:39','2018-09-05 17:15:18',1,24,10),(6,2,1536161263513,1533664063269,NULL,'2018-09-05 17:38:43','2018-09-05 18:53:19','2018-09-05 17:27:43',76,12,10),(7,2,1536161276145,1533664063269,NULL,'2018-09-05 18:53:28','2018-09-05 18:53:29','2018-09-05 17:27:56',1,87,10),(8,2,1536166031923,1533664063269,NULL,'2018-09-05 18:53:40','2018-09-05 18:53:42','2018-09-05 18:47:11',1,7,10),(9,2,1536166311702,1533664063269,NULL,'2018-09-05 18:54:20','2018-09-05 18:54:24','2018-09-05 18:51:51',1,3,10),(10,2,1536166440266,1533664063269,NULL,'2018-09-05 18:54:30','2018-09-05 18:54:31','2018-09-05 18:54:00',1,1,10),(11,2,1536736712105,1533664063269,NULL,'2018-09-12 09:56:37','2018-09-12 09:56:39','2018-09-12 09:18:32',1,39,10),(12,2,1536738318804,1533664063269,NULL,'2018-09-12 10:21:14','2018-09-12 10:21:15','2018-09-12 09:45:18',1,37,10),(13,2,1536739750099,2,NULL,'2018-09-12 11:45:05','2018-09-12 11:45:07','2018-09-12 10:09:10',1,97,10),(14,2,1536745391125,2,NULL,'2018-09-12 13:06:32','2018-09-12 13:06:34','2018-09-12 11:43:11',1,84,10),(15,2,1536746593964,2,NULL,'2018-09-12 13:06:57','2018-09-12 13:06:59','2018-09-12 12:03:13',1,65,10),(16,2,1536746948347,2,NULL,'2018-09-12 13:07:27','2018-09-12 13:07:29','2018-09-12 12:09:08',1,59,10),(17,2,1536749217521,2,NULL,'2018-09-12 13:09:34','2018-09-12 13:09:36','2018-09-12 12:46:57',1,24,10),(18,2,1536750494272,2,NULL,'2018-09-12 13:16:45','2018-09-12 13:16:51','2018-09-12 13:08:14',1,10,10),(19,2,1536750500365,2,NULL,'2018-09-12 13:22:30','2018-09-12 13:22:33','2018-09-12 13:08:20',1,15,10),(20,2,1536750506243,2,NULL,'2018-09-12 13:22:44','2018-09-12 13:22:45','2018-09-12 13:08:26',1,15,10),(21,2,1536750528187,2,NULL,'2018-09-12 13:22:55','2018-09-12 13:22:59','2018-09-12 13:08:48',1,15,10),(22,2,1536750536146,2,NULL,'2018-09-12 13:24:11','2018-09-12 13:24:13','2018-09-12 13:08:56',1,16,10),(23,2,1536750543858,2,NULL,'2018-09-12 13:24:26','2018-09-12 13:24:28','2018-09-12 13:09:03',1,16,10),(24,2,1536750551589,2,NULL,'2018-09-12 13:24:48','2018-09-12 13:24:50','2018-09-12 13:09:11',1,17,10),(25,2,1538121456917,2,NULL,'2018-09-28 10:02:35','2018-09-28 10:03:09','2018-09-28 09:57:36',2,6,11),(26,2,1538121826430,2,NULL,'2018-09-28 10:04:19','2018-09-28 10:05:27','2018-09-28 10:03:46',2,2,11);
/*!40000 ALTER TABLE `statistic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `streets`
--

DROP TABLE IF EXISTS `streets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `streets` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT 'ÐÐ°Ð¸Ð¼ÐµÐ½Ð¾Ð²Ð°Ð½Ð¸Ðµ ÑƒÐ»Ð¸Ñ†Ñ‹.',
  PRIMARY KEY (`id`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ð¡Ð»Ð¾Ð²Ð°Ñ€ÑŒ ÑƒÐ»Ð¸Ñ†';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `streets`
--

LOCK TABLES `streets` WRITE;
/*!40000 ALTER TABLE `streets` DISABLE KEYS */;
/*!40000 ALTER TABLE `streets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ.',
  `name` varchar(150) NOT NULL COMMENT 'ÐÐ°Ð¸Ð¼ÐµÐ½Ð¾Ð²Ð°Ð½Ð¸Ðµ',
  `password` varchar(45) NOT NULL COMMENT 'ÐŸÐ°Ñ€Ð¾Ð»ÑŒ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ.',
  `point` varchar(45) NOT NULL COMMENT 'Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ†Ð¸Ñ Ñ€Ð°Ð±Ð¾Ñ‡ÐµÐ³Ð¾ Ð¼ÐµÑÑ‚Ð°',
  `adress_rs` smallint(6) NOT NULL DEFAULT '0' COMMENT 'ÐÐ´Ñ€ÐµÑ Ñ‚Ð°Ð±Ð»Ð¾  Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ Ð² Ð³ÐµÑ€Ð»ÑÐ½Ð´Ðµ RS485',
  `enable` int(11) NOT NULL DEFAULT '1' COMMENT 'Ð”ÐµÐ¹ÑÑ‚ÐºÑƒÑŽÑ‰Ð¸Ð¹ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»ÑŒ Ð¸Ð»Ð¸ ÑƒÐ´Ð°Ð»ÐµÐ½Ð½Ñ‹Ð¹.',
  `admin_access` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Ð”Ð¾ÑÑ‚ÑƒÐ¿ Ðº Ð°Ð´Ð¼Ð¸Ð½Ð¸ÑÑ‚Ñ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð¸Ñ ÑÐ¸ÑÑ‚ÐµÐ¼Ñ‹.',
  `report_access` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Ð”Ð¾ÑÑ‚ÑƒÐ¿ Ðº Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð¸ÑŽ Ð¾Ñ‚Ñ‡ÐµÑ‚Ð¾Ð².',
  `parallel_access` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Ð Ð°Ð·Ñ€ÐµÑˆÐµÐ½Ð¸Ðµ Ð²ÐµÑÑ‚Ð¸ Ð¿Ð°Ñ€Ð°Ð»Ð»ÐµÐ»ÑŒÐ½Ñ‹Ð¹ Ð¿Ñ€Ð¸ÐµÐ¼',
  `point_ext` varchar(1045) NOT NULL DEFAULT '' COMMENT 'Ð’Ñ‹Ð²Ð¾Ð´ Ð² Ñ‚Ñ€ÐµÑ‚ÑŒÑŽ ÐºÐ¾Ð»Ð¾Ð½ÐºÑƒ Ð½Ð° Ð³Ð»Ð°Ð²Ð½Ð¾Ð¼ Ñ‚Ð°Ð±Ð»Ð¾. html + ÐºÐ»Ð¸ÐµÐ½Ñ‚ ###  Ð¾ÐºÐ½Ð¾ @@@',
  `tablo_text` varchar(1500) NOT NULL DEFAULT '' COMMENT 'Ð¢ÐµÐºÑÑ‚ Ð´Ð»Ñ Ð²Ñ‹Ð²Ð¾Ð´Ð° Ð½Ð° Ð³Ð»Ð°Ð²Ð½Ð¾Ðµ Ñ‚Ð°Ð±Ð»Ð¾ Ð² ÑˆÐ°Ð±Ð»Ð¾Ð½Ñ‹ Ð¿Ð°Ð½ÐµÐ»Ð¸ Ð²Ñ‹Ð·Ð²Ð°Ð½Ð½Ð¾Ð³Ð¾ Ð¸ Ñ‚Ñ€ÐµÑ‚ÑŒÑŽ ÐºÐ¾Ð»Ð¾Ð½ÐºÑƒ Ð¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ñ',
  `deleted` date DEFAULT NULL COMMENT 'Ð¿Ñ€Ð¸Ð·Ð½Ð°Ðº ÑƒÐ´Ð°Ð»ÐµÐ½Ð¸Ñ Ñ Ð¿Ñ€Ð¾ÑÑ‚Ð°Ð²Ð»ÐµÐ½Ð¸ÐµÐ¼ Ð´Ð°Ñ‚Ñ‹',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1 COMMENT='ÐŸÐ¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ñ‚ÐµÐ»Ð¸ ÑÐ¸ÑÑ‚ÐµÐ¼Ñ‹.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Administrator','','1',32,1,1,1,0,'','',NULL),(2,'Operator','','2',33,1,0,0,0,'<html><span style=\'font-size:26.0pt;color:blue\'>Floor 1<br>Office 1 #user #service #inputed','',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-10-01  8:08:30
