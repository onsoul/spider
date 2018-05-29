FROM anapsix/alpine-java:8_server-jre
ADD target/spider.jar /app.jar
EXPOSE 82
RUN bash -c 'touch /app.jar'
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]