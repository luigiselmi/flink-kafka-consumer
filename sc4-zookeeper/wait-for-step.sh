#!/bin/bash

echo "XXX zookeeper wait-for-step.sh ENABLE_INIT_DAEMON = '$ENABLE_INIT_DAEMON' DELAY = '$DELAY'"
echo "XXX zookeeper XXXDELAY = '$XXXDELAY'"

if [ ! -z "$DELAY" ] ;then
    sleep $DELAY
fi

if [[ $ENABLE_INIT_DAEMON = "true" ]]
   then
        echo "Validating if step ${INIT_DAEMON_STEP} can start in pipeline"
        while true; do
            sleep 5
            echo -n '.'
            string=$(curl -s $INIT_DAEMON_BASE_URI/canStart?step=$INIT_DAEMON_STEP)
            echo XXX zookeeper curl rc=$? string "$string"
            [ "$string" = "true" ] && break
        done
        echo "Can start step ${INIT_DAEMON_STEP}"
fi
