@ECHO off
chcp 65001

cd ..

if exist App\config\* del App\config\* /q
xcopy resource\config\* App\config\* /E