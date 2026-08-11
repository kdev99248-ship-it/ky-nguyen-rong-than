<?php 
include_once 'config.php';
?>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>管理后台</title>
  <meta name="renderer" content="webkit">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
  <link rel="stylesheet" href="layui/css/layui.css"  media="all">
  <link rel="stylesheet" href="layui/formSelects-v4.css" />
  <script src="layui/layui.all.js" type="text/javascript" charset="utf-8"></script>
  <script src="layui/lay/modules/layer.js" type="text/javascript" charset="utf-8"></script>
</head>
<body>
  
<?php 
switch($_POST['submit']){
	case '充值';
		$account=trim($_POST['account']);
		$money=trim($_POST['money']);
		$sid=trim($_POST['sid']);
		if(!$sid){
			echo "<script>alert('请选择游戏分区！');history.go(-1)</script>";
			exit;
		}		
		if(!$account){
			echo "<script>alert('请输入玩家角色名！');history.go(-1)</script>";
			exit;
		}
		if(!$money){
			echo "<script>alert('请选择充值金额！');history.go(-1)</script>";
			exit;
		}
		$sql="select * from t_s_server_list where zone_id='".$sid."'";
		$result=mysqli_query($conn,$sql); 
		$row2=mysqli_fetch_array($result);
		if(!$row2['id']){
			echo "<script>alert('该分区不存在！');history.go(-1)</script>";
			exit;			
		}	
		
		$sql="select * from {$serverlist[$sid]['table']}.t_player where name='".$account."'";
		$result=mysqli_query($conn,$sql); 
		$row=mysqli_fetch_array($result);
		if(!$row['playerId']){
			echo "<script>alert('角色不存在,发送失败".$sql."！');history.go(-1)</script>";
			exit;	
		}
		
		$action='recharge';
		$playerid=$row['playerId'];
		$productid=$money;
		$data=array(
			"action"=>$action,
			"playerid"=>$playerid, 
			"productid"=>intval($productid)
		);		
		$data=json_encode($data);
		$res=post_curl($serverlist[$sid]['url'], $data);
		echo "<script>alert('充值成功！$data');history.go(-1)</script>";	
		exit;

		break;	

	case '个人邮件';
		$title=trim($_POST['title']);
		$context=trim($_POST['context']);
		$account=trim($_POST['account']);
		$item=trim($_POST['item']);
		$num=trim($_POST['num']);
		$item2=trim($_POST['item2']);
		$num2=trim($_POST['num2']);		
		$item3=trim($_POST['item3']);
		$num3=trim($_POST['num3']);		
		$item4=trim($_POST['item4']);
		$num4=trim($_POST['num4']);	
		$item5=trim($_POST['item5']);
		$num5=trim($_POST['num5']);		
		$sid=trim($_POST['sid']);
		if(!$sid){
			echo "<script>alert('请选择游戏分区！');history.go(-1)</script>";
			exit;
		}		
		if(!$account){
			echo "<script>alert('请输入玩家账号！');history.go(-1)</script>";
			exit;
		}
		if(!$title){
			echo "<script>alert('请输入邮件标题！');history.go(-1)</script>";
			exit;			
		}
		if(!$context){
			echo "<script>alert('请输入邮件内容！');history.go(-1)</script>";
			exit;			
		}		
		if(!$item){
			echo "<script>alert('请选择发送的物品！');history.go(-1)</script>";
			exit;
		}
		if(!$num){
			echo "<script>alert('请输入物品数量！');history.go(-1)</script>";
			exit;
		}
		if($item){
			if(!$num){
				echo "<script>alert('请输入物品数量！');history.go(-1)</script>";
				exit;
			}
			$items.=$item.'_'.$num;
		}
		
		if($item2){
			if(!$num2){
				echo "<script>alert('请输入物品数量！');history.go(-1)</script>";
				exit;
			}
			$items.=',';
			$items.=$item2.'_'.$num2;
		}		
		if($item3[0]){
			if(!$num3){
				echo "<script>alert('请输入物品数量！');history.go(-1)</script>";
				exit;
			}
			$items.=',';
			$items.=$item3.'_'.$num3;
		}
		if($item4[0]){
			if(!$num4){
				echo "<script>alert('请输入物品数量！');history.go(-1)</script>";
				exit;
			}
			$items.=',';
			$items.=$item4.'_'.$num4;
		}
		if($item5[0]){
			if(!$num5){
				echo "<script>alert('请输入物品数量！');history.go(-1)</script>";
				exit;
			}
			$items.=',';
			$items.=$item5.'_'.$num5;
		}			
		$sql="select * from t_s_server_list where zone_id='".$sid."'";
		$result=mysqli_query($conn,$sql); 
		$row2=mysqli_fetch_array($result);
		if(!$row2['id']){
			echo "<script>alert('该分区不存在！');history.go(-1)</script>";
			exit;			
		}	
		
		$sql="select * from {$serverlist[$sid]['table']}.t_player where name='".$account."'";
		$result=mysqli_query($conn,$sql); 
		$row=mysqli_fetch_array($result);
		if(!$row['playerId']){
			echo "<script>alert('角色不存在,发送失败！');history.go(-1)</script>";
			exit;	
		}		
		$action='mail';
		$playerid=$row['playerId'];
		$data=array(
			"action"=>$action,
			"playerid"=>$playerid, 
			"mail"=>array(
				"sender"=>"GM",
				"title"=>$title,
				"content"=>$context,
				"reward"=>$items
			)
		);
		$data=json_encode($data);
		$res=post_curl($serverlist[$sid]['url'], $data);	
		echo "<script>alert('发送成功！');history.go(-1)</script>";	
		exit;						
		break;	

		
}

?> 


  
             
<fieldset class="layui-elem-field layui-field-title" style="margin-top: 20px;">
  <legend>系统功能</legend>
</fieldset>

<form class="layui-form" method="post" action="">
  <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">游戏分区</label>
      <div class="layui-input-inline">
        <select name="sid">
			<option value="">请选择游戏分区</option>
		  <?php 
			$sql="select * from t_s_server_list";
			$result=mysqli_query($conn,$sql);
			while($row=mysqli_fetch_array($result)){				  
		  ?>
			<option value="<?php echo $row['zone_id'];?>"><?php echo $row['name'];?></option>
		  <?php }?>
        </select>
      </div>
    </div>
  </div>

  <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">玩家角色</label>
      <div class="layui-input-inline">
        <input type="text" name="account" lay-verify="title" autocomplete="off" class="layui-input">
      </div>
    </div>
  </div>
  <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">充值金额</label>
      <div class="layui-input-inline">
        <select name="money" lay-verify="required" lay-search="">		
          <option value="">请选择充值金额</option>	
		  <?php 
			$sql="select * from h_game_data.t_recharge";
			$result=mysqli_query($conn,$sql);
			while($row=mysqli_fetch_array($result)){				  
		  ?>
			<option value="<?php echo $row['id'];?>"><?php echo $row['desc'];?>-----<?php echo $row['price'];?>元</option>
		  <?php }?>			

        </select>
      </div>
    </div>
  </div>  
    
  <div class="layui-form-item">
    <div class="layui-input-block">
	  <input type="submit" class="layui-btn" name="submit" value='充值'>
    </div>
  </div>



  <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">邮件标题</label>
      <div class="layui-input-inline">
        <input type="text" name="title" lay-verify="title" autocomplete="off" value="系统邮件" class="layui-input">
      </div>
    </div>
  </div>

  <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">邮件内容</label>
      <div class="layui-input-inline">
        <input type="text" name="context" lay-verify="title" autocomplete="off" value="请注意查收" class="layui-input">
      </div>
    </div>
  </div>


 <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">多物品一</label>
      <div class="layui-input-inline">		
        <select name="item" lay-verify="required" lay-search="">
			<option value="">请选择物品</option>	
			<?php 
				$lines=file("item.txt");
				foreach ($lines as $value) {
					$line=explode(";",$value);
					echo '<option value="'.$line[0].'">'.$line[1].'</option>';
				}
			?>		
        </select>			
      </div>	
      <div class="layui-input-inline">
        <input type="text" name="num" lay-verify="title" autocomplete="off" placeholder="请输入物品数量" class="layui-input">
      </div>	  
    </div>
  </div>  
 
 <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">多物品二</label>
      <div class="layui-input-inline">		
        <select name="item2" lay-verify="required" lay-search="">
			<option value="">请选择物品</option>	
			<?php 
				$lines=file("item.txt");
				foreach ($lines as $value) {
					$line=explode(";",$value);
					echo '<option value="'.$line[0].'">'.$line[1].'</option>';
				}
			?>			
        </select>			
      </div>	
      <div class="layui-input-inline">
        <input type="text" name="num2" lay-verify="title" autocomplete="off" placeholder="请输入物品数量" class="layui-input">
      </div>	  
    </div>
  </div>    
  

 <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">多物品三</label>
      <div class="layui-input-inline">		
        <select name="item3" lay-verify="required" lay-search="">
			<option value="">请选择物品</option>	
			<?php 
				$lines=file("item.txt");
				foreach ($lines as $value) {
					$line=explode(";",$value);
					echo '<option value="'.$line[0].'">'.$line[1].'</option>';
				}
			?>		
        </select>			
      </div>	
      <div class="layui-input-inline">
        <input type="text" name="num3" lay-verify="title" autocomplete="off" placeholder="请输入物品数量" class="layui-input">
      </div>	  
    </div>
  </div>  
  
 <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">多物品四</label>
      <div class="layui-input-inline">		
        <select name="item4" lay-verify="required" lay-search="">
			<option value="">请选择物品</option>	
			<?php 
				$lines=file("item.txt");
				foreach ($lines as $value) {
					$line=explode(";",$value);
					echo '<option value="'.$line[0].'">'.$line[1].'</option>';
				}
			?>			
        </select>			
      </div>	
      <div class="layui-input-inline">
        <input type="text" name="num4" lay-verify="title" autocomplete="off" placeholder="请输入物品数量" class="layui-input">
      </div>	  
    </div>
  </div>    
  
 <div class="layui-form-item">
    <div class="layui-inline">
      <label class="layui-form-label">多物品五</label>
      <div class="layui-input-inline">		
        <select name="item5" lay-verify="required" lay-search="">
			<option value="">请选择物品</option>	
			<?php 
				$lines=file("item.txt");
				foreach ($lines as $value) {
					$line=explode(";",$value);
					echo '<option value="'.$line[0].'">'.$line[1].'</option>';
				}
			?>		
        </select>			
      </div>	
      <div class="layui-input-inline">
        <input type="text" name="num5" lay-verify="title" autocomplete="off" placeholder="请输入物品数量" class="layui-input">
      </div>	  
    </div>
  </div>    
 
  <div class="layui-form-item">
    <div class="layui-input-block">
	  <input type="submit" class="layui-btn" name="submit" value='个人邮件'>
    </div>
  </div>





</form>
          
<script src="layui/layui.js" charset="utf-8"></script>
<script>
layui.use(['form', 'layedit', 'laydate'], function(){
  var form = layui.form
  ,layer = layui.layer
  ,layedit = layui.layedit
  ,laydate = layui.laydate; 
});
</script>



</body>
</html>