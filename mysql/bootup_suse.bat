basepath=$(cd `dirname $0`;pwd)
ln -sf $basepath/support-files/mysql.server /etc/init.d/mysqld
ln -sf /etc/init.d/mysqld /etc/init.d/rc2.d/S66mysqld 
ln -sf /etc/init.d/mysqld /etc/init.d/rc3.d/S66mysqld 
ln -sf /etc/init.d/mysqld /etc/init.d/rc4.d/S66mysqld
ln -sf /etc/init.d/mysqld /etc/init.d/rc5.d/S66mysqld
