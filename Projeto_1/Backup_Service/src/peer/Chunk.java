package peer;

public class Chunk {
	
	private int chunkNo = -1;
	private int fileId = -1;
	//private int replicationDeg; ??
	
	Chunk(int chunkNo, int fileId){
		this.chunkNo = chunkNo;
		this.fileId = fileId;
	}
}
