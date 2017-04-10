package peer;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class responsible for representing a chunk.
 * Each chunk has a fileId and a chunk number associated.
 * Each chunk has also its content (data) and the replication degree actual and the desired.
 * This class is serialized.
 * 
 * @attribute long serialVersionUID - serial id
 * @attribute int chunkNo - chunk number
 * @attribute String fileId - fileId associated
 * @attribute byte[] data - information saved at the disk
 * @attribute int replicationDeg - replication degree desired
 * @attribute ArrayList<Integer> peers - List of peers with the chunk associated (count = actual replication degree)
 */
public class Chunk implements Serializable {

	private static final long serialVersionUID = 1L;
	private int chunkNo = -1;
	private String fileId = null;
	private byte[] data = null;
	private int replicationDeg = 0;	//Desired
	private ArrayList<Integer> peers = new ArrayList<Integer>();
	
	/**
	 * Constructor
	 * @param fileNo
	 * @param chunkNo
	 * @param replicationDeg
	 */
	public Chunk(String fileNo, int chunkNo, int replicationDeg){
		this.fileId = fileNo;
		this.chunkNo = chunkNo;
		this.replicationDeg = replicationDeg;
	}
	
	/**
	 * Constructor
	 * @param fileId
	 * @param chunkNo
	 * @param data
	 */
	public Chunk(String fileId, int chunkNo, byte[] data){
		this.setChunkNo(chunkNo);
		this.setFileId(fileId);
		this.setData(data);
	}
	
	/**
	 * If a new peer has stored this chunk, the peerId will be saved at peers ArrayList of this chunks
	 * @param peerNo
	 */
	public synchronized void addPeerWithChunk(int peerNo){
		if(!peers.contains(peerNo))
			peers.add(peerNo);
	}
	
	/**
	 * If a peer has deleted this chunk from its file system, the peerId will be deleted of the 'peers' ArrayList
	 * @param peerNo
	 */
	public synchronized void removePeerWithChunk(int peerNo){
		if(peers.contains(peerNo))
			peers.remove((Integer)peerNo);
	}
	

	/**
	 * The actual replication degree correspond to the size of the 'peers' ArrayList.
	 * @return
	 */
	public synchronized int getAtualRepDeg(){
		return peers.size();
	}
	
	/*
	 * Gets and sets
	 */
	
	public void setReplicationDeg(int rep){
		replicationDeg = rep;
	}
	
	public int getReplicationDeg(){
		return replicationDeg;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
