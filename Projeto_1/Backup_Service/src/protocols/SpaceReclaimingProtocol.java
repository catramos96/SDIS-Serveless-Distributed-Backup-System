package protocols;

import network.MulticastListener;

public class SpaceReclaimingProtocol extends Protocol{

	/*			MSG="REMOVED"		 --> Peer 	 Delay			Nº Chunks in
	 * InitPeer ---------------> MC ---> Peer  -------------> 	backup < that		---> ChunkBackupProtocol
	 * 								 --> Peer					replication degree
	 */
	
	SpaceReclaimingProtocol(MulticastListener mc){
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
