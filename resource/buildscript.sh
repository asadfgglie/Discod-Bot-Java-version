#!/usr/bin/env sh

sudo docker stop discordbot

sudo docker container rm discordbot

sudo docker image rm discordbot:java11

sudo docker build -t discordbot:java11 ./ --no-cache

sudo docker run -d -v /home/asadfgglie/App/config:/bot/config --name discordbot discordbot:java11