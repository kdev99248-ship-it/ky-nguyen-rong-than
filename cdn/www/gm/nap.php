<?php
/* ===========================================================
 *  GM - Cộng Xu (đơn vị tiền nạp) cho tài khoản
 *
 *  Ví nằm ở nap_card.users:
 *      point       Xu không khoá - mua được mọi gói nạp
 *      point_lock  Xu khoá       - chỉ mua được khi chọn "Xu khoá"
 *
 *  Xu KHÔNG phải tiền trong game - không có gì trong GameServer đọc hai cột
 *  trên. Cộng Xu xong vào game sẽ chưa thấy gì. Xu chỉ thành kim cương khi bị
 *  TIÊU đi, luồng:
 *      trừ Xu -> POST {"action":"recharge","playerid":..,"productid":..}
 *      sang server.url_charge -> GameServer innerRecharge
 *  Vì đó là đường nạp nội bộ thật nên mọi hoạt động nạp trong game đều cộng
 *  tiến độ như nạp tiền thật.
 *  Danh sách gói và server đích nạp bằng db/sql/patch/03-nap-wallet.sql.
 *
 *  Trang này làm cả hai vế:
 *      "Cộng Xu"  - ghi thẳng nap_card.users (mint tiền)
 *      "Mua gói"  - gọi lại REST của webapp nap_card để tiêu Xu
 *
 *  Vế "Mua gói" cố ý KHÔNG tự trừ Xu rồi tự gọi 51011: làm vậy là chép lại
 *  TransferMoneyServiceImpl.transfer() và sẽ lệch khi bản gốc đổi. Gọi lại
 *  endpoint thật thì point_history / package_charge_history / transaction_log
 *  vẫn do một nguồn duy nhất ghi.
 *
 *  Muốn nạp thẳng kim cương cho nhân vật (bỏ qua ví, không tốn Xu) thì dùng
 *  nút 充值 ở index.php.
 *
 *  $conn của config.php nối vào user_center; ở đây dùng tên bảng đầy đủ
 *  nap_card.* để khỏi mở thêm kết nối và khỏi lặp lại mật khẩu DB - giống
 *  cách index.php gọi h_game.t_player.
 * =========================================================== */
include_once 'config.php';

/* index.php không kiểm tra đăng nhập. Trang này mint ra tiền nên tự gác,
 * dùng lại đúng cặp $adminname/$adminpass mà api.php vẫn dùng. */
if (isset($_POST['dnname'])) {
	if ($_POST['dnname'] === $adminname && $_POST['dnpass'] === $adminpass) {
		$_SESSION['user'] = $adminname;
	} else {
		$loginError = 'Sai tài khoản hoặc mật khẩu quản trị.';
	}
}
if (empty($_SESSION['user'])) {
	?>
	<!DOCTYPE html><html><head><meta charset="utf-8"><title>Đăng nhập GM</title>
	<link rel="stylesheet" href="layui/css/layui.css" media="all"></head><body>
	<fieldset class="layui-elem-field layui-field-title" style="margin-top:20px;">
		<legend>Đăng nhập quản trị</legend>
	</fieldset>
	<?php if (!empty($loginError)) { echo '<div class="layui-form-item" style="color:#c00;padding-left:20px;">' . htmlspecialchars($loginError) . '</div>'; } ?>
	<form class="layui-form" method="post" action="">
		<div class="layui-form-item">
			<label class="layui-form-label">Tài khoản</label>
			<div class="layui-input-inline"><input type="text" name="dnname" autocomplete="off" class="layui-input"></div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">Mật khẩu</label>
			<div class="layui-input-inline"><input type="password" name="dnpass" autocomplete="off" class="layui-input"></div>
		</div>
		<div class="layui-form-item"><div class="layui-input-block">
			<input type="submit" class="layui-btn" value="Đăng nhập">
		</div></div>
	</form>
	</body></html>
	<?php
	exit;
}

/* Webapp nap_card chạy dạng WAR trong Tomcat, nên server.port=8888 ở
 * application.properties bị bỏ qua - cổng thật là Connector của
 * runtime/tomcat8.5/conf/server.xml, tức 80. Gọi qua loopback. */
$napcardBuyUrl = 'http://127.0.0.1/api/package-charge/buy';

$msg     = '';
$msgType = '';   // ok | err
$info    = null; // số dư hiện tại của tài khoản vừa thao tác
$chars   = [];   // nhân vật của tài khoản đó, để lấy playerId

/* Đọc 1 tài khoản theo tên. Trả null nếu không có. */
function timTaiKhoan($conn, $userName) {
	$esc = mysqli_real_escape_string($conn, $userName);
	$res = mysqli_query($conn, "SELECT user_id, user_name, point, point_lock
	                            FROM nap_card.users WHERE user_name = '$esc' LIMIT 1");
	if (!$res) {
		return null;
	}
	return mysqli_fetch_assoc($res);
}

/* Nhân vật của một tài khoản. Cùng phép JOIN mà Utils.getByAccount dùng
 * (t_player.accountUid = t_account.uid), chỉ khác là lấy hết chứ không LIMIT 1,
 * vì một tài khoản có thể có nhiều nhân vật và GM phải chọn đúng con.
 * Mới nhất lên đầu để mặc định chọn con vừa chơi. */
function timNhanVat($conn, $account) {
	$esc = mysqli_real_escape_string($conn, $account);
	$res = mysqli_query($conn, "SELECT p.playerId, p.name, p.level, p.lastLoginTime
	                            FROM h_game.t_player p
	                            JOIN h_game.t_account a ON p.accountUid = a.uid
	                            WHERE a.accountId = '$esc'
	                            ORDER BY p.lastLoginTime DESC");
	$out = [];
	if ($res) {
		while ($r = mysqli_fetch_assoc($res)) {
			$out[] = $r;
		}
	}
	return $out;
}

/* config.php chỉ có post_curl. Endpoint mua gói là GET. */
function get_curl($url) {
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($ch, CURLOPT_TIMEOUT, 30);
	$result = curl_exec($ch);
	if (curl_errno($ch)) {
		$result = 'Lỗi gọi API: ' . curl_error($ch);
	}
	curl_close($ch);
	return $result;
}

/* Giá trị nút để ASCII ('add' / 'query') thay vì nhãn tiếng Việt: nhãn có dấu
 * phải khớp từng byte sau khi đi qua trình duyệt, dễ hỏng nếu charset lệch. */
$submit = isset($_POST['act']) ? $_POST['act'] : '';

if ($submit === 'query' || $submit === 'add' || $submit === 'buy') {
	$account = isset($_POST['account']) ? trim($_POST['account']) : '';
	if ($account === '') {
		$msg = 'Chưa nhập tên tài khoản.';
		$msgType = 'err';
	} else {
		$row = timTaiKhoan($conn, $account);
		if (!$row) {
			$msg = 'Không tìm thấy tài khoản "' . htmlspecialchars($account) . '" trong nap_card.users. '
			     . 'Tài khoản chỉ được tạo khi người chơi đăng ký trong game (/user/reg).';
			$msgType = 'err';
		} else {
			$chars = timNhanVat($conn, $row['user_name']);
			if ($submit === 'query') {
				$info = $row;
			} elseif ($submit === 'buy') {
				/* --- Tiêu Xu mua gói: gọi lại REST của webapp nap_card ---
				 * GET /api/package-charge/buy ép cứng typePoint = 0 nên luôn tiêu Xu
				 * KHÔNG khoá. Xu khoá chỉ tiêu được qua POST /buy kèm JWT của chính
				 * người chơi, GM không có token đó. */
				$productId = isset($_POST['productId']) ? intval($_POST['productId']) : 0;
				$srvId     = isset($_POST['srvId']) ? intval($_POST['srvId']) : 0;
				$playerId  = isset($_POST['playerId']) ? trim($_POST['playerId']) : '';

				if ($playerId === '') {
					/* Bỏ trống thì lấy nhân vật đăng nhập gần nhất. */
					if (empty($chars)) {
						$msg = 'Tài khoản "' . htmlspecialchars($account) . '" chưa có nhân vật nào trong h_game.t_player. '
						     . 'Phải vào game tạo nhân vật trước rồi mới mua gói được.';
						$msgType = 'err';
					} else {
						$playerId = $chars[0]['playerId'];
					}
				}

				if ($msgType !== 'err' && $productId <= 0) {
					$msg = 'Chưa chọn gói nạp.';
					$msgType = 'err';
				}
				if ($msgType !== 'err' && $srvId <= 0) {
					$msg = 'Chưa chọn máy chủ. Bảng nap_card.server rỗng thì chạy db/sql/patch/03-nap-wallet.sql.';
					$msgType = 'err';
				}

				if ($msgType !== 'err') {
					$url = $napcardBuyUrl
					     . '?srv_id='    . $srvId
					     . '&role_id='   . urlencode($playerId)
					     . '&money=0'
					     . '&usr='       . urlencode($row['user_name'])
					     . '&productId=' . $productId;
					$resp = trim(get_curl($url));

					/* transfer() trả về chuỗi thuần, không phải JSON. StringHttpMessageConverter
					 * của Spring Boot đời cũ mặc định ISO-8859-1, mà "Thành Công" lại nằm gọn
					 * trong Latin-1 nên không hỏng ở phía Java - chỉ về đây sai bảng mã. Nắn lại
					 * trước khi so chuỗi. preg_match('//u') = phép thử UTF-8 hợp lệ, không cần
					 * mbstring (container chỉ cài mysqli + pdo_mysql). */
					if ($resp !== '' && preg_match('//u', $resp) !== 1) {
						$resp = utf8_encode($resp);
					}

					/* Chỉ câu thành công mới có dấu '|' ("Thành Công| Xu còn: ..."), mọi câu lỗi
					 * đều không có. Dùng ký tự ASCII này để khỏi phụ thuộc dấu tiếng Việt. */
					if (strpos($resp, '|') !== false) {
						$msgType = 'ok';
						$msg = 'Đã nạp vào game cho playerId ' . htmlspecialchars($playerId)
						     . '. ' . htmlspecialchars($resp);
					} else {
						$msgType = 'err';
						$msg = 'API trả về: ' . htmlspecialchars($resp !== '' ? $resp : '(rỗng - Tomcat chưa chạy?)');
						/* Lỗi hay gặp nhất: server.db_password còn nguyên placeholder nên
						 * Utils.getByPlayerId không mở được kết nối vào h_game. */
						if (strpos($resp, 'Không tìm thấy thông tin tài khoản') !== false) {
							$msg .= ' — kiểm tra nap_card.server.db_password đã thay __MYSQL_PASSWORD__ chưa,'
							      . ' và playerId có đúng trong h_game.t_player không.';
						}
					}
					$info  = timTaiKhoan($conn, $row['user_name']);
					$chars = timNhanVat($conn, $row['user_name']);
				}
			} else {
				/* --- Cộng / trừ Xu --- */
				$amount = isset($_POST['amount']) ? intval($_POST['amount']) : 0;
				$isLock = (isset($_POST['ptype']) && $_POST['ptype'] === 'lock');
				$col    = $isLock ? 'point_lock' : 'point';
				$tenVi  = $isLock ? 'Xu khoá' : 'Xu không khoá';

				if ($amount === 0) {
					$msg = 'Số Xu phải khác 0 (số âm để trừ bớt).';
					$msgType = 'err';
				} else {
					$userId = intval($row['user_id']);
					$old    = intval($row[$col]);
					$new    = $old + $amount;
					if ($new < 0) {
						$new = 0;   // không để ví âm
					}
					$delta = $new - $old;

					$ghiChu = isset($_POST['notes']) ? trim($_POST['notes']) : '';
					if ($ghiChu === '') {
						$ghiChu = 'GM ' . ($delta >= 0 ? 'cộng' : 'trừ') . ' ' . $tenVi;
					}
					$ghiChuEsc = mysqli_real_escape_string($conn, $ghiChu);
					$byEsc     = mysqli_real_escape_string($conn, $_SESSION['user']);
					$nameEsc   = mysqli_real_escape_string($conn, $row['user_name']);
					$loai      = $isLock ? 'GM_ADD_LOCK' : 'GM_ADD';

					$ok = mysqli_query($conn, "UPDATE nap_card.users SET `$col` = $new WHERE user_id = $userId");
					if (!$ok) {
						$msg = 'Cập nhật ví thất bại: ' . htmlspecialchars(mysqli_error($conn));
						$msgType = 'err';
					} else {
						/* point_history: user_id, transaction_type, wallet_user_id, new_point
						 * đều NOT NULL và không có giá trị mặc định. Bảng wallet_user không
						 * dùng tới nên wallet_user_id ghi bằng user_id cho hợp lệ. */
						mysqli_query($conn, "INSERT INTO nap_card.point_history
							(user_id, transaction_type, wallet_user_id, old_point, new_point, point,
							 notes, created_by, created_on, account_name)
							VALUES ($userId, '$loai', $userId, $old, $new, $delta,
							        '$ghiChuEsc', '$byEsc', NOW(), '$nameEsc')");

						$msg = 'Xong. ' . htmlspecialchars($row['user_name']) . ': ' . $tenVi .
						       ' ' . number_format($old) . ' -> ' . number_format($new) .
						       ' (' . ($delta >= 0 ? '+' : '') . number_format($delta) . ')';
						$msgType = 'ok';
						$info = timTaiKhoan($conn, $row['user_name']);
					}
				}
			}
		}
	}
}

/* Nạp trước để dùng cho cả dropdown lẫn bảng tham chiếu ở cuối trang.
 *
 * config.php đặt error_reporting(0), nên truy vấn hỏng sẽ lặng lẽ trả về mảng
 * rỗng và dropdown trông y hệt lúc bảng rỗng thật. Giữ lại mysqli_error để phân
 * biệt được hai trường hợp đó - không có nó thì chỉ biết đoán. */
$dbError = '';

$packages = [];
$res = mysqli_query($conn, "SELECT id, price, sycee, title, is_show
                            FROM nap_card.package_charge ORDER BY display_id");
if (!$res) {
	$dbError = 'nap_card.package_charge: ' . mysqli_error($conn);
} else {
	while ($r = mysqli_fetch_assoc($res)) {
		$packages[] = $r;
	}
}

$servers = [];
$res = mysqli_query($conn, "SELECT id, name_server FROM nap_card.server ORDER BY id");
if (!$res) {
	$dbError .= ($dbError !== '' ? ' | ' : '') . 'nap_card.server: ' . mysqli_error($conn);
} else {
	while ($r = mysqli_fetch_assoc($res)) {
		$servers[] = $r;
	}
}

/* Thiếu dữ liệu thì nói thẳng phải chạy lệnh gì, thay vì để dropdown trống. */
$thieuDuLieu = '';
if ($dbError !== '') {
	$thieuDuLieu = 'Lỗi truy vấn DB — ' . $dbError;
} elseif (empty($packages) && empty($servers)) {
	$thieuDuLieu = 'Cả nap_card.package_charge lẫn nap_card.server đều rỗng — chưa nạp bản vá.';
} elseif (empty($packages)) {
	$thieuDuLieu = 'nap_card.package_charge rỗng — không có gói nạp nào để chọn.';
} elseif (empty($servers)) {
	$thieuDuLieu = 'nap_card.server rỗng — không biết nạp vào máy chủ nào.';
}
?>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<title>GM - Xu &amp; gói nạp</title>
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
	<link rel="stylesheet" href="layui/css/layui.css" media="all">
	<script src="layui/layui.all.js" type="text/javascript" charset="utf-8"></script>
</head>
<body>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 20px;">
	<legend>Cộng Xu cho tài khoản</legend>
</fieldset>

<?php if ($msg !== '') { ?>
	<div style="margin: 0 0 15px 20px; padding: 10px 15px; border-radius: 3px;
	            <?php echo $msgType === 'ok' ? 'background:#e8f5e9;color:#2e7d32;' : 'background:#fdecea;color:#c62828;'; ?>">
		<?php echo $msg; ?>
	</div>
<?php } ?>

<?php if ($info) { ?>
	<table class="layui-table" style="width: 620px; margin-left: 20px;">
		<thead><tr><th>Tài khoản</th><th>user_id</th><th>Xu không khoá</th><th>Xu khoá</th></tr></thead>
		<tbody><tr>
			<td><?php echo htmlspecialchars($info['user_name']); ?></td>
			<td><?php echo intval($info['user_id']); ?></td>
			<td><?php echo number_format(intval($info['point'])); ?></td>
			<td><?php echo number_format(intval($info['point_lock'])); ?></td>
		</tr></tbody>
	</table>
<?php } ?>

<?php if ($info && !empty($chars)) { ?>
	<table class="layui-table" style="width: 620px; margin-left: 20px;">
		<thead><tr><th>Nhân vật</th><th>playerId</th><th>Cấp</th><th>Đăng nhập gần nhất</th></tr></thead>
		<tbody>
		<?php foreach ($chars as $i => $c) { ?>
			<tr>
				<td><?php echo htmlspecialchars($c['name']); ?><?php echo $i === 0 ? ' <span class="layui-badge layui-bg-green">mặc định</span>' : ''; ?></td>
				<td><?php echo htmlspecialchars($c['playerId']); ?></td>
				<td><?php echo intval($c['level']); ?></td>
				<td><?php
					$t = intval($c['lastLoginTime']);
					echo $t > 0 ? date('d/m/Y H:i', (int)($t / 1000)) : '-';
				?></td>
			</tr>
		<?php } ?>
		</tbody>
	</table>
<?php } elseif ($info) { ?>
	<div style="margin-left:20px;color:#c62828;">Tài khoản này chưa có nhân vật nào trong game — chưa mua gói được.</div>
<?php } ?>

<form class="layui-form" method="post" action="">
	<div class="layui-form-item">
		<div class="layui-inline">
			<label class="layui-form-label">Tài khoản</label>
			<div class="layui-input-inline">
				<input type="text" name="account" autocomplete="off" class="layui-input"
				       placeholder="tên đăng nhập, không phải tên nhân vật"
				       value="<?php echo isset($_POST['account']) ? htmlspecialchars($_POST['account']) : ''; ?>">
			</div>
			<div class="layui-form-mid layui-word-aux">nap_card.users.user_name</div>
		</div>
	</div>

	<div class="layui-form-item">
		<div class="layui-inline">
			<label class="layui-form-label">Loại Xu</label>
			<div class="layui-input-inline">
				<?php $lockSel = (isset($_POST['ptype']) && $_POST['ptype'] === 'lock'); ?>
				<select name="ptype">
					<option value="free"<?php echo $lockSel ? '' : ' selected'; ?>>Xu không khoá (mua được mọi gói)</option>
					<option value="lock"<?php echo $lockSel ? ' selected' : ''; ?>>Xu khoá (chỉ dùng khi chọn Xu khoá)</option>
				</select>
			</div>
		</div>
	</div>

	<div class="layui-form-item">
		<div class="layui-inline">
			<label class="layui-form-label">Số Xu</label>
			<div class="layui-input-inline">
				<input type="text" name="amount" autocomplete="off" class="layui-input" placeholder="vd 5000000">
			</div>
			<div class="layui-form-mid layui-word-aux">số âm để trừ bớt; ví không xuống dưới 0</div>
		</div>
	</div>

	<div class="layui-form-item">
		<div class="layui-inline">
			<label class="layui-form-label">Ghi chú</label>
			<div class="layui-input-inline">
				<input type="text" name="notes" autocomplete="off" class="layui-input" placeholder="để trống sẽ tự điền">
			</div>
			<div class="layui-form-mid layui-word-aux">lưu vào nap_card.point_history</div>
		</div>
	</div>

	<div class="layui-form-item">
		<div class="layui-input-block">
			<button type="submit" class="layui-btn" name="act" value="add">Cộng Xu</button>
			<button type="submit" class="layui-btn layui-btn-primary" name="act" value="query">Tra cứu</button>
		</div>
	</div>

	<fieldset class="layui-elem-field layui-field-title" style="margin-top: 25px;">
		<legend>Tiêu Xu — nạp thẳng vào game</legend>
	</fieldset>

	<div style="margin: 0 0 15px 20px; color: #666;">
		Cộng Xu ở trên chỉ nạp vào <b>ví web</b>, trong game không thấy gì. Phải mua gói ở đây
		thì Xu mới bị trừ và GameServer mới cộng kim cương + tiến độ các hoạt động nạp.
		Luôn tiêu <b>Xu không khoá</b>. Giữa hai lần mua có khoá 10 giây.
	</div>

	<?php if ($thieuDuLieu !== '') { ?>
		<div style="margin: 0 0 15px 20px; padding: 10px 15px; border-radius: 3px;
		            background:#fff8e1; color:#8d6e00; border-left: 4px solid #f0ad4e; max-width: 700px;">
			<b>Chưa mua gói được.</b> <?php echo htmlspecialchars($thieuDuLieu); ?><br>
			Chạy trong thư mục repo trên VPS rồi tải lại trang:
			<code style="display:block; margin-top:6px;">./knrt.sh db-patch</code>
		</div>
	<?php } ?>

	<div class="layui-form-item">
		<div class="layui-inline">
			<label class="layui-form-label">Máy chủ</label>
			<div class="layui-input-inline">
				<?php
				/* Sinh <option> bằng echo, KHÔNG xuống dòng giữa hai thẻ.
				 * layui.form dựng select thành <input> rồi đổ nguyên innerHTML của
				 * option đang chọn vào thuộc tính value:
				 *     'value="' + (d ? f.html() : "") + '"'
				 * Viết option xuống dòng là value dính cả \n và tab đứng đầu, chữ bị
				 * đẩy khỏi khung và ô trông như trống. Select "ptype" ở trên viết
				 * liền một dòng nên không dính - giữ nguyên kiểu đó ở đây. */
				echo '<select name="srvId">';
				if (empty($servers)) {
					echo '<option value="0">-- chưa có máy chủ --</option>';
				}
				foreach ($servers as $s) {
					$sel = (isset($_POST['srvId']) && intval($_POST['srvId']) === intval($s['id'])) ? ' selected' : '';
					/* Escape name_server riêng rồi mới ghép id: nếu để chung, name_server
					 * lệch bảng mã sẽ làm htmlspecialchars trả '' cho CẢ chuỗi -> option
					 * rỗng -> ô trống (xem giải thích ở select "productId"). Ghép rời thì
					 * id (ASCII) luôn hiện, chỉ phần tên hỏng mới thay bằng chú thích. */
					$ten = htmlspecialchars((string)$s['name_server']);
					if (trim($ten) === '') {
						$ten = '(tên máy chủ trống/lỗi mã hoá)';
					}
					echo '<option value="' . intval($s['id']) . '"' . $sel . '>'
					   . intval($s['id']) . ' - ' . $ten
					   . '</option>';
				}
				echo '</select>';
				?>
			</div>
		</div>
	</div>

	<div class="layui-form-item">
		<div class="layui-inline">
			<label class="layui-form-label">Gói nạp</label>
			<div class="layui-input-inline">
				<?php
				/* Không xuống dòng giữa các <option> - xem giải thích ở select "srvId". */
				echo '<select name="productId">';
				if (empty($packages)) {
					echo '<option value="0">-- chưa có gói nạp --</option>';
				}
				foreach ($packages as $p) {
					$sel = (isset($_POST['productId']) && intval($_POST['productId']) === intval($p['id'])) ? ' selected' : '';
					/* Chỉ in title: 9 gói của 03-nap-wallet.sql đã ghi sẵn giá Xu trong
					 * tên ("10.000 Xu - 1.000 Kim Cương"). Bảng cuối trang có giá đầy đủ.
					 *
					 * layui đổ innerHTML của <option> đang chọn thẳng vào value của ô
					 * hiển thị, nên option rỗng = ô trông như TRỐNG dù gói có thật (đúng
					 * lỗi "DB có data mà select không hiện, cũng không báo lỗi"). Có hai
					 * đường ra option rỗng, khác với lỗi xuống-dòng đã sửa:
					 *   1) package_charge.title là NULL/'' trong DB.
					 *   2) title về sai bảng mã (không phải UTF-8 hợp lệ) -> htmlspecialchars
					 *      mặc định ENT_HTML401+UTF-8 NUỐT cả chuỗi, trả '' và không cảnh báo
					 *      (error_reporting(0)).
					 * Nhãn dự phòng dựng từ số (id/price/sycee - luôn ASCII hợp lệ) để ô
					 * không bao giờ rỗng; title tốt thì vẫn in nguyên như cũ. */
					$label = htmlspecialchars((string)$p['title']);
					if (trim($label) === '') {
						$label = 'Gói ' . intval($p['id']) . ' - ' . number_format(intval($p['price'])) . ' Xu';
						if (intval($p['sycee']) > 0) {
							$label .= ' / ' . number_format(intval($p['sycee'])) . ' KC';
						}
						$label = htmlspecialchars($label);
					}
					echo '<option value="' . intval($p['id']) . '"' . $sel . '>' . $label . '</option>';
				}
				echo '</select>';
				?>
			</div>
		</div>
	</div>

	<div class="layui-form-item">
		<div class="layui-inline">
			<label class="layui-form-label">playerId</label>
			<div class="layui-input-inline">
				<input type="text" name="playerId" autocomplete="off" class="layui-input"
				       placeholder="để trống = nhân vật mới chơi nhất"
				       value="<?php echo isset($_POST['playerId']) ? htmlspecialchars($_POST['playerId']) : ''; ?>">
			</div>
			<div class="layui-form-mid layui-word-aux">bấm "Tra cứu" để xem danh sách nhân vật</div>
		</div>
	</div>

	<div class="layui-form-item">
		<div class="layui-input-block">
			<button type="submit" class="layui-btn layui-btn-danger" name="act" value="buy">Mua gói ngay</button>
		</div>
	</div>
</form>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 10px;">
	<legend>Gói nạp mua bằng Xu</legend>
</fieldset>

<?php if (empty($packages)) { ?>
	<div style="margin-left:20px;color:#c62828;">nap_card.package_charge đang rỗng -
	chạy db/sql/patch/03-nap-wallet.sql thì người chơi mới tiêu Xu được.</div>
<?php } else { ?>
	<table class="layui-table" style="width: 620px; margin-left: 20px;">
		<thead><tr><th>id gói</th><th>Giá (Xu)</th><th>Kim cương</th><th>Tên gói</th><th>Hiện</th></tr></thead>
		<tbody>
		<?php foreach ($packages as $r) { ?>
			<tr>
				<td><?php echo intval($r['id']); ?></td>
				<td><?php echo number_format(intval($r['price'])); ?></td>
				<td><?php echo number_format(intval($r['sycee'])); ?></td>
				<td><?php echo htmlspecialchars($r['title']); ?></td>
				<td><?php echo intval($r['is_show']) ? 'có' : 'không'; ?></td>
			</tr>
		<?php } ?>
		</tbody>
	</table>
<?php } ?>

<div style="margin: 15px 0 40px 20px;">
	<a href="index.php" class="layui-btn layui-btn-primary layui-btn-sm">Về trang GM chính</a>
</div>

<script>
layui.use(['form'], function () { layui.form; });
</script>

</body>
</html>
