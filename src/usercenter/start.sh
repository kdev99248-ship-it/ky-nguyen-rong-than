#!/bin/sh
# UserCenter - đăng nhập, danh sách server, ban/gag  (Jetty :9000 -> DB user_center)
# Bản Linux của start.bat. Chạy được cả trong container lẫn trên bare metal.
#
# PHẢI bật cái này TRƯỚC GameServer: ngay lúc khởi động GameServer POST
# {"serverId":1011} sang /center/getServerInfo.do ở đây để lấy chuỗi kết nối của cả 4
# DB game và gamePort/httpPort/rechargePort. Không có nó thì GameServer exit(-1).
set -e
cd "$(dirname "$0")"

if [ -n "${JAVA_HOME:-}" ]; then
	JAVA="$JAVA_HOME/bin/java"
else
	JAVA=java
fi

mkdir -p logs bak/work bak/error

# Xem chú thích trong src/gameserver/start.sh về -Dfile.encoding và -Duser.timezone.
exec "$JAVA" \
	-Xms512m -Xmn512m -Xss256k \
	-Dfile.encoding=UTF-8 \
	-Duser.timezone="${TZ:-Asia/Ho_Chi_Minh}" \
	-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./oom.hprof \
	-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC \
	-XX:MaxGCPauseMillis=50 -XX:GCPauseIntervalMillis=200 -XX:SurvivorRatio=6 \
	-Xloggc:logs/gc.log \
	-classpath "./*:./UserCenter_lib/*" \
	com.playmore.http.jetty.SpringContextLoader
