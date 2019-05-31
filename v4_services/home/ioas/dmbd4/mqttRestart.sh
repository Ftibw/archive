workdir=/home/ioas/dmbd4
. ${workdir}/ports.conf
cd ${workdir}

ps -ef | grep ${workdir}/mqtt-service.jar  | awk '{print $2}' | xargs kill -9 2>/dev/null

nohup \
java -jar ${workdir}/mqtt-service.jar \
--spring.profiles.active=${profile} \
--server.port=${mqtt_server_port} \
>/dev/null  2>&1 &
