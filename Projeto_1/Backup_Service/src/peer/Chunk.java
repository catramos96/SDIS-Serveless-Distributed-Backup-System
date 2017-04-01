package peer;

public class Chunk{
	
	private int chunkNo = -1;
	private String fileId = null;
	private byte[] data = null;
	private int replicationDeg = 0;	//Desired
	private int atualRepDeg = 0;
	
	public Chunk(int chunkNo, int replicationDeg){
		this.chunkNo = chunkNo;
		this.replicationDeg = replicationDeg;
	}
	
	public Chunk(String fileId, int chunkNo, byte[] data){
		this.setChunkNo(chunkNo);
		this.setFileId(fileId);
		this.setData(data);
	}
	
	public void setAtualRepDeg(int r){
		atualRepDeg = r;
	}
	
	public int getAtualRepDeg(){
		return atualRepDeg;
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
