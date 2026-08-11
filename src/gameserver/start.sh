#!/bin/sh
# GameServer - logic game  (Jetty :9001, GM api :51011 -> DB h_game*)
# Bản Linux của start.bat. Chạy được cả trong container lẫn trên bare metal.
set -e
cd "$(dirname "$0")"

# runtime/jdk1.8.0_181 là JDK Windows, vô dụng trên Linux -> lấy java từ PATH.
# Container dùng eclipse-temurin:8-jdk; bare metal: apt install openjdk-8-jdk.
if [ -n "${JAVA_HOME:-}" ]; then
	JAVA="$JAVA_HOME/bin/java"
	JAVAC="$JAVA_HOME/bin/javac"
else
	JAVA=java
	JAVAC=javac
fi

# Bắt buộc JDK, không chạy được bằng JRE: NettyGameServer.initScriptManager()
# biên dịch scripts/java lúc chạy bằng javax.tools.getSystemJavaCompiler(),
# trên JRE hàm này trả null và server chết ngay lúc nạp script.
if ! command -v "$JAVAC" >/dev/null 2>&1; then
	echo "LỖI: không tìm thấy javac. Cần JDK chứ không phải JRE (apt install openjdk-8-jdk)." >&2
	exit 1
fi

# Config runtime đọc theo ĐƯỜNG DẪN FILE ${user.dir}/config/, không phải classpath
# (app/gameserver/config chỉ dùng cho classpath: datasource.xml, config.properties...).
# Thiếu file ở đây thì server chết CÂM: log4j chưa kịp cấu hình nên chính cái
# logger.error báo lỗi cũng bị nuốt, chỉ còn 3 dòng "No appenders could be found".
for f in log4j_devel.xml server-config.xml messages.xml; do
	if [ ! -f "config/$f" ]; then
		echo "LỖI: thiếu config/$f — chép từ app/gameserver/config/ sang:" >&2
		echo "      cp app/gameserver/config/$f config/" >&2
		exit 1
	fi
done

# logback ghi logs/work.log + bak/{work,error}/, JVM ghi logs/gc.log.
mkdir -p logs bak/work bak/error

# -Dfile.encoding: 168 file scripts/java là UTF-8. Thiếu cờ này Java 8 lấy charset
# từ LANG, mà container không set LANG -> ASCII -> hỏng chuỗi tiếng Trung/Việt lúc
# biên dịch script. Windows không cần vì code page mặc định khác.
# -Duser.timezone: container mặc định UTC, lệch giờ reset ngày và khung giờ hoạt động.
exec "$JAVA" \
	-Xms512m -Xmn512m -Xss256k \
	-Dfile.encoding=UTF-8 \
	-Duser.timezone="${TZ:-Asia/Ho_Chi_Minh}" \
	-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./oom.hprof \
	-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC \
	-XX:MaxGCPauseMillis=50 -XX:GCPauseIntervalMillis=200 -XX:SurvivorRatio=6 \
	-Xloggc:logs/gc.log \
	-classpath "./*:./lib/*:./app/gameserver/config:./framework/*:./app/gameserver/*" \
	Main
