package initiators;

import network.Message;
import peer.FileInfo;
import peer.Peer;
import resources.Logs;
import resources.Util;
import resources.Util.MessageType;

/**
 * Peer initiator response to client request for delete a file.
 * @attribute Peer peer - initiator peer
 * @attribute String filename - file filename
 * @attribute String message - response to client
 */
public class DeleteTrigger extends Thread{
	
	private String filename;
	private Peer peer;
	private String message;

	/**
	 * constructor
	 * @param peer
	 * @param filename
	 */
	public DeleteTrigger(Peer peer, String filename){
		this.peer = peer;
		this.filename = peer.fileManager.checkPath(filename);
		this.message = null;
	}

	/**
	 * Thread execution
	 */
	@Override
	public void run()
	{	
		//verifies if this file was backed up
		FileInfo info = peer.record.getBackupFileInfoByPath(filename);

		if(info == null)
		{
			message = filename + " not backed up by this peer!";
			Logs.log(message);
			return;
		}

		//create message
		String fileId = info.getFileId();
		Message msg = new Message(MessageType.DELETE,peer.getVersion(),peer.getID(),info.getFileId());

		//send message twice because UDP is not reliable
		peer.getMc().send(msg);
		Logs.sentMessageLog(msg);
		
		try {
			Thread.sleep(Util.WAITING_TIME);
		} catch (InterruptedException e) {
			Logs.exception("run", "DeleteTrigger", e.toString());
			e.printStackTrace();
		}
		peer.getMc().send(msg);
		Logs.sentMessageLog(msg);

		//delete restores
		String dir = peer.getFileManager().diskDIR + Util.RESTORES_DIR + info.getFilename();
		peer.getFileManager().deleteFile(dir);

		//delete history from multicast data restore (mdr)
		peer.getMessageRecord().resetChunkMessages(fileId);
		
		//delete entries from record (backups and restores)
		peer.getRecord().deleteStoredFile(fileId);		
		peer.getRecord().deleteRestoredFile(fileId);

		message = "Delete successful!";
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
