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
          gpsRecord.setLon(jsonRecord.get("lon").getAsString());
          gpsRecord.setLat(jsonRecord.get("lat").getAsString());
          gpsRecord.setLat(jsonRecord.get("altitude").getAsString());
          gpsRecord.setLat(jsonRecord.get("speed").getAsString());
          gpsRecord.setLat(jsonRecord.get("orientation").getAsString());
          gpsRecords.add(gpsRecord);
        }
    }
   
    return gpsRecords;
  }
  
  public static Tuple7<String,String,String,String,String,String,String> getGpsTuples(String jsonString) {
    //ArrayList<GpsRecord> gpsRecords = new ArrayList<GpsRecord>();
    
    JsonParser parser = new JsonParser();
    JsonElement element = parser.parse(jsonString);
    Tuple7<String,String,String,String,String,String,String> tp7 = null;
    if (element.isJsonArray()) {
        JsonArray jsonRecords = element.getAsJsonArray();        
        for (int i = 0; i < jsonRecords.size(); i++) {
          tp7 = new Tuple7<String,String, String, String, String, String, String>();
          JsonObject jsonRecord = jsonRecords.get(i).getAsJsonObject();
          tp7.setField(jsonString, 0);
          tp7.setField(jsonRecord.get("recorded_timestamp").getAsString(), 1);
          tp7.setField(jsonRecord.get("lon").getAsString(), 2);
          tp7.setField(jsonRecord.get("lat").getAsString(), 3);
          tp7.setField(jsonRecord.get("altitude").getAsString(), 4);
          tp7.setField(jsonRecord.get("speed").getAsString(), 5);
          tp7.setField(jsonRecord.get("orientation").getAsString(), 6);
        }
    }
   
    return tp7;
  }
  
  
}
