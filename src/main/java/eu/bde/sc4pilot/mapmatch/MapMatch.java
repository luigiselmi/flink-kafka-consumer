package eu.bde.sc4pilot.mapmatch;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.flink.api.java.tuple.Tuple7;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.bde.sc4pilot.json.GpsRecord;
import eu.bde.sc4pilot.rutilis.RUtil;

/**
 * 
 * @author Luigi Selmi
 *
 */

public class MapMatch {
  
  
  private final Logger log = LoggerFactory.getLogger(MapMatch.class);
  
  /**
   * This method requires three parameters in the following order
   * 1) The data set to be map matched 
   * 2) IP address of Rserve. The default value is localhost
   * 3) Port number of Rserve. The default value is 6311
   * @param urlDataset
   * @param rserveHost IP address of Rserve. The default value is localhost
   * @param rservePort Port number of Rserve. The default value is 6311
   * @throws MalformedURLException
   * @throws IOException
   * @throws REXPMismatchException 
   */
  public ArrayList<GpsRecord> mapMatch(ArrayList<GpsRecord> gpsrecords, String rserveHost, int rservePort) throws MalformedURLException, IOException, REXPMismatchException { 
    ArrayList<GpsRecord> matchedRecords = null;
    RList l = null;
    // Connection to Rserve
    RConnection c = initRserve(rserveHost, rservePort);
    // Rserve root folder. Contains the R script with the functions used by the client (this class)
    // and the geographical data for the map matching
    final String RSERVE_HOME = "/home/sc4pilot/rserve";
    RUtil util = new RUtil();
    try {      
      
      l = util.createRListFromList(gpsrecords, false);
      
      // Evaluates R commands
      c.eval("setwd('" + RSERVE_HOME + "')");
      c.voidEval("loadPackages()");
      c.voidEval("road<-readGeoData()");
      c.assign("gpsdata", REXP.createDataFrame(l));
      c.voidEval("initgps<-initGpsData(gpsdata)");
      c.voidEval("gdata<-readGpsData(gpsdata)");
      RList matches = c.eval("match(road,initgps,gdata)").asList();
      matchedRecords = util.createListFromRList(matches);
      
    } catch (RserveException e) {   
      e.printStackTrace();
    } catch (REXPMismatchException e) {     
      e.printStackTrace();
    } finally {
      c.close();
      log.info("Rserve: closed connection.");
    }
    
    return matchedRecords;
  
  }
  
  private RConnection initRserve(String rserveHost, int rservePort) {
    RConnection c = null;
    try {
      c = new RConnection(rserveHost, rservePort);
      REXP x = c.eval("R.version.string");
      log.info(x.asString());;
      
    } catch (RserveException e) {     
      e.printStackTrace();
    } catch (REXPMismatchException e) {     
      e.printStackTrace();
    }
    return c;
  }

}
