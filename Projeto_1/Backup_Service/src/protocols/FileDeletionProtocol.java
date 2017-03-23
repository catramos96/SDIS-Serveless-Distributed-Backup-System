package protocols;

import network.Message;
import network.MulticastListener;
import network.MulticastRecord;
import peer.Peer;

public class FileDeletionProtocol extends Protocol{
	
	/*			MSG="DELETE"		 --> Peer	
	 * InitPeer ---------------> MC ---> Peer 
	 * 								 --> Peer	
	 */

	public FileDeletionProtocol(MulticastListener mc, MulticastRecord record){
		this.mc = mc;
		this.record = record;
	}
	
	@Override
	public void warnPeers(Message msg) {
		// TODO Auto-generated method stub
		mc.send(msg);
		
	}

	@Override
	void executeProtocolAction(Message msg) {
		// TODO Auto-generated method stub
		System.out.println("Protocol: Executing File Deletion Protocol");
	}
	
}
