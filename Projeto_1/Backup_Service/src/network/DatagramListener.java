package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DatagramListener extends Thread
{
	public DatagramSocket socket = null;
	protected InetAddress address = null;
	protected int port = 0;
	private boolean running = false;
	
	public DatagramListener(InetAddress address, int port)
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
			socket = new DatagramSocket(this.port);
			setRunning(true);
			DatagramPacket r_packet;
			DatagramPacket s_packet;
			
			//receber a informacao
			while(isRunning())
			{
				byte[] rbuf = new byte[256];
				r_packet = new DatagramPacket(rbuf, rbuf.length);
				socket.receive(r_packet);
				
				System.out.println(new String(r_packet.getData()));
				//envia para o initiator peer o que quer
				
				//envia uma confirmacao da rececao
				byte[] sbuf = ("bye").getBytes();
				s_packet = new DatagramPacket(sbuf, sbuf.length,r_packet.getAddress(),r_packet.getPort());
				socket.send(s_packet);
			}
			
			//fechar a conexao			
			socket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	protected boolean isRunning() {
		return running;
	}

	protected void setRunning(boolean running) {
		this.running = running;
	}

}
