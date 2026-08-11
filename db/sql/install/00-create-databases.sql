-- ============================================================
--  Kỷ Nguyên Rồng Thần - tạo 6 database server cần
--  Charset khớp đúng DB đang chạy: utf8 / utf8_general_ci
--  Chạy trước tất cả các file 01..06
-- ============================================================

CREATE DATABASE IF NOT EXISTS `user_center`       DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS `h_game`            DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS `h_game_data`       DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS `h_game_log`        DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS `h_game_global_log` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS `nap_card`          DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
