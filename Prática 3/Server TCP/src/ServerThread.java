import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class ServerThread extends Thread
{
	protected ServerSocket socket = null;		//New
	protected Socket echosocket = null;			//New
	protected int port = 0;
	protected boolean finished = false;
	protected Map<String,String> database = null;
	
	//protected String multicastAddress = "224.0.0.3"; //example

	public ServerThread(int port) throws IOException
	{
		this.port = port;
		database = new HashMap<String,String>();
		
		socket = new ServerSocket(port);
		//socket.setSoTimeout(1000);
		
		System.out.println("SERVER OPENED");
	
	}

	public void run() 
	{	
		while(!finished)
		{	
			try
			{
				System.out.println("Waiting for connection request ...");
				//Establish conection
				echosocket = socket.accept();		//Waits until conection request
				System.out.println("Connection Established!");
			
				
				BufferedReader in = null;
				in = new BufferedReader(new InputStreamReader(echosocket.getInputStream()));
				String request = in.readLine();
				System.out.println("Server Received : " + request);
				
				//process requests
				String reply = process(request);
				
				//send reply
				PrintWriter out = null;
				out = new PrintWriter(echosocket.getOutputStream(),true);
				out.println(reply);
				
				//close connection
				echosocket.close();
				System.out.println("Connection closed");
	            			
			} catch (Exception e) {
				finished = true;
				e.printStackTrace();
			}
		}
		
		//socket.close();
			
	}
	
	private String process(String message) 
	{
		//split
		String[] parts = message.split(" ");
		
		//Invoke
		if(parts[0].equals("lookup"))
		{
			String result = lookup(parts[1]);
			
			if(result.equals("NOT_FOUND"))
				return result;
			
			return parts[1]+" "+result;
		}
		else if(parts[0].equals("register"))
		{
			int result;
			
			if((result = register(parts[1],parts[2])) == -1)
					return "ALREADY EXISTS";
			
			return result+" ";
		}
		
		return "ERROR";
	}
	
	private int register(String plate, String owner) 
	{
		if(database.containsKey(plate))
			return -1;
		
		database.put(plate, owner);	//insert
		
		return database.size();
	}
	
	private String lookup(String plate)
	{
		if(!database.containsKey(plate))
			return "NOT_FOUND";
		
		return database.get(plate);
	}
}
