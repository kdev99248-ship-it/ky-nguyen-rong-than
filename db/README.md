# db/ — MySQL và toàn bộ SQL

MySQL 5.6.49 Win64 (tách ra từ bộ phpStudy cũ). Không phải service Windows —
chạy như tiến trình thường qua `bin\1-mysql.bat`, port **3306**, `root` / `xpymw.com`.

```
db/
├── mysql/          MySQL 5.6.49
│   ├── bin/        28 file .exe (đã bỏ bản debug + binary test)
│   ├── data/       6 database đang chạy
│   ├── lib/        libmysql.dll (client)
│   ├── share/      bảng mã + thông báo lỗi
│   └── my.ini      config đang dùng  ← basedir/datadir trỏ E:/knrt/knrt-server/db/mysql/
├── sql/
│   ├── install/    ★ bộ cài server sạch (xem dưới)
│   ├── install.bat   chạy cả bộ install/ theo đúng thứ tự
│   ├── backup-2023-06-10/   snapshot dữ liệu thật ngày 10/06/2023
│   └── optional/   không nạp tự động
└── tools/
    └── sql-front/  GUI xem/sửa DB
```

## 6 database server cần

| Database | Nội dung | Ai dùng |
|---|---|---|
| `user_center` | tài khoản, danh sách server, ban/gag | UserCenter :9000, GM panel |
| `h_game` | dữ liệu động: nhân vật, mail, guild | GameServer :9001 |
| `h_game_data` | cấu hình tĩnh: hero, item, shop, drop, stage | GameServer (chỉ đọc) |
| `h_game_log` | log trong game | GameServer |
| `h_game_global_log` | log toàn cục | GameServer |
| `nap_card` | webapp nạp thẻ (Spring Boot + Liquibase) | Tomcat :80 |

## sql/install/ — bộ cài server sạch

Sinh trực tiếp bằng `mysqldump` từ 6 DB đang chạy, **không phải** từ các bản dump cũ.
Đã đối chiếu ngược 1:1 với DB thật:

| File | Nội dung | Đối chiếu |
|---|---|---|
| `00-create-databases.sql` | tạo 6 DB, utf8 / utf8_general_ci | — |
| `01-user_center.sql` | 21 bảng + view `v_server_info`, kèm data 7 bảng cấu hình server | 167/167 cột ✓ |
| `02-h_game.sql` | 45 bảng, **rỗng** | 319 cột (bỏ `t_mail_copy`) ✓ |
| `03-h_game_data.sql` | 148 bảng + **toàn bộ** cấu hình game | 1337 cột, 148/148 bảng khớp số dòng ✓ |
| `04-h_game_log.sql` | 11 bảng, **rỗng** | 176/176 cột ✓ |
| `05-h_game_global_log.sql` | 2 bảng, **rỗng** | 19/19 cột ✓ |
| `06-nap_card.sql` | 26 bảng + data cấu hình + sổ Liquibase | 230/230 cột ✓ |

Kết quả: server chạy được ngay, **không có** account / nhân vật / log nào.

```
bin\1-mysql.bat        (mở cửa sổ riêng, để nguyên)
db\sql\install.bat     (gõ YES để xác nhận)
```

### 5 bảng rác đã cố ý loại khỏi bộ cài

Do người vận hành tạo tay để backup nhanh, không có trong code:
`h_game.t_mail_copy`, `h_game_data.t_gacha_consume_copy`, `h_game_data.t_global_`,
`h_game_data.t_shop_table_copy`, `h_game_data.t_shop_table_copy1`.
Chúng vẫn còn trong DB đang chạy — chỉ không được tái tạo khi cài mới.

## sql/optional/ — phải nạp tay nếu cần

| File | Nội dung |
|---|---|
| `h_game-activity-seed.sql` | 112 dòng cấu hình hoạt động 2020 (4 dòng `t_custom_package`, 108 dòng `t_general_activity_config`). **DB đang chạy để 8 bảng này rỗng** — nạp vào là bật thêm tính năng, khác với server hiện tại. |
| `cuttable.sql` | 4 stored procedure cắt bảng log theo tháng (`pro_cutTable`). Không có code nào gọi, `h_game_log` cũng chưa hề chia bảng theo tháng. Để dành khi cần xoay vòng log. |

## Vì sao không dùng lại các bản dump cũ

Đã kiểm tra từng bản trước khi bỏ:

- Bộ `full/*.sql` là **của năm 2020**. `h_game_data` trong đó **thiếu 224 cột** so với DB
  hiện tại (`t_artifact.icon_1/2/3`, `t_equip.desc`, `t_crossrealm.*`…) và cấu hình đã lệch
  hẳn (`t_shop_goods` 757 vs 901, `t_item` 972 vs 1032, `t_drop` 1197 vs 1238). Cài bằng
  bộ đó sẽ ra shop/drop/item sai.
- `backup-2023-06-10/h_game_data.sql` **thiếu 30 cột** (`t_hero_relate.Reel_*`,
  `t_head_portrait.sorts`…).
- Chỉ `dump-2023-06-10/h_game_data.sql` khớp đủ 1360 cột — nhưng bộ `install/` sinh thẳng
  từ DB đang chạy nên chính xác hơn và không cần giữ file đó nữa.
- `h_game_log` của **cả hai** bản dump đều thiếu cột `t_player_logout_log.diamond`
  (thêm vào sau 10/06/2023). Đây là lý do phải sinh lại từ DB thật thay vì tái dùng file cũ.

## backup-2023-06-10/ — snapshot dữ liệu thật

Không dùng để cài mới, chỉ để tra cứu / khôi phục dữ liệu cũ.

| File | Dung lượng | Ghi chú |
|---|---|---|
| `h_game.sql` | 147 MB | 10.273 nhân vật, 5.570 mail — **nhiều hơn** DB đang chạy (10.214 nhân vật, 0 mail) |
| `h_game_data.sql` | 5.0 MB | thiếu 30 cột, chỉ để đối chiếu |
| `user_center.sql` | 2.1 MB | kèm tài khoản thật |
| `nap_card.sql` | 55 KB | kèm user nạp thẻ |
| `h_game_global_log.sql` | 2 KB | |

`h_game_log.sql` (512 MB) đã xoá — nó là tập con nghiêm ngặt của `h_game_log` đang chạy
(live nhiều hơn ở mọi bảng). Database `h_game_old` (264 MB) cũng đã DROP sau khi xác minh
trùng khớp tuyệt đối với `h_game.sql` ở trên. Cả hai vẫn còn trong `Kỷ Nguyên Rồng Thần.rar`.

## Sao lưu

```
db\mysql\bin\mysqldump.exe -uroot -pxpymw.com --default-character-set=utf8 ^
  --databases user_center h_game h_game_data h_game_log h_game_global_log nap_card ^
  > backup.sql
```

Dừng MySQL **luôn phải** qua `bin\stop-all.bat` (nó gọi `mysqladmin shutdown` trước khi
kill). Kill thẳng `mysqld.exe` sẽ hỏng InnoDB.
