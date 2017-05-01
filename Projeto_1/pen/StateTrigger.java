package initiators;

import java.util.ArrayList;
import java.util.HashMap;

import peer.Chunk;
import peer.FileInfo;
import peer.Peer;

/**
 * Peer initiator response to client request for state.
 * @attribute Peer peer - initiator peer
 * @attribute String message - response to client
 */
public class StateTrigger extends Thread
{
	private Peer peer;
	private String message = null;

	/**
	 * Constructor
	 * @param peer
	 */
	public StateTrigger(Peer peer)
	{
		this.peer = peer;
	}

	/**
	 * Thread execution
	 */
	@Override
	public void run()
	{
		message = "\n\nFiles whose backup was initiated : \n\n";

		HashMap<FileInfo, HashMap<Integer, ArrayList<Integer>>> storedConfirms = peer.getRecord().getStored();

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
		HashMap<String, ArrayList<Chunk>> files = peer.getRecord().getMyChunks();
		for(String s : files.keySet())
		{			
			ArrayList<Chunk> chunks = files.get(s);
			for(int i = 0; i < chunks.size(); i++)
			{
				//print do nome + tamanho + replication degree
				byte[] data = peer.getFileManager().getChunkContent(s,i);
				if(data != null)
				{
					int size = data.length;
					message += " name : "+chunks.get(i).getChunkNo()+chunks.get(i).getFileId() + " \n";
					message += " size : "+size + " KB\n";
					message += " perceived replication degree : "+chunks.get(i).getAtualRepDeg()+"\n";
					message += "\n";
				}
			}

		}

		//peer storage capacity
		message += "Peer stored capacity :\n\n";
		message += " Maximum amount : "+peer.getFileManager().getTotalSpace()+"\n";
		message += " Storage amount : "+peer.getFileManager().getRemainingSpace()+"\n";	
	}

	/**
	 * Return the feedback message to client
	 * @return
	 */
	public String response() {
		return message;
	}
}
