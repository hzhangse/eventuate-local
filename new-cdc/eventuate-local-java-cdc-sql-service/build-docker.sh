#! /bin/bash -e

if [ $(ls build/libs/*SNAPSHOT.jar | wc -l) != "1" ] ; then
    echo not exactly one jar in build/libs/
    exit 99
fi

docker build -t test-eventuate-local-java-new-cdc-mysql-service .

sudo docker login --username=rainbow954@163.com registry.cn-hangzhou.aliyuncs.com --password dockerhub@iotbull.com
sudo docker tag test-eventuate-local-java-new-cdc-mysql-service:latest registry.cn-hangzhou.aliyuncs.com/iotbull/test-eventuate-local-java-new-cdc-mysql-service:0.30.4
sudo docker push registry.cn-hangzhou.aliyuncs.com/iotbull/test-eventuate-local-java-new-cdc-mysql-service:0.30.4

