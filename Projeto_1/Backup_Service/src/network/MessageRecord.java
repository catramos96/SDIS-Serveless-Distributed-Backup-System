package network;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageRecord {

	private HashMap<String, HashMap<Integer, ArrayList<Integer>>> storedMessages = null;
	private HashMap<String, ArrayList<Integer>> chunkMessages = null;			
	private HashMap<String, ArrayList<Integer>> putchunkMessages = null;

	public MessageRecord(){
		storedMessages = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();	//apriori before store
		chunkMessages = new HashMap<String, ArrayList<Integer>>();						
		putchunkMessages = new HashMap<String, ArrayList<Integer>>();
	}

	/*
	 * ADDS
	 */

	public void addStoredMessage(String fileNo, int chunkNo, Integer sender){
		HashMap<Integer,ArrayList<Integer>> chunks = new HashMap<Integer,ArrayList<Integer>>();
		ArrayList<Integer> peers = new ArrayList<Integer>();

		if(storedMessages.containsKey(fileNo)){
			chunks = storedMessages.get(fileNo);
			if(chunks.containsKey(chunkNo)){
				peers = storedMessages.get(fileNo).get(chunkNo);
				if(peers.contains(sender))
					return;
			}
		}

		peers.add(sender);
		chunks.put(chunkNo, peers);
		storedMessages.put(fileNo, chunks);
	}

	public void addChunkMessage(String fileNo, int chunkNo){
		ArrayList<Integer> chunks = new ArrayList<Integer>();

		if(chunkMessages.containsKey(fileNo)){
			chunks = chunkMessages.get(fileNo);
			if(chunks.contains(chunkNo))
				return;
		}

		chunks.add(chunkNo);
		chunkMessages.put(fileNo, chunks);
	}

	public void startRecordingPutchunks(String fileNo, int chunkNo){
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		tmp.add(chunkNo);
		putchunkMessages.put(fileNo, tmp);
	}

	public void addPutchunkMessage(String fileNo, int chunkNo){

		if(putchunkMessages.containsKey(fileNo)){
			ArrayList<Integer> putchunks = putchunkMessages.get(fileNo);
			if(putchunks.contains(chunkNo))
				return;
			else
			{
				putchunks.add(chunkNo);
				putchunkMessages.put(fileNo, putchunks);
			}
		}
	}

	/*
	 * CONTAINS
	 */

	public boolean receivedStoredMessage(String fileNo,int chunkNo){
		if(storedMessages.containsKey(fileNo))
			if(storedMessages.get(fileNo).containsKey(chunkNo))
				return true;
		return false;
	}

	public boolean receivedChunkMessage(String fileNo, int chunkNo){
		if(chunkMessages.containsKey(fileNo))
			if(chunkMessages.get(fileNo).contains(chunkNo))
				return true;
		return false;
	}

	public boolean receivedPutchunkMessage(String fileNo, int chunkNo){
		if(putchunkMessages.containsKey(fileNo))
			if(putchunkMessages.get(fileNo).contains(chunkNo))
				return true;
		return false;
	}

	/*
	 * REMOVE RECORD
	 */

	public void removeStoredMessages(String fileNo, int chunkNo){
		if(receivedStoredMessage(fileNo,chunkNo)){
			HashMap<Integer,ArrayList<Integer>> chunks = storedMessages.get(fileNo);

			if(chunks.size() == 1)
				storedMessages.remove(fileNo);
			else
			{
				chunks.remove(chunkNo);
				storedMessages.put(fileNo,chunks);
			}
		}
	}

	public void removeChunkMessages(String fileNo, int chunkNo){
		if(receivedChunkMessage(fileNo, chunkNo)){
			ArrayList<Integer> chunks = chunkMessages.get(fileNo);
			if(chunks.size() == 1)
				chunkMessages.remove(fileNo);
			else
			{
				chunks.remove(chunkNo);
				chunkMessages.put(fileNo, chunks);
			}
		}
	}

	public void removePutChunkMessages(String fileNo, int chunkNo){
		if(putchunkMessages.containsKey(fileNo))
		{
			ArrayList<Integer> putchunks = putchunkMessages.get(fileNo);
			if(putchunks.contains(chunkNo))
			{
				putchunks.remove(chunkNo);
				putchunkMessages.put(fileNo, putchunks);
			}
		}
	}

	public void resetChunkMessages(String fileId) 
	{
		chunkMessages.remove(fileId);
	}

	/*
	 * GETS
	 */

	public synchronized ArrayList<Integer> getPeersWithChunk(String fileNo, int chunkNo){

		if(receivedStoredMessage(fileNo, chunkNo)){
			return storedMessages.get(fileNo).get(chunkNo);
		}

		return null;
	}
}
