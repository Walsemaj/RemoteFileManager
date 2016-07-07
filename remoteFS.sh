#!/bin/bash

refresh () {
 rm -rf RemoteFileManager/
 git clone https://github.com/Walsemaj/RemoteFileManager
 cd RemoteFileManager/RemoteFileManager
 mvn package
}

copyLog () {
 cp log4j.properties RemoteFileManager/RemoteFileManager/target/classes/
}

start () {
 sh RemoteFileManager/RemoteFileManager/target/bin/webapp
}

option="${1}"
case "${option}" in
 "refresh") refresh;;
 "copyLog") copyLog;;
 "start") start;;
 *) echo "Invalid Command (refresh|copyLog|start)";;
esac
