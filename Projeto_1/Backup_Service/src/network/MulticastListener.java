package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import resources.Util;

import peer.Peer;

public class MulticastListener extends Thread
{
	public MulticastSocket socket = null;
	protected InetAddress address = null;
	protected int port = 0;
	protected boolean running = false;
	protected Peer peer = null;

	public MulticastListener(InetAddress address, int port, Peer peer)
	{
		this.address = address;
		this.port = port;
		this.peer = peer;
	}
	
	/**
	 * Send message 
	 * @param message
	 */
	public void send(Message message)
	{
		byte[] msg = message.buildMessage();
		
		DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
		try 
		{
			socket.send(packet);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * receive message
	 * @return
	 */
	public byte[] receive()
	{
		//waits for multicast message
		byte[] m_buf = new byte[Util.PACKET_MAX_SIZE];
		DatagramPacket packet = new DatagramPacket(m_buf, m_buf.length);
		try 
		{
			socket.receive(packet);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return packet.getData();
	}

	@Override
	public void run() 
	{	
		try 
		{
			running = true;
			
			//open connection
			socket = new MulticastSocket(this.port);
			socket.setTimeToLive(1);
			socket.joinGroup(this.address);

			while(running)
			{
				byte[] messageReceived = receive();
				//new thread that handles message received
				new MessageHandler(peer,messageReceived).start();
			}

			//close connection
			socket.leaveGroup(address);
			socket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

}
