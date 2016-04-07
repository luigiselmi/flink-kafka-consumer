package eu.bde.sc4pilot.flink;

import java.util.Properties;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer082;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;

import eu.bde.sc4pilot.flink.WindowWordCount.Splitter;

public class WindowTrafficData {

  public static void main(String[] args) throws Exception {

    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", "localhost:9090");
    
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    
    DataStreamSource<String> stream = env
        .addSource(new FlinkKafkaConsumer09<>("taxy-data", new SimpleStringSchema(), properties));
    
    stream.map(new MapFunction<String, String>() {
        @Override
        public String map(String value) throws Exception {
          return "Kafka and Flink says: " + value;
        }
      });
        
    stream.print();
    
    env.execute();
  }  
  
}
