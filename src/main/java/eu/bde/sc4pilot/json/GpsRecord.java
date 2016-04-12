package eu.bde.sc4pilot.json;

public class GpsRecord {
  
  private String timestamp = "";
  private String lat = "";
  private String lon = "";
  private String altitude = "";
  private String speed = "";
  private String jsonString;
  
  public GpsRecord() {};
  
  public String getJsonString() {
    return jsonString;
  }

  public void setJsonString(String jsonString) {
    this.jsonString = jsonString;
  }

  public String getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
  public String getLat() {
    return lat;
  }
  public void setLat(String lat) {
    this.lat = lat;
  }
  public String getLon() {
    return lon;
  }
  public void setLon(String lon) {
    this.lon = lon;
  }
  public String getAltitude() {
    return altitude;
  }
  public void setAltitude(String altitude) {
    this.altitude = altitude;
  }
  public String getSpeed() {
    return speed;
  }
  public void setSpeed(String speed) {
    this.speed = speed;
  }

}
