#!/bin/bash

refresh () {
 cd ..
 rm -rf RemoteFileManager/
 git clone https://github.com/Walsemaj/RemoteFileManager
}

copyLog () {
 cp log4j.properties RemoteFileManager/RemoteFileManager/target/classes/
}

start () {
 cd RemoteFileManager/RemoteFileManager
 mvn package
 sh target/bin/webapp
}

option="${1}"
case "${option}" in
 "refresh") refresh;;
 "copyLog") copyLog;;
 "start") start;;
 *) echo "Invalid Command (refresh|copyLog|start)";;
esac
