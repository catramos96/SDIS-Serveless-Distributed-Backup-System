package initiators;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import peer.FileInfo;
import peer.Peer;
import resources.Util;

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
			message += " pathname : "+fileinfo.getFilename() + "\n";
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
		String diskDIR = Util.PEERS_DIR +"Peer"+ peer.getID()+Util.CHUNKS_DIR;
		File[] files = peer.fileManager.getFilesFromDirectory(diskDIR);
		for(int i = 0; i < files.length; i++)
		{
			//print do nome + tamanho + replication degree?
			message += " name : "+files[i].getName() + "\n";
			message += " size : "+files[i].length() + "\n";
			message += "\n";
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
