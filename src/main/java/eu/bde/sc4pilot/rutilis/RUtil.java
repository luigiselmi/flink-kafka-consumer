package eu.bde.sc4pilot.rutilis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.bde.sc4pilot.flink.FcdTaxiEvent;
import eu.bde.sc4pilot.json.GpsRecord;

/**
 * Provides methods to transform Java collections into R data frame
 * @author Luigi Selmi
 *
 */
public class RUtil {
  
  private final Logger log = LoggerFactory.getLogger(RUtil.class);
  
  /**
   *   
   * @param is
   * @param header
   * @return
   * @throws MalformedURLException
   * @throws IOException
   */
  public GpsColumns readCsv(InputStream is, boolean header) throws MalformedURLException, IOException {
    ArrayList<String> csvLines = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String recordLine;
    String headerLine;
    if(header) 
      headerLine = reader.readLine();
    while((recordLine = reader.readLine()) != null) {
      csvLines.add(recordLine);
    }
    
    int size = csvLines.size();
    
    GpsColumns gps = new GpsColumns(size);
    
    int [] deviceId = new int[size];
    String [] insertedTimestamp = new String[size];
    double [] longitude = new double[size];
    double [] latitude = new double[size]; 
    double [] altitude = new double[size];
    int [] speed = new int[size];
    double [] orientation = new double[size];
    String [] recordedTimestamp = new String[size];
    int [] valid = new int[size];
    int [] zoneid = new int[size];
    int [] transfer = new int[size];
    String [] unixTimestamp = new String[size];

    
    Iterator<String> icsvLines = csvLines.iterator();
    int recordNumber = 0;
    while (icsvLines.hasNext()) {
      String [] fields = icsvLines.next().split(",");
      deviceId[recordNumber] = Integer.valueOf(fields[0]);
      insertedTimestamp[recordNumber] = fields[1];
      longitude[recordNumber] = Double.valueOf(fields[2]);
      latitude[recordNumber] = Double.valueOf(fields[3]);
      altitude[recordNumber] = Double.valueOf(fields[4]);
      speed[recordNumber] = Integer.valueOf(fields[5]);
      orientation[recordNumber] = Double.valueOf(fields[6]);
      recordedTimestamp[recordNumber] = fields[7];
      valid[recordNumber] = Integer.valueOf(fields[8]);
      zoneid[recordNumber] = Integer.valueOf(fields[9]);
      transfer[recordNumber] = Integer.valueOf(fields[10]);
      unixTimestamp[recordNumber] = fields[11];
      recordNumber++;
    }
    
    gps.setDeviceId(deviceId);
    gps.setInsertedTimestamp(insertedTimestamp);
    gps.setLongitude(longitude);
    gps.setLatitude(latitude);
    gps.setAltitude(altitude);
    gps.setSpeed(speed);
    gps.setRecordedTimestamp(recordedTimestamp);
    gps.setValid(valid);
    gps.setZoneid(zoneid);
    gps.setTransfer(transfer);
    gps.setUnixTimestamp(unixTimestamp);
    
    return gps;
  }
  /**
   * Creates a R data frame from a CSV file  
   * @param is
   * @param header
   * @return
   * @throws MalformedURLException
   * @throws IOException
   */
  public RList createRListFromCsv(InputStream is, boolean header) throws MalformedURLException, IOException  {
    RList rlist = new RList();
    GpsColumns gps = readCsv(is, header);
  
    rlist.put(GpsColumns.names[0],new REXPInteger(gps.getDeviceId()));
    rlist.put(GpsColumns.names[1],new REXPString(gps.getInsertedTimestamp()));
    rlist.put(GpsColumns.names[2],new REXPDouble(gps.getLongitude()));
    rlist.put(GpsColumns.names[3],new REXPDouble(gps.getLatitude()));
    rlist.put(GpsColumns.names[4],new REXPDouble(gps.getAltitude()));
    rlist.put(GpsColumns.names[5],new REXPInteger(gps.getSpeed()));
    rlist.put(GpsColumns.names[6],new REXPDouble(gps.getOrientation()));
    rlist.put(GpsColumns.names[7],new REXPString(gps.getRecordedTimestamp()));
    rlist.put(GpsColumns.names[8],new REXPInteger(gps.getValid()));
    rlist.put(GpsColumns.names[9],new REXPInteger(gps.getZoneid()));
    rlist.put(GpsColumns.names[10],new REXPInteger(gps.getTransfer()));
    rlist.put(GpsColumns.names[11],new REXPString(gps.getUnixTimestamp()));
   
    return rlist;
  }
  
  /**
   * Creates an R data frame from an ArrayLst.
   * @return
   */
  public RList createRListFromList(ArrayList<GpsRecord> gpsrecords, boolean header) {
    RList rlist = new RList();
    
    int size = gpsrecords.size();
    
    int [] deviceId = new int[size];
    String [] insertedTimestamp = new String[size];
    double [] longitude = new double[size];
    double [] latitude = new double[size]; 
    double [] altitude = new double[size];
    int [] speed = new int[size];
    double [] orientation = new double[size];
    String [] recordedTimestamp = new String[size];
    int [] valid = new int[size];
    int [] zoneid = new int[size];
    int [] transfer = new int[size];
    String [] unixTimestamp = new String[size];
    String [] links = new String[size];
    
    Iterator<GpsRecord> igpsrecords = gpsrecords.iterator();
    int recordNumber = 0;
    while (igpsrecords.hasNext()) {
      GpsRecord gpsrecord = igpsrecords.next();
      deviceId[recordNumber] = Integer.valueOf( gpsrecord.getDeviceId() );
      insertedTimestamp[recordNumber] = gpsrecord.getTimestamp();
      longitude[recordNumber] = gpsrecord.getLon();
      latitude[recordNumber] = gpsrecord.getLat();
      altitude[recordNumber] = gpsrecord.getAltitude();
      speed[recordNumber] = gpsrecord.getSpeed();
      orientation[recordNumber] = gpsrecord.getOrientation();
      recordedTimestamp[recordNumber] = gpsrecord.getTimestamp();
      valid[recordNumber] = 1; // not set in the json gps data. taking the most common value from sample csv data set
      zoneid[recordNumber] = 20; // not set in the json gps data. taking the most common value from sample csv data set
      transfer[recordNumber] = gpsrecord.getTransfer();
      unixTimestamp[recordNumber] = gpsrecord.getTimestamp(); // not set in the json gps data, taking the same as recorded_timestamp
      recordNumber++;
    }
    
    rlist.put(GpsColumns.names[0],new REXPInteger(deviceId));
    rlist.put(GpsColumns.names[1],new REXPString(insertedTimestamp));
    rlist.put(GpsColumns.names[2],new REXPDouble(longitude));
    rlist.put(GpsColumns.names[3],new REXPDouble(latitude));
    rlist.put(GpsColumns.names[4],new REXPDouble(altitude));
    rlist.put(GpsColumns.names[5],new REXPInteger(speed));
    rlist.put(GpsColumns.names[6],new REXPDouble(orientation));
    rlist.put(GpsColumns.names[7],new REXPString(recordedTimestamp));
    rlist.put(GpsColumns.names[8],new REXPInteger(valid));
    rlist.put(GpsColumns.names[9],new REXPInteger(zoneid));
    rlist.put(GpsColumns.names[10],new REXPInteger(transfer));
    rlist.put(GpsColumns.names[11],new REXPString(unixTimestamp));
   
    return rlist;
  }
  /**
   * Transforms an RList of map-matched records to an array of GpsRecord
   * @param rlist
   * @return
   * @throws REXPMismatchException
   */
  public ArrayList<GpsRecord> createListFromRList(RList rlist) throws REXPMismatchException {
    ArrayList<GpsRecord> gpsrecords = new ArrayList<GpsRecord>();
    int [] deviceId = rlist.at("device_random_id").asIntegers();
    String [] insertedTimestamp = rlist.at("recorded_timestamp").asStrings();       
    String [] longitude = rlist.at("lon").asStrings();
    String [] latitude = rlist.at("lat").asStrings();
    String [] altitude = rlist.at("altitude").asStrings();
    int [] speed = rlist.at("speed").asIntegers();
    double [] orientation = rlist.at("orientation").asDoubles();
    int [] transfer = rlist.at("transfer").asIntegers();
    String [] link = rlist.at("osmids").asStrings();
    
    int size = deviceId.length;
    
    for (int i = 0; i < size; i++) {      
      GpsRecord gpsrecord = new GpsRecord();
      gpsrecord.setDeviceId(deviceId[i]);
      gpsrecord.setTimestamp(insertedTimestamp[i]);
      gpsrecord.setLon(Double.parseDouble(longitude[i]));
      gpsrecord.setLat(Double.parseDouble(latitude[i]));
      gpsrecord.setAltitude(Double.parseDouble(altitude[i]));
      gpsrecord.setSpeed(speed[i]);
      gpsrecord.setOrientation(orientation[i]);
      gpsrecord.setTransfer(transfer[i]);
      gpsrecord.setLink(link[i].replaceAll("[^0-9.]","")); // remove {}
      gpsrecords.add(gpsrecord);
    }
    
    return gpsrecords;
  }
  
  /**
   * Creates an R data frame from an object of type FcdTaxiEvent.
   * @return
   */
  public RList createRListFromEvent(FcdTaxiEvent event) {
	  RList rlist = new RList();
	  rlist.put(GpsColumns.names[0],new REXPInteger(event.getDeviceId()));
    rlist.put(GpsColumns.names[1],new REXPString(event.getTimestamp().toString()));
    rlist.put(GpsColumns.names[2],new REXPDouble(event.getLon()));
    rlist.put(GpsColumns.names[3],new REXPDouble(event.getLat()));
    rlist.put(GpsColumns.names[4],new REXPDouble(event.getAltitude()));
    rlist.put(GpsColumns.names[5],new REXPDouble(event.getSpeed()));
    rlist.put(GpsColumns.names[6],new REXPDouble(event.getOrientation()));
    rlist.put(GpsColumns.names[7],new REXPInteger(event.getTransfer()));
	  return rlist;
  }
}
