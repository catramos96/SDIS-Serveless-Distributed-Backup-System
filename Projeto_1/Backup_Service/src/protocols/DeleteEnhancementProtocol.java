package protocols;

import network.Message;
import peer.Peer;
import resources.Logs;
import resources.Util;

/**
 * Class DeleteEnhancementProtocol used for the Delete protocol enhancement.
 * The peer sent a message of type 'GETINITIATOR' from a file, and waits for the 'INITIATOR' message.
 * If the peer doesn't receive the 'INITIATOR' message, it must delete the chunks associated to the file.
 * @attribute Peer peer - peer that initiated the protocol
 * @attribute msg - Message to send
 */
public class DeleteEnhancementProtocol extends Thread {

	private Peer peer;
	private Message msg;
	
	/**
	 * Constructor
	 * @param peer
	 * @param msg
	 */
	public DeleteEnhancementProtocol(Peer peer, Message msg){
		this.peer = peer;
		this.msg = msg;
	}

	/**
	 * Thread execution
	 */
	@Override
	public void run() 
	{
		String fileId = msg.getFileId();
		//warns msgRecord that messages of type 'INITIATOR' will pass by the multicast for this fileId
		peer.getMessageRecord().startRecordingInitiators(fileId);
		
		//send message twice because udp is not reliable
		for(int i = 0; i < 2; i++)
		{
			peer.getMc().send(msg);
			Logs.sentMessageLog(msg);

			long startTime = System.currentTimeMillis(); //fetch starting time

			//verifies during x time if the restore was successful
			while((System.currentTimeMillis() - startTime) < Util.RND_DELAY)	
			{
				//true when file has initiator
				if(peer.getMessageRecord().receivedInitiatorMessage(fileId))
				{
					peer.getMessageRecord().resetInitiatorMessages(fileId);
					return;
				}
			}
		}
		
		//this message don't have initiator --> delete chunks with fileId
		peer.getFileManager().deleteChunks(fileId);
		peer.getRecord().deleteMyChunksByFile(fileId);
		peer.getMessageRecord().resetInitiatorMessages(fileId);
	}
}
