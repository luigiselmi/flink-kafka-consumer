package eu.bde.sc4pilot.flink;

import java.io.IOException;
import java.io.InputStream;

import org.apache.avro.LogicalTypes.Date;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;


public class FcdTaxiEvent {
	
	private static transient DateTimeFormatter timeFormatter =
			DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	public static final String FCD_THESSALONIKI_SCHEMA = "fcd-record-schema.avsc";
  private static Schema schema = null; // avro schema used to send the messages in binary format to a Kafka topic
	
	public int deviceId = -1;
	public DateTime timestamp;
	public double lon = 0.0;
	public double lat = 0.0;
	public double altitude = 0.0;
	public double speed = 0;
	public double orientation = 0.0;
	public int transfer = 0;
	
	public FcdTaxiEvent(int deviceId, 
			            DateTime timestamp, 
			            double lon, 
			            double lat, 
			            double altitude, 
			            double speed, 
			            double orientation, 
			            int transfer) {
		this.deviceId = deviceId;
		this.timestamp = timestamp;
		this.lon = lon;
		this.lat = lat;
		this.altitude = altitude;
		this.speed = speed;
		this.orientation = orientation;
		this.transfer = transfer;
	}

	public FcdTaxiEvent() {}
	
	/*
	 * Creates one event from a json string
	 */
	public static FcdTaxiEvent fromJsonString(String jsonString) {
	  return FcdTaxiEventUtils.fromJsonString(jsonString);
	}
	
	/*
	 * Creates one event from a tab separated string
	 */
	public static FcdTaxiEvent fromString(String line) {
		return FcdTaxiEventUtils.fromString(line);
	}
	
	/*
   * Set up the schema of the messages that will be sent to a kafka topic.
   * Must be called before any transformation from json to binary or vice versa.
   */
  public static void setSchema() {
    try(
      InputStream schemaIs = Resources.getResource(FCD_THESSALONIKI_SCHEMA).openStream()){
      Schema.Parser parser = new Schema.Parser();
      schema = parser.parse(schemaIs);
    }
    catch(IOException ioe){
      ioe.printStackTrace();
    }
  }
  /*
   * Serialize the JSON data into the Avro binary format
   */
  public byte [] toBinary() {
    if(schema == null) {
      setSchema();
    }
    GenericData.Record avroRecord = new GenericData.Record(schema);
    Injection<GenericRecord, byte[]> binaryRecord = GenericAvroCodecs.toBinary(schema);
    avroRecord.put("device_id", deviceId);
    avroRecord.put("timestamp", timestamp.toString(timeFormatter));
    avroRecord.put("lon", lon);
    avroRecord.put("lat", lat);
    avroRecord.put("altitude", altitude);
    avroRecord.put("speed", speed);
    avroRecord.put("orientation", orientation);
    avroRecord.put("transfer", transfer);
    byte[] value = binaryRecord.apply(avroRecord);
    return value;
  }
  
  public static FcdTaxiEvent fromBinary(byte [] avro) {
    if(schema == null) {
      setSchema();
    }
    FcdTaxiEvent event = new FcdTaxiEvent();
    Injection<GenericRecord, byte []> recordInjection = GenericAvroCodecs.toBinary(schema);
    GenericRecord jsonRecord = recordInjection.invert(avro).get();
    int device_id = (Integer) jsonRecord.get("device_id");
    event.deviceId = device_id;
    String timestamp = ((Utf8)jsonRecord.get("timestamp")).toString();
    event.timestamp = DateTime.parse(timestamp, timeFormatter);
    event.lon = (Double) jsonRecord.get("lon");
    event.lat = (Double) jsonRecord.get("lat");
    event.altitude = (Double) jsonRecord.get("altitude");
    event.speed = (Double) jsonRecord.get("speed");
    event.orientation = (Double) jsonRecord.get("orientation");
    event.transfer = (Integer) jsonRecord.get("transfer");
    return event;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(deviceId).append("\t");
    sb.append(timestamp.toString(timeFormatter)).append("\t");
    sb.append(lon).append("\t");
    sb.append(lat).append("\t");
    sb.append(altitude).append("\t");
    sb.append(speed).append("\t");
    sb.append(orientation).append("\t");
    sb.append(transfer);

    return sb.toString();
  }
	/**
	 * Two taxi events are equal if the deviceId and the timestamp are the same (minimum rules)
	 */
	@Override
	public boolean equals(Object other) {
		return other instanceof FcdTaxiEvent &&
				this.deviceId == ((FcdTaxiEvent) other).deviceId && // the deviceId is randomly generated 
		    this.timestamp.toString(timeFormatter).equals( ((FcdTaxiEvent) other).timestamp.toString(timeFormatter) );
	}

	@Override
	public int hashCode() {
		return this.deviceId;
	}

}
