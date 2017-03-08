package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastListener extends Thread
{
	public MulticastSocket socket = null;
	protected InetAddress address = null;
	protected int port = 0;
	protected boolean running = false;

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
			running = true;
			
			//abrir a conexao
			socket = new MulticastSocket(this.port);
			socket.setTimeToLive(1);
			socket.joinGroup(this.address);

			//receber a informacao
			String msg = "hello i'm a multicast";
			DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),address, port);
			socket.send(hi);

			while(running)
			{
				//waits for multicast message
				byte[] m_buf = new byte[256];
				DatagramPacket packet = new DatagramPacket(m_buf, m_buf.length);
				socket.receive(packet);

				System.out.println(new String(packet.getData()));
			}

			//fechar a conexao
			socket.leaveGroup(address);
			socket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

}
