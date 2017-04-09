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
	private String message;

	/**
	 * Peer initiator response to client request for DELETE
	 * @param peer
	 * @param filename
	 */
	public DeleteTrigger(Peer peer, String filename){
		this.peer = peer;
		this.filename = peer.fileManager.checkPath(filename);
		this.message = null;
	}

	@Override
	public void run()
	{	
		//verifies if this file was backed up
		FileInfo info = peer.record.getBackupFileInfoByPath(filename);

		if(info == null)
		{
			message = filename + " not backed up by this peer!";
			return;
		}

		//create message
		String fileId = info.getFileId();
		Message msg = new Message(MessageType.DELETE,peer.getVersion(),peer.getID(),info.getFileId());

		//send message twice because UDP is not reliable
		peer.mc.send(msg);
		Logs.sentMessageLog(msg);
		
		try {
			Thread.sleep(Util.WAITING_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		peer.mc.send(msg);
		Logs.sentMessageLog(msg);

		//delete restores
		String dir = peer.fileManager.diskDIR + Util.RESTORES_DIR + info.getFilename();
		peer.fileManager.deleteFile(dir);

		//delete history from multicast data restore (mdr)
		peer.msgRecord.resetChunkMessages(fileId);
		
		//delete entries from record (backups and restores)
		peer.getRecord().deleteStoredFile(fileId);		
		peer.getRecord().deleteRestoredFile(fileId);

		message = "Delete successful!";
	}

	public String response() {
		return message;
	}
}
