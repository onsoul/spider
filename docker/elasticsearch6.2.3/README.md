### 镜像说明 
>可能因为墙的原因，下载延迟很大。

>已经构好一个上传在阿里云

>docker pull registry.cn-shenzhen.aliyuncs.com/gather/elasticsearch:6.2.3

###自主构建 

> docker build -t {image.name}:{image.verion} .

> 例: docker build -t gs-es:6.2.3 .

> docker run -p 9200:9200 -p 9300:9300 gs-es:6.2.3

> docker ps

> curl localhost:9200/_cat
