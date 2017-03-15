package peer;

public class Chunk{
	
	private int chunkNo = -1;
	private String fileId;
	private byte[] data;
	//private int replicationDeg; ??
	
	public Chunk(String fileId, int chunkNo, byte[] data){
		this.setChunkNo(chunkNo);
		this.setFileId(fileId);
		this.setData(data);
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
