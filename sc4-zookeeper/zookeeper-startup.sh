#!/bin/bash

set -e

echo `date` $0
echo "XXX zookeeper INIT_DAEMON_BASE_URI = '$INIT_DAEMON_BASE_URI'   DELAY = '$DELAY'"
echo "XXX zookeeper ENABLE_INIT_DAEMON = '$ENABLE_INIT_DAEMON'  INIT_DAEMON_STEP = '$INIT_DAEMON_STEP'"

/wait-for-step.sh
/execute-step.sh

if [[ $ENABLE_INIT_DAEMON = "true" ]] ;then
    (
        while ! ( /healthcheck ) ;do echo expect to become healthy; sleep 1; done
        echo $0 initialisation finished, service is healthy
        /finish-step.sh
    ) &
fi
    
echo `date` $0 : zkServer.sh start-foreground 
exec /opt/zookeeper/bin/zkServer.sh start-foreground
