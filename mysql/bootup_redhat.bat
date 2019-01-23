basepath=$(cd `dirname $0`;pwd)
#redhat(centos)
init_d=/etc/rc.d/init.d		#服务脚本脚本所在目录init.d,不同系统会不一样,suse中为/etc/init.d
rc_d=/etc/rc.d				#rc<0-6>.d目录的父目录,不同系统会不一样,suse中为/etc/init.d
ln -sf $basepath/support-files/mysql.server $init_d/mysqld
ln -sf $init_d/mysqld $rc_d/rc2.d/S66mysqld 
ln -sf $init_d/mysqld $rc_d/rc3.d/S66mysqld 
ln -sf $init_d/mysqld $rc_d/rc4.d/S66mysqld
ln -sf $init_d/mysqld $rc_d/rc5.d/S66mysqld
