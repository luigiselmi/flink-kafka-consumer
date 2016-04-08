package eu.bde.sc4pilot.json;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GpsJsonReaderTest {

  GpsJsonReader reader = null;
  String jsonString = "";
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    reader = new GpsJsonReader();
    
    jsonString = IOUtils.toString(getClass().getResourceAsStream("gps.json"));
    
    
  }

  @Test
  public void testGetGpsRecord() {
    List<GpsRecord> records = reader.getGpsRecords(jsonString);
    Assert.assertTrue("Wrong records found", "2016-04-08 12:05:47.600".equals(records.get(0).getTimestamp()));
  }

}
