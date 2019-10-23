# Dockerfile for Apache Kafka used by the SC4 Pilot 
#
# Run the following docker command to build an image 
#
# $ docker build -t bde2020/pilot-sc4-kafka:v0.1.0 .
#
# from https://github.com/big-data-europe/docker-kafka

FROM bde2020/kafka:2.11-0.10.2.0

MAINTAINER Karl-Heinz Sylla <karl-heinz.sylla@iais.fraunhofer.de>

RUN chmod +x /wait-for-step.sh
RUN chmod +x /execute-step.sh
RUN chmod +x /finish-step.sh

COPY healthcheck /
COPY kafka-startup.sh /
COPY entrypoint.sh /

EXPOSE 9092

ENTRYPOINT ["/entrypoint.sh"]

CMD [ "./kafka-startup.sh" ]
