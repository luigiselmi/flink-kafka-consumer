package eu.bde.sc4pilot.rutilis;

import java.util.ArrayList;
/**
 * Container of GPS data per colums
 * @author Luigi Selmi
 *
 */
public class GpsColumns {

  public static String [] names = {
      "device_id",
      "inserted_timestamp",
      "longitude", 
      "latitude", 
      "altitude",
      "speed", 
      "orientation", 
      "recorded_timestamp", 
      "valid", 
      "zoneid", 
      "transfer", 
      "unix_timestamp"}; 
    
    public String [] getNames() {
      return names;
    }
    
    private int size = 0;
    
    public GpsColumns(int size) {
      this.size = size;
    }
    
    private int [] deviceId;
    private String [] insertedTimestamp;
    private double [] longitude;
    private double [] latitude; 
    private double [] altitude;
    private int [] speed;
    private double [] orientation;
    private String [] recordedTimestamp;
    private int [] valid;
    private int [] zoneid;
    private int [] transfer;
    private String [] unixTimestamp;

    public int getSize() {
      return size;
    }
    public void setSize(int size) {
      this.size = size;
    }
    public int[] getDeviceId() {
      return deviceId;
    }
    public void setDeviceId(int[] deviceId) {
      this.deviceId = deviceId;
    }
    public String[] getInsertedTimestamp() {
      return insertedTimestamp;
    }
    public void setInsertedTimestamp(String[] insertedTimestamp) {
      this.insertedTimestamp = insertedTimestamp;
    }
    public double[] getLongitude() {
      return longitude;
    }
    public void setLongitude(double[] longitude) {
      this.longitude = longitude;
    }
    public double[] getLatitude() {
      return latitude;
    }
    public void setLatitude(double[] latitude) {
      this.latitude = latitude;
    }
    public double[] getAltitude() {
      return altitude;
    }
    public void setAltitude(double[] altitude) {
      this.altitude = altitude;
    }
    public int[] getSpeed() {
      return speed;
    }
    public void setSpeed(int[] speed) {
      this.speed = speed;
    }
    public double[] getOrientation() {
      return orientation;
    }
    public void setOrientation(double[] orientation) {
      this.orientation = orientation;
    }
    public String[] getRecordedTimestamp() {
      return recordedTimestamp;
    }
    public void setRecordedTimestamp(String[] recordedTimestamp) {
      this.recordedTimestamp = recordedTimestamp;
    }
    public int[] getValid() {
      return valid;
    }
    public void setValid(int[] valid) {
      this.valid = valid;
    }
    public int[] getZoneid() {
      return zoneid;
    }
    public void setZoneid(int[] zoneid) {
      this.zoneid = zoneid;
    }
    public int[] getTransfer() {
      return transfer;
    }
    public void setTransfer(int[] transfer) {
      this.transfer = transfer;
    }
    public String[] getUnixTimestamp() {
      return unixTimestamp;
    }
    public void setUnixTimestamp(String[] unixTimestamp) {
      this.unixTimestamp = unixTimestamp;
    }

    
}
