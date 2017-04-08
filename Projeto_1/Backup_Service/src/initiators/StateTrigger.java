package initiators;

import java.util.ArrayList;
import java.util.HashMap;

import peer.Chunk;
import peer.FileInfo;
import peer.Peer;

public class StateTrigger extends Thread
{
	private Peer peer;
	private String message = null;
	
	/**
	 * Peer initiator response to client request for STATE
	 */
	public StateTrigger(Peer peer)
	{
		this.peer = peer;
	}
	
	public void run()
	{
		message = "\n\nFiles whose backup was initiated : \n\n";
		
		HashMap<FileInfo, HashMap<Integer, ArrayList<Integer>>> storedConfirms = peer.record.getStored();
		
		//For each file whose backup it has initiated:
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			message += " pathname : "+fileinfo.getPath() + "\n";
			message += " file id : "+fileinfo.getFileId()+"\n";
			message += " replication degree : "+fileinfo.getReplicationDeg()+"\n";
		
			HashMap<Integer, ArrayList<Integer>> chunks = storedConfirms.get(fileinfo);
			message += " chunks (id , perceived replication degree) :\n";
			for(Integer i : chunks.keySet())
			{
				ArrayList<Integer> peers = chunks.get(i);
				message += "  "+i.intValue()+fileinfo.getFileId()+ " , "+peers.size()+"\n";
			}
			
			message += "\n";
		}
		
		message += "Chunks stored :\n\n";
		
		//For each chunk it stores
		HashMap<String, ArrayList<Chunk>> files = peer.record.getMyChunks();
		for(String s : files.keySet())
		{
			ArrayList<Chunk> chunks = files.get(s);
			for(int i = 0; i < chunks.size(); i++)
			{
				//print do nome + tamanho + replication degree
				int size = chunks.get(i).getData().length / 1000;
				message += " name : "+chunks.get(i).getChunkNo()+chunks.get(i).getFileId() + " \n";
				message += " size : "+size + " KB\n";
				message += " perceived replication degree : "+chunks.get(i).getAtualRepDeg()+"\n";
				message += "\n";
			}
		}
		
		//peer storage capacity
		message += "Peer stored capacity :\n\n";
		message += " Maximum amount : "+peer.fileManager.getTotalSpace()+"\n";
		message += " Storage amount : "+peer.fileManager.getRemainingSpace()+"\n";	
	}

	public String response() {
		return message;
	}
}
