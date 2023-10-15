#FROM maven:3.5-jdk-8-alpine as builder
#
## Copy local code to the container image.
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#COPY setting.xml .
#
## Build a release artifact.
#RUN mvn package -DskipTests
#
## Run the web service on container startup.
#CMD ["java","-jar","/app/target/user_center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]




## First stage: complete build environment
#FROM maven:3.5.0-jdk-8-alpine AS builder
#
## download dependencies (no re-download when the source code changes)
#ADD ./pom.xml pom.xml
## RUN  mvn install
#
#ADD ./src src/
## package jar
## RUN mvn install -Dmaven.test.skip=true
#
#
## copy jar from the first stage
#COPY --from=builder target/user_center-0.0.1-SNAPSHOT.jar user_center-0.0.1-SNAPSHOT.jar
#EXPOSE 8081
#CMD ["java", "-jar", "user_center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]


# 基于java镜像创建新镜像
#FROM  maven:3.5-jdk-8-alpine as builder
## 作者
#MAINTAINER Louisbrilliant
## Build a release artifact.
#RUN mvn package -DskipTests
## 将jar包添加到容器中并更名为app.jar
#ADD target/user_center-0.0.1-SNAPSHOT.jar /root/docker_test/app.jar
## 运行jar包
#ENTRYPOINT ["nohup","java","-jar","/root/docker_test/app.jar","--spring.profiles.active=prod"]


#FROM openjdk:8-jdk-alpine
#COPY target/user_center-0.0.1-SNAPSHOT.jar /app/user_center-0.0.1-SNAPSHOT.jar
#WORKDIR /app
#EXPOSE 8081
#CMD ["java", "-jar", "user_center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]


#基础镜像使用java
#FROM openjdk:8u312-jre-slim-buster
#COPY target/user_center-0.0.1-SNAPSHOT.jar /app/user_center-0.0.1-SNAPSHOT.jar
#WORKDIR /app
#EXPOSE 8081
#CMD ["java", "-jar", "user_center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]


FROM centos
COPY target/user_center-0.0.1-SNAPSHOT.jar /app/user_center-0.0.1-SNAPSHOT.jar
ADD jdk-8u381-linux-x64.tar.gz /app/
WORKDIR /app
#5.配置环境变量
ENV JAVA_HOME=/app/jdk1.8.0_381
ENV CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH=$JAVA_HOME/bin:$PATH
EXPOSE 8081
CMD ["java", "-jar", "user_center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]

