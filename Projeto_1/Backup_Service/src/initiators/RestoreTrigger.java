package initiators;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import network.Message;
import peer.FileInfo;
import peer.Peer;
import resources.Logs;
import resources.Util.MessageType;

public class RestoreTrigger extends Thread{
	
	Peer peer = null;
	String filename = null;
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
		
		try	{
			fileId = peer.fileManager.getFileIdFromFilename(filename);
		} 
		catch (NoSuchAlgorithmException e){
			Logs.errorFileId(filename);
			return;
		}
		
		int chunks = peer.fileManager.getFileNumChunks(filename);

		//start recording chunk restores
		FileInfo info = new FileInfo(fileId,filename,chunks);
		
		peer.record.startRecordRestores(info);

		//create and send message for each chunk
		for(int i = 0; i < info.getNumChunks(); i++)
		{
			Message msg = new Message(MessageType.GETCHUNK,peer.getVersion(),peer.getID(),info.getFileId(),i);
			Logs.sentMessageLog(msg);
			peer.mc.send(msg);
		}
		
		
		//verifica de 100 em 100 ms se ja foram restaurados todos os chunks
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
		}
		
		peer.setMessage("restore");
	}
}
