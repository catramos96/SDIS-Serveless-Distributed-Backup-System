package peer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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
	 * STORE
	 */

	/**
	 * Put the fileId at the information structure.
	 * This means that our multicastRecord it's waiting for chunks from this specific file.
	 * @param fileNo
	 */
	public synchronized void startRecordStores(FileInfo file)
	{
		storedConfirms.put(file, new HashMap<Integer, ArrayList<Integer>>());
	}

	/**
	 * Save chunks from 'fileNo' if 'fileNo' was previously initiated
	 * @param fileNo
	 * @param chunkNo
	 * @param peerNo
	 */
	public synchronized void recordStoreChunks(String fileID, int chunkNo, int peerNo)
	{
		ArrayList<Integer> peers = new ArrayList<Integer>();

		//chunk from 'fileNo' match some file from the hashMap
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileID))
			{
				HashMap<Integer,ArrayList<Integer>> tmp = storedConfirms.get(fileinfo);

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
					storedConfirms.put(fileinfo, tmp);	
				}
				return;
			}
		}
	}

	/**
	 * Verifies if chunkNo from fileNo is stored
	 * @param fileNo
	 * @param chunkNo
	 * @return List of peers who stored chunkNo of fileNo
	 */
	public synchronized ArrayList<Integer> checkStored(String fileId, int chunkNo)
	{
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
			{
				HashMap<Integer,ArrayList<Integer>> tmp = storedConfirms.get(fileinfo);

				if(tmp.containsKey(chunkNo))
				{
					return tmp.get(chunkNo);
				}
			}
		}
		return null;
	}
	
	public synchronized void deleteStoreEntry(String fileId) { 
		
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
			{
				storedConfirms.remove(fileinfo);
				return;
			}
		}
	}
	
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

	public synchronized String getFilename(String fileId){
		
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
			{
				return fileinfo.getFilename();
			}
		}
		
		return null;
	}
	
	public synchronized int getReplicationDegree(String fileId){
		
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
			{
				return fileinfo.getRepDegree();
			}
		}
		return 0;
	}
	
	/*
	 * RESTORE
	 */

	/**
	 * 
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
	public synchronized boolean recordRestoreChunks(String fileID, int chunkNo, byte[] chunkBody)
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

	public synchronized boolean checkRestore(String fileId) {

		//finds file at restored file of this initiator peer
		for (FileInfo fileinfo : restoreConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
				return true;
		}
		return false;
	}
	
	public synchronized boolean checkChunkRestored(String fileId,int chunkNo){
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

	public synchronized boolean allRestored(FileInfo info)
	{
		if(restoreConfirms.containsKey(info))
		{
			int a = restoreConfirms.get(info).size();
			int b = info.getNumChunks();

			return (a==b);
		}
		return false;
	}

	public void deleteRestoreEntry(String fileId) { 
	    for (FileInfo fileinfo : restoreConfirms.keySet())  
	    { 
	      if(fileinfo.getFileId().equals(fileId)) 
	      { 
	        restoreConfirms.remove(fileinfo); 
	        return; 
	      } 
	    } 
	  } 
	
	/*
	 * MY CHUNKS
	 */
	
	public synchronized void addToMyChunks(String fileNo, int chunkNo, int repDegree){
		
		Chunk c = new Chunk(chunkNo,repDegree);
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		
		if(myChunks.containsKey(fileNo)){
			chunks = myChunks.get(fileNo);
			if(chunks.contains(c))
				return;
		}
		chunks.add(c);
		myChunks.put(fileNo,chunks);
	}
	
	public synchronized void addRepToChunk(String fileNo, int chunkNo, int newRepDegree){
		if(myChunks.containsKey(fileNo)){
			ArrayList<Chunk>chunks = myChunks.get(fileNo);
			for(Chunk c : chunks){
				if(c.getChunkNo() == chunkNo){
					c.setAtualRepDeg(newRepDegree);
					/*
					 * Se baixar executar o backupChunkProt mas nao pode ser executado aqui
					 */
				}
			}
		}
	}
	
	/*
	 * Gets e sets
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
	
	 /** 
	   * Verifies if some file with this filename started its backup from this peer. 
	   * @param filename 
	   * @return 
	   */ 
	  public FileInfo fileBackup(String filename)  
	  { 
	    for (FileInfo fileinfo : storedConfirms.keySet())  
	    { 
	      if(fileinfo.getPath().equals(filename)) 
	      { 
	        return fileinfo; 
	      } 
	    } 
	    return null; 
	  } 
}
