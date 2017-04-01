package initiators;

import network.Message;
import peer.FileInfo;
import peer.Peer;
import resources.Logs;
import resources.Util;
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
		FileInfo info = peer.record.fileBackup(filename);
		
		//file backed up 
		if(info != null)
		{
			//create message
			String fileId = info.getFileId();
			Message msg = new Message(MessageType.DELETE,peer.getVersion(),peer.getID(),info.getFileId());
			
			//send message twice because UDP is not reliable
			peer.mc.send(msg);
			Logs.sentMessageLog(msg);
			
			//delete restores
			String dir = peer.fileManager.diskDIR + Util.RESTORES_DIR+info.getFilename();
			peer.fileManager.deleteFile(dir);
			
			//delete entries
			peer.getMulticastRecord().deleteStoreEntry(fileId);		
			peer.getMulticastRecord().deleteRestoreEntry(fileId);
			
			//delete own file ?
			//peer.fileManager.deleteFile(filename);
			
			//client response
			peer.setMessage("delete file");
		}
		else
			peer.setMessage("delete file problem");
	}
}
