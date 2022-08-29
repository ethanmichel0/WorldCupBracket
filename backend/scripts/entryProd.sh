#!/bin/sh
jarFile=$(find . -name *.jar -print -quit)
echo $jarFile
java -jar $jarFile