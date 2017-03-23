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
	
	public MulticastRecord(){
		storedConfirms = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
		/*restoreConfirms = new HashMap<int[], ArrayList<Integer>>();
		putChunks = new HashMap<int[], ArrayList<Integer>>();
		restoreChunks = new HashMap<int[], ArrayList<Integer>>();*/
	};
	
	
	public void recordStoreChunk(String fileNo, int chunkNo, int peerNo){
		
		ArrayList<Integer> peers = new ArrayList<Integer>();
		
		if(storedConfirms.containsKey(fileNo)){
			HashMap<Integer,ArrayList<Integer>> tmp = storedConfirms.get(fileNo);
			if(tmp.containsKey(chunkNo))
				peers = tmp.get(chunkNo);
		}
		
		if(!peers.contains(peerNo)){
			peers.add(peerNo);
			HashMap<Integer, ArrayList<Integer>> tmp = new HashMap<Integer, ArrayList<Integer>>();
			tmp.put(chunkNo, peers);
			storedConfirms.put(fileNo, tmp);
		}
	}
	
	public int checkStored(String fileNo, int chunkNo){
		int size = 0;
		if(storedConfirms.containsKey(fileNo)){
			HashMap<Integer,ArrayList<Integer>> tmp = storedConfirms.get(fileNo);
			if(tmp.containsKey(chunkNo))
				size = tmp.get(chunkNo).size();
		}
		return size;
	}
}
