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
	
	public synchronized void startRecordStores(String fileNo){
		storedConfirms.put(fileNo, new HashMap<Integer, ArrayList<Integer>>());
	}
	
	public synchronized void recordStoreChunks(String fileNo, int chunkNo, int peerNo){
		
		ArrayList<Integer> peers = new ArrayList<Integer>();
		
		if(storedConfirms.containsKey(fileNo)){
			HashMap<Integer,ArrayList<Integer>> tmp = storedConfirms.get(fileNo);
			
			if(tmp.containsKey(chunkNo)){	//chunkNo already inserted
				System.out.println("HAS ENTRY CHUNKNO");
				peers = tmp.get(chunkNo);
			}
			
			if(!peers.contains(peerNo)){	//peer didn't store the chunk yet
				peers.add(peerNo);
				tmp.put(chunkNo, peers);
				storedConfirms.put(fileNo, tmp);	
			}
		}
	}
	
	public synchronized ArrayList<Integer> checkStored(String fileNo, int chunkNo){
		if(storedConfirms.containsKey(fileNo)){
			HashMap<Integer,ArrayList<Integer>> tmp = storedConfirms.get(fileNo);
			if(tmp.containsKey(chunkNo)){
				System.out.println("SENDER Chunk - " + chunkNo);
				return tmp.get(chunkNo);
			}
		}
		return null;
	}
}
