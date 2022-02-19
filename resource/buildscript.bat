@ECHO off
chcp 65001
docker stop discordbot

docker container rm discordbot

docker image rm discordbot:java11

docker build -t discordbot:java11 ./ --no-cache

docker run -d -v ./config:/bot/config --name discordbot discordbot:java11