package protocols;
import network.MulticastListener;

public class ChunkRestoreProtocol extends Protocol{

	public ChunkRestoreProtocol(MulticastListener mdr, MulticastListener mc){
		this.mdr = mdr;
		this.mc = mc;
	}
	
	@Override
	public void warnPeers() {
		// TODO Auto-generated method stub
		mc.send("restore");
	}

	@Override
	public void executeProtocolAction() {
		// TODO Auto-generated method stub
		System.out.println("Protocol: Executing Chunk Restore Protocol");
	}

}
