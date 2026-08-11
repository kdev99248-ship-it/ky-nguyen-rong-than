-- ===========================================================
--  Vá: t_arena_airank phải có đủ id 1..32
--
--  TRIỆU CHỨNG
--    GameServer khởi động xong phần DB rồi chết, exit 255, lặp vô hạn:
--
--      ArenaProcessorManager.initializeRanking:150 - 竞技场排行数据为空,开始生成机器人数据
--      ArenaProcessorManager.initializeRanking:203 - 竞技场排行榜载入失败,服务器关闭
--      java.lang.NullPointerException
--          at game.server.logic.arena.RobotService.generateRobot(RobotService.java:118)
--          at game.server.logic.arena.ArenaProcessorManager.createRobot(...:259)
--
--  NGUYÊN NHÂN
--    Server sạch -> bảng h_game.t_arena_ranking rỗng VÀ h_game.t_player chưa có
--    robot nào, nên ArenaProcessorManager đi nhánh createRobot() để tự sinh bảng
--    xếp hạng đấu trường. Nhánh đó lặp CỨNG id 1..32:
--
--      for (int i = 1; i <= 32; ++i) {
--          t_arena_airankBean b = BeanTemplet.getArenaAirankBean(i);
--          RobotService.generateRobot(b, list);      // dòng đầu: b.getHigh_rank()
--      }
--
--    Bộ dump 03-h_game_data.sql chỉ có id 1..28 (đối chiếu innodb_index_stats của
--    datadir Windows gốc: n_diff_pfx01 = 28, tức bản gốc cũng chỉ có 28 dòng).
--    Đến i=29 thì getArenaAirankBean trả null -> NPE ngay dòng đầu generateRobot
--    -> catch ở initializeRanking gọi System.exit(-1).
--
--    Server Windows cũ không lộ lỗi này vì DB của nó đã có sẵn robot từ lâu, nên
--    initializeRanking đi nhánh "đọc bảng xếp hạng có sẵn", không bao giờ chạy
--    createRobot. Chỉ cài mới từ bộ install/ mới dính.
--
--  CÁCH VÁ
--    Thêm 4 dải 29..32, nối tiếp đúng quy luật của 3 dải cuối (26..28: mỗi dải
--    100 hạng, ai_level=5, formwork=1 — robot yếu nhất, 1 tướng, không trang bị).
--    Bảng xếp hạng đấu trường vì thế dài 1399 hạng thay vì 999.
--    KHÔNG đụng tới id 1..28: cân bằng gốc giữ nguyên.
--
--    t_arena_airank chỉ được đọc ở đúng createRobot() (đã kiểm cả GameServer jar),
--    và createRobot() chỉ chạy đúng một lần lúc dựng server, nên vá này không ảnh
--    hưởng gameplay về sau.
--
--  Cột: id | low_rank | high_rank | ai_level | formwork
--  formwork trỏ sang t_arena_aitemplet.id (bảng đó có id 1..7, nên 1 là hợp lệ).
--
--  Idempotent: chạy lại bao nhiêu lần cũng được.
-- ===========================================================

USE h_game_data;

INSERT INTO `t_arena_airank` (`id`, `low_rank`, `high_rank`, `ai_level`, `formwork`) VALUES
	(29, 1000, 1099, 5, 1),
	(30, 1100, 1199, 5, 1),
	(31, 1200, 1299, 5, 1),
	(32, 1300, 1399, 5, 1)
ON DUPLICATE KEY UPDATE
	`low_rank`  = VALUES(`low_rank`),
	`high_rank` = VALUES(`high_rank`),
	`ai_level`  = VALUES(`ai_level`),
	`formwork`  = VALUES(`formwork`);
