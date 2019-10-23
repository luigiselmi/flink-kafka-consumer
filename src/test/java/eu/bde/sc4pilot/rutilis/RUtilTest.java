package eu.bde.sc4pilot.rutilis;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RUtilTest {

  @Before
  public void setUp() throws Exception {
  }
  /**
   * This test can be used to see whether the connection to Rserve works.
   * The code is commented so the build can be performed when Rserve is not available.
   * @throws RserveException
   * @throws REXPMismatchException
   */
  @Test
  public void testRConnection() throws RserveException, REXPMismatchException {
    /*
    RConnection c = getConnection();
    REXP x = c.eval("R.version.string");
    String message = x.asString();
    assertTrue("R version 3.2.3 (2015-12-10)".equals(message));
    */
  }
  
  private RConnection getConnection() {
    RConnection c = null;
    try {
      c = new RConnection("172.18.0.3", 6311);
      
      
    } catch (RserveException e) {     
      e.printStackTrace();
    } 
    
    return c;
  }

}
