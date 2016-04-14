package eu.bde.sc4pilot.flink;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple7;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import eu.bde.sc4pilot.json.GpsJsonReader;
import eu.bde.sc4pilot.json.GpsRecord;



public class WindowTrafficData {
 
  private static String INPUT_KAFKA_TOPIC = null;
  private static final Logger log = LoggerFactory.getLogger(WindowTrafficData.class);

  public static void main(String[] args) throws Exception {
    
    if (args.length < 1) {
      throw new IllegalArgumentException("Must connect to a Kafka topic that must be passed as first argument. \n");
    }
    INPUT_KAFKA_TOPIC = args[0];
    Properties properties = null;
    
    try (InputStream props = Resources.getResource("consumer.props").openStream()) {
      properties = new Properties();
      properties.load(props);
      
    }
    
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    
    // gets the data (json array) as a string
    DataStreamSource<String> stream = env
        .addSource(new FlinkKafkaConsumer09<>(INPUT_KAFKA_TOPIC, new SimpleStringSchema(), properties));
    
    // maps the data into Flink tuples    
    DataStream<Tuple7<String,String,Double,Double,Double,Double,Double>> streamTuples = stream.flatMap(new Json2Tuple());
    
    // define an aggregation function (such as average speed) to be applied in a specified window
    DataStream<Tuple2<String,Double>> averageSpeedStream = streamTuples
        .keyBy(GpsJsonReader.KEY)
        .timeWindow(Time.minutes(2),Time.minutes(1))
        .apply(new AverageSpeed());
    
    
    // write the result to the console or in a Kafka topic
    averageSpeedStream.print();
    
    env.execute("Window Traffic Data");
  }  
  /**
   * Transforms the input data, a string containing a json array, into a Flink tuple.
   * @author luigi
   *
   */
  public static class Json2Tuple implements FlatMapFunction<String, Tuple7<String,String,Double,Double,Double,Double,Double> > {

    @Override
    public void flatMap(String jsonString, Collector<Tuple7<String,String,Double,Double,Double,Double,Double>> out) throws Exception {
      ArrayList<GpsRecord> recs = GpsJsonReader.getGpsRecords(jsonString);
      Iterator<GpsRecord> irecs = recs.iterator();
      while (irecs.hasNext()) {
        GpsRecord record = irecs.next();
        Tuple7<String,String,Double,Double,Double,Double,Double> tp7 = new Tuple7<String,String,Double,Double,Double,Double,Double>();
        tp7.setField(INPUT_KAFKA_TOPIC, GpsJsonReader.KEY);
        tp7.setField(record.getTimestamp(), GpsJsonReader.RECORDED_TIMESTAMP);
        tp7.setField(record.getLat(), GpsJsonReader.LAT);
        tp7.setField(record.getLon(), GpsJsonReader.LON);
        tp7.setField(record.getAltitude(), GpsJsonReader.ALTITUDE);
        tp7.setField(record.getSpeed(), GpsJsonReader.SPEED);
        tp7.setField(record.getOrientation(), GpsJsonReader.ORIENTATION);
        out.collect(tp7);
      }
    }
    
  }
  /**
   * Aggregation function to calculate the average speed within a time window.
   * @author luigi
   *
   */
  public static class AverageSpeed implements WindowFunction<
                  Tuple7<String,String,Double,Double,Double,Double,Double>,
                  Tuple2<String,Double>,
                  Tuple,
                  TimeWindow> {

    @Override
    public void apply(
        Tuple key,
        TimeWindow window,
        Iterable<Tuple7<String, String, Double, Double, Double, Double, Double>> records,
        Collector<Tuple2<String, Double>> out) throws Exception {
      int count = 0;
      double speedAccumulator = 0.0;
      for (Tuple7<String, String, Double, Double, Double, Double, Double> record: records){
        double speed = record.getField(GpsJsonReader.SPEED);
        count++;
        speedAccumulator += speed;
      }
      double averageSpeed = speedAccumulator / count; 
      out.collect(new Tuple2<>(INPUT_KAFKA_TOPIC,averageSpeed));
    }
    
  }
  
}
