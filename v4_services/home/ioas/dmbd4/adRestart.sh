workdir=/home/ioas/dmbd4
cd ${workdir}
. ${workdir}/ports.conf

ps -ef | grep /home/ioas/dmbd4/dmbd4ad.jar | awk '{print $2}' | xargs kill -9
nohup java -jar /home/ioas/dmbd4/dmbd4ad.jar \
--spring.profiles.active=${profile} \
--mqtt-service-address=${mqtt_service_address} \
--video-editing-service-address=${video_editing_service_address} \
>/dev/null  2>&1 &

