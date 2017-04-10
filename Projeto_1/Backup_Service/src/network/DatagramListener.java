package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import peer.Peer;
import resources.Util;

/**
 * Class DatagramListener used for peers private communication channels.
 * @attribute DatagramSocket socket - Represents the socket used for receiving and sending messages.
 * @attribute InetAddress address - Represents the peer local address
 * @attribute boolean running - True if it stills active, False otherwise.
 * @attribute Peer peer - Peer that uses the channel.
 */
public class DatagramListener extends Thread{
	
	private DatagramSocket socket = null;
	private InetAddress address = null;
	private boolean running = false;
	private Peer peer = null;
	
	/**
	 * Constructor of the DatagramListener
	 * @param address
	 * @param peer
	 */
	public DatagramListener(InetAddress address,Peer peer){
		this.address = address;
		this.peer = peer;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Function that sends a message to the peer in destination address2:port2
	 * @param message
	 * @param address2
	 * @param port2
	 */
	public void send(Message message, InetAddress address2, int port2)
	{
		byte[] msg = message.buildMessage();
		
		DatagramPacket packet = new DatagramPacket(msg, msg.length, address2, port2);
		try {
			socket.send(packet);
		} 
		catch (IOException e) 	{
			e.printStackTrace();
		}
	}
	
	/**
	 * Function that receives a message
	 * @return Message content
	 */
	public byte[] receive()
	{
		byte[] m_buf = new byte[Util.PACKET_MAX_SIZE];
		DatagramPacket packet = new DatagramPacket(m_buf, m_buf.length);
		try {
			socket.receive(packet);
		} 
		catch (IOException e) 	{
			e.printStackTrace();
		}
		
		return packet.getData();
	}
	
	/**
	 * 
	 */
	public void run(){
		
		running = true;
	
		while(running)
		{
			byte[] messageReceived = receive();
			//new thread that handles message received
			new MessageHandler(peer,messageReceived).start();
		}
	
		//close connection
		socket.close();
	}
	
	/**
	 * Function that exists the thread
	 */
	public void destroy(){
		running = false;
	}
	
	/**
	 * Function that gets the port associated with the socket
	 * @return Port
	 */
	public int getPort(){
		return socket.getLocalPort();
	}
}
