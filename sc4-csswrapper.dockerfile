FROM bde2020/nginx-proxy-with-css

MAINTAINER Karl-Heinz Sylla <karl-heinz.sylla@iais.fraunhofer.de>

COPY config/nginx.conf /etc/nginx/nginx.conf
