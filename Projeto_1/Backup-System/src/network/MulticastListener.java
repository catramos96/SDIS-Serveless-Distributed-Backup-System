package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastListener extends Thread
{
	public MulticastSocket socket = null;
	protected InetAddress address = null;
	protected int port = 0;
	
	public MulticastListener(InetAddress address, int port)
	{
		this.address = address;
		this.port = port;
	}
	
	@Override
	public void run() 
	{	
		try 
		{
			//abrir a conexao
			socket = new MulticastSocket(this.port);
			socket.setTimeToLive(1);
			socket.joinGroup(this.address);
			
			//receber a informacao
			
			//fechar a conexao
			socket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

}
