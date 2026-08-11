<?php 
header("Content-type: text/html; charset=utf8");
error_reporting(0);
session_start();
date_default_timezone_set("PRC");
// mysqli thay cho mysql_*: API mysql_* bị xoá hẳn từ PHP 7 (container dùng 7.4).
// mysqli cũng có trong PHP 5.4 (php.ini Windows đã bật php_mysqli.dll) nên bản
// Windows chạy y như cũ. index.php truyền $conn vào mysqli_query().
$conn=@mysqli_connect("127.0.0.1","root","xpymw.com","user_center",3306) or die("数据库连接失败,请联系管理员！");
mysqli_set_charset($conn,"utf8");

function post_curl($url, $data_string) {
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_POST, 1);	
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER,true);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $data_string);
	$result = curl_exec($ch);
	if(curl_errno($ch)){
		print_r(curl_error($ch));
	}
	curl_close($ch);
	return $result;	
}
$serverlist=[
	1=>[
		"url"=>"http://127.0.0.1:51011/hgame/background_api",
		"table"=>"h_game"
	]
];

$adminname="admin";
$adminpass="nguyenle1999";
?>