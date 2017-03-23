package protocols;

import network.Message;
import network.MulticastListener;
import network.MulticastRecord;
import peer.Peer;

public class SpaceReclaimingProtocol extends Protocol{

	/*			MSG="REMOVED"		 --> Peer 	 Delay			Nº Chunks in
	 * InitPeer ---------------> MC ---> Peer  -------------> 	backup < that		---> ChunkBackupProtocol
	 * 								 --> Peer					replication degree
	 */
	
	public SpaceReclaimingProtocol(MulticastListener mc, MulticastRecord record){
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
		System.out.println("Protocol: Executing Space Reclaiming Protocol");
	}
	
}
