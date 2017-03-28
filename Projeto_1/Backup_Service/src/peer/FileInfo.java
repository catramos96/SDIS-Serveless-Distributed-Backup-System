package peer;

import java.io.Serializable;

public class FileInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String fileId;
	private String filename;
	private int numChunks;
	
	public FileInfo(String fileId, String filename, int numChunks)
	{
		this.fileId = fileId;
		this.numChunks = numChunks;
		String[] parts = filename.split("/+");
		
		this.filename = parts[parts.length-1];
	}

	public int getNumChunks() {
		return numChunks;
	}

	public void setNumChunks(int numChunks) {
		this.numChunks = numChunks;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public int getReplicationDeg() {
		return 0;
	}

}
