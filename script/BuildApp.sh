#!/usr/bin/env sh

cd ..

if [ -f "./build/libs/*.jar" ]; then
  echo "Re-create App dictionary."
  if [ -d "./App"]; then
    rm -rf ./App
  fi

  echo "Copy the file."
  cp ./build/libs/*.jar ./App/
  ren App\*.jar DiscordBot-all.jar

  cp ./resource/* ./App/
else
  echo "You need to run gradlew shadowJar before run this script."
fi