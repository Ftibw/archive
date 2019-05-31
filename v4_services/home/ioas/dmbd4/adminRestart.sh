workdir=/home/ioas/dmbd4
. ${workdir}/ports.conf
cd ${workdir}

ps -ef | grep ${workdir}/dmbd4admin.jar  | awk '{print $2}' | xargs kill -9 2>/dev/null

nohup \
java -jar ${workdir}/dmbd4admin.jar \
--spring.profiles.active=${profile} \
--mqtt-service-address=${mqtt_service_address} \
>/dev/null  2>&1 &
