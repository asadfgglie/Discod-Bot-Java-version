#!/usr/bin/env sh

cd ..

Jar="./build/libs/"
App="./App"

if [ -e $Jar ]; then
  echo "Re-create App dictionary."
  if [ -e $App ]; then
    rm -rf ./App
  fi
  mkdir ./App

  echo "Copy the file."
  cp ./build/libs/*.jar ./App/
  cp ./build/libs/*.jar ./AppVersionLibs/
  mv ./App/*.jar ./App/DiscordBot-all.jar

  cp -r ./resource/* ./App/
else
  echo "You need to run gradlew shadowJar before run this script."
fi