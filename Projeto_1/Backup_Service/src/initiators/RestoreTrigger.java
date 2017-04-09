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

public class RestoreTrigger extends Thread{
	
	private Peer peer = null;
	private String filename = null;
	private String message = null;
	
	/**
	 * Peer initiator response to client request for RESTORE
	 * @param filename
	 * @throws NoSuchAlgorithmException 
	 */
	public RestoreTrigger(Peer peer, String filename)
	{
		this.peer = peer;
		this.filename = filename;
		//this.filename = peer.fileManager.checkPath(filename);
	}
	
	@Override
	public void run()
	{
		try	
		{
			//verifies if this file was already backed up
			//FileInfo info = peer.record.getBackupFileInfoByPath(this.filename);
			FileInfo info = peer.record.getBackupFileInfoByName(filename);
			
			if(info == null)
			{
				message = filename + " is not a path or was not backed up by this peer!";
				return;
			}
			
			//verifies if this file was already restored
			FileInfo restoredInfo = peer.record.getRestoredFileInfoById(info.getFileId());
			if(restoredInfo != null)
			{
				message = info.getFilename() + " already restored!";
				return;
			}
						
			//prepares "record" for chunk messages
			peer.record.startRecordRestores(info);

			DatagramListener receiveChunkChannel = null;
			
			if(peer.enhancementVersion()){
				receiveChunkChannel = new DatagramListener(InetAddress.getLocalHost(),peer);
				receiveChunkChannel.start();
			}
			
			//create and send message for each chunk
			for(int i = 0; i < info.getNumChunks(); i++)
			{
				Message msg;
				
				//create message
				if(peer.enhancementVersion()){
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
			    if(peer.getMulticastRecord().checkAllChunksRestored(info))
			    {
			    	//creates the file
			    	peer.fileManager.restoreFile(info.getFilename(), peer.record.getRestores(info));
					Logs.fileRestored(info.getFilename());
					
					message = "Restore successful!";
					return;
			    }
			}
			
			if(peer.enhancementVersion())
				receiveChunkChannel.destroy();
			
			//if file was not restores, the entries of objects that mapped this file must be deleted
			peer.record.deleteRestoredFile(info.getFileId());
			peer.msgRecord.resetChunkMessages(info.getFileId());
			
			message = info.getFilename() + " not restored...";
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String response() {
		return message;
	}
}
