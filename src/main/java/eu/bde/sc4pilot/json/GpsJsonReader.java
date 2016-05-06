package eu.bde.sc4pilot.json;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.java.tuple.Tuple6;
import org.apache.flink.api.java.tuple.Tuple7;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GpsJsonReader {
  // Position in a Flink Tuple. Position '0' is reserved for the key
  public static final int KEY = 0;
  public static final int RECORDED_TIMESTAMP = 1;
  public static final int LON = 2;
  public static final int LAT = 3;
  public static final int ALTITUDE = 4;
  public static final int SPEED = 5;
  public static final int ORIENTATION = 6;  
  public static final int TRANSFER = 7;
  public static final int OSM_LINK = 8;
  
  /**
   * Gps data are available as a Json array or records.
   * @param jsonString
   * @return the collection of GPS records
   */
  public static ArrayList<GpsRecord> getGpsRecords(String jsonString) {
    ArrayList<GpsRecord> gpsRecords = new ArrayList<GpsRecord>();
    JsonParser parser = new JsonParser();
    JsonElement element = parser.parse(jsonString);
    if (element.isJsonArray()) {
        JsonArray jsonRecords = element.getAsJsonArray();        
        for (int i = 0; i < jsonRecords.size(); i++) {            
          GpsRecord gpsRecord = new GpsRecord();  
          JsonObject jsonRecord = jsonRecords.get(i).getAsJsonObject();
          gpsRecord.setJsonString(jsonString);
          gpsRecord.setTimestamp(jsonRecord.get("recorded_timestamp").getAsString());
          gpsRecord.setLon(jsonRecord.get("lon").getAsDouble());
          gpsRecord.setLat(jsonRecord.get("lat").getAsDouble());
          gpsRecord.setAltitude(jsonRecord.get("altitude").getAsDouble());
          gpsRecord.setSpeed(jsonRecord.get("speed").getAsInt());
          gpsRecord.setOrientation(jsonRecord.get("orientation").getAsDouble());
          gpsRecords.add(gpsRecord);
        }
    }
   
    return gpsRecords;
  }
  
  public static List<Tuple6<String,String,String,String,String,String>> getGpsTuples(String jsonString) {
    ArrayList<Tuple6<String,String,String,String,String,String>> gpsRecords = new ArrayList<Tuple6<String,String,String,String,String,String>>();
    JsonParser parser = new JsonParser();
    JsonElement element = parser.parse(jsonString);
    Tuple6<String,String,String,String,String,String> tp6 = null;
    if (element.isJsonArray()) {
        JsonArray jsonRecords = element.getAsJsonArray();        
        for (int i = 0; i < jsonRecords.size(); i++) {
          tp6 = new Tuple6<String, String, String, String, String, String>();
          JsonObject jsonRecord = jsonRecords.get(i).getAsJsonObject();          
          tp6.setField(jsonRecord.get("recorded_timestamp").getAsString(), RECORDED_TIMESTAMP);
          tp6.setField(jsonRecord.get("lon").getAsString(), LON);
          tp6.setField(jsonRecord.get("lat").getAsString(), LAT);
          tp6.setField(jsonRecord.get("altitude").getAsString(), ALTITUDE);
          tp6.setField(jsonRecord.get("speed").getAsString(), SPEED);
          tp6.setField(jsonRecord.get("orientation").getAsString(), ORIENTATION);
          gpsRecords.add(tp6);
        }
    }
   
    return gpsRecords;
  }
  
  
}
