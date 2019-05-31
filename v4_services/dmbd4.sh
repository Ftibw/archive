user=ioas
script_path=/home/ioas/dmbd4/restart.sh
service_name=dmbd4
kill_level=15
start_level=88

#必须是redhat或centos7以下版本
init_d=/etc/rc.d/init.d			#服务脚本脚本所在目录init.d,不同系统会不一样,suse中为/etc/init.d
rc_d=/etc/rc.d				#rc<0-6>.d目录的父目录,不同系统会不一样,suse中为/etc/init.d

echo '#!/bin/bash
# chkconfig: 2345 '${start_level}' '${kill_level}'
# description: '${service_name}'

if [[ `whoami` != '$user' ]];then
        su - '$user' -c "'${script_path}'"
else
        '${script_path}'
fi
' > ${init_d}/${service_name}
chmod +x ${init_d}/${service_name}

ln -sf ${init_d}/${service_name} ${rc_d}/rc0.d/K${kill_level}${service_name}
ln -sf ${init_d}/${service_name} ${rc_d}/rc1.d/K${kill_level}${service_name}

ln -sf ${init_d}/${service_name} ${rc_d}/rc2.d/S${start_level}${service_name}
ln -sf ${init_d}/${service_name} ${rc_d}/rc3.d/S${start_level}${service_name}
ln -sf ${init_d}/${service_name} ${rc_d}/rc4.d/S${start_level}${service_name}
ln -sf ${init_d}/${service_name} ${rc_d}/rc5.d/S${start_level}${service_name}

ln -sf ${init_d}/${service_name} ${rc_d}/rc6.d/k${kill_level}${service_name}
