package network;

public class Message 
{
	public MessageType type = null;
	public int version = -1;
	public int senderId = -1;
	public int fileId = -1;
	public int chunkNo = -1;
	public int replicationDeg = -1;
	
	private String body = null;
	
	public static final char CR = 0xD;
	public static final char LF = 0xA;
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
		PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE,REMOVED
	}
	
	public Message(){};
	
	public Message(MessageType type, int version, int senderId, int fileId, int chunkNo, int ReplicationDeg, String body)
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
	public String buildMessage(){
		String content = (type.name() + " " + version + " " + senderId + " " + fileId + " ");
		
		if(type.compareTo(MessageType.DELETE) != 0)
			content = content.concat(chunkNo + " ");
		
		if(type.compareTo(MessageType.PUTCHUNK) == 0)
			content = content.concat(replicationDeg + " ");
		
		content = content.concat(LINE_SEPARATOR + LINE_SEPARATOR);
		
		if(type.compareTo(MessageType.PUTCHUNK) == 0|| type.compareTo(MessageType.CHUNK) == 0)
			content  = content.concat(body);
		
		return content;
	}
	
	/*
	 * Preenche os atributos da classe com os respetivos valores 
	 */
	public void parseMessage(String msg){
		String[] values = msg.split(LINE_SEPARATOR + LINE_SEPARATOR);
		String[] header = values[0].split("\\s");
		
		body = values[1];
		type = MessageType.valueOf(header[0]);
		version = Integer.parseInt(header[1]);
		senderId = Integer.parseInt(header[2]);
		fileId = Integer.parseInt(header[3]);
		
		if(type.compareTo(MessageType.DELETE) != 0)
			chunkNo = Integer.parseInt(header[4]);
		else
			chunkNo = -1;
		
		if(type.compareTo(MessageType.PUTCHUNK) == 0)
			replicationDeg = Integer.parseInt(header[5]);
		else
			replicationDeg = -1;
	}
	
}
