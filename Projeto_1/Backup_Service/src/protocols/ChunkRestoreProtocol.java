package protocols;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import network.Message;
import peer.FileInfo;
import peer.Peer;
import resources.Util.MessageType;

public class ChunkRestoreProtocol extends Protocol{

	private Peer peer;
	private String filename;

	public ChunkRestoreProtocol(Peer peer, String filename){
		this.peer = peer;
		this.filename = filename;
	}

	@Override
	public void run()  
	{
		String fileId;
		try 
		{
			fileId = peer.fileManager.getFileIdFromResources(filename);
		} 
		catch (NoSuchAlgorithmException e) 
		{
			System.out.println("Error searching for fileId of "+filename);
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
			System.out.println("(Sent) Type : "+msg.getType() + " from sender : "+ msg.getSenderId() + " with chunk "+ msg.getChunkNo());
			peer.mc.send(msg);
		}

		//TODO descobrir uma maneira melhor de fazer isto
		while(!peer.record.allRestored(info))
		{
			//verifica de 100 em 100 ms se ja foram restaurados todos os chunks
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		//file restore
		try 
		{
			peer.fileManager.restoreFile(info.getFilename(), peer.record.getRestores(info));
			System.out.println("File "+filename+" restored");
		} 
		catch (IOException e) 
		{
			System.out.println("Error restoring file");
			e.printStackTrace();
		}
	}
}
