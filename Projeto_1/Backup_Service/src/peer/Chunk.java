package peer;

public class Chunk {
	
	private int chunkNo = -1;
	private String fileId;
	private byte[] data;
	//private int replicationDeg; ??
	
	public Chunk(String fileId, int chunkNo, byte[] data){
		this.chunkNo = chunkNo;
		this.fileId = fileId;
		this.data = data;
	}

}
