package protocols;

import network.MulticastListener;

public class ChunkBackupProtocol extends Protocol{

	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	ChunkBackupProtocol(MulticastListener mdb, MulticastListener mc){
		this.mdb = mdb;
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
