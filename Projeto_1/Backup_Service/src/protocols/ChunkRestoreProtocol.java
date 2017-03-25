package protocols;
import network.Message;
import network.MulticastListener;
import network.MulticastRecord;
import resources.Util;

public class ChunkRestoreProtocol extends Protocol{

	public ChunkRestoreProtocol(MulticastListener mdr, MulticastListener mc, MulticastRecord record, Message msg){
		this.mdr = mdr;
		this.mc = mc;
		this.record = record;
		this.msg = msg;
	}
	
	@Override
	public void run()  {
		int rep = 0;
		
		while(rep < Util.MAX_TRIES){
			
			mc.send(msg);		//msg GetChunk
			
			try {
				Thread.sleep(Util.WAITING_TIME);	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/*if(file_chunk_received) break*/
			
			rep++;
		}
	}
}
