package eu.bde.sc4pilot.flink;

import java.io.InputStream;
import java.util.Properties;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

/**
 * This class provides the execution plan of a Flink job for the map matching of taxis 
 * to the road segments and an aggregation functions for the computation of the average
 * speed on a road segment within a time window.
 * Flink subtasks list:
 * 1) source(), read the data from a Kafka topic
 * 2) map(), map-match the vehicles to the road segments
 * 3) keyBy(road_segment_id).window(5 min.).apply(average_speed), compute the average speed 
 * 4) sink(), store the data in Elasticsearch
 *  
 * @author Luigi Selmi
 *
 */
public class FlinkFcdConsumer {
  
  private static String KAFKA_TOPIC_PARAM_NAME = "topic";
  private static String KAFKA_TOPIC_PARAM_VALUE = null;
  private static String TIME_WINDOW_PARAM_NAME = "window";
  private static int TIME_WINDOW_PARAM_VALUE = 0;
  private static final Logger log = LoggerFactory.getLogger(FlinkFcdConsumer.class);

  public static void main(String[] args) throws Exception {
    
    ParameterTool parameter = ParameterTool.fromArgs(args);
    
    if (parameter.getNumberOfParameters() < 2) {
      throw new IllegalArgumentException("The application needs two arguments. The first is the name of the kafka topic from which it has to \n"
          + "fetch the data. The second argument is the size of the window, in seconds, to which the aggregation function must be applied. \n");
    }
    
    KAFKA_TOPIC_PARAM_VALUE = parameter.get(KAFKA_TOPIC_PARAM_NAME);
    TIME_WINDOW_PARAM_VALUE = parameter.getInt(TIME_WINDOW_PARAM_NAME, TIME_WINDOW_PARAM_VALUE);
    
    Properties properties = null;
    
    try (InputStream props = Resources.getResource("consumer.props").openStream()) {
      properties = new Properties();
      properties.load(props);
      
    }
    
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // set the time characteristic to include an event in a window (event time|ingestion time|processing time) 
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    
    FlinkKafkaConsumer010<FcdTaxiEvent> nrtFcdConsumer =
               new FlinkKafkaConsumer010<FcdTaxiEvent>(KAFKA_TOPIC_PARAM_VALUE, new FcdTaxiSchema(), properties);
    
    // gets the data from the kafka topic (avro binary data)
    DataStreamSource<FcdTaxiEvent> stream = env
        .addSource(nrtFcdConsumer);
    
    stream.print();
    
    // Start the data generator
    //DataStream<FcdTaxiEvent> taxiEventStream = env.addSource(new FcdTaxiSource(input, maxEventDelay, servingSpeedFactor));
    
    env.execute("Flink FCD Consumer");

  }

}
