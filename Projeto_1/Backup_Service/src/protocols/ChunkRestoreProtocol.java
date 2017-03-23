package protocols;
import network.Message;
import network.MulticastListener;
import network.MulticastRecord;
import peer.Peer;

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
		
		while(rep < 5){
			
			mc.send(msg);		//msg GetChunk
			
			try {
				Thread.sleep(1000);	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/*if(file_chunk_received) break*/
			
			rep++;
		}
	}

	/*@Override
	void executeProtocolAction(Message msg) {
		// TODO Auto-generated method stub
		System.out.println("Protocol: Executing Chunk Restore Protocol");
		/*
		try {
			Thread.sleep(delay.nextInt(400));	//delay
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/*if (not received Chunk 4 this chunk) -> mc.send("Chunk")
	}*/

}
