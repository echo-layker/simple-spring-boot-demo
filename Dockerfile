#基础镜像
FROM hub.hulushuju.com/jre/jre-8:8u191
#可以通过环境变量 自定义JVM参数
ENV JAVA_OPTIONS " "
WORKDIR /data/java
COPY target/simple-spring-boot-demo-0.0.1-SNAPSHOT.jar simple-spring-boot-demo-0.0.1-SNAPSHOT.jar
#挂载出日志目录
VOLUME /data/logs
CMD java -Djava.security.egd=file:/dev/./urandom ${JAVA_OPTIONS} -jar simple-spring-boot-demo-0.0.1-SNAPSHOT.jar