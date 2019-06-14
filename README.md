simple-spring-boot-demo

1.改造步骤
    
    复制 Dockerfile
    
```
#基础镜像
FROM hub.hulushuju.com/jre/jre-8:8u191
#可以通过环境变量 自定义JVM参数
ENV JAVA_OPTIONS " "
WORKDIR /data/java
COPY <你的jar包全路径> <容器中jar名称[可以与from名称相同]>
#挂载出日志目录
VOLUME /data/logs
CMD java -Djava.security.egd=file:/dev/./urandom ${JAVA_OPTIONS} -jar <容器中jar名称>
```

    添加Jenkinsfile
 


