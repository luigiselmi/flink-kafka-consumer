Flink-Kafka-Consumer [![Build Status]()]()
=====================
Flink consumer of a kafka topic of traffic data. 

## Description
The project consumes data from a Kafka topic using Flink for processing. 

##Documentation 
This project is a component of the pilot that address the 4th H2020 societal challenge: Smart Green and Integrated Transport. 
The pilot will provide a scalable and fault tolerant system to collect, process and store the data from sensors: GPS data from 
cabs and data from Bluetooth sensors all in the city of Thessaloniki, Greece.

##Requirements 
Flink reads the data from a Kafka topic so Zookeeper and at least a Kafka server must be 
started before running a producer or consumer and the topic used by them must be created as in the Apache kafka documentation.
The project is based on Maven. The default topic for the cab data is "taxy".

##Build 
The software is built from the project root folder simply running the command Maven

    mvn install

##Install and Run 
The build creates a jar file with all the dependences and the configuration of the main class in the target folder. 
To start the producer from the root folder run the command

    java -jar target/flink-kafka-consumer-0.0.1-SNAPSHOT-jar-with-dependencies.jar

Flink will start to read the traffic data from the Kafka topic and print it to sysout.

##Usage 
section with a description and examples of the main use cases and the APIs exposed by the software

##License 
section about the type of license that applies 

