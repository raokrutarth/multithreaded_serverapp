/*
 * 
 * Project 5 Testing
 * 
 * @Author Rahul Reddy Sathi, L05, rsathi
 * 
 * @version 04/19/15
 * 
 * CHECKING FOLLOWING
 * 
 * Send an invalid command.
 * Test :RESET  >>>> PASSED
 * Test :PENDING_REQUESTS
 * Test request order for :PENDING_REQUESTS
 * Test request order for :PENDING_REQUESTS with #,FROM,*
 * Test request order for :PENDING_REQUESTS with #,*,TO
 * Test :SHUTDOWN     
 * Test a request with an invalid FROM
 * Test a request with an invalid TO
 * Test sending a request with an invalid delimiter
 * Test sending a request with an invalid number of fields
 * Test sending a request with FROM = *
 * Test sending a request with FROM = TO
 * Test a scenario where there is an exact match
 * Test a scenario where the second request has * as TO          
 * Same as "testAnyMatch" but with a different order of requests 
 * Test a scenario where there is an exact match with NAME,TO,FROM 
 * Test first-come-first-serve                                
 * Test a scenario where there are two requests with TO = * but FROM is the same
 * 
 * 
 */
import org.junit.*;
import static org.junit.Assert.*;
import java.net.SocketTimeoutException;
import java.io.IOException;

public class SafeWalkServerTest {
    final String ERR_INVALID_REQUEST = "ERROR: invalid request";
    final String HOST = "localhost";
    
    /**
     * Send an invalid command. 
     **/
    @Test
    public void testInvalidCommand() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, ":INVALID_COMMAND");
        Thread ct1 = new Thread(c1);
        ct1.start();
        ct1.join();
        
        assertEquals("ERROR: invalid command", c1.getResult());
        
        Client c2 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct2 = new Thread(c2);
        ct2.start();
        ct2.join();
        st.join();
    }
     
    /**
     * Test :RESET. 
     **/
    @Test (timeout=6000)
    public void testReset() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH");
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, ":RESET");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        assertEquals("ERROR: connection reset", c1.getResult());
        assertEquals("RESPONSE: success", c2.getResult());
        
        Client c3 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct3 = new Thread(c3);
        ct3.start();
        ct3.join();
        st.join();
    }
    
    /**
     * Test :LIST_PENDING_REQUESTS. 
     **/
    @Test (timeout=6000)
    public void testListPendingRequests() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH", true);
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, ":PENDING_REQUESTS,*,*,*");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        assertEquals("[[Rahul, LWSN, PUSH]]", c2.getResult());
        
        Client c3 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct3 = new Thread(c3);
        ct3.start();
        ct3.join();
        st.join();
    }
    
    /**
     * Test request order for :PENDING_REQUESTS.  
     **/
    @Test (timeout=6000)
    public void testListPendingRequestsOrder() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH", true);
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, "Krutarth,LWSN,EE", true);
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        Client c3 = new Client(HOST, port, ":PENDING_REQUESTS,*,*,*");
        Thread ct3 = new Thread(c3);
        ct3.start();
        ct3.join();
        
        assertEquals("[[Rahul, LWSN, PUSH], [Krutarth, LWSN, EE]]", c3.getResult());
        
        Client c4 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct4 = new Thread(c4);
        ct4.start();
        ct4.join();
        st.join();
    }
    
    /**
     * Test request order for :PENDING_REQUESTS with #,FROM,*
     **/
    @Test (timeout=6000)
    public void testListPendingRequestsWithFrom() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH", true);
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, "Krutarth,LWSN,EE", true);
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        Client c3 = new Client(HOST, port, ":PENDING_REQUESTS,#,LWSN,*");
        Thread ct3 = new Thread(c3);
        ct3.start();
        ct3.join();
        
        assertEquals("RESPONSE: # of pending requests from LWSN = 2", c3.getResult());
        
        Client c4 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct4 = new Thread(c4);
        ct4.start();
        ct4.join();
        st.join();
    }
    
    /**
     * Test request order for :PENDING_REQUESTS with #,*,TO
     **/
    @Test (timeout=6000)
    public void testListPendingRequestsWithTo() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH", true);
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, "Krutarth,LWSN,EE", true);
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        Client c3 = new Client(HOST, port, ":PENDING_REQUESTS,#,*,EE");
        Thread ct3 = new Thread(c3);
        ct3.start();
        ct3.join();
        
        assertEquals("RESPONSE: # of pending requests to EE = 1", c3.getResult());
        
        Client c5 = new Client(HOST, port, ":PENDING_REQUESTS,#,*,PUSH");
        Thread ct5 = new Thread(c5);
        ct5.start();
        ct5.join();
        
        assertEquals("RESPONSE: # of pending requests to PUSH = 1", c5.getResult());
        
        Client c4 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct4 = new Thread(c4);
        ct4.start();
        ct4.join();
        st.join();
    }
    
    @Test (timeout = 6000)
    
    public void testPendingReq() throws InterruptedException, IOException 
    {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH",true);
        Thread ct1 = new Thread(c1);
        ct1.start();        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, "name,CL50,PUSH",true);
        Thread ct2 = new Thread(c2);
        ct2.start();
        Thread.sleep(100);
        
        Client c3 = new Client(HOST, port, ":PENDING_REQUESTS,#,*,*");
        Thread ct3 = new Thread(c3);
        ct3.start();
        Thread.sleep(100);
        
        ct1.join();
        ct2.join();
        ct3.join();
        
        assertEquals("RESPONSE: # of pending requests = 2", c3.getResult());        
        
        Client shutDown = new Client(HOST, port, ":SHUTDOWN");
        Thread shutDownT = new Thread(shutDown);
        shutDownT.start();
        shutDownT.join();
        st.join();
    }
    
    /**
     * Test :SHUTDOWN. 
     **/
    @Test (timeout=6000)
    public void testShutdown() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH");
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        st.join();
        
        assertEquals("ERROR: connection reset", c1.getResult());
        assertEquals("RESPONSE: success", c2.getResult());
    }
    
    /**
     * Test a request with an invalid FROM. 
     **/
    @Test (timeout=6000)
    public void testInvalidFrom() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,FROM,PUSH");
        Thread ct1 = new Thread(c1);
        ct1.start();
        ct1.join();
        
        assertEquals(ERR_INVALID_REQUEST, c1.getResult());
        
        Client c2 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct2.join();
        st.join();
    }
    
    /**
     * Test a request with an invalid TO. 
     **/
    @Test (timeout=6000)
    public void testInvalidTo() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,TO");
        Thread ct1 = new Thread(c1);
        ct1.start();
        ct1.join();
        
        assertEquals(ERR_INVALID_REQUEST, c1.getResult());
        
        Client c2 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct2.join();
        st.join();
    }
    
    /**
     * Test sending a request with an invalid delimiter. 
     **/
    @Test (timeout=6000)
    public void testInvalidRequest1() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul:LWSN:TO");
        Thread ct1 = new Thread(c1);
        ct1.start();
        ct1.join();
        
        assertEquals(ERR_INVALID_REQUEST, c1.getResult());
        
        Client c2 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct2.join();
        st.join();
    }
    
    /**
     * Test sending a request with an invalid number of fields. 
     **/
    @Test (timeout=6000)
    public void testInvalidRequest2() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,0,0");
        Thread ct1 = new Thread(c1);
        ct1.start();
        ct1.join();
        
        assertEquals(ERR_INVALID_REQUEST, c1.getResult());
        
        Client c2 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct2.join();
        st.join();
    }
    
    /**
     * Test sending a request with FROM = *. 
     **/
    @Test (timeout=6000)
    public void testFromStar() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,*,PUSH");
        Thread ct1 = new Thread(c1);
        ct1.start();
        ct1.join();
        
        assertEquals(ERR_INVALID_REQUEST, c1.getResult());
        
        Client c2 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct2.join();
        st.join();
    }
    
    /**
     * Test sending a request with FROM = TO. 
     **/
    @Test (timeout=6000)
    public void testToEqualsFrom() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,PUSH,PUSH");
        Thread ct1 = new Thread(c1);
        ct1.start();
        ct1.join();
        
        assertEquals(ERR_INVALID_REQUEST, c1.getResult());
        
        Client c2 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct2.join();
        st.join();
    }
    
    /**
     * Test a scenario where there is an exact match. 
     **/
    @Test (timeout=6000)
    public void testExactMatch() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH");
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Client c2 = new Client(HOST, port, "Krutarth,LWSN,PUSH");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        assertEquals("RESPONSE: Krutarth,LWSN,PUSH", c1.getResult());
        assertEquals("RESPONSE: Rahul,LWSN,PUSH", c2.getResult());
        
        Client c3 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct3 = new Thread(c3);
        ct3.start();
        
        ct3.join();
        st.join();
    }
    /**
     * Test a scenario where there is an exact match with NAME,TO,FROM 
     **/
    @Test (timeout=6000)
    public void testExactMatchnametofrom() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH",true);
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Client c2 = new Client(HOST, port, "Rahul,LWSN,PUSH",true);
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        assertEquals(ERR_INVALID_REQUEST, c2.getResult());
       
        Client c3 = new Client(HOST, port, ":PENDING_REQUESTS,*,*,*");
        Thread ct3 = new Thread(c3);
        ct3.start();
        Thread.sleep(100);
        
        ct1.join();
        ct2.join();
        ct3.join();
        
        assertEquals("[[Rahul, LWSN, PUSH]]", c3.getResult());        
        
        Client shutDown = new Client(HOST, port, ":SHUTDOWN");
        Thread shutDownT = new Thread(shutDown);
        shutDownT.start();
        shutDownT.join();
        st.join();
        
    }
    
    /**
     * Test a scenario where the second request has * as TO..  
     **/
    @Test (timeout=6000)
    public void testAnyMatch() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,PUSH");
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, "Krutarth,LWSN,*");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        assertEquals("RESPONSE: Krutarth,LWSN,*", c1.getResult());
        assertEquals("RESPONSE: Rahul,LWSN,PUSH", c2.getResult());
        
        Client c3 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct3 = new Thread(c3);
        ct3.start();
        
        ct3.join();
        st.join();
    }
    
    /**
     * Same as "testAnyMatch" but with a different order of requests. 
     **/
    @Test (timeout=6000)
    public void testAnyMatch2() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,*");
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, "Krutarth,LWSN,PUSH");
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        assertEquals("RESPONSE: Krutarth,LWSN,PUSH", c1.getResult());
        assertEquals("RESPONSE: Rahul,LWSN,*", c2.getResult());
        
        Client c3 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct3 = new Thread(c3);
        ct3.start();
        
        ct3.join();
        st.join();
    }
    
    /**
     * Test first-come-first-serve. 
     **/
    @Test (timeout=00)
    public void testFCFS() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Varun,LWSN,PUSH");
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, "Rahul,LWSN,EE", true);
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        Thread.sleep(100);
        
        Client c3 = new Client(HOST, port, "Krutarth,LWSN,*");
        Thread ct3 = new Thread(c3);
        ct3.start();
        
        ct1.join();
        ct2.join();
        ct3.join();
        
        assertEquals("RESPONSE: Krutarth,LWSN,*", c1.getResult());
        assertEquals("RESPONSE: Varun,LWSN,PUSH", c3.getResult());
        
        Client c4 = new Client(HOST, port, ":PENDING_REQUESTS,*,*,*");
        Thread ct4 = new Thread(c4);
        ct4.start();
        ct4.join();
        
        assertEquals("[[Rahul, LWSN, EE]]", c4.getResult());
        
        Client c5 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct5 = new Thread(c5);
        ct5.start();
        
        ct5.join();
        st.join();
    }
    
    /**
     * Test a scenario where there are two requests with TO = * but FROM is the same. 
     **/
    @Test (timeout=6000)
    public void testToBothStar() throws InterruptedException, IOException {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "Rahul,LWSN,*", true);
        Thread ct1 = new Thread(c1);
        ct1.start();
        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, "Krutarth,LWSN,*", true);
        Thread ct2 = new Thread(c2);
        ct2.start();
        
        ct1.join();
        ct2.join();
        
        Client c3 = new Client(HOST, port, ":PENDING_REQUESTS,*,*,*");
        Thread ct3 = new Thread(c3);
        ct3.start();
        ct3.join();
        
        assertEquals("[[Rahul, LWSN, *], [Krutarth, LWSN, *]]", c3.getResult());
        
        Client c4 = new Client(HOST, port, ":SHUTDOWN");
        Thread ct4 = new Thread(c4);
        ct4.start();
        
        ct4.join();
        st.join();
    }
}
