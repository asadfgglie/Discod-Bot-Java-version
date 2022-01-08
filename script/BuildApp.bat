@ECHO off
chcp 65001

cd ..

if exist build\libs\*.jar (
    echo Re-create `App` dictionary.
    if exist App rmdir App /s /q
    md App

    echo Copy the file.
    xcopy build\libs\*.jar App\
    ren App\*.jar DiscordBot-all.jar

    xcopy resource\* App\ /E
) ^
else (
    echo You need to run `gradlew shadowJar` before run this script.
)