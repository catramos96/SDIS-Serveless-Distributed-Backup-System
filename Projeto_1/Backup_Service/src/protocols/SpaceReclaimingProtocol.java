package protocols;

import network.Message;
import network.MulticastListener;
import network.MulticastRecord;

public class SpaceReclaimingProtocol extends Protocol{

	/*			MSG="REMOVED"		 --> Peer 	 Delay			Nº Chunks in
	 * InitPeer ---------------> MC ---> Peer  -------------> 	backup < that		---> ChunkBackupProtocol
	 * 								 --> Peer					replication degree
	 */
	
	public SpaceReclaimingProtocol(MulticastListener mc, MulticastRecord record, Message msg){
		this.mc = mc;
		this.record = record;
		this.msg = msg;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		mc.send(msg);
	}
	
}
