package protocols;

import java.util.Random;

import network.Message;
import network.MulticastListener;
import network.MulticastRecord;
import peer.Peer;

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
		int waitingTime = 1000;
		String fileNo = msg.getFileId();
		int chunkNo = msg.getChunkNo();
		
		System.out.println("FileNo: " + fileNo);
		System.out.println("ChunkNo: " + chunkNo);
		
		while(stored < 1)	//alterar para rep
		{
			mdb.send(msg);		//msg PutChunk
			
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			stored = record.checkStored(fileNo, chunkNo);
			System.out.println("STORED from record: " + stored);
			
			if(stored >= msg.getReplicationDeg())	//replication degree done
				break;
			
			waitingTime *= 2;	//doubles time for each rep
			rep++;
		}
		
		//stored = 0; //reiniciar os contadores
	}
}
