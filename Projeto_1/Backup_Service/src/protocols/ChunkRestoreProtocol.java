package protocols;

import network.Message;
import network.MessageRecord;
import network.MulticastListener;
import resources.Logs;
import resources.Util;

/**
 * Class ChunkRestoreProtocol used to restore a chunk of a file until 5 tries.
 */
public class ChunkRestoreProtocol extends Protocol {

	/**
	 * Constructor of ChunkRestoreProtocol
	 * @param mc - MulticasListener channel where it will be communicated the request of a chunk
	 * @param record - MessageRecord of the communication channels
	 * @param msg - Message of request
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
				Logs.chunkRestored(chunkNo);
				return;
			}
			
			//inc repetitions
			rep++;
		}
		Logs.chunkNotRestored(chunkNo);
	}
}
