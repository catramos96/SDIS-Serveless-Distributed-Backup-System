package peer;

import java.io.Serializable;

public class FileInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String fileId = null;
	private String filename = null;
	private String path = null;
	private int numChunks = -1;
	private int repDegree = -1;
	
	public FileInfo(String fileId, int repDegree)
	{
		this.fileId = fileId;
		this.repDegree = repDegree;
	}
	
	public FileInfo(String fileId, String filename, int numChunks,int repDegree)
	{
		this.fileId = fileId;
		this.numChunks = numChunks;
		this.path = filename;
		
		String[] parts = filename.split("/+");
		
		this.filename = parts[parts.length-1];
		this.repDegree = repDegree;
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
	
	public int getRepDegree() {
		return repDegree;
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
		return repDegree;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
