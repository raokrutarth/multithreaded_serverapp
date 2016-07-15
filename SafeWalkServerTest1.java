
import org.junit.*;
import static org.junit.Assert.*;
import java.net.SocketTimeoutException;
import java.io.IOException;

public class SafeWalkServerTest1 
{
    final String ERR_INVALID_REQUEST = "ERROR: invalid request";
    final String HOST = "localhost";

    @Test (timeout = 10000)
    public void test1() throws InterruptedException, IOException 
    {
        SafeWalkServer s = new SafeWalkServer();
        int port = s.getLocalPort();
        Thread st = new Thread(s);
        st.start();
        
        Client c1 = new Client(HOST, port, "user1,CL50,PUSH");   //dummy client
        Thread ct1 = new Thread(c1);
        ct1.start();        
        Thread.sleep(100);
        
        Client c2 = new Client(HOST, port, "user2,LWSN,EE",true);  // actual requester
        Thread ct2 = new Thread(c2);
        ct2.start();        
        Thread.sleep(100); 
        
        Client c3 = new Client(HOST, port, "user3,LWSN,*");     //volunteer
        Thread ct3 = new Thread(c3);
        ct3.start();        
        Thread.sleep(100);
        

        ct2.join();
        ct3.join();  
        
        assertEquals("RESPONSE: user3,LWSN,*", c2.getResult()); //user gets volunteer
        assertEquals("RESPONSE: user2,LWSN,EE", c3.getResult()); //volunteer gets user
        
        
        Client c4 = new Client(HOST, port, ":PENDING_REQUESTS,#,*,*");
        Thread ct4 = new Thread(c4);
        ct4.start();
        Thread.sleep(100);
        
        assertEquals("RESPONSE: # of pending requests = 1", c4.getResult()); //checking if user and volunteer removed from server.

        Client c5 = new Client(HOST, port, ":SHUTDOWN"); //closing the server
        Thread ct5 = new Thread(c5);
        ct5.start();
        Thread.sleep(100);
        
        assertEquals("RESPONSE: success", c5.getResult());
        assertEquals("ERROR: connection reset", c1.getResult()); //dummy client is removed
      
        
        ct1.join();        
        ct5.join();
        st.join();
    }
}
