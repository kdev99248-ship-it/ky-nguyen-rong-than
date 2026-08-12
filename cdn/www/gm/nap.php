<?php
/* ===========================================================
 *  GM - Cộng Xu (đơn vị tiền nạp) cho tài khoản
 *
 *  Ví nằm ở nap_card.users:
 *      point       Xu không khoá - mua được mọi gói nạp
 *      point_lock  Xu khoá       - chỉ mua được khi chọn "Xu khoá"
 *
 *  Người chơi tiêu Xu ở webapp nap_card, luồng:
 *      trừ Xu -> POST {"action":"recharge","playerid":..,"productid":..}
 *      sang server.url_charge -> GameServer innerRecharge
 *  nên mọi hoạt động nạp trong game đều cộng tiến độ như nạp tiền thật.
 *  Danh sách gói và server đích nạp bằng db/sql/patch/03-nap-wallet.sql.
 *
 *  Trang này CHỈ cộng/trừ Xu, không gọi API game. Muốn nạp thẳng kim cương
 *  cho nhân vật (bỏ qua ví) thì dùng nút 充值 ở index.php.
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

$msg     = '';
$msgType = '';   // ok | err
$info    = null; // số dư hiện tại của tài khoản vừa thao tác

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

/* Giá trị nút để ASCII ('add' / 'query') thay vì nhãn tiếng Việt: nhãn có dấu
 * phải khớp từng byte sau khi đi qua trình duyệt, dễ hỏng nếu charset lệch. */
$submit = isset($_POST['act']) ? $_POST['act'] : '';

if ($submit === 'query' || $submit === 'add') {
	$account = isset($_POST['account']) ? trim($_POST['account']) : '';
	if ($account === '') {
		$msg = 'Chưa nhập tên tài khoản.';
		$msgType = 'err';
	} else {
		$row = timTaiKhoan($conn, $account);
		if (!$row) {
			$msg = 'Không tìm thấy tài khoản "' . htmlspecialchars($account) . '" trong nap_card.users.';
			$msgType = 'err';
		} elseif ($submit === 'query') {
			$info = $row;
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
?>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<title>GM - Cộng Xu</title>
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
</form>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 10px;">
	<legend>Gói nạp mua bằng Xu</legend>
</fieldset>

<?php
$res = mysqli_query($conn, "SELECT id, price, sycee, title, is_show
                            FROM nap_card.package_charge ORDER BY display_id");
if (!$res || mysqli_num_rows($res) === 0) {
	echo '<div style="margin-left:20px;color:#c62828;">nap_card.package_charge đang rỗng - '
	   . 'chạy db/sql/patch/03-nap-wallet.sql thì người chơi mới tiêu Xu được.</div>';
} else {
?>
	<table class="layui-table" style="width: 620px; margin-left: 20px;">
		<thead><tr><th>id gói</th><th>Giá (Xu)</th><th>Kim cương</th><th>Tên gói</th><th>Hiện</th></tr></thead>
		<tbody>
		<?php while ($r = mysqli_fetch_assoc($res)) { ?>
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
