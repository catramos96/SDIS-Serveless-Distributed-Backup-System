package peer;

import java.io.Serializable;

/**
 * Class responsible for saving all information about files.
 * @attribute long serialVersionUID - serial id 
 * @attribute String fileId - file id calculated
 * @attribute String filename - name of the file
 * @attribute String path - path of the file
 * @attribute int numChunks - number of chunks create for this file
 * @attribute int repDegree - desired replication degree for this file
 */
public class FileInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String fileId = null;
	private String filename = null;
	private String path = null;
	private int numChunks = -1;
	private int repDegree = -1;
	
	/**
	 * Constructor
	 * @param fileId
	 * @param repDegree
	 */
	public FileInfo(String fileId, int repDegree)
	{
		this.fileId = fileId;
		this.repDegree = repDegree;
	}
	
	/**
	 * Constructor
	 * @param fileId
	 * @param filename
	 * @param numChunks
	 * @param repDegree
	 */
	public FileInfo(String fileId, String filename, int numChunks,int repDegree)
	{
		this.fileId = fileId;
		this.numChunks = numChunks;
		this.path = filename;
		
		String[] parts = filename.split("/+");
		
		this.filename = parts[parts.length-1];
		this.repDegree = repDegree;
	}

	/*
	 * Gets and sets
	 */
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
