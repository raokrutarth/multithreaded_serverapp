/**
 * Project 5
 *  
 * @author Krutarth Rao, raok, LM3
 * @author Rahul Sathi, rsathi, L05
 */
import java.io.*;  
import java.net.*;
import java.util.*;

public class SafeWalkServer extends ServerSocket implements Runnable
{
    private ArrayList<Request> requests = new ArrayList<Request>();
    public SafeWalkServer(int port) throws IOException
    {
        super(port);
        setReuseAddress(true);
    }
    public SafeWalkServer() throws IOException 
    {
        this(0);
        setReuseAddress(true);
        int a = getLocalPort();
        System.out.println("Unspecified port. Using self allocated port : " + a );
    }
    public void run() 
    {
        //System.err.println("Starting Run Method");
        while (!isClosed()) 
        {
            try 
            {
                Socket clientSocket = this.accept();
                new Request(clientSocket).run();
                
            } catch (IOException e) {
                System.err.println("IO Exception");
                e.printStackTrace();
            }
        }
    }
    
    
    public static boolean isPortValid(String port)
    {
        if (port != null && isNum(port))
        {
            int portNum = Integer.parseInt(port);
            if (portNum >= 1025 && portNum <= 65535)
            {
                return true;
            }
            else
            {
                System.out.println("Please enter a valid port.");
            }
        }
        else
        {
            System.out.println("Please enter a valid port.");
        }
        return false;
        
    }
    public static boolean isNum(String numStr)
    {
        try 
        {
            Integer.parseInt(numStr);
            return true;
        } 
        catch (NumberFormatException e) 
        {
            return false;
        } 
    }
    
    public static void main(String[] args)
    {
        SafeWalkServer sf = null;
        if (args.length == 0)
        {
            
            try 
            {
                sf = new SafeWalkServer();
                sf.run();
            } 
            catch (IOException e) 
            {  
                e.printStackTrace(); 
            }
        }
        else
        {
            try 
            {
                if (isPortValid(args[0]))
                {
                    sf = new SafeWalkServer(Integer.parseInt(args[0]) );
                    sf.run();
                }                
            } 
            catch (IOException e) 
            { 
                System.out.println("error@ creating server from port "); 
            }
        }
        
    } 
    
    public class Request implements Runnable
    {
        
        PrintWriter pw;
        BufferedReader br;
        Socket clientSocket;
        String[] cmd;
        
        Request (Socket clientSocket)
        {
            try
            { 
                this.clientSocket = clientSocket;
                pw = new PrintWriter( clientSocket.getOutputStream() );
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            }
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
        private boolean isValidStart(String start)
        {
            return start.equals("CL50") || start.equals("EE") || start.equals("LWSN") || 
                start.equals("PMU") || start.equals("PUSH") ;
        }
        private boolean isValidEnd(String end)
        {
            return end.equals("CL50") || end.equals("EE") || end.equals("LWSN") || 
                end.equals("PMU") || end.equals("PUSH") || end.equals("*") ;
        }  
        private boolean sameRequest(Request req) throws IndexOutOfBoundsException
        {
            if( req.cmd[0].equals(this.cmd[0]) && req.cmd[1].equals(this.cmd[1]) && req.cmd[2].equals(this.cmd[2]) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        
        public void run() 
        {
            try 
            {
                String inpt = br.readLine();
                cmd = inpt.split(",");
                if (cmd[0].charAt(0) == ':') 
                { 
                    if (cmd[0].equals(":RESET")) 
                    {
                        for (int k = 0; k < requests.size(); k++)
                        {
                            requests.get(k).reply("ERROR: connection reset");
                        }
                        this.reply("RESPONSE: success");
                        
                    } 
                    
                    else if (cmd[0].equals(":SHUTDOWN")) 
                    {
                        for (int k = 0; k < requests.size(); k++) 
                        {
                            requests.get(k).reply("ERROR: connection reset");
                            
                        }
                        reply("RESPONSE: success");
                        close();  
                    }
                    else if (cmd[0].equals(":PENDING_REQUESTS") && cmd.length == 4 
                                 && (cmd[1].equals("#") || cmd[1].equals("*") ) && isValidEnd(cmd[2]) && isValidEnd(cmd[3]) )
                    {
                        if (cmd[1].equals("#") )
                        {
                            if (cmd[2].equals("*") && cmd[3].equals("*") )
                            {
                                this.reply( "RESPONSE: # of pending requests = " + requests.size());
                            }
                            else if (isValidStart(cmd[2]) && cmd[3].equals("*") )
                            {
                                int counter = 0;
                                for (Request req : requests)
                                {
                                    
                                    if (req.cmd[1].equals(this.cmd[2]))
                                    {
                                        counter++;
                                    }
                                }
                                this.reply("RESPONSE: # of pending requests from " + this.cmd[2] + " = " + counter);                                
                            }
                            else if( cmd[2].equals("*") && isValidEnd(cmd[3]))
                            {
                                int counter = 0;
                                for (Request req : requests)
                                {                                    
                                    if (req.cmd[2].equals(this.cmd[3]))
                                    {
                                        counter++;
                                    }
                                }
                                this.reply("RESPONSE: # of pending requests to " + this.cmd[3] + " = " + counter);        
                            }
                        }
                        else if ( cmd[1].equals("*") && cmd[2].equals("*") && cmd[3].equals("*") && requests.size() > 0)
                        {
                            String result = "[";
                            for (int i = 0; i < requests.size() - 1; i++)
                            {
                                result = result + Arrays.toString(requests.get(i).cmd) + ", ";
                            }
                            result = result + Arrays.toString(requests.get(requests.size() - 1).cmd);
                            
                            result = result + "]";
                            this.reply(result);
                        }
                        else if ( cmd[1].equals("*") && cmd[2].equals("*") && cmd[3].equals("*") && requests.size() == 0 )
                        {
                            this.reply("[]");
                        }
                    }
                    else 
                    {
                        pw.println("ERROR: invalid command");
                        pw.flush();
                        br.close();
                        clientSocket.close();
                    }
                }
                try
                {
                    if( isValidStart(cmd[1]) && isValidEnd(cmd[2])  && !cmd[2].equals(cmd[1]) && cmd.length == 3  )
                    {
                        boolean done = false;
                        Request removReq =  null;
                        for (Request req : requests)
                        {
                            if (this.sameRequest(req))
                            {
                                this.reply("ERROR: invalid request");
                                done = true;
                                break;
                            }
                            else if ( !done && req.cmd[1].equals(this.cmd[1]) && ( req.cmd[2].equals(this.cmd[2]) || req.cmd[2].equals("*") || this.cmd[2].equals("*") )
                                         && (!(req.cmd[2].equals("*") && this.cmd[2].equals("*")) ) ) 
                            {
                                this.reply("RESPONSE: " + req.cmd[0] + "," + this.cmd[1] + "," + req.cmd[2] );
                                req.reply("RESPONSE: " + this.cmd[0] + ","  + this.cmd[1] + "," +  this.cmd[2]);
                                //System.out.println ( req.cmd[0] + " paired with " + this.cmd[0]);
                                removReq = req;
                                done = true;
                            }
                        }
                        Iterator<Request> requestRemover = requests.iterator();
                        while (requestRemover.hasNext() && removReq != null) 
                        {
                            if (requestRemover.next().equals(removReq)) 
                            {
                                requestRemover.remove();
                                break;
                            }
                        }
                        if (!done)  
                        {
                            requests.add(this);
                        }
                    }
                    else
                    {
                        this.reply("ERROR: invalid request");
                    }
                }
                catch (IndexOutOfBoundsException e) 
                { 
                    //System.out.println("no request executed by " + this.cmd[0]);
                    this.reply("ERROR: invalid request");
                }
            } 
            
            catch (IOException e) 
            {
                e.printStackTrace();
            }
            
        }
        
        void reply(String message) throws IOException 
        {
            pw.println(message);
            pw.close();
            br.close();
            clientSocket.close();
        }
    }    
}



