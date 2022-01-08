#!/usr/bin/env sh

sudo docker build -t discordbot:java11 ./ --no-cache

sudo docker run -d --name discordbot discordbot:java11