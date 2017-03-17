package protocols;

import network.Message;
import network.MulticastListener;
import network.Message.MessageType;

public class ChunkBackupProtocol extends Protocol{
	
	private int stored = 0;

	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	public ChunkBackupProtocol(MulticastListener mdb, MulticastListener mc){
		this.mdb = mdb;
		this.mc = mc;
	}
	
	public void incStored(){
		stored++; /*if received store for a specific chunk*/
	}

	@Override
	public void warnPeers(Message msg) {
		stored = 0;
		int rep = 0;
		
		while(rep < 5){
			
			mdb.send(msg);		//msg PutChunk
			
			try {
				Thread.sleep(1000);	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/*if(stored >= replicationDegree)
				break;*/
			
			rep++;
		}
	}

	@Override
	public void executeProtocolAction() {
		System.out.println("2 - Protocol: Executing Chunk Backup Protocol");
	
		/*
		try {
			Thread.sleep(delay.nextInt(400));	//delay
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		//if(replicationDegree <)
		//Message msg = new Message(MessageType.STORED,version,ID,c.getFileId(),c.getChunkNo(),replicationDegree,c.getData());
		//mc.send("stored");
	}
}
