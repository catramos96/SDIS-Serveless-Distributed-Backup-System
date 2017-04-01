package initiators;

import java.util.ArrayList;

import network.Message;
import peer.Chunk;
import peer.FileInfo;
import peer.Peer;
import protocols.ChunkBackupProtocol;
import resources.Logs;
import resources.Util.MessageType;

public class BackupTrigger extends Thread{
	ArrayList<Chunk> chunks = null;
	Peer peer = null;
	String filename = null;
	int replicationDegree = 0;
	boolean deleteFirst = false;
	/**
	 * Peer initiator response to client request for backup
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public BackupTrigger(Peer peer, String filename, int replicationDegree){	
	
		this.peer = peer;
		this.filename = filename;
		this.replicationDegree = replicationDegree;
		
		filename = peer.fileManager.checkPath(filename);
		
		//verifies if this file was already backed up 
		FileInfo info = peer.record.fileBackup(filename);
		if(info != null)
		{
			//if already exists, verify if the fileId's are the same
			String fileId1 = peer.fileManager.getFileIdFromFilename(filename);
			String fileId2 = info.getFileId(); 
			
			//yes : ignore this request
			if(fileId1.equals(fileId2))
				return;
			
			//no : it means that this new file is a modification
			this.deleteFirst = true;
		}
	}
	
	public void run()
	{
		if(deleteFirst)
		{
			System.out.println("start deleting");
			//must delete the old chunks
			new DeleteTrigger(peer, filename).start();	
			
			//espera que o delete termine? (devia arranjar uma maneira mais eficiente de fazer isto)
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//split file in chunks
		chunks = peer.fileManager.splitFileInChunks(filename);	
		
		for (int i = 0; i < chunks.size(); i++) 
		{
			//create message for each chunk
			Chunk c = chunks.get(i);
			Message msg = new Message(MessageType.PUTCHUNK,peer.getVersion(),peer.getID(),c.getFileId(),c.getChunkNo(),replicationDegree,c.getData());
			Logs.sentMessageLog(msg);
			
			//initiate file record
			FileInfo fileinfo = new FileInfo(msg.getFileId(),filename,chunks.size(),replicationDegree);
			peer.getMulticastRecord().startRecordStores(fileinfo);

			//warn other peers
			new ChunkBackupProtocol(peer.getMdb(),peer.getMulticastRecord(),msg).start(); // fazer aqui !!!
			
		}
		
		peer.setMessage("backup file");	//feedback for client
	}
}
