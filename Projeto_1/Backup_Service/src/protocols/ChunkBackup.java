package protocols;

public class ChunkBackup implements Protocol{

	/*			MSG="PUTCHUNK"		  --> Peer		MSG="STORED"		sleep(1sec)
	 * InitPeer ---------------> MDB ---> Peer -------------------> MC -------------> InitPeer
	 * 								  --> Peer		Random Delay
	 */
	
	void backupChunk(){
		
	}
	
	void storeChunk(){
		
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
