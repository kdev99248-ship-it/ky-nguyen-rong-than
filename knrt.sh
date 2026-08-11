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

  db-install            Nạp db/sql/install/ — server sạch, GHI ĐÈ cả 6 database
  db-shell [-e "SQL"]   Client mysql
  db-dump [file.sql]    Dump cả 6 database ra file
  db-import <file.sql>  Nạp một file dump (dùng để chuyển dữ liệu thật từ Windows sang)

Dịch vụ: mysql, usercenter, gameserver, napcard, php, nginx
EOF
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

		  Lần đầu chạy: database còn trống, nạp bằng  ./knrt.sh db-install
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

	help|-h|--help) usage ;;
	*) usage; exit 1 ;;
esac
