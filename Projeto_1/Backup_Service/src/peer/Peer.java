package peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import initiators.BackupTrigger;
import initiators.DeleteTrigger;
import initiators.ReclaimTrigger;
import initiators.RestoreTrigger;
import initiators.StateTrigger;
import network.MessageRMI;
import network.MessageRecord;
import network.MulticastListener;
import resources.Logs;

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

	/*Schedule for metadata saving*/
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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

		//load metadata
		loadRecord();

		/*
		System.out.println("TotalMemory: " + record.totalMemory);
		System.out.println("RemaingMemory: " + record.remaingMemory);
		 */	

		//init variables 
		msgRecord = new MessageRecord();
		fileManager = new FileManager(this.ID,record.totalMemory,record.remaingMemory);

		//init rmi for client communication
		initRMI(remoteObjName);

		//init multicasts
		initMulticasts(mc_ap, mdb_ap, mdr_ap);

		//save metadata in 30s intervals
		final Runnable saveMetadata = new Runnable() {
			public void run() {
				System.out.println("Saving Metadata..."); 
				saveRecord();
			}
		};
		scheduler.scheduleAtFixedRate(saveMetadata, 30, 30, TimeUnit.SECONDS);

		//save metadata when shouts down
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					System.out.println("Shouting down ...");
					Thread.sleep(200);
					saveRecord();
				} catch (InterruptedException e) {
					System.err.println("Server exception: " + e.toString());
					e.printStackTrace();
				}
			}
		});
	}

	/*
	 * Init rmi for client communication
	 */
	private void initRMI(String remoteObjectName) 
	{
		try {
			MessageRMI stub = (MessageRMI) UnicastRemoteObject.exportObject(this, 0);
			LocateRegistry.getRegistry().rebind(remoteObjectName, stub);

			System.out.println("Server ready!");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Initiate the 3 multicasts
	 * @param mc_ap
	 * @param mdb_ap
	 * @param mdr_ap
	 */
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
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Record Object Serialization
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
			System.out.println("Serialized data saved in peersDisk/peer"+ID+"/record.ser");
		}
		catch (FileNotFoundException e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
		catch (IOException e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Record Object deserialization
	 */
	public synchronized void loadRecord() {

		record = new Record();

		File recordFile = new File("../peersDisk/peer"+ID+"/record.ser");

		//file can be loaded
		if(recordFile.exists())
		{
			try 
			{
				FileInputStream fileIn = new FileInputStream("../peersDisk/peer"+ID+"/record.ser");
				ObjectInputStream in  = new ObjectInputStream(fileIn);
				record = (Record) in.readObject();
				in.close();
				fileIn.close();

				System.out.println("Serialized data loaded from peersDisk/peer"+ID+"/record.ser");
			} 
			catch (FileNotFoundException e) {
				System.err.println("Server exception: " + e.toString());
				e.printStackTrace();
			}
			catch (IOException e) {
				System.err.println("Server exception: " + e.toString());
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				System.err.println("Server exception: " + e.toString());
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see network.MessageRMI#backup(java.lang.String, int)
	 */
	@Override
	public String backup(String filename, int repDeg) 
	{
		Logs.initProtocol("Backup");

		BackupTrigger bt = new BackupTrigger(this,filename,repDeg);
		bt.start();
		try 
		{
			bt.join();
		} 
		catch (InterruptedException e) 
		{
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
		return bt.response();
	}

	/*
	 * (non-Javadoc)
	 * @see network.MessageRMI#restore(java.lang.String)
	 */
	@Override
	public String restore(String filename)
	{
		Logs.initProtocol("Restore");

		RestoreTrigger rt = new RestoreTrigger(this,filename);	

		rt.start();
		try 
		{
			rt.join();
		} 
		catch (InterruptedException e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
		return rt.response();
	}

	/*
	 * (non-Javadoc)
	 * @see network.MessageRMI#delete(java.lang.String)
	 */
	@Override
	public String delete(String filename)
	{
		Logs.initProtocol("Delete");

		DeleteTrigger dt = new DeleteTrigger(this,filename);
		dt.start();
		try 
		{
			dt.join();
		}
		catch (InterruptedException e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		//delete own file 
		fileManager.deleteFile(filename);

		return dt.response();
	}

	/*
	 * (non-Javadoc)
	 * @see network.MessageRMI#reclaim(int)
	 */
	@Override
	public String reclaim(int spaceToReclaim) 
	{
		Logs.initProtocol("Reclaim");

		ReclaimTrigger rt = new ReclaimTrigger(this,spaceToReclaim);
		rt.start();
		try 
		{
			rt.join();
		} 
		catch (InterruptedException e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
		return rt.response();
	}

	/*
	 * (non-Javadoc)
	 * @see network.MessageRMI#state()
	 */
	@Override
	public String state()
	{
		Logs.initProtocol("State");

		StateTrigger st = new StateTrigger(this);
		st.start();
		try
		{
			st.join();
		}
		catch (InterruptedException e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
		return st.response();
	}


	/*
	 * Gets & Sets
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

	public Record getMulticastRecord(){
		return record;
	}

	public MessageRecord getMessageRecord(){
		return msgRecord;
	}
}
