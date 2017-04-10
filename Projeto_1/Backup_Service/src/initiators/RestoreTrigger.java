package initiators;

import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

import network.DatagramListener;
import network.Message;
import peer.FileInfo;
import peer.Peer;
import protocols.ChunkRestoreProtocol;
import resources.Logs;
import resources.Util;
import resources.Util.MessageType;

/**
 * Peer initiator response to client request for restore a file.
 * @attribute Peer peer - initiator peer
 * @attribute String filename - file filename
 * @attribute String message - response to client
 */
public class RestoreTrigger extends Thread{
	
	private Peer peer;
	private String filename;
	private String message;
	
	/**
	 * constructor
	 * @param filename
	 * @throws NoSuchAlgorithmException 
	 */
	public RestoreTrigger(Peer peer, String filename)
	{
		this.peer = peer;
		this.filename = filename;
		//this.filename = peer.fileManager.checkPath(filename);
	}
	
	/**
	 * Thread execution
	 */
	@Override
	public void run()
	{
		try	
		{
			//verifies if this file was already backed up
			//FileInfo info = peer.record.getBackupFileInfoByPath(this.filename);
			FileInfo info = peer.getRecord().getBackupFileInfoByName(filename);
			
			if(info == null)
			{
				message = filename + " is not a path or was not backed up by this peer!";
				Logs.log(message);
				return;
			}
			
			//verifies if this file was already restored
			FileInfo restoredInfo = peer.getRecord().getRestoredFileInfoById(info.getFileId());
			if(restoredInfo != null)
			{
				message = info.getFilename() + " already restored!";
				Logs.log(message);
				return;
			}
						
			//prepares "record" for chunk messages
			peer.getRecord().startRecordRestores(info);

			DatagramListener receiveChunkChannel = null;
			
			if(peer.isEnhancement()){
				receiveChunkChannel = new DatagramListener(InetAddress.getLocalHost(),peer);
				receiveChunkChannel.start();
			}
			
			//create and send message for each chunk
			for(int i = 0; i < info.getNumChunks(); i++)
			{
				Message msg;
				
				//create message
				if(peer.isEnhancement()){
					String address = InetAddress.getLocalHost().getHostAddress();
					msg = new Message(MessageType.GETCHUNKENH,peer.getVersion(),peer.getID(),info.getFileId(),i,address,receiveChunkChannel.getPort());
				}else
					msg = new Message(MessageType.GETCHUNK,peer.getVersion(),peer.getID(),info.getFileId(),i);
				new ChunkRestoreProtocol(peer.getMc(),peer.getMessageRecord(),msg).start();
			}
			
			long startTime = System.currentTimeMillis(); //fetch starting time
			
			//verifies during x time if the restore was successful
			while((System.currentTimeMillis() - startTime) < Util.MAX_AVG_DELAY_TIME)	
			{
				//true when all 'chunk' messages received
			    if(peer.getRecord().checkAllChunksRestored(info))
			    {
			    	//creates the file
			    	peer.getFileManager().restoreFile(info.getFilename(), peer.getRecord().getRestores(info));
					Logs.fileRestored(info.getFilename());
					
					message = "Restore successful!";
					Logs.log(message);
					return;
			    }
			}
			
			if(peer.isEnhancement())
				receiveChunkChannel.destroy();
			
			//if file was not restores, the entries of objects that mapped this file must be deleted
			peer.getRecord().deleteRestoredFile(info.getFileId());
			peer.getMessageRecord().resetChunkMessages(info.getFileId());
			
			message = info.getFilename() + " not restored...";
			Logs.log(message);
		} 
		catch (IOException e) {
			Logs.exception("run", "RestoreTrigger", e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Return the feedback message to client
	 * @return
	 */
	public String response() {
		return message;
	}
}
