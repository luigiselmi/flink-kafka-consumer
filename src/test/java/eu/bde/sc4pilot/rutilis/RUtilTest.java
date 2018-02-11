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
   * This test can be used to whether the connection to Rserve works.
   * The code is commented so the build can be performed when Rserve is not available.
   * @throws RserveException
   * @throws REXPMismatchException
   */
  @Test
  public void testRConnection() throws RserveException, REXPMismatchException {
    /*
    RConnection c = getConnection();
    REXP x = c.eval("R.version.string");
    assertTrue("R version 3.1.1 (2014-07-10)".equals(x.asString()));
    */
  }
  
  private RConnection getConnection() {
    RConnection c = null;
    try {
      c = new RConnection("localhost", 6311);
      
      
    } catch (RserveException e) {     
      e.printStackTrace();
    } 
    
    return c;
  }

}
