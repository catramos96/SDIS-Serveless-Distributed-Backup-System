package network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import peer.FileInfo;

public class MulticastRecord implements Serializable {
	
	private static final long serialVersionUID = 1L;
	/*
	 * HashMap<String, HashMap<Integer, ArrayList<Integer>>>
	 * String -> FileNo
	 * Integer -> ChunkNo
	 * ArrayList -> PeersID
	 */
	private volatile HashMap<FileInfo, HashMap<Integer, ArrayList<Integer>>> storedConfirms = null;
	private volatile HashMap<FileInfo, HashMap<Integer, byte[] >> restoreConfirms = null;

	/*
	private volatile HashMap<int[], ArrayList<Integer>> putChunks = null;
	private volatile HashMap<int[], ArrayList<Integer>> restoreChunks = null;*/

	public MulticastRecord()
	{
		storedConfirms = new HashMap<FileInfo, HashMap<Integer, ArrayList<Integer>>>();
		restoreConfirms = new HashMap<FileInfo, HashMap<Integer, byte[] >>();

		/*putChunks = new HashMap<int[], ArrayList<Integer>>();
		restoreChunks = new HashMap<int[], ArrayList<Integer>>();*/
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

	public boolean checkRestore(String fileId) {

		//finds file at restored file of this initiator peer
		for (FileInfo fileinfo : restoreConfirms.keySet()) 
		{
			if(fileinfo.getFileId().equals(fileId))
				return true;
		}
		return false;
	}

	public boolean allRestored(FileInfo info)
	{
		if(restoreConfirms.containsKey(info))
		{
			int a = restoreConfirms.get(info).size();
			int b = info.getNumChunks();
		
			return (a==b);
		}
		return false;
	}

	public HashMap<Integer,byte[] > getRestores(FileInfo info) 
	{
		if(restoreConfirms.containsKey(info))
		{
			return restoreConfirms.get(info);
		}
		return null;
	}
}
