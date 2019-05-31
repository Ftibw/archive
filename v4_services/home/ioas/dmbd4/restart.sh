workdir=/home/ioas/dmbd4
cd ${workdir}
. ${workdir}/ports.conf

ps -ef | grep ${workdir}/dmbd4.jar  | awk '{print $2}' | xargs kill -9 2>/dev/null

nohup \
java -jar ${workdir}/dmbd4.jar \
--spring.profiles.active=${profile} \
--mqtt-service-address=${mqtt_service_address} \
--video-editing-service-address=${video_editing_service_address} \
>/dev/null  2>&1 &

