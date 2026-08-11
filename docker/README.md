# Chạy server trên Linux (Docker)

Bản port của bộ Windows sang Ubuntu. 6 container thay cho 5 cửa sổ console.
Toàn bộ file `.bat` giữ nguyên — máy Windows vẫn chạy được như cũ.

## Cài và khởi động

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-v2
sudo usermod -aG docker "$USER"      # đăng xuất/đăng nhập lại cho có hiệu lực

cd /srv/knrt-server                  # thư mục repo, đặt đâu cũng được
chmod +x knrt.sh db/sql/install.sh src/gameserver/start.sh src/usercenter/start.sh

./knrt.sh up                         # lần đầu sẽ build image php (~1 phút)
./knrt.sh db-install                 # nạp DB server sạch, gõ YES
./knrt.sh logs gameserver
```

Lần chạy đầu `knrt.sh` tạo `docker/.env` từ `.env.example` và tự điền `id -u` / `id -g`
của bạn. Xem lại `MYSQL_ROOT_PASSWORD` trong đó trước khi làm tiếp.

Đường dẫn gốc **không** còn phải là ASCII như trên Windows — giới hạn đó là của
MySQL 5.6 Win64 và JDK 8 Windows, không áp dụng ở đây.

## Các lệnh

| Lệnh | Việc |
|---|---|
| `./knrt.sh up` | Khởi động theo thứ tự mysql → usercenter → gameserver → napcard → php → nginx |
| `./knrt.sh down` | Dừng tất cả. Compose gửi SIGTERM, MySQL shutdown sạch — không cần `mysqladmin shutdown` như `stop-all.bat` |
| `./knrt.sh restart gameserver` | Khởi động lại một dịch vụ |
| `./knrt.sh logs [dịch-vụ]` | Bám log |
| `./knrt.sh ps` | Trạng thái |
| `./knrt.sh shell gameserver` | Vào trong container |
| `./knrt.sh build` | Build lại image php sau khi sửa `docker/php/` |
| `./knrt.sh db-install` | Nạp `db/sql/install/` — **ghi đè cả 6 database** |
| `./knrt.sh db-shell -e "show databases;"` | Client mysql |
| `./knrt.sh db-dump [file.sql]` | Dump 6 database |
| `./knrt.sh db-import file.sql` | Nạp một file dump |

## Cổng

Giữ nguyên bản đồ cổng của Windows.

| Dịch vụ | Cổng | Ra Internet? | Thay cho |
|---|---|---|---|
| MySQL | 3306 | **Không** — `bind-address=127.0.0.1` | `bin\1-mysql.bat` |
| UserCenter | 9000 | Có (client cần) | `bin\2-usercenter.bat` |
| GameServer | 9001 | Có (client cần) | `bin\3-gameserver.bat` |
| GameServer GM api | 51011 | **Nên chặn** — chỉ GM panel gọi qua 127.0.0.1 | |
| NapCard (Tomcat) | 80 | Có | `bin\4-napcard.bat` |
| nginx → `cdn/www` | 81, 88 | Có | `bin\5-web.bat` (Apache) |
| php-fpm | 9100 | **Không** — `listen.allowed_clients=127.0.0.1` | mod_php trong Apache |

`network_mode: host` nghĩa là **Docker không quản cổng** — không có `-p`, và các
quy tắc iptables mà Docker thường tự thêm cũng không có. Firewall là việc của bạn:

```bash
sudo ufw allow 22,80,81,88,9000,9001/tcp
sudo ufw enable
```
Cổng 51011 cố tình không mở: GM api không có xác thực ngoài whitelist `allow.ips`.

## Vì sao `network_mode: host`

Chuỗi kết nối 4 DB game **không nằm trong file config**. Lúc khởi động GameServer
POST `{"serverId":1011}` sang `http://127.0.0.1:9000/center/getServerInfo.do` của
UserCenter, nhận về một chuỗi base64 chứa `dbGame` / `dbGameData` / `dbGameLog` /
`dbGameGlobalLog` **và cả** `gamePort` / `httpPort` / `rechargePort`
(`ServerConfig.getServerConfigInfo` → `modifyJSON`). Nguồn của mớ đó là bảng
`user_center.t_s_server_config`, hàng `id=1011` (khớp `server-id` trong
`config/server-config.xml`) — tất cả ghi cứng `jdbc:mysql://127.0.0.1:3306/...`.

Chuyển sang bridge network đồng nghĩa phải `UPDATE` hàng chục dòng DB, sửa
`recharge_host`/`web_host`, và đóng gói lại jar. Host network giữ nguyên topology
loopback y hệt Windows: không sửa một dòng config nào.

## Chuyển dữ liệu thật từ Windows sang

`./knrt.sh db-install` cho ra server **sạch** — không account, không nhân vật.
Muốn mang dữ liệu đang chạy sang thì dump ở Windows rồi nạp:

```cmd
db\mysql\bin\mysqldump.exe -uroot -pxpymw.com --default-character-set=utf8 ^
  --databases user_center h_game h_game_data h_game_log h_game_global_log nap_card ^
  --result-file=knrt-full.sql
```

```bash
./knrt.sh db-import knrt-full.sql
```

Đừng chép thẳng `db/mysql/data/` sang: đó là datadir của MySQL 5.6 **Win64**.

## Sau khi nạp DB: phải sửa IP

Bộ `install/` **không** có dòng nào trong `t_s_server_list` — client sẽ thấy danh sách
server rỗng. Thêm server và trỏ về IP public của VPS:

```sql
-- cột address là địa chỉ CLIENT kết nối tới, không phải địa chỉ nội bộ
INSERT INTO t_s_server_list (id, name, mark, address, max_login_count, zone_id)
VALUES (1011, 'S1', 6, '<ip-public>:9001', 5000, 1);
```

`cdn/www/serverlist.php` cũng còn ghi cứng `192.168.1.111:11011` từ chủ cũ — sửa nếu
client của bạn đọc file đó.

## Khác Windows chỗ nào

| Windows | Linux/Docker | Vì sao |
|---|---|---|
| `runtime/jdk1.8.0_181` | `eclipse-temurin:8-jdk` | JDK trong repo là bản Windows. Vẫn **phải là JDK**: GameServer biên dịch `scripts/java` lúc chạy bằng `javax.tools`, trên JRE hàm này trả `null` |
| classpath `a;b;c` | `a:b:c` | dấu ngăn của Linux |
| — | `-Dfile.encoding=UTF-8` | 168 file `scripts/java` là UTF-8; container không set `LANG` nên Java 8 mặc định ASCII → hỏng chuỗi tiếng Trung/Việt lúc biên dịch |
| — | `-Duser.timezone=Asia/Ho_Chi_Minh` | container mặc định UTC → lệch giờ reset ngày và khung giờ hoạt động |
| MySQL 5.6.49 Win64 | image `mysql:5.6` | Giữ 5.6 để khớp `mysql-connector-java-5.0.7.jar` (2007) trong `lib/`. **Đừng lên MySQL 8** — connector đời đó không nói được `caching_sha2_password` |
| `my.ini` basedir/datadir `E:/...` | volume `knrt-mysql-data` | image tự quản datadir |
| Apache 2.4 + mod_php | nginx + php-fpm | `cdn/apache` và `cdn/php` chỉ có `.exe`/`.dll`. README gốc đã để sẵn `cdn/nginx/` cho bản Linux |
| PHP 5.4.45 | php 7.4 | `mysql_*` bị xoá từ PHP 7 → 15 lời gọi trong `gm/config.php` + `gm/index.php` đã chuyển sang `mysqli`. `mysqli` cũng có trong PHP 5.4 nên **bản Windows vẫn chạy** |
| `fastcgi_pass 127.0.0.1:9000` | `:9100` | 9000 trùng cổng UserCenter — lỗi này README gốc đã ghi ở mục "Việc còn lại" |
| `ROOT.xml` docBase `E:/...` | `docker/tomcat/ROOT.xml` | mount đè, bản Windows không đụng tới |
| `F:\hero.txt` trong `HeroStatisticScript` | `logs/hero.txt` | trên Linux `F:\hero.txt` là **tên file hợp lệ** — sẽ tạo file rác trong thư mục server chứ không báo lỗi |

`docker/mysql/my.cnf` giữ nguyên `sql-mode`, `default-storage-engine=MyISAM`,
`character-set-server=utf8`, `max_connections=512` của `my.ini`, và thêm:

- `lower_case_table_names=1` — Windows luôn hành xử như vậy, Linux mặc định phân biệt
  hoa thường. **Chỉ có tác dụng lúc datadir còn trống.** Đổi sau khi đã có dữ liệu là
  hỏng bảng: phải dump → `docker volume rm knrt-mysql-data` → đổi → nạp lại.
- `max_allowed_packet=64M` — mặc định 4M đủ cho bộ `install/` nhưng đứt giữa chừng khi
  nạp `backup-2023-06-10/h_game.sql` (147 MB).

## Sửa code game

Không đổi so với Windows: sửa `.java` trong `src/gameserver/scripts/java/`, GameServer
tự biên dịch lại lúc khởi động (và nạp nóng theo `reloadJavaScriptTime`).

```bash
./knrt.sh restart gameserver
```

`src/gameserver` được mount vào container, container chạy dưới `KNRT_UID`/`KNRT_GID`
của bạn — nên `scriptBin/`, `logs/`, `server.pid` không bị root chiếm.

Nếu sửa file `.sh` trên Windows: coi chừng CRLF. `sh` sẽ báo lỗi kiểu
`$'\r': command not found`. Sửa: `sed -i 's/\r$//' file.sh`.

## Sự cố hay gặp

**`gameserver` chỉ in 3 dòng `log4j:WARN No appenders could be found` rồi tắt ngay**

Đây **không** phải lỗi Linux — bản Windows cũng chết y hệt. GameServer đọc config
runtime bằng đường dẫn file `${user.dir}/config/...`, tức `src/gameserver/config/`,
chứ không phải `app/gameserver/config/` (thư mục sau chỉ nằm trên classpath).
Bộ repo gốc thiếu 3 file trong `config/` nên:

1. `loadLog4JConfig()` tìm `config/log4j_devel.xml` → không thấy → log4j **không có
   appender nào**, mọi `logger.error()` từ đó về sau rơi vào hư không;
2. `loadServerConfig()` tìm `config/server-config.xml` → không thấy → ném exception;
3. constructor bắt exception, `logger.error(...)` (mất tăm) rồi `System.exit(-1)`.

Ba dòng WARN chính là lần đầu tiên logger được gọi — ở đúng cái `catch` báo lỗi thật.

Đã sửa bằng cách chép `server-config.xml`, `messages.xml`, `log4j_devel.xml`,
`log4j_server.xml` từ `app/gameserver/config/` sang `config/`. Nếu clone lại repo mà
gặp triệu chứng này thì chép lại 4 file đó. Kiểm tra nhanh:

```bash
ls src/gameserver/config/server-config.xml src/gameserver/config/log4j_devel.xml
```

Lưu ý `NettyGameServer` **luôn** `System.setProperty("ideDebug","true")`, nên
`isIDEEnvironment()` luôn đúng và nó luôn dùng `log4j_devel.xml` (có ConsoleAppender)
— `log4j_server.xml` thực tế không bao giờ được dùng.

**`gameserver` thoát sau khi đã in log bình thường** — xem `./knrt.sh logs gameserver`.
Thường là chưa nạp DB (`./knrt.sh db-install`), UserCenter chưa lên (GameServer lấy
chuỗi kết nối DB từ nó, xem mục trên), hoặc dùng nhầm image JRE.

**`Primary script unknown` khi vào GM panel** — nginx và php mount `cdn/www` lệch
đường dẫn. Cả hai phải là `/opt/knrt/cdn/www`.

**MySQL không lên, log báo `Different lower_case_table_names settings`** — volume được
tạo từ trước khi có `my.cnf`. Dump, `docker volume rm knrt-mysql-data`, `up`, nạp lại.

**Cổng đã bị chiếm** — host network nên đụng thẳng tiến trình sẵn có trên VPS:
`sudo ss -ltnp | grep -E ':(80|81|3306|9000|9001)\b'`. Ubuntu hay có sẵn apache2/nginx
ở cổng 80.

**VPS ARM (Ampere, Graviton)** — image `mysql:5.6` chỉ có `linux/amd64`. Hoặc bật
qemu (`docker run --privileged tonistiigi/binfmt --install amd64`), hoặc đổi
`MYSQL_IMAGE=mariadb:10.5` trong `.env` rồi kiểm tra kỹ — chưa đối chiếu với
connector 5.0.7.

## Đổi mật khẩu DB

`xpymw.com` đang ghi cứng ở 5 chỗ. Đổi thì phải đổi hết:

1. `docker/.env` → `MYSQL_ROOT_PASSWORD`
2. `src/gameserver/app/gameserver/config/config.properties` → `db.account.password`
3. `src/usercenter/config.properties` → `db.account.password`
4. `src/napcard/WEB-INF/classes/application.properties` → `spring.datasource.password`
5. `cdn/www/gm/config.php` và `cdn/www/login/config.php`
6. Bảng `user_center.t_s_server_config`, cột `db_password` (nguồn cho 4 DB game)

Mục 6 hay bị quên và là lý do GameServer báo lỗi kết nối trong khi UserCenter vẫn chạy.
