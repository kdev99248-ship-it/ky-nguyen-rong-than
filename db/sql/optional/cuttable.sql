-- ----------------------------
-- Procedure structure for pro_cutTable
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_cutTable`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_cutTable`(IN tablename VARCHAR(50))
BEGIN
	SET @tablename = tablename; ###主表表名
	SET @tablecurmonth:=CONCAT(@tablename,'_',(EXTRACT(YEAR_MONTH FROM NOW())));
	SET @database_name = (select database());
	###当月表是否存在
	SET @curTableCount = (select count(*) from information_schema.TABLES where table_schema=@database_name and table_name=@tablecurmonth);	
	SELECT @curTableCount;
	IF @curTableCount=0 THEN###当月表不存在才进行以下操作

		###根据主表创建当月表
		SET @tsql_create=CONCAT('CREATE TABLE IF NOT EXISTS ',@tablecurmonth,' LIKE ', @tablename,';');
		PREPARE stmt_create FROM @tsql_create;
		EXECUTE stmt_create;
		###为防止copy主表时表引擎为MRG_MyISAM
		SET @tsql_alter=CONCAT('alter table ',@tablecurmonth,' ENGINE=MyISAM DEFAULT CHARSET=utf8;');
		PREPARE stmt_alter FROM @tsql_alter;
		EXECUTE stmt_alter;
		###为防止copy主表时含有数据
		SET @tsql_truncate=CONCAT('truncate table ',@tablecurmonth);
		PREPARE stmt_truncate FROM @tsql_truncate;
		EXECUTE stmt_truncate;
	END IF;
	
	###MERGE当月表
	SET @tsql_merge=CONCAT('ALTER TABLE ',@tablename,' ENGINE=MRG_MyISAM DEFAULT CHARSET=utf8 INSERT_METHOD=LAST UNION=(',@tablecurmonth,');');
	PREPARE stmt_merge FROM @tsql_merge;
	EXECUTE stmt_merge;
END
;;
DELIMITER ;

-- ----------------------------
-- Procedure structure for pro_snapItemCurrent
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_snapItemCurrent`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_snapItemCurrent`()
BEGIN

	SET @snap:=CONCAT('log_item_current_',(EXTRACT(YEAR_MONTH FROM DATE_ADD(NOW(), INTERVAL -1 MONTH))));
	SET @tsql=CONCAT('CREATE TABLE IF NOT EXISTS ',@snap,' SELECT * FROM log_item_current');
	PREPARE stmt FROM @tsql;
	EXECUTE stmt;

END
;;
DELIMITER ;

-- ----------------------------
-- Procedure structure for pro_snapPetCurrent
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_snapPetCurrent`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_snapPetCurrent`()
BEGIN

	SET @snap:=CONCAT('log_pet_current_',(EXTRACT(YEAR_MONTH FROM DATE_ADD(NOW(), INTERVAL -1 MONTH))));
	SET @tsql=CONCAT('CREATE TABLE IF NOT EXISTS ',@snap,' SELECT * FROM log_pet_current');
	PREPARE stmt FROM @tsql;
	EXECUTE stmt;

END
;;
DELIMITER ;

-- ----------------------------
-- Procedure structure for pro_snapRoleCurrent
-- ----------------------------
DROP PROCEDURE IF EXISTS `pro_snapRoleCurrent`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `pro_snapRoleCurrent`()
BEGIN

	SET @snap:=CONCAT('log_role_current_',(EXTRACT(YEAR_MONTH FROM DATE_ADD(NOW(), INTERVAL -1 MONTH))));
	SET @tsql=CONCAT('CREATE TABLE IF NOT EXISTS ',@snap,' SELECT * FROM log_role_current');
	PREPARE stmt FROM @tsql;
	EXECUTE stmt;

END
;;
DELIMITER ;

-- ----------------------------
-- Event structure for event_cutLogTable_new
-- ----------------------------
DROP EVENT IF EXISTS `event_cutLogTable_new`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` EVENT `event_cutLogTable_new` ON SCHEDULE EVERY 1 MONTH STARTS '2017-11-01 00:00:00' ON COMPLETION NOT PRESERVE ENABLE DO BEGIN
	CALL pro_snapItemCurrent();
	COMMIT;
	CALL pro_snapPetCurrent();
	COMMIT;
	CALL pro_snapRoleCurrent();
	COMMIT;
 	CALL pro_cutTable('log_cross_match');
	COMMIT;	
 	CALL pro_cutTable('log_cross_balance');
	COMMIT;	
 	CALL pro_cutTable('log_activitypet');
	COMMIT;	
 	CALL pro_cutTable('log_arena_challenge');
	COMMIT;	
 	CALL pro_cutTable('log_arena_exchange');
	COMMIT;	
 	CALL pro_cutTable('log_bagift');
	COMMIT;	
 	CALL pro_cutTable('log_brave_consume');
	COMMIT;	
 	CALL pro_cutTable('log_brave_gain');
	COMMIT;	
 	CALL pro_cutTable('log_capture');
	COMMIT;	
 	CALL pro_cutTable('log_captured');
	COMMIT;	
 	CALL pro_cutTable('log_diamond_consume');
	COMMIT;	
 	CALL pro_cutTable('log_diamond_gain');
	COMMIT;
 	CALL pro_cutTable('log_eat');
	COMMIT;	
 	CALL pro_cutTable('log_equipment_refine');
	COMMIT;	
 	CALL pro_cutTable('log_equipment_strengthen');
	COMMIT;
 	CALL pro_cutTable('log_gold_consume');
	COMMIT;	
 	CALL pro_cutTable('log_gold_gain');
	COMMIT;	
 	CALL pro_cutTable('log_growplan');
	COMMIT;
 	CALL pro_cutTable('log_guild');
	COMMIT;	
 	CALL pro_cutTable('log_guild_member');
	COMMIT;	
 	CALL pro_cutTable('log_honour_consume');
	COMMIT;
 	CALL pro_cutTable('log_honour_gain');
	COMMIT;	
 	CALL pro_cutTable('log_item_consume');
	COMMIT;	
 	CALL pro_cutTable('log_item_gain');
	COMMIT;
 	CALL pro_cutTable('log_login');
	COMMIT;	
 	CALL pro_cutTable('log_mall');
	COMMIT;	
 	CALL pro_cutTable('log_mission_fight');
	COMMIT;
 	CALL pro_cutTable('log_mission_raid');
	COMMIT;	
 	CALL pro_cutTable('log_mission_reset');
	COMMIT;	
 	CALL pro_cutTable('log_newbie');
	COMMIT;
 	CALL pro_cutTable('log_offline');
	COMMIT;	
 	CALL pro_cutTable('log_peace_mode');
	COMMIT;	
 	CALL pro_cutTable('log_peak_consume');
	COMMIT;
 	CALL pro_cutTable('log_peak_gain');
	COMMIT;	
 	CALL pro_cutTable('log_pet_break');
	COMMIT;	
 	CALL pro_cutTable('log_pet_compound');
	COMMIT;
 	CALL pro_cutTable('log_pet_evolution');
	COMMIT;	
 	CALL pro_cutTable('log_pet_levelup');
	COMMIT;	
 	CALL pro_cutTable('log_pet_off');
	COMMIT;	
 	CALL pro_cutTable('log_pet_on');
	COMMIT;	
 	CALL pro_cutTable('log_prestige_consume');
	COMMIT;	
 	CALL pro_cutTable('log_prestige_gain');
	COMMIT;	
 	CALL pro_cutTable('log_qiling_consume');
	COMMIT;	
 	CALL pro_cutTable('log_qiling_gain');
	COMMIT;	
 	CALL pro_cutTable('log_role_name_change');
	COMMIT;	
 	CALL pro_cutTable('log_role_property');
	COMMIT;	
 	CALL pro_cutTable('log_role_status_change');
	COMMIT;	
 	CALL pro_cutTable('log_rune_inlay');
	COMMIT;	
 	CALL pro_cutTable('log_rune_update');
	COMMIT;	
 	CALL pro_cutTable('log_security');
	COMMIT;	
 	CALL pro_cutTable('log_sign');
	COMMIT;	
 	CALL pro_cutTable('log_skillgrow');
	COMMIT;	
 	CALL pro_cutTable('log_smelt_star');
	COMMIT;	
 	CALL pro_cutTable('log_spar_consume');
	COMMIT;	
 	CALL pro_cutTable('log_spar_gain');
	COMMIT;	
 	CALL pro_cutTable('log_title');
	COMMIT;	
 	CALL pro_cutTable('log_treasure');
	COMMIT;	
	CALL pro_cutTable('log_mail');
	COMMIT;	
 	CALL pro_cutTable('log_bestrong');
	COMMIT;	
	CALL pro_cutTable('log_opera');
	COMMIT;	
 	CALL pro_cutTable('log_skill');
	COMMIT;	
	CALL pro_cutTable('log_trusteeship');
	COMMIT;	
 	CALL pro_cutTable('log_artifact_gs');
	COMMIT;	
	CALL pro_cutTable('log_artifact_levelup');
	COMMIT;	
 	CALL pro_cutTable('log_artifact_syn');
	COMMIT;	
	CALL pro_cutTable('log_equip_awake');
	COMMIT;	
 	CALL pro_cutTable('log_equip_awake_artifice');
	COMMIT;	
	CALL pro_cutTable('log_equip_level_up');
	COMMIT;	
 	CALL pro_cutTable('log_fashion_fumo');
	COMMIT;	
	CALL pro_cutTable('log_homebuff_add');
	COMMIT;	
 	CALL pro_cutTable('log_jumbo_court_exchange');
	COMMIT;	
	CALL pro_cutTable('log_jumbo_court_refresh');
	COMMIT;	
	CALL pro_cutTable('log_monster_train');
	COMMIT;	
	CALL pro_cutTable('log_pet_skill');
	COMMIT;
 	CALL pro_cutTable('log_rune_levelup');
	COMMIT;	
	CALL pro_cutTable('log_world_boss');
	COMMIT; 
	CALL pro_cutTable('log_world_hurt_boss');
	COMMIT;	
	CALL pro_cutTable('log_innerpill');
	COMMIT;
	CALL pro_cutTable('log_petextend');
	COMMIT;	
	CALL pro_cutTable('log_petsoul');
	COMMIT;
	CALL pro_cutTable('log_petsoul_innerpill');
	COMMIT;
	CALL pro_cutTable('log_cheat');
	COMMIT;
	CALL pro_cutTable('log_gf_guild');
	COMMIT;	
	CALL pro_cutTable('log_gf_player');
	COMMIT;	
	CALL pro_cutTable('log_pve');
	COMMIT;
	CALL pro_cutTable('log_illegal_process');
	COMMIT;
	CALL pro_cutTable('log_pet_unlock_strategy');
	COMMIT;
	CALL pro_cutTable('log_pet_strategy_starup');
	COMMIT;	
	CALL pro_cutTable('log_pet_strategy_train');
	COMMIT;
	CALL pro_cutTable('log_guild_demise');
	COMMIT;
	CALL pro_cutTable('log_guild_impeach');
	COMMIT;
	CALL pro_cutTable('log_guildfight_sign');
	COMMIT;	
	CALL pro_cutTable('log_send_gift');
	COMMIT;
	CALL pro_cutTable('log_awake');
	COMMIT;
	CALL pro_cutTable('log_place_event');
	COMMIT;
	CALL pro_cutTable('log_place_award');
	COMMIT;
	CALL pro_cutTable('log_god_mission');
	COMMIT;
	CALL pro_cutTable('log_sweep_god_mission');
	COMMIT;
	CALL pro_cutTable('log_six_book');
	COMMIT;
	CALL pro_cutTable('log_guild_boss_killed');
	COMMIT;
	CALL pro_cutTable('log_guild_boss_rank_award');
	COMMIT;
	CALL pro_cutTable('log_guild_boss_hurt_award');
	COMMIT;
	CALL pro_cutTable('log_god_installed');
	COMMIT;
	CALL pro_cutTable('log_wedding');
	COMMIT;
	CALL pro_cutTable('log_baby_abandon');
	COMMIT;
	CALL pro_cutTable('log_baby_feeding');
	COMMIT;
	CALL pro_cutTable('log_baby_gain');
	COMMIT;
	CALL pro_cutTable('log_goldegg');
	COMMIT;
	CALL pro_cutTable('log_fish_pond');
	COMMIT;
	CALL pro_cutTable('log_mount');
	COMMIT;
	CALL pro_cutTable('log_new_mission');
	COMMIT;
	CALL pro_cutTable('log_new_mission_reward');
	COMMIT;
	CALL pro_cutTable('log_guild_garden_plant');
	COMMIT;
	CALL pro_cutTable('log_guild_garden_harvest');
	COMMIT;
	CALL pro_cutTable('log_divorce');
	COMMIT;
	CALL pro_cutTable('log_pet_skill_change');
	COMMIT;
	CALL pro_cutTable('log_cross_mission_apply');
	COMMIT;
	CALL pro_cutTable('log_cross_mission_award');
	COMMIT;
	CALL pro_cutTable('log_cross_mission_change_name');
	COMMIT;
	CALL pro_cutTable('log_artifact_grow');
	COMMIT;
	CALL pro_cutTable('log_cross_arena_fight');
	COMMIT;
	CALL pro_cutTable('log_monopoly');
	COMMIT;
	CALL pro_cutTable('log_arena_monster');
	COMMIT;
	CALL pro_cutTable('log_lucky_money');
	COMMIT;
	CALL pro_cutTable('log_recharge_treasure');
	COMMIT;
	CALL pro_cutTable('log_pet_fly');
	COMMIT;
	CALL pro_cutTable('log_pet_magic_matrix');
	COMMIT;
	CALL pro_cutTable('log_wild_protect_mission');
	COMMIT;
	CALL pro_cutTable('log_wild_protect_mall');
	COMMIT;
	CALL pro_cutTable('log_resource_welfare');
	COMMIT;
	CALL pro_cutTable('log_eat_apple');
	COMMIT;
	CALL pro_cutTable('log_evil_kind_invade');
	COMMIT;
END
;;
DELIMITER ;