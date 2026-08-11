# Kỷ Nguyên Rồng Thần — Server

Server game Java. MySQL 5.6 + Jetty (nhúng) + Tomcat 8.5 + web/PHP.

Chạy được trên hai nền:

| | Khởi động | Web/PHP | Java |
|---|---|---|---|
| **Windows** (bản gốc) | `bin\start-all.bat` | Apache 2.4 + PHP 5.4 | `runtime/jdk1.8.0_181` |
| **Linux** (Docker) | `./knrt.sh up` | nginx + php-fpm 7.4 | `eclipse-temurin:8-jdk` |

Hai bản dùng chung `src/`, `db/sql/`, `cdn/www/`. Hướng dẫn Linux: **[docker/README.md](docker/README.md)**.

> **Cảnh báo dưới đây chỉ áp dụng cho bản Windows.**
>
> **Đường dẫn gốc phải là ASCII — đừng đổi lại tên có dấu.** MySQL 5.6 và JDK 8 đều không
> đọc được dấu tiếng Việt trong đường dẫn: chúng cắt cụt path ở ký tự có dấu đầu tiên rồi
> báo "Can't change dir" / "could not find java.dll". Toàn bộ config trỏ tới
> `E:\knrt\knrt-server`. Sau khi đổi tên, MySQL 5.6.49 đã khởi động và phục vụ bình thường.

## Cấu trúc

```
knrt-server/
├── knrt.sh       ★ điều khiển bản Linux (up/down/logs/db-*)
├── bin/          Script khởi động Windows (start-all, stop-all, 1..5)
├── docker/       → xem docker/README.md
│   ├── docker-compose.yml   6 container, network_mode: host
│   ├── mysql/    my.cnf (bản Linux của my.ini)
│   ├── php/      Dockerfile php:7.4-fpm + mysqli
│   ├── nginx/    knrt.conf (thay Apache :81)
│   └── tomcat/   ROOT.xml với docBase trong container
├── db/           → xem db/README.md
│   ├── mysql/    MySQL 5.6.49 Win64 (bin, data, my.ini) — chỉ dùng cho Windows
│   ├── sql/      install/ + install.bat + install.sh, optional/, backup-2023-06-10/
│   └── tools/    SQL-Front (GUI quản lý DB)
├── src/          ← Project IntelliJ (knrt-server.iml)
│   ├── gameserver/   logic game
│   ├── usercenter/   đăng nhập, danh sách server, ban/gag
│   └── napcard/      webapp nạp thẻ (Spring Boot, chạy trong Tomcat)
├── cdn/
│   ├── www/      gm/ login/ 7ball/versionZip (bản cập nhật resource client)
│   ├── client/   kynguyenrongthan.apk / .ipa
│   ├── apache/   Apache 2.4 (.exe/.dll — chỉ Windows)
│   ├── php/      PHP 5.4.45 (.dll — chỉ Windows)
│   ├── nginx/    nginx Windows, không dùng. Bản Linux nằm ở docker/nginx/
│   └── tools/    composer, pear
└── runtime/
    ├── jdk1.8.0_181/   JDK Windows. Trên Linux dùng image eclipse-temurin:8-jdk
    └── tomcat8.5/      Java thuần, dùng chung cho cả hai nền (có sẵn bin/*.sh)
```

## Cổng và luồng

Bản đồ cổng giống hệt nhau trên cả hai nền.

| Service | Cổng | Database | Windows | Linux |
|---|---|---|---|---|
| MySQL | 3306 | — | `bin/1-mysql.bat` | container `mysql` |
| UserCenter | 9000 | `user_center` | `bin/2-usercenter.bat` | container `usercenter` |
| GameServer | 9001, GM 51011 | `h_game`, `h_game_data`, `h_game_log`, `h_game_global_log` | `bin/3-gameserver.bat` | container `gameserver` |
| NapCard (Tomcat) | 80 | `nap_card` | `bin/4-napcard.bat` | container `napcard` |
| Web + PHP | 81 (+88) | qua PHP → `user_center`, `nap_card` | `bin/5-web.bat` (Apache) | container `nginx` + `php` |

```
Client (APK/IPA)
  ├─ UserCenter :9000 ──→ user_center
  ├─ GameServer :9001 ──→ h_game*
  ├─ Tomcat     :80   ──→ nap_card        (docBase → src/napcard)
  └─ Apache/nginx :81 ──→ cdn/www         (GM panel, login, CDN resource)
```

**Thứ tự khởi động không tuỳ ý: UserCenter phải lên trước GameServer.** Lúc khởi động
GameServer POST `{"serverId":1011}` sang `http://127.0.0.1:9000/center/getServerInfo.do`
và nhận về (base64) chuỗi kết nối của **cả 4 DB game** cùng `gamePort`/`httpPort`/
`rechargePort` — `ServerConfig.getServerConfigInfo()` → `modifyJSON()`. UserCenter chưa
lên thì GameServer `Connection refused` → `System.exit(-1)`.

Trên Linux mọi container dùng `network_mode: host`, nên `127.0.0.1` giữ nguyên ý nghĩa
như trên Windows. Bắt buộc phải vậy: nguồn của mớ chuỗi kết nối trên là *dữ liệu* bảng
`user_center.t_s_server_config` chứ không phải file config — xem `docker/README.md`.

**Windows** — chạy tất cả: `bin\start-all.bat`, dừng: `bin\stop-all.bat`
(`stop-all` gọi `mysqladmin shutdown` trước khi kill để không hỏng InnoDB.)

**Linux** — chạy: `./knrt.sh up`, dừng: `./knrt.sh down`
(compose gửi SIGTERM, entrypoint của image mysql tự shutdown sạch.)

Cài DB cho server sạch (không account/nhân vật): `db\sql\install.bat` trên Windows,
`./knrt.sh db-install` trên Linux — chi tiết ở `db/README.md`.

## Sửa code

Nguồn Java sửa được duy nhất là **`src/gameserver/scripts/java/`** (167 file, package `logic.*`).
Phần còn lại chỉ có jar đã biên dịch, không kèm source.

- Mở project: IntelliJ → Open → thư mục gốc (`knrt-server.iml` + `.idea/` đã cấu hình sẵn)
- Source root: `src/gameserver/scripts/java`
- SDK: trỏ vào `runtime/jdk1.8.0_181`

### `scriptBin/` là thư mục tạm — không cần giữ, không cần commit

GameServer **tự biên dịch** `scripts/java` lúc chạy. Đường đi trong bytecode:

```
NettyGameServer.initScriptManager()
  └─ ScriptManager.initialize("scripts/java", "./scriptBin")
       ├─ ScriptJavaLoader.initialize()  → buildClassPath()
       │     └─ if (scriptBin.exists()) _deleteScriptPath(scriptBin);   ← XOÁ SẠCH
       │        scriptBin.mkdirs();                                      ← tạo lại rỗng
       └─ loadScriptAll()  → javax.tools.JavaCompiler.getTask(...).call()  ← biên dịch lại
```

Nghĩa là mỗi lần khởi động server đều xoá trắng `scriptBin/` rồi dựng lại từ `.java`.
173 file `.class` cũ (sinh ngày 11/10/2023) đã xoá — server tự tạo lại.

> **Bắt buộc JDK, không chạy được bằng JRE.** Việc biên dịch dùng
> `javax.tools.ToolProvider.getSystemJavaCompiler()`; trên JRE hàm này trả `null` và
> server chết ngay lúc nạp script. `runtime/jdk1.8.0_181` là JDK đầy đủ nên bản Windows ổn;
> bản Linux dùng image `eclipse-temurin:8-jdk` (không phải `-jre`) và `start.sh` kiểm tra
> `javac` trước khi chạy để báo lỗi sớm thay vì chết giữa lúc nạp script.

> **Trên Linux phải có `-Dfile.encoding=UTF-8`** (đã đặt sẵn trong `start.sh`).
> 168 file trong `scripts/java` là UTF-8; container không set `LANG` nên Java 8 lấy
> charset mặc định là ASCII và làm hỏng chuỗi tiếng Trung/Việt ngay lúc biên dịch.

### Về ý định "build thành jar"

Engine **đã** là jar rồi (83 jar trong `lib/`, `app/gameserver/GameServer.0.0.1.jar`,
`framework/UserCenter.jar`) — không có source nên không build lại được.

Riêng `scripts/java` thì **không nên** nhét vào jar: hai chuỗi `"scripts/java"` và
`"./scriptBin"` nằm cứng trong bytecode `NettyGameServer`, đọc theo **thư mục thật** so với
CWD chứ không phải classpath. Đưa vào jar là mất luôn cơ chế nạp nóng (`reloadJavaScriptTime`).
Chính `pom.xml` trong `GameServer.0.0.1.jar` cũng để plugin `build-helper-maven-plugin`
(thứ đáng lẽ thêm `scripts/java` vào source) ở dạng **comment** — tức chủ đích ngay từ đầu
là để thư mục này nằm ngoài jar.

Muốn đóng gói mang đi thì chép nguyên `src/gameserver/` (bỏ `scriptBin/`, `logs/`), giữ
nguyên cấu trúc thư mục, rồi chạy `start.bat` — nó đã `cd /d "%~dp0"` nên CWD luôn đúng.

## Ba bẫy cấu hình

**1. Có hai thư mục config, dùng cho hai mục đích khác nhau — đừng gộp:**

| Thư mục | Nạp kiểu | Chứa |
|---|---|---|
| `src/gameserver/config/` | đường dẫn file `${user.dir}/config/` | `server-config.xml`, `messages.xml`, `log4j_devel.xml`, 3 file `db-game-*.xml` |
| `src/gameserver/app/gameserver/config/` | classpath | `config.properties`, `applicationContext.xml`, `quartz.xml`, `ehcache.xml`, ... |

Sửa `config.properties` thì sửa ở **`app/gameserver/config/`**. (Bản trùng ở `config/` với
port 9000 sai đã được xóa.)

⚠️ **Repo gốc thiếu 3 file trong `config/` và đó là lý do GameServer chết câm** — cả trên
Windows lẫn Linux. `NettyGameServer.loadConfig()` đọc theo đường dẫn file, không phải
classpath:

| Hàm | Đọc file | Có trong repo gốc? |
|---|---|---|
| `loadLog4JConfig()` | `config/log4j_devel.xml` | ❌ thiếu |
| `loadServerConfig()` | `config/server-config.xml` | ❌ thiếu |
| `loadMessageDictionaryConfig()` | `config/messages.xml` | ❌ thiếu |

Thiếu file đầu → log4j **không có appender nào** → mọi `logger.error()` sau đó bay vào hư
không. Thiếu file thứ hai → ném exception → `catch` trong constructor gọi `logger.error()`
(mất tăm) rồi `System.exit(-1)`. Kết quả người dùng nhìn thấy đúng 3 dòng:

```
log4j:WARN No appenders could be found for logger (game.server.NettyGameServer).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
```

rồi tắt, **không một dòng nào nói vì sao**. Đã sửa bằng cách chép 4 file
(`server-config.xml`, `messages.xml`, `log4j_devel.xml`, `log4j_server.xml`) từ
`app/gameserver/config/` sang `config/`. `src/gameserver/start.sh` giờ kiểm tra trước
khi chạy và báo lỗi rõ ràng nếu thiếu.

Chú ý `NettyGameServer` **luôn** `System.setProperty("ideDebug","true")` ở đầu constructor,
nên `MiscUtils.isIDEEnvironment()` luôn trả `true` → luôn dùng `log4j_devel.xml`
(có `ConsoleAppender`, nên log ra cả màn hình). `log4j_server.xml` thực tế là file chết.

**2. Cấu hình 4 DB của GameServer — bản đang chạy nằm ở đâu.** `DBFactory` **không** có
cơ chế dự phòng: mỗi hằng số mang sẵn cờ `fromJar`, `true` thì chỉ đọc classpath, `false`
thì chỉ `new FileInputStream()` (tương đối CWD). Không tìm thấy là hỏng, không thử chỗ khác:

| Tên | Đường dẫn trong bytecode | Bản thật đang được đọc |
|---|---|---|
| `GAME_DB` | `config/db-game-config.xml` | `src/gameserver/config/` (file trên đĩa) |
| `GAME_LOG` | `config/db-game-log.xml` | `src/gameserver/config/` (file trên đĩa) |
| `GAME_GLOBAL_LOG` | `config/db-game-global-log.xml` | `src/gameserver/config/` (file trên đĩa) |
| `GAME_DATA_DB` | `data/db-game-data-config.xml` | **bên trong `lib/ConfigData-0.0.1.jar`** |

Vì vậy thư mục `src/gameserver/data/` **rỗng là đúng** — không phải thiếu file. Cấu hình
`h_game_data` nằm trong jar cùng với `data/bean|dao|container|sqlmap` (149 bảng). Muốn đổi
chuỗi kết nối `h_game_data` thì phải sửa trong jar; đặt file vào `data/` trên đĩa **không có
tác dụng** vì `GAME_DATA_DB` mang `fromJar=true`, tức là chỉ đọc classpath.

Hệ quả: 4 file này là **bản trùng chết**, sửa vào không ảnh hưởng gì (đã đối chiếu md5 giống
hệt bản sống) — nên xoá để khỏi sửa nhầm:
`app/gameserver/config/db-game-config.xml`, `…/db-game-log.xml`,
`…/db-game-global-log.xml`, `config/ehcache.xsd`.

**3. `lib/` đứng trước `app/gameserver/` trong classpath.** Nếu thêm lại jar cùng tên vào
`app/gameserver/`, nó sẽ bị `lib/` che và không có tác dụng.

## Việc còn lại

- [x] Đổi tên thư mục gốc sang `E:\knrt\knrt-server` — MySQL đã chạy được
- [x] Gom bộ SQL cài server sạch → `db/sql/install/`
- [x] GameServer chỉ in 3 dòng `log4j:WARN` rồi tắt — thiếu `config/server-config.xml`,
      `config/messages.xml`, `config/log4j_devel.xml` (xem bẫy số 1). Đã chép sang và
      thêm bước kiểm tra trong `src/gameserver/start.sh`
- [ ] Khởi động và kiểm tra UserCenter / GameServer / Tomcat / Apache
- [ ] Kiểm tra `http.server.recharge.host=127.0.0.1:9880` trong config UserCenter —
      chưa rõ service nào lắng nghe cổng 9880
- [x] `fastcgi_pass 127.0.0.1:9000` trùng cổng UserCenter — bản Linux đã chuyển sang 9100
      (`docker/nginx/knrt.conf` + `docker/php/knrt-fpm.conf`). `cdn/nginx/conf/nginx.conf`
      để nguyên vì không còn dùng tới
- [x] Dọn `db/` — 2.0 GB → 1.1 GB
- [x] Dọn `src/gameserver/scriptBin/` — xoá 173 `.class`, server tự biên dịch lại
- [ ] Xoá 4 file config trùng chết (xem bẫy số 2) — chưa làm, chờ xác nhận
- [x] Viết script `.sh` để chạy trên VPS Linux → `knrt.sh`, `src/*/start.sh`,
      `db/sql/install.sh`, `docker/` — xem `docker/README.md`
- [ ] **Chạy thử bản Docker trên VPS** — mọi thứ đã viết xong nhưng chưa boot lần nào
- [ ] Sau khi `db-install`: thêm dòng vào `t_s_server_list` với IP public của VPS
      (bộ `install/` để bảng này rỗng nên client sẽ không thấy server nào)
