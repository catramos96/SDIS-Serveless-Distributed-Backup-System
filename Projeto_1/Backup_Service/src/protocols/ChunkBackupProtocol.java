package protocols;

import java.util.ArrayList;
import java.util.Random;

import network.Message;
import network.MulticastListener;
import peer.Record;
import resources.Logs;
import resources.Util;

public class ChunkBackupProtocol extends Protocol{

	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	/**
	 * Backup of 1 chunk
	 * @param mdb
	 * @param record
	 * @param msg
	 */
	public ChunkBackupProtocol(MulticastListener mdb, Record record, Message msg){
		this.mdb = mdb;
		this.delay = new Random();
		this.record = record;
		this.msg = msg;
	}

	@Override
	public void run() 
	{	
		int stored = 0;
		int rep = 0;
		int waitingTime = Util.WAITING_TIME;
		String fileNo = msg.getFileId();
		int chunkNo = msg.getChunkNo();
		
		//try 5 times 
		while(rep < Util.MAX_TRIES)	
		{
			Logs.tryNrStoreChunk(rep, msg.getChunkNo());
		
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
			
			//count peers with chunks stored
			ArrayList<Integer> stored_peers = record.checkStored(fileNo, chunkNo);
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
	}
}
