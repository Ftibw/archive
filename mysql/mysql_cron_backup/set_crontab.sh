basepath=$(cd `dirname $0`;pwd)
echo "10 4 * * * $basepath/mysqlbackup.sh" > mysqlbackup.cron
crontab $basepath/mysqlbackup.cron
