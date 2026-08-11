# Thay PHP 5.4.45 + php5apache2_4.dll của Windows.
#
# Vì sao 7.4 chứ không phải 5.6: image php:5.6 đã lưu trữ, chỉ còn amd64 và hết
# vá bảo mật từ 2018. Giá phải trả cho 7.4 là 15 lời gọi mysql_* trong
# cdn/www/gm/{config,index}.php — đã chuyển sang mysqli, và mysqli cũng có sẵn
# trong PHP 5.4 nên bản Windows vẫn chạy nguyên như cũ.
FROM php:7.4-fpm

# mysqli: gm/*.php và login/login.php.
# pdo_mysql: để sẵn, không cần thêm gói apt nào.
# curl/json (post_curl trong gm/config.php) đã biên dịch sẵn trong image gốc.
RUN docker-php-ext-install -j"$(nproc)" mysqli pdo_mysql

COPY knrt.ini      /usr/local/etc/php/conf.d/zz-knrt.ini
COPY knrt-fpm.conf /usr/local/etc/php-fpm.d/zz-knrt.conf
