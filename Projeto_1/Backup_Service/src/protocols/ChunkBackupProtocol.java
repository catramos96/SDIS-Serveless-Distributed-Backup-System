package protocols;

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
			mdb.send(msg);		//msg PutChunk
			
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			stored = record.checkStored(fileNo, chunkNo);
			if(stored > 0 )
				System.out.println("STORED from record: " + stored);
			
			/*if(stored >= msg.getReplicationDeg())	//replication degree done
				break;*/
			
			if(stored > 0){
				System.out.println("END - " + msg.getChunkNo());
				end = true;
				break;
			}
			
			waitingTime *= Util.TIME_REINFORCEMENT;	//doubles time for each rep
			rep++;
		}
		if(!end){
			System.out.println("ERROR - " + msg.getChunkNo());
		}
		
	}
}
