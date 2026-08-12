-- ===========================================================
--  Vá: bật đơn vị tiền nạp (Xu) để mua mọi gói nạp trong game
--
--  MỤC TIÊU
--    Có một loại "tiền nạp" mà GM cộng thẳng cho tài khoản, người chơi dùng nó
--    mua gói nạp, và mọi hoạt động nạp trong game (Quà Nạp Tích Luỹ, Nạp Đơn
--    Siêu Giá Trị, Quà Mỗi Ngày, Thẻ Tháng, Quỹ Trưởng Thành...) đều ăn theo -
--    y như người chơi nạp tiền thật.
--
--  KHÔNG PHẢI VIẾT MỚI - CHỨC NĂNG ĐÃ CÓ SẴN
--    Webapp nap_card đã cài sẵn toàn bộ chuỗi này, chỉ thiếu dữ liệu. Ví nằm ở
--    hai cột của nap_card.users:
--
--      users.point       Xu không khoá  (GM cộng, mua gì cũng được)
--      users.point_lock  Xu khoá        (thưởng sự kiện, dùng hạn chế hơn)
--
--    Luồng tiêu Xu (TransferMoneyServiceImpl.transfer):
--
--      1. Tra gói theo package_charge.id
--      2. Trừ users.point (typePoint 0) hoặc users.point_lock (typePoint 1)
--         đúng bằng package_charge.price
--      3. Ghi point_history (transaction_type 'BUY_GOLD')
--      4. addGoldForUser() POST sang server.url_charge:
--             {"action":"recharge","playerid":"<playerId>","productid":<id gói>}
--      5. GameServer nhận ở /hgame/background_api -> RechargeImpl.innerRecharge
--         -> RechargeService.rechargeForInner(playerId, productId, inStat)
--
--    Bước 5 là đường nạp NỘI BỘ thật sự của server, không phải phát quà tay,
--    nên mọi hoạt động nạp đều cộng tiến độ bình thường. Không phải biên dịch
--    lại Java dòng nào.
--
--  HAI BẢNG BỊ RỖNG LÀM CẢ CHUỖI CHẾT
--    nap_card.server          0 dòng -> findById() trả null -> lỗi ngay bước 2
--    nap_card.package_charge  0 dòng -> "Gói nạp không hợp lệ" ngay bước 1
--
--    Vá này nạp dữ liệu cho đúng hai bảng đó.
--
--  VÌ SAO package_charge.id PHẢI TRÙNG t_recharge.id
--    addGoldForUser gửi thẳng packageCharge.getId() làm productid:
--
--      TransactionRequestDto dto = new TransactionRequestDto(roleId, rechargeId);
--      // rechargeId chính là packageCharge.getId()
--
--    Còn phía game, innerRecharge tra BeanTemplet.getRechargeBean(productId) và
--    ném GMException("不存在的商品id") nếu không thấy. Nên id của package_charge
--    KHÔNG được tự đặt - phải là id có thật trong h_game_data.t_recharge.
--
--    9 id dưới đây là 9 gói "KC" mà nhà vận hành Việt Nam trước đã tự thêm vào
--    t_recharge (id 1-7, 12, 13), và cũng đúng là 9 id mà hoạt động "Nạp Đơn
--    Siêu Giá Trị" trỏ tới trong finishParam ("13,1" ... "1,1"). Dùng đúng bộ
--    này thì cả gói nạp lẫn mốc hoạt động khớp nhau.
--
--  QUY ĐỔI
--    price = số Xu bị trừ. Đặt bằng đúng số KC ghi trên nhãn gói cho dễ hiểu
--    (1 Xu = 1 đồng). Muốn rẻ hơn thì sửa cột price, không đụng id.
--    sycee = số kim cương nhận được, lấy từ t_recharge.diamonds - chỉ để hiển
--    thị, kim cương thật do GameServer cộng ở bước 5.
--    extra_sycee = 0 cố ý: khác 0 sẽ kích hoạt sendMailToUserFirstCharge gửi
--    thêm một request "mail" nữa sang url_charge.
--
--  MẬT KHẨU DB
--    server.db_password để chỗ giữ __MYSQL_PASSWORD__. db/sql/install.sh tự
--    thay bằng $DB_PASS lúc nạp, nên không có mật khẩu thật nào nằm trong repo.
--    Chạy tay ngoài install.sh thì thay nốt:
--      ./knrt.sh db-shell -e "UPDATE nap_card.server SET db_password='<mật khẩu>' WHERE id=1011;"
--
--    server.db_host là JDBC URL đầy đủ chứ không phải tên máy - Utils.getConnection
--    đưa thẳng vào DriverManager.getConnection(). Nó trỏ vào h_game vì
--    Utils.getByPlayerId/getByAccount truy vấn t_player và t_account ở đó.
--
--  GHI CHÚ: trang nạp GM cũ (gm/index.php, nút 充值) đọc dropdown phân khu từ
--    user_center.t_s_server_list. Hàng đó do ./knrt.sh setup-ip tạo với zone_id = 1,
--    và GameServer cũng bắt buộc phải có nó mới khởi động được, nên máy nào chạy
--    được thì dropdown đã có sẵn - không cần vá gì thêm. Trang mới
--    cdn/www/gm/nap.php (cộng Xu) không đụng tới bảng này.
--
--  Idempotent: chạy lại bao nhiêu lần cũng được.
-- ===========================================================

USE nap_card;

-- -----------------------------------------------------------
-- 1) Máy chủ đích của lệnh nạp
--
--    id = 1011 chứ không phải 1: PackageChargeController có
--        GET /api/package-charge/buy?srv_id=..&role_id=..&money=..&usr=..&productId=..
--    và srv_id được đưa thẳng vào serverService.findById(). Client gọi đúng URL
--    dạng này (PayMgr.lua trong APK), truyền server-id 1011. Nếu về sau client
--    gửi số khác thì phải đổi id ở đây cho khớp, nếu không findById trả null.
--
--    Đường GET đó cố định typePoint = 0, tức luôn tiêu Xu KHÔNG KHOÁ.
--    Xu khoá chỉ tiêu được qua POST /api/package-charge/buy (typePoint = 1).
-- -----------------------------------------------------------
INSERT INTO `server`
	(`id`, `name_server`, `server_id`, `active_charge`,
	 `db_host`, `db_user`, `db_password`, `url_charge`, `merge_server`,
	 `created_by`, `created_on`)
VALUES
	(1011, 'S1 - Kỷ Nguyên Rồng Thần', '1011', 1,
	 'jdbc:mysql://127.0.0.1:3306/h_game?useUnicode=true&characterEncoding=utf8&useSSL=false',
	 'root', '__MYSQL_PASSWORD__',
	 'http://127.0.0.1:51011/hgame/background_api', 0,
	 'patch', NOW())
ON DUPLICATE KEY UPDATE
	`name_server`   = VALUES(`name_server`),
	`server_id`     = VALUES(`server_id`),
	`active_charge` = VALUES(`active_charge`),
	`db_host`       = VALUES(`db_host`),
	`db_user`       = VALUES(`db_user`),
	`db_password`   = VALUES(`db_password`),
	`url_charge`    = VALUES(`url_charge`),
	`merge_server`  = VALUES(`merge_server`),
	`updated_by`    = 'patch',
	`updated_on`    = NOW();

-- -----------------------------------------------------------
-- 2) Danh sách gói nạp mua bằng Xu
--    id BẮT BUỘC trùng h_game_data.t_recharge.id (xem phần đầu file).
--
--    id | nhãn t_recharge | price (Xu) | sycee (kim cương)
-- -----------------------------------------------------------
INSERT INTO `package_charge`
	(`id`, `price`, `sycee`, `extra_sycee`, `display_id`, `title`, `is_show`)
VALUES
	(13,     10000,   1000, 0, 1, '10.000 Xu - 1.000 Kim Cương',        1),
	(12,     20000,   2000, 0, 2, '20.000 Xu - 2.000 Kim Cương',        1),
	( 7,     50000,   5000, 0, 3, '50.000 Xu - 5.000 Kim Cương',        1),
	( 6,    100000,  10000, 0, 4, '100.000 Xu - 10.000 Kim Cương',      1),
	( 5,    200000,  20000, 0, 5, '200.000 Xu - 20.000 Kim Cương',      1),
	( 4,    500000,  50000, 0, 6, '500.000 Xu - 50.000 Kim Cương',      1),
	( 3,   1000000, 100000, 0, 7, '1.000.000 Xu - 100.000 Kim Cương',   1),
	( 2,   2000000, 200000, 0, 8, '2.000.000 Xu - 200.000 Kim Cương',   1),
	( 1,   5000000, 500000, 0, 9, '5.000.000 Xu - 500.000 Kim Cương',   1)
ON DUPLICATE KEY UPDATE
	`price`       = VALUES(`price`),
	`sycee`       = VALUES(`sycee`),
	`extra_sycee` = VALUES(`extra_sycee`),
	`display_id`  = VALUES(`display_id`),
	`title`       = VALUES(`title`),
	`is_show`     = VALUES(`is_show`);
