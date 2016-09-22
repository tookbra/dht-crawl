#!/bin/sh

export DHT_SERVER_HOME=..
CONFIG_DIR=$DHT_SERVER_HOME/config

RESOLVED_CONFIG_DIR=`cd "$CONFIG_DIR"; pwd`
export CLASSPATH=$RESOLVED_CONFIG_DIR

for i in `ls $DHT_SERVER_HOME/lib/*.jar`; do
	CLASSPATH=$i:$CLASSPATH
done
nohup java -classpath $CLASSPATH -Xmx512m -Dio.netty.leakDetection.level=advanced com.tookbra.dht.Main $* > /dev/null 2>&1
