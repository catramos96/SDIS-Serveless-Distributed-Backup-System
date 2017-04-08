package network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import resources.Util;

public class Message 
{
	private Util.MessageType type = null;
	private char[] version;
	private int senderId = -1;
	private String fileId = null;
	private int chunkNo = -1;
	private int replicationDeg = -1;
	
	private byte[] body = null;
	
	private static final char CR = 0xD;
	private static final char LF = 0xA;
	public static final String LINE_SEPARATOR = "" + CR + LF;
	
	/**
	 * <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
	 * 
	 * PUTCHUNK	<Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> 	<CRLF><CRLF>	<Body>
	 * STORED 	<Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>
	 * GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>
	 * CHUNK 	<Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>	<Body>
	 * DELETE 	<Version> <SenderId> <FileId> 								<CRLF><CRLF>
	 * REMOVED 	<Version> <SenderId> <FileId> <ChunkNo> 					<CRLF><CRLF>
	 */
	
	public Message(Util.MessageType type, char[] version, int senderId, String fileId, int chunkNo, int ReplicationDeg, byte[] body)
	{
		if(!type.name().equals("PUTCHUNK"))
			System.out.println("Wrong Constructor putchunk");
		this.type = type;
		this.version = version;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDeg = ReplicationDeg;
		this.body = body;
	}
	
	public Message(Util.MessageType type, char[] version, int senderId, String fileId, int chunkNo)
	{
		if(!(type.name().equals("STORED")|| type.name().equals("GETCHUNK") || type.name().equals("REMOVED")) )
			System.out.println("Wrong Constructor stored/getchunk/removed");
		this.type = type;
		this.version = version;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
	}
	
	public Message(Util.MessageType type, char[] version, int senderId, String fileId, int chunkNo, byte[] body)
	{
		if(!type.name().equals("CHUNK"))
			System.out.println("Wrong Constructor chunk");
		else
		{
			this.type = type;
			this.version = version;
			this.senderId = senderId;
			this.fileId = fileId;
			this.chunkNo = chunkNo;
			this.body = body;
		}
	}

	public Message(Util.MessageType type, char[] version, int senderId, String fileId) {
		if(!type.name().equals("DELETE"))
			System.out.println("Wrong Constructor delete");
		else
		{
			this.type = type;
			this.version = version;
			this.senderId = senderId;
			this.fileId = fileId;
		}
	}
	

	/**
	 * Create new message
	 * @return
	 */
	public byte[] buildMessage() {
		
		String content = type.name() + " " + version[0]+version[1]+version[2] + " " + senderId + " " + fileId + " ";
		
		if(type.compareTo(Util.MessageType.DELETE) != 0)
			content += chunkNo + " ";
		
		if(type.compareTo(Util.MessageType.PUTCHUNK) == 0)
			content += replicationDeg + " ";
		
		content += LINE_SEPARATOR + LINE_SEPARATOR;
		
		if(type.compareTo(Util.MessageType.PUTCHUNK) == 0 || type.compareTo(Util.MessageType.CHUNK) == 0)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try 
			{
				baos.write(content.getBytes());
				baos.write(body);
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			byte[] a = baos.toByteArray();
			
			return a;
		}
		
		return content.getBytes();
	}
	
	/*
	 * Getters and Setters
	 */

	public Util.MessageType getType() {
		return type;
	}

	public void setType(Util.MessageType type) {
		this.type = type;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	public int getReplicationDeg() {
		return replicationDeg;
	}

	public void setReplicationDeg(int replicationDeg) {
		this.replicationDeg = replicationDeg;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public char[] getVersion() {
		return version;
	}

	public void setVersion(char[] version) {
		this.version = version;
	}
	
}
