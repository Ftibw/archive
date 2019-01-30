basepath=$(cd `dirname $0`;pwd)
# 25 yfdmbd bakfile and 25 yfdmbdlog bakfile
max_history=50
backup_dir=/var/ftp/iips/databackup

psw=$(grep "name=\"password\" value=\".*\"" /usr/apache-tomcat-6.0.10/webapps/dmbd3/WEB-INF/classes/config/env/* | awk 'NR==2{print $3}')
psw=$(echo $psw | sed "s#value=\"##" | sed "s#\"##")
/dmbd3_mysql/bin/mysqldump -u root -p$psw yfdmbd > $backup_dir/yfdmbd_$(echo `date +%Y%m%d`).sql
/dmbd3_mysql/bin/mysqldump -u root -p$psw yfdmbdlog > $backup_dir/yfdmbdlog_$(echo `date +%Y%m%d`).sql

delfile1=$(ls -l $backup_dir/yfdmbd_*.sql | awk '{print $9 }' | head -1)
delfile2=$(ls -l $backup_dir/yfdmbdlog_*.sql | awk '{print $9 }' | head -1)

count=$(ls -l $backup_dir/yfdmbd*.sql | wc -l)
if [[ $count > $max_history ]];then
        rm $delfile1
        rm $delfile2
        now_time=$(echo `date "+%Y-%m-%d %H:%M:%S"`)
        echo "delete $delfile1 at $now_time" >> $basepath/log.txt
        echo "delete $delfile2 at $now_time" >> $basepath/log.txt
fi
