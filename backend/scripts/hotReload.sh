#!/bin/sh
#every 5 seconds compile changes to .classFiles
while true
do 
    echo "checking for changes"
    mvn compile
    sleep 5
done