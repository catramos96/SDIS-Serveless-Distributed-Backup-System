package initiators;

import java.io.File;
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
	private String message = null;

	/**
	 * Peer initiator response to client request for backup
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public BackupTrigger(Peer peer, String filename, int replicationDegree)
	{	
		this.peer = peer;
		this.replicationDegree = replicationDegree;
		this.filename = peer.fileManager.checkPath(filename);
		
		//verifies original file existence
		File f = new File(this.filename);
		if(!f.exists())
		{
			message = filename + " not found!\n";
			return;
		}

		//verifies if this file was already backed up 
		FileInfo info = peer.record.fileBackup(this.filename);

		if(info != null)
		{
			//if already exists, verify if the fileId's are the same
			String fileId1 = peer.fileManager.getFileIdFromFilename(this.filename);
			String fileId2 = info.getFileId(); 

			//yes : ignore this request
			if(fileId1.equals(fileId2)){
				message = "File already backed up!";
				return;
			}

			//no : it means that this new file is a modification
			this.deleteFirst = true;
		}
	}

	public void run()
	{
		if(deleteFirst)
		{
			//must delete the old chunks
			Logs.initProtocol("Delete");
			DeleteTrigger dt = new DeleteTrigger(peer, filename);	
			dt.start();
			try 
			{
				dt.join();	 //waits for chunks delete
			} 
			catch (InterruptedException e) {
				System.err.println("Server exception: " + e.toString());
				e.printStackTrace();
			}
		}

		//split file in chunks
		chunks = peer.fileManager.splitFileInChunks(filename);	

		//list of service that my thread need to wait for
		ArrayList<ChunkBackupProtocol> subprotocols = new ArrayList<>();

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
			ChunkBackupProtocol cbp = new ChunkBackupProtocol(peer.getMdb(),peer.getMulticastRecord(),msg); 
			subprotocols.add(cbp);
			cbp.start();
		}

		//wait for all threads to finish
		for (ChunkBackupProtocol f : subprotocols)
		{
			try {
				f.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		message = "Backup successful!";
	}

	public String response() {
		return message;
	}
}
