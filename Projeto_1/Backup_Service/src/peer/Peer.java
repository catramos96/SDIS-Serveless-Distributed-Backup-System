package peer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;

import network.DatagramListener;
import network.MulticastListener;
import network.MulticastRecord;
import resources.Util;

public class Peer {

	//TEMPORARIO
	private ArrayList<String> mdrRestores;

	/*informations*/
	private int ID = 0;
	private char[] version;

	/*listeners*/
	public DatagramListener socket = null; 	//socket for communication with client

	public MulticastListener mc = null;
	public MulticastListener mdb = null;
	public MulticastListener mdr = null;

	/*FileManeger*/
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
		loadRecord();

		//shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					System.out.println("Shouting down ...");
					Thread.sleep(200);
					saveRecord();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		mdrRestores = new ArrayList<String>();

		this.ID = id;
		this.version = protocolVs;

		try 
		{
			//socket de conexao com o cliente
			InetAddress address = InetAddress.getByName(access_point[0]);
			int port = Integer.parseInt(access_point[1]);
			socket = new DatagramListener(address, port,this);	

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
	
	/*
	 * MulticastRecord Serialization 
	 */

	public void saveRecord() {
		try 
		{
			//record
			
			FileOutputStream fileOut = new FileOutputStream("../peersDisk/peer"+ID+"/record.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(record);
			out.close();
			fileOut.close();
			System.out.printf("Serialized data is saved in peersDisk/peer"+ID+"/record.ser");
			
			//filemanager
			
			fileOut = new FileOutputStream("../peersDisk/peer"+ID+"/filemanager.ser");
			out = new ObjectOutputStream(fileOut);
			out.writeObject(fileManager);
			out.close();
			fileOut.close();
			System.out.printf("Serialized data is saved in peersDisk/peer"+ID+"/filemanager.ser");
		}
		catch(IOException i) 
		{
			i.printStackTrace();
			System.out.println("ok");
		}
	}

	public void loadRecord() {
		record = new MulticastRecord();
		fileManager = new FileManager(ID,Util.DISK_SPACE_DEFAULT);
		try 
		{
			//record
			
			FileInputStream fileIn = new FileInputStream("../peersDisk/peer"+ID+"/record.ser");
			ObjectInputStream in  = new ObjectInputStream(fileIn);
			record = (MulticastRecord) in.readObject();
			in.close();
			fileIn.close();
			System.out.printf("Serialized data loaded from peersDisk/peer"+ID+"/record.ser");
			
			//filemanager
			
			fileIn = new FileInputStream("../peersDisk/peer"+ID+"/filemanager.ser");
			in  = new ObjectInputStream(fileIn);
			fileManager = (FileManager) in.readObject();
			in.close();
			fileIn.close();
			System.out.printf("Serialized data loaded from peersDisk/peer"+ID+"/filemanager.ser");
			
		} 
		catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("Restore object created");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	/*
	 * Peer getters and setters
	 */

	public synchronized boolean chunkRestored(String fileId, int chunkNo) {
		String chunkName = chunkNo+fileId;
		return mdrRestores.contains(chunkName);
	}

	public synchronized void addRestoredChunk(int chunkNo, String fileId){
		mdrRestores.add(chunkNo+fileId);
	}

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
