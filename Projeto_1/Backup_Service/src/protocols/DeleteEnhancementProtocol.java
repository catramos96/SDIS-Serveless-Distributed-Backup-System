package protocols;

import network.Message;
import peer.Peer;
import resources.Logs;
import resources.Util;

public class DeleteEnhancementProtocol extends Thread {

	private Peer peer;
	private Message msg;
	
	public DeleteEnhancementProtocol(Peer peer, Message msg){
		this.peer = peer;
		this.msg = msg;
	}

	@Override
	public void run() 
	{
		String fileId = msg.getFileId();
		//warns msgRecord that messages of type 'INITIATOR' will pass by the multicast for this fileId
		peer.msgRecord.startRecordingInitiators(fileId);
		
		//send message twice because udp is not reliable
		for(int i = 0; i < 2; i++)
		{
			System.out.println(msg.getFileId());
			peer.mc.send(msg);
			Logs.sentMessageLog(msg);

			long startTime = System.currentTimeMillis(); //fetch starting time

			//verifies during x time if the restore was successful
			while((System.currentTimeMillis() - startTime) < Util.RND_DELAY)	
			{
				//true when file has initiator
				if(peer.msgRecord.receivedInitiatorMessage(fileId))
				{
					peer.msgRecord.resetInitiatorMessages(fileId);
					return;
				}
			}
		}
		
		//this message don't have initiator --> delete chunks with fileId
		peer.fileManager.deleteChunks(fileId);
		peer.record.deleteMyChunksByFile(fileId);
		
		peer.msgRecord.resetInitiatorMessages(fileId);
	}
}
