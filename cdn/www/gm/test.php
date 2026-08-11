<?php 
include_once 'config.php';
?>
<!DOCTYPE html>
<html>

<body>
  
<?php 
		$action='recharge';
		$playerid="542776492034";
		$productid=1;
		$data=array(
			"action"=>$action,
			"playerid"=>$playerid, 
			"productid"=>intval($productid)
		);		
		$data=json_encode($data);
		$res=post_curl("http://127.0.0.1:51011/hgame/background_api", $data);
		echo "<script>alert('充值成功！$data');history.go(-1)</script>";	
		exit;

?> 
