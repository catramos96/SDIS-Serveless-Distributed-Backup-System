package initiators;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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
	}
	
	public void run(){
		String fileId;
		boolean restored = false;
		
		try	{
			filename = peer.fileManager.checkPath(filename);
			fileId = peer.fileManager.getFileIdFromFilename(filename);
			
			int chunks = peer.fileManager.getFileNumChunks(filename);

			//start recording chunk restores
			FileInfo info = new FileInfo(fileId,filename,chunks,1);			//1 - TMP -> REPLICATION DEGREE
			peer.record.startRecordRestores(info);

			//create and send message for each chunk
			for(int i = 0; i < info.getNumChunks(); i++)
			{
				Message msg = new Message(MessageType.GETCHUNK,peer.getVersion(),peer.getID(),info.getFileId(),i);
				Logs.sentMessageLog(msg);
				new ChunkRestoreProtocol(peer.getMc(),peer.getMulticastRecord(),msg).start();
			}
			
			long startTime = System.currentTimeMillis(); //fetch starting time
			
			while((System.currentTimeMillis()-startTime)<Util.MAX_AVG_DELAY_TIME)	
			{
			    if(peer.getMulticastRecord().allRestored(info)){
			    	peer.fileManager.restoreFile(info.getFilename(), peer.record.getRestores(info));
					Logs.fileRestored(info.getFilename());
					restored = true;
					break;
			    }
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if(!restored)
			message = "File Couldn't be restored";
		else
			message = "file restored with sucess";
		
		
	/*	//verifica de 100 em 100 ms se ja foram restaurados todos os chunks
		try{
			while(!peer.record.allRestored(info))
			{
				Thread.sleep(100);
			}
			//fileRestore
			peer.fileManager.restoreFile(info.getFilename(), peer.record.getRestores(info));
			Logs.fileRestored(info.getFilename());
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		
	}

	public String response() {
		return message;
	}
}
