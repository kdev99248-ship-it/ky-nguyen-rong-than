#!/usr/bin/env bash
# Kỷ Nguyên Rồng Thần — điều khiển server trên Linux.
# Thay bin\start-all.bat / stop-all.bat của Windows.
#
#   ./knrt.sh up            khởi động cả 6 container
#   ./knrt.sh down          dừng tất cả (MySQL shutdown sạch, không hỏng InnoDB)
#   ./knrt.sh logs          xem log tất cả  |  ./knrt.sh logs gameserver
#   ./knrt.sh db-install    nạp bộ SQL server sạch
#   ./knrt.sh help          danh sách đầy đủ
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$ROOT/docker"
ENV_FILE="$DOCKER_DIR/.env"

DATABASES=(user_center h_game h_game_data h_game_log h_game_global_log nap_card)

die() { printf '%s\n' "$*" >&2; exit 1; }

# --- docker compose v2 (plugin) hay docker-compose v1 (script rời) ----------
if docker compose version >/dev/null 2>&1; then
	DC=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
	DC=(docker-compose)
else
	die "Không tìm thấy docker compose. Cài: sudo apt install docker.io docker-compose-v2"
fi

# Chạy compose từ trong docker/ để nó tự nhặt .env và giải các đường dẫn ../
dc() { ( cd "$DOCKER_DIR" && "${DC[@]}" "$@" ); }

env_get() {
	[ -f "$ENV_FILE" ] || die "Thiếu $ENV_FILE — chạy ./knrt.sh up một lần để tạo."
	grep -E "^$1=" "$ENV_FILE" | tail -n1 | cut -d= -f2-
}

# Như env_get nhưng khoá không có thì trả chuỗi rỗng thay vì hỏng.
env_get_opt() {
	[ -f "$ENV_FILE" ] || return 0
	grep -E "^$1=" "$ENV_FILE" | tail -n1 | cut -d= -f2- || true
}

env_set() {
	[ -f "$ENV_FILE" ] || return 0
	if grep -qE "^$1=" "$ENV_FILE"; then
		sed -i "s|^$1=.*|$1=$2|" "$ENV_FILE"
	else
		printf '%s=%s\n' "$1" "$2" >> "$ENV_FILE"
	fi
}

ensure_env() {
	[ -f "$ENV_FILE" ] && return 0
	cp "$DOCKER_DIR/.env.example" "$ENV_FILE"
	# Để container ghi file trong src/ dưới đúng UID của bạn, không phải root.
	sed -i -e "s/^KNRT_UID=.*/KNRT_UID=$(id -u)/" \
	       -e "s/^KNRT_GID=.*/KNRT_GID=$(id -g)/" "$ENV_FILE"
	echo "Đã tạo docker/.env  (KNRT_UID=$(id -u), KNRT_GID=$(id -g))."
	echo "Xem lại MYSQL_ROOT_PASSWORD trong đó trước khi chạy tiếp."
	echo
}

# mysql/mysqldump trong container, dùng MYSQL_PWD để khỏi lộ mật khẩu ra ps.
# MYSQL_PASSWORD là biến mà db/sql/install.sh đọc.
mysql_exec() {
	local flags="$1"; shift
	local pass; pass="$(env_get MYSQL_ROOT_PASSWORD)"
	dc exec $flags -e MYSQL_PWD="$pass" -e MYSQL_PASSWORD="$pass" mysql "$@"
}

confirm() {
	local answer
	printf '%s ' "$1"
	read -r answer
	[ "$answer" = "YES" ] || die "Đã huỷ."
}

usage() {
	cat <<'EOF'
Cách dùng: ./knrt.sh <lệnh> [tham số]

  up                    Khởi động tất cả (mysql -> usercenter -> gameserver -> napcard -> php -> nginx)
  down                  Dừng tất cả. MySQL nhận SIGTERM và shutdown sạch.
  restart [dịch-vụ]     Khởi động lại tất cả, hoặc một dịch vụ
  ps                    Trạng thái container
  logs [dịch-vụ]        Bám log (Ctrl-C để thoát)
  build                 Build lại image php sau khi sửa docker/php/
  shell <dịch-vụ>       Mở shell trong container

  db-install            Nạp db/sql/install/ — server sạch, GHI ĐÈ cả 6 database.
                        Ghi đè cả user_center nên tự chạy lại setup-ip ở cuối.
  db-shell [-e "SQL"]   Client mysql
  db-dump [file.sql]    Dump cả 6 database ra file
  db-import <file.sql>  Nạp một file dump (dùng để chuyển dữ liệu thật từ Windows sang)
  db-patch              Áp db/sql/patch/ lên DB đang chạy — vá chỗ bộ dump gốc
                        thiếu dữ liệu. db-install đã tự chạy; chỉ cần gọi tay sau
                        db-import hoặc khi DB đã nạp từ trước.

  setup-ip [ip-public]  Ghi hàng t_s_server_list và trỏ GameServer về đúng IP.
                        Không có hàng đó GameServer exit 255. Bỏ trống tham số thì
                        dùng lại KNRT_PUBLIC_IP trong docker/.env, không có thì tự dò.

Dịch vụ: mysql, usercenter, gameserver, napcard, php, nginx
EOF
}

# Bộ install/ không có hàng nào trong t_s_server_list, mà GameServer thì không
# chạy được nếu thiếu: lúc khởi động nó POST {"serverId":N} sang
# /center/getServerInfo.do và lấy về CHUỖI KẾT NỐI CỦA CẢ 4 DB GAME từ hàng đó
# (cột db_game/db_game_data/db_game_log/db_game_global_log). Thiếu hàng ->
# ServerConfig chỉ log "远程数据库取服务器配置失败" rồi đi tiếp với 4 config = null
# -> DBFactory NPE -> "load GameData fail!" -> exit 255 -> lặp vô hạn.
#
# Chỗ khó thấy: GetServerConfigInfoHandler còn kiểm tra
#     t_s_server_list.address.startsWith(<IP nguồn của request>)
# nên cột address (địa chỉ client kết nối tới, phải là IP public) và IP mà
# GameServer dùng để gọi UserCenter phải TRÙNG NHAU. Vì vậy hàm này sửa luôn
# <user-center> trong server-config.xml thành IP public thay vì 127.0.0.1.
setup_ip() {
	ensure_env
	local cfg ip_addr server_id pass game_port http_port recharge_port jdbc_args
	cfg="$ROOT/src/gameserver/config/server-config.xml"
	[ -f "$cfg" ] || die "Không thấy $cfg"

	ip_addr="${1:-}"
	# Không truyền IP thì lấy lại IP của lần setup-ip trước (nhớ trong docker/.env),
	# vì db-install xoá sạch user_center nên hàm này hay phải chạy lại.
	if [ -z "$ip_addr" ]; then
		ip_addr="$(env_get_opt KNRT_PUBLIC_IP)"
		if [ -n "$ip_addr" ]; then
			echo "Dùng lại KNRT_PUBLIC_IP trong docker/.env: $ip_addr"
		fi
	fi
	if [ -z "$ip_addr" ]; then
		# src của route ra internet = địa chỉ kernel gắn cho gói đi ra.
		# Trên VPS được cấp IP thẳng thì đó chính là IP public.
		ip_addr="$(ip route get 1.1.1.1 2>/dev/null | sed -n 's/.* src \([0-9.]*\).*/\1/p' | head -n1)"
		[ -n "$ip_addr" ] || die "Không tự dò được IP. Chạy: ./knrt.sh setup-ip <ip-public>"
		echo "Tự dò được IP: $ip_addr"
	fi
	printf '%s' "$ip_addr" | grep -Eq '^([0-9]{1,3}\.){3}[0-9]{1,3}$' \
		|| die "IP không hợp lệ: $ip_addr"

	# Nếu IP không nằm trên interface nào thì máy không tự gọi vào chính nó qua
	# IP đó được (cloud NAT 1:1 kiểu AWS/GCP/Oracle). Xem docker/README.md.
	if command -v ip >/dev/null 2>&1 && ! ip -4 -o addr | grep -q " inet $ip_addr/"; then
		echo
		echo "  CẢNH BÁO: $ip_addr không có trên interface nào của máy này."
		echo "  Máy chắc đang sau NAT 1:1 -> GameServer sẽ không gọi được UserCenter"
		echo "  qua IP này. Xem mục 'VPS sau NAT' trong docker/README.md."
		echo
	fi

	server_id="$(sed -n 's/.*<server-id value="\([0-9]*\)".*/\1/p' "$cfg" | head -n1)"
	[ -n "$server_id" ] || die "Không đọc được <server-id> trong $cfg"
	pass="$(env_get MYSQL_ROOT_PASSWORD)"

	# game_port phải khớp cổng trong address (client đọc address để kết nối).
	# http_port 21011 vì GameServer mở thêm http_port+30000 = 51011 cho GM API,
	# và cdn/www/gm/config.php ghi cứng 51011.
	# recharge_port 9880 khớp http.server.recharge.host trong
	# src/gameserver/app/gameserver/config/config.properties.
	game_port=9001
	http_port=21011
	recharge_port=9880
	# Tham số JDBC lấy nguyên từ db.account.jdbcUrl của config.properties.
	# Cả chuỗi JSON phải <= 256 ký tự: cột db_game* là varchar(256).
	jdbc_args='?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&useServerPrepStmts=true&rewriteBatchedStatements=true'
	db_json() {
		printf '{"url":"jdbc:mysql://127.0.0.1:3306/%s%s","username":"root","password":"%s"}' \
			"$1" "$jdbc_args" "$pass"
	}

	echo "server-id=$server_id  address=$ip_addr:$game_port"

	mysql_exec -T mysql -uroot --default-character-set=utf8 user_center <<-SQL
	INSERT INTO t_s_server_list
	  (id, name, mark, address, max_login_count, zone_id, open_time,
	   game_port, http_port, recharge_port, area_id,
	   db_game, db_game_data, db_game_log, db_game_global_log,
	   max_connect, server_timezone, is_test)
	VALUES
	  ($server_id, 'S1', 6, '$ip_addr:$game_port', 5000, 1, NOW(),
	   $game_port, $http_port, $recharge_port, 1,
	   '$(db_json h_game)', '$(db_json h_game_data)',
	   '$(db_json h_game_log)', '$(db_json h_game_global_log)',
	   5000, 'GMT+7:00', 0)
	ON DUPLICATE KEY UPDATE
	  address=VALUES(address), game_port=VALUES(game_port),
	  http_port=VALUES(http_port), recharge_port=VALUES(recharge_port),
	  area_id=VALUES(area_id), db_game=VALUES(db_game),
	  db_game_data=VALUES(db_game_data), db_game_log=VALUES(db_game_log),
	  db_game_global_log=VALUES(db_game_global_log),
	  max_connect=VALUES(max_connect), server_timezone=VALUES(server_timezone);
	SQL

	sed -i "s|\(<user-center value=\"\)[^\"]*\(\"\)|\1http://$ip_addr:9000\2|" "$cfg"
	echo "Đã sửa <user-center> -> http://$ip_addr:9000"

	# Nhớ lại để db-install tự khôi phục hàng này sau khi ghi đè user_center.
	env_set KNRT_PUBLIC_IP "$ip_addr"

	# Phải restart usercenter: ServerInfoManagerImpl giữ danh sách server trong
	# RAM, chỉ nạp lại lúc khởi động hoặc khi sửa qua API GM. UPDATE tay bằng
	# SQL thì tiến trình đang chạy không thấy gì cả.
	echo "Khởi động lại usercenter (nó cache danh sách server trong RAM) rồi gameserver..."
	dc up -d --force-recreate usercenter gameserver
	echo
	echo "Xong. Theo dõi:  ./knrt.sh logs gameserver"
}

cmd="${1:-help}"
shift || true

case "$cmd" in

	up)
		ensure_env
		dc up -d "$@"
		echo
		dc ps
		cat <<-EOF

		  GM panel : http://<ip-vps>:81/gm/
		  Nạp thẻ  : http://<ip-vps>/
		  Log      : ./knrt.sh logs gameserver

		  Lần đầu chạy: database còn trống ->  ./knrt.sh setup-ip <ip-public>
		                                  ->  ./knrt.sh db-install
		               (db-install ghi đè user_center nên nó tự chạy lại setup-ip
		                ở cuối; thiếu hàng t_s_server_list là gameserver exit 255)
		EOF
		;;

	down)
		# Không cần mysqladmin shutdown như stop-all.bat: compose gửi SIGTERM,
		# entrypoint của image mysql shutdown sạch rồi mới thoát.
		dc down "$@"
		;;

	restart) dc restart "$@" ;;
	ps)      dc ps "$@" ;;
	logs)    dc logs -f --tail=200 "$@" ;;
	build)   dc build "$@" ;;

	shell)
		[ $# -ge 1 ] || die "Thiếu tên dịch vụ. Ví dụ: ./knrt.sh shell gameserver"
		svc="$1"; shift
		dc exec "$svc" sh -c 'command -v bash >/dev/null && exec bash; exec sh'
		;;

	db-install)
		echo
		echo "  Sẽ GHI ĐÈ 6 database: ${DATABASES[*]}"
		echo "  Sau khi nạp: KHÔNG còn account, nhân vật, log nào."
		echo
		confirm "Gõ YES rồi Enter để tiếp tục:"
		mysql_exec -T sh /sql/install.sh --yes

		# user_center bị ghi đè -> hàng t_s_server_list do setup-ip ghi BIẾN MẤT,
		# và GameServer quay lại chết "load GameData fail!" + exit 255. Cái bẫy này
		# đã sập một lần rồi, nên chạy lại luôn thay vì chỉ nhắc.
		echo
		echo "  db-install vừa xoá hàng t_s_server_list -> chạy lại setup-ip."
		if [ -n "$(env_get_opt KNRT_PUBLIC_IP)" ] || ip route get 1.1.1.1 >/dev/null 2>&1; then
			setup_ip ""
		else
			echo
			echo "  *** CHƯA XONG: phải chạy  ./knrt.sh setup-ip <ip-public>  ***"
			echo "  Không có bước đó gameserver sẽ lặp 'exited with code 255'."
			echo
		fi
		;;

	db-shell)
		mysql_exec "" mysql -uroot --default-character-set=utf8 "$@"
		;;

	db-dump)
		out="${1:-$ROOT/backup-$(date +%Y%m%d-%H%M%S).sql}"
		echo "Đang dump 6 database -> $out"
		mysql_exec -T mysqldump -uroot --default-character-set=utf8 \
			--databases "${DATABASES[@]}" > "$out"
		echo "Xong: $(du -h "$out" | cut -f1)  $out"
		;;

	db-import)
		[ $# -ge 1 ] || die "Thiếu file. Ví dụ: ./knrt.sh db-import backup.sql"
		[ -f "$1" ] || die "Không thấy file: $1"
		echo
		echo "  Sẽ nạp $1 vào MySQL, ghi đè các database có trong file."
		confirm "Gõ YES rồi Enter để tiếp tục:"
		mysql_exec -T mysql -uroot --default-character-set=utf8 < "$1"
		echo "Xong."
		;;

	# db-install đã tự chạy patch/ rồi. Lệnh này dành cho DB nạp từ trước, hoặc
	# DB khôi phục bằng db-import từ bản dump Windows — những bản đó chưa có vá.
	db-patch)
		found=0
		for f in "$ROOT"/db/sql/patch/*.sql; do
			[ -e "$f" ] || break
			found=1
			echo "  [patch] $(basename "$f")"
			mysql_exec -T mysql -uroot --default-character-set=utf8 < "$f"
		done
		[ "$found" -eq 1 ] || die "Không có file nào trong db/sql/patch/"
		echo
		echo "Xong. Các bản vá đều idempotent, chạy lại không sao."
		echo "Khởi động lại gameserver để nó dựng lại:  ./knrt.sh restart gameserver"
		;;

	# Xem chú thích dài ở hàm setup_ip() phía trên.
	# Không truyền IP: dùng lại KNRT_PUBLIC_IP trong docker/.env, không có thì tự dò.
	setup-ip)
		setup_ip "${1:-}"
		;;

	help|-h|--help) usage ;;
	*) usage; exit 1 ;;
esac
