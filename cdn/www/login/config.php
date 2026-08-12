<?php
return array(
	'db1'	=> array(
		'ip'		=> '127.0.0.1',
		// PHẢI là 127.0.0.1, không được để 'localhost': với 'localhost' mysqli nối qua
		// Unix socket (/var/run/mysqld/mysqld.sock), mà socket đó nằm trong container
		// mysql nên container php không thấy -> lỗi (HY000/2002) No such file or directory.
		// 127.0.0.1 ép mysqli đi TCP, chạy được qua network_mode: host.
		'host'		=> '127.0.0.1',
		'port'		=> 3306,
		'username'	=> 'root',
		'password'	=> 'xpymw.com',
		'database'	=> 'nap_card',
	)
);