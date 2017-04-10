package protocols;

import java.util.ArrayList;
import java.util.Random;

import network.Message;
import network.MessageRecord;
import network.MulticastListener;
import resources.Logs;
import resources.Util;

/**
 * Class ChunkBackupProtocol used to backup a chunk of a file until 5 tries.
 */
public class ChunkBackupProtocol extends Protocol{

	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	/**
	 * Constructor of ChunkBackupProtocol
	 * @param mdb - MulticastListener channel to where the request will be sent
	 * @param record - MessageRecord of the channel
	 * @param msg - Message of the request
	 */
	public ChunkBackupProtocol(MulticastListener mdb, MessageRecord record, Message msg){
		this.mdb = mdb;
		this.delay = new Random();
		this.msgRecord = record;
		this.msg = msg;
	}

	@Override
	public void run() 
	{	
		int stored = 0;			//number of times the chunk was stored
		int rep = 0;			//tries
		int waitingTime = Util.WAITING_TIME;
		String fileNo = msg.getFileId();
		int chunkNo = msg.getChunkNo();
		
		//resets stored messages of the record
		msgRecord.removeStoredMessages(fileNo, chunkNo);
		//resets putchunks messages of the record and start a new record
		msgRecord.removePutChunkMessages(fileNo, chunkNo);
		msgRecord.startRecordingPutchunks(fileNo);
		
		//try 5 times 
		while(rep < Util.MAX_TRIES)	
		{
			Logs.tryNrStoreChunk(rep, msg.getChunkNo());
			
			//if it receives a putchunk for the same file and chunk, the backup will end
			if(msgRecord.receivedPutchunkMessage(fileNo, chunkNo))
				return;
		
			//send message
			mdb.send(msg);
			Logs.sentMessageLog(msg);
			
			//waits
			try {
				Thread.sleep(waitingTime);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			ArrayList<Integer> stored_peers = msgRecord.getPeersWithChunk(fileNo, chunkNo);
			if(stored_peers != null)
				stored = stored_peers.size();
			
			//replication degree achieved
			if(stored >= msg.getReplicationDeg())
			{
				Logs.allChunksNrStored(msg.getChunkNo());
				return;
			}
			
			waitingTime *= Util.TIME_REINFORCEMENT;	//doubles time for each rep
			rep++;
		}
		
		Logs.chunkRepDegNotAccepted(msg.getChunkNo(),stored);
		msgRecord.removeStoredMessages(fileNo, chunkNo);
	}
}
