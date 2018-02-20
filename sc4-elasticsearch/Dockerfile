FROM elasticsearch:2.4.6

MAINTAINER Karl-Heinz Sylla <karl-heinz.sylla@iais.fraunhofer.de>

USER root

# Install vi for editing
RUN apt-get update && \
    apt-get install -y vim

#RUN chmod +x /wait-for-step.sh
#RUN chmod +x /execute-step.sh
#RUN chmod +x /finish-step.sh

COPY healthcheck /
COPY elasticsearch_fcd_mapping.json /
COPY elasticsearch-startup.sh /

#RUN chmod +x /healthcheck
#RUN chmod +x /elasticsearch-startup.sh

#RUN sysctl -w vm.max_map_count=262144

EXPOSE 9200
EXPOSE 9300

USER elasticsearch

ENV discovery.type=single-node
ENV cluster.name=elasticsearch

CMD ["sh", "/elasticsearch-startup.sh" ]
