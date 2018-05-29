#!/usr/bin/env bash

#一键运行
#必须先安装docker、docker-compose环境



#自行构建
#必须先安装git、jdk1.8+、maven3。5+ 
#git clone https://github.com/gsh199449/spider.git
#cd project_path
#mvn install
#docker build -t {image.name}:{image.version} .
#修改docker/docker-compose.yml,填入自己构建的镜像.



#以下目录是先处理好，用来镜像运行时挂载到宿主机的数据目录。
mkdir -p /data/es/data
mkdir -p /data/es/logs
mkdir -p /data/redis/data

chmod 775 /data/es/data
chmod 775 /data/es/logs
chmod 775 /data/redis/data


docker-compose up -d
