package peer;

import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

import network.DatagramListener;
import network.Message;
import resources.Util.MessageType;
import network.MulticastListener;
import network.MulticastRecord;
import protocols.ChunkBackupProtocol;
import protocols.ChunkRestoreProtocol;
import resources.Util;

public class Peer {

	//TEMPORARIO
	private ArrayList<String> mdrRestores;

	private int ID = 0;
	private char[] version;

	/*listeners*/
	public DatagramListener socket = null; 	//socket for communication with client

	public MulticastListener mc = null;
	public MulticastListener mdb = null;
	public MulticastListener mdr = null;

	/*objects*/
	public FileManager fileManager = null;

	/*MulticastRecord*/
	public MulticastRecord record = null;

	/**
	 * Create peer
	 * @param protocolVs
	 * @param id
	 * @param access_point
	 * @param mc_ap
	 * @param mdb_ap
	 * @param mdr_ap
	 */
	public Peer(char[] protocolVs, int id, String[] access_point, String[] mc_ap, String[] mdb_ap, String[] mdr_ap)
	{
		mdrRestores = new ArrayList<String>();

		this.ID = id;
		this.version = protocolVs;

		fileManager = new FileManager(ID,Util.DISK_SPACE_DEFAULT);
		record = new MulticastRecord();

		try 
		{
			//socket de conexao com o cliente
			InetAddress address = InetAddress.getByName(access_point[0]);
			int port = Integer.parseInt(access_point[1]);
			socket = new DatagramListener(address, port+id,this);	

			//sockets multicast
			if(mc_ap[0] == "")
				address = InetAddress.getLocalHost();
			else	
				address = InetAddress.getByName(mc_ap[0]);

			port = Integer.parseInt(mc_ap[1]);
			mc = new MulticastListener(address,port,this);

			address = InetAddress.getByName(mdb_ap[0]);
			port = Integer.parseInt(mdb_ap[1]);
			mdb = new MulticastListener(address,port,this);

			address = InetAddress.getByName(mdr_ap[0]);
			port = Integer.parseInt(mdr_ap[1]);
			mdr = new MulticastListener(address,port,this);		

			//inicializacao dos channels
			socket.start();

			mc.start();
			mdb.start();
			mdr.start();

			//Thread.sleep(Util.WAITING_TIME);		//delay para inicializar as variaveis do multicast
		} catch (IOException e)
		{
			System.out.println("Peer error");
			e.printStackTrace();
		}
	}

	/**
	 * Peer waiting
	 */
	public void randomDelay(){
		Random delay = new Random();
		try {
			Thread.sleep(delay.nextInt(Util.RND_DELAY));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean chunkRestored(String fileId, int chunkNo) {
		String chunkName = chunkNo+fileId;

		if(mdrRestores.contains(chunkName))
			System.out.println("sim!");
		else
			System.out.println("nao...");

		return mdrRestores.contains(chunkName);
	}
	
	public void addRestoredChunk(int chunkNo, String fileId){
		mdrRestores.add(chunkNo+fileId);
	}

	/*
	 * Peer getters and setters
	 */

	public char[] getVersion() {
		return version;
	}

	public void setVersion(char[] version) {
		this.version = version;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public MulticastListener getMc(){
		return mc;
	}

	public MulticastListener getMdb(){
		return mdb;
	}
	
	public MulticastListener getMdr(){
		return mdr;
	}

	public MulticastRecord getMulticastRecord(){
		return record;
	}
}
