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

/**
 * Serializable Class Record used to keep the peers state. It's loaded and saved when a peer initiate or exits.
 * @attribute serialVerionUID - Version of the Serialization.
 * @attribute storedConfirms - Record of peers who stored a certain chunk number of a certain file that the peer backed up(peers by chunks by file).
 * @attribute restoreConfirms - Record of chunks content restored by file (chunks content by file).
 * @attribute myChunks - Record of chunks by file identification that this peer stored (chunks by fileID) each chunk contains the atual replication
 * degree and the desired one.
 * @attribute totalMemory - Total memory in the disk of the peer for chunk backup.
 * @attribute remaingMemory - Remaing memory in the disk of the peer for chunk backup.
 *
 */
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

	/**
	 * Constructor of the Record
	 */
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
	 * Function that starts recording the stores for a certain file.
	 * @param fileNo - File identification
	 */
	public synchronized void startRecordStores(FileInfo file)
	{
		storedConfirms.put(file, new HashMap<Integer, ArrayList<Integer>>());
	}

	/**
	 * Function that records a new peer who stored a certain chunkNo of a certain fileId only if
	 * the record was started previously for that file.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 * @param peerNo - Number of the peer
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
	 * Function that gets the list of peers who stored a certain chunk of a certain file.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
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
	 * Function that deletes the entry in the storedConfirms of a file.
	 * @param fileId - File identification
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
	 * Function that removes a peer, if it exists, from the list of peers of the chunk of a certain file
	 * that was backed up by this peer (storedConfirms).
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param peerNo - Peer who previously stored the chunk, but not anymore
	 * @return True if the delete was a success, False otherwise
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
							peersList.remove((Integer)peerNo);

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
	 * Function that verifies if some file with the given fileId started its backup from this peer. 
	 * @param fileId - File identification
	 * @return FileInfo - Info of the File or Null in case there where any
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
	 * Function that verifies if come file with the given filename started its backup from this peer.
	 * @param filename - Name of the file
	 * @return FileInfo - Info of the File or Null in case there where any
	 */
	public synchronized FileInfo getBackupFileInfoByName(String filename){
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getFilename().equals(filename))
			{
				return fileinfo;
			}
		}
		return null;
	}

	/** 
	 * Function that verifies if some file with the given path started its backup from this peer. 
	 * @param path - Path of the file
	 * @return FileInfo - Info of the File or Null in case there where any
	 */ 
	public synchronized FileInfo getBackupFileInfoByPath(String path){
		for (FileInfo fileinfo : storedConfirms.keySet()) 
		{
			if(fileinfo.getPath().equals(path))
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
	 * Function that starts recording the restore for a certain file.
	 * @param fileNo - File identification
	 */
	public synchronized void startRecordRestores(FileInfo file)
	{
		restoreConfirms.put(file, new HashMap<Integer, byte[]>());
	}

	/**
	 * Function that adds a new restore to the restoreConfirms.
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @param chunkBody - Content of the chunks
	 * @return True if it added with success, False otherwise
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
	 * Function that gets a file information of a file in the restoreConfirms.
	 * @param fileId - File identification
	 * @return FileInfo - Information about the file or Null in case there where any
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
	 * Function that checks if a certain chunk of a file was already restored.
	 * @param fileId - File identification
	 * @param chunkNo - Chunk identification number
	 * @return True if it was already restored, False otherwise
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
	 * Function that checks if all the chunks of a certain file where restored.
	 * @param info - File information
	 * @return True in case of success, False otherwise
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
	 * Function that deletes the entry in the restoredConfirms for a given fileId
	 * @param fileId - File identification
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

	/**
	 * Function that checks if the peer has stored a certain chunk of a file.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 * @return True in case of success, False otherwise
	 */
	public synchronized boolean checkMyChunk(String fileID, int chunkNo){
		if(myChunks.containsKey(fileID))
		{
			for(Chunk c : myChunks.get(fileID)){
				if(c.getChunkNo() == chunkNo)
					return true;
			}
		}
		return false;
	}

	/**
	 * Function that adds a new chunk record to my chunks.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 * @param repDegree - Desired replication degree
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

	/**
	 * Function that sets the list of peers in a certain chunk of a file in the MyChunks.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 * @param peers - List of peers
	 */
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

	/**
	 * Function that adds a new peer to the list of peers in a chunk of a file in myChunks.
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 * @param peerNo - Peer identification
	 */
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

	/**
	 * Function that removes a peer of a chunk of a file in myChunks.
	 * @param fileNo
	 * @param chunkNo
	 * @param peerNo
	 * @return Returns the difference between the ActualReDeg and the RepDegDesired. 
	 * If the return is less than 0 then the repetition degree is bellow the desired
	 */
	public synchronized int remPeerWithMyChunk(String fileNo,int chunkNo,int peerNo){
		if(myChunks.containsKey(fileNo)){
			ArrayList<Chunk>chunks = myChunks.get(fileNo);
			for(Chunk c : chunks){
				if(c.getChunkNo() == chunkNo){
					c.removePeerWithChunk(peerNo);
					myChunks.put(fileNo, chunks);
					return (c.getAtualRepDeg() - c.getReplicationDeg());
				}
			}
		}
		return 0;
	}

	/**
	 * Function that checks if the peers has stored chunks of a certain file.
	 * @param fileNo - File identification
	 * @return True if it has stored, False otherwise
	 */
	public synchronized boolean myChunksBelongsToFile(String fileNo){
		return myChunks.containsKey(fileNo);
	}

	
	/**
	 * Function that deletes of MyChunks the entry associated to a certain file.
	 * @param fileId - File identification
	 */
	public synchronized void deleteMyChunksByFile(String fileId){
		if(myChunks.containsKey(fileId))
			myChunks.remove(fileId);
	}

	/**
	 * Function that removes a chunk of a file in myChunks
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 */
	public synchronized void removeFromMyChunks(String fileNo, int chunkNo){
		if(checkMyChunk(fileNo, chunkNo)){
			ArrayList<Chunk> chunks = myChunks.get(fileNo);
			for(Chunk c : chunks){
				if(c.getChunkNo() == chunkNo){
					chunks.remove(c);
					myChunks.put(fileNo, chunks);
					break;
				}
			}
			myChunks.put(fileNo, chunks);
		}
	}

	
	/**
	 * Function that get a chunk of myChunks
	 * @param fileNo - File identification
	 * @param chunkNo - Chunk identification number
	 * @return chunk
	 */
	public synchronized Chunk getMyChunk(String fileNo, int chunkNo){
		if(myChunks.containsKey(fileNo)){
			for(Chunk c : myChunks.get(fileNo))
				if(c.getChunkNo() == chunkNo)
					return c;
		}
		return null;
	}

	
	/**
	 * Function that gets a list with all the chunks the peer stored and that
	 * have a replication degree above the desired
	 * @return List of chunks
	 */
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
	
	/**
	 * Function that gets a list with all the chunks the peer stored and that
	 * have a replication degree bellow the desired
	 * @return List of chunks
	 */
	public synchronized ArrayList<Chunk> getChunksWithRepBellowDes(){
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
		
		for(Entry<String, ArrayList<Chunk>> array : myChunks.entrySet()){
			for(Chunk c : array.getValue()){
				if(c.getAtualRepDeg() < c.getReplicationDeg())
					chunks.add(c);
			}
		}
		
		return chunks;
	}
	
	/*
	 * GETS
	 */
	
	/**
	 * Function that gets all the chunks and their contents of a certain file of the restoresConfirms.
	 * @param info - File information
	 * @return HashMap<Integer,byte[] > where Integer is the chunkNo and the byte[] the content of the chunk
	 */
	public synchronized HashMap<Integer,byte[] > getRestores(FileInfo info) 
	{
		if(restoreConfirms.containsKey(info))
		{
			return restoreConfirms.get(info);
		}
		return null;
	}

	/**
	 * Function that returns the storedConfirms.
	 * @return HashMap<FileInfo, HashMap<Integer, ArrayList<Integer>>> where the hash FileInfo is the
	 * information of the file, the inner hash Integer is the chunk number and the arrayList is the
	 * list of peers.
	 */
	public HashMap<FileInfo, HashMap<Integer, ArrayList<Integer>>> getStored() {
		return storedConfirms;
	}

	
	/**
	 * Function that return the myChunks.
	 * @return HashMap<String, ArrayList<Chunk>> where the hash is the file identification
	 * and the array list the list of chunks.
	 */
	public HashMap<String, ArrayList<Chunk>> getMyChunks() {
		return myChunks;
	}

}
