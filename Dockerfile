FROM hub.c.163.com/library/java:8
ADD target/wise-spider.jar /app.jar
EXPOSE 82
RUN bash -c 'touch /app.jar'
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]