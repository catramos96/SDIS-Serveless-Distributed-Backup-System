package initiators;

import java.util.ArrayList;

import network.Message;
import peer.Peer;
import resources.Util.MessageType;

public class ReclaimTrigger extends Thread{
	protected Peer peer = null;
	protected int spaceToReclaim = 0;
	protected String message;
	
	/**
	 * Peer initiator response to client request for RECLAIM
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public ReclaimTrigger(Peer peer, int spaceToReclaim){
		this.peer = peer;
		this.spaceToReclaim = spaceToReclaim;
		this.message = null;
	}
	
	public void run(){
		
		//Calcula a memoria necessaria a libertar
		int memoryToRelease = peer.fileManager.memoryToRelease(spaceToReclaim);
		
		//System.out.println("SPACE: " + spaceToReclaim);
		//System.out.println("RELEASE MEMORY: " + memoryToRelease);
		
		if(memoryToRelease > 0){
			
			//elimina os ficheiros conforme o espa�o necess�rio
			ArrayList<String> backupChunks = peer.fileManager.deleteNecessaryChunks(memoryToRelease);
			
			for(String chunk : backupChunks){
				
				Integer chunkNo = Integer.parseInt(chunk.substring(0,1));
				String fileId = chunk.substring(1,chunk.length());	
				
				//System.out.println(fileId + "." + chunkNo);
				
				//System.out.println(peer.getMulticastRecord().checkStored(fileId, chunkNo));
				
				Message msg = new Message(MessageType.REMOVED,peer.getVersion(),peer.getID(),fileId,chunkNo);
				
				peer.getMc().send(msg);
				
			}
		
		}
		
		peer.fileManager.setTotalSpace(spaceToReclaim);
		
		message = "TOTAL MEMORY: " + peer.fileManager.getTotalSpace() +
				"\nREMAING MEMORY. " + peer.fileManager.getRemainingSpace();
	}

	public String response() {
		return message;
	}

}
