package network;

public class Message 
{

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
		PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE,REMOVED
	}
	
	public static final char CR = 0xD;
	public static final char LF = 0xA;
	public static final String LINE_SEPARATOR = "" + CR + LF; 
	
	/*
	 * verificar estas regras melhor
	private int version = 0;
	private int senderId = 0;
	private int fileId = 0;
	private int ChunkNo = -1; //exceto delete 
	private int ReplicationDeg = -1; //so para putchunk
	*/
	
	private byte[] body;
	
	public Message()
	{
		
	}
	
}
