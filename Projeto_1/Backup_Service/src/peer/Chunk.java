package peer;

import java.io.Serializable;
import java.util.ArrayList;

public class Chunk implements Serializable{

	private static final long serialVersionUID = 1L;
	private int chunkNo = -1;
	private String fileId = null;
	private byte[] data = null;
	private int replicationDeg = 0;	//Desired
	private ArrayList<Integer> peers = new ArrayList<Integer>();
	
	public Chunk(String fileNo, int chunkNo, int replicationDeg){
		this.fileId = fileNo;
		this.chunkNo = chunkNo;
		this.replicationDeg = replicationDeg;
	}
	
	public Chunk(String fileId, int chunkNo, byte[] data){
		this.setChunkNo(chunkNo);
		this.setFileId(fileId);
		this.setData(data);
	}
	
	public synchronized void addPeerWithChunk(int peerNo){
		if(!peers.contains(peerNo))
		{
			peers.add(peerNo);
		}
	}
	
	public synchronized void removePeerWithChunk(int peerNo){
		if(peers.contains(peerNo))
			peers.remove((Integer)peerNo);
	}
	
	public synchronized int getAtualRepDeg(){
		return peers.size();
	}
	
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
