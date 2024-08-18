#!/usr/bin/env bash

APP_NAME="ground_flip"
REPOSITORY=/home/ubuntu/ground_flip

echo "> Remove previous Docker image"
docker rmi "$APP_NAME"

echo "> Build Docker image"
docker build -t "$APP_NAME" "$REPOSITORY"

TARGET_PORT=0
CURRENT_PORT=$(sudo docker ps --filter "name=$APP_NAME" --format "{{.Ports}}" | cut -d: -f2 | cut -d- -f1)
echo "> CURRENT_PORT = $CURRENT_PORT"

if [ "$CURRENT_PORT" == "8081" ]; then
  TARGET_PORT=8082
else
  TARGET_PORT=8081
fi
echo "> TARGET_PORT = $TARGET_PORT"


NEW_CONTAINER_NAME="$APP_NAME-$TARGET_PORT"
OLD_CONTAINER_NAME="$APP_NAME-$CURRENT_PORT"

echo "> Run the Docker container on port $TARGET_PORT"
sudo docker run -d -p $TARGET_PORT:8080 --env-file  /home/ubuntu/ground_flip/.env -e TZ=Asia/Seoul -v /home/ubuntu/logs:/logs --name "$NEW_CONTAINER_NAME" "$APP_NAME"

for cnt in {1..10} # 10번 실행
do
        echo "check server start.."

        RESPONSE=$(curl -s http://127.0.0.1:${TARGET_PORT}/check)
        SUCCESS=$(echo $RESPONSE | grep '"result":"success"')
        if [ -z "$SUCCESS" ] # 실행되었다면 break
        then
                echo "server not start.."
        else
                break
        fi

        echo "wait 10 seconds" # 10 초간 대기
        sleep 10
done

echo "> Update NGINX configuration to route traffic to the new container"
NGINX_CONF="/etc/nginx/nginx.conf"
sudo sed -i "s/$CURRENT_PORT/$TARGET_PORT/g" "$NGINX_CONF"

echo "> Reload NGINX to apply the new configuration"
sudo nginx -s reload

sudo docker rm -f $OLD_CONTAINER_NAME

echo "> Deployment to port $TARGET_PORT completed successfully."

