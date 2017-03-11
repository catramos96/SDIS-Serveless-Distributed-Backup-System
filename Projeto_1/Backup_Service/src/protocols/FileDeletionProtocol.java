package protocols;

import network.MulticastListener;

public class FileDeletionProtocol extends Protocol{
	
	/*			MSG="DELETE"		 --> Peer	
	 * InitPeer ---------------> MC ---> Peer 
	 * 								 --> Peer	
	 */

	FileDeletionProtocol(MulticastListener mc){
		this.mc = mc;
	}
	
	@Override
	public void warnPeers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeProtocolAction() {
		// TODO Auto-generated method stub
		
	}
	
}
