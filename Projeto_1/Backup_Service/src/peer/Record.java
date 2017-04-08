package peer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import resources.Util;

public class Record implements Serializable {

	private static final long serialVersionUID = 1L;
	/*
	 * HashMap<String, HashMap<Integer, ArrayList<Integer>>>
	 * String -> FileNo
	 * Integer -> ChunkNo
	 * ArrayList -> PeersID
	 */
	private HashMap<FileInfo, HashMap<Integer, ArrayList<Integer>>> storedConfirms = null;				//from my backup files
	private HashMap<FileInfo, HashMap<Integer, byte[] >> restoreConfirms = null;						//for my restored files
	private HashMap<String, ArrayList<Chunk>> myChunks = null;

	public int totalMemory = Util.DISK_SPACE_DEFAULT;	//Just for loads and saves
	public int remaingMemory = Util.DISK_SPACE_DEFAULT;

	public Record()
	{
		storedConfirms = new HashMap<FileInfo, HashMap<Integer, ArrayList<Integer>>>();
		restoreConfirms = new HashMap<FileInfo, HashMap<Integer, byte[] >>();
		myChunks = new HashMap<String,  ArrayList<Chunk>>();
	};

	/*
	 * BACKUP STORES
	 */

	/**
	 * Put the fileId at the information structure.
	 * This means that our multicastRecord it's waiting for chunks from this specific file.
	 * @param fileNo
	 */
	public synchronized void startRecordStores(FileInfo file)
	{
		System.out.println("aqui");
		storedConfirms.put(file, new HashMap<Integer, ArrayList<Integer>>());
	}

	/**
	 * Save chunks from 'fileId' if 'fileId' was previously initiated
	 * @param fileNo
	 * @param chunkNo
	 * @param peerNo
	 */
	public synchronized void recordStoredChunk(String fileID, int chunkNo, int peerNo)
	{
		//chunk from 'fileID' match some file from the hashMap
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileID))
			{
				HashMap<Integer,ArrayList<Integer>> chunks = storedConfirms.get(fileinfo);
				ArrayList<Integer> peers = new ArrayList<Integer>();

				//other peers already stored this chunk
				if(chunks.containsKey(chunkNo))
				{	
					peers = chunks.get(chunkNo);
				}

				//peerNo doesn't belong to the list of peers with chunkNo stored
				if(!peers.contains(peerNo))
				{	
					peers.add(peerNo);
					chunks.put(chunkNo, peers);
					storedConfirms.put(fileinfo, chunks);	
				}
				return;
			}
		}
	}

	/**
	 * Verifies if chunkNo from fileId is stored
	 * @param fileNo
	 * @param chunkNo
	 * @return List of peers who stored chunkNo of fileNo
	 */
	public synchronized ArrayList<Integer> checkStoredChunk(String fileId, int chunkNo)
	{
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
			{
				HashMap<Integer,ArrayList<Integer>> chunks = storedConfirms.get(fileinfo);

				if(chunks != null && chunks.containsKey(chunkNo))
				{
					return chunks.get(chunkNo);
				}
			}
		}
		return null;
	}

	/**
	 * Delete some file from backup history
	 * @param fileId
	 */
	public synchronized void deleteStoredFile(String fileId) { 

		for(Iterator< Map.Entry< FileInfo, HashMap< Integer, ArrayList<Integer> >>> it = storedConfirms.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry< FileInfo, HashMap< Integer, ArrayList<Integer> >> entry = it.next();
			if(entry.getKey().getFileId().equals(fileId)) {
				it.remove();
			}
		}
	}

	/**
	 * ?
	 * @param fileId
	 * @param chunkNo
	 * @param peerNo
	 * @return
	 */
	public synchronized boolean deleteStored(String fileId, int chunkNo,Integer peerNo){
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
			{
				//chunks of file
				HashMap<Integer, ArrayList<Integer>> chunkPeersStored = storedConfirms.get(fileinfo);

				if(chunkPeersStored.containsKey(chunkNo)){
					//peers of file
					ArrayList<Integer> peersList = chunkPeersStored.get(chunkNo);

					//remove peer from list of stored
					if(peersList.contains(peerNo)){

						//No more peers for chunk ?
						if(peersList.size() == 1){		

							//Remove chunk
							if(chunkPeersStored.containsKey(chunkNo))
								chunkPeersStored.remove(chunkNo);
						}
						else
							peersList.remove(peerNo);

					}		
					//update chunkNo peersList
					chunkPeersStored.put(chunkNo, peersList);

					//update on hashmap
					storedConfirms.put(fileinfo,chunkPeersStored);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	/** 
	 * Verifies if some file with this fileId started its backup from this peer. 
	 * @param filename 
	 * @return 
	 */ 
	public synchronized FileInfo getBackupFileInfoById(String fileId){
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
			{
				return fileinfo;
			}
		}
		return null;
	}

	/** 
	 * Verifies if some file with this filename started its backup from this peer. 
	 * @param filename 
	 * @return 
	 */ 
	public synchronized FileInfo getBackupFileInfoByPath(String filename){
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getPath().equals(filename))
			{
				return fileinfo;
			}
		}
		return null;
	}

	/*
	 * RESTORE
	 */

	/**
	 * Put the fileinfo at the information structure.
	 * This means that our multicastRecord it's waiting for chunks from this specific file.
	 * @param file
	 */
	public synchronized void startRecordRestores(FileInfo file)
	{
		restoreConfirms.put(file, new HashMap<Integer, byte[]>());
	}

	/**
	 * 
	 * @param chunkNo
	 * @param chunkBody
	 * @return
	 */
	public synchronized boolean recordRestoredChunk(String fileID, int chunkNo, byte[] chunkBody)
	{
		//finds file at restored file of this initiator peer
		for (FileInfo fileinfo : restoreConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileID))
			{
				HashMap<Integer,byte[] > tmp = restoreConfirms.get(fileinfo);

				//chunk already restored
				if(tmp.containsKey(chunkNo))
				{	
					return false;
				}

				tmp.put(new Integer(chunkNo), chunkBody);
				restoreConfirms.put(fileinfo, tmp);
				return true;
			}
		}
		return false;	
	}

	/**
	 * 
	 * @param fileId
	 * @return
	 */
	public synchronized FileInfo getRestoredFileInfoById(String fileId) {

		//finds file at restored file of this initiator peer
		for (FileInfo fileinfo : restoreConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
				return fileinfo;
		}
		return null;
	}

	/**
	 * 
	 * @param fileId
	 * @param chunkNo
	 * @return
	 */
	public synchronized boolean checkRestoredChunk(String fileId,int chunkNo)
	{
		for (FileInfo fileinfo : restoreConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId)){
				HashMap<Integer,byte[] > chunks = restoreConfirms.get(fileinfo);
				if(chunks.containsKey(chunkNo))
					return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param info
	 * @return
	 */
	public synchronized boolean checkAllChunksRestored(FileInfo info)
	{
		if(restoreConfirms.containsKey(info))
		{
			int a = restoreConfirms.get(info).size();
			int b = info.getNumChunks();

			return (a==b);
		}
		return false;
	}

	/**
	 * 
	 * @param fileId
	 */
	public void deleteRestoredFile(String fileId) { 
		
		for(Iterator< Map.Entry<FileInfo, HashMap<Integer, byte[]>> > it = restoreConfirms.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry< FileInfo, HashMap<Integer, byte[]> > entry = it.next();
			if(entry.getKey().getFileId().equals(fileId)) {
				it.remove();
			}
		}
	} 

	/*
	 * MY CHUNKS
	 */

	public synchronized void addToMyChunks(String fileNo, int chunkNo, int repDegree){

		Chunk c = new Chunk(fileNo,chunkNo,repDegree);
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();

		if(myChunks.containsKey(fileNo)){
			chunks = myChunks.get(fileNo);
			if(chunks.contains(c))
				return;
		}
		chunks.add(c);
		myChunks.put(fileNo,chunks);
	}

	public synchronized void setPeersOnMyChunk(String fileNo, int chunkNo, ArrayList<Integer> peers){
		if(peers == null)
			return;

		if(myChunks.containsKey(fileNo)){
			ArrayList<Chunk>chunks = myChunks.get(fileNo);
			for(Chunk c : chunks){
				if(c.getChunkNo() == chunkNo){
					for(int peerNo : peers)
					{
						c.addPeerWithChunk(peerNo);
					}
				}
			}
		}
	}

	public synchronized void addPeerOnMyChunk(String fileNo, int chunkNo, int peerNo){
		if(myChunks.containsKey(fileNo)){
			ArrayList<Chunk>chunks = myChunks.get(fileNo);
			for(Chunk c : chunks){
				if(c.getChunkNo() == chunkNo){
					c.addPeerWithChunk(peerNo);
				}
			}
		}
	}

	/*
	 * Returns the difference between the ActualReDeg and the RepDegDesired
	 * If the return is less than 0 then the repetition degree is bellow the desired
	 */
	public synchronized int remPeerWithMyChunk(String fileNo,int chunkNo,int peerNo){
		if(myChunks.containsKey(fileNo)){
			ArrayList<Chunk>chunks = myChunks.get(fileNo);
			for(Chunk c : chunks){
				if(c.getChunkNo() == chunkNo){
					c.removePeerWithChunk(peerNo);
					return (c.getAtualRepDeg() - c.getReplicationDeg());
				}
			}
		}
		return 0;
	}

	public synchronized boolean hasOnMyChunk(String fileNo, int chunkNo){
		if(myChunks.containsKey(fileNo))
		{
			for(Chunk c : myChunks.get(fileNo)){
				if(c.getChunkNo() == chunkNo)
					return true;
			}
		}
		return false;
	}

	public synchronized boolean myChunksBelongsToFile(String fileNo){
		return myChunks.containsKey(fileNo);
	}

	public synchronized void removeFromMyChunks(String fileNo){
		if(myChunks.containsKey(fileNo))
			myChunks.remove(fileNo);
	}

	public synchronized void removeFromMyChunks(String fileNo, int chunkNo){
		if(hasOnMyChunk(fileNo, chunkNo)){
			ArrayList<Chunk> chunks = myChunks.get(fileNo);
			chunks.remove(chunkNo);
			myChunks.put(fileNo, chunks);
		}
	}

	public synchronized Chunk getMyChunk(String fileNo, int chunkNo){
		if(myChunks.containsKey(fileNo)){
			for(Chunk c : myChunks.get(fileNo))
				if(c.getChunkNo() == chunkNo)
					return c;
		}
		return null;
	}

	public synchronized ArrayList<Chunk> getChunksWithRepAboveDes(){
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		
		for(Entry<String, ArrayList<Chunk>> array : myChunks.entrySet()){
			for(Chunk c : array.getValue()){
				if(c.getAtualRepDeg() > c.getReplicationDeg())
					chunks.add(c);
			}
		}
		
		//Order by RepDegree Desc
		Collections.sort(chunks, new Comparator<Chunk>() {
		    public int compare(Chunk c1, Chunk c2) {
		        return c2.getAtualRepDeg() - c1.getAtualRepDeg();
		    }
		});
		
		return chunks;
	}
	
	/*
	 * Gets
	 */
 	public synchronized HashMap<Integer,byte[] > getRestores(FileInfo info) 
	{
		if(restoreConfirms.containsKey(info))
		{
			return restoreConfirms.get(info);
		}
		return null;
	}

	public HashMap<FileInfo, HashMap<Integer, ArrayList<Integer>>> getStored() {
		return storedConfirms;
	}

	public HashMap<String, ArrayList<Chunk>> getMyChunks() {
		return myChunks;
	}
	
}
