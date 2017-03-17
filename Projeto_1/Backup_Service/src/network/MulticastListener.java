package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import peer.Peer;

public class MulticastListener extends Thread
{
	public static int PACKET_MAX_SIZE = 65000;
	public MulticastSocket socket = null;
	public Peer peer;
	protected InetAddress address = null;
	protected int port = 0;
	protected boolean running = false;

	public MulticastListener(InetAddress address, int port, Peer peer)
	{
		this.address = address;
		this.port = port;
		this.peer = peer;
	}
	
	/*
	 * sent
	 */
	public void send(Message message)
	{
		byte[] msg = message.buildMessage();
		
		DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
		try {
			socket.send(packet);
			System.out.println("1 - Message sent: " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Receive
	 */
	public byte[] receive(){
		//waits for multicast message
		byte[] m_buf = new byte[PACKET_MAX_SIZE];
		DatagramPacket packet = new DatagramPacket(m_buf, m_buf.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("2 - Message received: " + packet.getData());
		
		return packet.getData();
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

			while(running)
			{
				byte[] messageReceived = receive();
				peer.notify(messageReceived);
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
