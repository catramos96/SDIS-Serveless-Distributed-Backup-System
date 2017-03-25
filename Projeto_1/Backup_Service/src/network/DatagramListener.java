package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import peer.Peer;

public class DatagramListener extends Thread
{
	public DatagramSocket socket = null;
	protected InetAddress address = null;
	protected int port = 0;
	protected boolean running = false;
	protected Peer peer = null; 
	
	public DatagramListener(InetAddress address, int port,Peer peer)
	{
		this.peer = peer;
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
				handle(r_packet.getData());
				
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

	private void handle(byte[] data) 
	{
		String message = new String(data);
		String[] parts = message.split("\\s");
		
		if(parts[0].equals("BACKUP"))
			peer.BackupTrigger(parts[1], Integer.parseInt(parts[2]));	//file name + replicationDegree
		else if(parts[0].equals("RESTORE"))
			peer.RestoreTrigger(parts[1]);	//fileName
		else if(parts[0].equals("DELETE"))
			peer.DeleteTrigger();
		else if(parts[0].equals("RECLAIM"))
			peer.ReclaimTrigger();
		else if(parts[0].equals("STATE"))
			peer.StateTrigger();
			
	}

	protected boolean isRunning() {
		return running;
	}

	protected void setRunning(boolean running) {
		this.running = running;
	}

}
