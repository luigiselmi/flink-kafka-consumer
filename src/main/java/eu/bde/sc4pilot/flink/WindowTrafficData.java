package eu.bde.sc4pilot.flink;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple7;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSink;
import org.apache.flink.streaming.connectors.elasticsearch.IndexRequestBuilder;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import eu.bde.sc4pilot.json.GpsJsonReader;
import eu.bde.sc4pilot.json.GpsRecord;
import eu.bde.sc4pilot.mapmatch.MapMatch;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;


/**
 * 
 * @author Luigi Selmi
 *
 */
public class WindowTrafficData {
 
  private static String KAFKA_TOPIC_PARAM_NAME = "topic";
  private static String KAFKA_TOPIC_PARAM_VALUE = null;
  private static String TIME_WINDOW_PARAM_NAME = "window";
  private static int TIME_WINDOW_PARAM_VALUE = 0;
  private static final Logger log = LoggerFactory.getLogger(WindowTrafficData.class);

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
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
    
    // gets the data from the kafka topic (json array) as a string
    DataStreamSource<String> stream = env
        .addSource(new FlinkKafkaConsumer09<>(KAFKA_TOPIC_PARAM_VALUE, new SimpleStringSchema(), properties));
    
    // maps the data into Flink tuples    
    //DataStream<Tuple7<String,String,Double,Double,Double,Double,Double>> streamTuples = stream.flatMap(new Json2Tuple());
    
    // map match locations given as (longitude, latitude) pairs to  streets
    DataStream<Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>> streamMatchedTuples = stream.flatMap(new MapMatcher());
    
    // define an aggregation function (such as average speed per road segment) to be applied in a specified window
    DataStream<Tuple4<String,Double,String,Integer>> averageSpeedStream = streamMatchedTuples
        .keyBy(GpsJsonReader.OSM_LINK)
        .timeWindow(Time.seconds(TIME_WINDOW_PARAM_VALUE),Time.seconds(60))
        .apply(new AverageSpeed());
    
    // print the matched record with the link to a street 
    streamMatchedTuples.print();
    
    // write the average speed in a time window to the console (or in a Kafka topic)
    // topic name, average speed in the road segment, road segment identifier, number of gps messages in the time window
    averageSpeedStream.print();
    
    // Save gps data into Elasticsearch
    saveGpsData(streamMatchedTuples);
   
    
    env.execute("Thessaloniki Floating Cars Data");
  } 
  /*
   * Save the gps records plus the road segment identifiers from the map match into Elasticsearch (v.1.7.3)
   */
  public static void saveGpsData(DataStream<Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>> inputStream) throws UnknownHostException {
	  Map<String, String> config = new HashMap<>();
	// This instructs the sink to emit after every element, otherwise they would be buffered
	config.put("bulk.flush.max.actions", "1");
	config.put("cluster.name", "pilot-sc4");
	
	List<TransportAddress> transports = new ArrayList<TransportAddress>();
	transports.add(new InetSocketTransportAddress("127.0.0.1", 9300));
	//transports.add(new InetSocketTransportAddress("node-2", 9300));

	inputStream.addSink(new ElasticsearchSink<Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>>(config, transports, new IndexRequestBuilder<Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>>() {
		@Override
		public IndexRequest createIndexRequest(
				Tuple9<Integer, String, Double, Double, Double, Integer, Double, Integer, String> gpsrecord,
				RuntimeContext ctx) {
			Map<String, Object> json = new HashMap<>();
	        json.put("id", gpsrecord.getField(0));
	        json.put("timestamp", gpsrecord.getField(1));
	        json.put("location", String.valueOf(gpsrecord.getField(3)) + "," + String.valueOf(gpsrecord.getField(2))); // lat,lon
	        json.put("altitude", gpsrecord.getField(4));
	        json.put("speed", gpsrecord.getField(5));
	        json.put("orientation", gpsrecord.getField(6));
	        json.put("transfer", gpsrecord.getField(7));
	        json.put("roadsegment", gpsrecord.getField(8));

	        return Requests.indexRequest()
	                .index("thessaloniki")
	                .type("floating-cars")
	                .source(json);

		}
	}));
  }

  /**
   * Transforms the input data, a string containing a json array, into a Flink tuple.
   */
  /*
  public static class Json2Tuple implements FlatMapFunction<String, Tuple7<String,String,Double,Double,Double,Double,Double> > {

    @Override
    public void flatMap(String jsonString, Collector<Tuple7<String,String,Double,Double,Double,Double,Double>> out) throws Exception {
      ArrayList<GpsRecord> recs = GpsJsonReader.getGpsRecords(jsonString);
      Iterator<GpsRecord> irecs = recs.iterator();
      while (irecs.hasNext()) {
        GpsRecord record = irecs.next();
        Tuple7<String,String,Double,Double,Double,Double,Double> tp7 = new Tuple7<String,String,Double,Double,Double,Double,Double>();
        tp7.setField(KAFKA_TOPIC_PARAM_VALUE, GpsJsonReader.KEY);
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
  */
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
   * It returns 
   * 1) the topic name
   * 2) the average speed in a road segment within the time window set
   * 3) the road segment identifier (from OpenStreetMap)
   * 4) the number of records used, that is the number of times a taxi sent a gps message from that same road segment
   *
   */
  public static class AverageSpeed implements WindowFunction<
                  Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>,
                  Tuple4<String,Double,String,Integer>,
                  Tuple,
                  TimeWindow> {

    @Override
    public void apply(
        Tuple key,
        TimeWindow window,
        Iterable<Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>> records,
        Collector<Tuple4<String, Double, String, Integer>> out) throws Exception {
      int count = 0;
      String roadSegmentId = "N/A"; // OpenStreetMap identifier of a road segment (between two junctions)
      double speedAccumulator = 0.0;
      for (Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String> tuple9: records){
        int speed = tuple9.getField(GpsJsonReader.SPEED);
        count++;
        speedAccumulator += speed;
        roadSegmentId = tuple9.getField(GpsJsonReader.OSM_LINK);
      }
      double averageSpeed = speedAccumulator / count; 
      out.collect(new Tuple4<>(KAFKA_TOPIC_PARAM_VALUE,averageSpeed, roadSegmentId,count));
    }
    
  }
  
    
}
