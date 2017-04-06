package peer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import initiators.BackupTrigger;
import initiators.DeleteTrigger;
import initiators.ReclaimTrigger;
import initiators.RestoreTrigger;
import initiators.StateTrigger;
import network.MessageRMI;
import network.MessageRecord;
import network.MulticastListener;

public class Peer implements MessageRMI {

	/*informations*/
	private int ID = 0;
	private char[] version;

	/*listeners*/
	//public DatagramListener socket = null; 	//socket for communication with client
	public MulticastListener mc = null;
	public MulticastListener mdb = null;
	public MulticastListener mdr = null;
	/*MessageRecord*/
	public MessageRecord msgRecord = null;

	/*FileManeger*/
	public FileManager fileManager = null;

	/*Record*/
	public Record record = null;

	/**
	 * Create peer
	 * @param protocolVs
	 * @param id
	 * @param access_point
	 * @param mc_ap
	 * @param mdb_ap
	 * @param mdr_ap
	 */
	public Peer(char[] protocolVs, int id, String remoteObjName, String[] mc_ap, String[] mdb_ap, String[] mdr_ap)
	{
		this.ID = id;
		this.version = protocolVs;
		
		loadRecord();
		msgRecord = new MessageRecord();
		fileManager = new FileManager(this.ID,record.totalMemory,record.remaingMemory);
		
		/*
		System.out.println("TotalMemory: " + record.totalMemory);
		System.out.println("RemaingMemory: " + record.remaingMemory);
		*/		
		initRMI(remoteObjName);
		initMulticasts(mc_ap, mdb_ap, mdr_ap);
		
		//shutdown  --> colocar uma funcao que guarda de x em x tempo?
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
	}
	
	private void initMulticasts(String[] mc_ap, String[] mdb_ap, String[] mdr_ap) {
		try 
		{
			InetAddress address;
			int port;
			
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
			mc.start();
			mdb.start();
			mdr.start();
		} 
		catch (IOException e)
		{
			System.err.println("Peer error: "+ e.toString());
			e.printStackTrace();
		}
	}

	private void initRMI(String remoteObjectName) 
	{
		try {
			MessageRMI stub = (MessageRMI) UnicastRemoteObject.exportObject(this, 0);
			LocateRegistry.getRegistry().rebind(remoteObjectName, stub);
			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	/*
	 * Record Serialization 
	 */

	public synchronized void saveRecord() {
		
		record.totalMemory = fileManager.getTotalSpace();
		record.remaingMemory = fileManager.getRemainingSpace();
		
		try 
		{
			FileOutputStream fileOut = new FileOutputStream("../peersDisk/peer"+ID+"/record.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(record);
			out.close();
			fileOut.close();
			System.out.printf("Serialized data is saved in peersDisk/peer"+ID+"/record.ser");
		}
		catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("Restore object created");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void loadRecord() {
		
		record = new Record();
		
		try 
		{
			FileInputStream fileIn = new FileInputStream("../peersDisk/peer"+ID+"/record.ser");
			ObjectInputStream in  = new ObjectInputStream(fileIn);
			record = (Record) in.readObject();
			in.close();
			fileIn.close();
			System.out.printf("Serialized data loaded from peersDisk/peer"+ID+"/record.ser");
		} 
		catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("FileNotFound");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
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

	public Record getMulticastRecord(){
		return record;
	}
	
	public MessageRecord getMessageRecord(){
		return msgRecord;
	}

	@Override
	public String backup(String filename, int repDeg) 
	{
		System.out.println("Backup Protocol initiated...");
		
		BackupTrigger bt = new BackupTrigger(this,filename,repDeg);
		bt.start();
		try {
			bt.join();
		} 
		catch (InterruptedException e) 
		{
			System.out.println("cant join");
			e.printStackTrace();
		}
		return bt.response();
	}

	@Override
	public String restore(String filename)
	{
		System.out.println("Restore Protocol initiated...");
		
		RestoreTrigger rt = new RestoreTrigger(this,filename);	
		rt.start();
		try 
		{
			rt.join();
		} 
		catch (InterruptedException e) {
			System.out.println("cant join");
			e.printStackTrace();
		}
		return rt.response();
	}

	@Override
	public String delete(String filename)
	{
		System.out.println("Delete Protocol initiated...");
		
		DeleteTrigger dt = new DeleteTrigger(this,filename);
		dt.start();
		try 
		{
			dt.join();
		}
		catch (InterruptedException e) {
			System.out.println("cant join");
			e.printStackTrace();
		}
		return dt.response();
	}

	@Override
	public String reclaim(int spaceToReclaim) 
	{
		System.out.println("Reclaim Protocol initiated...");
		
		ReclaimTrigger rt = new ReclaimTrigger(this,spaceToReclaim);
		rt.start();
		try 
		{
			rt.join();
		} 
		catch (InterruptedException e) {
			System.out.println("cant join");
			e.printStackTrace();
		}
		return rt.response();
	}

	@Override
	public String state()
	{
		System.out.println("State Protocol initiated...");
		
		StateTrigger st = new StateTrigger(this);
		st.start();
		try
		{
			st.join();
		}
		catch (InterruptedException e) {
			System.out.println("cant join");
			e.printStackTrace();
		}
		return st.response();
	}
}
