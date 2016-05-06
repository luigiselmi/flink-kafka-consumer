package eu.bde.sc4pilot.flink;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple7;
import org.apache.flink.api.java.tuple.Tuple8;
import org.apache.flink.api.java.tuple.Tuple9;
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
import eu.bde.sc4pilot.mapmatch.MapMatch;


/**
 * 
 * @author Luigi Selmi
 *
 */
public class WindowTrafficData {
 
  private static String INPUT_KAFKA_TOPIC = null;
  private static int TIME_WINDOW = 0;
  private static final Logger log = LoggerFactory.getLogger(WindowTrafficData.class);

  public static void main(String[] args) throws Exception {
    
    if (args.length < 2) {
      throw new IllegalArgumentException("The application needs two arguments. The first is the name of the kafka topic from which it has to \n"
          + "fetch the data. The second argument is the size of the window, in seconds, to which the aggregation function must be applied. \n");
    }
    
    INPUT_KAFKA_TOPIC = args[0];
    TIME_WINDOW = Integer.parseInt(args[1]);
    
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
    //DataStream<Tuple7<String,String,Double,Double,Double,Double,Double>> streamTuples = stream.flatMap(new Json2Tuple());
    
    // map match locations given as (longitude, latitude) pairs to  streets
    DataStream<Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>> streamMatchedTuples = stream.flatMap(new MapMatcher());
    
    // define an aggregation function (such as average speed) to be applied in a specified window
    /*
    DataStream<Tuple2<String,Double>> averageSpeedStream = streamTuples
        .keyBy(GpsJsonReader.KEY)
        .timeWindow(Time.seconds(TIME_WINDOW),Time.seconds(TIME_WINDOW))
        .apply(new AverageSpeed());
    */
    // print the matched record with the link to a street 
    streamMatchedTuples.print();
    
    // write the average speed to the console or in a Kafka topic
    //averageSpeedStream.print();
    
    env.execute("Window Traffic Data");
  }  
  /**
   * Transforms the input data, a string containing a json array, into a Flink tuple.
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
   * Match locations to streets
   */
  public static class MapMatcher implements FlatMapFunction<String,Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String> > {

    @Override
    public void flatMap(
        String jsonString,
        Collector<Tuple9<Integer, String, Double, Double, Double, Integer, Double, Integer,String>> out)
        throws Exception {
        // parse the string with the json records
        ArrayList<GpsRecord> recs = GpsJsonReader.getGpsRecords(jsonString);
        MapMatch matcher = new MapMatch();
        // map match the locations
        ArrayList<GpsRecord> matchedRecs = matcher.mapMatch(recs, "localhost", 6311);
        Iterator<GpsRecord> imatchedRecs = matchedRecs.iterator();
        while (imatchedRecs.hasNext()) {
          GpsRecord record = imatchedRecs.next();
          Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String> tp9 = new Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>();
          tp9.setField(record.getDeviceId(), GpsJsonReader.KEY);
          tp9.setField(record.getTimestamp(), GpsJsonReader.RECORDED_TIMESTAMP);
          tp9.setField(record.getLon(), GpsJsonReader.LON);
          tp9.setField(record.getLat(), GpsJsonReader.LAT);          
          tp9.setField(record.getAltitude(), GpsJsonReader.ALTITUDE);
          tp9.setField(record.getSpeed(), GpsJsonReader.SPEED);
          tp9.setField(record.getOrientation(), GpsJsonReader.ORIENTATION);
          tp9.setField(record.getTransfer(), GpsJsonReader.TRANSFER);
          tp9.setField(record.getLink(), GpsJsonReader.OSM_LINK);
          out.collect(tp9);
        }
    }
    
  }
  /**
   * Aggregation function to calculate the average speed within a time window.
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
