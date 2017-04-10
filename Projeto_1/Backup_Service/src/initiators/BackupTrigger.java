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

/**
 * Peer initiator response to client request for backup a file.
 * @attribute ArrayList<Chunk> chunks - chunks to backup
 * @attribute Peer peer - initiator peer
 * @attribute String filename - file filename
 * @attribute int replicationDegree - replication degree desired
 * @attribute String message - response to client
 */
public class BackupTrigger extends Thread{
	
	private ArrayList<Chunk> chunks = null;
	private Peer peer = null;
	private String filename = null;
	private int replicationDegree = 0;
	private String message = null;

	/**
	 * Constructor
	 * @param action
	 * @param filename
	 * @param replicationDegree
	 */
	public BackupTrigger(Peer peer, String filename, int replicationDegree)
	{	
		this.peer = peer;
		this.replicationDegree = replicationDegree;
		this.filename = peer.fileManager.checkPath(filename);
	}

	/**
	 * Thread execution
	 */
	@Override
	public void run()
	{
		//verifies original file existence
		File f = new File(this.filename);
		if(!f.exists())
		{
			message = filename + " not found!\n";
			Logs.log(message);
			return;
		}

		//verifies if this file was already backed up 
		FileInfo info = peer.getRecord().getBackupFileInfoByPath(this.filename);

		if(info != null)
		{
			//if already exists, verify if the fileId's are the same
			String fileId1 = peer.getFileManager().getFileIdFromFilename(this.filename);
			String fileId2 = info.getFileId(); 

			//yes : ignore this request
			if(fileId1.equals(fileId2)){
				message = "File already backed up!";
				Logs.log(message);
				return;
			}

			//no : it means that this file is a modification
			Logs.initProtocol("Delete");
			//delete old chunks
			DeleteTrigger dt = new DeleteTrigger(peer, filename);	
			dt.start();
			try 
			{
				dt.join();	 //waits for chunks delete
			} 
			catch (InterruptedException e) {
				Logs.exception("run", "BackupTrigger", e.toString());
				e.printStackTrace();
			}
		}

		//split file in chunks
		chunks = peer.getFileManager().splitFileInChunks(filename);
		
		//initiate file record
		String fileID = peer.getFileManager().getFileIdFromFilename(filename);
		FileInfo fileinfo = new FileInfo(fileID,filename,chunks.size(),replicationDegree);
		peer.getRecord().startRecordStores(fileinfo);

		//list of service that my thread need to wait for
		ArrayList<ChunkBackupProtocol> subprotocols = new ArrayList<>();

		for (int i = 0; i < chunks.size(); i++) 
		{
			//create message for each chunk
			Chunk c = chunks.get(i);
			Message msg = new Message(MessageType.PUTCHUNK,peer.getVersion(),peer.getID(),c.getFileId(),c.getChunkNo(),replicationDegree,c.getData());

			//warn other peers
			ChunkBackupProtocol cbp = new ChunkBackupProtocol(peer.getMdb(),peer.getMessageRecord(),msg); 
			subprotocols.add(cbp);
			cbp.start();
		}

		//wait for all threads to finish
		for (ChunkBackupProtocol cbp : subprotocols)
		{
			try {
				cbp.join();
			} catch (InterruptedException e) {
				Logs.exception("run", "BackupTrigger", e.toString());
				e.printStackTrace();
			}

		}
		message = "Backup successful!";
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
