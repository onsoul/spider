#!/bin/bash


## 基础镜像使用的 docker.elastic.co/elasticsearch/elasticsearch:6.2.3 ,可能因为墙的原因会拉取很慢！
docker build -t wise-es:6.2.3 .

docker-compose up -d