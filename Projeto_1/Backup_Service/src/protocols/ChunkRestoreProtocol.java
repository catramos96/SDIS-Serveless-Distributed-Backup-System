package protocols;
import network.Message;
import network.MulticastListener;

public class ChunkRestoreProtocol extends Protocol{

	public ChunkRestoreProtocol(MulticastListener mdr, MulticastListener mc){
		this.mdr = mdr;
		this.mc = mc;
	}
	
	@Override
	public void warnPeers(Message msg) {
		int rep = 0;
		
		while(rep < 5){
			
			mc.send("restore");		//msg GetChunk
			
			try {
				Thread.sleep(1000);	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/*if(file_chunk_received) break*/
			
			rep++;
		}
	}

	@Override
	public void executeProtocolAction() {
		System.out.println("Protocol: Executing Chunk Restore Protocol");
		
		try {
			Thread.sleep(delay.nextInt(400));	//delay
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/*if (not received Chunk 4 this chunk) -> mc.send("Chunk")*/
	}

}
