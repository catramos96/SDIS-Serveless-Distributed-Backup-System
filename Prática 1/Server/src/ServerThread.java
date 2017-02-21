import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class ServerThread extends Thread
{
	protected DatagramSocket socket = null;
	protected int port = 0;
	protected boolean finished = false;
	protected Map<String,String> database = null;
	
	//protected String multicastAddress = "224.0.0.3"; //example

	public ServerThread(int port) throws IOException
	{
		this.port = port;
		database = new HashMap<String,String>();
		
		socket = new DatagramSocket(port);
		//socket.setSoTimeout(1000);
		
		System.out.println("SERVER OPENED");
	}

	public void run() 
	{	
		while(!finished)
		{	
			try
			{
				
				//receive requests
				byte[] rbuf = new byte[256];
				DatagramPacket packet = new DatagramPacket(rbuf,rbuf.length);
				socket.receive(packet);
				
				//print 
				String received = new String(packet.getData());
				System.out.println("Server Received : " + received);
				
				//process requests
				String reply = process(received);
				byte[] sbuf = reply.getBytes();
				
				//send reply
				InetAddress address = packet.getAddress();
				int pport = packet.getPort();
	            packet = new DatagramPacket(sbuf, sbuf.length, address, pport);
	            socket.send(packet);
	            
	            			
			} catch (Exception e) {
				finished = true;
				e.printStackTrace();
			}
		}
		
		socket.close();
		System.out.println("\nSERVER CLOSED");	
			
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