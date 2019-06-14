FROM hub.shengcaij.com/jre/jre-8:8u191-winFonts
ENV JAVA_OPTIONS " "
WORKDIR /data/java
COPY target/data-conversion.jar data-conversion.jar
VOLUME /data/logs
CMD java -Djava.security.egd=file:/dev/./urandom ${JAVA_OPTIONS} -jar data-conversion.jar