package network;

import java.util.ArrayList;
import java.util.HashMap;

public class MulticastRecord {
	
	/*
	 * HashMap<String, HashMap<Integer, ArrayList<Integer>>>
	 * String -> FileNo
	 * Integer -> ChunkNo
	 * ArrayList -> PeersID
	 */
	private volatile HashMap<String, HashMap<Integer, ArrayList<Integer>>> storedConfirms = null;
	
	/*private volatile HashMap<int[], ArrayList<Integer>> restoreConfirms = null;
	private volatile HashMap<int[], ArrayList<Integer>> putChunks = null;
	private volatile HashMap<int[], ArrayList<Integer>> restoreChunks = null;*/
	
	public MulticastRecord()
	{
		storedConfirms = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
		/*restoreConfirms = new HashMap<int[], ArrayList<Integer>>();
		putChunks = new HashMap<int[], ArrayList<Integer>>();
		restoreChunks = new HashMap<int[], ArrayList<Integer>>();*/
	};
	
	/**
	 * Put the fileId at the information structure.
	 * This means that our multicastRecord it's waiting for chunks from this specific file.
	 * @param fileNo
	 */
	public synchronized void startRecordStores(String fileNo){
		storedConfirms.put(fileNo, new HashMap<Integer, ArrayList<Integer>>());
	}
	
	/**
	 * Save chunks from 'fileNo' if 'fileNo' was previously initiated
	 * @param fileNo
	 * @param chunkNo
	 * @param peerNo
	 */
	public synchronized void recordStoreChunks(String fileNo, int chunkNo, int peerNo){
		
		ArrayList<Integer> peers = new ArrayList<Integer>();
		
		//chunk from 'fileNo' match some file from the hashMap
		if(storedConfirms.containsKey(fileNo))
		{
			HashMap<Integer,ArrayList<Integer>> tmp = storedConfirms.get(fileNo);
			
			//other peers already stored this chunkNo
			if(tmp.containsKey(chunkNo))
			{	
				peers = tmp.get(chunkNo);
			}
			
			//peerNo doesn't belong to the list of peers with chunkNo stored
			if(!peers.contains(peerNo))
			{	
				peers.add(peerNo);
				tmp.put(chunkNo, peers);
				storedConfirms.put(fileNo, tmp);	
			}
		}
	}
	
	/**
	 * Verifies if chunkNo from fileNo is stored
	 * @param fileNo
	 * @param chunkNo
	 * @return List of peers who stored chunkNo of fileNo
	 */
	public synchronized ArrayList<Integer> checkStored(String fileNo, int chunkNo){
		
		if(storedConfirms.containsKey(fileNo))
		{
			HashMap<Integer,ArrayList<Integer>> tmp = storedConfirms.get(fileNo);
			
			if(tmp.containsKey(chunkNo))
			{
				//System.out.println("SENDER Chunk - " + chunkNo);
				return tmp.get(chunkNo);
			}
		}
		return null;
	}
}
