basepath=$(cd `dirname $0`;pwd)
mysql_home=/dmbd3_mysql		#多级目录都必须是mysql用户组,在根目录就不用设置所有父级目录权限了
count=$(ls -l / | grep -c dmbd3_mysql)
if [[ $count == 0 ]];then
        mkdir $mysql_home
fi

mysql_psw=123456
for key in $@
do
if [[ $key =~ "--p=" ]];then
        mysql_psw=$(echo $key | grep '^\-\-p=.*$' | awk '{gsub(/\-\-p=/,"");print $1}')
        if [[ -z $mysql_psw ]];then
                mysql_psw=123456
        fi
fi
done
if [[ -n $MYSQL_PSW ]];then
        mysql_psw=$MYSQL_PSW
fi

#-------------------------------递归,仅当在dmbd3_mysql目录内时,执行后续逻辑
if [[ $basepath != $mysql_home ]];then	
	ps -ef | grep $mysql_home/bin/mysql | awk '{print $2}' | xargs kill -9
	rm -rf $mysql_home/*
	echo "正在拷贝$basepath/*到${mysql_home}目录中..."
	cp -rf $basepath/* $mysql_home
	cd $mysql_home && ./configure.sh --p=$mysql_psw  	#因为递归调用,第一个脚本的入参也需要传入到后续脚本中
	exit 1
fi
cd $basepath

#-------------------------------先卸载旧的依赖mariadb,mysql
rpm -qa | grep mariadb | xargs rpm -e --nodeps
rpm -qa | grep mysql | xargs rpm -e --nodeps

#-------------------------------添加mysql组和用户
count=$(cat /etc/group | grep -c mysql)
if [[ $count == 0 ]];then
        groupadd mysql
fi
count=$(cat /etc/passwd | grep -c mysql)
if [[ $count == 0 ]];then
        useradd -r -g mysql mysql
fi

#-------------------------------初始化配置
./bin/mysqld  --initialize --user=mysql --basedir=$basepath --datadir=$basepath/data

################################创建socket使用的tmp目录
count=$(ls -l $basepath | grep -c tmp)
if [[ $count == 0 ]];then
        mkdir $basepath/tmp
fi

#-------------------------------设置my.cnf配置文件 
echo \
"[mysqld]
character-set-server=utf8
basedir=$basepath
datadir=$basepath/data
port=3306
socket=$basepath/tmp/mysql.sock
sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES
symbolic-links=0
lower_case_table_names=1
max_connections=1000
skip-grant-tables

[client]
default-character-set=utf8
socket=$basepath/tmp/mysql.sock

[mysql]
default-character-set=utf8
socket=$basepath/tmp/mysql.sock
" > my.cnf

#-------------------------------设置用户组和权限,确保新文件或目录都是mysql用户组
chown -R mysql:mysql $basepath  #多级目录,所有父目录都必须是mysql用户组
mv -f $basepath/my.cnf /etc

#-------------------------------启动mysql服务

echo " ----------------------------------------------"
echo "|                MySQL启动中...                |"
./bin/mysqld_safe&
sleep 3s
echo "|               MySQL启动成功!!!               |"

################################数据库登录配置 
if [[ $mysql_psw == "123456" ]];then
	echo " ----------------------------------------------"
	echo "|            当前MySQL密码为默认密码           |"
fi

#-------------------------------skip-grant-tables模式下,只能authentication_string修改密码
echo \
"use mysql;
update user set authentication_string=password('$mysql_psw') where user='root';
flush privileges;" > permit.sql
./bin/mysql -uroot < permit.sql

echo \
"alter user user() identified by '$mysql_psw';
grant all on *.* to root@'%' identified by '$mysql_psw';
flush privileges;" > permit.sql

#去掉my.cnf中的无认证登录并重启mysql服务
sed -i "s#skip-grant-tables##" $(grep "skip-grant-tables" -rl --include="my.cnf" /etc) 2>/dev/null
echo " ----------------------------------------------"
echo "|                MySQL重启中...                |"
./support-files/mysql.server restart
sleep 3s
echo "|               MySQL重启成功!!!               |"
echo " ----------------------------------------------"

#-------------------------------恢复正常登录模式
./bin/mysql --connect-expired-password -uroot -p$mysql_psw < permit.sql
rm -f permit.sql
#################################导入yfdmbd数据库脚本
./bin/mysql -uroot -p$mysql_psw < yfdmbdboc.sql
./bin/mysql -uroot -p$mysql_psw < yfdmbdlog.sql


