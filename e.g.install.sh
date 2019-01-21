#!/bin/bash
. /etc/init.d/functions
red_col="\e[1;31m"
reset_col="\e[0m"
#该脚本所在目录
basepath=$(cd `dirname $0`; pwd)

function uninstall(){
	if [[ `rpm -qa | grep -c $1` -ge 1 ]]||[[ `rpm -ql $1 2>&1 | grep -c $1` -ge 2 ]];then
		sudo rpm -qa | grep $1 | xargs rpm -e --nodeps
	fi
}
#------------------------更新系统-----------------------------
if [[ `rpm -qa | grep -c epel-release` -eq 0 ]];then
	sudo yum install epel-release -y
	sudo yum update -y
	if [ "$?" -eq 0 ] ; then
                action "系统更新完成！！！" /bin/true
        else
                action "系统更新失败,请您检查网络" /bin/false
                exit 1
        fi
fi

#======================== mongodb ========================
mongodb_process=`ps -aux | grep mongod |awk 'NR==1{print $1}'`
if [[ $mongodb_process != "mongod" ]];then
	sudo rpm -qa | grep mongodb | xargs rpm -e --nodeps
    	sudo echo -e "
[mongodb-org]
name=MongoDB Repository
baseurl=https://mirrors.tuna.tsinghua.edu.cn/mongodb/yum/el\$releasever/
gpgcheck=0
enabled=1
        " > /etc/yum.repos.d/mongodb.repo
	sudo yum makecache && \
	sudo yum install mongodb-org -y
######  sudo rm -f /var/run/mongodb/mongod.pid
	sudo service mongod start 
	if [ "$?" -eq 0 ] ; then
		action "mongodb服务启动成功！！！" /bin/true
	else
		action "mongodb启动失败,请您检查网络" /bin/false
		exit 1
	fi
else
	action "mongodb服务已经启动" /bin/true
fi
sudo chkconfig mongod on
unset mongodb_process

#======================== jdk1.8 ========================
#jdk 1.8+jre 1.8
if [[ `java -version 2>&1 | grep -c 1.8` -lt 2 ]];then
        #if [ `rpm -qa |grep -c java` -ge 1 ]||[ `rpm -qa |grep -c jdk` -ge 1 ];then
        #        rpm -qa | grep java | xargs rpm -e --nodeps
        #        rpm -qa | grep jdk | xargs rpm -e --nodeps
        #fi
		uninstall java
		uninstall jdk
        sudo yum install java-1.8.0-openjdk* -y
        if [ "$?" -eq 0 ] ; then
                action "jdk1.8安装成功！！！" /bin/true
        else
                action "jdk1.8失败,请您检查网络" /bin/false
                exit 1
        fi
else 
        action "jdk1.8已经安装" /bin/true
fi
#======================== nginx ========================
nginx_status=0
nginx_process=`ps -aux | grep nginx |awk 'NR==1{gsub(/:/,"");print $11}'`
if [[ $nginx_process != "nginx" ]];then
	uninstall nginx
	sudo yum install nginx  -y
	if [ "$?" -ne 0 ] ; then
          action "nginx安装失败,请您检查网络" /bin/false
          exit 1
    	fi
	nginx_status=1
fi
sudo sed -i "s#root .*/static#root ${basepath}/static#g" `grep "root .*/static" -rl --include="nginx.conf" ./`
sudo cp ${basepath}/data/nginx.conf -f /etc/nginx/nginx.conf
sudo systemctl restart nginx.service
if [ "$?" -eq 0 ] ; then
	msg=""
        if [ $nginx_status == 1 ];then
                msg="启动成功！！！"
        else
                msg="重启成功"
        fi
    	action "nginx服务${msg}" /bin/true
	unset msg
else
    	action "nginx服务启动失败！！！" /bin/false
    	exit 1
fi
sudo systemctl enable nginx.service
unset nginx_status
unset nginx_process
#======================== redis =========================
redis_process=`ps -aux | grep redis |awk 'NR==1{print $1}'`
if [[ $redis_process != "redis" ]];then
	uninstall redis
	sudo yum install epel-release -y && \
	sudo yum install redis -y && \
	sudo systemctl start redis.service
	if [ "$?" -eq 0 ] ; then
                action "redis服务启动成功！！！" /bin/true
        else
                action "redis安装失败,请您检查网络" /bin/false
                exit 1
    fi
else
	action "reids服务已经启动" /bin/true
fi
sudo systemctl enable redis.service
unset redis_process
#====================== ImageMagick ======================
im_status=0
if [[ `rpm -qa | grep -c ImageMagick` -eq 0 ]]||[[ `rpm -ql ImageMagick 2>&1 | grep -c ImageMagick` -eq 0 ]];then
	sudo rpm -qa | grep ImageMagick | xargs rpm -e --nodeps >/dev/null
	sudo yum install ImageMagick -y
	im_status=1	
fi
mogrify >>/dev/null
if [ "$?" -eq 0 ] ; then
	msg=""
	if [ $im_status == 0 ];then 
		msg="已经安装"
	else
		msg="安装成功"
	fi
	action "ImageMagick$msg" /bin/true
	unset msg
else
	action "ImageMagick安装失败,请您检查网络" /bin/false
	exit 1
fi
unset im_status

##===================== Libreoffice6.1 =====================
libreoffice_status=0
if [[ `rpm -qa | grep -c libreoffice6.1` -eq 0 ]];then
	sudo cp -rf  ${basepath}/data/fonts/chinese  /usr/share/fonts/
	sudo fc-cache -fv
	cd ${basepath}/data/LibreOffice_6.1.0.3/RPMS
	su -c 'yum install *.rpm -y' && \
	cd - >/dev/null && \
	libreoffice_status=1
fi
if [ "$?" -eq 0 ] ; then
	msg=""
        if [ $libreoffice_status == 0 ];then
                msg="已经安装"
        else
                msg="安装成功"
        fi
	action "Libreoffice${msg}" /bin/true
	unset msg
else
	action "Libreoffice安装失败,请您检查安装包是否完整" /bin/false
	exit 1
fi
unset libreoffice_status
#======================== ffmpeg ========================
ffmpeg_status=0
if [[ `rpm -qa | grep -c ffmpeg` -eq 0 ]]||[[ `rpm -ql ffmpeg 2>&1 | grep -c ffmpeg` -eq 0 ]];then
    sudo rpm -qa | grep ffmpeg | xargs rpm -e --nodeps >/dev/null
    sudo rpm --import http://li.nux.ro/download/nux/RPM-GPG-KEY-nux.ro
	sudo rpm -Uvh http://li.nux.ro/download/nux/dextop/el7/x86_64/nux-dextop-release-0-5.el7.nux.noarch.rpm
	sudo yum install ffmpeg ffmpeg-devel -y
    ffmpeg_status=1
fi
ffmpeg -h >>2 2>>/dev/null
if [ "$?" -eq 0 ] ; then
        msg=""
        if [ $ffmpeg_status == 0 ];then
                msg="已经安装"
        else
                msg="安装成功"
        fi
        action "ffmpeg$msg" /bin/true
        unset msg
else
        action "ffmpeg安装失败,请您检查网络" /bin/false
        exit 1
fi
unset ffmpeg_status
#-----------------------关闭防火墙----------------------------
systemctl stop firewalld
systemctl disable firewalld
unset basepath
#------------------------重启系统---------------------------- 
function check_input(){
	read -p "重启系统[0]/暂不重启[1]?:" NUM
		expr $NUM + 1 >& /dev/null
	if [ "$?" -ne 0 ];then
		action "请您输入数值" /bin/false
	elif [[ "$NUM" > 1 ]];then
		action "请选择<重启系统[0]>或者<暂不重启[1]>选项数字" /bin/false
	fi
}

function machine_restart(){
	echo -e "${red_col}即将重启,请稍后....\n${reset_col}"
	sudo shutdown -r now
}

function chose_step(){
while :
do 
 check_input
        case $NUM in
                0)
                machine_restart
                ;;
                1)
                exit 0
        esac
done
}
chose_step
