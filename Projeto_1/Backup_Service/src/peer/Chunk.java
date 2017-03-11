package peer;

public class Chunk {
	
	private int chunkNo;
	private int fileId;
	//private int replicationDeg; ??
	
	Chunk(int chunkNo, int fileId){
		this.chunkNo = chunkNo;
		this.fileId = fileId;
	}
}
