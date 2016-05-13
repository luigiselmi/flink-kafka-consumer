Pilot SC4 Flink-Kafka-Consumer [![Build Status](https://travis-ci.org/big-data-europe/pilot-sc4-flink-kafka-consumer.svg?branch=master)](https://travis-ci.org/big-data-europe/pilot-sc4-flink-kafka-consumer)
=====================
Flink consumer of a Kafka topic. 

## Description
The application consumes data from a Kafka topic using Apache Flink for processing. A stream coming from a Kafka topic is read within
a time window, transformed from a string containing a json array into a Flink tuple in order to be used in an aggregation function (average speed).
The result is printed to the console. 

##Documentation 
This project is a component of the pilot that address the 4th H2020 Societal Challenge: Smart Green and Integrated Transport. 
The pilot will provide a scalable and fault tolerant system to collect, process and store the data from sensors: GPS data from 
cabs and data from Bluetooth sensors in the city of Thessaloniki, Greece.

##Requirements 
The application reads the data from a Kafka topic so Apache Kafka must be installed and run as explained in [Apache Kafka Quick Start](http://kafka.apache.org/documentation.html#quickstart).
The data stream is fed by a consumer that fetches traffic data from the cabs in Thessaloniki, Greece. The software for the consumer is available on Github in the [kafka-client](https://github.com/luigiselmi/kafka-clients) repositoy.   

##Build 
The software is based on Maven and can be build from the project root folder simply running the command

    mvn install

##Install and Run 
The build creates a jar file with all the dependences and the configuration of the main class in the target folder. The application is configured to connect to a Kafka broker
that manages the topic in the property file, the default port is 9090. To start the producer two arguments must be passed to the application. The first argument is the Kafka topic i.e. the stream 
from which the data is fetched. The second argument is the time interval in which the aggregation function must be applied. As an example run the following command

    java -jar target/pilot-sc4-flink-kafka-consumer-0.0.1-SNAPSHOT-jar-with-dependencies.jar taxy 120

The application will start to read the traffic data from the "taxy" topic, aggregate the speed of taxies every two minutes in road segments in which the taxies are localized, and print the result (average speed per road segment in the time window) to the console.

##Usage 
section with a description and examples of the main use cases and the APIs exposed by the software

##License 
TBD

