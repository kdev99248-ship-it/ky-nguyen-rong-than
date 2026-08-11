#!/bin/sh
# ===========================================================
#  Nạp database cho Kỷ Nguyên Rồng Thần (cài mới / server sạch)
#  Bản Linux của install.bat.
#
#  MySQL phải đang chạy trước:  ./knrt.sh up
#  Gọi gọn nhất qua wrapper:    ./knrt.sh db-install
#
#  Bộ cài này sinh từ DB đang chạy và đã đối chiếu 1:1:
#    - schema khớp 100% (trừ 5 bảng rác _copy đã cố ý loại)
#    - h_game_data: 148/148 bảng khớp đúng số dòng
#  Server sẽ sạch: KHÔNG có account, nhân vật, log.
#
#  CẢNH BÁO: ghi đè toàn bộ 6 database dưới đây.
# ===========================================================
set -eu

SQLDIR="$(cd "$(dirname "$0")" && pwd)/install"

MYSQL_BIN="${MYSQL_BIN:-mysql}"
DB_HOST="${MYSQL_HOST:-127.0.0.1}"
DB_PORT="${MYSQL_PORT:-3306}"
DB_USER="${MYSQL_USER:-root}"
DB_PASS="${MYSQL_PASSWORD:-xpymw.com}"

ASSUME_YES=0
case "${1:-}" in
	--yes|-y) ASSUME_YES=1 ;;
esac

if ! command -v "$MYSQL_BIN" >/dev/null 2>&1; then
	echo "Không tìm thấy client '$MYSQL_BIN'. Chạy qua ./knrt.sh db-install để dùng client trong container." >&2
	exit 1
fi

if [ ! -d "$SQLDIR" ]; then
	echo "Không tìm thấy thư mục $SQLDIR" >&2
	exit 1
fi

echo
echo "  Sẽ GHI ĐÈ các database trên ${DB_HOST}:${DB_PORT}:"
echo "    user_center, h_game, h_game_data,"
echo "    h_game_log, h_game_global_log, nap_card"
echo

if [ "$ASSUME_YES" -eq 0 ]; then
	printf 'Gõ YES rồi Enter để tiếp tục: '
	read -r OK
	if [ "$OK" != "YES" ]; then
		echo "Đã huỷ."
		exit 1
	fi
fi

# MYSQL_PWD tránh cảnh báo "password on the command line is insecure" của MySQL 5.6.
export MYSQL_PWD="$DB_PASS"
run_sql() {
	"$MYSQL_BIN" -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" --default-character-set=utf8 "$@"
}

echo "[0/6] Tạo database..."
run_sql < "$SQLDIR/00-create-databases.sql"

imp() {
	echo "[$1/6] $2  <-  $3"
	run_sql "$2" < "$SQLDIR/$3"
}

imp 1 user_center       01-user_center.sql
imp 2 h_game            02-h_game.sql
imp 3 h_game_data       03-h_game_data.sql
imp 4 h_game_log        04-h_game_log.sql
imp 5 h_game_global_log 05-h_game_global_log.sql
imp 6 nap_card          06-nap_card.sql

echo
echo "  HOÀN TẤT."
echo "  Kiểm tra: ./knrt.sh db-shell -e 'show databases;'"
echo
echo "  File trong optional/ KHÔNG được nạp tự động - xem db/README.md"
echo
