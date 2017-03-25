package peer;

public class FileInfo 
{
	private String fileId;
	private String filename;
	private int numChunks;
	
	public FileInfo(String fileId, String filename, int numChunks)
	{
		this.fileId = fileId;
		this.filename = filename;
		this.numChunks = numChunks;
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

}
