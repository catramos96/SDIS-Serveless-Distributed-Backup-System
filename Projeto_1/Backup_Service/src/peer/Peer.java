package peer;


import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import network.MulticastListener;

public class Peer {
	
	public int ID = 0;
	public DatagramSocket socket = null; 	//socket for comunication with client
	public MulticastListener mc = null;
	public MulticastListener mdb = null;
	public MulticastListener mdr = null;
	public FileManager fileManager = null;
	
	public Peer(int id, String[] access_point, String[] mc_ap, String[] mdb_ap, String[] mdr_ap) throws SocketException
	{
		try 
		{
			//socket de conexao com o cliente
			InetAddress address = InetAddress.getByName(access_point[0]);
			int port = Integer.parseInt(access_point[1]);
			socket = new DatagramSocket(port, address);	
			
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
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		//trasmitir informacao para o cliente
		
		//inicializacao dos multicast channels
		/*
		mc.start();
		mdb.start();
		mdr.start();
		*/
		
		socket.close();
	}

}
