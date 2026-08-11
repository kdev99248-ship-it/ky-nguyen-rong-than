<?php 
include_once 'config.php';
$name=$_POST['name'];
$pass=$_POST['pass'];
if($name != $adminname){
	echo json_encode(array("code"=>0,"msg"=>"管理员账号错误"));
	exit;	
}
if($pass != $adminpass){
	echo json_encode(array("code"=>0,"msg"=>"管理员密码错误"));
	exit;	
}
$_SESSION['user']=$name;
echo json_encode(array("code"=>1,"msg"=>"登录成功"));

?>