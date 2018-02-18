Pilot SC4 - Traffic Monitoring [![Build Status](https://travis-ci.org/big-data-europe/pilot-sc4-flink-kafka-consumer.svg?branch=master)](https://travis-ci.org/big-data-europe/pilot-sc4-flink-kafka-consumer)
============================= 

## Description
The job consumes data from a Kafka topic using Apache Flink for processing. A stream coming from a Kafka topic is read
 within a time window, transformed from a string containing a json array into a Flink tuple in order to be used in an
 aggregation function (average speed). The result is printed to the console. 

## Documentation 
This project is a component of the pilot that addresses the 4th H2020 Societal Challenge: Smart Green and Integrated Transport. 
The pilot will provide a scalable and fault tolerant system to collect, process and store the data from taxis equipped
with GPS devices from the city of Thessaloniki, Greece.

## Dependencies 
The job reads the data from a Kafka topic so Apache Kafka must be installed and run as explained in [Apache Kafka Quick Start](http://kafka.apache.org/documentation.html#quickstart).
The data stream is fed by a consumer that fetches traffic data from the cabs in Thessaloniki, Greece. The software for the producer is available on Github in the [pilot-sc4-kafka-producer](https://github.com/big-data-europe/pilot-sc4-kafka-producer) repositoy. 
The job depends also on a Rserve server that receives R commands for a map matching algorithm. The project for the Rserve is [pilot-sc4-postgis](https://github.com/big-data-europe/pilot-sc4-postgis). Finally an instance of Elasticsearch must be started for the storage.   

## Build 
The software is based on Maven and can be build from the project root folder simply running the command

    $ mvn install

## Install and Run 
The build creates a jar file with all the dependences and the configuration of the main class in the target folder. The job is configured to connect to a Kafka broker
that manages the topic in the property file, the default port is 9090. To start the producer two arguments must be passed to the job. One argument is the Kafka topic i.e. the stream 
from which the data is fetched. The other argument is the time interval in which the aggregation function must be applied, in minutes. As an example run the following command

    $ java -jar target/pilot-sc4-monitoring-0.1.0-SNAPSHOT-jar-with-dependencies.jar --topic taxi --window 5

The job will start to read the traffic data from the "taxi" topic, aggregate the speed of taxies every two minutes in road segments in which the taxies are localized, and print the result (average speed per road segment in the time window) to the console.
The job can also be started from the Flink JobManager, see the [Flink JobManager Quick Star Setup](https://ci.apache.org/projects/flink/flink-docs-release-1.0/quickstart/setup_quickstart.html) to learn how to do it. Once Flink is started you can submit a job uploading the project jar file and setting the following parameters

    Entry Class: eu.bde.sc4pilot.flink.FlinkFcdConsumerElasticsearch
    Program Arguments: --topic taxi --window 5

## Elasticsearch 
The job saves the data into Elasticsearch, a document database for json data. The version supported by Apache Flink 1.2.1 is [Elasticsearch 2.4.6](https://www.elastic.co/downloads/past-releases/elasticsearch-2-4-6). Install Elasticsearch and start it from the root folder

    $ bin/elasticsearch -d -p pid

The commnad will start Elasticsearch in background and the process id in the pid file. Once Elasticsearch has started a schema,called mapping in Elasticsearch, must be created to inform Elasticsearch the data types used and to index the fields. Currently a mapping is available to store the Floating Cars Data. In  order to create the index, e.g. "thessaloniki-monitoring", use the command 

    $ curl -XPUT 'localhost:9200/thessaloniki-monitoring'

and then send the schema using the command

    $ curl -XPUT "http://localhost:9200/thessaloniki-monitoring/_mapping/floating-cars" -d @elasticsearch_fcd_mapping.json
    
Elasticsearch is now ready to store and index the json data. You can check the index that has been creted sending the following request using the browser or curl

    $ curl http://localhost:9200/thessaloniki-monitoring
    
Before starting the consumer check the value of the property 'cluster.name' in the Elasticsearch configuration file 'elasticsearch.yml'. 
The same key-value pair, e.g cluster.name: pilot-sc4, must be used in the consumer's configuration paramaters. 

## Kibana
The data stored in Elasticsearch can be easily visualized using Kibana. The version supported by Elasticsearch 2.4.6 is [Kibana 4.6.6](https://www.elastic.co/downloads/past-releases/kibana-4-6-6). 
Once Kibana is installed and start, an index pattern must be defined so that Kibana can find and retrieve the data from Elasticsearch. The index pattern must match with the index name used. In our example the index name is "thessaloniki-monitoring" as in the path used
to create the index. Please follow the instruction in [Getting started with Kibana](https://www.elastic.co/guide/en/kibana/current/getting-started.html) to learn how to create different types of visualizations such as vertical bar charts, pie charts, tile maps and more. Check the format of the date used by Kibana and change in case it is different from the one used in your data. 
 

## License 
TBD

