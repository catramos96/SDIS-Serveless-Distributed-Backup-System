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
		/* catch (InterruptedException e) {
			e.printStackTrace();
		}*/
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

	/*
	 * Peer triggers
	 */

	/**
	 * Peer initiator response to client request for backup
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public void BackupTrigger(String filename, int replicationDegree){

		//split file in chunks
		ArrayList<Chunk> chunks = fileManager.splitFileInChunks(filename);	

		for (int i = 0; i < chunks.size(); i++) 
		{
			//create message for each chunk
			Chunk c = chunks.get(i);
			Message msg = new Message(MessageType.PUTCHUNK,version,ID,c.getFileId(),c.getChunkNo(),replicationDegree,c.getData());
			System.out.println("(Sent) Type : "+ msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());

			//initiate file record
			FileInfo fileinfo = new FileInfo(msg.getFileId(),filename,chunks.size());
			record.startRecordStores(fileinfo);

			//warn other peers
			new ChunkBackupProtocol(mdb,record,msg).start();
		}
	}

	/**
	 * Peer initiator response to client request for RESTORE
	 * @param filename
	 * @throws NoSuchAlgorithmException 
	 */
	public void RestoreTrigger(String filename)
	{
		/*
		String fileId;
		try 
		{
			fileId = fileManager.getFileIdFromResources(filename);
		} 
		catch (NoSuchAlgorithmException e) 
		{
			System.out.println("Error searching for fileId of "+filename);
			return;
		}
		int chunks = fileManager.getFileNumChunks(filename);

		//start recording chunk restores
		FileInfo info = new FileInfo(fileId,filename,chunks);
		
		record.startRecordRestores(info);

		//create and send message for each chunk
		for(int i = 0; i < chunks; i++)
		{
			Message msg = new Message(MessageType.GETCHUNK,version,ID,fileId,i);
			System.out.println("(Sent) Type : "+msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());
			mc.send(msg);
			//new ChunkRestoreProtocol(mdr,mc,record,msg).start();
		}*/
		new ChunkRestoreProtocol(this, filename).start();
	}

	/**
	 * Peer initiator response to client request for DELETE
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public void DeleteTrigger(){

	}

	/**
	 * Peer initiator response to client request for RECLAIM
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public void ReclaimTrigger(){

	}

	/**
	 * Peer initiator response to client request for STATE
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public void StateTrigger(){

	}

	/*
	 * Peer responses
	 */

	/**
	 * Peer response to other peer PUTCHUNK message
	 * @param c
	 */
	public synchronized void receivedPutchunk(String fileId, int chunkNo, byte[] body)
	{	
		Chunk c = new Chunk(fileId, chunkNo, body);

		//response message : STORED
		Message msg = new Message(Util.MessageType.STORED,version,ID,c.getFileId(),c.getChunkNo());
		System.out.println("(Sent) Type : "+ msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());

		//verifies chunk existence in this peer
		boolean alreadyExists = fileManager.chunkExists(c.getFileId(),c.getChunkNo());

		//no space available and file does not exist -> can't store
		if(!fileManager.hasSpaceAvailable(c) && !alreadyExists)
			return;
		else
		{
			//waiting time
			randomDelay();

			/*if(record.checkStored(msg.getFileId(), msg.getChunkNo()) < c.getReplicationDeg()){*/

			//send STORED message
			mc.send(msg);
			//only save if file doesn't exist
			if(!alreadyExists)
				fileManager.save(c);

			/*}	*/
		}
	}

	/**
	 * Peer response to other peer STORE message
	 */
	public synchronized void receivedStore(String fileId, int chunkNo, int sender)
	{
		//record chunk only if this peer is the initiator peer
		record.recordStoreChunks(fileId, chunkNo, sender);
	}

	/**
	 * Peer response to other peer GETCHUNK message
	 */
	public synchronized void receivedGetchunk(String fileId, int chunkNo)
	{
		//peers has stored this chunk
		if(fileManager.chunkExists(fileId,chunkNo))
		{
			//body
			byte[] body = fileManager.getChunkContent(fileId, chunkNo);
			//create CHUNK message
			Message msg = new Message(Util.MessageType.CHUNK,version,ID,fileId,chunkNo,body);
			randomDelay();
			//chunk still needed by the initiator peer
			if(!chunkRestored(fileId, chunkNo))
			{
				System.out.println("(Sent) Type : "+ msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());
				mdr.send(msg);
			}
		}

	}

	private boolean chunkRestored(String fileId, int chunkNo) {
		String chunkName = chunkNo+fileId;
		
		return mdrRestores.contains(chunkName);
	}

	/**
	 * Peer response to other peer CHUNK message
	 */
	public synchronized void receivedChunk(String fileId, int chunkNo, byte[] chunkBody)
	{
		//verifies if the file belongs to record --> initiator peer
		if(record.checkRestore(fileId))
		{
			//chunk restore
			if(record.recordRestoreChunks(fileId,chunkNo,chunkBody))
				System.out.println("Chunk Number "+chunkNo+" restored");
		}
		else	//other peer
		{
			//save history of chunks at mdr (chunkNo, fileId)
			mdrRestores.add(chunkNo+fileId);
		}
	}

	/**
	 * Peer response to other peer DELETE message
	 */
	public synchronized void receivedDelete(String fileNo){

		//fileChunksExists?
		//Remove all chunks from fileNo
	}

	/**
	 * Peer response to other peer REMOVED message
	 */
	public synchronized void receivedRemoved(String fileNo,int chunkNo){
		//fileChunksExists?
		//Remove all chunks from fileNo
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

	public MulticastRecord getMulticastRecord(){
		return record;
	}
}
