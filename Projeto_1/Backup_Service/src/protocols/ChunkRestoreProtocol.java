package protocols;

import network.Message;
import network.MessageRecord;
import network.MulticastListener;
import peer.Record;
import resources.Logs;
import resources.Util;

public class ChunkRestoreProtocol extends Protocol{

	/**
	 * Order to restore 1 chunk
	 * @param mc
	 * @param record
	 * @param msg
	 */
	public ChunkRestoreProtocol(MulticastListener mc, MessageRecord record, Message msg){
		this.mc = mc;
		this.msgRecord = record;
		this.msg = msg;
	}

	@Override
	public void run()  
	{
		int rep = 0;
		int waitingTime = Util.WAITING_TIME;
		String fileNo = msg.getFileId();
		int chunkNo = msg.getChunkNo();
		
		
		
		while(rep < Util.MAX_TRIES)	
		{
			Logs.tryNrReceiveChunk(rep,chunkNo);
			
			//send message
			mc.send(msg);
			Logs.sentMessageLog(msg);
			
			//wait
			try {
				Thread.sleep(waitingTime);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//verifies if some chunk was restored
			if(msgRecord.receivedChunkMessage(fileNo, chunkNo)){
				System.out.println("Restored chunk n: " + chunkNo);
				return;
			}
			
			//inc repetitions
			rep++;
		}
		
		System.out.println("Chunk no: " + chunkNo + " not restored.");
	}
}
