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
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import initiators.BackupTrigger;
import initiators.DeleteTrigger;
import initiators.ReclaimTrigger;
import initiators.RestoreTrigger;
import initiators.StateTrigger;
import network.Message;
import network.MessageRMI;
import network.MessageRecord;
import network.MulticastListener;
import protocols.ChunkBackupProtocol;
import resources.Logs;
import resources.Util;
import resources.Util.MessageType;

public class Peer implements MessageRMI {

	/*informations*/
	private int ID = 0;
	private char[] version;

	/*listeners*/
	public MulticastListener mc = null;
	public MulticastListener mdb = null;
	public MulticastListener mdr = null;

	/*MessageRecord*/
	public MessageRecord msgRecord = null;

	/*FileManeger*/
	public FileManager fileManager = null;

	/*Record*/
	public Record record = null;

	/*Schedule*/
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

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
		
		/*
		 * Enhancement of Delete Protocol Here
		 * Só depois de atualizar os seus chunks e os que ainda estão no sistema
		 * é que pode iniciar o enhancement do reclaim protocol
		 */
		
		
		//Enhancement of Reclaim Protocol
		if(version[2] != '0'){
			final Runnable checkChunks = new Runnable() {
				public void run() {
					System.out.println("Check Chunks Replication..."); 
					ArrayList<Chunk> chunks = record.getChunksWithRepBellowDes();
					
					for(Chunk c : chunks){
						
						msgRecord.removePutChunkMessages(c.getFileId(), c.getChunkNo());
						msgRecord.startRecordingPutchunks(c.getFileId(), c.getChunkNo());
						
						Util.randomDelay();
						
						if(msgRecord.receivedPutchunkMessage(c.getFileId(), c.getChunkNo())){
							
							byte[] data = fileManager.getChunkContent(c.getFileId(), c.getChunkNo());
							Message msg = new Message(MessageType.PUTCHUNK,version,ID,c.getFileId(),c.getChunkNo(),c.getReplicationDeg(),data);
							new ChunkBackupProtocol(mdb, msgRecord, msg).start();
							
							//Warns the peers that it also has the chunk
							msg = new Message(MessageType.STORED,version,ID,c.getFileId(),c.getChunkNo());
							mc.send(msg);
						}
					}
				}
			};
			scheduler.scheduleAtFixedRate(checkChunks, 60, 60, TimeUnit.SECONDS);
		}
		

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
			System.err.println("Server exception: AAA " + e.toString());
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
	public String backup(String filename, int repDeg, boolean enhancement) 
	{
		if(enhancement)
			if(version[2] == '0')
				return "Peer protocol not compatible.";

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
	public String restore(String filename, boolean enhancement)
	{
		if(enhancement)
			if(version[2] == '0')
				return "Peer protocol not compatible.";
		
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
	public String delete(String filename, boolean enhancement)
	{
		if(enhancement)
			if(version[2] == '0')
				return "Peer protocol not compatible.";
		
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
		fileManager.deleteFile(fileManager.checkPath(filename));

		return dt.response();
	}

	/*
	 * (non-Javadoc)
	 * @see network.MessageRMI#reclaim(int)
	 */
	@Override
	public String reclaim(int spaceToReclaim, boolean enhancement) 
	{
		if(enhancement)
			if(version[2] == '0')
				return "Peer protocol not compatible.";
		
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

	public Record getRecord(){
		return record;
	}

	public MessageRecord getMessageRecord(){
		return msgRecord;
	}
	
	public boolean enhancementVersion(){
		return (!(version[0] == 1 && version[2] == '0'));
	}
}
