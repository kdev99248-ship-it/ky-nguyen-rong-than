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
./knrt.sh setup-ip <ip-public>       # BẮT BUỘC — thiếu là gameserver exit 255
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
| `./knrt.sh db-install` | Nạp `db/sql/install/` — **ghi đè cả 6 database** (đã bao gồm `db-patch`) |
| `./knrt.sh db-shell -e "show databases;"` | Client mysql |
| `./knrt.sh db-dump [file.sql]` | Dump 6 database |
| `./knrt.sh db-import file.sql` | Nạp một file dump |
| `./knrt.sh db-patch` | Áp `db/sql/patch/` lên DB đang chạy — cần sau `db-import` hoặc với DB nạp từ trước |

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

## Sau khi nạp DB: bắt buộc chạy `setup-ip`

```bash
./knrt.sh setup-ip <ip-public>     # bỏ trống thì tự dò qua `ip route get 1.1.1.1`
```

Không có bước này GameServer **không khởi động được** — nó lặp `exited with code 255`.

### Vì sao

Bộ `install/` không có dòng nào trong `t_s_server_list`, mà hàng đó không chỉ để hiện
danh sách server cho client: nó còn là **nơi GameServer lấy chuỗi kết nối của cả 4 DB
game**. Lúc khởi động, `ServerConfig.load()` POST `{"serverId":1011}` sang
`/center/getServerInfo.do`, UserCenter trả về base64 của hàng đó, GameServer đọc 4 cột
`db_game` / `db_game_data` / `db_game_log` / `db_game_global_log` ra `DataBaseConfig`.
`config/db-game-config.xml` chỉ có `${url}` `${username}` `${password}` — giá trị thật
nằm trong DB, không nằm trong file.

Hỏng ở đâu cũng ra cùng một kiểu chết, vì `ServerConfig.load()` **chỉ log rồi đi tiếp**:

```
远程数据库取服务器配置失败 ！ code: 0      <- log, KHÔNG throw
  -> 4 DataBaseConfig = null
  -> DBFactory.getProperties() NPE
  -> t_loadingDao.select() NPE -> "load GameData fail!"
  -> RuntimeException: 加载GameData错误 -> "start server failed" -> exit 255
```

### Cái bẫy: `address` bị dùng cho hai việc

`GetServerConfigInfoHandler` chỉ trả cấu hình khi

```java
findServer.getAddress().startsWith(getIpAddress(request))
```

Nên cột `address` vừa là **địa chỉ client kết nối tới** (phải là IP public), vừa là
**whitelist IP nguồn** của chính GameServer. Để `<user-center value="http://127.0.0.1:9000">`
trong `src/gameserver/config/server-config.xml` thì IP nguồn là `127.0.0.1`, không khớp
`address` = IP public, và log ra:

```
error result : {"errorMsg":"请求ip有误：127.0.0.1"}
```

`setup-ip` sửa cả hai đầu: ghi `address = <ip-public>:9001` và đổi `<user-center>` thành
`http://<ip-public>:9000`, để IP nguồn của request đúng bằng IP public.

### Hàng nó ghi

```sql
INSERT INTO t_s_server_list
  (id, name, mark, address, max_login_count, zone_id, open_time,
   game_port, http_port, recharge_port, area_id,
   db_game, db_game_data, db_game_log, db_game_global_log,
   max_connect, server_timezone, is_test)
VALUES
  (1011, 'S1', 6, '<ip-public>:9001', 5000, 1, NOW(),
   9001, 21011, 9880, 1,
   '{"url":"jdbc:mysql://127.0.0.1:3306/h_game?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&useServerPrepStmts=true&rewriteBatchedStatements=true","username":"root","password":"<MYSQL_ROOT_PASSWORD>"}',
   '... h_game_data ...', '... h_game_log ...', '... h_game_global_log ...',
   5000, 'GMT+7:00', 0);
```

| Cột | Vì sao giá trị đó |
|---|---|
| `game_port` 9001 | cổng socket client, phải khớp cổng ghi trong `address` |
| `http_port` 21011 | GameServer mở thêm `http_port + 30000` = **51011** cho GM API, mà `cdn/www/gm/config.php` ghi cứng 51011 |
| `recharge_port` 9880 | khớp `http.server.recharge.host` trong `src/gameserver/app/gameserver/config/config.properties` |
| `server_timezone` | dạng `GMT+7:00`; để trống thì code mặc định `GMT+8:00` |
| `db_game*` | JSON `{"url","username","password"}`; `setup-ip` lấy mật khẩu từ `MYSQL_ROOT_PASSWORD` trong `docker/.env`. Cột là `varchar(256)` — chuỗi trên dài 198 ký tự, mật khẩu dài hơn ~47 ký tự sẽ bị cắt cụt và JSON hỏng |

### Phải restart UserCenter sau mỗi lần sửa tay

`ServerInfoManagerImpl` giữ danh sách server trong RAM và chỉ `refresh()` lúc khởi động
hoặc khi sửa qua API GM. `UPDATE t_s_server_list` bằng `db-shell` thì tiến trình đang
chạy không thấy gì — `setup-ip` đã tự `up -d --force-recreate usercenter gameserver`.

### VPS sau NAT

`setup-ip` cảnh báo nếu IP public không nằm trên interface nào (`ip -4 addr`). Đó là
kiểu mạng của AWS/GCP/Oracle: NIC mang IP private, IP public được NAT 1:1 bên ngoài, nên
máy không tự gọi vào chính nó qua IP public được. Khi đó phải để nginx chèn header —
`getIpAddress()` đọc `x-forwarded-for` **trước** `getRemoteAddr()`:

```nginx
# docker/nginx/knrt.conf
server {
    listen 127.0.0.1:9002;
    location / {
        proxy_pass http://127.0.0.1:9000;
        proxy_set_header X-Forwarded-For <ip-public>;
    }
}
```

rồi để `<user-center value="http://127.0.0.1:9002" />`. Nhớ thêm `nginx` vào `depends_on`
của `gameserver` trong `docker-compose.yml`, không thì GameServer khởi động trước proxy.

### Còn lại

`cdn/www/serverlist.php` ghi cứng `192.168.1.111:11011` từ chủ cũ — sửa nếu client của
bạn đọc file đó thay vì gọi UserCenter.

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

Nếu thấy `NullPointerException` ở `DBFactory.getProperties` rồi `load GameData fail!` và
`exited with code 255 (restarting)`, cuộn ngược lên tìm dòng này:

```
error result : {"errorMsg":"请求ip有误：127.0.0.1"}
远程数据库取服务器配置失败 ！ code: 0
```

Đó là hàng `t_s_server_list` sai hoặc chưa có — chạy `./knrt.sh setup-ip <ip-public>`,
xem mục "Sau khi nạp DB". Các nguyên nhân còn lại: chưa nạp DB
(`./knrt.sh db-install`), UserCenter chưa lên, hoặc dùng nhầm image JRE.

**`竞技场排行榜载入失败,服务器关闭` → NPE ở `RobotService.generateRobot` → `exit 255`**

Đi được xa hơn lỗi trên: DB đã kết nối (`GameDBOperator starting`, `create 2 GameLine`),
chết ở bước dựng bảng xếp hạng đấu trường.

```
ArenaProcessorManager.initializeRanking:150 - 竞技场排行数据为空,开始生成机器人数据
ArenaProcessorManager.initializeRanking:203 - 竞技场排行榜载入失败,服务器关闭
java.lang.NullPointerException
     at game.server.logic.arena.RobotService.generateRobot(RobotService.java:118)
     at game.server.logic.arena.ArenaProcessorManager.createRobot(...:259)
```

Server sạch nên `t_arena_ranking` rỗng và chưa có robot nào → `createRobot()` tự sinh,
mà nó lặp **cứng** `for (i = 1; i <= 32; i++)` đọc `h_game_data.t_arena_airank` theo id.
Bộ dump gốc chỉ có id 1..28, đến i=29 thì bean là `null` → NPE → `System.exit(-1)`.

Chạy `./knrt.sh db-patch && ./knrt.sh restart gameserver`. Chi tiết ở `db/README.md`,
mục `sql/patch/`.

Cùng lúc đó thường có một NPE nữa ở `logic.guildwar.NotifyRaceCountdownScript` — cái
này **vô hại**, `ScriptManager.call` bắt lại và ghi `call script error!`; nó không phải
lý do container thoát. Đừng đuổi theo nó.

**`dependency failed to start: container knrt-usercenter is unhealthy` nhưng log
usercenter lại kết thúc bằng `SERVER START COMPLETE.....`**

App không sao — **probe hỏng**. Dấu hiệu nhận biết: `up` thất bại sau ~1 giây, trong khi
healthcheck phải mất `start_period 30s + 18×10s ≈ 210s` mới dám kết luận unhealthy. Thất
bại tức thì nghĩa là container đã mang sẵn nhãn unhealthy từ lần chạy trước.

Bản cũ probe bằng `bash -c '</dev/tcp/127.0.0.1/9000'` — đòi image vừa có `bash` vừa có
`bash` biên dịch kèm net-redirections, `eclipse-temurin` không hứa cả hai. Nay đổi sang
đọc thẳng bảng socket của kernel, chỉ cần `grep`:

```
grep -q ':2328 [0-9A-F]*:[0-9A-F]* 0A' /proc/net/tcp /proc/net/tcp6
```

`2328` = 9000 hệ 16, `0A` = LISTEN. Sau khi sửa phải **tạo lại container** thì healthcheck
mới có hiệu lực:

```bash
./knrt.sh down && ./knrt.sh up
```

Muốn biết chính xác probe hỏng vì gì thì xem log của chính nó:

```bash
docker inspect --format '{{json .State.Health}}' knrt-usercenter | python3 -m json.tool
```

Trường `Log[].Output` chứa nguyên văn thông báo lỗi của lệnh probe.

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
