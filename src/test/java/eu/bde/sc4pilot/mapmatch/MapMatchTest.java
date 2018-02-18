package eu.bde.sc4pilot.mapmatch;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import eu.bde.sc4pilot.flink.FcdTaxiEvent;
import eu.bde.sc4pilot.json.GpsRecord;

public class MapMatchTest {

  @Before
  public void setUp() throws Exception {
  }
  /**
   * This test can be used to check whether the connection to Rserve and PostGis works.
   * The code is commented so the build can be performed when Rserve is not available.
   * @throws MalformedURLException
   * @throws IOException
   * @throws REXPMismatchException
   */
  @Test
  public void testMapMatch() throws MalformedURLException, IOException, REXPMismatchException {
    /*
    FcdTaxiEvent event = new FcdTaxiEvent();
    event.setDeviceId(69510);
    event.setTimestamp(DateTime.parse("2013-07-09 07:50:53.000", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")));
    event.setLon(22.960117);
    event.setLat(40.604947000000003);
    event.setAltitude(2.0);
    event.setSpeed(42.0);
    event.setOrientation(253.89999399999999);
    event.setTransfer(-1);
    MapMatch matcher = new MapMatch();
    ArrayList<GpsRecord> matches = matcher.mapMatch(event, "172.18.0.2", 6311);
    GpsRecord match = matches.get(0);
    assertTrue("132270824".equals(match.getLink()));
    */
  }
  
}
