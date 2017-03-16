package protocols;

import network.Message;
import network.MulticastListener;

public class SpaceReclaimingProtocol extends Protocol{

	/*			MSG="REMOVED"		 --> Peer 	 Delay			Nº Chunks in
	 * InitPeer ---------------> MC ---> Peer  -------------> 	backup < that		---> ChunkBackupProtocol
	 * 								 --> Peer					replication degree
	 */
	
	public SpaceReclaimingProtocol(MulticastListener mc){
		this.mc = mc;
	}

	@Override
	public void warnPeers(Message msg) {
		// TODO Auto-generated method stub
		mc.send(msg);
	}

	@Override
	public void executeProtocolAction() {
		// TODO Auto-generated method stub
		System.out.println("Protocol: Executing Space Reclaiming Protocol");
		
	}
	
}
