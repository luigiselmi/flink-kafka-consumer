# Dockerfile for Zookeeper used by the SC4 Pilot 
#
# Run the following docker command to build an image 
#
# $ docker build -t bde2020/pilot-sc4-zookeeper:v0.1.0 .

FROM java:8

MAINTAINER Karl-Heinz Sylla <karl-heinz.sylla@iais.fraunhofer.de>

# disable debconf frontend warnings
ENV DEBIAN_FRONTEND noninteractive

# Install utilities
RUN apt-get update && apt-get install -y netcat

# Zookeeper version to be used
ENV ZK_VERSION zookeeper-3.4.10

# Download distribution
RUN wget -q http://ftp.halifax.rwth-aachen.de/apache/zookeeper/"$ZK_VERSION"/"$ZK_VERSION".tar.gz -O /tmp/"$ZK_VERSION".tar.gz \
    && tar xfz /tmp/"$ZK_VERSION".tar.gz -C /opt \
    && rm /tmp/"$ZK_VERSION".tar.gz

# The common installation dirname to use zookeeper
RUN ln -s /opt/"$ZK_VERSION" /opt/zookeeper

# Define the run configuration
RUN cd /opt/zookeeper/conf \
    && cp -p zoo_sample.cfg zoo.cfg

EXPOSE 2181

COPY wait-for-step.sh /
COPY execute-step.sh /
COPY finish-step.sh /

COPY healthcheck /
COPY zookeeper-startup.sh /

CMD [ "./zookeeper-startup.sh" ]
