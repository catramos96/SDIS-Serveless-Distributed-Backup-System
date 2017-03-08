package peer;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import network.DatagramListener;
import network.MulticastListener;

public class Peer {
	
	public int ID = 0;
	public DatagramListener socket = null; 	//socket for communication with client
	public MulticastListener mc = null;
	public MulticastListener mdb = null;
	public MulticastListener mdr = null;
	public FileManager fileManager = null;
	
	public Peer(int id, String[] access_point, String[] mc_ap, String[] mdb_ap, String[] mdr_ap)
	{
		this.ID = id;
		try 
		{
			//socket de conexao com o cliente
			InetAddress address = InetAddress.getByName(access_point[0]);
			int port = Integer.parseInt(access_point[1]);
			socket = new DatagramListener(address, port+id);	
			
			//sockets multicast
			if(mc_ap[0] == "")
				address = InetAddress.getLocalHost();
			else	
				address = InetAddress.getByName(mc_ap[0]);
			
			port = Integer.parseInt(mc_ap[1]);
			mc = new MulticastListener(address,port);
			
			/*
			address = InetAddress.getByName(mdb_ap[0]);
			port = Integer.parseInt(mdb_ap[1]);
			mdb = new MulticastListener(address,port);
			
			address = InetAddress.getByName(mdr_ap[0]);
			port = Integer.parseInt(mdr_ap[1]);
			mdr = new MulticastListener(address,port);
			*/			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//inicializacao dos channels
		socket.start();
		mc.start();
		/*
		mdb.start();
		mdr.start();
		*/
		
	}

}
