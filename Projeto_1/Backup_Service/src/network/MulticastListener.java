package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import peer.Peer;

public class MulticastListener extends Thread
{
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
		String msg = message.buildMessage();
		
		DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(),address, port);
		try {
			socket.send(packet);
			System.out.println("Message sent: " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Receive
	 */
	public String receive(){
		//waits for multicast message
		byte[] m_buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(m_buf, m_buf.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Removes the last sequences of white spaces (\s) and null characters (\0)
		String msg_received = (new String(packet.getData()).replaceAll("[\0 \\s]*$", ""));
		System.out.println("Message received: " + msg_received);
		
		return msg_received;
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
				String messageReceived = receive();
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
