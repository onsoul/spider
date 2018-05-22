#!/bin/bash

cp spider.war /opt/spider
jar -xvf spider.war

## m /WEB-INF/classes/sataticValue.json
## docker-compose up