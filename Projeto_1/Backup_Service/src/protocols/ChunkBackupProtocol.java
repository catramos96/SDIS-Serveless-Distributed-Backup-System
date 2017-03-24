package protocols;

import java.util.ArrayList;
import java.util.Random;

import network.Message;
import network.MulticastListener;
import network.MulticastRecord;
import resources.Util;

public class ChunkBackupProtocol extends Protocol{

	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	public ChunkBackupProtocol(MulticastListener mdb, MulticastRecord record, Message msg){
		this.mdb = mdb;
		this.delay = new Random();
		this.record = record;
		this.msg = msg;
	}

	@Override
	public void run() {
		
		int stored = 0;
		int rep = 0;
		int waitingTime = Util.WAITING_TIME;
		String fileNo = msg.getFileId();
		int chunkNo = msg.getChunkNo();
		boolean end = false;
		
		while(rep < Util.MAX_TRIES)	
		{
			System.out.println("Try number "+ rep + " to stored chunk number "+msg.getChunkNo());
			
			mdb.send(msg);		//msg PutChunk
			
			try 
			{
				Thread.sleep(waitingTime);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			ArrayList<Integer> stored_peers = record.checkStored(fileNo, chunkNo);
			if(stored_peers != null)
				stored = stored_peers.size();
			
			//replication degree achieved
			if(stored >= msg.getReplicationDeg())
			{
				System.out.println("All Chunks with number "+ msg.getChunkNo()+ " Stored");
				end = true;
				break;
			}
			
			waitingTime *= Util.TIME_REINFORCEMENT;	//doubles time for each rep
			rep++;
		}
		if(!end)
		{
			System.out.println("Replication Degree not pleased for chunk number " + msg.getChunkNo());
		}
		
	}
}
