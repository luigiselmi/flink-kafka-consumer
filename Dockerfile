# Docker file for the FCD traffic Monitoring. The application is part of the BDE SC4 Pilot.
# It connects to a Kafka topic, sends the data to an R server to map match the coordinates
# of vehicles to the road segments and then aggregates the vehicles speed to calculate the 
# average speed in each road segment. Finally the data is sent to Elasticsearch (Flink sink). 
# 1) Build an image using this docker file. Run the following docker command
# 
#    $ docker build -t bde2020/pilot-sc4-monitoring:v0.1.0 .
#
# 2) Run a container and submit the application to the Flink Job Manager.
#
# docker run --rm --name fcd-flink-app --link flink-master:flink-master -it bde2020/pilot-sc4-monitoring:v0.1.0 /bin/bash

# Pull the base image
FROM bde2020/flink-maven-template:1.4.0-hadoop2.7

MAINTAINER Luigi Selmi <luigiselmi@gmail.com>

# Install  network tools (ifconfig, netstat, ping, ip)
RUN apt-get update && \
    apt-get install -y net-tools && \
    apt-get install -y iputils-ping && \
    apt-get install -y iproute2

ENV FLINK_APPLICATION_JAR_NAME pilot-sc4-monitoring-0.10.0-SNAPSHOT-jar-with-dependencies.jar
ENV FLINK_APPLICATION_ARGS "--topic taxi --window 5"
