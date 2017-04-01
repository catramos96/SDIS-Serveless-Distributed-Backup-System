package protocols;

import network.Message;
import network.MulticastListener;
import peer.Record;
import resources.Util;

public class ChunkRestoreProtocol extends Protocol{

	public ChunkRestoreProtocol(MulticastListener mc, Record record, Message msg){
		this.mc = mc;
		this.record = record;
		this.msg = msg;
	}

	@Override
	public void run()  
	{
		boolean restored = false;
		boolean end = false;
		int rep = 0;
		int waitingTime = Util.WAITING_TIME;
		String fileNo = msg.getFileId();
		int chunkNo = msg.getChunkNo();
		
		while(rep < Util.MAX_TRIES)	
		{
			System.out.println("Times :" + rep + " of " +  chunkNo);
			
			mc.send(msg);		//msg GETCHUNK
			
			try {
				Thread.sleep(waitingTime);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(record.checkChunkRestored(fileNo, chunkNo)){
				end = true;
				System.out.println("restored chunk n: " + chunkNo);
				break;
			}
			
			waitingTime *= Util.TIME_REINFORCEMENT;	//doubles time for each rep
			rep++;
		}
		if(!end){
			System.out.println("chunk no: " + chunkNo + " not restored");
		}
		
	}
}
