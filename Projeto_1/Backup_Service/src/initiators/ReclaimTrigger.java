package initiators;

import peer.Peer;

public class ReclaimTrigger extends Thread{
	protected Peer peer = null;
	protected int spaceToReclaim = 0;
	/**
	 * Peer initiator response to client request for RECLAIM
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public ReclaimTrigger(Peer peer, int spaceToReclaim){
		this.peer = peer;
		this.spaceToReclaim = spaceToReclaim;
	}
	
	public void run(){
		
		int memoryToRelease = peer.fileManager.memoryToRelease(spaceToReclaim);
		
		System.out.println("SPACE: " + spaceToReclaim);
		System.out.println("RELEASE MEMORY: " + memoryToRelease);
		
		if(memoryToRelease > 0){
			peer.fileManager.deleteNecessaryChunks(spaceToReclaim);
		}
		
	}
	
}
