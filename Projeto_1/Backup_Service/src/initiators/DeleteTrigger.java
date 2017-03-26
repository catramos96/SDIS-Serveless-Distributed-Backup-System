package initiators;

import java.security.NoSuchAlgorithmException;

import network.Message;
import peer.Peer;
import resources.Logs;
import resources.Util.MessageType;

public class DeleteTrigger extends Thread{
	private String filename;
	private Peer peer;
	/**
	 * Peer initiator response to client request for DELETE
	 */
	public DeleteTrigger(Peer peer, String filename){
		this.peer = peer;
		this.filename = filename;
	}
	
	public void run()
	{
		String fileId;
		
		try	{
			fileId = peer.fileManager.getFileIdFromResources(filename);
		} 
		catch (NoSuchAlgorithmException e){
			Logs.errorFileId(filename);
			return;
		}
		
		Message msg = new Message(MessageType.DELETE,peer.getVersion(),peer.getID(),fileId);
		Logs.sentMessageLog(msg);
		
		//send message twice because UDP is not reliable
		peer.mc.send(msg);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		peer.mc.send(msg);
		
		peer.getMulticastRecord().deleteStored(fileId);		
	}
}
