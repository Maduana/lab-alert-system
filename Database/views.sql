-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: lab_alert_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Temporary view structure for view `vw_powerbi_alerts`
--

DROP TABLE IF EXISTS `vw_powerbi_alerts`;
/*!50001 DROP VIEW IF EXISTS `vw_powerbi_alerts`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_powerbi_alerts` AS SELECT 
 1 AS `alert_id`,
 1 AS `result_id`,
 1 AS `patient_id`,
 1 AS `patient_code`,
 1 AS `patient_name`,
 1 AS `pathologist_id`,
 1 AS `pathologist_user_id`,
 1 AS `pathologist_name`,
 1 AS `pathologist_employee_code`,
 1 AS `pathologist_registration_no`,
 1 AS `pathologist_email`,
 1 AS `pathologist_phone`,
 1 AS `pathologist_specialty`,
 1 AS `pathologist_active`,
 1 AS `test_name`,
 1 AS `result_value`,
 1 AS `unit`,
 1 AS `ai_score`,
 1 AS `severity`,
 1 AS `alert_message`,
 1 AS `notification_status`,
 1 AS `acknowledged`,
 1 AS `acknowledged_by`,
 1 AS `acknowledged_at`,
 1 AS `created_at`,
 1 AS `review_due_at`,
 1 AS `escalated`,
 1 AS `reminder_count`,
 1 AS `last_reminder_at`,
 1 AS `alert_date`,
 1 AS `alert_year`,
 1 AS `alert_quarter`,
 1 AS `alert_month_number`,
 1 AS `alert_month_name`,
 1 AS `alert_year_month`,
 1 AS `alert_date_key`,
 1 AS `alert_day_of_month`,
 1 AS `alert_day_name`,
 1 AS `alert_day_of_week_number`,
 1 AS `alert_hour`,
 1 AS `review_status`,
 1 AS `alert_status`,
 1 AS `review_target_minutes`,
 1 AS `review_minutes`,
 1 AS `review_seconds`,
 1 AS `overdue_minutes`,
 1 AS `minutes_until_due`,
 1 AS `target_performance`,
 1 AS `acknowledged_flag`,
 1 AS `open_flag`,
 1 AS `overdue_flag`,
 1 AS `due_soon_flag`,
 1 AS `escalated_flag`,
 1 AS `critical_flag`,
 1 AS `warning_flag`,
 1 AS `email_sent_flag`,
 1 AS `email_failed_flag`,
 1 AS `acknowledged_on_time_flag`,
 1 AS `acknowledged_late_flag`,
 1 AS `reminder_sent_flag`,
 1 AS `reminder_limit_flag`,
 1 AS `active_pathologist_flag`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_powerbi_alerts_history`
--

DROP TABLE IF EXISTS `vw_powerbi_alerts_history`;
/*!50001 DROP VIEW IF EXISTS `vw_powerbi_alerts_history`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_powerbi_alerts_history` AS SELECT 
 1 AS `alert_id`,
 1 AS `result_id`,
 1 AS `patient_id`,
 1 AS `patient_code`,
 1 AS `patient_name`,
 1 AS `pathologist_id`,
 1 AS `pathologist_user_id`,
 1 AS `pathologist_name`,
 1 AS `pathologist_employee_code`,
 1 AS `pathologist_registration_no`,
 1 AS `pathologist_email`,
 1 AS `pathologist_phone`,
 1 AS `pathologist_specialty`,
 1 AS `pathologist_active`,
 1 AS `test_name`,
 1 AS `result_value`,
 1 AS `unit`,
 1 AS `ai_score`,
 1 AS `severity`,
 1 AS `alert_message`,
 1 AS `notification_status`,
 1 AS `acknowledged`,
 1 AS `acknowledged_by`,
 1 AS `acknowledged_at`,
 1 AS `created_at`,
 1 AS `review_due_at`,
 1 AS `escalated`,
 1 AS `reminder_count`,
 1 AS `last_reminder_at`,
 1 AS `alert_date`,
 1 AS `alert_year`,
 1 AS `alert_quarter`,
 1 AS `alert_month_number`,
 1 AS `alert_month_name`,
 1 AS `alert_year_month`,
 1 AS `alert_date_key`,
 1 AS `alert_day_of_month`,
 1 AS `alert_day_name`,
 1 AS `alert_day_of_week_number`,
 1 AS `alert_hour`,
 1 AS `review_status`,
 1 AS `alert_status`,
 1 AS `review_target_minutes`,
 1 AS `review_minutes`,
 1 AS `review_seconds`,
 1 AS `overdue_minutes`,
 1 AS `minutes_until_due`,
 1 AS `target_performance`,
 1 AS `acknowledged_flag`,
 1 AS `open_flag`,
 1 AS `overdue_flag`,
 1 AS `due_soon_flag`,
 1 AS `escalated_flag`,
 1 AS `critical_flag`,
 1 AS `warning_flag`,
 1 AS `email_sent_flag`,
 1 AS `email_failed_flag`,
 1 AS `acknowledged_on_time_flag`,
 1 AS `acknowledged_late_flag`,
 1 AS `reminder_sent_flag`,
 1 AS `reminder_limit_flag`,
 1 AS `active_pathologist_flag`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_powerbi_alerts_live`
--

DROP TABLE IF EXISTS `vw_powerbi_alerts_live`;
/*!50001 DROP VIEW IF EXISTS `vw_powerbi_alerts_live`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_powerbi_alerts_live` AS SELECT 
 1 AS `alert_id`,
 1 AS `result_id`,
 1 AS `patient_id`,
 1 AS `patient_code`,
 1 AS `patient_name`,
 1 AS `pathologist_id`,
 1 AS `pathologist_user_id`,
 1 AS `pathologist_name`,
 1 AS `pathologist_employee_code`,
 1 AS `pathologist_registration_no`,
 1 AS `pathologist_email`,
 1 AS `pathologist_phone`,
 1 AS `pathologist_specialty`,
 1 AS `pathologist_active`,
 1 AS `test_name`,
 1 AS `result_value`,
 1 AS `unit`,
 1 AS `ai_score`,
 1 AS `severity`,
 1 AS `alert_message`,
 1 AS `notification_status`,
 1 AS `acknowledged`,
 1 AS `acknowledged_by`,
 1 AS `acknowledged_at`,
 1 AS `created_at`,
 1 AS `review_due_at`,
 1 AS `escalated`,
 1 AS `reminder_count`,
 1 AS `last_reminder_at`,
 1 AS `alert_date`,
 1 AS `alert_year`,
 1 AS `alert_quarter`,
 1 AS `alert_month_number`,
 1 AS `alert_month_name`,
 1 AS `alert_year_month`,
 1 AS `alert_date_key`,
 1 AS `alert_day_of_month`,
 1 AS `alert_day_name`,
 1 AS `alert_day_of_week_number`,
 1 AS `alert_hour`,
 1 AS `review_status`,
 1 AS `alert_status`,
 1 AS `review_target_minutes`,
 1 AS `review_minutes`,
 1 AS `review_seconds`,
 1 AS `overdue_minutes`,
 1 AS `minutes_until_due`,
 1 AS `target_performance`,
 1 AS `acknowledged_flag`,
 1 AS `open_flag`,
 1 AS `overdue_flag`,
 1 AS `due_soon_flag`,
 1 AS `escalated_flag`,
 1 AS `critical_flag`,
 1 AS `warning_flag`,
 1 AS `email_sent_flag`,
 1 AS `email_failed_flag`,
 1 AS `acknowledged_on_time_flag`,
 1 AS `acknowledged_late_flag`,
 1 AS `reminder_sent_flag`,
 1 AS `reminder_limit_flag`,
 1 AS `active_pathologist_flag`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_powerbi_audit_logs`
--

DROP TABLE IF EXISTS `vw_powerbi_audit_logs`;
/*!50001 DROP VIEW IF EXISTS `vw_powerbi_audit_logs`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_powerbi_audit_logs` AS SELECT 
 1 AS `audit_id`,
 1 AS `user_id`,
 1 AS `username`,
 1 AS `role`,
 1 AS `action_type`,
 1 AS `entity_type`,
 1 AS `entity_id`,
 1 AS `action_details`,
 1 AS `created_at`,
 1 AS `audit_date`,
 1 AS `audit_time`,
 1 AS `audit_year`,
 1 AS `audit_quarter`,
 1 AS `audit_month_number`,
 1 AS `audit_month_name`,
 1 AS `audit_year_month`,
 1 AS `audit_day_of_month`,
 1 AS `audit_day_name`,
 1 AS `audit_day_of_week_number`,
 1 AS `audit_hour`,
 1 AS `event_category`,
 1 AS `event_severity`,
 1 AS `event_outcome`,
 1 AS `system_event_flag`,
 1 AS `successful_login_flag`,
 1 AS `failed_login_flag`,
 1 AS `logout_flag`,
 1 AS `unauthorised_event_flag`,
 1 AS `unauthorised_acknowledgement_flag`,
 1 AS `unauthorised_result_access_flag`,
 1 AS `unauthorised_patient_lookup_flag`,
 1 AS `email_sent_flag`,
 1 AS `email_failed_flag`,
 1 AS `report_preview_flag`,
 1 AS `report_export_flag`,
 1 AS `alert_created_flag`,
 1 AS `alert_acknowledged_flag`,
 1 AS `email_alert_viewed_flag`,
 1 AS `reminder_event_flag`,
 1 AS `reminder_tracking_failure_flag`,
 1 AS `result_captured_flag`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_powerbi_dates`
--

DROP TABLE IF EXISTS `vw_powerbi_dates`;
/*!50001 DROP VIEW IF EXISTS `vw_powerbi_dates`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_powerbi_dates` AS SELECT 
 1 AS `calendar_date`,
 1 AS `calendar_year`,
 1 AS `calendar_quarter`,
 1 AS `month_number`,
 1 AS `month_name`,
 1 AS `year_months`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_powerbi_pathologists`
--

DROP TABLE IF EXISTS `vw_powerbi_pathologists`;
/*!50001 DROP VIEW IF EXISTS `vw_powerbi_pathologists`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_powerbi_pathologists` AS SELECT 
 1 AS `pathologist_id`,
 1 AS `pathologist_user_id`,
 1 AS `pathologist_username`,
 1 AS `pathologist_name`,
 1 AS `employee_code`,
 1 AS `registration_no`,
 1 AS `specialty`,
 1 AS `active`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_powerbi_tests`
--

DROP TABLE IF EXISTS `vw_powerbi_tests`;
/*!50001 DROP VIEW IF EXISTS `vw_powerbi_tests`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_powerbi_tests` AS SELECT 
 1 AS `test_name`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_powerbi_user_access`
--

DROP TABLE IF EXISTS `vw_powerbi_user_access`;
/*!50001 DROP VIEW IF EXISTS `vw_powerbi_user_access`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_powerbi_user_access` AS SELECT 
 1 AS `user_email`,
 1 AS `pathologist_id`,
 1 AS `access_role`,
 1 AS `active`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `vw_powerbi_alerts`
--

/*!50001 DROP VIEW IF EXISTS `vw_powerbi_alerts`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_powerbi_alerts` AS select `a`.`id` AS `alert_id`,`a`.`result_id` AS `result_id`,`a`.`patient_id` AS `patient_id`,`p`.`patient_code` AS `patient_code`,concat(`p`.`first_name`,' ',`p`.`last_name`) AS `patient_name`,`p`.`pathologist_id` AS `pathologist_id`,`ph`.`user_id` AS `pathologist_user_id`,coalesce(`u`.`full_name`,concat('Pathologist #',`p`.`pathologist_id`)) AS `pathologist_name`,`ph`.`employee_code` AS `pathologist_employee_code`,`ph`.`registration_no` AS `pathologist_registration_no`,`ph`.`email` AS `pathologist_email`,`ph`.`phone` AS `pathologist_phone`,`ph`.`specialty` AS `pathologist_specialty`,`ph`.`active` AS `pathologist_active`,`lr`.`test_name` AS `test_name`,`lr`.`result_value` AS `result_value`,`lr`.`unit` AS `unit`,`lr`.`ai_score` AS `ai_score`,`a`.`severity` AS `severity`,`a`.`alert_message` AS `alert_message`,`a`.`notification_status` AS `notification_status`,`a`.`acknowledged` AS `acknowledged`,`a`.`acknowledged_by` AS `acknowledged_by`,`a`.`acknowledged_at` AS `acknowledged_at`,`a`.`created_at` AS `created_at`,`a`.`review_due_at` AS `review_due_at`,`a`.`escalated` AS `escalated`,`a`.`reminder_count` AS `reminder_count`,`a`.`last_reminder_at` AS `last_reminder_at`,cast(`a`.`created_at` as date) AS `alert_date`,year(`a`.`created_at`) AS `alert_year`,quarter(`a`.`created_at`) AS `alert_quarter`,month(`a`.`created_at`) AS `alert_month_number`,monthname(`a`.`created_at`) AS `alert_month_name`,date_format(`a`.`created_at`,'%Y-%m') AS `alert_year_month`,date_format(`a`.`created_at`,'%Y-%m-%d') AS `alert_date_key`,dayofmonth(`a`.`created_at`) AS `alert_day_of_month`,dayname(`a`.`created_at`) AS `alert_day_name`,dayofweek(`a`.`created_at`) AS `alert_day_of_week_number`,hour(`a`.`created_at`) AS `alert_hour`,(case when ((`a`.`acknowledged` = true) and (`a`.`acknowledged_at` is not null) and (`a`.`review_due_at` is not null) and (`a`.`acknowledged_at` <= `a`.`review_due_at`)) then 'ACKNOWLEDGED ON TIME' when ((`a`.`acknowledged` = true) and (`a`.`acknowledged_at` is not null) and (`a`.`review_due_at` is not null) and (`a`.`acknowledged_at` > `a`.`review_due_at`)) then 'ACKNOWLEDGED LATE' when (`a`.`acknowledged` = true) then 'ACKNOWLEDGED' when (`a`.`review_due_at` is null) then 'DEADLINE NOT CONFIGURED' when (`a`.`review_due_at` < now()) then 'OVERDUE' when ((`a`.`review_due_at` >= now()) and (`a`.`review_due_at` <= (now() + interval 5 minute))) then 'DUE SOON' else 'ON TIME' end) AS `review_status`,(case when (`a`.`acknowledged` = true) then 'CLOSED' when ((`a`.`review_due_at` is not null) and (`a`.`review_due_at` < now())) then 'OVERDUE' else 'OPEN' end) AS `alert_status`,(case when (upper(`a`.`severity`) = 'CRITICAL') then 15 when (upper(`a`.`severity`) = 'WARNING') then 60 else NULL end) AS `review_target_minutes`,(case when ((`a`.`acknowledged` = true) and (`a`.`acknowledged_at` is not null)) then timestampdiff(MINUTE,`a`.`created_at`,`a`.`acknowledged_at`) else NULL end) AS `review_minutes`,(case when ((`a`.`acknowledged` = true) and (`a`.`acknowledged_at` is not null)) then timestampdiff(SECOND,`a`.`created_at`,`a`.`acknowledged_at`) else NULL end) AS `review_seconds`,(case when ((`a`.`acknowledged` = false) and (`a`.`review_due_at` is not null) and (`a`.`review_due_at` < now())) then timestampdiff(MINUTE,`a`.`review_due_at`,now()) else 0 end) AS `overdue_minutes`,(case when ((`a`.`acknowledged` = false) and (`a`.`review_due_at` is not null) and (`a`.`review_due_at` >= now())) then timestampdiff(MINUTE,now(),`a`.`review_due_at`) else NULL end) AS `minutes_until_due`,(case when ((`a`.`acknowledged` = true) and (`a`.`acknowledged_at` is not null) and (`a`.`review_due_at` is not null) and (`a`.`acknowledged_at` <= `a`.`review_due_at`)) then 'WITHIN TARGET' when ((`a`.`acknowledged` = true) and (`a`.`acknowledged_at` is not null) and (`a`.`review_due_at` is not null) and (`a`.`acknowledged_at` > `a`.`review_due_at`)) then 'OUTSIDE TARGET' when ((`a`.`acknowledged` = false) and (`a`.`review_due_at` is not null) and (`a`.`review_due_at` < now())) then 'OUTSIDE TARGET' when (`a`.`acknowledged` = false) then 'PENDING' else 'UNKNOWN' end) AS `target_performance`,(case when (`a`.`acknowledged` = true) then 1 else 0 end) AS `acknowledged_flag`,(case when (`a`.`acknowledged` = false) then 1 else 0 end) AS `open_flag`,(case when ((`a`.`acknowledged` = false) and (`a`.`review_due_at` is not null) and (`a`.`review_due_at` < now())) then 1 else 0 end) AS `overdue_flag`,(case when ((`a`.`acknowledged` = false) and (`a`.`review_due_at` is not null) and (`a`.`review_due_at` >= now()) and (`a`.`review_due_at` <= (now() + interval 5 minute))) then 1 else 0 end) AS `due_soon_flag`,(case when (`a`.`escalated` = true) then 1 else 0 end) AS `escalated_flag`,(case when (upper(`a`.`severity`) = 'CRITICAL') then 1 else 0 end) AS `critical_flag`,(case when (upper(`a`.`severity`) = 'WARNING') then 1 else 0 end) AS `warning_flag`,(case when (upper(`a`.`notification_status`) = 'EMAIL_SENT') then 1 else 0 end) AS `email_sent_flag`,(case when (upper(`a`.`notification_status`) = 'EMAIL_FAILED') then 1 else 0 end) AS `email_failed_flag`,(case when ((`a`.`acknowledged` = true) and (`a`.`acknowledged_at` is not null) and (`a`.`review_due_at` is not null) and (`a`.`acknowledged_at` <= `a`.`review_due_at`)) then 1 else 0 end) AS `acknowledged_on_time_flag`,(case when ((`a`.`acknowledged` = true) and (`a`.`acknowledged_at` is not null) and (`a`.`review_due_at` is not null) and (`a`.`acknowledged_at` > `a`.`review_due_at`)) then 1 else 0 end) AS `acknowledged_late_flag`,(case when (coalesce(`a`.`reminder_count`,0) > 0) then 1 else 0 end) AS `reminder_sent_flag`,(case when (coalesce(`a`.`reminder_count`,0) >= 3) then 1 else 0 end) AS `reminder_limit_flag`,(case when (`ph`.`active` = true) then 1 else 0 end) AS `active_pathologist_flag` from ((((`alerts` `a` join `patients` `p` on((`p`.`id` = `a`.`patient_id`))) left join `pathologists` `ph` on((`ph`.`id` = `p`.`pathologist_id`))) left join `users` `u` on((`u`.`id` = `ph`.`user_id`))) join `lab_results` `lr` on((`lr`.`id` = `a`.`result_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_powerbi_alerts_history`
--

/*!50001 DROP VIEW IF EXISTS `vw_powerbi_alerts_history`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_powerbi_alerts_history` AS select `pba`.`alert_id` AS `alert_id`,`pba`.`result_id` AS `result_id`,`pba`.`patient_id` AS `patient_id`,`pba`.`patient_code` AS `patient_code`,`pba`.`patient_name` AS `patient_name`,`pba`.`pathologist_id` AS `pathologist_id`,`pba`.`pathologist_user_id` AS `pathologist_user_id`,`pba`.`pathologist_name` AS `pathologist_name`,`pba`.`pathologist_employee_code` AS `pathologist_employee_code`,`pba`.`pathologist_registration_no` AS `pathologist_registration_no`,`pba`.`pathologist_email` AS `pathologist_email`,`pba`.`pathologist_phone` AS `pathologist_phone`,`pba`.`pathologist_specialty` AS `pathologist_specialty`,`pba`.`pathologist_active` AS `pathologist_active`,`pba`.`test_name` AS `test_name`,`pba`.`result_value` AS `result_value`,`pba`.`unit` AS `unit`,`pba`.`ai_score` AS `ai_score`,`pba`.`severity` AS `severity`,`pba`.`alert_message` AS `alert_message`,`pba`.`notification_status` AS `notification_status`,`pba`.`acknowledged` AS `acknowledged`,`pba`.`acknowledged_by` AS `acknowledged_by`,`pba`.`acknowledged_at` AS `acknowledged_at`,`pba`.`created_at` AS `created_at`,`pba`.`review_due_at` AS `review_due_at`,`pba`.`escalated` AS `escalated`,`pba`.`reminder_count` AS `reminder_count`,`pba`.`last_reminder_at` AS `last_reminder_at`,`pba`.`alert_date` AS `alert_date`,`pba`.`alert_year` AS `alert_year`,`pba`.`alert_quarter` AS `alert_quarter`,`pba`.`alert_month_number` AS `alert_month_number`,`pba`.`alert_month_name` AS `alert_month_name`,`pba`.`alert_year_month` AS `alert_year_month`,`pba`.`alert_date_key` AS `alert_date_key`,`pba`.`alert_day_of_month` AS `alert_day_of_month`,`pba`.`alert_day_name` AS `alert_day_name`,`pba`.`alert_day_of_week_number` AS `alert_day_of_week_number`,`pba`.`alert_hour` AS `alert_hour`,`pba`.`review_status` AS `review_status`,`pba`.`alert_status` AS `alert_status`,`pba`.`review_target_minutes` AS `review_target_minutes`,`pba`.`review_minutes` AS `review_minutes`,`pba`.`review_seconds` AS `review_seconds`,`pba`.`overdue_minutes` AS `overdue_minutes`,`pba`.`minutes_until_due` AS `minutes_until_due`,`pba`.`target_performance` AS `target_performance`,`pba`.`acknowledged_flag` AS `acknowledged_flag`,`pba`.`open_flag` AS `open_flag`,`pba`.`overdue_flag` AS `overdue_flag`,`pba`.`due_soon_flag` AS `due_soon_flag`,`pba`.`escalated_flag` AS `escalated_flag`,`pba`.`critical_flag` AS `critical_flag`,`pba`.`warning_flag` AS `warning_flag`,`pba`.`email_sent_flag` AS `email_sent_flag`,`pba`.`email_failed_flag` AS `email_failed_flag`,`pba`.`acknowledged_on_time_flag` AS `acknowledged_on_time_flag`,`pba`.`acknowledged_late_flag` AS `acknowledged_late_flag`,`pba`.`reminder_sent_flag` AS `reminder_sent_flag`,`pba`.`reminder_limit_flag` AS `reminder_limit_flag`,`pba`.`active_pathologist_flag` AS `active_pathologist_flag` from `vw_powerbi_alerts` `pba` where ((`pba`.`created_at` < (now() - interval 7 day)) and (`pba`.`acknowledged_flag` = 1)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_powerbi_alerts_live`
--

/*!50001 DROP VIEW IF EXISTS `vw_powerbi_alerts_live`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_powerbi_alerts_live` AS select `pba`.`alert_id` AS `alert_id`,`pba`.`result_id` AS `result_id`,`pba`.`patient_id` AS `patient_id`,`pba`.`patient_code` AS `patient_code`,`pba`.`patient_name` AS `patient_name`,`pba`.`pathologist_id` AS `pathologist_id`,`pba`.`pathologist_user_id` AS `pathologist_user_id`,`pba`.`pathologist_name` AS `pathologist_name`,`pba`.`pathologist_employee_code` AS `pathologist_employee_code`,`pba`.`pathologist_registration_no` AS `pathologist_registration_no`,`pba`.`pathologist_email` AS `pathologist_email`,`pba`.`pathologist_phone` AS `pathologist_phone`,`pba`.`pathologist_specialty` AS `pathologist_specialty`,`pba`.`pathologist_active` AS `pathologist_active`,`pba`.`test_name` AS `test_name`,`pba`.`result_value` AS `result_value`,`pba`.`unit` AS `unit`,`pba`.`ai_score` AS `ai_score`,`pba`.`severity` AS `severity`,`pba`.`alert_message` AS `alert_message`,`pba`.`notification_status` AS `notification_status`,`pba`.`acknowledged` AS `acknowledged`,`pba`.`acknowledged_by` AS `acknowledged_by`,`pba`.`acknowledged_at` AS `acknowledged_at`,`pba`.`created_at` AS `created_at`,`pba`.`review_due_at` AS `review_due_at`,`pba`.`escalated` AS `escalated`,`pba`.`reminder_count` AS `reminder_count`,`pba`.`last_reminder_at` AS `last_reminder_at`,`pba`.`alert_date` AS `alert_date`,`pba`.`alert_year` AS `alert_year`,`pba`.`alert_quarter` AS `alert_quarter`,`pba`.`alert_month_number` AS `alert_month_number`,`pba`.`alert_month_name` AS `alert_month_name`,`pba`.`alert_year_month` AS `alert_year_month`,`pba`.`alert_date_key` AS `alert_date_key`,`pba`.`alert_day_of_month` AS `alert_day_of_month`,`pba`.`alert_day_name` AS `alert_day_name`,`pba`.`alert_day_of_week_number` AS `alert_day_of_week_number`,`pba`.`alert_hour` AS `alert_hour`,`pba`.`review_status` AS `review_status`,`pba`.`alert_status` AS `alert_status`,`pba`.`review_target_minutes` AS `review_target_minutes`,`pba`.`review_minutes` AS `review_minutes`,`pba`.`review_seconds` AS `review_seconds`,`pba`.`overdue_minutes` AS `overdue_minutes`,`pba`.`minutes_until_due` AS `minutes_until_due`,`pba`.`target_performance` AS `target_performance`,`pba`.`acknowledged_flag` AS `acknowledged_flag`,`pba`.`open_flag` AS `open_flag`,`pba`.`overdue_flag` AS `overdue_flag`,`pba`.`due_soon_flag` AS `due_soon_flag`,`pba`.`escalated_flag` AS `escalated_flag`,`pba`.`critical_flag` AS `critical_flag`,`pba`.`warning_flag` AS `warning_flag`,`pba`.`email_sent_flag` AS `email_sent_flag`,`pba`.`email_failed_flag` AS `email_failed_flag`,`pba`.`acknowledged_on_time_flag` AS `acknowledged_on_time_flag`,`pba`.`acknowledged_late_flag` AS `acknowledged_late_flag`,`pba`.`reminder_sent_flag` AS `reminder_sent_flag`,`pba`.`reminder_limit_flag` AS `reminder_limit_flag`,`pba`.`active_pathologist_flag` AS `active_pathologist_flag` from `vw_powerbi_alerts` `pba` where ((`pba`.`acknowledged_flag` = 0) or (`pba`.`created_at` >= (now() - interval 7 day)) or (`pba`.`acknowledged_at` >= (now() - interval 24 hour))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_powerbi_audit_logs`
--

/*!50001 DROP VIEW IF EXISTS `vw_powerbi_audit_logs`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_powerbi_audit_logs` AS select `al`.`id` AS `audit_id`,`al`.`user_id` AS `user_id`,coalesce(nullif(trim(`al`.`username`),''),'SYSTEM') AS `username`,coalesce(nullif(upper(trim(`al`.`role`)),''),'SYSTEM') AS `role`,upper(trim(`al`.`action_type`)) AS `action_type`,coalesce(nullif(upper(trim(`al`.`entity_type`)),''),'UNSPECIFIED') AS `entity_type`,`al`.`entity_id` AS `entity_id`,coalesce(nullif(trim(`al`.`action_details`),''),'No details recorded.') AS `action_details`,`al`.`created_at` AS `created_at`,cast(`al`.`created_at` as date) AS `audit_date`,cast(`al`.`created_at` as time) AS `audit_time`,year(`al`.`created_at`) AS `audit_year`,quarter(`al`.`created_at`) AS `audit_quarter`,month(`al`.`created_at`) AS `audit_month_number`,monthname(`al`.`created_at`) AS `audit_month_name`,date_format(`al`.`created_at`,'%Y-%m') AS `audit_year_month`,dayofmonth(`al`.`created_at`) AS `audit_day_of_month`,dayname(`al`.`created_at`) AS `audit_day_name`,dayofweek(`al`.`created_at`) AS `audit_day_of_week_number`,hour(`al`.`created_at`) AS `audit_hour`,(case when (upper(`al`.`action_type`) in ('LOGIN','LOGOUT','LOGIN_FAILED')) then 'AUTHENTICATION' when (upper(`al`.`action_type`) like 'UNAUTHORIZED%') then 'SECURITY' when (upper(`al`.`action_type`) in ('ALERT_CREATED','ALERT_ACKNOWLEDGED','EMAIL_ALERT_VIEWED','ALERT_REMINDER_EMAIL_SENT','ALERT_REMINDER_SENT','ALERT_REMINDER_RECORD_FAILED')) then 'ALERT WORKFLOW' when (upper(`al`.`action_type`) in ('EMAIL_SENT','EMAIL_FAILED')) then 'EMAIL' when (upper(`al`.`action_type`) in ('REPORT_PREVIEWED','REPORT_EXPORTED')) then 'REPORTING' when (upper(`al`.`action_type`) = 'RESULT_CAPTURED') then 'LABORATORY RESULT' else 'OTHER' end) AS `event_category`,(case when (upper(`al`.`action_type`) like 'UNAUTHORIZED%') then 'HIGH' when (upper(`al`.`action_type`) in ('LOGIN_FAILED','EMAIL_FAILED','ALERT_REMINDER_RECORD_FAILED')) then 'MEDIUM' else 'INFORMATIONAL' end) AS `event_severity`,(case when (upper(`al`.`action_type`) like '%FAILED%') then 'FAILED' when (upper(`al`.`action_type`) like 'UNAUTHORIZED%') then 'DENIED' else 'SUCCESS' end) AS `event_outcome`,(case when ((`al`.`user_id` is null) or (`al`.`username` is null) or (trim(`al`.`username`) = '')) then 1 else 0 end) AS `system_event_flag`,(case when (upper(`al`.`action_type`) = 'LOGIN') then 1 else 0 end) AS `successful_login_flag`,(case when (upper(`al`.`action_type`) = 'LOGIN_FAILED') then 1 else 0 end) AS `failed_login_flag`,(case when (upper(`al`.`action_type`) = 'LOGOUT') then 1 else 0 end) AS `logout_flag`,(case when (upper(`al`.`action_type`) like 'UNAUTHORIZED%') then 1 else 0 end) AS `unauthorised_event_flag`,(case when (upper(`al`.`action_type`) = 'UNAUTHORIZED_ALERT_ACKNOWLEDGEMENT') then 1 else 0 end) AS `unauthorised_acknowledgement_flag`,(case when (upper(`al`.`action_type`) = 'UNAUTHORIZED_RESULT_FORM_ACCESS') then 1 else 0 end) AS `unauthorised_result_access_flag`,(case when (upper(`al`.`action_type`) = 'UNAUTHORIZED_PATIENT_LOOKUP') then 1 else 0 end) AS `unauthorised_patient_lookup_flag`,(case when (upper(`al`.`action_type`) = 'EMAIL_SENT') then 1 else 0 end) AS `email_sent_flag`,(case when (upper(`al`.`action_type`) = 'EMAIL_FAILED') then 1 else 0 end) AS `email_failed_flag`,(case when (upper(`al`.`action_type`) = 'REPORT_PREVIEWED') then 1 else 0 end) AS `report_preview_flag`,(case when (upper(`al`.`action_type`) = 'REPORT_EXPORTED') then 1 else 0 end) AS `report_export_flag`,(case when (upper(`al`.`action_type`) = 'ALERT_CREATED') then 1 else 0 end) AS `alert_created_flag`,(case when (upper(`al`.`action_type`) = 'ALERT_ACKNOWLEDGED') then 1 else 0 end) AS `alert_acknowledged_flag`,(case when (upper(`al`.`action_type`) = 'EMAIL_ALERT_VIEWED') then 1 else 0 end) AS `email_alert_viewed_flag`,(case when (upper(`al`.`action_type`) in ('ALERT_REMINDER_EMAIL_SENT','ALERT_REMINDER_SENT')) then 1 else 0 end) AS `reminder_event_flag`,(case when (upper(`al`.`action_type`) = 'ALERT_REMINDER_RECORD_FAILED') then 1 else 0 end) AS `reminder_tracking_failure_flag`,(case when (upper(`al`.`action_type`) = 'RESULT_CAPTURED') then 1 else 0 end) AS `result_captured_flag` from `audit_logs` `al` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_powerbi_dates`
--

/*!50001 DROP VIEW IF EXISTS `vw_powerbi_dates`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_powerbi_dates` AS select distinct cast(`alerts`.`created_at` as date) AS `calendar_date`,year(`alerts`.`created_at`) AS `calendar_year`,quarter(`alerts`.`created_at`) AS `calendar_quarter`,month(`alerts`.`created_at`) AS `month_number`,monthname(`alerts`.`created_at`) AS `month_name`,date_format(`alerts`.`created_at`,'%Y-%m') AS `year_months` from `alerts` where (`alerts`.`created_at` is not null) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_powerbi_pathologists`
--

/*!50001 DROP VIEW IF EXISTS `vw_powerbi_pathologists`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_powerbi_pathologists` AS select `ph`.`id` AS `pathologist_id`,`ph`.`user_id` AS `pathologist_user_id`,`u`.`username` AS `pathologist_username`,`u`.`full_name` AS `pathologist_name`,`ph`.`employee_code` AS `employee_code`,`ph`.`registration_no` AS `registration_no`,`ph`.`specialty` AS `specialty`,`ph`.`active` AS `active` from (`pathologists` `ph` join `users` `u` on((`u`.`id` = `ph`.`user_id`))) where (`u`.`role` = 'PATHOLOGIST') */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_powerbi_tests`
--

/*!50001 DROP VIEW IF EXISTS `vw_powerbi_tests`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_powerbi_tests` AS select distinct `lab_results`.`test_name` AS `test_name` from `lab_results` where (`lab_results`.`test_name` is not null) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_powerbi_user_access`
--

/*!50001 DROP VIEW IF EXISTS `vw_powerbi_user_access`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_powerbi_user_access` AS select lower(trim(`powerbi_user_access`.`user_email`)) AS `user_email`,`powerbi_user_access`.`pathologist_id` AS `pathologist_id`,`powerbi_user_access`.`access_role` AS `access_role`,`powerbi_user_access`.`active` AS `active` from `powerbi_user_access` where (`powerbi_user_access`.`active` = true) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-20 22:56:51
