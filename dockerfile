FROM openjdk:8u212-jdk
ENV VERSION 2.0.0
ADD admin-service/target/mi-service-${VERSION}.jar /opt/application/mi-service-${VERSION}.jar
ADD run.sh /opt/application/run.sh
ADD dockerconfig /opt/application/config
CMD ["/bin/sh","/opt/application/run.sh"]
