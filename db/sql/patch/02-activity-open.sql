-- ===========================================================
--  Vá: mở vĩnh viễn các hoạt động nạp trong t_general_activity_config
--
--  TRIỆU CHỨNG
--    Nạp file db/sql/optional/h_game-activity-seed.sql xong, vào game vẫn
--    không thấy hoạt động nạp nào ngoài 6 mục thường trú (Thẻ Tháng, Quỹ
--    Trưởng Thành, Quà Cấp Độ, Quà Online, Tiếp Tế Bánh Mì, Hoàn Trả).
--
--  NGUYÊN NHÂN
--    Seed là bản dump của server Trung Quốc "万抽版" ngày 03/11/2020. Trong
--    108 dòng thì 102 dòng là sự kiện chạy theo lịch cứng 02/09/2020 ->
--    02/12/2020, tức đã hết hạn từ lâu.
--
--    Cách server đọc thời gian (game/server/logic/operateActivity/
--    OperateActivityService.checkActivityTime):
--
--      timeType = 1  ->  checkActivityTime(startTime, endTime)
--                        so mốc thời gian TUYỆT ĐỐI -> 102 dòng này chết.
--      timeType = 2  ->  checkActivityTime(openingTime)
--                        openingTime tính theo SỐ NGÀY kể từ ngày khai server,
--                        và nếu openingTime = '-1' thì hàm return true ngay,
--                        không kiểm tra gì -> MỞ VĨNH VIỄN.
--
--    LƯU Ý ngược đời: quy ước timeType ở đây NGƯỢC với các bảng
--    ActivityCheckTimeScript (t_extra_drop, t_flash_sale, t_limittime_exchange),
--    bên đó 1 = theo ngày khai server, 2 = mốc tuyệt đối. Đừng suy từ bảng này
--    sang bảng kia.
--
--    May mắn là cả 102 dòng đều đã sẵn openingTime = '-1', nên chỉ cần đổi
--    timeType từ 1 sang 2 là mở vĩnh viễn, không phải sửa gì thêm.
--
--  VÌ SAO KHÔNG BẬT CẢ 102 DÒNG
--    102 dòng đó chỉ là 3 sự kiện lặp lại qua 26 đợt (mỗi 3-4 ngày một đợt),
--    và chỉ có 4 bộ phần thưởng khác nhau:
--
--      累充豪礼  34 dòng -> 1 bộ thưởng duy nhất
--      超值单充  34 dòng -> 1 bộ thưởng duy nhất
--      每日豪礼  34 dòng -> 2 bộ thưởng (17 + 17)
--
--    Bật hết sẽ ra 102 mục trùng nhau trong giao diện. Nên: tắt tất cả, rồi
--    bật lại đúng 1 dòng đại diện cho mỗi sự kiện và đặt tên tiếng Việt.
--    Cột name chỉ dùng để hiển thị (OperateActivityService dòng 613 nạp vào
--    config rồi gửi thẳng cho client), đổi tên không ảnh hưởng logic.
--
--    open = 0 chặn thật, không chỉ là nhãn: checkAndStopOperateActivity bỏ qua
--    config có open = 0 nên start giữ nguyên 0, và handler nhận thưởng cũng
--    chặn ở dòng 818 (open == 0 || start == 0 -> báo "hoạt động đã đóng").
--
--  CẢNH BÁO: resetType KHÔNG HOẠT ĐỘNG
--    Sự kiện "Quà Mỗi Ngày" và "Nạp Đơn" gốc để resetType = 1 (reset theo
--    ngày). Trong bản jar này resetType = 1 KHÔNG có code chạy:
--
--      baseActivityCrossDay()  -> thân hàm rỗng
--      resetActivityData()     -> khai báo nhưng không nơi nào gọi
--      resetPlayerData(player) -> có được gọi trong tick người chơi, nhưng
--                                 bỏ qua ngay nếu resetType != 3
--
--    Tức là các mốc thưởng chỉ nhận được MỘT LẦN, không lặp lại mỗi ngày.
--
--    ĐỪNG chữa bằng cách đổi sang resetType = 3 (cron). Vòng lặp reset có lỗi
--    chỉ số - nó dùng getRewardList().get(i) với i là chỉ số MỐC thay vì chỉ số
--    phần thưởng. Đã đối chiếu dữ liệu thật: cả 3 sự kiện đều có số mốc lớn hơn
--    số phần thưởng mỗi mốc (11 mốc / 3 thưởng, 9 mốc / 4 thưởng, 9 mốc / 3
--    thưởng) nên chắc chắn ném IndexOutOfBoundsException, mà lại ném bên trong
--    tick người chơi. Bản gốc chưa từng dùng resetType = 3 nên nhánh này chưa
--    bao giờ được chạy thử.
--
--  ẢNH BANNER HỎNG
--    Cột icon trỏ sang http://c.qbjlq.51sfsy.com/bt_shop_Res/banner/BT_*.png -
--    tên miền của nhà vận hành cũ, đã chết, và cdn/www không có ảnh thay thế.
--    Banner sẽ không tải được. Tự sửa khi có ảnh:
--      UPDATE t_general_activity_config SET icon = 'http://163.61.73.198:81/banner/BT_7.png' WHERE id = '...';
--
--  ĐIỀU KIỆN CHẠY
--    Vá này chỉ có tác dụng SAU KHI đã nạp db/sql/optional/h_game-activity-seed.sql.
--    install.sh không nạp optional/ nên trên bản cài mới bảng rỗng và toàn bộ
--    UPDATE dưới đây khớp 0 dòng - vô hại, không lỗi.
--
--    Nạp seed rồi chạy lại vá:
--      ./knrt.sh db-shell h_game < db/sql/optional/h_game-activity-seed.sql
--      ./knrt.sh db-shell       < db/sql/patch/02-activity-open.sql
--    Rồi khởi động lại GameServer (config chỉ đọc lúc dựng server).
--
--  GHI CHÚ VỀ SEED
--    t_general_activity_config trong seed KHÔNG có PRIMARY KEY (khác mọi bảng
--    còn lại cùng file), và 24 dòng cuối bị lặp lại - 108 dòng nhưng chỉ 84 id.
--    Không gây lỗi khi nạp, và server nạp config vào map theo id nên bản sau
--    đè bản trước. Các UPDATE dưới đây lọc theo id nên chạm cả hai bản sao với
--    cùng giá trị -> kết quả không đổi. Cố ý không dọn trùng ở đây vì dọn phải
--    DROP/RENAME bảng, rủi ro hơn nhiều so với lợi ích.
--
--  Idempotent: chạy lại bao nhiêu lần cũng được.
-- ===========================================================

USE h_game;

-- -----------------------------------------------------------
-- 1) Tắt toàn bộ 102 dòng sự kiện theo lịch 2020 (đã hết hạn)
-- -----------------------------------------------------------
UPDATE `t_general_activity_config`
	SET `open` = 0
	WHERE `timeType` = 1;

-- -----------------------------------------------------------
-- 2) Bật lại 1 dòng đại diện cho mỗi sự kiện, mở vĩnh viễn
--
--    timeType = 2 + openingTime = '-1'  ->  luôn mở
--    status   = 1                       ->  đang hoạt động
--    Giữ nguyên weight gốc (thứ tự hiển thị) và itemList (phần thưởng).
-- -----------------------------------------------------------

-- Quà Nạp Tích Luỹ (累充豪礼) - activityType 1, cộng dồn tổng kim cương đã nạp.
-- 11 mốc: 20.000 -> 5.000.000 kim cương. resetType 0 = không reset, đúng bản chất
-- "tích luỹ" nên mốc này hoạt động đầy đủ.
UPDATE `t_general_activity_config`
	SET `name`        = 'Quà Nạp Tích Luỹ',
	    `remark`      = 'Thường trú - mở vĩnh viễn',
	    `timeType`    = 2,
	    `openingTime` = '-1',
	    `status`      = 1,
	    `open`        = 1
	WHERE `id` = '65b6a924-748b-4ea6-8f5f-2f2b4d363c0a';

-- Nạp Đơn Siêu Giá Trị (超值单充) - activityType 2, thưởng theo từng lần nạp lẻ.
-- 9 mốc, finishType 47 với finishParam "<id sản phẩm>,<số lần>", trỏ đúng 9 gói
-- KC tiếng Việt trong h_game_data.t_recharge: 13, 12, 7, 6, 5, 4, 3, 2, 1.
-- => package_charge ở 04-nap-wallet.sql phải có đủ 9 id này thì mốc mới tính.
UPDATE `t_general_activity_config`
	SET `name`        = 'Nạp Đơn Siêu Giá Trị',
	    `remark`      = 'Thường trú - mở vĩnh viễn',
	    `timeType`    = 2,
	    `openingTime` = '-1',
	    `status`      = 1,
	    `open`        = 1
	WHERE `id` = '77a56b29-ae51-4998-8d1e-36c556a44fb3';

-- Quà Mỗi Ngày (每日豪礼) - activityType 2, 9 mốc theo tổng kim cương nạp.
-- Đây là bản thưởng thứ hai trong hai bản của seed (khác bản kia đúng 1 vật phẩm).
-- Xem CẢNH BÁO ở đầu file: không reset theo ngày, thực chất nhận một lần.
UPDATE `t_general_activity_config`
	SET `name`        = 'Quà Mỗi Ngày',
	    `remark`      = 'Thường trú - mở vĩnh viễn',
	    `timeType`    = 2,
	    `openingTime` = '-1',
	    `status`      = 1,
	    `open`        = 1
	WHERE `id` = 'cea62b20-67cd-49a7-8411-d823e9b8e88e';

-- Bản thưởng thứ hai của 每日豪礼. Bỏ dấu chú thích nếu muốn thêm một mục nữa
-- (giao diện sẽ có 2 mục "Quà Mỗi Ngày" với phần thưởng lệch nhau 1 vật phẩm).
-- UPDATE `t_general_activity_config`
-- 	SET `name`        = 'Quà Mỗi Ngày II',
-- 	    `remark`      = 'Thường trú - mở vĩnh viễn',
-- 	    `timeType`    = 2,
-- 	    `openingTime` = '-1',
-- 	    `status`      = 1,
-- 	    `open`        = 1
-- 	WHERE `id` = '8e81732e-aaea-4eaf-8c7c-4d2036f32af6';

-- -----------------------------------------------------------
-- 3) Việt hoá tên 6 hoạt động thường trú
--    Nhóm này vốn đã timeType = 2, openingTime = '-1' nên đang mở sẵn;
--    ở đây chỉ đổi tên hiển thị, không đụng thời gian hay phần thưởng.
-- -----------------------------------------------------------
UPDATE `t_general_activity_config` SET `name` = 'Thẻ Tháng'         WHERE `id` = 'aee6d1a9-c282-400e-b606-6693fd880b54';
UPDATE `t_general_activity_config` SET `name` = 'Quỹ Trưởng Thành'  WHERE `id` = 'efff6c4e-bdc2-4e1f-8675-d72086451047';
UPDATE `t_general_activity_config` SET `name` = 'Quà Cấp Độ'        WHERE `id` = '8d86a63a-edbc-4a5b-b5d4-bb01c9a515c9';
UPDATE `t_general_activity_config` SET `name` = 'Quà Online'        WHERE `id` = '5cf62a09-1b62-4497-8398-f5c8be9f54af';
UPDATE `t_general_activity_config` SET `name` = 'Tiếp Tế Bánh Mì'   WHERE `id` = 'cbae1e10-7d89-4ce9-af94-8382a47712ec';
UPDATE `t_general_activity_config` SET `name` = 'Hoàn Trả Nạp'      WHERE `id` = '9b6ae9b8-0d25-44b3-873f-581d3ae082ff';
