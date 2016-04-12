package eu.bde.sc4pilot.flink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import eu.bde.sc4pilot.json.GpsJsonReader;
import eu.bde.sc4pilot.json.GpsRecord;


public class WindowTrafficData {

  public static void main(String[] args) throws Exception {

    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", "localhost:9090");
    
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    
    DataStreamSource<String> stream = env
        .addSource(new FlinkKafkaConsumer09<>("taxy", new SimpleStringSchema(), properties));
        
    DataStream<List<Tuple2<String,String>>> result = stream.map(new Json2Tuple());
    
    result.print();
    
    env.execute("Window Traffic Data");
  }  
  
  public static class Json2Tuple implements MapFunction<String,List<Tuple2<String,String>>> {

    @Override
    public List<Tuple2<String,String>> map(String jsonString) throws Exception {
      ArrayList<Tuple2<String,String>> tuples = new ArrayList<Tuple2<String, String>>();
      ArrayList<GpsRecord> recs = GpsJsonReader.getGpsRecords(jsonString);
      Iterator<GpsRecord> irecs = recs.iterator();
      while (irecs.hasNext()) {
        GpsRecord record = irecs.next();
        Tuple2<String,String> tp2 = new Tuple2<String,String>();
        tp2.setField(record.getLat(), 0);
        tp2.setField(record.getLon(), 1);
        tuples.add(tp2);
      }
      return tuples;
    }
    
  }
  
}
