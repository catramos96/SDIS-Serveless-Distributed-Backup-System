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
	
	public ChunkBackupProtocol(MulticastListener mdb, MulticastListener mc, MulticastRecord record){
		this.mdb = mdb;
		this.mc = mc;
		this.delay = new Random();
		this.record = record;
	}

	@Override
	public void warnPeers(Message msg) {
		
		int stored = 0;
		int rep = 0;
		int waitingTime = 1000;
		String fileNo = msg.getFileId();
		int chunkNo = msg.getChunkNo();
		
		System.out.println("FileNo: " + fileNo);
		System.out.println("ChunkNo: " + chunkNo);
		
		while(rep < 1)	//alterar para rep
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

	@Override
	public void executeProtocolAction(Message msg) {
		System.out.println("2 - Protocol: Executing Chunk Backup Protocol");
		
		try 
		{
			Thread.sleep(delay.nextInt(400)); //delay
		} 
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		mc.send(msg);
	}
}
