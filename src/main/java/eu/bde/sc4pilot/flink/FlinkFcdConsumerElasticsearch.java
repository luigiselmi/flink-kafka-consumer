package eu.bde.sc4pilot.flink;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.ZoneId;
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
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.tuple.Tuple6;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.elasticsearch2.ElasticsearchSink;
import org.apache.flink.streaming.connectors.elasticsearch2.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch2.RequestIndexer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.util.Collector;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.Resources;

import eu.bde.sc4pilot.json.GpsJsonReader;
import eu.bde.sc4pilot.json.GpsRecord;
import eu.bde.sc4pilot.mapmatch.MapMatch;



/**
 * This class provides the execution plan of a Flink job. It reads the records
 * from a Kafka topic, determines from which cells the message were originated
 * within a bounding box, split the data according to the cell number and 
 * counts the number of events in each cell (records in a time window)
 * Flink subtasks list:
 * 1) source(), reads the data from a Kafka topic
 * 2) mapToGrid(), computes the cell in which the records are originated
 * 3) keyBy(cell number).window(5 min.).apply(count_events), makes one partition 
 *    for each cell, computes the number of records in each cell within the time window 
 * 4) sink(), print the data
 *  
 * @author Luigi Selmi
 *
 */
public class FlinkFcdConsumerElasticsearch {
	
  private static String KAFKA_TOPIC_PARAM_NAME = "topic";
  private static String KAFKA_TOPIC_PARAM_VALUE = null;
  private static String TIME_WINDOW_PARAM_NAME = "window";
  private static String HDFS_SINK_PARAM_NAME = "sink";
  private static String HDFS_SINK_PARAM_VALUE = null;
  private static int TIME_WINDOW_PARAM_VALUE = 0;
  private static final int MAX_EVENT_DELAY = 60; // events are at most 60 sec out-of-order.
  private static final Logger log = LoggerFactory.getLogger(FlinkFcdConsumerElasticsearch.class);

  public static void main(String[] args) throws Exception {
	  
	ParameterTool parameter = ParameterTool.fromArgs(args);
    
    if (parameter.getNumberOfParameters() < 2) {
      throw new IllegalArgumentException("The application needs two arguments, \n" +
      		 "the name of the Kafka topic, \n" +
             "the size of the window, in minutes. \n" );
    }
    
    KAFKA_TOPIC_PARAM_VALUE = parameter.get(KAFKA_TOPIC_PARAM_NAME);
    TIME_WINDOW_PARAM_VALUE = parameter.getInt(TIME_WINDOW_PARAM_NAME, TIME_WINDOW_PARAM_VALUE);
    HDFS_SINK_PARAM_VALUE = parameter.get(HDFS_SINK_PARAM_NAME);
    
    Properties properties = null;
    
    try (InputStream props = Resources.getResource("consumer.props").openStream()) {
      properties = new Properties();
      properties.load(props);
      
    }
    
    // set up streaming execution environment
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    
    // set the time characteristic to include an event in a window (event time|ingestion time|processing time) 
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    //env.getConfig().setAutoWatermarkInterval(1000);
    
    // create a Kafka consumer
	  FlinkKafkaConsumer010<FcdTaxiEvent> consumer = new FlinkKafkaConsumer010<FcdTaxiEvent>(
			KAFKA_TOPIC_PARAM_VALUE,
			new FcdTaxiSchema(),
			properties);
	
	  // assign a timestamp extractor to the consumer
	  consumer.assignTimestampsAndWatermarks(new FcdTaxiTSExtractor());
	
	  // create a FCD event data stream
	  DataStream<FcdTaxiEvent> events = env.addSource(consumer);
	  
	// map-match locations given as (longitude, latitude) pairs to  streets
	  //DataStream<Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>> streamMatchedTuples = events
	  DataStream<Tuple6<String,Double,Double, Double, Integer, String>> streamMatchedTuples = events
			  .flatMap(new MapMatcher())
			  .keyBy(8) // key by road segment identifier
			  .timeWindow(Time.minutes(TIME_WINDOW_PARAM_VALUE))
			  .apply(new RoadSegment());
			  
	
	  // Counts the events that happen in any cell within the bounding box
	  /*
	  DataStream<Tuple5<Integer, Double, Double, Integer, String>> boxBoundedEvents = events
			// match each event within the bounding box to grid cell
			.map(new GridCellMatcher())
			// partition by cell
			.keyBy(0)
			// build time window
			.timeWindow(Time.minutes(TIME_WINDOW_PARAM_VALUE))
			.apply(new EventCounter());
	*/
	  // stores the data in Elasticsearch
	  saveFcdDataElasticsearch(streamMatchedTuples);
	  
	  //streamMatchedTuples.print();
    
    env.execute("Read Taxi Data from Kafka");
  
  
  }
  
  /**
   * Counts the number of events..
   */
  /*
  public static class EventCounter implements WindowFunction<
	  Tuple2<Integer, Boolean>,       // input type (cell id, is within bb)
	  Tuple5<Integer, Double, Double, Integer, String>, // output type (cell id, counts, window time)
	  Tuple,                          // key type
	  TimeWindow>                     // window type
	{
    private static transient DateTimeFormatter timeFormatter =
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    
	  @SuppressWarnings("unchecked")
	  @Override
	  public void apply(
		  Tuple key,
		  TimeWindow window,
		  Iterable<Tuple2<Integer, Boolean>> gridCells,
		  Collector<Tuple5<Integer, Double, Double, Integer, String>> out) throws Exception {

		  int cellId = ((Tuple1<Integer>)key).f0;
		  double cellLat = GeoUtils.getCellLatitude(cellId);
		  double cellLon = GeoUtils.getCellLongitude(cellId);
		  String windowTime = timeFormatter.print(window.getEnd());
		
		  // counts all the records (number of vehicles) from the same cell 
		  // within the bounding box or outside (cell id = 0)
		  int cnt = 0;
		  for(Tuple2<Integer, Boolean> c : gridCells) {
			  cnt += 1;
		  }

		  out.collect(new Tuple5<>(cellId, cellLat, cellLon, cnt, windowTime));
	  }
  }
 */
  /**
   * Match locations to streets
   */
  public static class MapMatcher implements FlatMapFunction<FcdTaxiEvent, Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String> > {

    @Override
    public void flatMap(
        FcdTaxiEvent event,
        Collector<Tuple9<Integer, String, Double, Double, Double, Integer, Double, Integer,String>> out)
        throws Exception {
        
        MapMatch matcher = new MapMatch();
        // map match the locations
        ArrayList<GpsRecord> matchedRecs = matcher.mapMatch(event, "localhost", 6311);
        if(matchedRecs != null && ! matchedRecs.isEmpty()) {
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
  public static class RoadSegment implements WindowFunction<
                  Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>,
                  Tuple6<String, Double, Double, Double, Integer, String>,
                  Tuple,
                  TimeWindow> {

    @Override
    public void apply(
        Tuple key,
        TimeWindow window,
        Iterable<Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String>> records,
        Collector<Tuple6<String, Double, Double, Double, Integer, String>> out) throws Exception {
      int count = 0; // number of vehicles in the road segment within the time window
      String roadSegmentId = "N/A"; // OpenStreetMap identifier of a road segment (between two junctions)
      double averageSpeed = 0.0;
      double meanLatitude = 0.0;
      double meanLongitude = 0.0;
      
      for (Tuple9<Integer,String,Double,Double,Double,Integer,Double,Integer,String> tuple9: records){
        int speed = tuple9.getField(GpsJsonReader.SPEED);
        double longitude = tuple9.getField(2);
        double latitude = tuple9.getField(3);
        meanLatitude += latitude;
        meanLongitude += longitude;
        count++;
        averageSpeed += speed;
        roadSegmentId = tuple9.getField(GpsJsonReader.OSM_LINK);
      }
      
      averageSpeed = averageSpeed / count; 
      meanLongitude = meanLongitude / count;
      meanLatitude = meanLatitude / count;
      
      DateTimeFormatter timeFormatter =ISODateTimeFormat.dateHourMinuteSecond();
      String timestamp = timeFormatter.print(window.getEnd());
      out.collect(new Tuple6<>(roadSegmentId,meanLongitude, meanLatitude, averageSpeed, count, timestamp));
      System.out.println(roadSegmentId + ", " + meanLongitude + ", " + meanLatitude + ", " + averageSpeed + ", " + count + ", " + timestamp);
    }
    
  }
  
  /**
   * Assigns timestamps to FCD Taxi records.
   * Watermarks are a fixed time interval behind the max timestamp and are periodically emitted.
  */
  public static class FcdTaxiTSExtractor extends BoundedOutOfOrdernessTimestampExtractor<FcdTaxiEvent> {
	
	  public FcdTaxiTSExtractor() {
		  super(Time.seconds(MAX_EVENT_DELAY));
	  }
	
	  @Override
	  public long extractTimestamp(FcdTaxiEvent event) {
			return event.timestamp.getMillis();
	  }
  }
  
  /**
   * Stores the data in Elasticsearch  
   * @param inputStream
   * @throws UnknownHostException
   */
	public static void saveFcdDataElasticsearch(
		DataStream<Tuple6<String, Double, Double, Double, Integer, String>> inputStream) throws UnknownHostException {
		Map<String, String> config = new HashMap<>();
		// This instructs the sink to emit after every element, otherwise they would be
		// buffered
		config.put("bulk.flush.max.actions", "1");
		config.put("cluster.name", "pilot-sc4");

		List<InetSocketAddress> transports = new ArrayList<InetSocketAddress>();
		log.info("XXXXX (InetAddress.getByName(elasticsearch), 9300))");
		transports.add(new InetSocketAddress(InetAddress.getByName("localhost"), 9300));

		inputStream.addSink(new ElasticsearchSink<>(config, transports, new RoadSegmentInserter()));
    
	}
	
	public static class RoadSegmentInserter implements ElasticsearchSinkFunction<Tuple6<String, Double, Double, Double, Integer, String>> {
    
    @Override
    public void process(Tuple6<String, Double, Double, Double, Integer, String> record, RuntimeContext ctx,
        RequestIndexer indexer) {
       
        Map<String, Object> json = new HashMap<>();
        json.put("roadsegmentid", record.f0);
        json.put("location", record.f2.toString() + "," + record.f1.toString()); // lat,lon
        json.put("speed", record.f3);
        json.put("count", record.f4);
        json.put("timestamp", record.f5);

        IndexRequest rqst = Requests.indexRequest().index("thessaloniki").type("floating-cars").source(json);
      
        indexer.add(rqst);

    }

      
}



}
