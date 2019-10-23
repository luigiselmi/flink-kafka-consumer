package eu.bde.sc4pilot.json;

public class GpsRecord {
  private String jsonString; 
  private int deviceId = -1;
  private double lat = 0.0;
  private double lon = 0.0;
  private double altitude = 0.0;
  private int speed = 0;
  private double orientation = 0.0;
  private int transfer = 0;
  private String timestamp = "";
  private String link;
  
  
  public int getTransfer() {
    return transfer;
  }
  public void setTransfer(int transfer) {
    this.transfer = transfer;
  }
  public int getDeviceId() {
    return deviceId;
  }
  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }
  public String getLink() {
    return link;
  }
  public void setLink(String link) {
    this.link = link;
  }
  public String getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
  public double getLat() {
    return lat;
  }
  public void setLat(double lat) {
    this.lat = lat;
  }
  public double getLon() {
    return lon;
  }
  public void setLon(double lon) {
    this.lon = lon;
  }
  public double getAltitude() {
    return altitude;
  }
  public void setAltitude(double altitude) {
    this.altitude = altitude;
  }
  public int getSpeed() {
    return speed;
  }
  public void setSpeed(int speed) {
    this.speed = speed;
  }
  public double getOrientation() {
    return orientation;
  }
  public void setOrientation(double orientation) {
    this.orientation = orientation;
  }
  public String getJsonString() {
    return jsonString;
  }
  public void setJsonString(String jsonString) {
    this.jsonString = jsonString;
  }

}
