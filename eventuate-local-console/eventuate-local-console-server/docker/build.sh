#!/usr/bin/env bash
set -e

rm -fr build
mkdir build

cd ../../
git archive --format=tar.gz -o eventuate-local-console-server/docker/build/eventuate-local-console.tar.gz HEAD

cd eventuate-local-console-server/docker
docker build -t eventuate-local-console-test .

sudo docker login --username=rainbow954@163.com registry.cn-hangzhou.aliyuncs.com --password dockerhub@iotbull.com
sudo docker tag eventuate-local-console-test registry.cn-hangzhou.aliyuncs.com/iotbull/eventuate-local-console:0.30.4
sudo docker push registry.cn-hangzhou.aliyuncs.com/iotbull/eventuate-local-console:0.30.4
 





 