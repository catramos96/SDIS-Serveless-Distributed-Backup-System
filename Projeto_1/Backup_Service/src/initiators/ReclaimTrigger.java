package initiators;

import java.util.ArrayList;

import network.Message;
import peer.Chunk;
import peer.Peer;
import resources.Logs;
import resources.Util.MessageType;

/**
 * Peer initiator response to client request for reclaim disk space
 * @attribute Peer peer - initiator peer
 * @attribute int spaceToReclaim - space that the user wants to reclaim
 * @attribute String message - response to client
 */
public class ReclaimTrigger extends Thread
{
	private Peer peer;
	private int spaceToReclaim;
	private String message;
	
	/**
	 * constructor
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public ReclaimTrigger(Peer peer, int spaceToReclaim){
		this.peer = peer;
		this.spaceToReclaim = spaceToReclaim;
		this.message = null;
	}
	
	/**
	 * Thread execution
	 */
	@Override
	public void run(){
		
		//calculate the memory needed to release
		int memoryToRelease = peer.getFileManager().memoryToRelease(spaceToReclaim);
		
		if(memoryToRelease > 0)
		{
			ArrayList<Chunk> priorityChunks = peer.getRecord().getChunksWithRepAboveDes();
			ArrayList<String> backupChunks = peer.getFileManager().deleteNecessaryChunks(priorityChunks,memoryToRelease);
			
			for(String chunk : backupChunks)
			{
				Integer chunkNo = Integer.parseInt(chunk.substring(0,1));
				String fileId = chunk.substring(1,chunk.length());	
				
				//Remove from record
				peer.getRecord().removeFromMyChunks(fileId, chunkNo);
				
				Message msg = new Message(MessageType.REMOVED,peer.getVersion(),peer.getID(),fileId,chunkNo);
				peer.getMc().send(msg);
				Logs.sentMessageLog(msg);
			
			}
		}
		
		peer.getFileManager().setTotalSpace(spaceToReclaim);

		message = "TOTAL MEMORY: " + peer.getFileManager().getTotalSpace() +"\nREMAING MEMORY. " + peer.getFileManager().getRemainingSpace();
		Logs.log(message);
	}

	/**
	 * Return the feedback message to client
	 * @return
	 */
	public String response() {
		return message;
	}

}
