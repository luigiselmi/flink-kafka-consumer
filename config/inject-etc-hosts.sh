#!/bin/bash

echo "# sc4-data-integrator-ui" | sudo tee -a /etc/hosts
echo "127.0.0.1 demo.big-data-europe.local flink-master.big-data-europe.local hdfs-namenode.big-data-europe.local hdfs-datanode.big-data-europe.local hue.big-data-europe.local kibana.big-data-europe.local" | sudo tee -a /etc/hosts;
