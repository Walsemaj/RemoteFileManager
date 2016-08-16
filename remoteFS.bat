cd %~dp0/RemoteFileManager
SET PORT=8888
CALL mvn package
CALL target/bin/webapp.bat
