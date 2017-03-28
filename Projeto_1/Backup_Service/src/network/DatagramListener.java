package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import initiators.BackupTrigger;
import initiators.DeleteTrigger;
import initiators.ReclaimTrigger;
import initiators.RestoreTrigger;
import initiators.StateTrigger;
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
		String arg1 = parts[1];			//filename or space to reclaim
		
		System.out.println(parts[0]);
		
		if(parts[0].equals("BACKUP"))
			new BackupTrigger(peer,arg1, Integer.parseInt(parts[2])).start();	//file name + replicationDegree
		else if(parts[0].equals("RESTORE"))
			new RestoreTrigger(peer,arg1).start();	
		else if(parts[0].equals("DELETE"))
			new DeleteTrigger(peer,arg1).start();	
		else if(parts[0].equals("RECLAIM"))
			new ReclaimTrigger(peer,Integer.parseInt(arg1)).start();
		else if(parts[0].equals("STATE"))
			new StateTrigger().start();
			
	}

	protected boolean isRunning() {
		return running;
	}

	protected void setRunning(boolean running) {
		this.running = running;
	}

}
