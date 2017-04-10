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
import java.util.HashMap;
import java.util.Map.Entry;
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
import protocols.DeleteEnhancementProtocol;
import resources.Logs;
import resources.Util;
import resources.Util.MessageType;

/**
 * Class responsible for the peer. 
 * @attribute int ID - peerId
 * @attribute char[] version - protocol version used
 * @attribute boolean enhancement - result from the observation of the protocol version used
 * @attribute MulticastListener mc - multicast general channel
 * @attribute MulticastListener mdb - multicast data backup channel
 * @attribute MulticastListener mdr -  multicast data restore channel
 * @attribute MessageRecord msgRecord - record specific messages captured by the multicast channels 
 * @attribute FileManager fileManager - responsible for all the interactions with the disk
 * @attribute Record record - responsible for mapping chunks (metadata)
 * @attribute ScheduledExecutorService scheduler - execute scheduled runnables 
 */
public class Peer implements MessageRMI {

	/*informations*/
	private int ID = 0;
	private char[] version;
	private boolean enhancement; 

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
	 * Constructor. 
	 * Check if this peer will execute the enhancements;
	 * Loads metadata, if any; 
	 * Initializes variables like : MessageRecord, FileManager, multicasts;
	 * Initializes RMI;
	 * If enhancement, verifies if there is any chunk stored that was deleted when server was down;
	 * Verifies if exists chunks stored with actual replication degree lower than desired;
	 * Schedule metadata saving and verification of replication degrees.
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
		verifyEnhancement();

		//load metadata
		loadRecord();

		//init variables 
		msgRecord = new MessageRecord();
		fileManager = new FileManager(this.ID,record.totalMemory,record.remaingMemory);

		//init rmi for client communication
		initRMI(remoteObjName);

		//init multicasts
		initMulticasts(mc_ap, mdb_ap, mdr_ap);

		//Enhancement of Delete Protocol
		if(enhancement)
			verifyDeletions();

		//Enhancement of Reclaim Protocol
		verifyChunks();

		//save metadata in 30s intervals
		saveMetadata();

		//save metadata when shouts down
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					Thread.sleep(200);
					saveRecord();
				} catch (InterruptedException e) {
					Logs.exception("addShutdownHook", "Peer", e.toString());
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * If the version is '1''.''0', this peer dont execute enhancements.
	 * Otherwise, execute enhancements.
	 */
	private void verifyEnhancement() {
		if((version[0] == '1') && (version[1] == '.') && (version[2] == '0'))
			enhancement = false;
		else
			enhancement = true;
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

				Logs.serializeWarn("loaded from",ID);
			} 
			catch (FileNotFoundException e) {
				Logs.exception("loadRecord", "Peer", e.toString());
				e.printStackTrace();
			}
			catch (IOException e) {
				Logs.exception("loadRecord", "Peer", e.toString());
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				Logs.exception("loadRecord", "Peer", e.toString());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Init RMI for client communication
	 * @param remoteObjectName
	 */
	private void initRMI(String remoteObjectName) 
	{
		try {
			MessageRMI stub = (MessageRMI) UnicastRemoteObject.exportObject(this, 0);
			LocateRegistry.getRegistry().rebind(remoteObjectName, stub);
			Logs.rmiReady();
		} catch (Exception e) {
			Logs.exception("initRMI", "Peer", e.toString());
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

			//channels execution
			mc.start();
			mdb.start();
			mdr.start();
		} 
		catch (IOException e)
		{
			Logs.exception("initMulticasts", "Peer", e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * When the peer starts, and the protocol permits enhancements, it must verify if it missed some 'DELETE'
	 * message. If that happen, this peer will have chunks that will never be reclaimed. 
	 * To avoid that situation, the peer sent a 'GETINITIATOR' message for each file stored. If the peer,
	 * after a waiting time, received a 'INITIATOR' message for each file, it means that there was no DELETE 
	 * message lost. Otherwise, if some file don't receive the 'INITIATOR' message, it must delete all chunks 
	 * stored from that file.
	 */
	private void verifyDeletions() 
	{
		Logs.checkChunks("deletition"); 
		
		HashMap<String, ArrayList<Chunk>> myChunks = record.getMyChunks();

		//list of service that my thread need to wait for
		ArrayList<DeleteEnhancementProtocol> subprotocols = new ArrayList<>();

		for(Entry<String, ArrayList<Chunk>> s : myChunks.entrySet())
		{
			Message msg = new Message(MessageType.GETINITIATOR,version,ID,s.getKey());
			DeleteEnhancementProtocol dep = new DeleteEnhancementProtocol(this, msg);
			dep.start();

			subprotocols.add(dep);
		}	
		//wait for all threads to finish
		for (DeleteEnhancementProtocol dep : subprotocols)
		{
			try {
				dep.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Logs.updated("deletition");
	}

	/**
	 * Function that adds to the scheduler the function checkChunks only if the enhancements are active.
	 */
	private void verifyChunks() {
		/**
		 * Function that gets all the chunks stored by this peer with the atual replication degree
		 * bellow the desired and try to initiate the chunk backup protocol for each chunk after a random time,
		 * only if, between this time, it didn't received any putchunk message for the chunk to backup.
		 * If the chunk backup protocol was initiated for a chunk, the peer will also send a Stored
		 * message warning the peers that himself has the chunk in backup.
		 */
		final Runnable checkChunks = new Runnable() {
			public void run() {
				Logs.checkChunks("replication degree"); 
				ArrayList<Chunk> chunks = record.getChunksWithRepBellowDes();

				for(Chunk c : chunks){

					msgRecord.removePutChunkMessages(c.getFileId(), c.getChunkNo());
					msgRecord.startRecordingPutchunks(c.getFileId());

					Util.randomDelay();

					if(!msgRecord.receivedPutchunkMessage(c.getFileId(), c.getChunkNo())){

						byte[] data = fileManager.getChunkContent(c.getFileId(), c.getChunkNo());
						Message msg = new Message(MessageType.PUTCHUNK,version,ID,c.getFileId(),c.getChunkNo(),c.getReplicationDeg(),data);
						new ChunkBackupProtocol(mdb, msgRecord, msg).start();

						Util.randomDelay();
						
						msgRecord.addStoredMessage(c.getFileId(), c.getChunkNo(), ID);
						//Warns the peers that it also has the chunk
						msg = new Message(MessageType.STORED,version,ID,c.getFileId(),c.getChunkNo());
						mc.send(msg);
					}
				}
			}
		};

		if(enhancement)
			scheduler.scheduleAtFixedRate(checkChunks, 60, 120, TimeUnit.SECONDS);
		
		Logs.updated("replication degree");
	}

	/**
	 * Runnable executed in 90s interval to save metadata, preventing mapping lost if the server crashes. 
	 */
	private void saveMetadata() {
		final Runnable saveMetadata = new Runnable() {
			public void run() { 
				saveRecord();
			}
		};
		scheduler.scheduleAtFixedRate(saveMetadata, 30, 90, TimeUnit.SECONDS);
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
			Logs.serializeWarn("saved in", ID);
		}
		catch (FileNotFoundException e) {
			Logs.exception("saveRecord", "Peer", e.toString());
			e.printStackTrace();
		}
		catch (IOException e) {
			Logs.exception("saveRecord", "Peer", e.toString());
			e.printStackTrace();
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
			Logs.exception("backup", "Peer", e.toString());
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
			Logs.exception("restore", "Peer", e.toString());
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
			Logs.exception("delete", "Peer", e.toString());
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
			Logs.exception("reclaim", "Peer", e.toString());
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
			Logs.exception("state", "Peer", e.toString());
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

	public boolean isEnhancement() {
		return enhancement;
	}

	public void setEnhancement(boolean enhancement) {
		this.enhancement = enhancement;
	}

	public FileManager getFileManager() {
		return fileManager;
	}
}
