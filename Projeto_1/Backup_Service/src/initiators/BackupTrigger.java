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
	/**
	 * Peer initiator response to client request for backup
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public BackupTrigger(Peer peer, String filename, int replicationDegree){
		//split file in chunks
		chunks = peer.fileManager.splitFileInChunks(filename);	
		this.peer = peer;
		this.filename = filename;
		this.replicationDegree = replicationDegree;
	}
	
	public void run(){
		
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
		
		peer.setMessage("backup file");
	}
}
