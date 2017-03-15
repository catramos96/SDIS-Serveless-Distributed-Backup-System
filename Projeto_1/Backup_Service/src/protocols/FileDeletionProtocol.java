package protocols;

import network.Message;
import network.MulticastListener;

public class FileDeletionProtocol extends Protocol{
	
	/*			MSG="DELETE"		 --> Peer	
	 * InitPeer ---------------> MC ---> Peer 
	 * 								 --> Peer	
	 */

	public FileDeletionProtocol(MulticastListener mc){
		this.mc = mc;
	}
	
	@Override
	public void warnPeers(Message msg) {
		// TODO Auto-generated method stub
		mc.send("deletion");
		
	}

	@Override
	public void executeProtocolAction() {
		// TODO Auto-generated method stub
		System.out.println("Protocol: Executing File Deletion Protocol");
		
	}
	
}
