#!/usr/bin/env bash

APP_NAME="ground_flip"
REPOSITORY=/home/ubuntu/ground_flip

echo "> Check the currently running container"
CONTAINER_ID=$(docker ps -aqf "name=$APP_NAME")
pwd
ls
if [ -z "$CONTAINER_ID" ];
then
  echo "> No such container is running."
else
  echo "> Stop and remove container: $CONTAINER_ID"
  docker stop "$CONTAINER_ID"
  docker rm "$CONTAINER_ID"
fi

echo "> Remove previous Docker image"
docker rmi "$APP_NAME"

echo "> Build Docker image"
docker build -t "$APP_NAME" "$REPOSITORY"

echo "> Run the Docker container"
docker run -d -p 8080:8080 --env-file /home/ubuntu/ground_flip/.env --name "$APP_NAME" "$APP_NAME"
