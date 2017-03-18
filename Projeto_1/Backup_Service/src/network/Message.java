package network;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Message 
{
	private MessageType type = null;
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
	 * PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
	 * STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	 * GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	 * CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
	 * DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
	 * REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	 */
	
	public static enum MessageType
	{
		PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED
	}
	
	public Message(){};
	
	public Message(MessageType type, char[] version, int senderId, String fileId, int chunkNo, int ReplicationDeg, byte[] body)
	{
		this.type = type;
		this.version = version;
		this.senderId = senderId;
		this.fileId = fileId;
		this.chunkNo = chunkNo;
		this.replicationDeg = ReplicationDeg;
		this.body = body;
	}
	
	/*
	 * Constroi o conteudo de uma mensagem
	 */
	public byte[] buildMessage() {
		
		String content = type.name() + " " + version + " " + senderId + " " + fileId + " ";
		
		if(type.compareTo(MessageType.DELETE) != 0)
			content += chunkNo + " ";
		
		if(type.compareTo(MessageType.PUTCHUNK) == 0)
			content += replicationDeg + " ";
		
		content += LINE_SEPARATOR + LINE_SEPARATOR;
		
		if(type.compareTo(MessageType.PUTCHUNK) == 0 || type.compareTo(MessageType.CHUNK) == 0)
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
			
			System.out.println(a.length);
			
			return a;
		}
		
		return content.getBytes();
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}
	
}
