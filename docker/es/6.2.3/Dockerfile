FROM docker.elastic.co/elasticsearch/elasticsearch:6.2.3
MAINTAINER birdben (wanhao@doupai.cc)
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v6.2.3/elasticsearch-analysis-ik-6.2.3.zip

VOLUME /usr/share/elasticsearch/data
VOLUME /usr/share/elasticsearch/logs
ADD ./elasticsearch.yml /usr/share/elasticsearch/config/elasticsearch.yml 

